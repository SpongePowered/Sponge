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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import net.minecraft.world.gen.feature.WorldGenBigMushroom;
import net.minecraft.world.gen.feature.WorldGenBigTree;
import net.minecraft.world.gen.feature.WorldGenBirchTree;
import net.minecraft.world.gen.feature.WorldGenBlockBlob;
import net.minecraft.world.gen.feature.WorldGeneratorBonusChest;
import net.minecraft.world.gen.feature.WorldGenBush;
import net.minecraft.world.gen.feature.WorldGenCactus;
import net.minecraft.world.gen.feature.WorldGenClay;
import net.minecraft.world.gen.feature.WorldGenDeadBush;
import net.minecraft.world.gen.feature.WorldGenDesertWells;
import net.minecraft.world.gen.feature.WorldGenDoublePlant;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.feature.WorldGenEndIsland;
import net.minecraft.world.gen.feature.WorldGenFire;
import net.minecraft.world.gen.feature.WorldGenFlowers;
import net.minecraft.world.gen.feature.WorldGenFossils;
import net.minecraft.world.gen.feature.WorldGenGlowStone1;
import net.minecraft.world.gen.feature.WorldGenGlowStone2;
import net.minecraft.world.gen.feature.WorldGenHellLava;
import net.minecraft.world.gen.feature.WorldGenHugeTrees;
import net.minecraft.world.gen.feature.WorldGenIcePath;
import net.minecraft.world.gen.feature.WorldGenIceSpike;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.feature.WorldGenLiquids;
import net.minecraft.world.gen.feature.WorldGenMegaJungle;
import net.minecraft.world.gen.feature.WorldGenMegaPineTree;
import net.minecraft.world.gen.feature.WorldGenMelon;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenPumpkin;
import net.minecraft.world.gen.feature.WorldGenReed;
import net.minecraft.world.gen.feature.WorldGenSand;
import net.minecraft.world.gen.feature.WorldGenSavannaTree;
import net.minecraft.world.gen.feature.WorldGenShrub;
import net.minecraft.world.gen.feature.WorldGenSpikes;
import net.minecraft.world.gen.feature.WorldGenSwamp;
import net.minecraft.world.gen.feature.WorldGenTaiga1;
import net.minecraft.world.gen.feature.WorldGenTaiga2;
import net.minecraft.world.gen.feature.WorldGenTallGrass;
import net.minecraft.world.gen.feature.WorldGenTrees;
import net.minecraft.world.gen.feature.WorldGenVines;
import net.minecraft.world.gen.feature.WorldGenWaterlily;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.util.CustomCatalogRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.common.registry.RegistryHelper;
import org.spongepowered.common.world.gen.InternalPopulatorTypes;
import org.spongepowered.common.world.gen.SpongePopulatorType;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public final class PopulatorTypeRegistryModule implements AdditionalCatalogRegistryModule<PopulatorType> {

    public static PopulatorTypeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    public final Map<Class<?>, PopulatorType> populatorClassToTypeMappings = Maps.newHashMap();

    @RegisterCatalog(PopulatorTypes.class)
    protected final Map<String, PopulatorType> populatorTypeMappings = Maps.newHashMap();

    public Function<Class<?>, PopulatorType> customTypeFunction;

    PopulatorTypeRegistryModule() {
        this.customTypeFunction = (type) -> {
            return new SpongePopulatorType(type.getSimpleName(), type.getName().contains("net.minecraft.") ? "minecraft" : "unknown");
        };
    }

    @Override
    public Optional<PopulatorType> getById(String id) {
        return Optional.ofNullable(this.populatorTypeMappings.get(checkNotNull(id).toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<PopulatorType> getAll() {
        return ImmutableList.copyOf(this.populatorTypeMappings.values());
    }

    @Override
    public void registerDefaults() {
        this.populatorTypeMappings.put("big_mushroom", new SpongePopulatorType("big_mushroom"));
        this.populatorTypeMappings.put("block_blob", new SpongePopulatorType("block_blob"));
        this.populatorTypeMappings.put("cactus", new SpongePopulatorType("cactus"));
        this.populatorTypeMappings.put("chorus_flower", new SpongePopulatorType("chorus_flower"));
        this.populatorTypeMappings.put("dead_bush", new SpongePopulatorType("dead_bush"));
        this.populatorTypeMappings.put("desert_well", new SpongePopulatorType("desert_well"));
        this.populatorTypeMappings.put("double_plant", new SpongePopulatorType("double_plant"));
        this.populatorTypeMappings.put("dungeon", new SpongePopulatorType("dungeon"));
        this.populatorTypeMappings.put("end_island", new SpongePopulatorType("end_island"));
        this.populatorTypeMappings.put("flower", new SpongePopulatorType("flower"));
        this.populatorTypeMappings.put("forest", new SpongePopulatorType("forest"));
        this.populatorTypeMappings.put("fossil", new SpongePopulatorType("fossil"));
        this.populatorTypeMappings.put("glowstone", new SpongePopulatorType("glowstone"));
        this.populatorTypeMappings.put("ice_path", new SpongePopulatorType("ice_path"));
        this.populatorTypeMappings.put("ice_spike", new SpongePopulatorType("ice_spike"));
        this.populatorTypeMappings.put("lake", new SpongePopulatorType("lake"));
        this.populatorTypeMappings.put("melon", new SpongePopulatorType("melon"));
        this.populatorTypeMappings.put("mushroom", new SpongePopulatorType("mushroom"));
        this.populatorTypeMappings.put("nether_fire", new SpongePopulatorType("nether_fire"));
        this.populatorTypeMappings.put("ore", new SpongePopulatorType("ore"));
        this.populatorTypeMappings.put("pumpkin", new SpongePopulatorType("pumpkin"));
        this.populatorTypeMappings.put("generic_block", new SpongePopulatorType("generic_block"));
        this.populatorTypeMappings.put("generic_object", new SpongePopulatorType("generic_object"));
        this.populatorTypeMappings.put("reed", new SpongePopulatorType("reed"));
        this.populatorTypeMappings.put("sea_floor", new SpongePopulatorType("sea_floor"));
        this.populatorTypeMappings.put("shrub", new SpongePopulatorType("shrub"));
        this.populatorTypeMappings.put("vine", new SpongePopulatorType("vine"));
        this.populatorTypeMappings.put("water_lily", new SpongePopulatorType("water_lily"));

        // internal
        this.populatorTypeMappings.put("animal", new SpongePopulatorType("animal"));
        this.populatorTypeMappings.put("bonus_chest", new SpongePopulatorType("bonus_chest"));
        this.populatorTypeMappings.put("end_spike", new SpongePopulatorType("end_spike"));
        this.populatorTypeMappings.put("ender_dragon", new SpongePopulatorType("ender_dragon"));
        this.populatorTypeMappings.put("plains_grass", new SpongePopulatorType("plains_grass"));
        this.populatorTypeMappings.put("snow", new SpongePopulatorType("snow"));
        this.populatorTypeMappings.put("structure", new SpongePopulatorType("structure"));
        this.populatorTypeMappings.put("unknown", new SpongePopulatorType("unknown"));

        this.populatorClassToTypeMappings.put(WorldGenBigMushroom.class, this.populatorTypeMappings.get("big_mushroom"));
        this.populatorClassToTypeMappings.put(WorldGenBigTree.class, this.populatorTypeMappings.get("forest"));
        this.populatorClassToTypeMappings.put(WorldGenBirchTree.class, this.populatorTypeMappings.get("forest"));
        this.populatorClassToTypeMappings.put(WorldGenBlockBlob.class, this.populatorTypeMappings.get("block_blob"));
        this.populatorClassToTypeMappings.put(WorldGenBush.class, this.populatorTypeMappings.get("mushroom"));
        this.populatorClassToTypeMappings.put(WorldGenCactus.class, this.populatorTypeMappings.get("cactus"));
        this.populatorClassToTypeMappings.put(WorldGenClay.class, this.populatorTypeMappings.get("sea_floor"));
        this.populatorClassToTypeMappings.put(WorldGenDeadBush.class, this.populatorTypeMappings.get("dead_bush"));
        this.populatorClassToTypeMappings.put(WorldGenDesertWells.class, this.populatorTypeMappings.get("desert_well"));
        this.populatorClassToTypeMappings.put(WorldGenDoublePlant.class, this.populatorTypeMappings.get("double_plant"));
        this.populatorClassToTypeMappings.put(WorldGenDungeons.class, this.populatorTypeMappings.get("dungeon"));
        this.populatorClassToTypeMappings.put(WorldGenEndIsland.class, this.populatorTypeMappings.get("end_island"));
        this.populatorClassToTypeMappings.put(WorldGeneratorBonusChest.class, this.populatorTypeMappings.get("bonus_chest"));
        this.populatorClassToTypeMappings.put(WorldGenFire.class, this.populatorTypeMappings.get("generic_block"));
        this.populatorClassToTypeMappings.put(WorldGenFlowers.class, this.populatorTypeMappings.get("flower"));
        this.populatorClassToTypeMappings.put(WorldGenFossils.class, this.populatorTypeMappings.get("fossil"));
        this.populatorClassToTypeMappings.put(WorldGenGlowStone1.class, this.populatorTypeMappings.get("glowstone"));
        this.populatorClassToTypeMappings.put(WorldGenGlowStone2.class, this.populatorTypeMappings.get("glowstone"));
        this.populatorClassToTypeMappings.put(WorldGenHellLava.class, this.populatorTypeMappings.get("generic_block"));
        this.populatorClassToTypeMappings.put(WorldGenHugeTrees.class, this.populatorTypeMappings.get("forest"));
        this.populatorClassToTypeMappings.put(WorldGenIcePath.class, this.populatorTypeMappings.get("ice_path"));
        this.populatorClassToTypeMappings.put(WorldGenIceSpike.class, this.populatorTypeMappings.get("ice_spike"));
        this.populatorClassToTypeMappings.put(WorldGenLakes.class, this.populatorTypeMappings.get("lake"));
        this.populatorClassToTypeMappings.put(WorldGenLiquids.class, this.populatorTypeMappings.get("generic_block"));
        this.populatorClassToTypeMappings.put(WorldGenMegaJungle.class, this.populatorTypeMappings.get("forest"));
        this.populatorClassToTypeMappings.put(WorldGenMegaPineTree.class, this.populatorTypeMappings.get("forest"));
        this.populatorClassToTypeMappings.put(WorldGenMelon.class, this.populatorTypeMappings.get("melon"));
        this.populatorClassToTypeMappings.put(WorldGenMinable.class, this.populatorTypeMappings.get("ore"));
        this.populatorClassToTypeMappings.put(WorldGenPumpkin.class, this.populatorTypeMappings.get("pumpkin"));
        this.populatorClassToTypeMappings.put(WorldGenReed.class, this.populatorTypeMappings.get("reed"));
        this.populatorClassToTypeMappings.put(WorldGenSand.class, this.populatorTypeMappings.get("sea_floor"));
        this.populatorClassToTypeMappings.put(WorldGenSavannaTree.class, this.populatorTypeMappings.get("forest"));
        this.populatorClassToTypeMappings.put(WorldGenShrub.class, this.populatorTypeMappings.get("forest"));
        this.populatorClassToTypeMappings.put(WorldGenSpikes.class, this.populatorTypeMappings.get("ender_crystal_platform"));
        this.populatorClassToTypeMappings.put(WorldGenSwamp.class, this.populatorTypeMappings.get("forest"));
        this.populatorClassToTypeMappings.put(WorldGenTaiga1.class, this.populatorTypeMappings.get("forest"));
        this.populatorClassToTypeMappings.put(WorldGenTaiga2.class, this.populatorTypeMappings.get("forest"));
        this.populatorClassToTypeMappings.put(WorldGenTallGrass.class, this.populatorTypeMappings.get("shrub"));
        this.populatorClassToTypeMappings.put(WorldGenTrees.class, this.populatorTypeMappings.get("forest"));
        this.populatorClassToTypeMappings.put(WorldGenVines.class, this.populatorTypeMappings.get("vine"));
        this.populatorClassToTypeMappings.put(WorldGenWaterlily.class, this.populatorTypeMappings.get("water_lily"));
    }

    @Override
    public void registerAdditionalCatalog(PopulatorType extraCatalog) {
        checkNotNull(extraCatalog, "CatalogType cannot be null");
        checkArgument(!extraCatalog.getId().isEmpty(), "Id cannot be empty");
        checkArgument(!this.populatorTypeMappings.containsKey(extraCatalog.getId()), "Duplicate Id");
        this.populatorTypeMappings.put(extraCatalog.getId(), extraCatalog);
    }

    public void registerClassMapping(Class<? extends net.minecraft.world.gen.feature.WorldGenerator> generator, PopulatorType type) {
        this.populatorClassToTypeMappings.put(generator, type);
    }

    @CustomCatalogRegistration
    public void registerCatalogs() {
        registerDefaults();
        RegistryHelper.mapFields(PopulatorTypes.class, this.populatorTypeMappings);
        RegistryHelper.mapFields(InternalPopulatorTypes.class, this.populatorTypeMappings);
    }

    public boolean hasRegistrationFor(Class<?> cls) {
        return this.populatorClassToTypeMappings.containsKey(cls);
    }

    public PopulatorType getForClass(Class<?> cls) {
        return this.populatorClassToTypeMappings.get(cls);
    }

    public PopulatorType getOrCreateForType(Class<?> cls) {
        if (hasRegistrationFor(cls)) {
            return getForClass(cls);
        }
        PopulatorType type = this.customTypeFunction.apply(cls);
        if (type == null) {
            return InternalPopulatorTypes.UNKNOWN;
        }
        registerAdditionalCatalog(type);
        this.populatorClassToTypeMappings.put(cls, type);
        return type;
    }

    private static final class Holder {

        static final PopulatorTypeRegistryModule INSTANCE = new PopulatorTypeRegistryModule();
    }
}
