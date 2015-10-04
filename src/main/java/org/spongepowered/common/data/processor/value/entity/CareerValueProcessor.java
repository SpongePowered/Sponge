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

import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.Career;
import org.spongepowered.api.data.type.Careers;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.interfaces.entity.IMixinVillager;

import java.util.Optional;

public class CareerValueProcessor extends AbstractSpongeValueProcessor<Career, Value<Career>> {

    public CareerValueProcessor() {
        super(Keys.CAREER);
    }

    @Override
    public Value<Career> constructValue(Career defaultValue) {
        return new SpongeValue<Career>(Keys.CAREER, defaultValue);
    }

    @Override
    public Optional<Career> getValueFromContainer(ValueContainer<?> container) {
        return container instanceof IMixinVillager ? Optional.of(((IMixinVillager) container).getCareer()) : Optional.<Career>empty();
    }

    @Override
    public boolean supports(ValueContainer<?> container) {
        return container instanceof IMixinVillager;
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, Career value) {
        final ImmutableValue<Career> newValue = ImmutableDataCachingUtil.getValue(ImmutableSpongeValue.class, Keys.CAREER, value, Careers.ARMORER);
        if (container instanceof IMixinVillager) {
            final Career old = ((IMixinVillager) container).getCareer();
            final ImmutableValue<Career> oldValue = ImmutableDataCachingUtil.getValue(ImmutableSpongeValue.class, Keys.CAREER, old, Careers.ARMORER);
            try {
                ((IMixinVillager) container).setCareer(value);
                return DataTransactionBuilder.successReplaceResult(newValue, oldValue);
            } catch (Exception e) {
                return DataTransactionBuilder.errorResult(newValue);
            }
        }
        return DataTransactionBuilder.failResult(ImmutableSpongeValue.cachedOf(Keys.CAREER, Careers.ARMORER, value));
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionBuilder.failNoData();
    }
}
