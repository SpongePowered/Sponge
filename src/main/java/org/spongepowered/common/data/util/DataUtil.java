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
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.persistence.DataContentUpdater;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.TypeTokens;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.data.CustomDataHolderBridge;
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
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.persistence.SerializedDataTransaction;
import org.spongepowered.common.data.processor.common.AbstractSingleDataSingleTargetProcessor;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.TypeTokenHelper;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nullable;

@SuppressWarnings({"unchecked", "UnstableApiUsage"})
public final class DataUtil {

    public static final DataFixer spongeDataFixer = new DataFixer(Constants.Sponge.SPONGE_DATA_VERSION);
    private static final Supplier<InvalidDataException> INVALID_DATA_EXCEPTION_SUPPLIER = InvalidDataException::new;

    static {
        spongeDataFixer.func_188256_a(FixTypes.LEVEL, new SpongeLevelFixer());
        spongeDataFixer.func_188256_a(FixTypes.ENTITY, new EntityTrackedUser());
        spongeDataFixer.func_188256_a(FixTypes.PLAYER, new PlayerRespawnData());
    }

    public static DataView checkDataExists(final DataView dataView, final DataQuery query) throws InvalidDataException {
        if (!checkNotNull(dataView).contains(checkNotNull(query))) {
            throw new InvalidDataException("Missing data for query: " + query.asString('.'));
        }
        return dataView;
    }

    @SuppressWarnings("rawtypes")
    public static <T> T getData(final DataView dataView, final Key<? extends BaseValue<T>> key) throws InvalidDataException {
        checkDataExists(dataView, checkNotNull(key).getQuery());
        final Object object;
        final TypeToken<?> elementToken = key.getElementToken();
        // Order matters here
        // We always check DataSerializeable first, since this should override
        // any other handling (e.g. for CatalogTypes)
        if (elementToken.isSubtypeOf(TypeToken.of(DataSerializable.class))) {
            object = dataView.getSerializable(key.getQuery(), (Class<DataSerializable>) elementToken.getRawType())
                .orElseThrow(() -> new InvalidDataException("Missing value for key: " + key.getId()));
        } else if (elementToken.isSubtypeOf(TypeToken.of(CatalogType.class))) {
            object = dataView.getCatalogType(key.getQuery(), (Class<CatalogType>) elementToken.getRawType())
                .orElseThrow(() -> new InvalidDataException("Missing value for key: " + key.getId()));
        } else if (elementToken.isSubtypeOf(TypeToken.of(Text.class))) {
            final String input = dataView.getString(key.getQuery())
                    .orElseThrow(() -> new InvalidDataException("Missing value for key: " + key.getId()));
            object = TextSerializers.PLAIN.deserialize(input);
        } else if (elementToken.isSubtypeOf(TypeToken.of(List.class))) {
            final Optional<?> opt;
            if (elementToken.isSubtypeOf(TypeTokens.LIST_DATA_SERIALIZEABLE_TOKEN)) {
                final Class<?> listElement = TypeTokenHelper.getGenericParam(elementToken, 0);
                opt = dataView.getSerializableList(key.getQuery(), (Class) listElement);
            } else {
                opt = dataView.getList(key.getQuery());
            }
            object = opt.orElseThrow(() -> new InvalidDataException("Missing value for key: " + key.getId()));
        } else if (elementToken.isSubtypeOf(TypeToken.of(Set.class))) {
            final List<?> objects = dataView.getList(key.getQuery()).orElse(Collections.emptyList());
            object = new HashSet<Object>(objects);
        } else if (elementToken.isSubtypeOf(TypeToken.of(Map.class))) {
            object = dataView.getMap(key.getQuery()).orElseThrow(() -> new InvalidDataException("Missing value for key: " + key.getId()));
        } else if (elementToken.isSubtypeOf(TypeToken.of(Enum.class))) {
            object = Enum.valueOf((Class<Enum>) elementToken.getRawType(), dataView.getString(key.getQuery())
                .orElseThrow(() -> new InvalidDataException("Missing value for key: " + key.getId())));
        } else {
            final Optional<? extends DataTranslator<?>> translator = SpongeDataManager.getInstance().getTranslator(elementToken.getRawType());
            if (translator.isPresent()) {
                object = translator.map(trans -> trans.translate(dataView.getView(key.getQuery()).orElseThrow(() -> new InvalidDataException("Missing value for key: " + key.getId()))))
                    .orElseThrow(() -> new InvalidDataException("Could not translate translateable: " + key.getId()));
            } else {
                object = dataView.get(key.getQuery())
                    .orElseThrow(() -> new InvalidDataException("Could not translate translateable: " + key.getId()));
            }
        }

        return (T) object;
    }

