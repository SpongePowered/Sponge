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
package org.spongepowered.common.data.manipulator.mutable;

import static org.spongepowered.common.data.util.ComparatorUtil.shortComparator;

import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableMobSpawnerData;
import org.spongepowered.api.data.manipulator.mutable.MobSpawnerData;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.data.value.mutable.WeightedEntityCollectionValue;
import org.spongepowered.api.util.weighted.WeightedCollection;
import org.spongepowered.api.util.weighted.WeightedEntity;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeMobSpawnerData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.mutable.SpongeBoundedValue;
import org.spongepowered.common.data.value.mutable.SpongeNextEntityToSpawnValue;
import org.spongepowered.common.data.value.mutable.SpongeWeightedEntityCollectionValue;

public class SpongeMobSpawnerData extends AbstractData<MobSpawnerData, ImmutableMobSpawnerData> implements MobSpawnerData {

    private short remainingDelay;
    private short minimumDelay;
    private short maximumDelay;
    private short count;
    private short maximumEntities;
    private short playerRange;
    private short spawnRange;
    private WeightedEntity nextEntityToSpawn;
    private WeightedCollection<WeightedEntity> entities;

    public SpongeMobSpawnerData(short remainingDelay, short minimumDelay, short maximumDelay, short count,
                                short maximumEntities, short playerRange, short spawnRange,
                                WeightedEntity nextEntityToSpawn,
                                WeightedCollection<WeightedEntity> entities) {
        super(MobSpawnerData.class);
        this.remainingDelay = remainingDelay;
        this.minimumDelay = minimumDelay;
        this.maximumDelay = maximumDelay;
        this.count = count;
        this.maximumEntities = maximumEntities;
        this.playerRange = playerRange;
        this.spawnRange = spawnRange;
        this.nextEntityToSpawn = nextEntityToSpawn;
        this.entities = entities;
        registerGettersAndSetters();
    }

    public SpongeMobSpawnerData() {
        super(MobSpawnerData.class);
    }


    @Override
    public MutableBoundedValue<Short> remainingDelay() {
        return new SpongeBoundedValue<Short>(Keys.SPAWNER_REMAINING_DELAY, (short) 0, shortComparator(), (short) 0, this.maximumDelay, this.remainingDelay);
    }

    @Override
    public MutableBoundedValue<Short> minimumSpawnDelay() {
        return new SpongeBoundedValue<Short>(Keys.SPAWNER_MINIMUM_DELAY, (short) 0, shortComparator(), (short) 0, Short.MAX_VALUE, this.minimumDelay);
    }

    @Override
    public MutableBoundedValue<Short> maximumSpawnDelay() {
        return new SpongeBoundedValue<Short>(Keys.SPAWNER_MINIMUM_DELAY, (short) 0, shortComparator(), (short) 0, Short.MAX_VALUE, this.maximumDelay);
    }

    @Override
    public MutableBoundedValue<Short> spawnCount() {
        return new SpongeBoundedValue<Short>(Keys.SPAWNER_SPAWN_COUNT, (short) 0, shortComparator(), (short) 0, Short.MAX_VALUE, this.count);
    }

    @Override
    public MutableBoundedValue<Short> maximumNearbyEntities() {
        return new SpongeBoundedValue<Short>(Keys.SPAWNER_MAXIMUM_NEARBY_ENTITIES, (short) 0, shortComparator(), (short) 0, Short.MAX_VALUE, this.maximumEntities);
    }

    @Override
    public MutableBoundedValue<Short> requiredPlayerRange() {
        return new SpongeBoundedValue<Short>(Keys.SPAWNER_REQURED_PLAYER_RANGE, (short) 0, shortComparator(), (short) 0, Short.MAX_VALUE, this.playerRange);
    }

    @Override
    public MutableBoundedValue<Short> spawnRange() {
        return new SpongeBoundedValue<Short>(Keys.SPAWNER_SPAWN_RANGE, (short) 0, shortComparator(), (short) 0, Short.MAX_VALUE, this.spawnRange);
    }

    @Override
    public NextEntityToSpawnValue nextEntityToSpawn() {
        return new SpongeNextEntityToSpawnValue(this.nextEntityToSpawn);
    }

    @Override
    public WeightedEntityCollectionValue possibleEntitiesToSpawn() {
        return new SpongeWeightedEntityCollectionValue(Keys.SPAWNER_ENTITIES, this.entities);
    }

    @Override
    public MobSpawnerData copy() {
        return new SpongeMobSpawnerData(this.remainingDelay, this.minimumDelay, this.maximumDelay, this.count, this.maximumEntities, this.playerRange,
                                        this.spawnRange, this.nextEntityToSpawn, this.entities);
    }

    @Override
    public ImmutableMobSpawnerData asImmutable() {
        return new ImmutableSpongeMobSpawnerData(this.remainingDelay, this.minimumDelay, this.maximumDelay, this.count, this.maximumEntities,
                                                 this.playerRange, this.spawnRange, this.nextEntityToSpawn, this.entities);
    }

    @Override
    public int compareTo(MobSpawnerData o) {
        return ComparisonChain.start()
                .compare(o.remainingDelay().get().intValue(), this.remainingDelay)
                .compare(o.minimumSpawnDelay().get().intValue(), this.minimumDelay)
                .compare(o.maximumSpawnDelay().get().intValue(), this.maximumDelay)
                .compare(o.maximumNearbyEntities().get().intValue(), this.maximumEntities)
                .compare(o.spawnCount().get().intValue(), this.count)
                .compare(o.requiredPlayerRange().get().intValue(), this.playerRange)
                .compare(o.spawnRange().get().intValue(), this.spawnRange)
                .result();
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
            .set(Keys.SPAWNER_REMAINING_DELAY.getQuery(), this.remainingDelay)
            .set(Keys.SPAWNER_MINIMUM_DELAY.getQuery(), this.minimumDelay)
            .set(Keys.SPAWNER_MAXIMUM_DELAY.getQuery(), this.maximumDelay)
            .set(Keys.SPAWNER_SPAWN_COUNT.getQuery(), this.count)
            .set(Keys.SPAWNER_MAXIMUM_NEARBY_ENTITIES.getQuery(), this.maximumEntities)
            .set(Keys.SPAWNER_REQURED_PLAYER_RANGE.getQuery(), this.playerRange)
            .set(Keys.SPAWNER_SPAWN_RANGE.getQuery(), this.spawnRange)
            .set(Keys.SPAWNER_NEXT_ENTITY_TO_SPAWN.getQuery(), this.nextEntityToSpawn)
            .set(Keys.SPAWNER_ENTITIES.getQuery(), this.entities);

    }

    @Override
    protected void registerGettersAndSetters() {
        // TODO
    }
}
