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
package org.spongepowered.common.mixin.api.mcp.tileentity;

import org.spongepowered.api.block.tileentity.MobSpawner;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.MobSpawnerData;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.data.value.mutable.WeightedCollectionValue;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.weighted.WeightedSerializableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.tileentity.MobSpawnerBaseLogicBridge;
import org.spongepowered.common.data.manipulator.mutable.SpongeMobSpawnerData;
import org.spongepowered.common.data.processor.common.SpawnerUtils;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeWeightedCollectionValue;
import org.spongepowered.common.util.Constants;

import java.util.List;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.world.spawner.AbstractSpawner;

@NonnullByDefault
@Mixin(MobSpawnerTileEntity.class)
public abstract class TileEntityMobSpawnerMixin_API extends TileEntityMixin_API implements MobSpawner {

    @Shadow public abstract AbstractSpawner getSpawnerBaseLogic();

    @Override
    public void spawnEntityBatchImmediately(final boolean force) {
        final MobSpawnerBaseLogicBridge bridge = ((MobSpawnerBaseLogicBridge) getSpawnerBaseLogic());

        if (force) {
            final short oldMaxNearby = (short) bridge.bridge$getMaxNearbyEntities();
            bridge.bridge$setMaxNearbyEntities(Short.MAX_VALUE);

            bridge.bridge$setSpawnDelay(0);
            getSpawnerBaseLogic().tick();

            bridge.bridge$setMaxNearbyEntities(oldMaxNearby);
        } else {
            bridge.bridge$setSpawnDelay(0);
        }
    }

    @Override
    public MobSpawnerData getMobSpawnerData() {
//        final MobSpawnerBaseLogicAccessor accessor = (MobSpawnerBaseLogicAccessor) this.getSpawnerBaseLogic();
//        return new SpongeMobSpawnerData(
//                (short) accessor.accessor$getSpawnDelay(),
//                (short) accessor.accessor$getMinSpawnDelay(),
//                (short) accessor.accessor$getMaxSpawnDelay(),
//                (short) accessor.accessor$getSpawnCount(),
//                (short) accessor.accessor$getMaxNearbyEntities(),
//                (short) accessor.accessor$getActivatingRangeFromPlayer(),
//                (short) accessor.accessor$getSpawnRange(),
//                SpawnerUtils.getNextEntity(accessor),
//                SpawnerUtils.getEntities(this.mobSpawnerLogic));
        final MobSpawnerBaseLogicBridge accessor = (MobSpawnerBaseLogicBridge) this.getSpawnerBaseLogic();
        return new SpongeMobSpawnerData(
            (short) accessor.bridge$getSpawnDelay(),
            (short) accessor.bridge$getMinSpawnDelay(),
            (short) accessor.bridge$getMaxSpawnDelay(),
            (short) accessor.bridge$getSpawnCount(),
            (short) accessor.bridge$getMaxNearbyEntities(),
            (short) accessor.bridge$getActivatingRangeFromPlayer(),
            (short) accessor.bridge$getSpawnRange(),
            SpawnerUtils.getNextEntity(accessor),
            SpawnerUtils.getEntities(getSpawnerBaseLogic()));
    }

    @Override
    public MutableBoundedValue<Short> remainingDelay() {
        return SpongeValueFactory.boundedBuilder(Keys.SPAWNER_REMAINING_DELAY)
            .minimum((short) 0)
            .maximum(Short.MAX_VALUE)
            .defaultValue(Constants.TileEntity.Spawner.DEFAULT_REMAINING_DELAY)
            .actualValue((short) ((MobSpawnerBaseLogicBridge) getSpawnerBaseLogic()).bridge$getSpawnDelay())
            .build();
    }

    @Override
    public MutableBoundedValue<Short> minimumSpawnDelay() {
        return SpongeValueFactory.boundedBuilder(Keys.SPAWNER_MINIMUM_DELAY)
            .minimum((short) 0)
            .maximum(Short.MAX_VALUE)
            .defaultValue(Constants.TileEntity.Spawner.DEFAULT_MINIMUM_SPAWN_DELAY)
            .actualValue((short) ((MobSpawnerBaseLogicBridge) getSpawnerBaseLogic()).bridge$getMinSpawnDelay())
            .build();
    }

