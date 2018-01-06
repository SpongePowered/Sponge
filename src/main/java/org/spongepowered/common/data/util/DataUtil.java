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

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.persistence.DataContentUpdater;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.DataProcessor;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.SpongeManipulatorRegistry;
import org.spongepowered.common.data.ValueProcessor;
import org.spongepowered.common.data.fixer.entity.EntityTrackedUser;
import org.spongepowered.common.data.fixer.entity.player.PlayerRespawnData;
import org.spongepowered.common.data.fixer.world.SpongeLevelFixer;
import org.spongepowered.common.data.nbt.NbtDataType;
import org.spongepowered.common.data.nbt.data.NbtDataProcessor;
import org.spongepowered.common.data.nbt.validation.DelegateDataValidator;
import org.spongepowered.common.data.nbt.validation.RawDataValidator;
import org.spongepowered.common.data.nbt.validation.ValidationType;
import org.spongepowered.common.data.nbt.value.NbtValueProcessor;
import org.spongepowered.common.data.persistence.SerializedDataTransaction;
import org.spongepowered.common.data.processor.common.AbstractSingleDataSingleTargetProcessor;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nullable;

@SuppressWarnings("unchecked")
public final class DataUtil {

    // TODO Bump this when needing to fix sponge added file data
    public static final int DATA_VERSION = 1;
    public static final DataFixer spongeDataFixer = new DataFixer(DATA_VERSION);
    private static final Supplier<InvalidDataException> INVALID_DATA_EXCEPTION_SUPPLIER = InvalidDataException::new;

    static {
        spongeDataFixer.registerFix(FixTypes.LEVEL, new SpongeLevelFixer());
        spongeDataFixer.registerFix(FixTypes.ENTITY, new EntityTrackedUser());
        spongeDataFixer.registerFix(FixTypes.PLAYER, new PlayerRespawnData());
    }

    public static DataView checkDataExists(final DataView dataView, final DataQuery query) throws InvalidDataException {
        if (!checkNotNull(dataView).contains(checkNotNull(query))) {
            throw new InvalidDataException("Missing data for query: " + query.asString('.'));
        }
        return dataView;
    }

    public static <T> T getData(final DataView dataView, final Key<? extends BaseValue<T>> key) throws InvalidDataException {
        checkDataExists(dataView, checkNotNull(key).getQuery());
        final Object object = dataView.get(key.getQuery()).get();
        return (T) object;
    }

    public static <T> T getData(final DataView dataView, final Key<?> key, Class<T> clazz) throws InvalidDataException {
        checkDataExists(dataView, checkNotNull(key).getQuery());
        final Object object = dataView.get(key.getQuery()).get();
        if (clazz.isInstance(object)) {
            return (T) object;
        }
        throw new InvalidDataException("Could not cast to the correct class type!");
    }

    public static <T> T getData(final DataView dataView, final DataQuery query, Class<T> data) throws InvalidDataException {
        checkDataExists(dataView, query);
        final Object object = dataView.get(query).get();
        if (data.isInstance(object)) {
            return (T) object;
        }
        throw new InvalidDataException("Data does not match!");
    }

    public static List<DataView> getSerializedManipulatorList(Iterable<DataManipulator<?, ?>> manipulators) {
        return getSerializedManipulatorList(manipulators, DataUtil::getRegistrationFor);
    }

    public static List<DataView> getSerializedImmutableManipulatorList(Iterable<ImmutableDataManipulator<?, ?>> manipulators) {
        return getSerializedManipulatorList(manipulators, DataUtil::getRegistrationFor);
    }

    private static <T extends DataSerializable> List<DataView> getSerializedManipulatorList(Iterable<T> manipulators, Function<T, DataRegistration> func) {
        checkNotNull(manipulators);
        final ImmutableList.Builder<DataView> builder = ImmutableList.builder();
        for (T manipulator : manipulators) {
            final DataContainer container = DataContainer.createNew();
            container.set(Queries.CONTENT_VERSION, DataVersions.Data.CURRENT_CUSTOM_DATA);
            container.set(DataQueries.DATA_ID, func.apply(manipulator).getId())
                     .set(DataQueries.INTERNAL_DATA, manipulator.toContainer());
            builder.add(container);
        }
        return builder.build();
    }

