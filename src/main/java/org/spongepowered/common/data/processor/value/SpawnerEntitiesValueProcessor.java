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
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.WeightedCollectionValue;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.processor.common.SpawnerUtils;
import org.spongepowered.common.data.value.mutable.SpongeWeightedCollectionValue;
import org.spongepowered.common.mixin.core.tileentity.MobSpawnerBaseLogicAccessor;
import org.spongepowered.common.mixin.core.tileentity.TileEntityMobSpawnerAccessor;

import java.util.Optional;
import net.minecraft.world.spawner.AbstractSpawner;

public class SpawnerEntitiesValueProcessor extends AbstractSpongeValueProcessor<TileEntityMobSpawnerAccessor, WeightedTable<EntityArchetype>, WeightedCollectionValue<EntityArchetype>> {

    public SpawnerEntitiesValueProcessor() {
        super(TileEntityMobSpawnerAccessor.class, Keys.SPAWNER_ENTITIES);
    }

    @Override
    protected WeightedCollectionValue<EntityArchetype> constructValue(final WeightedTable<EntityArchetype> actualValue) {
        return new SpongeWeightedCollectionValue<>(this.key, actualValue);
    }

    @Override
    protected boolean set(final TileEntityMobSpawnerAccessor container, final WeightedTable<EntityArchetype> value) {
        final MobSpawnerBaseLogicAccessor logic = (MobSpawnerBaseLogicAccessor) container.accessor$getSpawnerLogic();
        SpawnerUtils.setEntities(logic, value);
        SpawnerUtils.setNextEntity((AbstractSpawner) logic, SpawnerUtils.getNextEntity(logic));
        return true;
    }

    @Override
    protected Optional<WeightedTable<EntityArchetype>> getVal(final TileEntityMobSpawnerAccessor container) {
        return Optional.of(SpawnerUtils.getEntities(container.accessor$getSpawnerLogic()));
    }

    @Override
    protected ImmutableValue<WeightedTable<EntityArchetype>> constructImmutableValue(final WeightedTable<EntityArchetype> value) {
        return constructValue(value).asImmutable();
    }

    @Override
    public DataTransactionResult removeFrom(final ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
