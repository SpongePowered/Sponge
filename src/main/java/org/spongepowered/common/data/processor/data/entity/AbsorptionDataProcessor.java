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
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableAbsorptionData;
import org.spongepowered.api.data.manipulator.mutable.entity.AbsorptionData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeAbsorptionData;
import org.spongepowered.common.data.processor.common.AbstractSingleDataSingleTargetProcessor;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;
import net.minecraft.entity.LivingEntity;

public final class AbsorptionDataProcessor extends AbstractSingleDataSingleTargetProcessor<LivingEntity, Double, Value<Double>, AbsorptionData, ImmutableAbsorptionData> {

    public AbsorptionDataProcessor() {
        super(Keys.ABSORPTION, LivingEntity.class);
    }

    @Override
    protected AbsorptionData createManipulator() {
        return new SpongeAbsorptionData();
    }

    @Override
    protected boolean set(LivingEntity living, Double value) {
        checkNotNull(value, "value");
        living.func_110149_m(value.floatValue());
        return true;
    }

    @Override
    protected Optional<Double> getVal(LivingEntity living) {
        return Optional.of((double) living.func_110139_bj());
    }

    @Override
    protected ImmutableValue<Double> constructImmutableValue(Double value) {
        return new ImmutableSpongeValue<>(this.key, Constants.Entity.DEFAULT_ABSORPTION, value);
    }

    @Override
    protected Value<Double> constructValue(Double actualValue) {
        return new SpongeValue<>(this.key, Constants.Entity.DEFAULT_ABSORPTION, actualValue);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

}
