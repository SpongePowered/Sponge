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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.DataManipulatorRegistry;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.common.data.util.BlockDataProcessorDelegate;
import org.spongepowered.common.data.util.BlockValueProcessorDelegate;
import org.spongepowered.common.data.util.ComparatorUtil;
import org.spongepowered.common.data.util.DataProcessorDelegate;
import org.spongepowered.common.data.util.ValueProcessorDelegate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("unchecked")
public final class SpongeDataRegistry implements DataManipulatorRegistry {

    private static final SpongeDataRegistry instance = new SpongeDataRegistry();

    // Builders

    private final Map<Class<? extends DataManipulator<?, ?>>, DataManipulatorBuilder<?, ?>> builderMap = new MapMaker()
        .concurrencyLevel(4)
        .makeMap();
    private final Map<Class<? extends ImmutableDataManipulator<?, ?>>, DataManipulatorBuilder<?, ?>> immutableBuilderMap = new MapMaker()
        .concurrencyLevel(4)
        .makeMap();

    // Registrations

    private final Map<Class<? extends DataManipulator<?, ?>>, List<DataProcessor<?, ?>>> processorMap = new MapMaker()
        .concurrencyLevel(4)
        .makeMap();
    private final Map<Class<? extends ImmutableDataManipulator<?, ?>>, List<DataProcessor<?, ?>>> immutableProcessorMap = new MapMaker()
        .concurrencyLevel(4)
        .makeMap();
    private final Map<Key<? extends BaseValue<?>>, List<ValueProcessor<?, ?>>> valueProcessorMap = new MapMaker()
        .concurrencyLevel(4)
        .makeMap();
    private final Map<Class<? extends ImmutableDataManipulator<?, ?>>, List<BlockDataProcessor<?>>> blockDataMap = new MapMaker()
        .concurrencyLevel(4)
        .makeMap();
    private final Map<Key<? extends BaseValue<?>>, List<BlockValueProcessor<?, ?>>> blockValueMap = new MapMaker()
        .concurrencyLevel(4)
        .makeMap();

    // Processor delegates

    private final Map<Key<? extends BaseValue<?>>, ValueProcessorDelegate<?, ?>> valueDelegates = new MapMaker()
        .concurrencyLevel(4)
        .makeMap();
    private final Map<Class<? extends DataManipulator<?, ?>>, DataProcessorDelegate<?, ?>> dataProcessorDelegates = new MapMaker()
        .concurrencyLevel(4)
        .makeMap();
    private final Map<Class<? extends ImmutableDataManipulator<?, ?>>, DataProcessorDelegate<?, ?>> immutableDataProcessorDelegates = new MapMaker()
        .concurrencyLevel(4)
        .makeMap();
    private final Map<Class<? extends ImmutableDataManipulator<?, ?>>, BlockDataProcessorDelegate<?>> blockDataProcessorDelegates = new MapMaker()
        .concurrencyLevel(4)
        .makeMap();
    private final Map<Key<? extends BaseValue<?>>, BlockValueProcessorDelegate<?, ?>> blockValueProcessorDelegates = new MapMaker()
        .concurrencyLevel(4)
        .makeMap();

    private static boolean allowRegistrations = true;

    private SpongeDataRegistry() {
    }


    public static SpongeDataRegistry getInstance() {
        return SpongeDataRegistry.instance;
    }

