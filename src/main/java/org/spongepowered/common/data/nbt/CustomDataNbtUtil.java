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
package org.spongepowered.common.data.nbt;

import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.persistence.SerializedDataTransaction;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.bridge.data.CustomDataHolderBridge;
import org.spongepowered.common.util.Constants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

public class CustomDataNbtUtil {

    public static DataTransactionResult apply(final NBTTagCompound compound, final DataManipulator<?, ?> manipulator) {
        if (!compound.hasKey(Constants.Forge.FORGE_DATA, Constants.NBT.TAG_COMPOUND)) {
            compound.setTag(Constants.Forge.FORGE_DATA, new NBTTagCompound());
        }
        final NBTTagCompound forgeCompound = compound.getCompoundTag(Constants.Forge.FORGE_DATA);
        if (!forgeCompound.hasKey(Constants.Sponge.SPONGE_DATA, Constants.NBT.TAG_COMPOUND)) {
            forgeCompound.setTag(Constants.Sponge.SPONGE_DATA, new NBTTagCompound());
        }
        final NBTTagCompound spongeTag = forgeCompound.getCompoundTag(Constants.Sponge.SPONGE_DATA);

        final boolean isReplacing;
        // Validate that the custom manipulator isn't already existing in the compound
        final NBTTagList list;
        if (spongeTag.hasKey(Constants.Sponge.CUSTOM_MANIPULATOR_TAG_LIST, Constants.NBT.TAG_LIST)) {
            list = spongeTag.getTagList(Constants.Sponge.CUSTOM_MANIPULATOR_TAG_LIST, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < list.tagCount(); i++) {
                final NBTTagCompound dataCompound = list.getCompoundTagAt(i);
                final String clazzName = dataCompound.getString(Constants.Sponge.CUSTOM_DATA_CLASS);
                if (manipulator.getClass().getName().equals(clazzName)) {
                    final NBTTagCompound current = dataCompound.getCompoundTag(Constants.Sponge.CUSTOM_DATA);
                    final DataContainer currentView = NbtTranslator.getInstance().translate(current);
                    final DataManipulator<?, ?> existing = deserialize(clazzName, currentView);
                    isReplacing = existing != null;
                    final DataContainer container = manipulator.toContainer();
                    final NBTTagCompound newCompound = NbtTranslator.getInstance().translateData(container);
                    dataCompound.setTag(Constants.Sponge.CUSTOM_DATA_CLASS, newCompound);
                    if (isReplacing) {
                        return DataTransactionResult.successReplaceResult(manipulator.getValues(), existing.getValues());
                    }
                    return DataTransactionResult.successReplaceResult(manipulator.getValues(), ImmutableList.of());
                }
            }
        } else {
            list = new NBTTagList();
            spongeTag.setTag(Constants.Sponge.CUSTOM_MANIPULATOR_TAG_LIST, list);
        }
        // We are now adding to the list, not replacing
        final NBTTagCompound newCompound = new NBTTagCompound();
        newCompound.setString(Constants.Sponge.CUSTOM_DATA_CLASS, manipulator.getClass().getName());
        final DataContainer dataContainer = manipulator.toContainer();
        final NBTTagCompound dataCompound = NbtTranslator.getInstance().translateData(dataContainer);
        newCompound.setTag(Constants.Sponge.CUSTOM_DATA, dataCompound);
        list.appendTag(newCompound);
        return DataTransactionResult.builder().result(DataTransactionResult.Type.SUCCESS).success(manipulator.getValues()).build();
    }

