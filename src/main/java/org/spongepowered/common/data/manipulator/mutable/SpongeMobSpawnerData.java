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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableMobSpawnerData;
import org.spongepowered.api.data.manipulator.mutable.MobSpawnerData;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.data.value.mutable.WeightedCollectionValue;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.util.weighted.WeightedSerializableObject;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeMobSpawnerData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.data.value.mutable.SpongeNextEntityToSpawnValue;
import org.spongepowered.common.data.value.mutable.SpongeWeightedCollectionValue;
import org.spongepowered.common.entity.SpongeEntitySnapshotBuilder;

import java.util.stream.Collectors;

public class SpongeMobSpawnerData extends AbstractData<MobSpawnerData, ImmutableMobSpawnerData> implements MobSpawnerData {

    private short remainingDelay;
    private short minimumDelay;
    private short maximumDelay;
    private short count;
    private short maximumEntities;
    private short playerRange;
    private short spawnRange;
    private WeightedSerializableObject<EntitySnapshot> nextEntityToSpawn;
    private WeightedTable<EntitySnapshot> entities;

    public SpongeMobSpawnerData(short remainingDelay, short minimumDelay, short maximumDelay, short count,
                                short maximumEntities, short playerRange, short spawnRange,
                                WeightedSerializableObject<EntitySnapshot> nextEntityToSpawn,
                                WeightedTable<EntitySnapshot> entities) {
        super(MobSpawnerData.class);
        this.remainingDelay = remainingDelay;
        this.minimumDelay = minimumDelay;
        this.maximumDelay = maximumDelay;
        this.count = count;
        this.maximumEntities = maximumEntities;
        this.playerRange = playerRange;
        this.spawnRange = spawnRange;
        this.nextEntityToSpawn = checkNotNull(nextEntityToSpawn);
        checkNotNull(entities).forEach(Preconditions::checkNotNull);
        this.entities = entities.stream().collect(Collectors.toCollection(WeightedTable<EntitySnapshot>::new));
        registerGettersAndSetters();
    }

    public SpongeMobSpawnerData() {
        this((short) 20, (short) 200, (short) 800, (short) 4, (short) 6, (short) 16, (short) 4,
                new WeightedSerializableObject<>(new SpongeEntitySnapshotBuilder().type(EntityTypes.PIG).build(), 1),
                new WeightedTable<>());
    }


    @Override
    public MutableBoundedValue<Short> remainingDelay() {
        return SpongeValueFactory.boundedBuilder(Keys.SPAWNER_REMAINING_DELAY)
            .minimum((short) 0)
            .maximum(this.maximumDelay)
            .defaultValue((short) 20)
            .actualValue(this.remainingDelay)
            .build();
    }

    @Override
    public MutableBoundedValue<Short> minimumSpawnDelay() {
        return SpongeValueFactory.boundedBuilder(Keys.SPAWNER_MINIMUM_DELAY)
            .minimum((short) 0)
            .maximum(Short.MAX_VALUE)
            .defaultValue((short) 200)
            .actualValue(this.minimumDelay)
            .build();
    }

    @Override
    public MutableBoundedValue<Short> maximumSpawnDelay() {
        return SpongeValueFactory.boundedBuilder(Keys.SPAWNER_MAXIMUM_DELAY)
            .minimum((short) 1)
            .maximum(Short.MAX_VALUE)
            .defaultValue((short) 800)
            .actualValue(this.maximumDelay)
            .build();
    }

    @Override
    public MutableBoundedValue<Short> spawnCount() {
        return SpongeValueFactory.boundedBuilder(Keys.SPAWNER_SPAWN_COUNT)
            .minimum((short) 0)
            .maximum(Short.MAX_VALUE)
            .defaultValue((short) 4)
            .actualValue(this.count)
            .build();
    }

    @Override
    public MutableBoundedValue<Short> maximumNearbyEntities() {
        return SpongeValueFactory.boundedBuilder(Keys.SPAWNER_MAXIMUM_NEARBY_ENTITIES)
            .minimum((short) 0)
            .maximum(Short.MAX_VALUE)
            .defaultValue((short) 6)
            .actualValue(this.maximumEntities)
            .build();
    }