    public static void finalizeRegistration() {
        allowRegistrations = false;
        final SpongeDataRegistry registry = instance;
        for (Map.Entry<Key<? extends BaseValue<?>>, List<ValueProcessor<?, ?>>> entry : registry.valueProcessorMap.entrySet()) {
            ImmutableList.Builder<ValueProcessor<?, ?>> valueListBuilder = ImmutableList.builder();
            Collections.sort(entry.getValue(), ComparatorUtil.VALUE_PROCESSOR_COMPARATOR);
            valueListBuilder.addAll(entry.getValue());
            final ValueProcessorDelegate<?, ?> delegate = new ValueProcessorDelegate(entry.getKey(), valueListBuilder.build());
            registry.valueDelegates.put(entry.getKey(), delegate);
        }
        registry.valueProcessorMap.clear();
        for (Map.Entry<Class<? extends DataManipulator<?, ?>>, List<DataProcessor<?, ?>>> entry : registry.processorMap.entrySet()) {
            ImmutableList.Builder<DataProcessor<?, ?>> dataListBuilder = ImmutableList.builder();
            Collections.sort(entry.getValue(), ComparatorUtil.DATA_PROCESSOR_COMPARATOR);
            dataListBuilder.addAll(entry.getValue());
            final DataProcessorDelegate<?, ?> delegate = new DataProcessorDelegate(dataListBuilder.build());
            registry.dataProcessorDelegates.put(entry.getKey(), delegate);
        }
        registry.processorMap.clear();
        for (Map.Entry<Class<? extends ImmutableDataManipulator<?, ?>>, List<DataProcessor<?, ?>>> entry : registry.immutableProcessorMap.entrySet()) {
            ImmutableList.Builder<DataProcessor<?, ?>> dataListBuilder = ImmutableList.builder();
            Collections.sort(entry.getValue(), ComparatorUtil.DATA_PROCESSOR_COMPARATOR);
            dataListBuilder.addAll(entry.getValue());
            final DataProcessorDelegate<?, ?> delegate = new DataProcessorDelegate(dataListBuilder.build());
            registry.immutableDataProcessorDelegates.put(entry.getKey(), delegate);
        }
        registry.immutableProcessorMap.clear();
        for (Map.Entry<Class<? extends ImmutableDataManipulator<?, ?>>, List<BlockDataProcessor<?>>> entry : registry.blockDataMap.entrySet()) {
            ImmutableList.Builder<BlockDataProcessor<?>> dataListBuilder = ImmutableList.builder();
            Collections.sort(entry.getValue(), ComparatorUtil.BLOCK_DATA_PROCESSOR_COMPARATOR);
            dataListBuilder.addAll(entry.getValue());
            final BlockDataProcessorDelegate<?> delegate = new BlockDataProcessorDelegate(dataListBuilder.build());
            registry.blockDataProcessorDelegates.put(entry.getKey(), delegate);
        }
        registry.blockDataMap.clear();
        for (Map.Entry<Key<? extends BaseValue<?>>, List<BlockValueProcessor<?, ?>>> entry : registry.blockValueMap.entrySet()) {
            ImmutableList.Builder<BlockValueProcessor<?, ?>> valueListBuilder = ImmutableList.builder();
            Collections.sort(entry.getValue(), ComparatorUtil.BLOCK_VALUE_PROCESSOR_COMPARATOR);
            valueListBuilder.addAll(entry.getValue());
            final BlockValueProcessorDelegate<?, ?> delegate = new BlockValueProcessorDelegate(entry.getKey(), valueListBuilder.build());
            registry.blockValueProcessorDelegates.put(entry.getKey(), delegate);
        }
        registry.blockValueMap.clear();

    }


    @Override
    public <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> void register(Class<T> manipulatorClass,
                                                                                                     Class<I> immutableManipulatorClass,
                                                                                                     DataManipulatorBuilder<T, I> builder) {
        checkState(allowRegistrations, "Registrations are no longer allowed!");
        if (!this.builderMap.containsKey(checkNotNull(manipulatorClass))) {
            this.builderMap.put(manipulatorClass, checkNotNull(builder));
            this.immutableBuilderMap.put(checkNotNull(immutableManipulatorClass), builder);
        } else {
            throw new IllegalStateException("Already registered the DataUtil for " + manipulatorClass.getCanonicalName());
        }
    }

    @Override
    public <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> Optional<DataManipulatorBuilder<T, I>>
    getBuilder(Class<T> manipulatorClass) {
        return Optional.ofNullable((DataManipulatorBuilder<T, I>) (Object) this.builderMap.get(checkNotNull(manipulatorClass)));
    }

    @Override
    public <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> Optional<DataManipulatorBuilder<T, I>>
    getBuilderForImmutable(Class<I> immutableManipulatorClass) {
        return Optional.ofNullable((DataManipulatorBuilder<T, I>) (Object) this.immutableBuilderMap.get(checkNotNull(immutableManipulatorClass)));
    }

