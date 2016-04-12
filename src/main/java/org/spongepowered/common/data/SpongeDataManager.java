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
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataManager;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.ImmutableDataBuilder;
import org.spongepowered.api.data.ImmutableDataHolder;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataBuilder;
import org.spongepowered.api.data.persistence.DataContentUpdater;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.config.DataSerializableTypeSerializer;
import org.spongepowered.common.data.builder.manipulator.SpongeDataManipulatorBuilder;
import org.spongepowered.common.data.builder.manipulator.SpongeImmutableDataManipulatorBuilder;
import org.spongepowered.common.data.processor.common.AbstractSingleDataSingleTargetProcessor;
import org.spongepowered.common.data.util.ComparatorUtil;
import org.spongepowered.common.data.util.DataFunction;
import org.spongepowered.common.data.util.DataProcessorDelegate;
import org.spongepowered.common.data.util.ValueProcessorDelegate;
import org.spongepowered.common.registry.type.data.DataTranslatorRegistryModule;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public final class SpongeDataManager implements DataManager {
    static {
        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(DataSerializable.class), new DataSerializableTypeSerializer());
    }

    private static final SpongeDataManager instance = new SpongeDataManager();
    private final Map<Class<?>, DataBuilder<?>> builders = Maps.newHashMap();
    private final Map<Class<? extends ImmutableDataHolder<?>>, ImmutableDataBuilder<?, ?>> immutableDataBuilderMap = new MapMaker().concurrencyLevel(4).makeMap();

    // Builders

    private final Map<Class<? extends DataManipulator<?, ?>>, DataManipulatorBuilder<?, ?>> builderMap = new MapMaker()
            .concurrencyLevel(4)
            .makeMap();
    private final Map<Class<? extends ImmutableDataManipulator<?, ?>>, DataManipulatorBuilder<?, ?>> immutableBuilderMap = new MapMaker()
            .concurrencyLevel(4)
            .makeMap();

    private final Map<Class<?>, DataTranslator<?>> dataSerializerMap = new MapMaker().concurrencyLevel(4).makeMap();

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


    // Processor delegates

    private final Map<Key<? extends BaseValue<?>>, ValueProcessorDelegate<?, ?>> valueDelegates = new IdentityHashMap<>();
    private final Map<Class<? extends DataManipulator<?, ?>>, DataProcessorDelegate<?, ?>> dataProcessorDelegates =  new IdentityHashMap<>();
    private final Map<Class<? extends ImmutableDataManipulator<?, ?>>, DataProcessorDelegate<?, ?>> immutableDataProcessorDelegates =  new IdentityHashMap<>();
    private final Map<Class<? extends DataManipulator<?, ?>>, Class<? extends DataManipulator<?, ?>>> interfaceToImplDataManipulatorClasses = new IdentityHashMap<>();

    // Content updaters
    private final Map<Class<? extends DataSerializable>, List<DataContentUpdater>> updatersMap = new IdentityHashMap<>();

    private static boolean allowRegistrations = true;
    public static SpongeDataManager getInstance() {
        return instance;
    }

    private SpongeDataManager() {}

    @Override
    public <T extends DataSerializable> void registerBuilder(Class<T> clazz, DataBuilder<T> builder) {
        checkNotNull(clazz);
        checkNotNull(builder);
        if (!this.builders.containsKey(clazz)) {
            if (!(builder instanceof AbstractDataBuilder || builder instanceof SpongeDataManipulatorBuilder || builder instanceof SpongeImmutableDataManipulatorBuilder)) {
                SpongeImpl.getLogger().warn("A custom DataBuilder is not extending AbstractDataBuilder! It is recommended that "
                                            + "the custom data builder does extend it to gain automated content versioning updates and maintain "
                                            + "simplicity. The offending builder's class is: {}", builder.getClass());
            }
            this.builders.put(clazz, builder);
        } else {
            SpongeImpl.getLogger().warn("A DataBuilder has already been registered for %s. Attempted to register %s instead.%n", clazz,
                    builder.getClass());
        }
    }

    @Override
    public <T extends DataSerializable> void registerContentUpdater(Class<T> clazz, DataContentUpdater updater) {
        checkNotNull(updater, "DataContentUpdater was null!");
        if (!this.updatersMap.containsKey(checkNotNull(clazz, "DataSerializable class was null!"))) {
            this.updatersMap.put(clazz, new ArrayList<>());
        }
        final List<DataContentUpdater> updaters = this.updatersMap.get(clazz);
        updaters.add(updater);
        Collections.sort(updaters, ComparatorUtil.DATA_CONTENT_UPDATER_COMPARATOR);
    }

    @Override
    public <T extends DataSerializable> Optional<DataContentUpdater> getWrappedContentUpdater(Class<T> clazz, final int fromVersion,
            final int toVersion) {
        checkArgument(fromVersion != toVersion, "Attempting to convert to the same version!");
        checkArgument(fromVersion < toVersion, "Attempting to backwards convert data! This isn't supported!");
        final List<DataContentUpdater> updaters = this.updatersMap.get(checkNotNull(clazz, "DataSerializable class was null!"));
        if (updaters == null) {
            return Optional.empty();
        }
        ImmutableList.Builder<DataContentUpdater> builder = ImmutableList.builder();
        int version = fromVersion;
        for (DataContentUpdater updater : updaters) {
            if (updater.getInputVersion() == version) {
                if (updater.getOutputVersion() > toVersion) {
                    continue;
                }
                version = updater.getOutputVersion();
                builder.add(updater);
            }
        }
        if (version < toVersion || version > toVersion) { // There wasn't a registered updater for the version being requested
            Exception e = new IllegalStateException("The requested content version for: " + clazz.getSimpleName() + " was requested, "
                                                    + "\nhowever, the versions supplied: from "+ fromVersion + " to " + toVersion + " is impossible"
                                                    + "\nas the latest version registered is: " + version+". Please notify the developer of"
                                                    + "\nthe requested consumed DataSerializable of this error.");
            e.printStackTrace();
            return Optional.empty();
        }
        return Optional.of(new DataUpdaterDelegate(builder.build(), fromVersion, toVersion));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends DataSerializable> void registerBuilderAndImpl(Class<T> clazz, Class<? extends T> implClass, DataBuilder<T> builder) {
        registerBuilder(clazz, builder);
        registerBuilder((Class<T>) (Class) implClass, builder);
    }

    @Override
    @SuppressWarnings({"unchecked", "SuspiciousMethodCalls"})
    public <T extends DataSerializable> Optional<DataBuilder<T>> getBuilder(Class<T> clazz) {
        checkNotNull(clazz);
        if (this.builders.containsKey(clazz)) {
            return Optional.of((DataBuilder<T>) this.builders.get(clazz));
        } else if (this.builderMap.containsKey(clazz)) {
            return Optional.of((DataBuilder<T>) this.builderMap.get(clazz));
        } else if (this.immutableDataBuilderMap.containsKey(clazz)) {
            return Optional.of((DataBuilder<T>) this.immutableDataBuilderMap.get(clazz));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public <T extends DataSerializable> Optional<T> deserialize(Class<T> clazz, final DataView dataView) {
        final Optional<DataBuilder<T>> optional = getBuilder(clazz);
        if (optional.isPresent()) {
            return optional.get().build(dataView);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public <T extends ImmutableDataHolder<T>, B extends ImmutableDataBuilder<T, B>> void register(Class<T> holderClass, B builder) {
        if (!this.immutableDataBuilderMap.containsKey(checkNotNull(holderClass))) {
            this.immutableDataBuilderMap.put(holderClass, checkNotNull(builder));
        } else {
            throw new IllegalStateException("Already registered the DataUtil for " + holderClass.getCanonicalName());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ImmutableDataHolder<T>, B extends ImmutableDataBuilder<T, B>> Optional<B> getImmutableBuilder(Class<T> holderClass) {
        return Optional.ofNullable((B) this.immutableDataBuilderMap.get(checkNotNull(holderClass)));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void finalizeRegistration() {
        allowRegistrations = false;
        final SpongeDataManager registry = instance;
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

        SpongeDataManager serializationService = SpongeDataManager.getInstance();
        registry.dataProcessorDelegates.entrySet().forEach(entry -> {
            if (!Modifier.isInterface(entry.getKey().getModifiers()) && !Modifier.isAbstract(entry.getKey().getModifiers())) {
                DataFunction<DataContainer, DataManipulator, Optional<? extends DataManipulator<?, ?>>> function =
                        (dataContainer, dataManipulator) -> ((DataProcessor) entry.getValue()).fill(dataContainer, dataManipulator);
                SpongeDataManipulatorBuilder builder = new SpongeDataManipulatorBuilder(entry.getValue(), entry.getKey(), function);
                registry.builderMap.put(entry.getKey(), checkNotNull(builder));
                serializationService.registerBuilder(entry.getKey(), builder);
            } else {
                final Class<? extends DataManipulator<?, ?>> clazz = registry.interfaceToImplDataManipulatorClasses.get(entry.getKey());
                DataFunction<DataContainer, DataManipulator, Optional<? extends DataManipulator<?, ?>>> function =
                        (dataContainer, dataManipulator) -> ((DataProcessor) entry.getValue()).fill(dataContainer, dataManipulator);
                SpongeDataManipulatorBuilder builder = new SpongeDataManipulatorBuilder(entry.getValue(), clazz, function);
                registry.builderMap.put(entry.getKey(), checkNotNull(builder));
                serializationService.registerBuilder(entry.getKey(), builder);
            }
        });
        registry.immutableProcessorMap.entrySet().forEach(entry -> {
            ImmutableList.Builder<DataProcessor<?, ?>> dataListBuilder = ImmutableList.builder();
            Collections.sort(entry.getValue(), ComparatorUtil.DATA_PROCESSOR_COMPARATOR);
            dataListBuilder.addAll(entry.getValue());
            final DataProcessorDelegate<?, ?> delegate = new DataProcessorDelegate(dataListBuilder.build());
            registry.immutableDataProcessorDelegates.put(entry.getKey(), delegate);
        });
        registry.immutableProcessorMap.clear();

    }


    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> void register(Class<? extends T> manipulatorClass,
            Class<? extends I> immutableManipulatorClass, DataManipulatorBuilder<T, I> builder) {
        checkState(allowRegistrations, "Registrations are no longer allowed!");
        if (!this.builderMap.containsKey(checkNotNull(manipulatorClass))) {
            this.builderMap.put(manipulatorClass, checkNotNull(builder));
            this.immutableBuilderMap.put(checkNotNull(immutableManipulatorClass), builder);
            SpongeDataManager.getInstance().registerBuilder((Class<T>) manipulatorClass, builder);
        } else {
            throw new IllegalStateException("Already registered the DataUtil for " + manipulatorClass.getCanonicalName());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> Optional<DataManipulatorBuilder<T, I>>
    getManipulatorBuilder(Class<T> manipulatorClass) {
        return Optional.ofNullable((DataManipulatorBuilder<T, I>) this.builderMap.get(checkNotNull(manipulatorClass)));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> Optional<DataManipulatorBuilder<T, I>>
    getImmutableManipulatorBuilder(Class<I> immutableManipulatorClass) {
        return Optional.ofNullable((DataManipulatorBuilder<T, I>) this.immutableBuilderMap.get(checkNotNull(immutableManipulatorClass)));
    }

    @Override
    public <T> void registerTranslator(Class<T> objectClass, DataTranslator<T> translator) {
        checkState(allowRegistrations, "Registrations are no longer allowed");
        checkNotNull(objectClass, "Target object class cannot be null!");
        checkNotNull(translator, "DataTranslator for : " + objectClass + " cannot be null!");
        checkArgument(translator.getToken().isAssignableFrom(objectClass), "DataTranslator is not compatible with the target object class: " + objectClass);
        if (!this.dataSerializerMap.containsKey(checkNotNull(objectClass, "Target class cannot be null!"))) {
            this.dataSerializerMap.put(objectClass, checkNotNull(translator, "DataTranslator for " + objectClass + " cannot be null!"));
            DataTranslatorRegistryModule.getInstance().registerAdditionalCatalog(translator);
        } else {
            throw new IllegalStateException("Already registered the DataTranslator for " + objectClass.getCanonicalName());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<DataTranslator<T>> getTranslator(Class<T> objectclass) {
        return Optional.ofNullable((DataTranslator<T>) this.dataSerializerMap.get(checkNotNull(objectclass, "Target class cannot be null!")));
    }

    public Optional<DataManipulatorBuilder<?, ?>> getWildManipulatorBuilder(Class<? extends DataManipulator<?, ?>> manipulatorClass) {
        return Optional.ofNullable(this.builderMap.get(checkNotNull(manipulatorClass)));
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
        if (!this.interfaceToImplDataManipulatorClasses.containsKey(manipulatorClass)) { // we only need to insert it once.
            this.interfaceToImplDataManipulatorClasses.put(manipulatorClass, implClass);
        }
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
    }

    public <E, V extends BaseValue<E>, T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> void
    registerDualProcessor(Class<T> manipulatorClass, Class<? extends T> implClass, Class<I> immutableDataManipulator,
            Class<? extends I> implImClass, AbstractSingleDataSingleTargetProcessor<?, E, V, T, I> processor) {
        registerDataProcessorAndImpl(manipulatorClass, implClass, immutableDataManipulator, implImClass, processor);
        registerValueProcessor(processor.getKey(), processor);
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
    @SuppressWarnings("unchecked")
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
        return Optional.ofNullable(this.dataProcessorDelegates.get(checkNotNull(mutableClass)));
    }

    /**
     * Gets the raw typed {@link DataProcessor} with no type generics.
     *
     * @param class1 The class of the {@link DataManipulator}
     * @return The raw typed data processor
     */
    @SuppressWarnings({"rawtypes", "SuspiciousMethodCalls"})
    public Optional<DataProcessor> getWildDataProcessor(Class<? extends DataManipulator> class1) {
        return Optional.ofNullable(this.dataProcessorDelegates.get(checkNotNull(class1)));
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
    @SuppressWarnings("unchecked")
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
        return Optional.ofNullable(this.immutableDataProcessorDelegates.get(checkNotNull(immutableClass)));
    }


    public <E, V extends BaseValue<E>> void registerValueProcessor(Key<V> key, ValueProcessor<E, V> valueProcessor) {
        checkState(allowRegistrations, "Registrations are no longer allowed!");
        checkNotNull(valueProcessor);
        checkArgument(!(valueProcessor instanceof ValueProcessorDelegate), "Cannot register ValueProcessorDelegates! READ THE DOCS!");
        checkNotNull(key);
        List<ValueProcessor<?, ?>> processorList = this.valueProcessorMap.get(key);
        if (processorList == null) {
            processorList = Collections.synchronizedList(Lists.newArrayList());
            this.valueProcessorMap.put(key, processorList);
        }
        checkArgument(!processorList.contains(valueProcessor), "Duplicate ValueProcessor registration!");
        processorList.add(valueProcessor);
    }

    @SuppressWarnings("unchecked")
    public <E, V extends BaseValue<E>> Optional<ValueProcessor<E, V>> getValueProcessor(Key<V> key) {
        return Optional.ofNullable((ValueProcessor<E, V>) this.valueDelegates.get(key));
    }

    public Optional<ValueProcessor<?, ?>> getWildValueProcessor(Key<?> key) {
        return Optional.ofNullable(this.valueDelegates.get(key));
    }

    @SuppressWarnings("unchecked")
    public <E> Optional<ValueProcessor<E, ? extends BaseValue<E>>> getBaseValueProcessor(Key<? extends BaseValue<E>> key) {
        return Optional.ofNullable((ValueProcessor<E, ? extends BaseValue<E>>) this.valueDelegates.get(key));
    }

}