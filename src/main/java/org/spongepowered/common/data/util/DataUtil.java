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
package org.spongepowered.common.data.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.DataManipulator.Immutable;
import org.spongepowered.api.data.DataManipulator.Mutable;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataContentUpdater;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataSerializable;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.world.Location;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.data.CustomDataHolderBridge;
import org.spongepowered.common.data.DataProcessor;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.SpongeManipulatorRegistry;
import org.spongepowered.common.data.ValueProcessor;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.persistence.SerializedDataTransaction;
import org.spongepowered.common.data.processor.common.AbstractSingleDataSingleTargetProcessor;
import org.spongepowered.common.util.Constants;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import javax.annotation.Nullable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings({"unchecked", "UnstableApiUsage"})
public final class DataUtil {

    public static DataView checkDataExists(final DataView dataView, final DataQuery query) throws InvalidDataException {
        if (!checkNotNull(dataView).contains(checkNotNull(query))) {
            throw new InvalidDataException("Missing data for query: " + query.asString('.'));
        }
        return dataView;
    }



    public static <T> T getData(final DataView dataView, final DataQuery query, final Class<T> data) throws InvalidDataException {
        checkDataExists(dataView, query);
        final Object object = dataView.get(query).orElseThrow(dataNotFound());
        if (data.isInstance(object)) {
            return (T) object;
        }
        throw new InvalidDataException("Data does not match!");
    }

    public static List<DataView> getSerializedManipulatorList(final Iterable<? extends Mutable> manipulators) {
        return getSerializedManipulatorList(manipulators, DataUtil::getRegistrationFor);
    }

    public static List<DataView> getSerializedImmutableManipulatorList(final Iterable<? extends Immutable<?, ?>> manipulators) {
        return getSerializedManipulatorList(manipulators, DataUtil::getRegistrationFor);
    }

    private static <T extends DataSerializable> List<DataView> getSerializedManipulatorList(final Iterable<? extends T> manipulators,
        final Function<T, ? extends DataRegistration<?, ?>> func) {
        checkNotNull(manipulators);
        final ImmutableList.Builder<DataView> builder = ImmutableList.builder();
        for (final T manipulator : manipulators) {
            final DataContainer container = DataContainer.createNew();
            container.set(Queries.CONTENT_VERSION, Constants.Sponge.CURRENT_CUSTOM_DATA);
            container.set(Constants.Sponge.DATA_ID, func.apply(manipulator).getId())
                     .set(Constants.Sponge.INTERNAL_DATA, manipulator.toContainer());
            builder.add(container);
        }
        return builder.build();
    }

    public static SerializedDataTransaction deserializeManipulatorList(final List<? extends DataView> containers) {
        checkNotNull(containers);
        final SerializedDataTransaction.Builder builder = SerializedDataTransaction.builder();
        for (final DataView view : containers) {
            final DataView updated = updateDataViewForDataManipulator(view);
            findDataId(builder, updated).ifPresent(dataId -> tryDeserializeManipulator(builder, updated, dataId));
        }
        return builder.build();
    }

    private static Optional<String> findDataId(final SerializedDataTransaction.Builder builder, final DataView view) {
        final Optional<String> dataId = view.getString(Constants.Sponge.DATA_ID);
        if (!dataId.isPresent()) {
            // Show failed deserialization when this is v1 data
            @SuppressWarnings("deprecation") final String dataClass = view.getString(Constants.Sponge.DATA_CLASS).orElseThrow(DataUtil.dataNotFound());
            addFailedDeserialization(builder, view, dataClass, null);
        }
        return dataId;
    }

    private static void tryDeserializeManipulator(final SerializedDataTransaction.Builder builder, final DataView view, final String dataId) {
        final DataView manipulatorView = view.getView(Constants.Sponge.INTERNAL_DATA).orElseThrow(DataUtil.dataNotFound());
        try {
            final Optional<Mutable> build = deserializeManipulator(dataId, manipulatorView);
            if (build.isPresent()) {
                builder.successfulData(build.get());
            } else {
                addFailedDeserialization(builder, view, dataId, null);
            }
        } catch (final Exception e) {
            addFailedDeserialization(builder, view, dataId, e);
        }
    }

    private static Optional<DataManipulator.Mutable> deserializeManipulator(final String dataId, final DataView data) {
        return getRegistrationFor(dataId) // Get Registration
                .map(DataRegistration::getDataStore) // Find Builder
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map()
                .flatMap(b -> (Optional<T>) b.build(data)); // Build CustomData
    }