    public static DataTransactionResult apply(final DataView view, final DataManipulator<?, ?> manipulator) {
        if (!view.contains(Constants.Forge.ROOT)) {
            view.set(Constants.Forge.ROOT, DataContainer.createNew());
        }

        final DataView forgeCompound = view.getView(Constants.Forge.ROOT).orElseThrow(DataUtil.dataNotFound());
        if (!forgeCompound.contains(Constants.Sponge.SPONGE_ROOT)) {
            forgeCompound.set(Constants.Sponge.SPONGE_ROOT, DataContainer.createNew());
        }
        final DataView spongeTag = forgeCompound.getView(Constants.Sponge.SPONGE_ROOT).orElseThrow(DataUtil.dataNotFound());

        final boolean isReplacing;
        // Validate that the custom manipulator isn't already existing in the compound
        final List<DataView> customData;
        if (spongeTag.contains(Constants.Sponge.CUSTOM_MANIPULATOR_LIST)) {
            customData = spongeTag.getViewList(Constants.Sponge.CUSTOM_MANIPULATOR_LIST).orElseThrow(DataUtil.dataNotFound());
            for (final DataView dataView : customData) {
                final String dataId = dataView.getString(Constants.Sponge.DATA_ID).orElseThrow(DataUtil.dataNotFound());
                if (DataUtil.getRegistrationFor(manipulator).getId().equals(dataId)) {
                    final DataView existingData = dataView.getView(Constants.Sponge.INTERNAL_DATA).orElseThrow(DataUtil.dataNotFound());
                    final DataManipulator<?, ?> existing = deserialize(dataId, existingData);
                    isReplacing = existing != null;
                    final DataContainer container = manipulator.toContainer();
                    dataView.set(Constants.Sponge.INTERNAL_DATA, container);
                    if (isReplacing) {
                        return DataTransactionResult.successReplaceResult(manipulator.getValues(), existing.getValues());
                    }
                    return DataTransactionResult.successReplaceResult(manipulator.getValues(), ImmutableList.of());
                }

            }
        } else {
            customData = new ArrayList<>();
        }
        final DataContainer container = DataContainer.createNew();
        container.set(Constants.Sponge.DATA_ID, DataUtil.getRegistrationFor(manipulator).getId());
        container.set(Constants.Sponge.INTERNAL_DATA, manipulator.toContainer());
        customData.add(container);
        spongeTag.set(Constants.Sponge.CUSTOM_MANIPULATOR_LIST, customData);
        return DataTransactionResult.builder().result(DataTransactionResult.Type.SUCCESS).success(manipulator.getValues()).build();
    }

