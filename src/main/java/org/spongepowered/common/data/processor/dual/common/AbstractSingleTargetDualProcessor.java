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
package org.spongepowered.common.data.processor.dual.common;

import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.DataProcessor;
import org.spongepowered.common.data.ValueProcessor;
import org.spongepowered.common.data.processor.common.AbstractSingleDataSingleTargetProcessor;

import java.util.Optional;

/**
 * An abstract single data target processor that acts both as a {@link ValueProcessor}
 * and a {@link DataProcessor}. This is only useful when the supported {@link DataManipulator}
 * only supports getting/setting a single {@link Value} based on a specific {@link Key}.
 *
 * @param <E> The type of object being supported/processed on
 * @param <L> The type of data value
 * @param <V> The type of value
 * @param <I> The mutable manipulator value type
 * @param <S> The immutable manipulator value type
 */
public abstract class AbstractSingleTargetDualProcessor<E, L, V extends BaseValue<L>, I extends DataManipulator<I, S>,
    S extends ImmutableDataManipulator<S, I>> extends AbstractSingleDataSingleTargetProcessor<E, L, V, I, S> implements ValueProcessor<L, V> {

    public AbstractSingleTargetDualProcessor(Class<E> tClass, Key<V> key) {
        super(key, tClass);
    }

    @Override
    public final Key<? extends BaseValue<L>> getKey() {
        return this.key;
    }

    /**
     * Builds a {@link Value} of the type produced by this processor from an
     * input, actual value.
     *
     * @param actualValue The actual value
     * @return The constructed {@link Value}
     */
    protected abstract V constructValue(L actualValue);

    @SuppressWarnings("unchecked")
    @Override
    public final boolean supports(ValueContainer<?> container) {
        return this.holderClass.isInstance(container) && supports((E) container);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final Optional<L> getValueFromContainer(ValueContainer<?> container) {
        if (!supports(container)) {
            return Optional.empty();
        } else {
            return getVal((E) container);
        }
    }

    @Override
    public Optional<V> getApiValueFromContainer(ValueContainer<?> container) {
        final Optional<L> optionalValue = getValueFromContainer(container);
        if(optionalValue.isPresent()) {
            return Optional.of(constructValue(optionalValue.get()));
        } else {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, L value) {
        final ImmutableValue<L> newValue = constructImmutableValue(value);
        if (supports(container)) {
            final DataTransactionResult.Builder builder = DataTransactionResult.builder();
            final Optional<L> oldVal = getVal((E) container);
            try {
                if (set((E) container, value)) {
                    if (oldVal.isPresent()) {
                        builder.replace(constructImmutableValue(oldVal.get()));
                    }
                    return builder.result(DataTransactionResult.Type.SUCCESS).success(newValue).build();
                }
                return builder.result(DataTransactionResult.Type.FAILURE).reject(newValue).build();
            } catch (Exception e) {
                SpongeImpl.getLogger().debug("An exception occurred when setting data: ", e);
                return builder.result(DataTransactionResult.Type.ERROR).reject(newValue).build();
            }
        }
        return DataTransactionResult.failResult(newValue);
    }

    @Override
    public final DataTransactionResult remove(DataHolder dataHolder) {
        return removeFrom(dataHolder);
    }
}
