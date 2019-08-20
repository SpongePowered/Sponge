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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Multimap;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.DataRegistrationNotFoundException;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.registry.util.RegistrationDependency;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.category.CustomDataRegistrationCategory;
import org.spongepowered.common.config.type.CustomDataConfig;
import org.spongepowered.common.data.builder.manipulator.SpongeDataManipulatorBuilder;
import org.spongepowered.common.data.nbt.NbtDataType;
import org.spongepowered.common.data.nbt.SpongeNbtProcessorDelegate;
import org.spongepowered.common.data.nbt.data.NbtDataProcessor;
import org.spongepowered.common.data.nbt.value.NbtValueProcessor;
import org.spongepowered.common.data.util.DataFunction;
import org.spongepowered.common.data.util.DataProcessorDelegate;
import org.spongepowered.common.data.util.ValueProcessorDelegate;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;
import org.spongepowered.common.registry.type.data.KeyRegistryModule;
import org.spongepowered.common.util.Constants;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@SuppressWarnings({"SuspiciousMethodCalls", "unchecked", "rawtypes"})
@RegistrationDependency(KeyRegistryModule.class)
public class SpongeManipulatorRegistry implements SpongeAdditionalCatalogRegistryModule<DataRegistration<?, ?>> {

    private static final SpongeManipulatorRegistry INSTANCE = new SpongeManipulatorRegistry();

    public static SpongeManipulatorRegistry getInstance() {
        return INSTANCE;
    }

    private final Map<Class<? extends DataManipulator<?, ?>>, Class<? extends DataManipulator<?, ?>>> interfaceToImplDataManipulatorClasses = new IdentityHashMap<>();
    private final Map<Class<? extends DataManipulator<?, ?>>, DataProcessorDelegate<?, ?>> dataProcessorDelegates =  new IdentityHashMap<>();
    private final Map<Class<? extends ImmutableDataManipulator<?, ?>>, DataProcessorDelegate<?, ?>> immutableDataProcessorDelegates =  new IdentityHashMap<>();
    private ImmutableTable<Class<? extends DataManipulator<?, ?>>, NbtDataType, NbtDataProcessor<?, ?>> nbtProcessorTable = ImmutableTable.of();
    private ImmutableTable<Key<?>, NbtDataType, NbtValueProcessor<?, ?>> nbtValueTable = ImmutableTable.of();
    private final Map<Key<? extends BaseValue<?>>, ValueProcessorDelegate<?, ?>> valueDelegates = new IdentityHashMap<>();

    // This will be replaced with an immutable variant on #bake()
    private Multimap<PluginContainer, DataRegistration<?, ?>> pluginBasedRegistrations = ImmutableMultimap.of();

    // This will be replaced with an immutable variant on #bake()
    private Collection<DataRegistration<?, ?>> registrations = Collections.emptyList();

    private Map<Class<? extends DataManipulator<?, ?>>, DataRegistration<?, ?>> manipulatorRegistrationMap = ImmutableMap.of();
    private Map<Class<? extends ImmutableDataManipulator<?, ?>>, DataRegistration<?, ?>> immutableRegistrationMap = ImmutableMap.of();
    private Map<String, DataRegistration<?, ?>> registrationMap = ImmutableMap.of();
    private final Map<String, DataRegistration<?, ?>> legacyRegistrationIds = new MapMaker().concurrencyLevel(4).makeMap();

    @Nullable private TemporaryRegistry tempRegistry = new TemporaryRegistry();

    void registerLegacyId(String legacyId, DataRegistration<?, ?> registration) {
        if (this.legacyRegistrationIds.containsKey(legacyId)) {
            throw new IllegalStateException("Legacy registration id already registered: id" + legacyId + " for registration: " + registration);
        }
        this.legacyRegistrationIds.put(legacyId, registration);
    }

    @Override
    public boolean allowsApiRegistration() {
        return true;
    }

    @Override
    public void registerAdditionalCatalog(DataRegistration<?, ?> extraCatalog) {
        checkArgument(extraCatalog instanceof SpongeDataRegistration);
        // Technically, if the registration was not registered, well....
        // it should have already been registered at this point
        final SpongeDataRegistration registration = (SpongeDataRegistration) extraCatalog;
        SpongeDataManager.getInstance().registerInternally(registration);
        SpongeManipulatorRegistry.getInstance().register(registration);
    }

