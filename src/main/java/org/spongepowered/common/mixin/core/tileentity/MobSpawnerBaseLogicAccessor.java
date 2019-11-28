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
package org.spongepowered.common.mixin.core.tileentity;

import net.minecraft.entity.Entity;
import net.minecraft.util.WeightedSpawnerEntity;
import net.minecraft.world.spawner.AbstractSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(AbstractSpawner.class)
public interface MobSpawnerBaseLogicAccessor {

    /** The delay to spawn. */
    @Accessor("spawnDelay") int accessor$getSpawnDelay();

    @Accessor("spawnDelay") void accessor$setSpawnDelay(int spawnDelay);

    /** List of potential entities to spawn */
    @Accessor("potentialSpawns") List<WeightedSpawnerEntity> accessor$getPotentialSpawns();

    @Accessor("spawnData") WeightedSpawnerEntity accessor$getSpawnData();

    @Accessor("minSpawnDelay") int accessor$getMinSpawnDelay();

    @Accessor("minSpawnDelay") void accessor$setMinSpawnDelay(int minSpawnDelay);

    @Accessor("maxSpawnDelay") int accessor$getMaxSpawnDelay();

    @Accessor("maxSpawnDelay") void accessor$setMaxSpawnDelay(int maxSpawnDelay);

    @Accessor("spawnCount") int accessor$getSpawnCount();

    @Accessor("spawnCount") void accessor$setSpawnCount(int spawnCount);

    @Accessor("maxNearbyEntities") int accessor$getMaxNearbyEntities();

    @Accessor("maxNearbyEntities") void accessor$setMaxNearbyEntities(int maxNearbyEntities);

    /** The distance from which a player activates the spawner. */
    @Accessor("activatingRangeFromPlayer") int accessor$getActivatingRangeFromPlayer();

    @Accessor("activatingRangeFromPlayer") void accessor$setActivatingRangeFromPlayer(int activatingRangeFromPlayer);

    /** The range coefficient for spawning entities around. */
    @Accessor("spawnRange") int accessor$getSpawnRange();

    @Accessor("spawnRange") void accessor$setSpawnRange(int spawnRange);


}
