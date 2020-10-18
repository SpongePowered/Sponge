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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.MapMaker;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataHolderBuilder;
import org.spongepowered.api.data.DataManager;
import org.spongepowered.api.data.DataManipulator.Mutable;
import org.spongepowered.api.data.DataProvider;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataBuilder;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataContentUpdater;
import org.spongepowered.api.data.persistence.DataSerializable;
import org.spongepowered.api.data.persistence.DataStore;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.data.ChangeDataHolderEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.config.CatalogTypeTypeSerializer;
import org.spongepowered.common.config.DataSerializableTypeSerializer;
import org.spongepowered.common.data.builder.item.SpongeItemStackSnapshotDataBuilder;
import org.spongepowered.common.data.key.KeyBasedDataListener;
import org.spongepowered.common.data.persistence.datastore.DataStoreRegistry;
import org.spongepowered.common.data.provider.CustomDataProvider;
import org.spongepowered.common.data.provider.DataProviderRegistry;
import org.spongepowered.common.entity.SpongeEntityArchetypeBuilder;
import org.spongepowered.common.entity.SpongeEntitySnapshotBuilder;
import org.spongepowered.common.item.SpongeItemStackBuilder;
import org.spongepowered.common.registry.MappedRegistry;
import org.spongepowered.common.util.Constants;
import org.spongepowered.plugin.PluginContainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Singleton
public final class SpongeDataManager implements DataManager {
    private static final SpongeDataManager INSTANCE = new SpongeDataManager();

    private final DataStoreRegistry dataStoreRegistry = new DataStoreRegistry();
    private final DataProviderRegistry dataProviderRegistry = new DataProviderRegistry();
    private final Map<ResourceKey, SpongeDataRegistration> registrations = new HashMap<>();

    static {
        TypeSerializerCollection.defaults().register(
                // We have a separate type serializer for CatalogTypes, so we explicitly discount them here.
                // See https://github.com/SpongePowered/SpongeCommon/issues/1348
                x -> DataSerializableTypeSerializer.TYPE.isSupertypeOf(x)
                        && !CatalogTypeTypeSerializer.TYPE.isSupertypeOf(x),
                DataSerializableTypeSerializer.INSTANCE
        );
    }


    public static final DataFixer spongeDataFixer = addFixers(new DataFixerBuilder(Constants.Sponge.SPONGE_DATA_VERSION)).build(Util.getServerExecutor());

    static DataFixerBuilder addFixers(DataFixerBuilder builder) {
    //        builder.addFixer(new SpongeLevelFixer(builder.addSchema(Constants.Legacy.World.WORLD_UUID_1_9_VERSION, Schema::new), true));
    //        builder.addFixer(new EntityTrackedUser(builder.addSchema(Constants.Legacy.Entity.TRACKER_ID_VERSION, Schema::new), true));
// TODO this fixer did nothing       builder.addFixer(new PlayerRespawnData(builder.addSchema( Constants.Sponge.PlayerData.RESPAWN_DATA_1_9_VERSION, Schema::new), true));
        return builder;
    }

    // Builders
    private final Map<Class<?>, DataBuilder<?>> builders = new IdentityHashMap<>();

    private final Map<Class<? extends DataHolder.Immutable<?>>, DataHolderBuilder.Immutable<?, ?>> immutableDataBuilderMap = new MapMaker()
        .concurrencyLevel(4)
        .makeMap();
    // Content updaters
    private final Map<Class<? extends DataSerializable>, List<DataContentUpdater>> updatersMap = new IdentityHashMap<>();

    static boolean allowRegistrations = true;
    private List<DataContentUpdater> customDataUpdaters = new ArrayList<>();

    private final Map<String, List<SpongeDataRegistration>> registrationByPluginContainerId = new IdentityHashMap<>();
    private final Map<String, SpongeDataRegistration> legacyRegistrations = new HashMap<>();
    private List<KeyBasedDataListener<?>> keyListeners = new ArrayList<>();

    public static SpongeDataManager getInstance() {
        return SpongeDataManager.INSTANCE;
    }

    @Inject
    private SpongeDataManager() {
        // TODO register data builders
        this.registerBuilder(ItemStack.class, new SpongeItemStackBuilder());
        this.registerBuilder(ItemStackSnapshot.class, new SpongeItemStackSnapshotDataBuilder());
        this.registerBuilder(EntitySnapshot.class, new SpongeEntitySnapshotBuilder());
        this.registerBuilder(EntityArchetype.class, new SpongeEntityArchetypeBuilder());
    }

