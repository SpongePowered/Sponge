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
package org.spongepowered.common.data.processor.common;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.Value.Immutable;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.ValueProcessor;

import java.util.Optional;

public abstract class AbstractSpongeValueProcessor<C, E, V extends Value<E>> implements ValueProcessor<E, V> {

    private final Class<C> containerClass;
    protected final Key<V> key;

    protected AbstractSpongeValueProcessor(Class<C> containerClass, Key<V> key) {
        this.key = checkNotNull(key, "The key is null!");
        this.containerClass = containerClass;
    }

    /**
     * Builds a {@link Value} of the type produced by this processor from an
     * input, actual value.
     *
     * @param actualValue The actual value
     * @return The constructed {@link Value}
     */
    protected abstract V constructValue(E actualValue);

    protected abstract boolean set(C container, E value);

    protected abstract Optional<E> getVal(C container);

    protected abstract Immutable<E> constructImmutableValue(E value);

    protected boolean supports(C container) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean supports(ValueContainer<?> container) {
        return this.containerClass.isInstance(container) && this.supports((C) container);
    }


    @Override
    public final Key<? extends Value<E>> getKey() {
        return this.key;
    }

    @Override
    public int getPriority() {
        return 100;
    }


    @SuppressWarnings("unchecked")
    @Override
    public Optional<E> getValueFromContainer(ValueContainer<?> container) {
        if (!this.supports(container)) {
            return Optional.empty();
        }
        return this.getVal((C) container);
    }

    @Override
    public Optional<V> getApiValueFromContainer(ValueContainer<?> container) {
        final Optional<E> optionalValue = this.getValueFromContainer(container);
        if(optionalValue.isPresent()) {
            return Optional.of(this.constructValue(optionalValue.get()));
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, E value) {
        final Immutable<E> newValue = this.constructImmutableValue(value);
        if (this.supports(container)) {
            final DataTransactionResult.Builder builder = DataTransactionResult.builder();
            final Optional<E> oldVal = this.getVal((C) container);
            try {
                if (this.set((C) container, value)) {
                    if (oldVal.isPresent()) {
                        builder.replace(this.constructImmutableValue(oldVal.get()));
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

}