    public static <T> T getData(final DataView dataView, final Key<?> key, final Class<T> clazz) throws InvalidDataException {
        checkDataExists(dataView, checkNotNull(key).getQuery());
        final Object object = dataView.get(key.getQuery()).orElseThrow(dataNotFound());
        if (clazz.isInstance(object)) {
            return (T) object;
        }
        throw new InvalidDataException("Could not cast to the correct class type!");
    }

    public static <T> T getData(final DataView dataView, final DataQuery query, final Class<T> data) throws InvalidDataException {
        checkDataExists(dataView, query);
        final Object object = dataView.get(query).orElseThrow(dataNotFound());
        if (data.isInstance(object)) {
            return (T) object;
        }
        throw new InvalidDataException("Data does not match!");
    }

    public static List<DataView> getSerializedManipulatorList(final Iterable<? extends DataManipulator<?, ?>> manipulators) {
        return getSerializedManipulatorList(manipulators, DataUtil::getRegistrationFor);
    }

    public static List<DataView> getSerializedImmutableManipulatorList(final Iterable<? extends ImmutableDataManipulator<?, ?>> manipulators) {
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
            final Optional<DataManipulator<?, ?>> build = deserializeManipulator(dataId, manipulatorView);
            if (build.isPresent()) {
                builder.successfulData(build.get());
            } else {
                addFailedDeserialization(builder, view, dataId, null);
            }
        } catch (final Exception e) {
            addFailedDeserialization(builder, view, dataId, e);
        }
    }

    private static <T extends DataManipulator<?, ?>> Optional<T> deserializeManipulator(final String dataId, final DataView data) {
        return getRegistrationFor(dataId) // Get Registration
                .map(DataRegistration::getDataManipulatorBuilder) // Find Builder
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
                .getWrappedContentUpdater(DataManipulator.class, version, Constants.Sponge.CURRENT_CUSTOM_DATA)
                .orElseThrow(() -> new IllegalArgumentException("Could not find a content updater for DataManipulator information with version: " + version));
            return contentUpdater.update(dataView);
        }
        return dataView;
    }

    public static ImmutableList<ImmutableDataManipulator<?, ?>> deserializeImmutableManipulatorList(final List<? extends DataView> containers) {
        checkNotNull(containers);
        final ImmutableList.Builder<ImmutableDataManipulator<?, ?>> builder = ImmutableList.builder();
        for (DataView view : containers) {
            view = updateDataViewForDataManipulator(view);
            final String dataId = view.getString(Constants.Sponge.DATA_ID).orElseThrow(DataUtil.dataNotFound());
            final DataView manipulatorView = view.getView(Constants.Sponge.INTERNAL_DATA).orElseThrow(DataUtil.dataNotFound());
            try {
                deserializeManipulator(dataId, manipulatorView).map(DataManipulator::asImmutable).ifPresent(builder::add);
            } catch (final Exception e) {
                new InvalidDataException("Could not translate " + dataId + "!", e).printStackTrace();
            }
        }
        return builder.build();
    }

    public static Location<World> getLocation(final DataView view, final boolean castToInt) {
        final UUID worldUuid = UUID.fromString(view.getString(Queries.WORLD_ID).orElseThrow(dataNotFound()));
        final double x = view.getDouble(Queries.POSITION_X).orElseThrow(dataNotFound());
        final double y = view.getDouble(Queries.POSITION_Y).orElseThrow(dataNotFound());
        final double z = view.getDouble(Queries.POSITION_Z).orElseThrow(dataNotFound());
        if (castToInt) {
            return new Location<>(SpongeImpl.getGame().getServer().getWorld(worldUuid).orElseThrow(dataNotFound()), (int) x, (int) y, (int) z);
        }
        return new Location<>(SpongeImpl.getGame().getServer().getWorld(worldUuid).orElseThrow(dataNotFound()), x, y, z);
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
    public static <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> void
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

    public static <E, V extends BaseValue<E>, T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> void
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
    public static <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> Optional<DataProcessor<T, I>> getProcessor(
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
    public static Optional<DataProcessor<?, ?>> getWildProcessor(final Class<? extends DataManipulator<?, ?>> mutableClass) {
        return Optional.ofNullable(SpongeManipulatorRegistry.getInstance().getDelegate(mutableClass));
    }

    /**
     * Gets the raw typed {@link DataProcessor} with no type generics.
     *
     * @param mutableClass The class of the {@link DataManipulator}
     * @return The raw typed data processor
     */
    @SuppressWarnings("rawtypes")
    public static Optional<DataProcessor> getWildDataProcessor(final Class<? extends DataManipulator> mutableClass) {
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
    public static Optional<DataProcessor> getWildImmutableProcessor(final Class<? extends ImmutableDataManipulator<?, ?>> immutableClass) {
        return Optional.ofNullable(SpongeManipulatorRegistry.getInstance().getDelegate(immutableClass));
    }


    public static <E, V extends BaseValue<E>> void registerValueProcessor(final Key<V> key, final ValueProcessor<E, V> valueProcessor) {
        checkState(!SpongeDataManager.areRegistrationsComplete(), "Registrations are no longer allowed!");
        checkNotNull(valueProcessor);
        checkArgument(!(valueProcessor instanceof ValueProcessorDelegate), "Cannot register ValueProcessorDelegates! READ THE DOCS!");
        checkNotNull(key);
        SpongeManipulatorRegistry.getInstance().registerValueProcessor(key, valueProcessor);
    }

    public static <E, V extends BaseValue<E>> Optional<ValueProcessor<E, V>> getValueProcessor(final Key<V> key) {
        return Optional.ofNullable((ValueProcessor<E, V>) SpongeManipulatorRegistry.getInstance().getDelegate(key));
    }

    public static Optional<ValueProcessor<?, ?>> getWildValueProcessor(final Key<?> key) {
        return Optional.ofNullable(SpongeManipulatorRegistry.getInstance().getDelegate(key));
    }

    public static <E> Optional<ValueProcessor<E, ? extends BaseValue<E>>> getBaseValueProcessor(final Key<? extends BaseValue<E>> key) {
        return Optional.ofNullable((ValueProcessor<E, ? extends BaseValue<E>>) SpongeManipulatorRegistry.getInstance().getDelegate(key));
    }

    public static RawDataValidator getValidators(final ValidationType validationType) {

        return new DelegateDataValidator(ImmutableList.of(), validationType);
    }

    public static <E, V extends BaseValue<E>> Optional<NbtValueProcessor<E, V>> getNbtProcessor(final NbtDataType dataType, final Key<V> key) {
        return Optional.ofNullable((NbtValueProcessor<E, V>) SpongeManipulatorRegistry.getInstance().getNbtProcessor(dataType, key));
    }

    @SuppressWarnings("rawtypes")
    public static Optional<NbtDataProcessor> getRawNbtProcessor(final NbtDataType dataType, final Class<? extends DataManipulator> aClass) {
        return Optional.ofNullable(SpongeManipulatorRegistry.getInstance().getNbtDelegate(dataType, aClass));
    }

    @SuppressWarnings("rawtypes")
    public static Optional<NbtValueProcessor> getRawNbtProcessor(final NbtDataType dataType, final Key<?> key) {
        return Optional.ofNullable(SpongeManipulatorRegistry.getInstance().getNbtProcessor(dataType, key));
    }

    public static Collection<NbtDataProcessor<?, ?>> getNbtProcessors(final NbtDataType type) {
        return SpongeManipulatorRegistry.getInstance().getNbtProcessors(type);
    }

    public static Collection<NbtValueProcessor<?, ?>> getNbtValueProcessors(final NbtDataType type) {
        return SpongeManipulatorRegistry.getInstance().getNbtValueProcessors(type);
    }

    public static DataRegistration<?, ?> getRegistrationFor(final DataManipulator<?, ?> manipulator) {
        return SpongeManipulatorRegistry.getInstance().getRegistrationFor(manipulator);
    }

    private static DataRegistration<?, ?> getRegistrationFor(final ImmutableDataManipulator<?, ?> immutableDataManipulator) {
        return SpongeManipulatorRegistry.getInstance().getRegistrationFor(immutableDataManipulator);
    }

    private static Optional<DataRegistration<?, ?>> getRegistrationFor(final String id) {
        return (Optional<DataRegistration<?, ?>>) (Optional<?>) SpongeManipulatorRegistry.getInstance().getRegistrationFor(id);
    }

    public static DataTransactionResult apply(final CompoundNBT compound, final DataManipulator<?, ?> manipulator) {
        if (!compound.func_150297_b(Constants.Forge.FORGE_DATA, Constants.NBT.TAG_COMPOUND)) {
            compound.func_74782_a(Constants.Forge.FORGE_DATA, new CompoundNBT());
        }
        final CompoundNBT forgeCompound = compound.func_74775_l(Constants.Forge.FORGE_DATA);
        if (!forgeCompound.func_150297_b(Constants.Sponge.SPONGE_DATA, Constants.NBT.TAG_COMPOUND)) {
            forgeCompound.func_74782_a(Constants.Sponge.SPONGE_DATA, new CompoundNBT());
        }
        final CompoundNBT spongeTag = forgeCompound.func_74775_l(Constants.Sponge.SPONGE_DATA);

        // Validate that the custom manipulator isn't already existing in the compound
        final ListNBT list;
        final DataRegistration<?, ?> registration = getRegistrationFor(manipulator);

        if (spongeTag.func_150297_b(Constants.Sponge.CUSTOM_MANIPULATOR_TAG_LIST, Constants.NBT.TAG_LIST)) {
            list = spongeTag.func_150295_c(Constants.Sponge.CUSTOM_MANIPULATOR_TAG_LIST, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < list.func_74745_c(); i++) {
                final CompoundNBT dataCompound = list.func_150305_b(i);
                final String dataId = dataCompound.func_74779_i(Constants.Sponge.MANIPULATOR_ID);
                if (dataId.equalsIgnoreCase(registration.getId())) {
                    final CompoundNBT current = dataCompound.func_74775_l(Constants.Sponge.CUSTOM_DATA);
                    final DataContainer currentView = NbtTranslator.getInstance().translate(current);
                    final Optional<DataManipulator<?, ?>> existingManipulator = deserializeManipulator(dataId, currentView);
                    final DataContainer replacement = manipulator.toContainer();
                    final CompoundNBT replacementCompound = NbtTranslator.getInstance().translateData(replacement);
                    dataCompound.func_74782_a(Constants.Sponge.CUSTOM_DATA, replacementCompound);
                    return DataTransactionResult.successReplaceResult(manipulator.getValues(), existingManipulator.map(DataManipulator::getValues)
                        .orElseGet(ImmutableSet::of));
                }
            }
        } else {
            list = new ListNBT();
            // We are now adding to the list, not replacing
            final CompoundNBT newCompound = new CompoundNBT();
            newCompound.func_74778_a(Constants.Sponge.MANIPULATOR_ID, registration.getId());
            final DataContainer dataContainer = manipulator.toContainer();
            final CompoundNBT dataCompound = NbtTranslator.getInstance().translateData(dataContainer);
            newCompound.func_74782_a(Constants.Sponge.CUSTOM_DATA, dataCompound);
            list.func_74742_a(newCompound);
            spongeTag.func_74782_a(Constants.Sponge.CUSTOM_MANIPULATOR_TAG_LIST, list);
        }

        return DataTransactionResult.builder().result(DataTransactionResult.Type.SUCCESS).success(manipulator.getValues()).build();
    }

    @SuppressWarnings("rawtypes")
    public static DataTransactionResult remove(final CompoundNBT data, final Class<? extends DataManipulator<?, ?>> containerClass) {
        if (!data.func_150297_b(Constants.Forge.FORGE_DATA, Constants.NBT.TAG_COMPOUND)) {
            return DataTransactionResult.successNoData();
        }
        final CompoundNBT forgeTag = data.func_74775_l(Constants.Forge.FORGE_DATA);
        if (!forgeTag.func_150297_b(Constants.Sponge.SPONGE_DATA, Constants.NBT.TAG_COMPOUND)) {
            return DataTransactionResult.successNoData();
        }
        final CompoundNBT spongeData = forgeTag.func_74775_l(Constants.Sponge.SPONGE_DATA);
        if (!spongeData.func_150297_b(Constants.Sponge.CUSTOM_MANIPULATOR_TAG_LIST, Constants.NBT.TAG_LIST)) {
            return DataTransactionResult.successNoData();
        }
        final ListNBT dataList = spongeData.func_150295_c(Constants.Sponge.CUSTOM_MANIPULATOR_TAG_LIST, Constants.NBT.TAG_COMPOUND);
        if (dataList.func_74745_c() == 0) {
            return DataTransactionResult.successNoData();
        }
        final DataRegistration<?, ?> registration = SpongeManipulatorRegistry.getInstance().getRegistrationFor((Class) containerClass);
        for (int i = 0; i < dataList.func_74745_c(); i++) {
            final CompoundNBT dataCompound = dataList.func_150305_b(i);
            final String dataId = dataCompound.func_74779_i(Constants.Sponge.MANIPULATOR_ID);
            if (registration.getId().equalsIgnoreCase(dataId)) {
                final CompoundNBT current = dataCompound.func_74775_l(Constants.Sponge.CUSTOM_DATA);
                final DataContainer currentView = NbtTranslator.getInstance().translate(current);
                final Optional<DataManipulator<?, ?>> existing = deserializeManipulator(dataId, currentView);
                dataList.func_74744_a(i);
                return existing.map(DataManipulator::getValues)
                    .map(DataTransactionResult::successRemove)
                    .orElseGet(DataTransactionResult::successNoData);
            }
        }
        return DataTransactionResult.successNoData();
    }

    @SuppressWarnings("unchecked")
    public static void readCustomData(final CompoundNBT compound, final DataHolder dataHolder) {
        if (dataHolder instanceof CustomDataHolderBridge) {
            if (compound.func_150297_b(Constants.Sponge.CUSTOM_MANIPULATOR_TAG_LIST, Constants.NBT.TAG_LIST)) {
                final ListNBT list = compound.func_150295_c(Constants.Sponge.CUSTOM_MANIPULATOR_TAG_LIST, Constants.NBT.TAG_COMPOUND);
                final ImmutableList.Builder<DataView> builder = ImmutableList.builder();
                translateTagListToView(builder, list);
                try {
                    final SerializedDataTransaction transaction = deserializeManipulatorList(builder.build());
                    final List<DataManipulator<?, ?>> manipulators = transaction.deserializedManipulators;
                    for (final DataManipulator<?, ?> manipulator : manipulators) {
                        dataHolder.offer(manipulator);
                    }
                    if (!transaction.failedData.isEmpty()) {
                        ((CustomDataHolderBridge) dataHolder).bridge$addFailedData(transaction.failedData);
                    }
                } catch (final InvalidDataException e) {
                    SpongeImpl.getLogger().error("Could not translate custom plugin data! ", e);
                }
            }
            if (compound.func_150297_b(Constants.Sponge.FAILED_CUSTOM_DATA, Constants.NBT.TAG_LIST)) {
                final ListNBT list = compound.func_150295_c(Constants.Sponge.FAILED_CUSTOM_DATA, Constants.NBT.TAG_COMPOUND);
                final ImmutableList.Builder<DataView> builder = ImmutableList.builder();
                translateTagListToView(builder, list);
                // We want to attempt to refresh the failed data if it does succeed in getting read.
                compound.func_82580_o(Constants.Sponge.FAILED_CUSTOM_DATA);
                // Re-attempt to deserialize custom data
                final SerializedDataTransaction transaction = deserializeManipulatorList(builder.build());
                final List<DataManipulator<?, ?>> manipulators = transaction.deserializedManipulators;
                final List<Class<? extends DataManipulator<?, ?>>> classesLoaded = new ArrayList<>();
                for (final DataManipulator<?, ?> manipulator : manipulators) {
                    if (!classesLoaded.contains(manipulator.getClass())) {
                        classesLoaded.add((Class<? extends DataManipulator<?, ?>>) manipulator.getClass());
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
        if (!list.func_82582_d()) {
            for (int i = 0; i < list.func_74745_c(); i++) {
                final CompoundNBT internal = list.func_150305_b(i);
                builder.add(NbtTranslator.getInstance().translateFrom(internal));
            }

        }
    }

    public static void writeCustomData(final CompoundNBT compound, final DataHolder dataHolder) {
        if (dataHolder instanceof CustomDataHolderBridge) {
            final Collection<DataManipulator<?, ?>> manipulators = ((CustomDataHolderBridge) dataHolder).bridge$getCustomManipulators();
            if (!manipulators.isEmpty()) {
                final List<DataView> manipulatorViews = getSerializedManipulatorList(manipulators);
                final ListNBT manipulatorTagList = new ListNBT();
                for (final DataView dataView : manipulatorViews) {
                    manipulatorTagList.func_74742_a(NbtTranslator.getInstance().translateData(dataView));
                }
                compound.func_74782_a(Constants.Sponge.CUSTOM_MANIPULATOR_TAG_LIST, manipulatorTagList);
            }
            final List<DataView> failedData = ((CustomDataHolderBridge) dataHolder).bridge$getFailedData();
            if (!failedData.isEmpty()) {
                final ListNBT failedList = new ListNBT();
                for (final DataView failedDatum : failedData) {
                    failedList.func_74742_a(NbtTranslator.getInstance().translateData(failedDatum));
                }
                compound.func_74782_a(Constants.Sponge.FAILED_CUSTOM_DATA, failedList);
            }
        }
    }

}
