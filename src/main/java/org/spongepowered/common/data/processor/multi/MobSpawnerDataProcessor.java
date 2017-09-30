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
package org.spongepowered.common.data.processor.multi;

import com.google.common.collect.Maps;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableMobSpawnerData;
import org.spongepowered.api.data.manipulator.mutable.MobSpawnerData;
import org.spongepowered.common.data.manipulator.mutable.SpongeMobSpawnerData;
import org.spongepowered.common.data.processor.common.AbstractMultiDataSingleTargetProcessor;
import org.spongepowered.common.data.processor.common.SpawnerUtils;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.interfaces.IMixinMobSpawner;

import java.util.Map;
import java.util.Optional;

public class MobSpawnerDataProcessor extends AbstractMultiDataSingleTargetProcessor<IMixinMobSpawner, MobSpawnerData, ImmutableMobSpawnerData> {

    public MobSpawnerDataProcessor() {
        super(IMixinMobSpawner.class);
    }

    @Override
    protected boolean doesDataExist(IMixinMobSpawner entity) {
        return true;
    }

    @Override
    protected boolean set(IMixinMobSpawner entity, Map<Key<?>, Object> values) {
        MobSpawnerBaseLogic logic = entity.getLogic();
        SpawnerUtils.applyData(logic, values);
        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(IMixinMobSpawner spawner) {
        MobSpawnerBaseLogic logic = spawner.getLogic();
        Map<Key<?>, Object> values = Maps.newIdentityHashMap();

        values.put(Keys.SPAWNER_REMAINING_DELAY, (short) logic.spawnDelay);
        values.put(Keys.SPAWNER_MINIMUM_DELAY, (short) logic.minSpawnDelay);
        values.put(Keys.SPAWNER_MAXIMUM_DELAY, (short) logic.maxSpawnDelay);
        values.put(Keys.SPAWNER_SPAWN_COUNT, (short) logic.spawnCount);
        values.put(Keys.SPAWNER_MAXIMUM_NEARBY_ENTITIES, (short) logic.maxNearbyEntities);
        values.put(Keys.SPAWNER_REQUIRED_PLAYER_RANGE, (short) logic.activatingRangeFromPlayer);
        values.put(Keys.SPAWNER_SPAWN_RANGE, (short) logic.spawnRange);
        values.put(Keys.SPAWNER_NEXT_ENTITY_TO_SPAWN, SpawnerUtils.getNextEntity(logic));
        values.put(Keys.SPAWNER_ENTITIES, SpawnerUtils.getEntities(logic));

        return values;
    }

    @Override
    protected MobSpawnerData createManipulator() {
        return new SpongeMobSpawnerData();
    }

    @Override
    public Optional<MobSpawnerData> fill(DataContainer container, MobSpawnerData data) {
        if (!container.contains(
                Keys.SPAWNER_REMAINING_DELAY.getQuery(),
                Keys.SPAWNER_MINIMUM_DELAY.getQuery(),
                Keys.SPAWNER_MAXIMUM_DELAY.getQuery(),
                Keys.SPAWNER_SPAWN_COUNT.getQuery(),
                Keys.SPAWNER_MAXIMUM_NEARBY_ENTITIES.getQuery(),
                Keys.SPAWNER_REQUIRED_PLAYER_RANGE.getQuery(),
                Keys.SPAWNER_SPAWN_RANGE.getQuery(),
                Keys.SPAWNER_NEXT_ENTITY_TO_SPAWN.getQuery(),
                Keys.SPAWNER_ENTITIES.getQuery()
        )) {
            return Optional.empty();
        }

        data.set(Keys.SPAWNER_REMAINING_DELAY, DataUtil.getData(container, Keys.SPAWNER_REMAINING_DELAY));
        data.set(Keys.SPAWNER_MINIMUM_DELAY, DataUtil.getData(container, Keys.SPAWNER_MINIMUM_DELAY));
        data.set(Keys.SPAWNER_MAXIMUM_DELAY, DataUtil.getData(container, Keys.SPAWNER_MAXIMUM_DELAY));
        data.set(Keys.SPAWNER_SPAWN_COUNT, DataUtil.getData(container, Keys.SPAWNER_SPAWN_COUNT));
        data.set(Keys.SPAWNER_MAXIMUM_NEARBY_ENTITIES, DataUtil.getData(container, Keys.SPAWNER_MAXIMUM_NEARBY_ENTITIES));
        data.set(Keys.SPAWNER_REQUIRED_PLAYER_RANGE, DataUtil.getData(container, Keys.SPAWNER_REQUIRED_PLAYER_RANGE));
        data.set(Keys.SPAWNER_SPAWN_RANGE, DataUtil.getData(container, Keys.SPAWNER_SPAWN_RANGE));
        data.set(Keys.SPAWNER_NEXT_ENTITY_TO_SPAWN, DataUtil.getData(container, Keys.SPAWNER_NEXT_ENTITY_TO_SPAWN));
        data.set(Keys.SPAWNER_ENTITIES, DataUtil.getData(container, Keys.SPAWNER_ENTITIES));

        return Optional.of(data);
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionResult.failNoData();
    }
}
