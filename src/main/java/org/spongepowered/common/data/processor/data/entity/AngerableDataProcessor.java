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
package org.spongepowered.common.data.processor.data.entity;

import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableAngerableData;
import org.spongepowered.api.data.manipulator.mutable.entity.AngerableData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeAngerableData;
import org.spongepowered.common.data.processor.common.AbstractSingleDataSingleTargetProcessor;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.mixin.core.entity.monster.EntityPigZombieAccessor;

import java.util.Optional;

public class AngerableDataProcessor extends
    AbstractSingleDataSingleTargetProcessor<EntityPigZombieAccessor, Integer, MutableBoundedValue<Integer>, AngerableData, ImmutableAngerableData> {

    public AngerableDataProcessor() {
        super(Keys.ANGER, EntityPigZombieAccessor.class);
    }

    @Override
    protected MutableBoundedValue<Integer> constructValue(final Integer actualValue) {
        return SpongeValueFactory.boundedBuilder(Keys.ANGER)
                .actualValue(actualValue)
                .defaultValue(0)
                .minimum(Integer.MIN_VALUE)
                .maximum(Integer.MAX_VALUE)
                .build();
    }

    @Override
    protected boolean set(final EntityPigZombieAccessor entity, final Integer value) {
        entity.accessor$setAngerLevel(value);
        return true;
    }

    @Override
    protected Optional<Integer> getVal(final EntityPigZombieAccessor entity) {
        return Optional.of(entity.accessor$getAngerLevel());
    }

    @Override
    protected ImmutableValue<Integer> constructImmutableValue(final Integer value) {
        return new ImmutableSpongeValue<Integer>(Keys.ANGER, 0, value);
    }

    @Override
    protected AngerableData createManipulator() {
        return new SpongeAngerableData();
    }

    @Override
    public DataTransactionResult removeFrom(final ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
