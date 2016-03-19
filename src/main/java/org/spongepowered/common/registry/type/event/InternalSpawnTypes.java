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

import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnType;
import org.spongepowered.common.data.type.SpongeSpawnType;
import org.spongepowered.common.event.spawn.SpongeSpawnCause;
import org.spongepowered.common.event.spawn.SpongeSpawnCauseBuilder;

public final class InternalSpawnTypes {

    public static final SpawnType BLOCK_SPAWNING = new SpongeSpawnType("BlockSpawning");
    public static final SpawnType BREEDING = new SpongeSpawnType("Breeding");
    public static final SpawnType DISPENSE = new SpongeSpawnType("Dispense");
    public static final SpawnType DROPPED_ITEM = new SpongeSpawnType("DroppedItem");
    public static final SpawnType EXPERIENCE = new SpongeSpawnType("Experience");
    public static final SpawnType FALLING_BLOCK = new SpongeSpawnType("FallingBlock");
    public static final SpawnType MOB_SPAWNER = new SpongeSpawnType("MobSpawner");
    public static final SpawnType PASSIVE = new SpongeSpawnType("Passive");
    public static final SpawnType PLACEMENT = new SpongeSpawnType("Placement");
    public static final SpawnType PROJECTILE = new SpongeSpawnType("Projectile");
    public static final SpawnType SPAWN_EGG = new SpongeSpawnType("SpawnEgg");
    public static final SpawnType STRUCTURE = new SpongeSpawnType("Structure");
    public static final SpawnType TNT_IGNITE = new SpongeSpawnType("TNT");
    public static final SpawnType WEATHER = new SpongeSpawnType("Weather");
    public static final SpawnType CUSTOM = new SpongeSpawnType("Custom");
    public static final SpawnType CHUNK_LOAD = new SpongeSpawnType("ChunkLoad");
    public static final SpawnType WORLD_SPAWNER = new SpongeSpawnType("WorldSpawner");
    public static final SpawnType PLUGIN = new SpongeSpawnType("Plugin");
    public static final SpawnType FORCED = new SpongeSpawnType("FORCED");
    public static final SpawnType ENTITY_DEATH = new SpongeSpawnType("EntityDeath");

    // SpawnCauses used
    public static final SpawnCause FORCED_SPAWN = of(InternalSpawnTypes.FORCED);
    public static final SpawnCause UNKNOWN_DISPENSE_SPAWN_CAUSE = of(InternalSpawnTypes.DISPENSE);
    public static final SpawnCause STRUCTURE_SPAWNING = of(InternalSpawnTypes.STRUCTURE);
    public static final SpawnCause CUSTOM_SPAWN = of(InternalSpawnTypes.CUSTOM);
    public static final SpawnCause WORLD_SPAWNER_CAUSE = of(InternalSpawnTypes.WORLD_SPAWNER);

    private static SpawnCause of(SpawnType spawnType) {
        return new SpongeSpawnCauseBuilder().type(spawnType).build();
    }

    private InternalSpawnTypes() {
    }

}
