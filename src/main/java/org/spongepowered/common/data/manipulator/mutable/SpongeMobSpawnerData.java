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
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableMobSpawnerData;
import org.spongepowered.api.data.manipulator.mutable.MobSpawnerData;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.data.value.mutable.WeightedCollectionValue;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.util.weighted.WeightedSerializableObject;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeMobSpawnerData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.data.util.ImplementationRequiredForTest;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeWeightedCollectionValue;

import java.util.stream.Collectors;

// Due to usage of Constants which references catalog types
@ImplementationRequiredForTest
public class SpongeMobSpawnerData extends AbstractData<MobSpawnerData, ImmutableMobSpawnerData> implements MobSpawnerData {

    private short remainingDelay;
    private short minimumDelay;
    private short maximumDelay;
    private short count;
    private short maximumEntities;
    private short playerRange;
    private short spawnRange;
    private WeightedSerializableObject<EntityArchetype> nextEntityToSpawn;
    private WeightedTable<EntityArchetype> entities;

    public SpongeMobSpawnerData(short remainingDelay, short minimumDelay, short maximumDelay, short count,
                                short maximumEntities, short playerRange, short spawnRange,
                                WeightedSerializableObject<EntityArchetype> nextEntityToSpawn,
                                WeightedTable<EntityArchetype> entities) {
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
        this.entities = entities.stream().collect(Collectors.toCollection(WeightedTable<EntityArchetype>::new));
        registerGettersAndSetters();
    }

    public SpongeMobSpawnerData() {
        this(Constants.TileEntity.Spawner.DEFAULT_REMAINING_DELAY, Constants.TileEntity.Spawner.DEFAULT_MINIMUM_SPAWN_DELAY,
                Constants.TileEntity.Spawner.DEFAULT_MAXIMUM_SPAWN_DELAY, Constants.TileEntity.Spawner.DEFAULT_SPAWN_COUNT,
                Constants.TileEntity.Spawner.DEFAULT_MAXMIMUM_NEARBY_ENTITIES, Constants.TileEntity.Spawner.DEFAULT_REQUIRED_PLAYER_RANGE,
                Constants.TileEntity.Spawner.DEFAULT_SPAWN_RANGE, Constants.TileEntity.Spawner.DEFAULT_NEXT_ENTITY_TO_SPAWN, new WeightedTable<>());
    }


    @Override
    public MutableBoundedValue<Short> remainingDelay() {
        return SpongeValueFactory.boundedBuilder(Keys.SPAWNER_REMAINING_DELAY)
            .minimum((short) 0)
            .maximum(this.maximumDelay)
            .defaultValue(Constants.TileEntity.Spawner.DEFAULT_REMAINING_DELAY)
            .actualValue(this.remainingDelay)
            .build();
    }

    @Override
    public MutableBoundedValue<Short> minimumSpawnDelay() {
        return SpongeValueFactory.boundedBuilder(Keys.SPAWNER_MINIMUM_DELAY)
            .minimum((short) 0)
            .maximum(Short.MAX_VALUE)
            .defaultValue(Constants.TileEntity.Spawner.DEFAULT_MINIMUM_SPAWN_DELAY)
            .actualValue(this.minimumDelay)
            .build();
    }

    @Override
    public MutableBoundedValue<Short> maximumSpawnDelay() {
        return SpongeValueFactory.boundedBuilder(Keys.SPAWNER_MAXIMUM_DELAY)
            .minimum(Constants.TileEntity.Spawner.MINIMUM_MAXIMUM_SPAWN_DELAY)
            .maximum(Short.MAX_VALUE)
            .defaultValue(Constants.TileEntity.Spawner.DEFAULT_MAXIMUM_SPAWN_DELAY)
            .actualValue(this.maximumDelay)
            .build();
    }

    @Override
    public MutableBoundedValue<Short> spawnCount() {
        return SpongeValueFactory.boundedBuilder(Keys.SPAWNER_SPAWN_COUNT)
            .minimum((short) 0)
            .maximum(Short.MAX_VALUE)
            .defaultValue(Constants.TileEntity.Spawner.DEFAULT_SPAWN_COUNT)
            .actualValue(this.count)
            .build();
    }