    @Override
    public Optional<DataRegistration<?, ?>> getById(String id) {
        final DataRegistration<?, ?> dataRegistration = this.registrationMap.get(id);
        return Optional.ofNullable(dataRegistration);
    }

    @Override
    public Collection<DataRegistration<?, ?>> getAll() {
        return this.registrationMap.values();
    }


    private static final class TemporaryRegistry {

        private final Map<Class<? extends DataManipulator<?, ?>>, List<DataProcessor<?, ?>>> processorMap = new MapMaker()
            .concurrencyLevel(4)
            .makeMap();


        private final Map<Class<? extends ImmutableDataManipulator<?, ?>>, List<DataProcessor<?, ?>>> immutableProcessorMap = new MapMaker()
            .concurrencyLevel(4)
            .makeMap();

        private final Map<Class<? extends DataManipulator<?, ?>>, List<NbtDataProcessor<?, ?>>> nbtProcessorMap = new MapMaker()
            .concurrencyLevel(4)
            .makeMap();

        private final Map<Key<? extends BaseValue<?>>, List<ValueProcessor<?, ?>>> valueProcessorMap = new MapMaker()
            .concurrencyLevel(4)
            .makeMap();

        private final ConcurrentSkipListSet<SpongeDataRegistration<?, ?>> registrations = new ConcurrentSkipListSet<>(
            Comparator.comparing(DataRegistration::getId));

    }

    private SpongeManipulatorRegistry() {
    }

    public <M extends DataManipulator<M, I>, I extends ImmutableDataManipulator<I, M>> DataRegistration<M, I> getRegistrationFor(
        Class<? extends M> manipulator) {
        final DataRegistration<?, ?> dataRegistration = this.manipulatorRegistrationMap.get(manipulator.getClass());
        if (dataRegistration == null) {
            throw new DataRegistrationNotFoundException("Could not locate a DataRegistration for class", manipulator);
        }
        return (DataRegistration<M, I>) dataRegistration;
    }

    public DataRegistration<?, ?> getRegistrationFor(DataManipulator<?, ?> manipulator) {
        final DataRegistration<?, ?> dataRegistration = this.manipulatorRegistrationMap.get(manipulator.getClass());
        if (dataRegistration == null) {
            if (this.tempRegistry != null) {
                for (SpongeDataRegistration<?, ?> registration : this.tempRegistry.registrations) {
                    if (registration.getManipulatorClass() == manipulator.getClass()) {
                        return registration;
                    }
                }
            }
            throw new DataRegistrationNotFoundException("Could not locate a DataRegistration for class " + manipulator.getClass());
        }
        return dataRegistration;
    }

    public DataRegistration<?, ?> getRegistrationFor(ImmutableDataManipulator<?, ?> immutable) {
        final DataRegistration<?, ?> dataRegistration = this.immutableRegistrationMap.get(immutable.getClass());
        if (dataRegistration == null) {
            if (this.tempRegistry != null) {
                for (SpongeDataRegistration<?, ?> registration : this.tempRegistry.registrations) {
                    if (registration.getImmutableManipulatorClass() == immutable.getClass()) {
                        return registration;
                    }
                }
            }
            throw new DataRegistrationNotFoundException("Could not locate a DataRegistration for class " + immutable.getClass());
        }
        return dataRegistration;
    }

    public <M extends DataManipulator<M, I>, I extends ImmutableDataManipulator<I, M>> DataRegistration<M, I> getRegistrationForImmutable(
        Class<? extends I> manipulator) {
        final DataRegistration<?, ?> dataRegistration = this.immutableRegistrationMap.get(manipulator);
        if (dataRegistration == null) {
            throw new DataRegistrationNotFoundException("Could not locate a DataRegistration for class", null, manipulator);
        }
        return (DataRegistration<M, I>) dataRegistration;
    }

