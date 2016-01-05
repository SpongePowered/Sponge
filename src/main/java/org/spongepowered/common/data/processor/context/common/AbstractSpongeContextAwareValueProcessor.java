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
package org.spongepowered.common.data.processor.context.common;

import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.context.DataContext;
import org.spongepowered.api.data.context.DataContextual;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.context.ContextAwareValueProcessor;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;

import java.util.Optional;

public abstract class AbstractSpongeContextAwareValueProcessor<Container, Context extends DataContext, E, V extends BaseValue<E>>
    extends AbstractSpongeValueProcessor<Container, E, V> implements ContextAwareValueProcessor<E, V> {

    protected final Class<Context> contextClass;

    protected AbstractSpongeContextAwareValueProcessor(Class<Container> containerClass, Class<Context> contextClass, Key<V> key) {
        super(containerClass, key);
        this.contextClass = contextClass;
    }

    @Override
    public Optional<E> getValueFromContainer(DataContextual contextual, ValueContainer<?> container) {
        if (this.supports(contextual, container)) {
            return this.getVal(contextual, (Context) container);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<V> getApiValueFromContainer(DataContextual contextual, ValueContainer<?> container) {
        final Optional<E> value = this.getValueFromContainer(contextual, container);
        if (value.isPresent()) {
            return Optional.of(this.constructValue(value.get()));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public DataTransactionResult offerToStore(DataContextual contextual, ValueContainer<?> container, E value) {
        final ImmutableValue<E> newValue = this.constructImmutableValue(value);

        if (this.supports(contextual, container)) {
            final DataTransactionResult.Builder builder = DataTransactionResult.builder();
            final Optional<E> oldValue = this.getVal(contextual, (Context) container);

            try {
                if (this.set(contextual, (Context) container, value)) {
                    if (oldValue.isPresent()) {
                        builder.replace(this.constructImmutableValue(oldValue.get()));
                    }

                    return builder.result(DataTransactionResult.Type.SUCCESS).success(newValue).build();
                }
            } catch (Exception e) {
                SpongeImpl.getLogger().debug("An exception occurred when setting data: ", e);
                return builder.result(DataTransactionResult.Type.ERROR).reject(newValue).build();
            }
        }

        return DataTransactionResult.failResult(newValue);
    }

    protected abstract Optional<E> getVal(DataContextual contextual, Context container);

    protected abstract boolean set(DataContextual contextual, Context container, E value);

    @Override
    public boolean supports(ValueContainer<?> container) {
        // Check this.contextClass instead of this.containerClass
        return this.contextClass.isInstance(container) && this.supports((Container) container);
    }

    @Override
    public boolean supports(DataContextual contextual, ValueContainer<?> container) {
        return this.supports(contextual) && this.supports(container);
    }

    protected boolean supports(DataContextual contextual) {
        return true;
    }

}
