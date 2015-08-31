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
package org.spongepowered.common.data.processor.value.entity;

import static org.spongepowered.common.data.util.ComparatorUtil.doubleComparator;

import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeBoundedValue;
import org.spongepowered.common.data.value.mutable.SpongeBoundedValue;

import java.util.Optional;

public class HealthValueProcessor extends AbstractSpongeValueProcessor<Double, MutableBoundedValue<Double>> {

    public HealthValueProcessor() {
        super(Keys.HEALTH);
    }

    @Override
    public MutableBoundedValue<Double> constructValue(Double defaultValue) {
        return new SpongeBoundedValue<Double>(Keys.HEALTH, 0D, doubleComparator(), 1D, Double.MAX_VALUE, defaultValue);
    }

    @Override
    public Optional<Double> getValueFromContainer(ValueContainer<?> container) {
        if (container instanceof EntityLivingBase) {
            return Optional.of((double) ((EntityLivingBase) container).getHealth());
        }
        return Optional.empty();
    }

    @Override
    public Optional<MutableBoundedValue<Double>> getApiValueFromContainer(ValueContainer<?> container) {
        if (container instanceof EntityLivingBase) {
            final double health = ((EntityLivingBase) container).getHealth();
            final double maxHealth = ((EntityLivingBase) container).getMaxHealth();
            return Optional.<MutableBoundedValue<Double>>of(new SpongeBoundedValue<Double>(Keys.HEALTH, maxHealth, doubleComparator(),
                                                                                           1D, maxHealth, health));
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(ValueContainer<?> container) {
        return container instanceof EntityLivingBase;
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, Double value) {
        final ImmutableBoundedValue<Double> proposedValue = new ImmutableSpongeBoundedValue<Double>(Keys.HEALTH, value, 20D,
                                                                                                    doubleComparator(), 1D,
                                                                                                    (double) Float.MAX_VALUE);
        if (container instanceof EntityLivingBase) {
            final DataTransactionBuilder builder = DataTransactionBuilder.builder();
            final double maxHealth = ((EntityLivingBase) container).getMaxHealth();
            final ImmutableBoundedValue<Double> newHealthValue = new ImmutableSpongeBoundedValue<Double>(Keys.HEALTH, value, maxHealth,
                                                                                                         doubleComparator(), 0D, maxHealth);
            final ImmutableBoundedValue<Double> oldHealthValue = getApiValueFromContainer(container).get().asImmutable();
            if (value > maxHealth) {
                return DataTransactionBuilder.errorResult(newHealthValue);
            }
            try {
                ((EntityLivingBase) container).setHealth(value.floatValue());
            } catch (Exception e) {
                return DataTransactionBuilder.errorResult(newHealthValue);
            }
            return builder.success(newHealthValue).replace(oldHealthValue).result(DataTransactionResult.Type.SUCCESS).build();
        }
        return DataTransactionBuilder.failResult(proposedValue);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionBuilder.failNoData();
    }
}
