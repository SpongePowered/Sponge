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
package org.spongepowered.common.data.builder.item;

import static org.spongepowered.common.data.util.DataUtil.getData;

import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.persistence.DataBuilder;
import org.spongepowered.api.util.persistence.DataContentUpdater;
import org.spongepowered.api.util.persistence.InvalidDataException;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.item.inventory.SpongeItemStackSnapshot;
import org.spongepowered.common.util.persistence.NbtTranslator;

import java.util.Optional;

import javax.annotation.Nullable;

public class SpongeItemStackSnapshotBuilder implements DataBuilder<ItemStackSnapshot> {

    private final static int SUPPORTED_VERSION = 1;

    @Override
    public Optional<ItemStackSnapshot> build(DataView container) throws InvalidDataException {
        if (container.contains(Queries.CONTENT_VERSION)) {
            final int contentVersion = DataUtil.getData(container, Queries.CONTENT_VERSION, Integer.class);
            if (contentVersion < SUPPORTED_VERSION) {
                Optional<DataContentUpdater> updater = SpongeDataManager
                        .getInstance().getWrappedContentUpdater(ItemStackSnapshot.class, contentVersion, SUPPORTED_VERSION);
                if (!updater.isPresent()) {
                    throw new InvalidDataException("Could not get an updater for ItemEnchantment data from the version: " + contentVersion
                                                   + " to " + SUPPORTED_VERSION + ". "
                                                   + "\nPlease notify the SpongePowered developers of this issue!");
                }
                container = updater.get().update(container);
            }
        }
        if (container.contains(DataQueries.ITEM_TYPE, DataQueries.ITEM_COUNT)) {
            final String itemString = getData(container, DataQueries.ITEM_TYPE, String.class);
            final ItemType itemType = SpongeImpl.getRegistry().getType(ItemType.class, itemString).get();
            final int count = getData(container, DataQueries.ITEM_COUNT, Integer.class);
            final int damage = container.getInt(DataQueries.ITEM_DAMAGE_VALUE).orElse(0);
            final ImmutableList<ImmutableDataManipulator<?, ?>> manipulators;
            if (container.contains(DataQueries.DATA_MANIPULATORS)) {
                manipulators = DataUtil.deserializeImmutableManipulatorList(container.getViewList(DataQueries.DATA_MANIPULATORS).get());
            } else {
                manipulators = ImmutableList.of();
            }
            @Nullable final NBTTagCompound compound;
            if (container.contains(DataQueries.UNSAFE_NBT)) {
                compound = NbtTranslator.getInstance().translateData(container.getView(DataQueries.UNSAFE_NBT).get());
            } else {
                compound = null;
            }
            return Optional.of(new SpongeItemStackSnapshot(itemType, count, damage, manipulators, compound));
        }
        return Optional.empty();
    }
}
