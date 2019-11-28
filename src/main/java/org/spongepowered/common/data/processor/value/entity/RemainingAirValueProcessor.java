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

import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.util.Constants;

import java.util.Optional;
import net.minecraft.entity.LivingEntity;

public class RemainingAirValueProcessor extends AbstractSpongeValueProcessor<LivingEntity, Integer, MutableBoundedValue<Integer>> {

    public RemainingAirValueProcessor() {
        super(LivingEntity.class, Keys.REMAINING_AIR);
    }

    @Override
    public MutableBoundedValue<Integer> constructValue(final Integer defaultValue) {
        return SpongeValueFactory.boundedBuilder(Keys.REMAINING_AIR)
            .defaultValue(Constants.Sponge.Entity.DEFAULT_MAX_AIR)
            .minimum(-20)
            .maximum(Integer.MAX_VALUE)
            .actualValue(defaultValue)
            .build();
    }

    @Override
    protected boolean set(final LivingEntity container, final Integer value) {
        container.func_70050_g(value);
        return true;
    }

    @Override
    protected Optional<Integer> getVal(final LivingEntity container) {
        return Optional.of(container.func_70086_ai());
    }

    @Override
    protected ImmutableBoundedValue<Integer> constructImmutableValue(final Integer value) {
        return constructValue(value).asImmutable();
    }

    @Override
    protected boolean supports(final LivingEntity container) {
        return container.func_70090_H();
    }

    @Override
    public DataTransactionResult removeFrom(final ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

}
