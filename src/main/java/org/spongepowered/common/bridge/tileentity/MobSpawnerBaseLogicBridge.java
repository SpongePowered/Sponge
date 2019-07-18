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
package org.spongepowered.common.bridge.tileentity;

import net.minecraft.entity.Entity;
import net.minecraft.util.WeightedSpawnerEntity;

import java.util.List;

public interface MobSpawnerBaseLogicBridge {

    int bridge$getSpawnDelay();

    void bridge$setSpawnDelay(int spawnDelay);

    List<WeightedSpawnerEntity> bridge$getPotentialSpawns();

    void bridge$setPotentialSpawns(List<WeightedSpawnerEntity> potentialSpawns);

    WeightedSpawnerEntity bridge$getSpawnData();

    void bridge$setSpawnData(WeightedSpawnerEntity spawnData);

    double bridge$getMobRotation();

    void bridge$setMobRotation(double mobRotation);

    double bridge$getPrevMobRotation();

    void bridge$setPrevMobRotation(double prevMobRotation);

    int bridge$getMinSpawnDelay();

    void bridge$setMinSpawnDelay(int minSpawnDelay);

    int bridge$getMaxSpawnDelay();

    void bridge$setMaxSpawnDelay(int maxSpawnDelay);

    int bridge$getSpawnCount();

    void bridge$setSpawnCount(int spawnCount);

    Entity bridge$getCachedEntity();

    void bridge$setCachedEntity(Entity cachedEntity);

    int bridge$getMaxNearbyEntities();

    void bridge$setMaxNearbyEntities(int maxNearbyEntities);

    int bridge$getActivatingRangeFromPlayer();

    void bridge$setActivatingRangeFromPlayer(int activatingRangeFromPlayer);

    int bridge$getSpawnRange();

    void bridge$setSpawnRange(int spawnRange);
}