    @Override
    public MutableBoundedValue<Short> maximumNearbyEntities() {
        return SpongeValueFactory.boundedBuilder(Keys.SPAWNER_MAXIMUM_NEARBY_ENTITIES)
            .minimum((short) 0)
            .maximum(Short.MAX_VALUE)
            .defaultValue(Constants.TileEntity.Spawner.DEFAULT_MAXMIMUM_NEARBY_ENTITIES)
            .actualValue(this.maximumEntities)
            .build();
    }

    @Override
    public MutableBoundedValue<Short> requiredPlayerRange() {
        return SpongeValueFactory.boundedBuilder(Keys.SPAWNER_REQUIRED_PLAYER_RANGE)
            .minimum((short) 0)
            .maximum(Short.MAX_VALUE)
            .defaultValue(Constants.TileEntity.Spawner.DEFAULT_REQUIRED_PLAYER_RANGE)
            .actualValue(this.playerRange)
            .build();
    }

    @Override
    public MutableBoundedValue<Short> spawnRange() {
        return SpongeValueFactory.boundedBuilder(Keys.SPAWNER_SPAWN_RANGE)
            .minimum((short) 0)
            .maximum(Short.MAX_VALUE)
            .defaultValue(Constants.TileEntity.Spawner.DEFAULT_SPAWN_RANGE)
            .actualValue(this.spawnRange)
            .build();
    }

    @Override
    public Value<WeightedSerializableObject<EntityArchetype>> nextEntityToSpawn() {
        return new SpongeValue<>(Keys.SPAWNER_NEXT_ENTITY_TO_SPAWN, Constants.TileEntity.Spawner.DEFAULT_NEXT_ENTITY_TO_SPAWN,
                this.nextEntityToSpawn);
    }

    @Override
    public WeightedCollectionValue<EntityArchetype> possibleEntitiesToSpawn() {
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

    private void setEntities(WeightedTable<EntityArchetype> entities) {
        checkNotNull(entities);
        this.entities.clear();
        this.entities.addAll(entities);
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

        registerFieldGetter(Keys.SPAWNER_REMAINING_DELAY, () -> this.remainingDelay);
        registerFieldGetter(Keys.SPAWNER_MINIMUM_DELAY, () -> this.minimumDelay);
        registerFieldGetter(Keys.SPAWNER_MAXIMUM_DELAY, () -> this.maximumDelay);
        registerFieldGetter(Keys.SPAWNER_SPAWN_COUNT, () -> this.count);
        registerFieldGetter(Keys.SPAWNER_MAXIMUM_NEARBY_ENTITIES, () -> this.maximumEntities);
        registerFieldGetter(Keys.SPAWNER_REQUIRED_PLAYER_RANGE, () -> this.playerRange);
        registerFieldGetter(Keys.SPAWNER_SPAWN_RANGE, () -> this.spawnRange);
        registerFieldGetter(Keys.SPAWNER_NEXT_ENTITY_TO_SPAWN, () -> this.nextEntityToSpawn);
        registerFieldGetter(Keys.SPAWNER_ENTITIES, () -> this.entities);

        registerFieldSetter(Keys.SPAWNER_REMAINING_DELAY, remaining -> this.remainingDelay = checkNotNull(remaining));
        registerFieldSetter(Keys.SPAWNER_MINIMUM_DELAY, minDelay -> this.minimumDelay = checkNotNull(minDelay));
        registerFieldSetter(Keys.SPAWNER_MAXIMUM_DELAY, maxDelay -> this.maximumDelay = checkNotNull(maxDelay));
        registerFieldSetter(Keys.SPAWNER_SPAWN_COUNT, count -> this.count = checkNotNull(count));
        registerFieldSetter(Keys.SPAWNER_MAXIMUM_NEARBY_ENTITIES, maxEntities -> this.maximumEntities = checkNotNull(maxEntities));
        registerFieldSetter(Keys.SPAWNER_REQUIRED_PLAYER_RANGE, playerRange -> this.playerRange = checkNotNull(playerRange));
        registerFieldSetter(Keys.SPAWNER_SPAWN_RANGE, spawnRange -> this.spawnRange = checkNotNull(spawnRange));
        registerFieldSetter(Keys.SPAWNER_NEXT_ENTITY_TO_SPAWN, nextEntity -> this.nextEntityToSpawn = checkNotNull(nextEntity));
        registerFieldSetter(Keys.SPAWNER_ENTITIES, SpongeMobSpawnerData.this::setEntities);
    }
}
