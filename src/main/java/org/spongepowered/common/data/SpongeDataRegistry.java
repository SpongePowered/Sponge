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
package org.spongepowered.common.data;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Optional;
import com.google.common.collect.MapMaker;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.DataManipulatorRegistry;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.BaseValue;

import java.util.Map;

@SuppressWarnings("unchecked")
public class SpongeDataRegistry implements DataManipulatorRegistry {

    private static final SpongeDataRegistry instance = new SpongeDataRegistry();
    private final Map<Class<? extends DataManipulator<?, ?>>, DataManipulatorBuilder<?, ?>> builderMap = new MapMaker().concurrencyLevel(4).makeMap();
    private final Map<Class<? extends ImmutableDataManipulator<?, ?>>, DataManipulatorBuilder<?, ?>> immutableBuilderMap =
            new MapMaker().concurrencyLevel(4).makeMap();
    private final Map<Class<? extends DataManipulator<?, ?>>, DataProcessor<?, ?>> processorMap = new MapMaker().concurrencyLevel(4).makeMap();
    private Map<Class<? extends ImmutableDataManipulator<?, ?>>, DataProcessor<?, ?>> immutableProcessorMap =
            new MapMaker().concurrencyLevel(4).makeMap();
    private final Map<Key<? extends BaseValue<?>>, ValueProcessor<?, ?>> valueProcessorMap = new MapMaker().concurrencyLevel(4).makeMap();
    private final Map<Class<? extends DataManipulator<?, ?>>, BlockDataProcessor<?>> blockDataMap = new MapMaker().concurrencyLevel(4).makeMap();
    private final Map<Class<? extends DataManipulator<?, ?>>, BlockValueProcessor<?, ?>> blockValueMap = new MapMaker().concurrencyLevel(4).makeMap();

    private SpongeDataRegistry() {
    }


    public static SpongeDataRegistry getInstance() {
        return SpongeDataRegistry.instance;
    }


    @Override
    public <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> void register(Class<T> manipulatorClass,
            Class<I> immutableManipulatorClass, DataManipulatorBuilder<T, I> builder) {
        if (!this.builderMap.containsKey(checkNotNull(manipulatorClass))) {
            this.builderMap.put(manipulatorClass, checkNotNull(builder));
            this.immutableBuilderMap.put(checkNotNull(immutableManipulatorClass), builder);
        } else {
            throw new IllegalStateException("Already registered the DataUtil for " + manipulatorClass.getCanonicalName());
        }
    }

    @Override
    public <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> Optional<DataManipulatorBuilder<T, I>> getBuilder(Class<T>
            manipulatorClass) {
        return Optional.fromNullable((DataManipulatorBuilder<T, I>) (Object) this.builderMap.get(checkNotNull(manipulatorClass)));
    }

    @Override
    public <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> Optional<DataManipulatorBuilder<T, I>> getBuilderForImmutable(
            Class<I> immutableManipulatorClass) {
        return Optional.fromNullable((DataManipulatorBuilder<T, I>) (Object) this.immutableBuilderMap.get(checkNotNull(immutableManipulatorClass)));
    }

    public <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> void registerDataProcessor(Class<T> manipulatorClass,
            Class<I> immutableManipulatorClass, DataProcessor<T, I> processor) {
        checkState(!this.processorMap.containsKey(checkNotNull(manipulatorClass)), "Already registered a DataProcessor for the given "
                + "DataManipulator: " + manipulatorClass.getCanonicalName());
        this.processorMap.put(manipulatorClass, checkNotNull(processor));
        this.immutableProcessorMap.put(immutableManipulatorClass, processor);
    }

    public <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> void registerDataProcessorAndImpl(Class<T> manipulatorClass,
            Class<? extends T> implClass, Class<I> immutableDataManipulator, Class<? extends I> implImClass, DataProcessor<T, I> processor) {
        checkState(!this.processorMap.containsKey(checkNotNull(manipulatorClass)), "Already registered a DataProcessor for the given "
                + "DataManipulator: " + manipulatorClass.getCanonicalName());
        checkState(!this.processorMap.containsKey(checkNotNull(implClass)), "Already registered a DataProcessor for the given "
                + "DataManipulator: " + implClass.getCanonicalName());
        this.builderMap.put(manipulatorClass, processor);
        this.immutableBuilderMap.put(immutableDataManipulator, processor);
        this.processorMap.put(manipulatorClass, checkNotNull(processor));
        this.processorMap.put(implClass, processor);
        this.immutableProcessorMap.put(immutableDataManipulator, processor);
        this.immutableProcessorMap.put(implImClass, processor);
    }