    public static DataTransactionResult remove(final NBTTagCompound data, final Class<? extends DataManipulator<?, ?>> containerClass) {
        if (!data.hasKey(Constants.Forge.FORGE_DATA, Constants.NBT.TAG_COMPOUND)) {
            return DataTransactionResult.successNoData();
        }
        final NBTTagCompound forgeTag = data.getCompoundTag(Constants.Forge.FORGE_DATA);
        if (!forgeTag.hasKey(Constants.Sponge.SPONGE_DATA, Constants.NBT.TAG_COMPOUND)) {
            return DataTransactionResult.successNoData();
        }
        final NBTTagCompound spongeData = forgeTag.getCompoundTag(Constants.Sponge.SPONGE_DATA);
        if (!spongeData.hasKey(Constants.Sponge.CUSTOM_MANIPULATOR_TAG_LIST, Constants.NBT.TAG_LIST)) {
            return DataTransactionResult.successNoData();
        }
        final NBTTagList dataList = spongeData.getTagList(Constants.Sponge.CUSTOM_MANIPULATOR_TAG_LIST, Constants.NBT.TAG_COMPOUND);
        if (dataList.tagCount() == 0) {
            return DataTransactionResult.successNoData();
        }
        final boolean isRemoving;
        for (int i = 0; i < dataList.tagCount(); i++) {
            final NBTTagCompound dataCompound = dataList.getCompoundTagAt(i);
            final String dataClass = dataCompound.getString(Constants.Sponge.CUSTOM_DATA_CLASS);
            if (containerClass.getName().equals(dataClass)) {
                final NBTTagCompound current = dataCompound.getCompoundTag(Constants.Sponge.CUSTOM_DATA);
                final DataContainer currentView = NbtTranslator.getInstance().translate(current);
                final DataManipulator<?, ?> existing = deserialize(dataClass, currentView);
                isRemoving = existing != null;
                dataList.removeTag(i);
                if (isRemoving) {
                    return DataTransactionResult.successRemove(existing.getValues());
                }
                return DataTransactionResult.successNoData();
            }
        }
        return DataTransactionResult.successNoData();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Nullable
    private static DataManipulator<?, ?> deserialize(final String dataClass, final DataView view) {
        try {
            final Class<?> clazz = Class.forName(dataClass);
            final Optional<DataManipulatorBuilder<?, ?>> optional = SpongeDataManager.getInstance().getBuilder((Class) clazz);
            if (optional.isPresent()) {
                final Optional<? extends DataManipulator<?, ?>> manipulatorOptional = optional.get().build(view);
                if (manipulatorOptional.isPresent()) {
                    return manipulatorOptional.get();
                }
            }
        } catch (final Exception e) {
            new InvalidDataException("Could not translate " + dataClass + "! Don't worry though, we'll try to translate the rest of the data.", e).printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static void readCustomData(final NBTTagCompound compound, final DataHolder dataHolder) {
        if (dataHolder instanceof CustomDataHolderBridge) {
            if (compound.hasKey(Constants.Sponge.CUSTOM_MANIPULATOR_TAG_LIST, Constants.NBT.TAG_LIST)) {
                final NBTTagList list = compound.getTagList(Constants.Sponge.CUSTOM_MANIPULATOR_TAG_LIST, Constants.NBT.TAG_COMPOUND);
                final ImmutableList.Builder<DataView> builder = ImmutableList.builder();
                translateTagListToView(builder, list);
                try {
                    final SerializedDataTransaction transaction = DataUtil.deserializeManipulatorList(builder.build());
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
            if (compound.hasKey(Constants.Sponge.FAILED_CUSTOM_DATA, Constants.NBT.TAG_LIST)) {
                final NBTTagList list = compound.getTagList(Constants.Sponge.FAILED_CUSTOM_DATA, Constants.NBT.TAG_COMPOUND);
                final ImmutableList.Builder<DataView> builder = ImmutableList.builder();
                translateTagListToView(builder, list);
                // We want to attempt to refresh the failed data if it does succeed in getting read.
                compound.removeTag(Constants.Sponge.FAILED_CUSTOM_DATA);
                // Re-attempt to deserialize custom data
                final SerializedDataTransaction transaction = DataUtil.deserializeManipulatorList(builder.build());
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

    private static void translateTagListToView(final ImmutableList.Builder<? super DataView> builder, final NBTTagList list) {
        if (!list.isEmpty()) {
            for (int i = 0; i < list.tagCount(); i++) {
                final NBTTagCompound internal = list.getCompoundTagAt(i);
                builder.add(NbtTranslator.getInstance().translateFrom(internal));
            }

        }
    }

    public static void writeCustomData(final NBTTagCompound compound, final DataHolder dataHolder) {
        if (dataHolder instanceof CustomDataHolderBridge) {
            final Collection<DataManipulator<?, ?>> manipulators = ((CustomDataHolderBridge) dataHolder).bridge$getCustomManipulators();
            if (!manipulators.isEmpty()) {
                final List<DataView> manipulatorViews = DataUtil.getSerializedManipulatorList(manipulators);
                final NBTTagList manipulatorTagList = new NBTTagList();
                for (final DataView dataView : manipulatorViews) {
                    manipulatorTagList.appendTag(NbtTranslator.getInstance().translateData(dataView));
                }
                compound.setTag(Constants.Sponge.CUSTOM_MANIPULATOR_TAG_LIST, manipulatorTagList);
            }
            final List<DataView> failedData = ((CustomDataHolderBridge) dataHolder).bridge$getFailedData();
            if (!failedData.isEmpty()) {
                final NBTTagList failedList = new NBTTagList();
                for (final DataView failedDatum : failedData) {
                    failedList.appendTag(NbtTranslator.getInstance().translateData(failedDatum));
                }
                compound.setTag(Constants.Sponge.FAILED_CUSTOM_DATA, failedList);
            }
        }
    }
}