    public static SerializedDataTransaction deserializeManipulatorList(List<DataView> containers) {
        checkNotNull(containers);
        final SerializedDataTransaction.Builder builder = SerializedDataTransaction.builder();
        for (DataView view : containers) {
            final DataView updated = updateDataViewForDataManipulator(view);
            findDataId(builder, updated).ifPresent(dataId -> tryDeserializeManipulator(builder, updated, dataId));
        }
        return builder.build();
    }

    private static Optional<String> findDataId(SerializedDataTransaction.Builder builder, DataView view) {
        final Optional<String> dataId = view.getString(DataQueries.DATA_ID);
        if (!dataId.isPresent()) {
            // Show failed deserialization when this is v1 data
            @SuppressWarnings("deprecation")
            String dataClass = view.getString(DataQueries.DATA_CLASS).orElseThrow(DataUtil.dataNotFound());
            addFailedDeserialization(builder, view, dataClass, null);
        }
        return dataId;
    }

    private static void tryDeserializeManipulator(SerializedDataTransaction.Builder builder, DataView view, String dataId) {
        final DataView manipulatorView = view.getView(DataQueries.INTERNAL_DATA).orElseThrow(DataUtil.dataNotFound());
        try {
            Optional<DataManipulator<?, ?>> build = deserializeManipulator(dataId, manipulatorView);
            if (build.isPresent()) {
                builder.successfulData(build.get());
            } else {
                addFailedDeserialization(builder, view, dataId, null);
            }
        } catch (Exception e) {
            addFailedDeserialization(builder, view, dataId, e);
        }
    }

    private static <T extends DataManipulator<?, ?>> Optional<T> deserializeManipulator(String dataId, DataView data) {
        return getRegistrationFor(dataId) // Get Registration
                .map(DataRegistration::getDataManipulatorBuilder) // Find Builder
                .flatMap(b -> (Optional<T>) b.build(data)); // Build CustomData
    }

    private static void addFailedDeserialization(SerializedDataTransaction.Builder builder, DataView view, String dataId, @Nullable Throwable cause) {
        SpongeImpl.getDataConfig().getConfig().getDataRegistrationConfig().addFailedData(dataId, cause);
        SpongeImpl.getDataConfig().getConfig().getDataRegistrationConfig().purgeOrAllow(builder, dataId, view);
    }

    private static DataView updateDataViewForDataManipulator(DataView dataView) {
        final int version = dataView.getInt(Queries.CONTENT_VERSION).orElse(1);
        if (version != DataVersions.Data.CURRENT_CUSTOM_DATA) {
            final DataContentUpdater contentUpdater = SpongeDataManager.getInstance()
                .getWrappedContentUpdater(DataManipulator.class, version, DataVersions.Data.CURRENT_CUSTOM_DATA)
                .orElseThrow(() -> new IllegalArgumentException("Could not find a content updater for DataManipulator information with version: " + version));
            return contentUpdater.update(dataView);
        }
        return dataView;
    }

    public static ImmutableList<ImmutableDataManipulator<?, ?>> deserializeImmutableManipulatorList(List<DataView> containers) {
        checkNotNull(containers);
        final ImmutableList.Builder<ImmutableDataManipulator<?, ?>> builder = ImmutableList.builder();
        for (DataView view : containers) {
            view = updateDataViewForDataManipulator(view);
            final String dataId = view.getString(DataQueries.DATA_ID).orElseThrow(DataUtil.dataNotFound());
            final DataView manipulatorView = view.getView(DataQueries.INTERNAL_DATA).orElseThrow(DataUtil.dataNotFound());
            try {
                deserializeManipulator(dataId, manipulatorView).map(DataManipulator::asImmutable).ifPresent(builder::add);
            } catch (Exception e) {
                new InvalidDataException("Could not translate " + dataId + "!", e).printStackTrace();
            }
        }
        return builder.build();
    }

