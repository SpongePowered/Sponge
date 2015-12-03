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
package org.spongepowered.common.data.context;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.context.ContextViewer;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.context.DataContextual;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.ValueContainer;

import java.util.Optional;

public final class ContextValueProcessorDelegate<E, V extends BaseValue<E>> implements ContextValueProcessor<E, V> {

    private final Key<V> key;
    private final ImmutableList<ContextValueProcessor<E, V>> processors;

    public ContextValueProcessorDelegate(Key<V> key, ImmutableList<ContextValueProcessor<E, V>> processors) {
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
    public Optional<E> getValueFromContainer(DataContextual contextual, ContextViewer viewer, ValueContainer<?> container) {
        for (ContextValueProcessor<E, V> processor : this.processors) {
            if (processor.supports(contextual, viewer, container)) {
                final Optional<E> optional = processor.getValueFromContainer(contextual, viewer, container);
                if (optional.isPresent()) {
                    return optional;
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<V> getApiValueFromContainer(DataContextual contextual, ContextViewer viewer, ValueContainer<?> container) {
        for (ContextValueProcessor<E, V> processor : this.processors) {
            if (processor.supports(contextual, viewer, container)) {
                final Optional<V> optional = processor.getApiValueFromContainer(contextual, viewer, container);
                if (optional.isPresent()) {
                    return optional;
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public boolean supports(DataContextual contextual, ContextViewer viewer, ValueContainer<?> container) {
        for (ContextValueProcessor<E, V> processor : this.processors) {
            if (processor.supports(contextual, viewer, container)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public DataTransactionResult offerToStore(DataContextual contextual, ContextViewer viewer, ValueContainer<?> container, E value) {
        for (ContextValueProcessor<E, V> processor : this.processors) {
            if (processor.supports(contextual, viewer, container)) {
                final DataTransactionResult result = processor.offerToStore(contextual, viewer, container, value);
                if (!result.getType().equals(DataTransactionResult.Type.FAILURE)) {
                    return result;
                }
            }
        }

        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult removeFrom(DataContextual contextual, ContextViewer viewer, ValueContainer<?> container) {
        for (ContextValueProcessor<E, V> processor : this.processors) {
            if (processor.supports(contextual, viewer, container)) {
                final DataTransactionResult result = processor.removeFrom(contextual, viewer, container);
                if (!result.getType().equals(DataTransactionResult.Type.FAILURE)) {
                    return result;
                }
            }
        }

        return DataTransactionResult.failNoData();
    }

}
