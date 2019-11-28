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

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFallDistanceData;
import org.spongepowered.api.data.manipulator.mutable.entity.FallDistanceData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeFallDistanceData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.value.SpongeValueFactory;

import java.util.Optional;
import net.minecraft.entity.LivingEntity;

public class FallDistanceDataProcessor
        extends AbstractEntitySingleDataProcessor<LivingEntity, Float, MutableBoundedValue<Float>, FallDistanceData, ImmutableFallDistanceData> {

    public FallDistanceDataProcessor() {
        super(LivingEntity.class, Keys.FALL_DISTANCE);
    }

    @Override
    protected boolean set(LivingEntity entity, Float value) {
        entity.fallDistance = checkNotNull(value);
        return true;
    }

    @Override
    protected Optional<Float> getVal(LivingEntity entity) {
        return Optional.of(entity.fallDistance);
    }

    @Override
    protected MutableBoundedValue<Float> constructValue(Float value) {
        return SpongeValueFactory.boundedBuilder(this.key)
                .actualValue(value)
                .defaultValue(0F)
                .minimum(0F)
                .maximum(Float.MAX_VALUE)
                .build();
    }

    @Override
    protected ImmutableValue<Float> constructImmutableValue(Float value) {
        return constructValue(value).asImmutable();
    }

    @Override
    protected FallDistanceData createManipulator() {
        return new SpongeFallDistanceData();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

}
