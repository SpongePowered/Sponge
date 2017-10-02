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

import org.spongepowered.api.event.cause.entity.spawn.SpawnType;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;
import org.spongepowered.common.registry.Namespaces;

@RegisterCatalog(SpawnTypes.class)
public class SpawnTypeRegistryModule extends AbstractCatalogRegistryModule<SpawnType> implements AdditionalCatalogRegistryModule<SpawnType> {

    public SpawnTypeRegistryModule() {
        super(Namespaces.SPONGE);
    }

    @Override
    public void registerAdditionalCatalog(SpawnType extraCatalog) {
        checkArgument(!this.map.containsKey(extraCatalog.getId()),
                "SpawnType with the same id is already registered: {}", extraCatalog.getId());
        this.map.put(extraCatalog.getId(), extraCatalog);
    }

    @Override
    public void registerDefaults() {
        this.map.put("sponge:block_spawning", InternalSpawnTypes.BLOCK_SPAWNING);
        this.map.put("sponge:breeding", InternalSpawnTypes.BREEDING);
        this.map.put("sponge:dispense", InternalSpawnTypes.DISPENSE);
        this.map.put("sponge:dropped_item", InternalSpawnTypes.DROPPED_ITEM);
        this.map.put("sponge:experience", InternalSpawnTypes.EXPERIENCE);
        this.map.put("sponge:falling_block", InternalSpawnTypes.FALLING_BLOCK);
        this.map.put("sponge:mob_spawner", InternalSpawnTypes.MOB_SPAWNER);
        this.map.put("sponge:passive", InternalSpawnTypes.PASSIVE);
        this.map.put("sponge:placement", InternalSpawnTypes.PLACEMENT);
        this.map.put("sponge:projectile", InternalSpawnTypes.PROJECTILE);
        this.map.put("sponge:spawn_egg", InternalSpawnTypes.SPAWN_EGG);
        this.map.put("sponge:structure", InternalSpawnTypes.STRUCTURE);
        this.map.put("sponge:tnt_ignite", InternalSpawnTypes.TNT_IGNITE);
        this.map.put("sponge:weather", InternalSpawnTypes.WEATHER);
        this.map.put("sponge:custom", InternalSpawnTypes.CUSTOM);
        this.map.put("sponge:chunk_load", InternalSpawnTypes.CHUNK_LOAD);
        this.map.put("sponge:world_spawner", InternalSpawnTypes.WORLD_SPAWNER);
        this.map.put("sponge:plugin", InternalSpawnTypes.PLUGIN);
    }
}
