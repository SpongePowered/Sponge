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
import org.spongepowered.common.data.builder.manipulator.SpongeDataBuilder;
import org.spongepowered.common.data.builder.manipulator.SpongeImmutableDataBuilder;
import org.spongepowered.common.data.util.ComparatorUtil;
import org.spongepowered.common.data.util.DataProcessorDelegate;
import org.spongepowered.common.data.util.ValueProcessorDelegate;
import org.spongepowered.common.service.persistence.SpongeSerializationService;
import org.spongepowered.common.util.ReflectionUtil;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

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
    private Map<Class<? extends ImmutableDataManipulator<?, ?>>, List<DataProcessor<?, ?>>> immutableProcessorMap = new MapMaker()
        .concurrencyLevel(4)
        .makeMap();
    private final Map<Key<? extends BaseValue<?>>, List<ValueProcessor<?, ?>>> valueProcessorMap = new MapMaker()
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

    private static boolean allowRegistrations = true;

    private SpongeDataRegistry() {
    }


    public static SpongeDataRegistry getInstance() {
        return SpongeDataRegistry.instance;
    }

    public static void finalizeRegistration() {
        allowRegistrations = false;
        final SpongeDataRegistry registry = instance;
        registry.valueProcessorMap.entrySet().forEach( entry -> {
            ImmutableList.Builder<ValueProcessor<?, ?>> valueListBuilder = ImmutableList.builder();
            Collections.sort(entry.getValue(), ComparatorUtil.VALUE_PROCESSOR_COMPARATOR);
            valueListBuilder.addAll(entry.getValue());
            final ValueProcessorDelegate<?, ?> delegate = new ValueProcessorDelegate(entry.getKey(), valueListBuilder.build());
            registry.valueDelegates.put(entry.getKey(), delegate);
        });
        registry.valueProcessorMap.clear();
        registry.processorMap.entrySet().forEach(entry -> {
            ImmutableList.Builder<DataProcessor<?, ?>> dataListBuilder = ImmutableList.builder();
            Collections.sort(entry.getValue(), ComparatorUtil.DATA_PROCESSOR_COMPARATOR);
            dataListBuilder.addAll(entry.getValue());
            final DataProcessorDelegate<?, ?> delegate = new DataProcessorDelegate(dataListBuilder.build());
            registry.dataProcessorDelegates.put(entry.getKey(), delegate);
        });
        registry.processorMap.clear();
        registry.immutableProcessorMap.entrySet().forEach(entry -> {
            ImmutableList.Builder<DataProcessor<?, ?>> dataListBuilder = ImmutableList.builder();
            Collections.sort(entry.getValue(), ComparatorUtil.DATA_PROCESSOR_COMPARATOR);
            dataListBuilder.addAll(entry.getValue());
            final DataProcessorDelegate<?, ?> delegate = new DataProcessorDelegate(dataListBuilder.build());
            registry.immutableDataProcessorDelegates.put(entry.getKey(), delegate);
        });
        registry.immutableProcessorMap.clear();

        registry.builderMap.values().forEach(builder -> {
            if (builder instanceof SpongeDataBuilder) {
                ((SpongeDataBuilder<?, ?>) builder).finalizeRegistration();
            }
        });
    }


    @Override
    public <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> void register(Class<? extends T> manipulatorClass,
                                                                                                     Class<? extends I> immutableManipulatorClass,
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
        return Optional.ofNullable((DataManipulatorBuilder<T, I>) this.builderMap.get(checkNotNull(manipulatorClass)));
    }

    @Override
    public <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> Optional<DataManipulatorBuilder<T, I>>
        getBuilderForImmutable(Class<I> immutableManipulatorClass) {
        return Optional.ofNullable((DataManipulatorBuilder<T, I>) this.immutableBuilderMap.get(checkNotNull(immutableManipulatorClass)));
    }

    public Optional<DataManipulatorBuilder<?, ?>> getWildBuilderForImmutable(Class<? extends ImmutableDataManipulator<?, ?>> immutable) {
        return Optional.ofNullable(this.immutableBuilderMap.get(checkNotNull(immutable)));
    }

    /**
     * Registers a {@link DataManipulator} class and the
     * {@link ImmutableDataManipulator} class along with the implemented
     * classes such that the processor is meant to handle the implementations
     * for those specific classes.
     *
     * @param manipulatorClass The manipulator class
     * @param implClass The implemented manipulator class
     * @param immutableDataManipulator The immutable class
     * @param implImClass The implemented immutable class
     * @param processor The processor
     * @param <T> The type of data manipulator
     * @param <I> The type of immutable data manipulator
     */
    public <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> void
    registerDataProcessorAndImpl(Class<T> manipulatorClass, Class<? extends T> implClass, Class<I> immutableDataManipulator,
                                 Class<? extends I> implImClass, DataProcessor<T, I> processor) {
        checkState(allowRegistrations, "Registrations are no longer allowed!");
        checkArgument(!Modifier.isAbstract(implClass.getModifiers()), "The Implemented DataManipulator class cannot be abstract!");
        checkArgument(!Modifier.isInterface(implClass.getModifiers()), "The Implemented DataManipulator class cannot be an interface!");
        checkArgument(!Modifier.isAbstract(implImClass.getModifiers()), "The implemented ImmutableDataManipulator class cannot be an interface!");
        checkArgument(!Modifier.isInterface(implImClass.getModifiers()), "The implemented ImmutableDataManipulator class cannot be an interface!");
        checkArgument(!(processor instanceof DataProcessorDelegate), "Cannot register DataProcessorDelegates!");
        List<DataProcessor<?, ?>> processorList = this.processorMap.get(manipulatorClass);
        if (processorList == null) {
            processorList = new CopyOnWriteArrayList<>();
            this.processorMap.put(manipulatorClass, processorList);
            this.processorMap.put(implClass, processorList);
        }
        checkArgument(!processorList.contains(processor), "Duplicate DataProcessor Registration!");
        processorList.add(processor);

        List<DataProcessor<?, ?>> immutableProcessorList = this.immutableProcessorMap.get(immutableDataManipulator);
        if (immutableProcessorList == null) {
            immutableProcessorList = new CopyOnWriteArrayList<>();
            this.immutableProcessorMap.put(immutableDataManipulator, immutableProcessorList);
            this.immutableProcessorMap.put(implImClass, immutableProcessorList);
        }
        checkArgument(!immutableProcessorList.contains(processor), "Duplicate DataProcessor Registration!");
        immutableProcessorList.add(processor);
        
        SpongeSerializationService service = SpongeSerializationService.getInstance();
        SpongeDataBuilder<T, I> builder = new SpongeDataBuilder<T, I>(implClass,
                ReflectionUtil.createInstance(implImClass), processor);
        if (!service.getBuilder(implClass).isPresent()) {
            service.registerBuilderAndImpl(manipulatorClass, implClass, builder);
        }
        if (!service.getBuilder(implImClass).isPresent()) {
            service.registerBuilderAndImpl(immutableDataManipulator, implImClass, new SpongeImmutableDataBuilder<>(builder));
        }
    }

    /**
     * Gets the {@link DataProcessorDelegate} for the provided
     * {@link DataManipulator} class.
     *
     * @param mutableClass The class of the data manipulator
     * @param <T> The type of data manipulator
     * @param <I> The type of immutable data manipulator
     * @return The data processor
     */
    public <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> Optional<DataProcessor<T, I>> getProcessor(
        Class<T> mutableClass) {
        return Optional.ofNullable((DataProcessor<T, I>) this.dataProcessorDelegates.get(checkNotNull(mutableClass)));
    }

    /**
     * Gets a wildcarded typed {@link DataProcessor} for the provided
     * {@link DataManipulator} class. This is primarily useful when the
     * type information is not known (due to type erasure).
     *
     * @param mutableClass The mutable class
     * @return The data processor
     */
    public Optional<DataProcessor<?, ?>> getWildProcessor(Class<? extends DataManipulator<?, ?>> mutableClass) {
        return Optional.<DataProcessor<?, ?>>ofNullable(this.dataProcessorDelegates.get(checkNotNull(mutableClass)));
    }

    /**
     * Gets the raw typed {@link DataProcessor} with no type generics.
     *
     * @param class1 The class of the {@link DataManipulator}
     * @return The raw typed data processor
     */
    @SuppressWarnings("rawtypes")
    public Optional<DataProcessor> getWildDataProcessor(Class<? extends DataManipulator> class1) {
        return Optional.<DataProcessor>ofNullable(this.dataProcessorDelegates.get(checkNotNull(class1)));
    }

    /**
     * Gets the {@link DataProcessor} for the {@link ImmutableDataManipulator}
     * class.
     *
     * @param immutableClass The immutable data manipulator class
     * @param <T> The type of DataManipulator
     * @param <I> The type of ImmutableDataManipulator
     * @return The data processor
     */
    public <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> Optional<DataProcessor<T, I>>
        getImmutableProcessor(Class<I> immutableClass) {
        return Optional.ofNullable((DataProcessor<T, I>) this.immutableDataProcessorDelegates.get(checkNotNull(immutableClass)));
    }

    /**
     * Gets the raw typed {@link DataProcessor} for the
     * {@link ImmutableDataManipulator} class.
     *
     * @param immutableClass The immutable data manipulator class
     * @return The raw typed data processor
     */
    @SuppressWarnings("rawtypes")
    public Optional<DataProcessor> getWildImmutableProcessor(Class<? extends ImmutableDataManipulator<?, ?>> immutableClass) {
        return Optional.<DataProcessor>ofNullable(this.immutableDataProcessorDelegates.get(checkNotNull(immutableClass)));
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
        return Optional.ofNullable((ValueProcessor<E, V>) this.valueDelegates.get(key));
    }

    public Optional<ValueProcessor<?, ?>> getWildValueProcessor(Key<?> key) {
        return Optional.<ValueProcessor<?, ?>>ofNullable(this.valueDelegates.get(key));
    }

    public <E> Optional<ValueProcessor<E, ? extends BaseValue<E>>> getBaseValueProcessor(Key<? extends BaseValue<E>> key) {
        return Optional.<ValueProcessor<E, ? extends BaseValue<E>>>ofNullable(
                (ValueProcessor<E, ? extends BaseValue<E>>) this.valueDelegates.get
                        (key));
    }

}