    public <M extends DataManipulator<M, I>, I extends ImmutableDataManipulator<I, M>> Optional<DataRegistration<M, I>> getRegistrationFor(
        String id) {
        final DataRegistration<?, ?> dataRegistration = this.registrationMap.get(id);
        return Optional.ofNullable((DataRegistration<M, I>) dataRegistration);
    }

    <M extends DataManipulator<M, I>, I extends ImmutableDataManipulator<I, M>> void validateRegistrationId(String id) {
        checkState(this.tempRegistry != null);
        this.tempRegistry.registrations.stream()
            .filter(registration -> registration.getId().equalsIgnoreCase(id))
            .findFirst()
            .ifPresent(registration -> {
                throw new IllegalStateException("Existing DataRegistration exists for id: " + id);
            });
    }

    Collection<Class<? extends DataManipulator<?, ?>>> getRegistrations(PluginContainer container) {
        return this.pluginBasedRegistrations.get(container).stream()
            .map(DataRegistration::getManipulatorClass)
            .collect(Collectors.toList());
    }

    public Optional<DataRegistration<?, ?>> getRegistrationForLegacyId(String id) {
        return Optional.ofNullable(this.legacyRegistrationIds.get(id));
    }


    public <M extends DataManipulator<M, I>, I extends ImmutableDataManipulator<I, M>> DataRegistration<M, I> register(
        SpongeDataRegistration<M, I> registration) {
        checkState(this.tempRegistry != null);
        if (this.tempRegistry.registrations.contains(registration)) {
            throw new IllegalStateException("Existing DataRegistration exists for id: " + registration);
        }
        this.tempRegistry.registrations.add(registration);
        return registration;
    }

    public <M extends DataManipulator<M, I>, I extends ImmutableDataManipulator<I, M>> SpongeManipulatorRegistry register(
        Class<M> manipulatorClass,
        Class<? extends M> implClass,
        Class<I> immutableDataManipulator,
        Class<? extends I> implImClass,
        DataProcessor<M, I> processor) {

        // TODO - Require sponge registrations internally
        checkState(this.tempRegistry != null);
        if (!this.interfaceToImplDataManipulatorClasses.containsKey(manipulatorClass)) { // we only need to insert it once.
            this.interfaceToImplDataManipulatorClasses.put(manipulatorClass, implClass);
        }
        checkState(SpongeDataManager.allowRegistrations, "Registrations are no longer allowed!");
        List<DataProcessor<?, ?>> processorList = this.tempRegistry.processorMap.get(manipulatorClass);
        if (processorList == null) {
            processorList = new CopyOnWriteArrayList<>();
            this.tempRegistry.processorMap.put(manipulatorClass, processorList);
            this.tempRegistry.processorMap.put(implClass, processorList);
        }
        checkArgument(!processorList.contains(processor), "Duplicate DataProcessor Registration!");
        processorList.add(processor);

        List<DataProcessor<?, ?>> immutableProcessorList = this.tempRegistry.immutableProcessorMap.get(immutableDataManipulator);
        if (immutableProcessorList == null) {
            immutableProcessorList = new CopyOnWriteArrayList<>();
            this.tempRegistry.immutableProcessorMap.put(immutableDataManipulator, immutableProcessorList);
            this.tempRegistry.immutableProcessorMap.put(implImClass, immutableProcessorList);
        }
        checkArgument(!immutableProcessorList.contains(processor), "Duplicate DataProcessor Registration!");
        immutableProcessorList.add(processor);

        return this;
    }

