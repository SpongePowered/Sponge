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
package org.spongepowered.common.registry.type.event;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.event.cause.entity.spawn.SpawnType;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.data.type.SpongeSpawnType;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RegisterCatalog(SpawnTypes.class)
public class SpawnTypeRegistryModule extends AbstractCatalogRegistryModule<SpawnType>
    implements AlternateCatalogRegistryModule<SpawnType>, AdditionalCatalogRegistryModule<SpawnType> {

    public static final SpawnType FORCED = generateType("forced", "Forced");
    public static final SpawnType ENTITY_DEATH = generateType("entity_death", "EntityDeath");

    @Override
    public void registerAdditionalCatalog(SpawnType extraCatalog) {
        checkArgument(!this.map.containsKey(extraCatalog.getKey()),
                "SpawnType with the same id is already registered: {}", extraCatalog.getKey().toString());
        this.map.put(extraCatalog.getKey(), extraCatalog);
    }

    @Override
    public void registerDefaults() {
        registerDefault("dispense", "Dispense");
        registerDefault("block_spawning", "BlockSpawning");
        registerDefault("breeding", "Breeding");
        registerDefault("dropped_item", "DroppedItem");
        registerDefault("experience", "Experience");
        registerDefault("falling_block", "FallingBlock");
        registerDefault("mob_spawner", "MobSpawner");
        registerDefault("passive", "Passive");
        registerDefault("placement", "Placement");
        registerDefault("projectile", "Projectile");
        registerDefault("spawn_egg", "SpawnEgg");
        registerDefault("structure", "Structure");
        registerDefault("tnt_ignite", "TNTIgnite");
        registerDefault("weather", "Weather");
        registerDefault("custom", "Custom");
        registerDefault("chunk_load", "ChunkLoad");
        registerDefault("world_spawner", "WorldSpawner");
        registerDefault("plugin", "Plugin");
        registerAdditionalCatalog(FORCED);
        registerAdditionalCatalog(ENTITY_DEATH);
    }

    private static SpawnType generateType(String id, String name) {
        return new SpongeSpawnType(id, name);
    }

    private void registerDefault(String id, String name) {
        this.map.put(CatalogKey.sponge(id), generateType(id, name));
    }

}
