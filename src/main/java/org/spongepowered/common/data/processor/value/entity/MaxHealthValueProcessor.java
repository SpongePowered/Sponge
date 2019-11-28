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

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.SpongeValueFactory;

import java.util.Optional;

public class MaxHealthValueProcessor extends AbstractSpongeValueProcessor<EntityLivingBase, Double, MutableBoundedValue<Double>> {

    public MaxHealthValueProcessor() {
        super(EntityLivingBase.class, Keys.MAX_HEALTH);
    }

    @Override
    public MutableBoundedValue<Double> constructValue(final Double defaultValue) {
        return SpongeValueFactory.boundedBuilder(Keys.MAX_HEALTH)
            .defaultValue(20D)
            .minimum(1D)
            .maximum(((Float) Float.MAX_VALUE).doubleValue())
            .actualValue(defaultValue)
            .build();
    }

    @Override
    protected boolean set(final EntityLivingBase container, final Double value) {
        container.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(value);
        return true;
    }

    @Override
    protected Optional<Double> getVal(final EntityLivingBase container) {
        return Optional.of((double) container.func_110138_aP());
    }

    @Override
    protected ImmutableBoundedValue<Double> constructImmutableValue(final Double value) {
        return constructValue(value).asImmutable();
    }

    @Override
    public DataTransactionResult removeFrom(final ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
