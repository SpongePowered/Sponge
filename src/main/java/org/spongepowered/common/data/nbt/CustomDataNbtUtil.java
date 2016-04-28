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
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.util.NbtDataUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

public class CustomDataNbtUtil {

    @SuppressWarnings("unchecked")
    public static DataTransactionResult apply(NBTTagCompound compound, DataManipulator<?, ?> manipulator) {
        if (!compound.hasKey(NbtDataUtil.FORGE_DATA_TAG, NbtDataUtil.TAG_COMPOUND)) {
            compound.setTag(NbtDataUtil.FORGE_DATA_TAG, new NBTTagCompound());
        }
        final NBTTagCompound forgeCompound = compound.getCompoundTag(NbtDataUtil.FORGE_DATA_TAG);
        if (!forgeCompound.hasKey(NbtDataUtil.SPONGE_DATA, NbtDataUtil.TAG_COMPOUND)) {
            forgeCompound.setTag(NbtDataUtil.SPONGE_DATA, new NBTTagCompound());
        }
        final NBTTagCompound spongeTag = forgeCompound.getCompoundTag(NbtDataUtil.SPONGE_DATA);

        boolean isReplacing;
        // Validate that the custom manipulator isn't already existing in the compound
        if (spongeTag.hasKey(NbtDataUtil.CUSTOM_MANIPULATOR_TAG_LIST, NbtDataUtil.TAG_LIST)) {
            final NBTTagList list = spongeTag.getTagList(NbtDataUtil.CUSTOM_MANIPULATOR_TAG_LIST, NbtDataUtil.TAG_COMPOUND);
            for (int i = 0; i < list.tagCount(); i++) {
                final NBTTagCompound dataCompound = list.getCompoundTagAt(i);
                final String clazzName = dataCompound.getString(NbtDataUtil.CUSTOM_DATA_CLASS);
                if (manipulator.getClass().getName().equals(clazzName)) {
                    final NBTTagCompound current = dataCompound.getCompoundTag(NbtDataUtil.CUSTOM_DATA);
                    final DataContainer currentView = NbtTranslator.getInstance().translate(current);
                    DataManipulator<?, ?> existing = deserialize(clazzName, currentView);
                    isReplacing = existing != null;
                    final DataContainer container = manipulator.toContainer();
                    final NBTTagCompound newCompound = NbtTranslator.getInstance().translateData(container);
                    dataCompound.setTag(NbtDataUtil.CUSTOM_DATA_CLASS, newCompound);
                    if (isReplacing) {
                        return DataTransactionResult.successReplaceResult(manipulator.getValues(), existing.getValues());
                    } else {
                        return DataTransactionResult.successReplaceResult(manipulator.getValues(), ImmutableList.of());
                    }
                }
            }
            // We are now adding to the list, not replacing
            final NBTTagCompound newCompound = new NBTTagCompound();
            newCompound.setString(NbtDataUtil.CUSTOM_DATA_CLASS, manipulator.getClass().getName());
            final DataContainer dataContainer = manipulator.toContainer();
            final NBTTagCompound dataCompound = NbtTranslator.getInstance().translateData(dataContainer);
            newCompound.setTag(NbtDataUtil.CUSTOM_DATA, dataCompound);
            list.appendTag(newCompound);
            return DataTransactionResult.builder().result(DataTransactionResult.Type.SUCCESS).success(manipulator.getValues()).build();
        } else {
            final NBTTagList list = new NBTTagList();
            // We are now adding to the list, not replacing
            final NBTTagCompound newCompound = new NBTTagCompound();
            newCompound.setString(NbtDataUtil.CUSTOM_DATA_CLASS, manipulator.getClass().getName());
            final DataContainer dataContainer = manipulator.toContainer();
            final NBTTagCompound dataCompound = NbtTranslator.getInstance().translateData(dataContainer);
            newCompound.setTag(NbtDataUtil.CUSTOM_DATA, dataCompound);
            list.appendTag(newCompound);
            spongeTag.setTag(NbtDataUtil.CUSTOM_MANIPULATOR_TAG_LIST, list);
            return DataTransactionResult.builder().result(DataTransactionResult.Type.SUCCESS).success(manipulator.getValues()).build();
        }
    }

