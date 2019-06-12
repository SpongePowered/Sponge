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

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.common.data.value.SpongeValueFactory.boundedBuilder;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableMobSpawnerData;
import org.spongepowered.api.data.manipulator.mutable.MobSpawnerData;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.immutable.ImmutableWeightedCollectionValue;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.util.weighted.WeightedSerializableObject;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.SpongeMobSpawnerData;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeWeightedCollectionValue;

import java.util.stream.Collectors;

public class ImmutableSpongeMobSpawnerData extends AbstractImmutableData<ImmutableMobSpawnerData, MobSpawnerData> implements ImmutableMobSpawnerData {

    private final short remaining;
    private final short minSpawnDelay;
    private final short maxSpawnDelay;
    private final short count;
    private final short maxNearby;
    private final short playerRange;
    private final short spawnRange;
    private final WeightedSerializableObject<EntityArchetype> nextToSpawn;
    private final WeightedTable<EntityArchetype> entitiesToSpawn;

    private final ImmutableBoundedValue<Short> remainingValue;
    private final ImmutableBoundedValue<Short> minValue;
    private final ImmutableBoundedValue<Short> maxValue;
    private final ImmutableBoundedValue<Short> countValue;
    private final ImmutableBoundedValue<Short> nearbyValue;
    private final ImmutableBoundedValue<Short> playerRangeValue;
    private final ImmutableBoundedValue<Short> spawnRangeValue;
    private final ImmutableValue<WeightedSerializableObject<EntityArchetype>> nextValue;
    private final ImmutableWeightedCollectionValue<EntityArchetype> toSpawnValue;

    public ImmutableSpongeMobSpawnerData() {
        this(Constants.TileEntity.Spawner.DEFAULT_REMAINING_DELAY, Constants.TileEntity.Spawner.DEFAULT_MINIMUM_SPAWN_DELAY,
                Constants.TileEntity.Spawner.DEFAULT_MAXIMUM_SPAWN_DELAY, Constants.TileEntity.Spawner.DEFAULT_SPAWN_COUNT,
                Constants.TileEntity.Spawner.DEFAULT_MAXMIMUM_NEARBY_ENTITIES, Constants.TileEntity.Spawner.DEFAULT_REQUIRED_PLAYER_RANGE,
                Constants.TileEntity.Spawner.DEFAULT_SPAWN_RANGE, Constants.TileEntity.Spawner.DEFAULT_NEXT_ENTITY_TO_SPAWN, new WeightedTable<>());
    }

    public ImmutableSpongeMobSpawnerData(short remaining, short minSpawnDelay, short maxSpawnDelay, short count, short maxNearby,
                                         short playerRange, short spawnRange, WeightedSerializableObject<EntityArchetype> nextToSpawn,
                                         WeightedTable<EntityArchetype> entitiesToSpawn) {
        super(ImmutableMobSpawnerData.class);
        this.remaining = remaining;
        this.minSpawnDelay = minSpawnDelay;
        this.maxSpawnDelay = maxSpawnDelay;
        this.count = count;
        this.maxNearby = maxNearby;
        this.playerRange = playerRange;
        this.spawnRange = spawnRange;
        this.nextToSpawn = checkNotNull(nextToSpawn);
        this.entitiesToSpawn = checkNotNull(entitiesToSpawn).stream().collect(Collectors.toCollection(WeightedTable<EntityArchetype>::new));

        this.remainingValue = boundedBuilder(Keys.SPAWNER_REMAINING_DELAY)
                .defaultValue((short) 20)
                .minimum((short) 0)
                .maximum(Short.MAX_VALUE)
                .actualValue(remaining)
                .build()
                .asImmutable();
        this.minValue = boundedBuilder(Keys.SPAWNER_MINIMUM_DELAY)
                .defaultValue((short) 200)
                .minimum((short) 0)
                .maximum(Short.MAX_VALUE)
                .actualValue(minSpawnDelay)
                .build()
                .asImmutable();
        this.maxValue = boundedBuilder(Keys.SPAWNER_MAXIMUM_DELAY)
                .defaultValue((short) 800)
                .minimum((short) 1)
                .maximum(Short.MAX_VALUE)
                .actualValue(maxSpawnDelay)
                .build()
                .asImmutable();
        this.countValue = boundedBuilder(Keys.SPAWNER_SPAWN_COUNT)
                .defaultValue((short) 4)
                .minimum((short) 0)
                .maximum(Short.MAX_VALUE)
                .actualValue(count)
                .build()
                .asImmutable();
        this.nearbyValue = boundedBuilder(Keys.SPAWNER_MAXIMUM_NEARBY_ENTITIES)
                .defaultValue((short) 6)
                .minimum((short) 0)
                .maximum(Short.MAX_VALUE)
                .actualValue(maxNearby)
                .build()
                .asImmutable();
        this.playerRangeValue = boundedBuilder(Keys.SPAWNER_REQUIRED_PLAYER_RANGE)
                .defaultValue((short) 16)
                .minimum((short) 0)
                .maximum(Short.MAX_VALUE)
                .actualValue(playerRange)
                .build()
                .asImmutable();
        this.spawnRangeValue = boundedBuilder(Keys.SPAWNER_SPAWN_RANGE)
                .defaultValue((short) 4)
                .minimum((short) 0)
                .maximum(Short.MAX_VALUE)
                .actualValue(spawnRange)
                .build()
                .asImmutable();
        this.nextValue = new ImmutableSpongeValue<>(Keys.SPAWNER_NEXT_ENTITY_TO_SPAWN, Constants.TileEntity.Spawner.DEFAULT_NEXT_ENTITY_TO_SPAWN,
                this.nextToSpawn);
        this.toSpawnValue = new ImmutableSpongeWeightedCollectionValue<>(Keys.SPAWNER_ENTITIES, this.entitiesToSpawn);

        registerGetters();
    }

