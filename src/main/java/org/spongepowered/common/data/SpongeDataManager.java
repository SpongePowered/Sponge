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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.MapMaker;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataHolderBuilder;
import org.spongepowered.api.data.DataManager;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.DataManipulator.Mutable;
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
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.data.ChangeDataHolderEvent;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.data.CustomDataHolderBridge;
import org.spongepowered.common.config.DataSerializableTypeSerializer;
import org.spongepowered.common.data.key.KeyBasedDataListener;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.registry.MappedRegistry;
import org.spongepowered.common.registry.SpongeCatalogRegistry;
import org.spongepowered.common.util.Constants;
import org.spongepowered.plugin.PluginContainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
public final class SpongeDataManager implements DataManager {
    private static final SpongeDataManager INSTANCE = new SpongeDataManager();

    private static final TypeToken<CatalogType> catalogTypeToken = TypeToken.of(CatalogType.class);
    private static final TypeToken<DataSerializable> dataSerializableTypeToken = TypeToken.of(DataSerializable.class);

    static {
        TypeSerializers.getDefaultSerializers().registerPredicate(
            // We have a separate type serializer for CatalogTypes, so we explicitly discount them here.
            // See https://github.com/SpongePowered/SpongeCommon/issues/1348
            x -> dataSerializableTypeToken.isSupertypeOf(x) && !catalogTypeToken.isSupertypeOf(x), new DataSerializableTypeSerializer()
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

    private final Map<PluginContainer, List<SpongeDataRegistration>> pluginRegistrations = new IdentityHashMap<>();
    private final Map<Key<?>, SpongeDataRegistration> registrationByKey = new HashMap<>();
    private final Map<String, SpongeDataRegistration> legacyRegistrations = new HashMap<>();
    private List<KeyBasedDataListener<?>> keyListeners = new ArrayList<>();

    public static SpongeDataManager getInstance() {
        return INSTANCE;
    }

    @Inject
    private SpongeDataManager() {}

    @Override
    public <T extends DataSerializable> void registerBuilder(Class<T> clazz, DataBuilder<T> builder) {
        Preconditions.checkNotNull(clazz);
        Preconditions.checkNotNull(builder);
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
        Preconditions.checkNotNull(updater, "DataContentUpdater was null!");
        Preconditions.checkNotNull(clazz, "DataSerializable class was null!");

        final List<DataContentUpdater> updaters = this.updatersMap.computeIfAbsent(clazz, k -> new ArrayList<>());
        updaters.add(updater);
        Collections.sort(updaters, Constants.Functional.DATA_CONTENT_UPDATER_COMPARATOR);
    }

    public void registerCustomDataContentUpdater(DataContentUpdater updater) {
        this.customDataUpdaters.add(updater);
    }

    @Override
    public <T extends DataSerializable> Optional<DataContentUpdater> getWrappedContentUpdater(Class<T> clazz, final int fromVersion, final int toVersion) {
        Preconditions.checkArgument(fromVersion != toVersion, "Attempting to convert to the same version!");
        Preconditions.checkArgument(fromVersion < toVersion, "Attempting to backwards convert data! This isn't supported!");
        final List<DataContentUpdater> updaters = this.updatersMap.get(
            Preconditions.checkNotNull(clazz, "DataSerializable class was null!"));
        if (updaters == null) {
            return Optional.empty();
        }
        return getWrappedContentUpdater(clazz, fromVersion, toVersion, updaters);
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
        Preconditions.checkNotNull(clazz);
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
        final DataHolderBuilder.Immutable<?, ?> previous = this.immutableDataBuilderMap.putIfAbsent(Preconditions.checkNotNull(holderClass), Preconditions.checkNotNull(builder));
        if (previous != null) {
            throw new IllegalStateException("Already registered the DataUtil for " + holderClass.getCanonicalName());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataHolder.Immutable<T>, B extends DataHolderBuilder.Immutable<T, B>> Optional<B> getImmutableBuilder(Class<T> holderClass) {
        return Optional.ofNullable((B) this.immutableDataBuilderMap.get(Preconditions.checkNotNull(holderClass)));
    }

    public static void finalizeRegistration() {
        allowRegistrations = false;
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
        Preconditions.checkState(allowRegistrations);
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

        return Collections.unmodifiableCollection(this.pluginRegistrations.getOrDefault(container, Collections.emptyList()));
    }

    @Override
    public DataContainer createContainer() {
        return new MemoryDataContainer();
    }

    @Override
    public DataContainer createContainer(DataView.SafetyMode safety) {
        return new MemoryDataContainer(safety);
    }

    void validateRegistration(SpongeDataRegistration registration) {
        Preconditions.checkState(allowRegistrations);

        // TODO do we want this?
        Preconditions.checkState(registration.key.getNamespace().equals(registration.plugin.getMetadata().getId()), "Registration namespace not matchin plugin id");

        // Make sure the Keys are not already registered
        Preconditions.checkState(Collections.disjoint(this.registrationByKey.keySet(), registration.keys), "Duplicate key registration");
        Preconditions.checkState(Collections.disjoint(this.registrationByKey.keySet(), registration.dataProviderMap.keySet()), "Duplicate key registration");
    }

    public static boolean areRegistrationsComplete() {
        return !allowRegistrations;
    }

    public <E extends DataHolder> void registerKeyListener(KeyBasedDataListener<E> keyListener) {
        if (areRegistrationsComplete()) { // TODO do we need actually to wait for listener registration?
            this.registerKeyListener(keyListener);
        } else {
            this.keyListeners.add(keyListener);
        }
    }

    public void registerDataRegistration(SpongeDataRegistration registration) {
        this.validateRegistration(registration);

        this.pluginRegistrations.computeIfAbsent(registration.getPluginContainer(), k -> new ArrayList<>()).add(registration);
        for (Key<?> key : registration.getKeys()) {
            this.registrationByKey.put(key, registration);
        }
    }

    private Optional<SpongeDataRegistration> getDataRegistration(Key<?> key) {
        return Optional.ofNullable(this.registrationByKey.get(key));
    }

    public void serializeCustomData(CompoundNBT compound, Object object) {
        if (object instanceof CustomDataHolderBridge) {
            final Collection<Mutable> manipulators = ((CustomDataHolderBridge) object).bridge$getCustomManipulators();
            if (!manipulators.isEmpty()) {
                final DataHolder dataHolder = (DataHolder) object;
                final ListNBT manipulatorTagList = new ListNBT();
                for (DataManipulator.Mutable manipulator : manipulators) {
// TODO dataproviders?
//                    manipulator.getKeys().stream().map(this::getDataProvider)
//                            .filter(Optional::isPresent).map(Optional::get).distinct()
//                            .forEach(provider -> provider.offer((DataHolder.Mutable) dataHolder, manipulator.get(provider.getKey()).orElse(null)));

                    // Get all data registrations for the keys in the manipulator (this usually should be only one)
                    manipulator.getKeys().stream().map(this::getDataRegistration)
                            .filter(Optional::isPresent).map(Optional::get).distinct().forEach(registration -> {
                    // For each registration attempt to serialize using the datastore for the dataholder
                        registration.getDataStore(TypeToken.of(dataHolder.getClass())).ifPresent(dataStore -> {
                            final DataView serialized = dataStore.serialize(manipulator);
                            if (!serialized.isEmpty()) { // Omit if the datastore did not serialize anything
                                final DataContainer container = DataContainer.createNew();
                                // Add Metadata
                                container.set(Queries.CONTENT_VERSION, Constants.Sponge.CURRENT_CUSTOM_DATA)
                                         .set(Constants.Sponge.DATA_ID, registration.getKey().toString())
                                         .set(Constants.Sponge.INTERNAL_DATA, serialized);
                                manipulatorTagList.add(NbtTranslator.getInstance().translateData(container));
                            }
                        });
                    });
                }
                compound.put(Constants.Sponge.CUSTOM_MANIPULATOR_TAG_LIST, manipulatorTagList);
            } else {
                compound.remove(Constants.Sponge.CUSTOM_MANIPULATOR_TAG_LIST);
            }

            final List<DataView> failedData = ((CustomDataHolderBridge) object).bridge$getFailedData();
            if (!failedData.isEmpty()) {
                final ListNBT failedList = new ListNBT();
                for (final DataView failedDatum : failedData) {
                    failedList.add(NbtTranslator.getInstance().translateData(failedDatum));
                }
                compound.put(Constants.Sponge.FAILED_CUSTOM_DATA, failedList);
            } else {
                compound.remove(Constants.Sponge.FAILED_CUSTOM_DATA);
            }
        }
    }

    private DataView updateDataViewForDataManipulator(final DataView dataView) {
        final int version = dataView.getInt(Queries.CONTENT_VERSION).orElse(1);
        if (version != Constants.Sponge.CURRENT_CUSTOM_DATA) {
            final DataContentUpdater contentUpdater = getWrappedContentUpdater(DataManipulator.Mutable.class, version, Constants.Sponge.CURRENT_CUSTOM_DATA, this.customDataUpdaters)
                    .orElseThrow(() -> new IllegalArgumentException("Could not find a content updater for DataManipulator information with version: " + version));
            return contentUpdater.update(dataView);
        }
        return dataView;
    }

    public void deserializeCustomData(CompoundNBT compound, Object object) {
        if (object instanceof CustomDataHolderBridge) {
            if (compound.contains(Constants.Sponge.CUSTOM_MANIPULATOR_TAG_LIST, Constants.NBT.TAG_LIST)) {
                final ListNBT list = compound.getList(Constants.Sponge.CUSTOM_MANIPULATOR_TAG_LIST, Constants.NBT.TAG_COMPOUND);
                if (!list.isEmpty()) {
                    final DataHolder dataHolder = (DataHolder) object;
                    final ImmutableList.Builder<DataView> failed = ImmutableList.builder();
                    for (INBT inbt : list) {
                        final DataView dataContainer = this.updateDataViewForDataManipulator(NbtTranslator.getInstance().translate((CompoundNBT) inbt));
                        final SpongeCatalogRegistry catalogRegistry = SpongeCommon.getRegistry().getCatalogRegistry();
                        // Then find the registration for deserialization
                        final Optional<DataRegistration> registration = dataContainer.getString(Constants.Sponge.DATA_ID)
                                .flatMap(registrationId -> catalogRegistry.get(DataRegistration.class, ResourceKey.resolve(registrationId)));
                        // Find and attempt to deserialize with the datastore for this dataholder
                        final Optional<DataStore> dataStore = registration.flatMap(r -> r.getDataStore(TypeToken.of(dataHolder.getClass())));
                        if (dataStore.isPresent()) {
                            final DataView internalData = dataContainer.getView(Constants.Sponge.INTERNAL_DATA).orElse(DataContainer.createNew());
                            final Mutable mutable = dataStore.get().deserialize(internalData);
                            // Offer all deserialized data to the custom data holder
                            for (Key k : mutable.getKeys()) {
                                ((CustomDataHolderBridge) object).bridge$offerCustom(k, mutable.get(k).orElse(null));
                            }
                        } else { // If no registration/datastore was found add this to failed data
                            failed.add(dataContainer);
                        }
                    }
                    ((CustomDataHolderBridge) object).bridge$addFailedData(failed.build());
                }
            }
        }
    }

}
