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

import co.aikar.timings.SpongeTimingsFactory;
import co.aikar.timings.Timing;
import com.google.common.collect.ImmutableList;
import org.spongepowered.api.context.ContextViewer;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.context.DataContext;
import org.spongepowered.api.data.context.DataContextual;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.SpongeImpl;

import java.util.Optional;

public final class ContextDataProcessorDelegate<M extends DataManipulator<M, I>, I extends ImmutableDataManipulator<I, M>> implements ContextDataProcessor<M, I> {

    private final ImmutableList<Tuple<ContextDataProcessor<M, I>, Timing>> processors;

    public ContextDataProcessorDelegate(ImmutableList<ContextDataProcessor<M, I>> processors) {
        ImmutableList.Builder<Tuple<ContextDataProcessor<M, I>, Timing>> builder = ImmutableList.builder();
        for (ContextDataProcessor<M, I> processor : processors) {
            builder.add(new Tuple<>(processor, SpongeTimingsFactory.ofSafe(SpongeImpl.getPlugin(), processor.getClass().getCanonicalName())));
        }

        this.processors = builder.build();
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean supports(DataContextual contextual, ContextViewer viewer, DataContext context) {
        for (Tuple<ContextDataProcessor<M, I>, Timing> tuple : this.processors) {
            tuple.getSecond().startTiming();

            if (tuple.getFirst().supports(contextual, viewer, context)) {
                tuple.getSecond().stopTiming();
                return true;
            }

            tuple.getSecond().stopTiming();
        }

        return false;
    }

    @Override
    public Optional<M> from(DataContextual contextual, ContextViewer viewer, DataContext context) {
        for (Tuple<ContextDataProcessor<M, I>, Timing> tuple : this.processors) {
            tuple.getSecond().startTiming();

            if (tuple.getFirst().supports(contextual, viewer, context)) {
                final Optional<M> optional = tuple.getFirst().from(contextual, viewer, context);
                tuple.getSecond().stopTiming();
                if (optional.isPresent()) {
                    return optional;
                }
            }

            tuple.getSecond().stopTiming();
        }

        return Optional.empty();
    }

    @Override
    public Optional<M> createFrom(DataContextual contextual, ContextViewer viewer, DataContext context) {
        for (Tuple<ContextDataProcessor<M, I>, Timing> tuple : this.processors) {
            tuple.getSecond().startTiming();

            if (tuple.getFirst().supports(contextual, viewer, context)) {
                final Optional<M> optional = tuple.getFirst().createFrom(contextual, viewer, context);
                tuple.getSecond().stopTiming();
                if (optional.isPresent()) {
                    return optional;
                }
            }

            tuple.getSecond().stopTiming();
        }

        return Optional.empty();
    }

    @Override
    public DataTransactionResult set(DataContextual contextual, ContextViewer viewer, DataContext context, M manipulator, MergeFunction function) {
        for (Tuple<ContextDataProcessor<M, I>, Timing> tuple : this.processors) {
            tuple.getSecond().startTiming();

            if (tuple.getFirst().supports(contextual, viewer, context)) {
                final DataTransactionResult result = tuple.getFirst().set(contextual, viewer, context, manipulator, function);
                if (!result.getType().equals(DataTransactionResult.Type.FAILURE)) {
                    tuple.getSecond().stopTiming();
                    return result;
                }
            }

            tuple.getSecond().stopTiming();
        }

        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult remove(DataContextual contextual, ContextViewer viewer, DataContext context) {
        for (Tuple<ContextDataProcessor<M, I>, Timing> tuple : this.processors) {
            tuple.getSecond().startTiming();

            if (tuple.getFirst().supports(contextual, viewer, context)) {
                final DataTransactionResult result = tuple.getFirst().remove(contextual, viewer, context);
                tuple.getSecond().stopTiming();
                if (!result.getType().equals(DataTransactionResult.Type.FAILURE)) {
                    return result;
                }
            }

            tuple.getSecond().stopTiming();
        }

        return DataTransactionResult.failNoData();
    }

}
