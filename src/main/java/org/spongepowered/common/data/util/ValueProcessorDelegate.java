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
package org.spongepowered.common.data.util;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.ValueProcessor;

import java.util.Optional;

/**
 * This is really just a lazy class to handle processing on multiple
 * {@link ValueProcessor} registrations.
 *
 * @param <E>
 * @param <V>
 */
public final class ValueProcessorDelegate<E, V extends BaseValue<E>> implements ValueProcessor<E, V> {

    private final Key<V> key;
    private final ImmutableList<ValueProcessor<E, V>> processors;

    public ValueProcessorDelegate(Key<V> key, ImmutableList<ValueProcessor<E, V>> processors) {
        this.key = key;
        this.processors = processors;
    }

    @Override
    public Key<? extends BaseValue<E>> getKey() {
        return this.key;
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Optional<E> getValueFromContainer(ValueContainer<?> container) {
        for (ValueProcessor<E, V> processor : this.processors) {
            if (processor.supports(container)) {
                final Optional<E> optional = processor.getValueFromContainer(container);
                if (optional.isPresent()) {
                    return optional;
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<V> getApiValueFromContainer(ValueContainer<?> container) {
        for (ValueProcessor<E, V> processor : this.processors) {
            if (processor.supports(container)) {
                final Optional<V> optional = processor.getApiValueFromContainer(container);
                if (optional.isPresent()) {
                    return optional;
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(ValueContainer<?> container) {
        for (ValueProcessor<E, V> processor : this.processors) {
            if (processor.supports(container)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, E value) {
        for (ValueProcessor<E, V> processor : this.processors) {
            if (processor.supports(container)) {
                final DataTransactionResult result = processor.offerToStore(container, value);
                if (!result.getType().equals(DataTransactionResult.Type.FAILURE)) {
                    return result;
                }
            }
        }
        for (ValueProcessor<E, V> processor : this.processors) {
            if (processor.supports(container)) {
                final Optional<V> currentValueOptional = processor.getApiValueFromContainer(container);
                if (currentValueOptional.isPresent()) {
                    V currentValue = currentValueOptional.get();
                    ImmutableValue<?> rejectedValue;
                    if (currentValue instanceof Value<?>) {
                        Value<E> mutableCurrentValue = (Value<E>) currentValue;
                        mutableCurrentValue.set(value);
                        rejectedValue = mutableCurrentValue.asImmutable();
                    } else {
                        ImmutableValue<E> immutableCurrentValue = (ImmutableValue<E>) currentValue;
                        rejectedValue = immutableCurrentValue.with(value);
                    }
                    return DataTransactionResult.failResult(rejectedValue);
                }
            }
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        for (ValueProcessor<E, V> processor : this.processors) {
            if (processor.supports(container)) {
                final DataTransactionResult result = processor.removeFrom(container);
                if (!result.getType().equals(DataTransactionResult.Type.FAILURE)) {
                    return result;
                }
            }
        }
        return DataTransactionResult.failNoData();
    }
}