    public Optional<DataManipulatorBuilder<?, ?>> getWildBuilderForImmutable(Class<? extends ImmutableDataManipulator<?, ?>> immutable) {
        return Optional.<DataManipulatorBuilder<?, ?>>fromNullable((DataManipulatorBuilder<?, ?>) (Object) this.immutableBuilderMap.get(checkNotNull(immutable)));
    }

    public <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> void
    registerDataProcessorAndImpl(Class<T> manipulatorClass, Class<? extends T> implClass, Class<I> immutableDataManipulator,
                                 Class<? extends I> implImClass, DataProcessor<T, I> processor, DataManipulatorBuilder<T, I> builder) {
        checkState(allowRegistrations, "Registrations are no longer allowed!");
        checkState(!this.processorMap.containsKey(checkNotNull(manipulatorClass)), "Already registered a DataProcessor for the given "
                                                                                   + "DataManipulator: " + manipulatorClass.getCanonicalName());
        checkState(!this.processorMap.containsKey(checkNotNull(implClass)), "Already registered a DataProcessor for the given "
                                                                            + "DataManipulator: " + implClass.getCanonicalName());
        checkArgument(!(processor instanceof DataProcessorDelegate), "Cannot register DataProcessorDelegates!");
        if (!this.builderMap.containsKey(manipulatorClass)) {
            this.builderMap.put(manipulatorClass, builder);
            this.immutableBuilderMap.put(immutableDataManipulator, builder);
        }

        List<DataProcessor<?, ?>> processorList = this.processorMap.get(manipulatorClass);
        if (processorList == null) {
            processorList = Collections.synchronizedList(Lists.<DataProcessor<?, ?>>newArrayList());
            this.processorMap.put(manipulatorClass, processorList);
            this.processorMap.put(implClass, processorList);
        }
        checkArgument(!processorList.contains(processor), "Duplicate DataProcessor Registration!");
        processorList.add(processor);

        List<DataProcessor<?, ?>> immutableProcessorList = this.immutableProcessorMap.get(immutableDataManipulator);
        if (immutableProcessorList == null) {
            immutableProcessorList = Collections.synchronizedList(Lists.<DataProcessor<?, ?>>newArrayList());
            this.immutableProcessorMap.put(immutableDataManipulator, processorList);
            this.immutableProcessorMap.put(implImClass, processorList);
        }
        checkArgument(!immutableProcessorList.contains(processor), "Duplicate DataProcessor Registration!");
        immutableProcessorList.add(processor);
    }

    public <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> void registerBlockProcessorAndImpl(Class<I> manipulatorClass,
                                                                                                                          Class<? extends I> implClass,
                                                                                                                          BlockDataProcessor<I> processor) {
        checkState(allowRegistrations, "Registrations are no longer allowed!");
        checkState(!this.blockDataMap.containsKey(checkNotNull(manipulatorClass)), "Already registered a DataProcessor for the given "
                                                                                   + "ImmutableDataManipulator: "
                                                                                   + manipulatorClass.getCanonicalName());
        checkState(!this.blockDataMap.containsKey(checkNotNull(implClass)), "Already registered a DataProcessor for the given "
                                                                            + "DataManipulator: " + implClass.getCanonicalName());
        List<BlockDataProcessor<?>> processorList = this.blockDataMap.get(manipulatorClass);
        if (processorList == null) {
            processorList = Collections.synchronizedList(Lists.<BlockDataProcessor<?>>newArrayList());
            this.blockDataMap.put(manipulatorClass, processorList);
            this.blockDataMap.put(implClass, processorList);
        }
        checkArgument(!processorList.contains(processor), "Duplicate DataProcessor Registration!");
        processorList.add(processor);
    }


    public <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> Optional<DataProcessor<T, I>> getProcessor(
        Class<T> mutableClass) {
        return Optional.ofNullable((DataProcessor<T, I>) (Object) this.dataProcessorDelegates.get(checkNotNull(mutableClass)));
    }

    public Optional<DataProcessor<?, ?>> getWildProcessor(Class<? extends DataManipulator<?, ?>> mutableClass) {
        return Optional.<DataProcessor<?, ?>>ofNullable(this.dataProcessorDelegates.get(checkNotNull(mutableClass)));
    }