    private static void addFailedDeserialization(
        final SerializedDataTransaction.Builder builder, final DataView view, final String dataId, @Nullable final Throwable cause) {
        SpongeImpl.getCustomDataConfigAdapter().getConfig().getDataRegistrationConfig().addFailedData(dataId, cause);
        SpongeImpl.getCustomDataConfigAdapter().getConfig().getDataRegistrationConfig().purgeOrAllow(builder, dataId, view);
        try {
            // we need to save the config with the updated values.
            SpongeImpl.getCustomDataConfigAdapter().save();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private static DataView updateDataViewForDataManipulator(final DataView dataView) {
        final int version = dataView.getInt(Queries.CONTENT_VERSION).orElse(1);
        if (version != Constants.Sponge.CURRENT_CUSTOM_DATA) {
            final DataContentUpdater contentUpdater = SpongeDataManager.getInstance()
                .getWrappedContentUpdater(Mutable.class, version, Constants.Sponge.CURRENT_CUSTOM_DATA)
                .orElseThrow(() -> new IllegalArgumentException("Could not find a content updater for DataManipulator information with version: " + version));
            return contentUpdater.update(dataView);
        }
        return dataView;
    }

    public static ImmutableList<Immutable<?, ?>> deserializeImmutableManipulatorList(final List<? extends DataView> containers) {
        checkNotNull(containers);
        final ImmutableList.Builder<Immutable<?, ?>> builder = ImmutableList.builder();
        for (DataView view : containers) {
            view = updateDataViewForDataManipulator(view);
            final String dataId = view.getString(Constants.Sponge.DATA_ID).orElseThrow(DataUtil.dataNotFound());
            final DataView manipulatorView = view.getView(Constants.Sponge.INTERNAL_DATA).orElseThrow(DataUtil.dataNotFound());
            try {
                deserializeManipulator(dataId, manipulatorView).map(Mutable::asImmutable).ifPresent(builder::add);
            } catch (final Exception e) {
                new InvalidDataException("Could not translate " + dataId + "!", e).printStackTrace();
            }
        }
        return builder.build();
    }

    public static Location getLocation(final DataView view, final boolean castToInt) {
        final UUID worldUuid = UUID.fromString(view.getString(Queries.WORLD_ID).orElseThrow(dataNotFound()));
        final double x = view.getDouble(Queries.POSITION_X).orElseThrow(dataNotFound());
        final double y = view.getDouble(Queries.POSITION_Y).orElseThrow(dataNotFound());
        final double z = view.getDouble(Queries.POSITION_Z).orElseThrow(dataNotFound());
        if (castToInt) {
            return Location.of(SpongeImpl.getGame().getServer().getWorldManager().getWorld(worldUuid).orElseThrow(dataNotFound()), (int) x, (int) y, (int) z);
        }
        return Location.of(SpongeImpl.getGame().getServer().getWorldManager().getWorld(worldUuid).orElseThrow(dataNotFound()), x, y, z);
    }

    public static Vector3i getPosition3i(final DataView view) {
        checkDataExists(view, Constants.Sponge.SNAPSHOT_WORLD_POSITION);
        final DataView internal = view.getView(Constants.Sponge.SNAPSHOT_WORLD_POSITION).orElseThrow(dataNotFound());
        final int x = internal.getInt(Queries.POSITION_X).orElseThrow(dataNotFound());
        final int y = internal.getInt(Queries.POSITION_Y).orElseThrow(dataNotFound());
        final int z = internal.getInt(Queries.POSITION_Z).orElseThrow(dataNotFound());
        return new Vector3i(x, y, z);
    }

    public static Vector3d getPosition3d(final DataView view, final DataQuery query) {
        checkDataExists(view, query);
        final DataView internal = view.getView(query).orElseThrow(dataNotFound());
        final double x = internal.getDouble(Queries.POSITION_X).orElseThrow(dataNotFound());
        final double y = internal.getDouble(Queries.POSITION_Y).orElseThrow(dataNotFound());
        final double z = internal.getDouble(Queries.POSITION_Z).orElseThrow(dataNotFound());
        return new Vector3d(x, y, z);
    }

    private static Supplier<InvalidDataException> dataNotFound() {
        return INVALID_DATA_EXCEPTION_SUPPLIER;
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
    public static <T extends Mutable<T, I>, I extends Immutable<I, T>> void
    registerDataProcessorAndImpl(final Class<T> manipulatorClass, final Class<? extends T> implClass, final Class<I> immutableDataManipulator,
        final Class<? extends I> implImClass, final DataProcessor<T, I> processor) {
        checkState(!SpongeDataManager.areRegistrationsComplete(), "Registrations are no longer allowed!");
        checkArgument(!Modifier.isAbstract(implClass.getModifiers()), "The Implemented DataManipulator class cannot be abstract!");
        checkArgument(!Modifier.isInterface(implClass.getModifiers()), "The Implemented DataManipulator class cannot be an interface!");
        checkArgument(!Modifier.isAbstract(implImClass.getModifiers()), "The implemented ImmutableDataManipulator class cannot be an interface!");
        checkArgument(!Modifier.isInterface(implImClass.getModifiers()), "The implemented ImmutableDataManipulator class cannot be an interface!");
        checkArgument(!(processor instanceof DataProcessorDelegate), "Cannot register DataProcessorDelegates!");

        SpongeManipulatorRegistry.getInstance().register(manipulatorClass, implClass, immutableDataManipulator, implImClass, processor);
    }

    public static <E, V extends Value<E>, T extends Mutable<T, I>, I extends Immutable<I, T>> void
    registerDualProcessor(final Class<T> manipulatorClass, final Class<? extends T> implClass, final Class<I> immutableDataManipulator,
        final Class<? extends I> implImClass, final AbstractSingleDataSingleTargetProcessor<?, E, V, T, I> processor) {
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
    public static <T extends Mutable<T, I>, I extends Immutable<I, T>> Optional<DataProcessor<T, I>> getProcessor(
        final Class<T> mutableClass) {
        return Optional.ofNullable((DataProcessor<T, I>) SpongeManipulatorRegistry.getInstance().getDelegate(mutableClass));
    }

    /**
     * Gets a wildcarded typed {@link DataProcessor} for the provided
     * {@link DataManipulator} class. This is primarily useful when the
     * type information is not known (due to type erasure).
     *
     * @param mutableClass The mutable class
     * @return The data processor
     */
    public static Optional<DataProcessor<?, ?>> getWildProcessor(final Class<? extends Mutable<?, ?>> mutableClass) {
        return Optional.ofNullable(SpongeManipulatorRegistry.getInstance().getDelegate(mutableClass));
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
    public static <T extends Mutable<T, I>, I extends Immutable<I, T>> Optional<DataProcessor<T, I>>
    getImmutableProcessor(final Class<I> immutableClass) {
        return Optional.ofNullable((DataProcessor<T, I>) SpongeManipulatorRegistry.getInstance().getDelegate(immutableClass));
    }

    /**
     * Gets the raw typed {@link DataProcessor} for the
     * {@link ImmutableDataManipulator} class.
     *
     * @param immutableClass The immutable data manipulator class
     * @return The raw typed data processor
     */
    @SuppressWarnings("rawtypes")
    public static Optional<DataProcessor> getWildImmutableProcessor(final Class<? extends Immutable<?, ?>> immutableClass) {
        return Optional.ofNullable(SpongeManipulatorRegistry.getInstance().getDelegate(immutableClass));
    }


    public static <E, V extends Value<E>> void registerValueProcessor(final Key<V> key, final ValueProcessor<E, V> valueProcessor) {
        checkState(!SpongeDataManager.areRegistrationsComplete(), "Registrations are no longer allowed!");
        checkNotNull(valueProcessor);
        checkArgument(!(valueProcessor instanceof ValueProcessorDelegate), "Cannot register ValueProcessorDelegates! READ THE DOCS!");
        checkNotNull(key);
        SpongeManipulatorRegistry.getInstance().registerValueProcessor(key, valueProcessor);
    }


    public static DataRegistration<?, ?> getRegistrationFor(final Mutable<?, ?> manipulator) {
        return SpongeManipulatorRegistry.getInstance().getRegistrationFor(manipulator);
    }


    private static Optional<DataRegistration<?, ?>> getRegistrationFor(final String id) {
        return (Optional<DataRegistration<?, ?>>) (Optional<?>) SpongeManipulatorRegistry.getInstance().getRegistrationFor(id);
    }

    public static DataTransactionResult apply(final CompoundNBT compound, final Mutable<?, ?> manipulator) {
        if (!compound.contains(Constants.Forge.FORGE_DATA, Constants.NBT.TAG_COMPOUND)) {
            compound.put(Constants.Forge.FORGE_DATA, new CompoundNBT());
        }
        final CompoundNBT forgeCompound = compound.getCompound(Constants.Forge.FORGE_DATA);
        if (!forgeCompound.contains(Constants.Sponge.SPONGE_DATA, Constants.NBT.TAG_COMPOUND)) {
            forgeCompound.put(Constants.Sponge.SPONGE_DATA, new CompoundNBT());
        }
        final CompoundNBT spongeTag = forgeCompound.getCompound(Constants.Sponge.SPONGE_DATA);

        // Validate that the custom manipulator isn't already existing in the compound
        final ListNBT list;
        final DataRegistration<?, ?> registration = getRegistrationFor(manipulator);

        if (spongeTag.contains(Constants.Sponge.CUSTOM_MANIPULATOR_TAG_LIST, Constants.NBT.TAG_LIST)) {
            list = spongeTag.getList(Constants.Sponge.CUSTOM_MANIPULATOR_TAG_LIST, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < list.tagCount(); i++) {
                final CompoundNBT dataCompound = list.getCompound(i);
                final String dataId = dataCompound.getString(Constants.Sponge.MANIPULATOR_ID);
                if (dataId.equalsIgnoreCase(registration.getId())) {
                    final CompoundNBT current = dataCompound.getCompound(Constants.Sponge.CUSTOM_DATA);
                    final DataContainer currentView = NbtTranslator.getInstance().translate(current);
                    final Optional<Mutable<?, ?>> existingManipulator = deserializeManipulator(dataId, currentView);
                    final DataContainer replacement = manipulator.toContainer();
                    final CompoundNBT replacementCompound = NbtTranslator.getInstance().translateData(replacement);
                    dataCompound.put(Constants.Sponge.CUSTOM_DATA, replacementCompound);
                    return DataTransactionResult.successReplaceResult(manipulator.getValues(), existingManipulator.map(Mutable::getValues)
                        .orElseGet(ImmutableSet::of));
                }
            }
        } else {
            list = new ListNBT();
            // We are now adding to the list, not replacing
            final CompoundNBT newCompound = new CompoundNBT();
            newCompound.putString(Constants.Sponge.MANIPULATOR_ID, registration.getId());
            final DataContainer dataContainer = manipulator.toContainer();
            final CompoundNBT dataCompound = NbtTranslator.getInstance().translateData(dataContainer);
            newCompound.put(Constants.Sponge.CUSTOM_DATA, dataCompound);
            list.add(newCompound);
            spongeTag.put(Constants.Sponge.CUSTOM_MANIPULATOR_TAG_LIST, list);
        }

        return DataTransactionResult.builder().result(DataTransactionResult.Type.SUCCESS).success(manipulator.getValues()).build();
    }

    @SuppressWarnings("rawtypes")
    public static DataTransactionResult remove(final CompoundNBT data, final Class<? extends Mutable<?, ?>> containerClass) {
        if (!data.contains(Constants.Forge.FORGE_DATA, Constants.NBT.TAG_COMPOUND)) {
            return DataTransactionResult.successNoData();
        }
        final CompoundNBT forgeTag = data.getCompound(Constants.Forge.FORGE_DATA);
        if (!forgeTag.contains(Constants.Sponge.SPONGE_DATA, Constants.NBT.TAG_COMPOUND)) {
            return DataTransactionResult.successNoData();
        }
        final CompoundNBT spongeData = forgeTag.getCompound(Constants.Sponge.SPONGE_DATA);
        if (!spongeData.contains(Constants.Sponge.CUSTOM_MANIPULATOR_TAG_LIST, Constants.NBT.TAG_LIST)) {
            return DataTransactionResult.successNoData();
        }
        final ListNBT dataList = spongeData.getList(Constants.Sponge.CUSTOM_MANIPULATOR_TAG_LIST, Constants.NBT.TAG_COMPOUND);
        if (dataList.tagCount() == 0) {
            return DataTransactionResult.successNoData();
        }
        final DataRegistration<?, ?> registration = SpongeManipulatorRegistry.getInstance().getRegistrationFor((Class) containerClass);
        for (int i = 0; i < dataList.tagCount(); i++) {
            final CompoundNBT dataCompound = dataList.getCompound(i);
            final String dataId = dataCompound.getString(Constants.Sponge.MANIPULATOR_ID);
            if (registration.getId().equalsIgnoreCase(dataId)) {
                final CompoundNBT current = dataCompound.getCompound(Constants.Sponge.CUSTOM_DATA);
                final DataContainer currentView = NbtTranslator.getInstance().translate(current);
                final Optional<Mutable<?, ?>> existing = deserializeManipulator(dataId, currentView);
                dataList.removeTag(i);
                return existing.map(Mutable::getValues)
                    .map(DataTransactionResult::successRemove)
                    .orElseGet(DataTransactionResult::successNoData);
            }
        }
        return DataTransactionResult.successNoData();
    }

    @SuppressWarnings("unchecked")
    public static void readCustomData(final CompoundNBT compound, final DataHolder dataHolder) {
        if (dataHolder instanceof CustomDataHolderBridge) {
            if (compound.contains(Constants.Sponge.CUSTOM_MANIPULATOR_TAG_LIST, Constants.NBT.TAG_LIST)) {
                final ListNBT list = compound.getList(Constants.Sponge.CUSTOM_MANIPULATOR_TAG_LIST, Constants.NBT.TAG_COMPOUND);
                final ImmutableList.Builder<DataView> builder = ImmutableList.builder();
                translateTagListToView(builder, list);
                try {
                    final SerializedDataTransaction transaction = deserializeManipulatorList(builder.build());
                    final List<Mutable<?, ?>> manipulators = transaction.deserializedManipulators;
                    for (final Mutable<?, ?> manipulator : manipulators) {
                        dataHolder.offer(manipulator);
                    }
                    if (!transaction.failedData.isEmpty()) {
                        ((CustomDataHolderBridge) dataHolder).bridge$addFailedData(transaction.failedData);
                    }
                } catch (final InvalidDataException e) {
                    SpongeImpl.getLogger().error("Could not translate custom plugin data! ", e);
                }
            }
            if (compound.contains(Constants.Sponge.FAILED_CUSTOM_DATA, Constants.NBT.TAG_LIST)) {
                final ListNBT list = compound.getList(Constants.Sponge.FAILED_CUSTOM_DATA, Constants.NBT.TAG_COMPOUND);
                final ImmutableList.Builder<DataView> builder = ImmutableList.builder();
                translateTagListToView(builder, list);
                // We want to attempt to refresh the failed data if it does succeed in getting read.
                compound.remove(Constants.Sponge.FAILED_CUSTOM_DATA);
                // Re-attempt to deserialize custom data
                final SerializedDataTransaction transaction = deserializeManipulatorList(builder.build());
                final List<Mutable<?, ?>> manipulators = transaction.deserializedManipulators;
                final List<Class<? extends Mutable<?, ?>>> classesLoaded = new ArrayList<>();
                for (final Mutable<?, ?> manipulator : manipulators) {
                    if (!classesLoaded.contains(manipulator.getClass())) {
                        classesLoaded.add((Class<? extends Mutable<?, ?>>) manipulator.getClass());
                        // If for any reason a failed data was not deserialized, but
                        // there already exists new data, we just simply want to
                        // ignore the failed data for removal.
                        if (!((CustomDataHolderBridge) dataHolder).bridge$getCustom(manipulator.getClass()).isPresent()) {
                            dataHolder.offer(manipulator);
                        }
                    }
                }
                if (!transaction.failedData.isEmpty()) {
                    ((CustomDataHolderBridge) dataHolder).bridge$addFailedData(transaction.failedData);
                }
            }
        }
    }

    private static void translateTagListToView(final ImmutableList.Builder<? super DataView> builder, final ListNBT list) {
        if (!list.isEmpty()) {
            for (int i = 0; i < list.tagCount(); i++) {
                final CompoundNBT internal = list.getCompound(i);
                builder.add(NbtTranslator.getInstance().translateFrom(internal));
            }

        }
    }

    public static void writeCustomData(final CompoundNBT compound, final DataHolder dataHolder) {
        if (dataHolder instanceof CustomDataHolderBridge) {
            final Collection<Mutable<?, ?>> manipulators = ((CustomDataHolderBridge) dataHolder).bridge$getCustomManipulators();
            if (!manipulators.isEmpty()) {
                final List<DataView> manipulatorViews = getSerializedManipulatorList(manipulators);
                final ListNBT manipulatorTagList = new ListNBT();
                for (final DataView dataView : manipulatorViews) {
                    manipulatorTagList.add(NbtTranslator.getInstance().translateData(dataView));
                }
                compound.put(Constants.Sponge.CUSTOM_MANIPULATOR_TAG_LIST, manipulatorTagList);
            }
            final List<DataView> failedData = ((CustomDataHolderBridge) dataHolder).bridge$getFailedData();
            if (!failedData.isEmpty()) {
                final ListNBT failedList = new ListNBT();
                for (final DataView failedDatum : failedData) {
                    failedList.add(NbtTranslator.getInstance().translateData(failedDatum));
                }
                compound.put(Constants.Sponge.FAILED_CUSTOM_DATA, failedList);
            }
        }
    }

}
