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

import static org.spongepowered.common.data.util.ComparatorUtil.intComparator;

import net.minecraft.entity.EntityAgeable;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.SpongeValueFactory;

import java.util.Optional;

public class AgeValueProcessor extends AbstractSpongeValueProcessor<EntityAgeable, Integer> {

    public AgeValueProcessor() {
        super(EntityAgeable.class, Keys.AGE);
    }

    @Override
    public BoundedValue.Mutable<Integer> constructMutableValue(Integer age) {
        return SpongeValueFactory.boundedBuilder(Keys.AGE)
                .comparator(intComparator())
                .minimum(Integer.MIN_VALUE)
                .maximum(Integer.MAX_VALUE)
                .value(age)
                .build();
    }

    @Override
    protected boolean set(EntityAgeable container, Integer value) {
        container.setGrowingAge(value);
        return true;
    }

    @Override
    protected Optional<Integer> getVal(EntityAgeable container) {
        return Optional.of(container.getGrowingAge());
    }

    @Override
    protected BoundedValue.Immutable<Integer> constructImmutableValue(Integer value) {
        return constructMutableValue(value).asImmutable();
    }

    @Override
    public Optional<BoundedValue.Mutable<Integer>> getApiValueFromContainer(ValueContainer<?> container) {
        if (this.supports(container)) {
            Optional<Integer> value = this.getVal((EntityAgeable) container);
            if (value.isPresent()) {
                return Optional.of(this.constructMutableValue(value.get()));
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(ValueContainer<?> container) {
        return container instanceof EntityAgeable;
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, Integer value) {
        final BoundedValue.Immutable<Integer> proposedValue = constructImmutableValue(value);
        if (this.supports(container)) {
            final DataTransactionResult.Builder builder = DataTransactionResult.builder();
            final BoundedValue.Immutable<Integer> newAgeValue = SpongeValueFactory.boundedBuilder(Keys.AGE)
                    .defaultValue(0)
                    .minimum(Integer.MIN_VALUE)
                    .maximum(Integer.MAX_VALUE)
                    .value(value)
                    .build()
                    .asImmutable();
            final BoundedValue.Immutable<Integer> oldAgeValue = getApiValueFromContainer(container).get().asImmutable();
            try {
                ((EntityAgeable) container).setGrowingAge(value);
            } catch (Exception e) {
                return DataTransactionResult.errorResult(newAgeValue);
            }
            return builder.success(newAgeValue).replace(oldAgeValue).result(DataTransactionResult.Type.SUCCESS).build();
        }
        return DataTransactionResult.failResult(proposedValue);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