    @Override
    public MutableBoundedValue<Short> requiredPlayerRange() {
        return SpongeValueFactory.boundedBuilder(Keys.SPAWNER_REQUIRED_PLAYER_RANGE)
            .minimum((short) 0)
            .maximum(Short.MAX_VALUE)
            .defaultValue((short) 16)
            .actualValue(this.playerRange)
            .build();
    }

    @Override
    public MutableBoundedValue<Short> spawnRange() {
        return SpongeValueFactory.boundedBuilder(Keys.SPAWNER_SPAWN_RANGE)
            .minimum((short) 0)
            .maximum(Short.MAX_VALUE)
            .defaultValue((short) 4)
            .actualValue(this.spawnRange)
            .build();
    }

    @Override
    public NextEntityToSpawnValue nextEntityToSpawn() {
        return new SpongeNextEntityToSpawnValue(this.nextEntityToSpawn);
    }

    @Override
    public WeightedCollectionValue<EntitySnapshot> possibleEntitiesToSpawn() {
        return new SpongeWeightedCollectionValue<>(Keys.SPAWNER_ENTITIES, this.entities);
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
    public DataContainer toContainer() {
        return super.toContainer()
            .set(Keys.SPAWNER_REMAINING_DELAY.getQuery(), this.remainingDelay)
            .set(Keys.SPAWNER_MINIMUM_DELAY.getQuery(), this.minimumDelay)
            .set(Keys.SPAWNER_MAXIMUM_DELAY.getQuery(), this.maximumDelay)
            .set(Keys.SPAWNER_SPAWN_COUNT.getQuery(), this.count)
            .set(Keys.SPAWNER_MAXIMUM_NEARBY_ENTITIES.getQuery(), this.maximumEntities)
            .set(Keys.SPAWNER_REQUIRED_PLAYER_RANGE.getQuery(), this.playerRange)
            .set(Keys.SPAWNER_SPAWN_RANGE.getQuery(), this.spawnRange)
            .set(Keys.SPAWNER_NEXT_ENTITY_TO_SPAWN.getQuery(), this.nextEntityToSpawn)
            .set(Keys.SPAWNER_ENTITIES.getQuery(), this.entities);

    }

    public short getRemainingDelay() {
        return this.remainingDelay;
    }

    public void setRemainingDelay(short remainingDelay) {
        this.remainingDelay = remainingDelay;
    }

    public short getMinimumDelay() {
        return this.minimumDelay;
    }

    public void setMinimumDelay(short minimumDelay) {
        this.minimumDelay = minimumDelay;
    }

    public short getMaximumDelay() {
        return this.maximumDelay;
    }

    public void setMaximumDelay(short maximumDelay) {
        this.maximumDelay = maximumDelay;
    }

    public short getCount() {
        return this.count;
    }

    public void setCount(short count) {
        this.count = count;
    }

    public short getMaximumEntities() {
        return this.maximumEntities;
    }

    public void setMaximumEntities(short maximumEntities) {
        this.maximumEntities = maximumEntities;
    }

    public short getPlayerRange() {
        return this.playerRange;
    }

    public void setPlayerRange(short playerRange) {
        this.playerRange = playerRange;
    }

    public short getSpawnRange() {
        return this.spawnRange;
    }

    public void setSpawnRange(short spawnRange) {
        this.spawnRange = spawnRange;
    }

    public WeightedSerializableObject<EntitySnapshot> getNextEntityToSpawn() {
        return this.nextEntityToSpawn;
    }

    public void setNextEntityToSpawn(WeightedSerializableObject<EntitySnapshot> nextEntityToSpawn) {
        this.nextEntityToSpawn = checkNotNull(nextEntityToSpawn);
    }

    public WeightedTable<EntitySnapshot> getEntities() {
        return this.entities;
    }

    public void setEntities(WeightedTable<EntitySnapshot> entities) {
        checkNotNull(entities).forEach(Preconditions::checkNotNull);
        this.entities = entities;
    }

    @Override
    protected void registerGettersAndSetters() {
        registerKeyValue(Keys.SPAWNER_REMAINING_DELAY, SpongeMobSpawnerData.this::remainingDelay);
        registerKeyValue(Keys.SPAWNER_MINIMUM_DELAY, SpongeMobSpawnerData.this::minimumSpawnDelay);
        registerKeyValue(Keys.SPAWNER_MAXIMUM_DELAY, SpongeMobSpawnerData.this::maximumSpawnDelay);
        registerKeyValue(Keys.SPAWNER_SPAWN_COUNT, SpongeMobSpawnerData.this::spawnCount);
        registerKeyValue(Keys.SPAWNER_MAXIMUM_NEARBY_ENTITIES, SpongeMobSpawnerData.this::maximumNearbyEntities);
        registerKeyValue(Keys.SPAWNER_REQUIRED_PLAYER_RANGE, SpongeMobSpawnerData.this::requiredPlayerRange);
        registerKeyValue(Keys.SPAWNER_SPAWN_RANGE, SpongeMobSpawnerData.this::spawnRange);
        registerKeyValue(Keys.SPAWNER_NEXT_ENTITY_TO_SPAWN, SpongeMobSpawnerData.this::nextEntityToSpawn);
        registerKeyValue(Keys.SPAWNER_ENTITIES, SpongeMobSpawnerData.this::possibleEntitiesToSpawn);

        registerFieldGetter(Keys.SPAWNER_REMAINING_DELAY, SpongeMobSpawnerData.this::getRemainingDelay);
        registerFieldGetter(Keys.SPAWNER_MINIMUM_DELAY, SpongeMobSpawnerData.this::getMinimumDelay);
        registerFieldGetter(Keys.SPAWNER_MAXIMUM_DELAY, SpongeMobSpawnerData.this::getMaximumDelay);
        registerFieldGetter(Keys.SPAWNER_SPAWN_COUNT, SpongeMobSpawnerData.this::getCount);
        registerFieldGetter(Keys.SPAWNER_MAXIMUM_NEARBY_ENTITIES, SpongeMobSpawnerData.this::getMaximumEntities);
        registerFieldGetter(Keys.SPAWNER_REQUIRED_PLAYER_RANGE, SpongeMobSpawnerData.this::getPlayerRange);
        registerFieldGetter(Keys.SPAWNER_SPAWN_RANGE, SpongeMobSpawnerData.this::getSpawnRange);
        registerFieldGetter(Keys.SPAWNER_NEXT_ENTITY_TO_SPAWN, SpongeMobSpawnerData.this::getNextEntityToSpawn);
        registerFieldGetter(Keys.SPAWNER_ENTITIES, SpongeMobSpawnerData.this::getEntities);

        registerFieldSetter(Keys.SPAWNER_REMAINING_DELAY, SpongeMobSpawnerData.this::setRemainingDelay);
        registerFieldSetter(Keys.SPAWNER_MINIMUM_DELAY, SpongeMobSpawnerData.this::setMinimumDelay);
        registerFieldSetter(Keys.SPAWNER_MAXIMUM_DELAY, SpongeMobSpawnerData.this::setMaximumDelay);
        registerFieldSetter(Keys.SPAWNER_SPAWN_COUNT, SpongeMobSpawnerData.this::setCount);
        registerFieldSetter(Keys.SPAWNER_MAXIMUM_NEARBY_ENTITIES, SpongeMobSpawnerData.this::setMaximumEntities);
        registerFieldSetter(Keys.SPAWNER_REQUIRED_PLAYER_RANGE, SpongeMobSpawnerData.this::setPlayerRange);
        registerFieldSetter(Keys.SPAWNER_SPAWN_RANGE, SpongeMobSpawnerData.this::setSpawnRange);
        registerFieldSetter(Keys.SPAWNER_NEXT_ENTITY_TO_SPAWN, SpongeMobSpawnerData.this::setNextEntityToSpawn);
        registerFieldSetter(Keys.SPAWNER_ENTITIES, SpongeMobSpawnerData.this::setEntities);
    }
}