    public static Location<World> getLocation(DataView view, boolean castToInt) {
        final UUID worldUuid = UUID.fromString(view.getString(Queries.WORLD_ID).get());
        final double x = view.getDouble(Queries.POSITION_X).get();
        final double y = view.getDouble(Queries.POSITION_Y).get();
        final double z = view.getDouble(Queries.POSITION_Z).get();
        if (castToInt) {
            return new Location<>(SpongeImpl.getGame().getServer().getWorld(worldUuid).get(), (int) x, (int) y, (int) z);
        }
        return new Location<>(SpongeImpl.getGame().getServer().getWorld(worldUuid).get(), x, y, z);
    }

    public static Vector3i getPosition3i(DataView view) {
        return getPosition3i(view, DataQueries.SNAPSHOT_WORLD_POSITION);
    }

    public static Vector3i getPosition3i(DataView view, DataQuery query) {
        checkDataExists(view, DataQueries.SNAPSHOT_WORLD_POSITION);
        final DataView internal = view.getView(DataQueries.SNAPSHOT_WORLD_POSITION).get();
        final int x = internal.getInt(Queries.POSITION_X).get();
        final int y = internal.getInt(Queries.POSITION_Y).get();
        final int z = internal.getInt(Queries.POSITION_Z).get();
        return new Vector3i(x, y, z);
    }

    public static Vector3d getPosition3d(DataView view) {
        return getPosition3d(view, DataQueries.SNAPSHOT_WORLD_POSITION);
    }

    public static Vector3d getPosition3d(DataView view, DataQuery query) {
        checkDataExists(view, query);
        final DataView internal = view.getView(query).get();
        final double x = internal.getDouble(Queries.POSITION_X).get();
        final double y = internal.getDouble(Queries.POSITION_Y).get();
        final double z = internal.getDouble(Queries.POSITION_Z).get();
        return new Vector3d(x, y, z);
    }