    public <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> Optional<DataProcessor<T, I>> getUtil(Class<T>
            manipulatorClass) {
        return Optional.fromNullable((DataProcessor<T, I>) (Object) this.processorMap.get(checkNotNull(manipulatorClass)));
    }

    public <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> void registerBlockProcessor(Class<T> manipulatorclass,
            BlockDataProcessor<T> util) {
        if (!this.blockDataMap.containsKey(checkNotNull(manipulatorclass))) {
            this.blockDataMap.put(manipulatorclass, checkNotNull(util));
        } else {
            throw new IllegalStateException("Already registered a SpongeBlockProcessor for the given DataManipulator: " + manipulatorclass
                    .getCanonicalName());
        }
    }

    public <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> void registerBlockProcessorAndImpl(Class<T> manipulatorClass,
            Class<? extends T> implClass,
            Class<I> immutableManipulatorClass, Class<? extends I> implImmManClass, BlockDataProcessor<T> processor) {
        checkState(!this.blockDataMap.containsKey(checkNotNull(manipulatorClass)), "Already registered a DataProcessor for the given "
                + "DataManipulator: " + manipulatorClass.getCanonicalName());
        checkState(!this.blockDataMap.containsKey(checkNotNull(implClass)), "Already registered a DataProcessor for the given "
                + "DataManipulator: " + implClass.getCanonicalName());
        this.blockDataMap.put(manipulatorClass, checkNotNull(processor));
        this.blockDataMap.put(implClass, processor);
    }


    public <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> Optional<DataProcessor<T, I>> getProcessor(
            Class<T> mutableClass) {
        return Optional.fromNullable((DataProcessor<T, I>) (Object) this.processorMap.get(checkNotNull(mutableClass)));
    }

    public Optional<DataProcessor<?, ?>> getWildProcessor(Class<? extends DataManipulator<?, ?>> mutableClass) {
        return Optional.<DataProcessor<?, ?>>fromNullable(this.processorMap.get(checkNotNull(mutableClass)));
    }

    public <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> Optional<DataProcessor<T, I>> getImmutableProcessor(Class<I>
            immutableClass) {
        return Optional.fromNullable((DataProcessor<T, I>) (Object) this.immutableProcessorMap.get(checkNotNull(immutableClass)));
    }

    public <T extends DataManipulator<T, ?>> Optional<BlockDataProcessor<T>> getBlockDataFor(Class<T>
            manipulatorClass) {
        return Optional.fromNullable((BlockDataProcessor<T>) (Object) this.blockDataMap.get(checkNotNull(manipulatorClass)));
    }

    public <E, V extends BaseValue<E>> void registerValueProcessor(Key<V> key, ValueProcessor<E, V> valueProcessor) {
        checkState(!this.valueProcessorMap.containsKey(key), "Already have a registered value processor for that key!");
        this.valueProcessorMap.put(key, valueProcessor);
    }


    public <E, V extends BaseValue<E>> Optional<ValueProcessor<E, V>> getValueProcessor(Key<V> key) {
        return Optional.fromNullable((ValueProcessor<E, V>) (Object) this.valueProcessorMap.get(key));
    }

    public Optional<ValueProcessor<?, ?>> getWildValueProcessor(Key<?> key) {
        return Optional.<ValueProcessor<?, ?>>fromNullable(this.valueProcessorMap.get(key));
    }

    public <E> Optional<ValueProcessor<E, ? extends BaseValue<E>>> getBaseValueProcessor(Key<? extends BaseValue<E>> key) {
        return Optional.<ValueProcessor<E, ? extends BaseValue<E>>>fromNullable(
            (ValueProcessor<E, ? extends BaseValue<E>>) (Object) this.valueProcessorMap.get
                (key));
    }
}