    @SuppressWarnings("rawtypes")
    public Optional<DataProcessor> getWildDataProcessor(Class<? extends DataManipulator> class1) {
        return Optional.<DataProcessor>ofNullable(this.dataProcessorDelegates.get(checkNotNull(class1)));
    }

    public <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> Optional<DataProcessor<T, I>>
    getImmutableProcessor(Class<I> immutableClass) {
        return Optional.ofNullable((DataProcessor<T, I>) (Object) this.immutableDataProcessorDelegates.get(checkNotNull(immutableClass)));
    }

    @SuppressWarnings("rawtypes")
    public Optional<DataProcessor> getWildImmutableProcessor(Class<? extends ImmutableDataManipulator<?, ?>> immutableClass) {
        return Optional.<DataProcessor>fromNullable(this.immutableDataProcessorDelegates.get(checkNotNull(immutableClass)));
    }

    public <I extends ImmutableDataManipulator<I, ?>> Optional<BlockDataProcessor<I>> getBlockDataFor(Class<I> manipulatorClass) {
        return Optional.ofNullable((BlockDataProcessor<I>) (Object) this.blockDataProcessorDelegates.get(checkNotNull(manipulatorClass)));
    }

    @SuppressWarnings("rawtypes")
    public Optional<BlockDataProcessor> getWildBlockDataProcessor(Class<? extends ImmutableDataManipulator> immutableClass) {
        return Optional.<BlockDataProcessor>fromNullable(this.blockDataProcessorDelegates.get(checkNotNull(immutableClass)));
    }


    public <E, V extends BaseValue<E>> void registerValueProcessor(Key<V> key, ValueProcessor<E, V> valueProcessor) {
        checkState(allowRegistrations, "Registrations are no longer allowed!");
        checkNotNull(valueProcessor);
        checkArgument(!(valueProcessor instanceof ValueProcessorDelegate), "Cannot register ValueProcessorDelegates! READ THE DOCS!");
        checkNotNull(key);
        List<ValueProcessor<?, ?>> processorList = this.valueProcessorMap.get(key);
        if (processorList == null) {
            processorList = Collections.synchronizedList(Lists.<ValueProcessor<?, ?>>newArrayList());
            this.valueProcessorMap.put(key, processorList);
        }
        checkArgument(!processorList.contains(valueProcessor), "Duplicate ValueProcessor registration!");
        processorList.add(valueProcessor);
    }

    public <E, V extends BaseValue<E>> Optional<ValueProcessor<E, V>> getValueProcessor(Key<V> key) {
        return Optional.ofNullable((ValueProcessor<E, V>) (Object) this.valueDelegates.get(key));
    }

    public Optional<ValueProcessor<?, ?>> getWildValueProcessor(Key<?> key) {
        return Optional.<ValueProcessor<?, ?>>ofNullable(this.valueDelegates.get(key));
    }

    public <E> Optional<ValueProcessor<E, ? extends BaseValue<E>>> getBaseValueProcessor(Key<? extends BaseValue<E>> key) {
        return Optional.<ValueProcessor<E, ? extends BaseValue<E>>>ofNullable(
            (ValueProcessor<E, ? extends BaseValue<E>>) (Object) this.valueDelegates.get
                (key));
    }

    public <E, V extends BaseValue<E>> Optional<BlockValueProcessor<E, V>> getBlockValueProcessor(Key<V> key) {
        return Optional.fromNullable((BlockValueProcessor<E, V>) (Object) this.blockValueProcessorDelegates.get(key));
    }

    public Optional<BlockValueProcessor<?, ?>> getWildBlockValueProcessor(Key<?> key) {
        return Optional.<BlockValueProcessor<?, ?>>fromNullable(this.blockValueProcessorDelegates.get(key));
    }

    public <E> Optional<BlockValueProcessor<E, ?>> getBaseBlockValueProcessor(Key<? extends BaseValue<E>> key) {
        return Optional.<BlockValueProcessor<E, ?>>fromNullable(
                (BlockValueProcessor<E, ? extends BaseValue<E>>) (Object) this.blockValueProcessorDelegates.get(key));
    }
}
