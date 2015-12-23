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
package org.spongepowered.common.data.processor.value;

import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableMobSpawnerData;
import org.spongepowered.api.data.manipulator.mutable.MobSpawnerData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.util.weighted.WeightedSerializableObject;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.processor.common.SpawnerUtils;
import org.spongepowered.common.data.value.mutable.SpongeNextEntityToSpawnValue;
import org.spongepowered.common.interfaces.IMixinMobSpawner;

import java.util.Optional;

public class SpawnerNextEntityToSpawnValueProcessor extends AbstractSpongeValueProcessor<IMixinMobSpawner, WeightedSerializableObject<EntitySnapshot>, MobSpawnerData.NextEntityToSpawnValue> {

    public SpawnerNextEntityToSpawnValueProcessor() {
        super(IMixinMobSpawner.class, Keys.SPAWNER_NEXT_ENTITY_TO_SPAWN);
    }

    @Override
    protected MobSpawnerData.NextEntityToSpawnValue constructValue(WeightedSerializableObject<EntitySnapshot> actualValue) {
        return new SpongeNextEntityToSpawnValue(actualValue);
    }

    @Override
    protected boolean set(IMixinMobSpawner container, WeightedSerializableObject<EntitySnapshot> value) {
        SpawnerUtils.setNextSnapshot(container.getSpawnerBaseLogic(), value);
        return true;
    }

    @Override
    protected Optional<WeightedSerializableObject<EntitySnapshot>> getVal(IMixinMobSpawner container) {
        return SpawnerUtils.getNextSnapshot(container.getSpawnerBaseLogic());
    }

    @Override
    protected ImmutableMobSpawnerData.ImmutableNextEntityToSpawnValue constructImmutableValue(WeightedSerializableObject<EntitySnapshot> value) {
        return constructValue(value).asImmutable();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