    @Override
    public <T extends DataSerializable> void registerBuilder(Class<T> clazz, DataBuilder<T> builder) {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(builder);
        DataBuilder<?> previousBuilder = this.builders.putIfAbsent(clazz, builder);
        if (previousBuilder != null) {
            SpongeCommon.getLogger().warn("A DataBuilder has already been registered for {}. Attempted to register {} instead.", clazz,
                    builder.getClass());
        } else if (!(builder instanceof AbstractDataBuilder)) {
            SpongeCommon.getLogger().warn("A custom DataBuilder is not extending AbstractDataBuilder! It is recommended that "
                    + "the custom data builder does extend it to gain automated content versioning updates and maintain "
                    + "simplicity. The offending builder's class is: {}", builder.getClass());
        }
    }

    @Override
    public <T extends DataSerializable> void registerContentUpdater(Class<T> clazz, DataContentUpdater updater) {
        Objects.requireNonNull(updater, "DataContentUpdater was null!");
        Objects.requireNonNull(clazz, "DataSerializable class was null!");

        final List<DataContentUpdater> updaters = this.updatersMap.computeIfAbsent(clazz, k -> new ArrayList<>());
        updaters.add(updater);
        updaters.sort(Constants.Functional.DATA_CONTENT_UPDATER_COMPARATOR);
    }

    public void registerCustomDataContentUpdater(DataContentUpdater updater) {
        this.customDataUpdaters.add(updater);
    }

    @Override
    public <T extends DataSerializable> Optional<DataContentUpdater> getWrappedContentUpdater(Class<T> clazz, final int fromVersion, final int toVersion) {
        if (fromVersion == toVersion) {
            throw new IllegalArgumentException("Attempting to convert to the same version!");
        }
        if (fromVersion < toVersion) {
            throw new IllegalArgumentException("Attempting to backwards convert data! This isn't supported!");
        }
        final List<DataContentUpdater> updaters = this.updatersMap.get(
            Objects.requireNonNull(clazz, "DataSerializable class was null!"));
        if (updaters == null) {
            return Optional.empty();
        }
        return SpongeDataManager.getWrappedContentUpdater(clazz, fromVersion, toVersion, updaters);
    }

    public Optional<DataContentUpdater> getWrappedCustomContentUpdater(Class<Mutable> mutableClass, int version, int currentCustomData) {
        return SpongeDataManager.getWrappedContentUpdater(mutableClass, version, currentCustomData, this.customDataUpdaters);
    }

