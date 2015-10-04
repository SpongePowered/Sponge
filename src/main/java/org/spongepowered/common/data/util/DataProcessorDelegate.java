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
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.common.data.DataProcessor;

import java.util.Optional;

public final class DataProcessorDelegate<M extends DataManipulator<M, I>, I extends ImmutableDataManipulator<I, M>> implements DataProcessor<M, I> {

    private final ImmutableList<DataProcessor<M, I>> processors;

    public DataProcessorDelegate(ImmutableList<DataProcessor<M, I>> processors) {
        this.processors = processors;
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean supports(DataHolder dataHolder) {
        for (DataProcessor<M, I> processor : this.processors) {
            if (processor.supports(dataHolder)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean supports(EntityType entityType) {
        return false;
    }

    @Override
    public Optional<M> from(DataHolder dataHolder) {
        for (DataProcessor<M, I> processor : this.processors) {
            if (processor.supports(dataHolder)) {
                final Optional<M> optional = processor.from(dataHolder);
                if (optional.isPresent()) {
                    return optional;
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<M> fill(DataHolder dataHolder, M manipulator, MergeFunction overlap) {
        for (DataProcessor<M, I> processor : this.processors) {
            if (processor.supports(dataHolder)) {
                final Optional<M> optional = processor.fill(dataHolder, manipulator, overlap);
                if (optional.isPresent()) {
                    return optional;
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<M> fill(DataContainer container, M m) {
        for (DataProcessor<M, I> processor : this.processors) {
            final Optional<M> optional = processor.fill(container, m);
            if (optional.isPresent()) {
                return optional;
            }
        }
        return Optional.empty();
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, M manipulator, MergeFunction function) {
        for (DataProcessor<M, I> processor : this.processors) {
            if (processor.supports(dataHolder)) {
                final DataTransactionResult result = processor.set(dataHolder, manipulator, function);
                if (!result.getType().equals(DataTransactionResult.Type.FAILURE)) {
                    return result;
                }
            }
        }
        return DataTransactionBuilder.failNoData();
    }

    @Override
    public Optional<I> with(Key<? extends BaseValue<?>> key, Object value, I immutable) {
        for (DataProcessor<M, I> processor : this.processors) {
            final Optional<I> optional = processor.with(key, value, immutable);
            if (optional.isPresent()) {
                return optional;
            }
        }
        return Optional.empty();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        for (DataProcessor<M, I> processor : this.processors) {
            if (processor.supports(dataHolder)) {
                final DataTransactionResult result = processor.remove(dataHolder);
                if (!result.getType().equals(DataTransactionResult.Type.FAILURE)) {
                    return result;
                }
            }
        }
        return DataTransactionBuilder.failNoData();
    }

    @Override
    public Optional<M> createFrom(DataHolder dataHolder) {
        for (DataProcessor<M, I> processor : this.processors) {
            if (processor.supports(dataHolder)) {
                final Optional<M> optional = processor.createFrom(dataHolder);
                if (optional.isPresent()) {
                    return optional;
                }
            }
        }
        return Optional.empty();
    }

}