    @Override
    public MutableBoundedValue<Short> maximumSpawnDelay() {
        return SpongeValueFactory.boundedBuilder(Keys.SPAWNER_MAXIMUM_DELAY)
            .minimum((short) 0)
            .maximum(Short.MAX_VALUE)
            .defaultValue(Constants.TileEntity.Spawner.DEFAULT_MAXIMUM_SPAWN_DELAY)
            .actualValue((short) ((MobSpawnerBaseLogicBridge) getSpawnerBaseLogic()).bridge$getMaxSpawnDelay())
            .build();
    }

    @Override
    public MutableBoundedValue<Short> spawnCount() {
        return SpongeValueFactory.boundedBuilder(Keys.SPAWNER_SPAWN_COUNT)
            .minimum((short) 0)
            .maximum(Short.MAX_VALUE)
            .defaultValue(Constants.TileEntity.Spawner.DEFAULT_SPAWN_COUNT)
            .actualValue((short) ((MobSpawnerBaseLogicBridge) getSpawnerBaseLogic()).bridge$getSpawnCount())
            .build();
    }

    @Override
    public MutableBoundedValue<Short> maximumNearbyEntities() {
        return SpongeValueFactory.boundedBuilder(Keys.SPAWNER_MAXIMUM_NEARBY_ENTITIES)
            .minimum((short) 0)
            .maximum(Short.MAX_VALUE)
            .defaultValue(Constants.TileEntity.Spawner.DEFAULT_MAXMIMUM_NEARBY_ENTITIES)
            .actualValue((short) ((MobSpawnerBaseLogicBridge) getSpawnerBaseLogic()).bridge$getMaxNearbyEntities())
            .build();
    }

    @Override
    public MutableBoundedValue<Short> requiredPlayerRange() {
        return SpongeValueFactory.boundedBuilder(Keys.SPAWNER_REQUIRED_PLAYER_RANGE)
            .minimum((short) 0)
            .maximum(Short.MAX_VALUE)
            .defaultValue(Constants.TileEntity.Spawner.DEFAULT_REQUIRED_PLAYER_RANGE)
            .actualValue((short) ((MobSpawnerBaseLogicBridge) getSpawnerBaseLogic()).bridge$getActivatingRangeFromPlayer())
            .build();
    }

    @Override
    public MutableBoundedValue<Short> spawnRange() {
        return SpongeValueFactory.boundedBuilder(Keys.SPAWNER_SPAWN_RANGE)
            .minimum((short) 0)
            .maximum(Short.MAX_VALUE)
            .defaultValue(Constants.TileEntity.Spawner.DEFAULT_SPAWN_RANGE)
            .actualValue((short) ((MobSpawnerBaseLogicBridge) getSpawnerBaseLogic()).bridge$getSpawnRange())
            .build();
    }

    @Override
    public Value<WeightedSerializableObject<EntityArchetype>> nextEntityToSpawn() {
        return new SpongeValue<>(Keys.SPAWNER_NEXT_ENTITY_TO_SPAWN,
            Constants.TileEntity.Spawner.DEFAULT_NEXT_ENTITY_TO_SPAWN, SpawnerUtils.getNextEntity((MobSpawnerBaseLogicBridge) getSpawnerBaseLogic()));
    }

    @Override
    public WeightedCollectionValue<EntityArchetype> possibleEntitiesToSpawn() {
        return new SpongeWeightedCollectionValue<>(Keys.SPAWNER_ENTITIES, SpawnerUtils.getEntities(getSpawnerBaseLogic()));
    }

    @Override
    public void supplyVanillaManipulators(final List<DataManipulator<?, ?>> manipulators) {
        super.supplyVanillaManipulators(manipulators);
        manipulators.add(getMobSpawnerData());
    }

}