    public static Supplier<InvalidDataException> dataNotFound() {
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
    public static <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> void
    registerDataProcessorAndImpl(Class<T> manipulatorClass, Class<? extends T> implClass, Class<I> immutableDataManipulator,
        Class<? extends I> implImClass, DataProcessor<T, I> processor) {
        checkState(!SpongeDataManager.areRegistrationsComplete(), "Registrations are no longer allowed!");
        checkArgument(!Modifier.isAbstract(implClass.getModifiers()), "The Implemented DataManipulator class cannot be abstract!");
        checkArgument(!Modifier.isInterface(implClass.getModifiers()), "The Implemented DataManipulator class cannot be an interface!");
        checkArgument(!Modifier.isAbstract(implImClass.getModifiers()), "The implemented ImmutableDataManipulator class cannot be an interface!");
        checkArgument(!Modifier.isInterface(implImClass.getModifiers()), "The implemented ImmutableDataManipulator class cannot be an interface!");
        checkArgument(!(processor instanceof DataProcessorDelegate), "Cannot register DataProcessorDelegates!");

        SpongeManipulatorRegistry.getInstance().register(manipulatorClass, implClass, immutableDataManipulator, implImClass, processor);
    }

    public static <E, V extends BaseValue<E>, T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> void
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
    public static <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> Optional<DataProcessor<T, I>> getProcessor(
        Class<T> mutableClass) {
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
    public static Optional<DataProcessor<?, ?>> getWildProcessor(Class<? extends DataManipulator<?, ?>> mutableClass) {
        return Optional.ofNullable(SpongeManipulatorRegistry.getInstance().getDelegate(mutableClass));
    }

    /**
     * Gets the raw typed {@link DataProcessor} with no type generics.
     *
     * @param mutableClass The class of the {@link DataManipulator}
     * @return The raw typed data processor
     */
    @SuppressWarnings({"rawtypes", "SuspiciousMethodCalls"})
    public static Optional<DataProcessor> getWildDataProcessor(Class<? extends DataManipulator> mutableClass) {
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
    public static <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> Optional<DataProcessor<T, I>>
    getImmutableProcessor(Class<I> immutableClass) {
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
    public static Optional<DataProcessor> getWildImmutableProcessor(Class<? extends ImmutableDataManipulator<?, ?>> immutableClass) {
        return Optional.ofNullable(SpongeManipulatorRegistry.getInstance().getDelegate(immutableClass));
    }


    public static <E, V extends BaseValue<E>> void registerValueProcessor(Key<V> key, ValueProcessor<E, V> valueProcessor) {
        checkState(!SpongeDataManager.areRegistrationsComplete(), "Registrations are no longer allowed!");
        checkNotNull(valueProcessor);
        checkArgument(!(valueProcessor instanceof ValueProcessorDelegate), "Cannot register ValueProcessorDelegates! READ THE DOCS!");
        checkNotNull(key);
        SpongeManipulatorRegistry.getInstance().registerValueProcessor(key, valueProcessor);
    }

    public static <E, V extends BaseValue<E>> Optional<ValueProcessor<E, V>> getValueProcessor(Key<V> key) {
        return Optional.ofNullable((ValueProcessor<E, V>) SpongeManipulatorRegistry.getInstance().getDelegate(key));
    }

    public static Optional<ValueProcessor<?, ?>> getWildValueProcessor(Key<?> key) {
        return Optional.ofNullable(SpongeManipulatorRegistry.getInstance().getDelegate(key));
    }

    public static <E> Optional<ValueProcessor<E, ? extends BaseValue<E>>> getBaseValueProcessor(Key<? extends BaseValue<E>> key) {
        return Optional.ofNullable((ValueProcessor<E, ? extends BaseValue<E>>) SpongeManipulatorRegistry.getInstance().getDelegate(key));
    }

    public static RawDataValidator getValidators(ValidationType validationType) {

        return new DelegateDataValidator(ImmutableList.of(), validationType);
    }

    public static <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> Optional<NbtDataProcessor<T, I>> getNbtProcessor(NbtDataType dataType, Class<T> clazz) {
        return Optional.ofNullable((NbtDataProcessor<T, I>) SpongeManipulatorRegistry.getInstance().getNbtDelegate(dataType, clazz));
    }

    public static <E, V extends BaseValue<E>> Optional<NbtValueProcessor<E, V>> getNbtProcessor(NbtDataType dataType, Key<V> key) {
        return Optional.ofNullable((NbtValueProcessor<E, V>) SpongeManipulatorRegistry.getInstance().getNbtProcessor(dataType, key));
    }

    @SuppressWarnings("rawtypes")
    public static Optional<NbtDataProcessor> getRawNbtProcessor(NbtDataType dataType, Class<? extends DataManipulator> aClass) {
        return Optional.ofNullable(SpongeManipulatorRegistry.getInstance().getNbtDelegate(dataType, aClass));
    }

    @SuppressWarnings("rawtypes")
    public static Optional<NbtValueProcessor> getRawNbtProcessor(NbtDataType dataType, Key<?> key) {
        return Optional.ofNullable(SpongeManipulatorRegistry.getInstance().getNbtProcessor(dataType, key));
    }

    public static Collection<NbtDataProcessor<?, ?>> getNbtProcessors(NbtDataType type) {
        return SpongeManipulatorRegistry.getInstance().getNbtProcessors(type);
    }

    public static Collection<NbtValueProcessor<?, ?>> getNbtValueProcessors(NbtDataType type) {
        return SpongeManipulatorRegistry.getInstance().getNbtValueProcessors(type);
    }

    public static DataRegistration<?, ?> getRegistrationFor(DataManipulator<?, ?> manipulator) {
        return SpongeManipulatorRegistry.getInstance().getRegistrationFor(manipulator);
    }

    public static DataRegistration<?, ?> getRegistrationFor(ImmutableDataManipulator<?, ?> immutableDataManipulator) {
        return SpongeManipulatorRegistry.getInstance().getRegistrationFor(immutableDataManipulator);
    }

    public static Optional<DataRegistration<?, ?>> getRegistrationFor(String id) {
        return (Optional<DataRegistration<?, ?>>) (Optional<?>) SpongeManipulatorRegistry.getInstance().getRegistrationFor(id);
    }

}