    private static Optional<DataContentUpdater> getWrappedContentUpdater(Class<?> clazz, int fromVersion, int toVersion, List<DataContentUpdater> updaters) {
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

    @Override
    @SuppressWarnings({"unchecked"})
    public <T extends DataSerializable> Optional<DataBuilder<T>> getBuilder(Class<T> clazz) {
        Objects.requireNonNull(clazz);
        DataBuilder<?> dataBuilder = this.builders.get(clazz);
        if (dataBuilder != null) {
            return Optional.of((DataBuilder<T>) dataBuilder);
        }
        return Optional.ofNullable((DataBuilder<T>) this.immutableDataBuilderMap.get(clazz));
    }

    @Override
    public <T extends DataSerializable> Optional<T> deserialize(Class<T> clazz, final DataView dataView) {
        final Optional<DataBuilder<T>> optional = this.getBuilder(clazz);
        return optional.flatMap(tDataBuilder -> tDataBuilder.build(dataView));
    }

    @Override
    public <T extends DataHolder.Immutable<T>, B extends DataHolderBuilder.Immutable<T, B>> void register(Class<T> holderClass, B builder) {
        final DataHolderBuilder.Immutable<?, ?> previous = this.immutableDataBuilderMap.putIfAbsent(Objects.requireNonNull(holderClass), Objects.requireNonNull(builder));
        if (previous != null) {
            throw new IllegalStateException("Already registered the DataUtil for " + holderClass.getCanonicalName());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataHolder.Immutable<T>, B extends DataHolderBuilder.Immutable<T, B>> Optional<B> getImmutableBuilder(Class<T> holderClass) {
        return Optional.ofNullable((B) this.immutableDataBuilderMap.get(Objects.requireNonNull(holderClass)));
    }

    public static void finalizeRegistration() {
        SpongeDataManager.allowRegistrations = false;
        SpongeDataManager.getInstance().registerKeyListeners();
    }

    private void registerKeyListeners() {
        this.keyListeners.forEach(this::registerKeyListener0);
        this.keyListeners.clear();
        this.keyListeners = null;
    }

    private void registerKeyListener0(KeyBasedDataListener<?> listener) {
        final EventManager eventManager = Sponge.getEventManager();
        eventManager.registerListener(listener.getOwner(), ChangeDataHolderEvent.ValueChange.class, listener);
    }

    @Override
    public void registerLegacyManipulatorIds(String legacyId, DataRegistration registration) {
        if (!SpongeDataManager.allowRegistrations) {
            throw new IllegalStateException("Data Registration is not allowed anymore.");
        }
        final SpongeDataRegistration previous = this.legacyRegistrations.putIfAbsent(legacyId, (SpongeDataRegistration) registration);
        if (previous != null) {
            throw new IllegalStateException("Legacy registration id already registered: id" + legacyId + " for registration: " + registration);
        }
    }

    public Optional<DataRegistration> getRegistrationForLegacyId(String id) {
        return Optional.ofNullable(this.legacyRegistrations.get(id));
    }

    @SuppressWarnings(value = {"unchecked", "rawtypes"})
    @Override
    public <T> Optional<DataTranslator<T>> getTranslator(Class<T> objectClass) {
        final Registry<DataTranslator> registry = SpongeCommon.getRegistry().getCatalogRegistry().getRegistry(DataTranslator.class);
        final DataTranslator reverseMapping = ((MappedRegistry<DataTranslator, Class>) registry).getReverseMapping(objectClass);
        return Optional.ofNullable(reverseMapping);
    }

    @Override
    public Collection<DataRegistration> getAllRegistrationsFor(PluginContainer container) {
        return Collections.unmodifiableCollection(this.registrationByPluginContainerId.getOrDefault(container.getMetadata().getId(), Collections.emptyList()));
    }

    @Override
    public DataContainer createContainer() {
        return new MemoryDataContainer();
    }

    @Override
    public DataContainer createContainer(DataView.SafetyMode safety) {
        return new MemoryDataContainer(safety);
    }

    public static boolean areRegistrationsComplete() {
        return !SpongeDataManager.allowRegistrations;
    }

    public <E extends DataHolder> void registerKeyListener(KeyBasedDataListener<E> keyListener) {
        if (SpongeDataManager.areRegistrationsComplete()) { // TODO do we need actually to wait for listener registration?
            this.registerKeyListener(keyListener);
        } else {
            this.keyListeners.add(keyListener);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void registerCustomDataRegistration(SpongeDataRegistration registration) {
        if (!SpongeDataManager.allowRegistrations) {
            throw new IllegalStateException("Data Registration is not allowed anymore.");
        }

        if (!registration.key.getNamespace().equals(registration.plugin.getMetadata().getId())) {
            throw new IllegalStateException(String.format("Registration namespace (%s) is not matching plugin id (%s)", registration.key, registration.plugin.getMetadata().getId()));
        }

        this.registrations.put(registration.getKey(), registration);
        this.registrationByPluginContainerId.computeIfAbsent(registration.getPluginContainer().getMetadata().getId(), k -> new ArrayList<>()).add(registration);

        for (final DataStore dataStore : registration.getDataStores()) {
            this.dataStoreRegistry.register(dataStore, registration.getKeys());
        }

        for (Key key : registration.getKeys()) {
            this.registerCustomDataProviderForKey(registration, key);
        }
    }

    private <V extends Value<E>, E> void registerCustomDataProviderForKey(final SpongeDataRegistration registration, final Key<V> key) {
        final Collection<DataProvider<V, E>> providers = registration.getProvidersFor(key);

        final Set<TypeToken<? extends DataHolder>> dataStoreSupportedTokens = new HashSet<>();
        this.dataStoreRegistry.getDataStores(key).stream().map(DataStore::getSupportedTokens).forEach(dataStoreSupportedTokens::addAll);

        for (DataProvider<V, E> provider : providers) {
            this.dataProviderRegistry.register(provider);
            dataStoreSupportedTokens.removeIf(provider::isSupported);
        }

        // For all tokens supported by a datastore register a CustomDataProvider
        if (!dataStoreSupportedTokens.isEmpty()) {
            this.dataProviderRegistry.register(new CustomDataProvider<>(key, dataStoreSupportedTokens));
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void registerDataRegistration(SpongeDataRegistration registration) {
        this.registrations.put(registration.getKey(), registration);
        this.registrationByPluginContainerId.computeIfAbsent(registration.getPluginContainer().getMetadata().getId(), k -> new ArrayList<>()).add(registration);
        for (DataStore dataStore : registration.getDataStores()) {
            this.dataStoreRegistry.register(dataStore, registration.getKeys());
        }
        for (Key key : registration.getKeys()) {
            final Collection<DataProvider<?, ?>> providers = registration.getProvidersFor(key);
            for (DataProvider<?, ?> provider : providers) {
                this.dataProviderRegistry.register(provider);
            }
        }
    }

    public void registerDefaultProviders() {
        this.dataProviderRegistry.registerDefaultProviders();
    }

    public static DataStoreRegistry getDatastoreRegistry() {
        return SpongeDataManager.INSTANCE.dataStoreRegistry;
    }

    public static DataProviderRegistry getProviderRegistry() {
        return SpongeDataManager.INSTANCE.dataProviderRegistry;
    }

    public Optional<DataRegistration> getRegistration(ResourceKey key) {
        return Optional.ofNullable(this.registrations.get(key));
    }
}