    @SuppressWarnings("unchecked")
    public static DataTransactionResult apply(DataView view, DataManipulator<?, ?> manipulator) {
        if (!view.contains(DataQueries.Compatibility.Forge.ROOT)) {
            view.set(DataQueries.Compatibility.Forge.ROOT, new MemoryDataContainer());
        }

        final DataView forgeCompound = view.getView(DataQueries.Compatibility.Forge.ROOT).orElseThrow(DataUtil.dataNotFound());
        if (!forgeCompound.contains(DataQueries.General.SPONGE_ROOT)) {
            forgeCompound.set(DataQueries.General.SPONGE_ROOT, new MemoryDataContainer());
        }
        final DataView spongeTag = forgeCompound.getView(DataQueries.General.SPONGE_ROOT).orElseThrow(DataUtil.dataNotFound());

        boolean isReplacing;
        // Validate that the custom manipulator isn't already existing in the compound
        if (spongeTag.contains(DataQueries.General.CUSTOM_MANIPULATOR_LIST)) {
            final List<DataView> customData = spongeTag.getViewList(DataQueries.General.CUSTOM_MANIPULATOR_LIST).orElseThrow(DataUtil.dataNotFound());
            for (DataView dataView : customData) {
                final String dataClass = dataView.getString(DataQueries.DATA_CLASS).orElseThrow(DataUtil.dataNotFound());
                if (manipulator.getClass().getName().equals(dataClass)) {
                    final DataView existingData = dataView.getView(DataQueries.INTERNAL_DATA).orElseThrow(DataUtil.dataNotFound());
                    DataManipulator<?, ?> existing = deserialize(dataClass, existingData);
                    isReplacing = existing != null;
                    final DataContainer container = manipulator.toContainer();
                    dataView.set(DataQueries.INTERNAL_DATA, container);
                    if (isReplacing) {
                        return DataTransactionResult.successReplaceResult(manipulator.getValues(), existing.getValues());
                    } else {
                        return DataTransactionResult.successReplaceResult(manipulator.getValues(), ImmutableList.of());
                    }
                }

            }
            // We are now adding to the list, not replacing
            final MemoryDataContainer container = new MemoryDataContainer();
            container.set(DataQueries.DATA_CLASS, manipulator.getClass().getName());
            container.set(DataQueries.INTERNAL_DATA, manipulator.toContainer());
            customData.add(container);
            spongeTag.set(DataQueries.General.CUSTOM_MANIPULATOR_LIST, customData);
            return DataTransactionResult.builder().result(DataTransactionResult.Type.SUCCESS).success(manipulator.getValues()).build();
        } else {
            final List<DataView> dataViews = new ArrayList<>();
            final MemoryDataContainer container = new MemoryDataContainer();
            container.set(DataQueries.DATA_CLASS, manipulator.getClass().getName())
                    .set(DataQueries.INTERNAL_DATA, manipulator.toContainer());
            dataViews.add(container);
            spongeTag.set(DataQueries.General.CUSTOM_MANIPULATOR_LIST, dataViews);
            return DataTransactionResult.builder().result(DataTransactionResult.Type.SUCCESS).success(manipulator.getValues()).build();
        }
    }

    public static DataTransactionResult remove(NBTTagCompound data, Class<? extends DataManipulator<?, ?>> containerClass) {
        if (!data.hasKey(NbtDataUtil.FORGE_DATA_TAG, NbtDataUtil.TAG_COMPOUND)) {
            return DataTransactionResult.successNoData();
        }
        final NBTTagCompound forgeTag = data.getCompoundTag(NbtDataUtil.FORGE_DATA_TAG);
        if (!forgeTag.hasKey(NbtDataUtil.SPONGE_DATA, NbtDataUtil.TAG_COMPOUND)) {
            return DataTransactionResult.successNoData();
        }
        final NBTTagCompound spongeData = forgeTag.getCompoundTag(NbtDataUtil.SPONGE_DATA);
        if (!spongeData.hasKey(NbtDataUtil.CUSTOM_MANIPULATOR_TAG_LIST, NbtDataUtil.TAG_LIST)) {
            return DataTransactionResult.successNoData();
        }
        final NBTTagList dataList = spongeData.getTagList(NbtDataUtil.CUSTOM_MANIPULATOR_TAG_LIST, NbtDataUtil.TAG_COMPOUND);
        if (dataList.tagCount() == 0) {
            return DataTransactionResult.successNoData();
        }
        boolean isRemoving;
        for (int i = 0; i < dataList.tagCount(); i++) {
            final NBTTagCompound dataCompound = dataList.getCompoundTagAt(i);
            final String dataClass = dataCompound.getString(NbtDataUtil.CUSTOM_DATA_CLASS);
            if (containerClass.getName().equals(dataClass)) {
                final NBTTagCompound current = dataCompound.getCompoundTag(NbtDataUtil.CUSTOM_DATA);
                final DataContainer currentView = NbtTranslator.getInstance().translate(current);
                DataManipulator<?, ?> existing = deserialize(dataClass, currentView);
                isRemoving = existing != null;
                if (isRemoving) {
                    dataList.removeTag(i);
                    return DataTransactionResult.successRemove(existing.getValues());
                } else {
                    dataList.removeTag(i);
                    return DataTransactionResult.successNoData();
                }
            }
        }
        return DataTransactionResult.successNoData();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private static DataManipulator<?, ?> deserialize(String dataClass, DataView view) {
        try {
            final Class<?> clazz = Class.forName(dataClass);
            final Optional<DataManipulatorBuilder<?, ?>> optional = SpongeDataManager.getInstance().getBuilder((Class) clazz);
            if (optional.isPresent()) {
                final Optional<? extends DataManipulator<?, ?>> manipulatorOptional = optional.get().build(view);
                if (manipulatorOptional.isPresent()) {
                    return manipulatorOptional.get();
                }
            }
        } catch (Exception e) {
            new InvalidDataException("Could not translate " + dataClass + "! Don't worry though, we'll try to translate the rest of the data.", e).printStackTrace();
        }
        return null;
    }
}