    @Override
    public ImmutableBoundedValue<Short> remainingDelay() {
        return this.remainingValue;
    }

    @Override
    public ImmutableBoundedValue<Short> minimumSpawnDelay() {
        return this.minValue;
    }

    @Override
    public ImmutableBoundedValue<Short> maximumSpawnDelay() {
        return this.maxValue;
    }

    @Override
    public ImmutableBoundedValue<Short> spawnCount() {
        return this.countValue;
    }

    @Override
    public ImmutableBoundedValue<Short> maximumNearbyEntities() {
        return this.nearbyValue;
    }

    @Override
    public ImmutableBoundedValue<Short> requiredPlayerRange() {
        return this.playerRangeValue;
    }

    @Override
    public ImmutableBoundedValue<Short> spawnRange() {
        return this.spawnRangeValue;
    }

    @Override
    public ImmutableValue<WeightedSerializableObject<EntityArchetype>> nextEntityToSpawn() {
        return this.nextValue;
    }

    @Override
    public ImmutableWeightedCollectionValue<EntityArchetype> possibleEntitiesToSpawn() {
        return this.toSpawnValue;
    }

    @Override
    public MobSpawnerData asMutable() {
        return new SpongeMobSpawnerData(this.remaining,
                                        this.minSpawnDelay,
                                        this.maxSpawnDelay,
                                        this.count,
                                        this.maxNearby,
                                        this.playerRange,
                                        this.spawnRange,
                                        this.nextToSpawn,
                                        this.entitiesToSpawn);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
            .set(Keys.SPAWNER_REMAINING_DELAY, this.remaining)
            .set(Keys.SPAWNER_MINIMUM_DELAY, this.minSpawnDelay)
            .set(Keys.SPAWNER_MAXIMUM_DELAY, this.maxSpawnDelay)
            .set(Keys.SPAWNER_SPAWN_COUNT, this.count)
            .set(Keys.SPAWNER_MAXIMUM_NEARBY_ENTITIES, this.maxNearby)
            .set(Keys.SPAWNER_REQUIRED_PLAYER_RANGE, this.playerRange)
            .set(Keys.SPAWNER_SPAWN_RANGE, this.spawnRange)
            .set(Keys.SPAWNER_NEXT_ENTITY_TO_SPAWN, this.nextToSpawn)
            .set(Keys.SPAWNER_ENTITIES, this.entitiesToSpawn);
    }

    @Override
    protected void registerGetters() {
        registerKeyValue(Keys.SPAWNER_REMAINING_DELAY, ImmutableSpongeMobSpawnerData.this::remainingDelay);
        registerKeyValue(Keys.SPAWNER_MINIMUM_DELAY, ImmutableSpongeMobSpawnerData.this::minimumSpawnDelay);
        registerKeyValue(Keys.SPAWNER_MAXIMUM_DELAY, ImmutableSpongeMobSpawnerData.this::maximumSpawnDelay);
        registerKeyValue(Keys.SPAWNER_SPAWN_COUNT, ImmutableSpongeMobSpawnerData.this::spawnCount);
        registerKeyValue(Keys.SPAWNER_MAXIMUM_NEARBY_ENTITIES, ImmutableSpongeMobSpawnerData.this::maximumNearbyEntities);
        registerKeyValue(Keys.SPAWNER_REQUIRED_PLAYER_RANGE, ImmutableSpongeMobSpawnerData.this::requiredPlayerRange);
        registerKeyValue(Keys.SPAWNER_SPAWN_RANGE, ImmutableSpongeMobSpawnerData.this::spawnRange);
        registerKeyValue(Keys.SPAWNER_NEXT_ENTITY_TO_SPAWN, ImmutableSpongeMobSpawnerData.this::nextEntityToSpawn);
        registerKeyValue(Keys.SPAWNER_ENTITIES, ImmutableSpongeMobSpawnerData.this::possibleEntitiesToSpawn);

        registerFieldGetter(Keys.SPAWNER_REMAINING_DELAY, () -> this.remaining);
        registerFieldGetter(Keys.SPAWNER_MINIMUM_DELAY, () -> this.minSpawnDelay);
        registerFieldGetter(Keys.SPAWNER_MAXIMUM_DELAY, () -> this.maxSpawnDelay);
        registerFieldGetter(Keys.SPAWNER_SPAWN_COUNT, () -> this.count);
        registerFieldGetter(Keys.SPAWNER_MAXIMUM_NEARBY_ENTITIES, () -> this.maxNearby);
        registerFieldGetter(Keys.SPAWNER_REQUIRED_PLAYER_RANGE, () -> this.playerRange);
        registerFieldGetter(Keys.SPAWNER_SPAWN_RANGE, () -> this.spawnRange);
        registerFieldGetter(Keys.SPAWNER_NEXT_ENTITY_TO_SPAWN, () -> this.nextToSpawn);
        registerFieldGetter(Keys.SPAWNER_ENTITIES, () -> this.entitiesToSpawn);

    }
}
