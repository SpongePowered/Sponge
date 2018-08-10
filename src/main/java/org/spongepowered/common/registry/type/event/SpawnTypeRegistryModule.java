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
import org.spongepowered.api.event.cause.entity.spawn.SpawnType;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.data.type.SpongeSpawnType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class SpawnTypeRegistryModule implements AlternateCatalogRegistryModule<SpawnType>, AdditionalCatalogRegistryModule<SpawnType> {

    public static final SpawnType FORCED = generateType("forced", "Forced");
    public static final SpawnType ENTITY_DEATH = generateType("entity_death", "EntityDeath");

    @RegisterCatalog(SpawnTypes.class)
    private final Map<String, SpawnType> spawnTypeMap = new HashMap<>();

    @Override
    public void registerAdditionalCatalog(SpawnType extraCatalog) {
        checkArgument(!this.spawnTypeMap.containsKey(extraCatalog.getId().toLowerCase(Locale.ENGLISH)),
                "SpawnType with the same id is already registered: {}", extraCatalog.getId());
        this.spawnTypeMap.put(extraCatalog.getId().toLowerCase(Locale.ENGLISH), extraCatalog);
    }

    @Override
    public Optional<SpawnType> getById(String id) {
        String key = checkNotNull(id).toLowerCase(Locale.ENGLISH);
        if (!key.contains(":")) {
            key = "sponge:" + key; // There are no minecraft based spawn types.
        }
        return Optional.ofNullable(this.spawnTypeMap.get(key));
    }

    @Override
    public Collection<SpawnType> getAll() {
        return ImmutableSet.copyOf(this.spawnTypeMap.values());
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
        this.spawnTypeMap.put("sponge:" + id, generateType(id, name));
    }

    @Override
    public Map<String, SpawnType> provideCatalogMap() {
        final HashMap<String, SpawnType> map = new HashMap<>();
        for (Map.Entry<String, SpawnType> entry : this.spawnTypeMap.entrySet()) {
            map.put(entry.getKey().replace("sponge:", ""), entry.getValue());
        }
        return map;
    }
}
