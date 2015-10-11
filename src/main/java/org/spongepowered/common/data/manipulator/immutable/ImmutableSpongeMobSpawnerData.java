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
package org.spongepowered.common.data.manipulator.immutable;

import static org.spongepowered.common.data.util.ComparatorUtil.shortComparator;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableMobSpawnerData;
import org.spongepowered.api.data.manipulator.mutable.MobSpawnerData;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.api.data.value.immutable.ImmutableWeightedEntityCollectionValue;
import org.spongepowered.api.util.weighted.WeightedCollection;
import org.spongepowered.api.util.weighted.WeightedEntity;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.SpongeMobSpawnerData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeBoundedValue;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeEntityToSpawnValue;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeWeightedEntityCollectionValue;

public class ImmutableSpongeMobSpawnerData extends AbstractImmutableData<ImmutableMobSpawnerData, MobSpawnerData> implements ImmutableMobSpawnerData {

    private final short remaining;
    private final short minSpawnDelay;
    private final short maxSpawnDelay;
    private final short count;
    private final short maxNearby;
    private final short playerRange;
    private final short spawnRange;
    private final WeightedEntity nextToSpawn;
    private final WeightedCollection<WeightedEntity> entitiesToSpawn;

    public ImmutableSpongeMobSpawnerData(short remaining, short minSpawnDelay, short maxSpawnDelay, short count, short maxNearby,
                                         short playerRange, short spawnRange, WeightedEntity nextToSpawn,
                                         WeightedCollection<WeightedEntity> entitiesToSpawn) {
        super(ImmutableMobSpawnerData.class);
        this.remaining = remaining;
        this.minSpawnDelay = minSpawnDelay;
        this.maxSpawnDelay = maxSpawnDelay;
        this.count = count;
        this.maxNearby = maxNearby;
        this.playerRange = playerRange;
        this.spawnRange = spawnRange;
        this.nextToSpawn = nextToSpawn;
        this.entitiesToSpawn = entitiesToSpawn;
        registerGetters();
    }

    @Override
    public ImmutableBoundedValue<Short> remainingDelay() {
        return ImmutableSpongeBoundedValue.cachedOf(Keys.SPAWNER_REMAINING_DELAY, (short) 0, this.remaining, shortComparator(), (short) 0, Short.MAX_VALUE);
    }

    @Override
    public ImmutableBoundedValue<Short> minimumSpawnDelay() {
        return ImmutableSpongeBoundedValue.cachedOf(Keys.SPAWNER_MINIMUM_DELAY, (short) 0, this.minSpawnDelay, shortComparator(), (short) 0, Short.MAX_VALUE);
    }

    @Override
    public ImmutableBoundedValue<Short> maximumSpawnDelay() {
        return ImmutableSpongeBoundedValue.cachedOf(Keys.SPAWNER_MAXIMUM_DELAY, (short) 0, this.maxSpawnDelay, shortComparator(), (short) 0, Short.MAX_VALUE);
    }

    @Override
    public ImmutableBoundedValue<Short> spawnCount() {
        return ImmutableSpongeBoundedValue.cachedOf(Keys.SPAWNER_SPAWN_COUNT, (short) 0, this.count, shortComparator(), (short) 0, Short.MAX_VALUE);
    }

    @Override
    public ImmutableBoundedValue<Short> maximumNearbyEntities() {
        return ImmutableSpongeBoundedValue.cachedOf(Keys.SPAWNER_MAXIMUM_NEARBY_ENTITIES, (short) 0, this.maxNearby, shortComparator(), (short) 0, Short.MAX_VALUE);
    }

    @Override
    public ImmutableBoundedValue<Short> requiredPlayerRange() {
        return ImmutableSpongeBoundedValue.cachedOf(Keys.SPAWNER_REQURED_PLAYER_RANGE, (short) 0, this.playerRange, shortComparator(), (short) 0, Short.MAX_VALUE);
    }

    @Override
    public ImmutableBoundedValue<Short> spawnRange() {
        return ImmutableSpongeBoundedValue.cachedOf(Keys.SPAWNER_SPAWN_RANGE, (short) 0, this.spawnRange, shortComparator(), (short) 0, Short.MAX_VALUE);
    }

    @Override
    public ImmutableNextEntityToSpawnValue nextEntityToSpawn() {
        return new ImmutableSpongeEntityToSpawnValue(this.nextToSpawn);
    }

    @Override
    public ImmutableWeightedEntityCollectionValue possibleEntitiesToSpawn() {
        return new ImmutableSpongeWeightedEntityCollectionValue(Keys.SPAWNER_ENTITIES, this.entitiesToSpawn);
    }

    @Override
    public MobSpawnerData asMutable() {
        return new SpongeMobSpawnerData(this.remaining, this.minSpawnDelay, this.maxSpawnDelay, this.count, this.maxNearby, this.playerRange, this.spawnRange, this.nextToSpawn, this.entitiesToSpawn);
    }

    @Override
    public int compareTo(ImmutableMobSpawnerData o) {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
            .set(Keys.SPAWNER_REMAINING_DELAY, this.remaining)
            .set(Keys.SPAWNER_MINIMUM_DELAY, this.minSpawnDelay)
            .set(Keys.SPAWNER_MAXIMUM_DELAY, this.maxSpawnDelay)
            .set(Keys.SPAWNER_SPAWN_COUNT, this.count)
            .set(Keys.SPAWNER_MAXIMUM_NEARBY_ENTITIES, this.maxNearby)
            .set(Keys.SPAWNER_REQURED_PLAYER_RANGE, this.playerRange)
            .set(Keys.SPAWNER_SPAWN_RANGE, this.spawnRange)
            .set(Keys.SPAWNER_NEXT_ENTITY_TO_SPAWN, this.nextToSpawn)
            .set(Keys.SPAWNER_ENTITIES, this.entitiesToSpawn);
    }

    @Override
    protected void registerGetters() {
        // TODO
    }
}
