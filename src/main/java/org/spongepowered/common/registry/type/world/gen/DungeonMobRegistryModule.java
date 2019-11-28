/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.registry.type.world.gen;

import com.google.common.collect.Lists;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.DungeonsFeature;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.registry.RegistryModule;
import org.spongepowered.api.registry.util.RegistrationDependency;
import org.spongepowered.api.util.weighted.WeightedSerializableObject;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.SpongeEntityArchetypeBuilder;
import org.spongepowered.common.mixin.core.world.gen.feature.WorldGenDungeonsAccessor;
import org.spongepowered.common.registry.type.entity.EntityTypeRegistryModule;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RegistrationDependency(EntityTypeRegistryModule.class)
public class DungeonMobRegistryModule implements RegistryModule {

    // We use a non-catalog registry to allow Forge to add mobs from DungeonHooks
    private final WeightedTable<EntityArchetype> dungeonMobs = new WeightedTable<>();

    private final List<EntityType> presentTypes = Lists.newArrayList();

    public static DungeonMobRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void registerDefaults() {
        final DungeonsFeature worldGenDungeons = new DungeonsFeature();
        final Map<String, Long> types = Arrays.stream(((WorldGenDungeonsAccessor) worldGenDungeons).accessor$getSpawnerTypes())
                .collect(Collectors.groupingBy(ResourceLocation::toString, Collectors.counting()));

        for(final String mob : types.keySet()) {
            put(EntityUtil.fromNameToType(mob).get(),
                    types.get(mob).intValue() * 100); // times 100 to fit with forge's format
        }
    }

    /**
     * <p>Puts the given type into the registry (converted into an {@link EntityArchetype}
     * with the given weight.</p>
     *
     * <p>If a given type already exists, the old weight will be overridden.</p>
     * @param type Type to add
     * @param weight Weight of the type
     */
    public void put(final EntityType type, final int weight) {
        remove(type);

        this.dungeonMobs.add(new WeightedSerializableObject<>(new SpongeEntityArchetypeBuilder().type(type).build(), weight));
        this.presentTypes.add(type);
    }

    /**
     * Removes the given type from the registry.
     *
     * @param type Type to remove
     */
    public void remove(final EntityType type) {
        get(type).ifPresent(this.dungeonMobs::remove);
        this.presentTypes.remove(type);
    }

    /**
     * Gets the {@link WeightedSerializableObject}{@code <EntityArchetype>} for the given
     * {@link EntityType}, if one exists.
     * @param type Type to find
     * @return Weighed archetype, if one exists
     */
    public Optional<WeightedSerializableObject<EntityArchetype>> get(final EntityType type) {
        return this.dungeonMobs
                .getEntries()
                .stream()
                .map(entry -> (WeightedSerializableObject<EntityArchetype>) entry)
                .findFirst();
    }

    public WeightedTable<EntityArchetype> getRaw() {
        return this.dungeonMobs;
    }

    private static final class Holder {

        static final DungeonMobRegistryModule INSTANCE = new DungeonMobRegistryModule();
    }
}
