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
import net.minecraft.entity.SharedMonsterAttributes;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeBoundedValue;
import org.spongepowered.common.data.value.mutable.SpongeBoundedValue;

import java.util.Optional;

public class MaxHealthValueProcessor extends AbstractSpongeValueProcessor<EntityLivingBase, Double, MutableBoundedValue<Double>> {

    public MaxHealthValueProcessor() {
        super(EntityLivingBase.class, Keys.MAX_HEALTH);
    }

    @Override
    public MutableBoundedValue<Double> constructValue(Double defaultValue) {
        return new SpongeBoundedValue<>(Keys.MAX_HEALTH, 20D, doubleComparator(), 1D, Double.MAX_VALUE, defaultValue);
    }

    @Override
    protected boolean set(EntityLivingBase container, Double value) {
        container.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(value);
        return true;
    }

    @Override
    protected Optional<Double> getVal(EntityLivingBase container) {
        return Optional.of((double) container.getMaxHealth());
    }

    @Override
    protected ImmutableValue<Double> constructImmutableValue(Double value) {
        return new ImmutableSpongeBoundedValue<>(Keys.MAX_HEALTH, value, 20D, doubleComparator(), 1D, Double.MAX_VALUE);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionBuilder.failNoData();
    }
}
