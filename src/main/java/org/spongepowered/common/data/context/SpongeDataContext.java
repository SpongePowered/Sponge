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

import static com.google.common.base.Preconditions.checkNotNull;

import co.aikar.timings.SpongeTimings;
import co.aikar.timings.Timing;
import com.google.common.collect.Lists;
import org.spongepowered.api.context.ContextViewer;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.context.DataContext;
import org.spongepowered.api.data.context.DataContextual;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.util.GuavaCollectors;
import org.spongepowered.common.data.SpongeDataManager;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SpongeDataContext implements DataContext {

    private List<DataManipulator<?, ?>> manipulators = Lists.newArrayList();
    private boolean dirtyManipulators = true;
    protected final DataContextual contextual;
    protected final ContextViewer viewer;

    public SpongeDataContext(DataContextual contextual, ContextViewer viewer) {
        this.contextual = checkNotNull(contextual, "contextual");
        this.viewer = checkNotNull(viewer, "viewer");
    }

    @Override
    public DataContextual getContextual() {
        return this.contextual;
    }

    @Override
    public ContextViewer getViewer() {
        return this.viewer;
    }

    @Override
    public <T extends DataManipulator<?, ?>> Optional<T> get(Class<T> containerClass) {
        try (Timing timing = SpongeTimings.dataGetManipulator.startTiming()) {
            final Optional<ContextDataProcessor<?, ?>> optional = SpongeDataManager.getInstance().getWildContextProcessor(checkNotNull(containerClass, "container class"));
            if (optional.isPresent()) {
                this.dirtyManipulators = true;
                return (Optional<T>) optional.get().from(this.contextual, this.viewer, this);
            }

            return Optional.empty();
        }
    }

    @Override
    public <T extends DataManipulator<?, ?>> Optional<T> getOrCreate(Class<T> containerClass) {
        try (Timing timing = SpongeTimings.dataGetOrCreateManipulator.startTiming()) {
            final Optional<ContextDataProcessor<?, ?>> optional = SpongeDataManager.getInstance().getWildContextProcessor(checkNotNull(containerClass, "container class"));
            if (optional.isPresent()) {
                this.dirtyManipulators = true;
                return (Optional<T>) optional.get().createFrom(this.contextual, this.viewer, this);
            }

            return Optional.empty();
        }
    }

    @Override
    public boolean supports(Class<? extends DataManipulator<?, ?>> holderClass) {
        try (Timing timing = SpongeTimings.dataSupportsManipulator.startTiming()) {
            final Optional<ContextDataProcessor<?, ?>> optional = SpongeDataManager.getInstance().getWildContextProcessor(checkNotNull(holderClass, "holder class"));
            return optional.isPresent() && optional.get().supports(this.contextual, this.viewer, this);
        }
    }

    @Override
    public <E> DataTransactionResult offer(Key<? extends BaseValue<E>> key, E value) {
        try (Timing timing = SpongeTimings.dataOfferKey.startTiming()) {
            final Optional<ContextValueProcessor<E, ? extends BaseValue<E>>> optional = SpongeDataManager.getInstance().getBaseContextValueProcessor(checkNotNull(key, "key"));
            if (optional.isPresent()) {
                this.dirtyManipulators = true;
                return optional.get().offerToStore(this.contextual, this.viewer, this, value);
            }

            return DataTransactionResult.builder().result(DataTransactionResult.Type.FAILURE).build();
        }
    }

    @Override
    public <E> DataTransactionResult offer(Key<? extends BaseValue<E>> key, E value, Cause cause) {
        return this.offer(key, value);
    }

    @Override
    public DataTransactionResult offer(DataManipulator<?, ?> valueContainer, MergeFunction function) {
        try (Timing timing = SpongeTimings.dataOfferManipulator.startTiming()) {
            final Optional<ContextDataProcessor> optional = SpongeDataManager.getInstance().getWildContextDataProcessor(checkNotNull(valueContainer, "value container").getClass());
            if (optional.isPresent()) {
                this.dirtyManipulators = true;
                return optional.get().set(this.contextual, this.viewer, this, valueContainer, checkNotNull(function));
            }

            return DataTransactionResult.failResult(valueContainer.getValues());
        }
    }

    @Override
    public DataTransactionResult offer(DataManipulator<?, ?> valueContainer, MergeFunction function, Cause cause) {
        return this.offer(valueContainer, function);
    }

    @Override
    public DataTransactionResult remove(Class<? extends DataManipulator<?, ?>> containerClass) {
        try (Timing timing = SpongeTimings.dataRemoveManipulator.startTiming()) {
            final Optional<ContextDataProcessor<?, ?>> optional = SpongeDataManager.getInstance().getWildContextProcessor(checkNotNull(containerClass, "container class"));
            if (optional.isPresent()) {
                this.dirtyManipulators = true;
                return optional.get().remove(this.contextual, this.viewer, this);
            }

            return DataTransactionResult.failNoData();
        }
    }

    @Override
    public DataTransactionResult remove(Key<?> key) {
        try (Timing timing = SpongeTimings.dataRemoveKey.startTiming()) {
            final Optional<ContextValueProcessor<?, ?>> optional = SpongeDataManager.getInstance().getWildContextValueProcessor(checkNotNull(key, "key"));
            if (optional.isPresent()) {
                this.dirtyManipulators = true;
                return optional.get().removeFrom(this.contextual, this.viewer, this);
            }

            return DataTransactionResult.failNoData();
        }
    }

    @Override
    public DataTransactionResult undo(DataTransactionResult result) {
        checkNotNull(result, "result");
        try (Timing timing = SpongeTimings.dataOfferManipulator.startTiming()) {
            if (result.getReplacedData().isEmpty() && result.getSuccessfulData().isEmpty()) {
                return DataTransactionResult.successNoData();
            }

            final DataTransactionResult.Builder builder = DataTransactionResult.builder();
            for (ImmutableValue<?> replaced : result.getReplacedData()) {
                builder.absorbResult(this.offer(replaced));
            }

            for (ImmutableValue<?> successful : result.getSuccessfulData()) {
                builder.absorbResult(this.remove(successful));
            }

            return builder.build();
        }
    }

    @Override
    public DataTransactionResult copyFrom(DataContext that, MergeFunction function) {
        return this.offer(that.getContainers(), function);
    }

    @Override
    public Collection<DataManipulator<?, ?>> getContainers() {
        this.recalculateManipulatorList();
        return this.manipulators.stream().collect(GuavaCollectors.toImmutableList());
    }

    protected void collectManipulators(List<DataManipulator<?, ?>> manipulators) {
    }

    @Override
    public <E> Optional<E> get(Key<? extends BaseValue<E>> key) {
        try (Timing timing = SpongeTimings.dataGetByKey.startTiming()) {
            final Optional<ContextValueProcessor<E, ? extends BaseValue<E>>> optional =
                    SpongeDataManager.getInstance().getBaseContextValueProcessor(checkNotNull(key, "key"));
            if (optional.isPresent()) {
                this.dirtyManipulators = true;
                return optional.get().getValueFromContainer(this.contextual, this.viewer, this);
            }

            return Optional.empty();
        }
    }

    @Override
    public <E, V extends BaseValue<E>> Optional<V> getValue(Key<V> key) {
        try (Timing timing = SpongeTimings.dataGetValue.startTiming()) {
            final Optional<ContextValueProcessor<E, V>> optional = SpongeDataManager.getInstance().getContextValueProcessor(checkNotNull(key, "key"));
            if (optional.isPresent()) {
                this.dirtyManipulators = true;
                return optional.get().getApiValueFromContainer(this.contextual, this.viewer, this);
            }

            return Optional.empty();
        }
    }

    @Override
    public boolean supports(Key<?> key) {
        try (Timing timing = SpongeTimings.dataSupportsKey.startTiming()) {
            final Optional<ContextValueProcessor<?, ?>> optional = SpongeDataManager.getInstance().getWildContextValueProcessor(checkNotNull(key, "key"));
            return optional.isPresent() && optional.get().supports(this.contextual, this.viewer, this);
        }
    }

    @Override
    public DataContext copy() {
        return new SpongeDataContext(this.contextual, this.viewer);
    }

    @Override
    public Set<Key<?>> getKeys() {
        this.recalculateManipulatorList();
        return this.manipulators.stream()
                .map(ValueContainer::getKeys)
                .flatMap(Collection::stream)
                .collect(GuavaCollectors.toImmutableSet());
    }

    @Override
    public Set<ImmutableValue<?>> getValues() {
        this.recalculateManipulatorList();
        return this.manipulators.stream()
                .map(ValueContainer::getValues)
                .flatMap(Collection::stream)
                .collect(GuavaCollectors.toImmutableSet());
    }

    private void recalculateManipulatorList() {
        if (this.dirtyManipulators) {
            this.manipulators = Lists.newArrayList();
            this.collectManipulators(this.manipulators);
            this.dirtyManipulators = false;
        }
    }

}
