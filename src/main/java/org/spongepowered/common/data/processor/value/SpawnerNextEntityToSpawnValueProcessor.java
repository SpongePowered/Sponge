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
import org.spongepowered.api.data.value.Value.Immutable;
import org.spongepowered.api.data.value.Value.Mutable;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.util.weighted.WeightedSerializableObject;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.processor.common.SpawnerUtils;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.mixin.accessor.world.spawner.AbstractSpawnerAccessor;
import org.spongepowered.common.mixin.accessor.tileentity.MobSpawnerTileEntityAccessor;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

public class SpawnerNextEntityToSpawnValueProcessor extends AbstractSpongeValueProcessor<MobSpawnerTileEntityAccessor,
        WeightedSerializableObject<EntityArchetype>, Mutable<WeightedSerializableObject<EntityArchetype>>> {

    public SpawnerNextEntityToSpawnValueProcessor() {
        super(MobSpawnerTileEntityAccessor.class, Keys.SPAWNER_NEXT_ENTITY_TO_SPAWN);
    }

    @Override
    protected Mutable<WeightedSerializableObject<EntityArchetype>> constructValue(final WeightedSerializableObject<EntityArchetype> actualValue) {
        return new SpongeValue<>(Keys.SPAWNER_NEXT_ENTITY_TO_SPAWN,
                Constants.TileEntity.Spawner.DEFAULT_NEXT_ENTITY_TO_SPAWN, actualValue);
    }

    @Override
    protected boolean set(final MobSpawnerTileEntityAccessor container, final WeightedSerializableObject<EntityArchetype> value) {
        SpawnerUtils.setNextEntity(container.accessor$getSpawnerLogic(), value);
        return true;
    }

    @Override
    protected Optional<WeightedSerializableObject<EntityArchetype>> getVal(final MobSpawnerTileEntityAccessor container) {
        return Optional.of(SpawnerUtils.getNextEntity((AbstractSpawnerAccessor) container.accessor$getSpawnerLogic()));
    }

    @Override
    protected Immutable<WeightedSerializableObject<EntityArchetype>> constructImmutableValue(final WeightedSerializableObject<EntityArchetype> value) {
        return this.constructValue(value).asImmutable();
    }

    @Override
    public DataTransactionResult removeFrom(final ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
