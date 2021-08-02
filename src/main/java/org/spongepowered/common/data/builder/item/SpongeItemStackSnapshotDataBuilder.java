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

import com.google.common.collect.ImmutableList;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataBuilder;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.common.data.persistence.NBTTranslator;
import org.spongepowered.common.item.SpongeItemStack;
import org.spongepowered.common.item.SpongeItemStackSnapshot;
import org.spongepowered.common.util.Constants;

import java.util.List;
import java.util.Optional;

import net.minecraft.nbt.CompoundTag;

public final class SpongeItemStackSnapshotDataBuilder extends AbstractDataBuilder<ItemStackSnapshot> implements DataBuilder<ItemStackSnapshot> {

    public SpongeItemStackSnapshotDataBuilder() {
        super(ItemStackSnapshot.class, Constants.Sponge.ItemStackSnapshot.CURRENT_VERSION);
    }

    @Override
    protected Optional<ItemStackSnapshot> buildContent(DataView container) throws InvalidDataException {
        if (container.contains(Constants.ItemStack.TYPE, Constants.ItemStack.COUNT)) {
            final ItemType itemType = container.getRegistryValue(Constants.ItemStack.TYPE, RegistryTypes.ITEM_TYPE, Sponge.game().registries()).get();
            if (itemType == ItemTypes.AIR.get()) {
                return Optional.of(ItemStackSnapshot.empty());
            }
            final int count = container.getInt(Constants.ItemStack.COUNT).get();

            final @Nullable CompoundTag compound;
            if (container.contains(Constants.Sponge.UNSAFE_NBT)) {
                compound = NBTTranslator.INSTANCE.translate(container.getView(Constants.Sponge.UNSAFE_NBT).get());
                SpongeItemStack.BuilderImpl.fixEnchantmentData(itemType, compound);
            } else {
                compound = null;
            }

            final ImmutableList<DataManipulator.Immutable> manipulators;
            if (container.contains(Constants.Sponge.DATA_MANIPULATORS)) {
                final List<DataView> views = container.getViewList(Constants.Sponge.DATA_MANIPULATORS).get();
                // TODO manipulators = DataUtil.deserializeImmutableManipulatorList(container.getViewList(Constants.Sponge.DATA_MANIPULATORS).get());
                manipulators = ImmutableList.of();
            } else {
                manipulators = ImmutableList.of();
            }

            return Optional.of(new SpongeItemStackSnapshot(itemType, count, manipulators, compound));
        }
        return Optional.empty();
    }
}
