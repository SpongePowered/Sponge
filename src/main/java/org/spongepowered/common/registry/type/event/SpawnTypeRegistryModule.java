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
import org.spongepowered.api.registry.util.RegisterCatalog;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SpawnTypeRegistryModule implements AdditionalCatalogRegistryModule<SpawnType> {

    @RegisterCatalog(SpawnTypes.class)
    private final Map<String, SpawnType> spawnTypeMap = new HashMap<>();

    @Override
    public void registerAdditionalCatalog(SpawnType extraCatalog) {
        checkArgument(!this.spawnTypeMap.containsKey(extraCatalog.getId().toLowerCase()),
                "SpawnType with the same id is already registered: {}", extraCatalog.getId());
        this.spawnTypeMap.put(extraCatalog.getId().toLowerCase(), extraCatalog);
    }

    @Override
    public Optional<SpawnType> getById(String id) {
        return Optional.ofNullable(this.spawnTypeMap.get(checkNotNull(id, "Id cannot be null!").toLowerCase()));
    }

    @Override
    public Collection<SpawnType> getAll() {
        return ImmutableSet.copyOf(this.spawnTypeMap.values());
    }

    @Override
    public void registerDefaults() {
        this.spawnTypeMap.put("block_spawning", InternalSpawnTypes.BLOCK_SPAWNING);
        this.spawnTypeMap.put("breeding", InternalSpawnTypes.BREEDING);
        this.spawnTypeMap.put("dispense", InternalSpawnTypes.DISPENSE);
        this.spawnTypeMap.put("dropped_item", InternalSpawnTypes.DROPPED_ITEM);
        this.spawnTypeMap.put("experience", InternalSpawnTypes.EXPERIENCE);
        this.spawnTypeMap.put("falling_block", InternalSpawnTypes.FALLING_BLOCK);
        this.spawnTypeMap.put("mob_spawner", InternalSpawnTypes.MOB_SPAWNER);
        this.spawnTypeMap.put("passive", InternalSpawnTypes.PASSIVE);
        this.spawnTypeMap.put("placement", InternalSpawnTypes.PLACEMENT);
        this.spawnTypeMap.put("projectile", InternalSpawnTypes.PROJECTILE);
        this.spawnTypeMap.put("spawn_egg", InternalSpawnTypes.SPAWN_EGG);
        this.spawnTypeMap.put("structure", InternalSpawnTypes.STRUCTURE);
        this.spawnTypeMap.put("tnt_ignite", InternalSpawnTypes.TNT_IGNITE);
        this.spawnTypeMap.put("weather", InternalSpawnTypes.WEATHER);
        this.spawnTypeMap.put("custom", InternalSpawnTypes.CUSTOM);
        this.spawnTypeMap.put("chunk_load", InternalSpawnTypes.CHUNK_LOAD);
        this.spawnTypeMap.put("world_spawner", InternalSpawnTypes.WORLD_SPAWNER);
        this.spawnTypeMap.put("plugin", InternalSpawnTypes.PLUGIN);
    }
}
