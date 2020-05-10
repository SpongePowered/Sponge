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
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataManager;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.ImmutableDataBuilder;
import org.spongepowered.api.data.ImmutableDataHolder;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataBuilder;
import org.spongepowered.api.data.persistence.DataContentUpdater;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.config.DataSerializableTypeSerializer;
import org.spongepowered.common.data.builder.manipulator.SpongeDataManipulatorBuilder;
import org.spongepowered.common.data.builder.manipulator.SpongeImmutableDataManipulatorBuilder;
import org.spongepowered.common.registry.type.data.DataTranslatorRegistryModule;
import org.spongepowered.common.registry.type.data.KeyRegistryModule;
import org.spongepowered.common.util.Constants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
public final class SpongeDataManager implements DataManager {

    static {
        // TODO: centralize registration
        TypeSerializerCollection.defaults().register(DataSerializableTypeSerializer.predicate(), DataSerializableTypeSerializer.INSTANCE);
    }

    // Builders
    private final Map<Class<?>, DataBuilder<?>> builders = new IdentityHashMap<>();


    final Map<Class<? extends DataManipulator<?, ?>>, DataManipulatorBuilder<?, ?>> builderMap = new MapMaker()
        .concurrencyLevel(4)
        .makeMap();

    private final Map<Class<? extends ImmutableDataManipulator<?, ?>>, DataManipulatorBuilder<?, ?>> immutableBuilderMap = new MapMaker()
        .concurrencyLevel(4)
        .makeMap();

    private final Map<Class<? extends ImmutableDataHolder<?>>, ImmutableDataBuilder<?, ?>> immutableDataBuilderMap = new MapMaker()
        .concurrencyLevel(4)
        .makeMap();
    // Content updaters
    private final Map<Class<? extends DataSerializable>, List<DataContentUpdater>> updatersMap = new IdentityHashMap<>();


    static boolean allowRegistrations = true;


    public static SpongeDataManager getInstance() {
        return SpongeImpl.getDataManager();
    }

    @Inject
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
            SpongeImpl.getLogger().warn("A DataBuilder has already been registered for {}. Attempted to register {} instead.", clazz,
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
        Collections.sort(updaters, Constants.Functional.DATA_CONTENT_UPDATER_COMPARATOR);
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

    @SuppressWarnings({"unchecked", "rawtypes"}) <T extends DataSerializable> void registerBuilderAndImpl(Class<T> clazz,
      Class<? extends T> implClass, DataBuilder<T> builder) {
        this.registerBuilder(clazz, builder);
        this.registerBuilder((Class<T>) implClass, builder);
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
        final Optional<DataBuilder<T>> optional = this.getBuilder(clazz);
        return optional.flatMap(tDataBuilder -> tDataBuilder.build(dataView));
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

    public static void finalizeRegistration() {
        allowRegistrations = false;
        SpongeManipulatorRegistry.getInstance().bake();
        KeyRegistryModule.getInstance().registerKeyListeners();
    }

    @Override
    public void registerLegacyManipulatorIds(String legacyId, DataRegistration<?, ?> registration) {
        checkState(allowRegistrations);
        SpongeManipulatorRegistry.getInstance().registerLegacyId(legacyId, registration);
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

    @Deprecated
    @Override
    public <T> void registerTranslator(Class<T> objectClass, DataTranslator<T> translator) {
        checkState(allowRegistrations, "Registrations are no longer allowed");
        checkArgument(translator.getToken().isSupertypeOf(objectClass),
                "DataTranslator is not compatible with the target object class: %s", objectClass);
        DataTranslatorRegistryModule.getInstance().registerAdditionalCatalog(translator);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<DataTranslator<T>> getTranslator(Class<T> objectClass) {
        return DataTranslatorRegistryModule.getInstance().getByClass(objectClass);
    }

    @Override
    public Collection<Class<? extends DataManipulator<?, ?>>> getAllRegistrationsFor(PluginContainer container) {
        return SpongeManipulatorRegistry.getInstance().getRegistrations(container);
    }

    @Override
    public DataContainer createContainer() {
        return new MemoryDataContainer();
    }

    @Override
    public DataContainer createContainer(DataView.SafetyMode safety) {
        return new MemoryDataContainer(safety);
    }

    public Optional<DataManipulatorBuilder<?, ?>> getWildManipulatorBuilder(Class<? extends DataManipulator<?, ?>> manipulatorClass) {
        return Optional.ofNullable(this.builderMap.get(checkNotNull(manipulatorClass)));
    }

    public Optional<DataManipulatorBuilder<?, ?>> getWildBuilderForImmutable(Class<? extends ImmutableDataManipulator<?, ?>> immutable) {
        return Optional.ofNullable(this.immutableBuilderMap.get(checkNotNull(immutable)));
    }

    <M extends DataManipulator<M, I>, I extends ImmutableDataManipulator<I, M>> void validateRegistration(
      SpongeDataRegistrationBuilder<M, I> builder) {
        checkState(allowRegistrations);
        final Class<M> manipulatorClass = builder.manipulatorClass;
        final Class<? extends M> implementationClass = builder.implementationData;
        final Class<I> immutableClass = builder.immutableClass;
        final Class<? extends I> immutableImplementation = builder.immutableImplementation;
        final DataManipulatorBuilder<M, I> manipulatorBuilder = builder.manipulatorBuilder;
        checkState(!this.builders.containsKey(manipulatorClass), "DataManipulator already registered!");
        checkState(!this.builderMap.containsKey(manipulatorClass), "DataManipulator already registered!");
        checkState(!this.builderMap.containsValue(manipulatorBuilder), "DataManipulatorBuilder already registered!");
        checkState(!this.builders.containsKey(immutableClass), "ImmutableDataManipulator already registered!");
        checkState(!this.immutableBuilderMap.containsKey(immutableClass), "ImmutableDataManipulator already registered!");
        checkState(!this.immutableBuilderMap.containsValue(manipulatorBuilder), "DataManipulatorBuilder already registered!");

        if (implementationClass != null) {
            checkState(!this.builders.containsKey(implementationClass), "DataManipulator implementation already registered!");
            checkState(!this.builderMap.containsKey(implementationClass), "DataManipulator implementation already registered!");
        }
        if (immutableImplementation != null) {
            checkState(!this.builders.containsKey(immutableImplementation), "ImmutableDataManipulator implementation already registered!");
            checkState(!this.immutableBuilderMap.containsKey(immutableImplementation), "ImmutableDataManipulator implementation already registered!");
        }
    }

    public static boolean areRegistrationsComplete() {
        return !allowRegistrations;
    }

    <M extends DataManipulator<M, I>, I extends ImmutableDataManipulator<I, M>> void registerInternally(
      SpongeDataRegistration<M, I> registration) {
        this.builders.put(registration.getManipulatorClass(), registration.getDataManipulatorBuilder());
        this.builderMap.put(registration.getManipulatorClass(), registration.getDataManipulatorBuilder());
        if (!registration.getImplementationClass().equals(registration.getManipulatorClass())) {
            this.builders.put(registration.getImplementationClass(), registration.getDataManipulatorBuilder());
            this.builderMap.put(registration.getImplementationClass(), registration.getDataManipulatorBuilder());
        }
        this.immutableBuilderMap.put(registration.getImmutableManipulatorClass(), registration.getDataManipulatorBuilder());
        if (!registration.getImmutableImplementationClass().equals(registration.getImmutableManipulatorClass())) {
            this.immutableBuilderMap.put(registration.getImmutableImplementationClass(), registration.getDataManipulatorBuilder());
        }
    }
}