    public <E, V extends BaseValue<E>> void registerValueProcessor(Key<V> key, ValueProcessor<E, V> valueProcessor) {
        checkState(this.tempRegistry != null);
        checkNotNull(valueProcessor);
        checkArgument(!(valueProcessor instanceof ValueProcessorDelegate), "Cannot register ValueProcessorDelegates! READ THE DOCS!");
        checkNotNull(key);
        List<ValueProcessor<?, ?>>
            processorList =
            this.tempRegistry.valueProcessorMap.computeIfAbsent(key, k -> Collections.synchronizedList(Lists.newArrayList()));
        checkArgument(!processorList.contains(valueProcessor), "Duplicate ValueProcessor registration!");
        processorList.add(valueProcessor);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Nullable
    public DataProcessor<?, ?> getDelegate(Class<?> mClass) {
        if (this.tempRegistry != null) {
            // During soft registrations
            if (DataManipulator.class.isAssignableFrom(mClass)) {
                List<DataProcessor<?, ?>> dataProcessors = this.tempRegistry.processorMap.get(mClass);
                if (dataProcessors == null) {
                    return null;
                }
                return new DataProcessorDelegate(ImmutableList.copyOf(dataProcessors));
            } else {
                List<DataProcessor<?, ?>> dataProcessors = this.tempRegistry.immutableProcessorMap.get(mClass);
                if (dataProcessors == null) {
                    return null;
                }
                return new DataProcessorDelegate(ImmutableList.copyOf(dataProcessors));
            }
        }
        return DataManipulator.class.isAssignableFrom(mClass)
               ? this.dataProcessorDelegates.get(mClass)
               : this.immutableDataProcessorDelegates.get(mClass);
    }

    @Nullable
    public ValueProcessor<?, ?> getDelegate(Key<?> key) {
        if (this.tempRegistry != null) {
            // During soft registrations
            List<ValueProcessor<?, ?>> list = this.tempRegistry.valueProcessorMap.get(key);

            if (list == null) {
                return null;
            }

            return new ValueProcessorDelegate(key, ImmutableList.copyOf(list));
        }
        return this.valueDelegates.get(key);
    }

    @Nullable
    public NbtDataProcessor<?, ?> getNbtDelegate(NbtDataType dataType, Class<?> manipulatorClass) {
        return this.nbtProcessorTable.get(dataType, manipulatorClass);
    }

    @Nullable
    public NbtValueProcessor<?, ?> getNbtProcessor(NbtDataType type, Key<?> key) {
        return this.nbtValueTable.get(type, key);
    }

    public Collection<NbtDataProcessor<?, ?>> getNbtProcessors(NbtDataType nbtDataType) {
        return this.nbtProcessorTable.column(nbtDataType).values();
    }

    public Collection<NbtValueProcessor<?, ?>> getNbtValueProcessors(NbtDataType nbtDataType) {
        return this.nbtValueTable.column(nbtDataType).values();
    }

    void bake() {
        checkState(this.tempRegistry != null);
        // ValueProcessors
        this.tempRegistry.valueProcessorMap.forEach((key, value) -> {
            ImmutableList.Builder<ValueProcessor<?, ?>> valueListBuilder = ImmutableList.builder();
            value.sort(Constants.Functional.VALUE_PROCESSOR_COMPARATOR);
            valueListBuilder.addAll(value);
            final ValueProcessorDelegate<?, ?> delegate = new ValueProcessorDelegate(key, valueListBuilder.build());
            this.valueDelegates.put(key, delegate);
        });
        // DataProcessors
        this.tempRegistry.processorMap.forEach((key, value) -> {
            ImmutableList.Builder<DataProcessor<?, ?>> dataListBuilder = ImmutableList.builder();
            value.sort(Constants.Functional.DATA_PROCESSOR_COMPARATOR);
            dataListBuilder.addAll(value);
            final DataProcessorDelegate<?, ?> delegate = new DataProcessorDelegate(dataListBuilder.build());
            this.dataProcessorDelegates.put(key, delegate);
        });
        SpongeDataManager manager = SpongeDataManager.getInstance();

        // DataManipulatorBuilders part 2 (Have to register them back for serialization stuff
        this.dataProcessorDelegates.forEach((key, value) -> {
            if (!Modifier.isInterface(key.getModifiers()) && !Modifier.isAbstract(key.getModifiers())) {
                DataFunction<DataContainer, DataManipulator, Optional<? extends DataManipulator<?, ?>>> function =
                    ((DataProcessor) value)::fill;
                SpongeDataManipulatorBuilder builder = new SpongeDataManipulatorBuilder(value, key, function);
                manager.builderMap.put(key, checkNotNull(builder));
                manager.registerBuilder(key, builder);
            } else {
                final Class<? extends DataManipulator<?, ?>> clazz = this.interfaceToImplDataManipulatorClasses.get(key);
                DataFunction<DataContainer, DataManipulator, Optional<? extends DataManipulator<?, ?>>> function =
                    ((DataProcessor) value)::fill;
                SpongeDataManipulatorBuilder builder = new SpongeDataManipulatorBuilder(value, clazz, function);
                manager.builderMap.put(key, checkNotNull(builder));
                manager.registerBuilder(key, builder);
            }
        });

        // Immutable DataProcessors
        this.tempRegistry.immutableProcessorMap.forEach((key, value) -> {
            ImmutableList.Builder<DataProcessor<?, ?>> dataListBuilder = ImmutableList.builder();
            value.sort(Constants.Functional.DATA_PROCESSOR_COMPARATOR);
            dataListBuilder.addAll(value);
            final DataProcessorDelegate<?, ?> delegate = new DataProcessorDelegate(dataListBuilder.build());
            this.immutableDataProcessorDelegates.put(key, delegate);
        });
        // NBT processors
        ImmutableTable.Builder<Class<? extends DataManipulator<?, ?>>, NbtDataType, NbtDataProcessor<?, ?>> builder = ImmutableTable.builder();
        this.tempRegistry.nbtProcessorMap.forEach((key, value) -> {
            final HashMultimap<NbtDataType, NbtDataProcessor<?, ?>> processorMultimap = HashMultimap.create();
            for (NbtDataProcessor<?, ?> nbtDataProcessor : value) {
                processorMultimap.put(nbtDataProcessor.getTargetType(), nbtDataProcessor);
            }
            for (Map.Entry<NbtDataType, Collection<NbtDataProcessor<?, ?>>> nbtDataTypeCollectionEntry : processorMultimap.asMap().entrySet()) {
                ImmutableList.Builder<NbtDataProcessor<?, ?>> processorBuilder = ImmutableList.builder();
                processorBuilder.addAll(nbtDataTypeCollectionEntry.getValue());
                final NbtDataType dataType = nbtDataTypeCollectionEntry.getKey();
                builder.put(key, dataType, new SpongeNbtProcessorDelegate(processorBuilder.build(), dataType));
            }
        });
        this.nbtProcessorTable = builder.build();

        ImmutableSet.Builder<DataRegistration<?, ?>> registrationBuilder = ImmutableSet.builder();
        ImmutableMap.Builder<Class<? extends DataManipulator<?, ?>>, DataRegistration<?, ?>> manipulatorBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<Class<? extends ImmutableDataManipulator<?, ?>>, DataRegistration<?, ?>> immutableBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<String, DataRegistration<?, ?>> idBuilder = ImmutableMap.builder();
        ImmutableMultimap.Builder<PluginContainer, DataRegistration<?, ?>> pluginBuilder = ImmutableMultimap.builder();
        this.tempRegistry.registrations.forEach(registration -> {
                registrationBuilder.add(registration);
                manipulatorBuilder.put(registration.getManipulatorClass(), registration);
                if (!registration.getImplementationClass().equals(registration.getManipulatorClass())) {
                    manipulatorBuilder.put(registration.getImplementationClass(), registration);
                }
                immutableBuilder.put(registration.getImmutableManipulatorClass(), registration);
                if (!registration.getImmutableImplementationClass().equals(registration.getImmutableManipulatorClass())) {
                    immutableBuilder.put(registration.getImmutableImplementationClass(), registration);
                }
                idBuilder.put(registration.getId(), registration);
                pluginBuilder.put(registration.getPluginContainer(), registration);
            });

        this.registrations = registrationBuilder.build();
        this.manipulatorRegistrationMap = manipulatorBuilder.build();
        this.immutableRegistrationMap = immutableBuilder.build();
        this.registrationMap = idBuilder.build();
        this.pluginBasedRegistrations = pluginBuilder.build();

        final SpongeConfig<CustomDataConfig> customDataConfigAdapter = SpongeImpl.getCustomDataConfigAdapter();
        final CustomDataRegistrationCategory customDataRegCat = customDataConfigAdapter.getConfig().getDataRegistrationConfig();
        customDataRegCat.populateRegistrations(this.registrations);
        // Save the list of registered id's, this way the customDataRegCat can be re-understood.
        customDataConfigAdapter.save();

        this.tempRegistry = null; // Finalizes the registration by setting the temporary object to null
    }
}
