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
package org.spongepowered.common.data.utils;

import static org.spongepowered.common.data.DataTransactionBuilder.builder;
import static org.spongepowered.common.data.DataTransactionBuilder.fail;

import com.google.common.base.Optional;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulators.RepresentedItemData;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.SpongeDataUtil;
import org.spongepowered.common.data.manipulators.SpongeRepresentedItemData;
import org.spongepowered.common.item.SpongeItemStackBuilder;

public class SpongeRepresentedItemBuilder implements SpongeDataUtil<RepresentedItemData> {

    @Override
    public Optional<RepresentedItemData> fillData(DataHolder holder, RepresentedItemData manipulator, DataPriority priority) {
        return Optional.absent();
    }

    @Override
    public DataTransactionResult setData(DataHolder dataHolder, RepresentedItemData manipulator, DataPriority priority) {
        if (dataHolder instanceof EntityItem) {
            final ItemStack underlying = ((ItemStack) ((EntityItem) dataHolder).getEntityItem());
            final ItemStack clone = new SpongeItemStackBuilder().fromItemStack(underlying).build();
            final RepresentedItemData old = create();
            old.setValue(clone);

            final ItemStack newItem = manipulator.getValue();
            ((EntityItem) dataHolder).setEntityItemStack(((net.minecraft.item.ItemStack) newItem));
            return builder().replace(old).result(DataTransactionResult.Type.SUCCESS).build();
        }
        return fail(manipulator);
    }

    @Override
    public boolean remove(DataHolder dataHolder) {
        return false;
    }

    @Override
    public Optional<RepresentedItemData> build(DataView container) throws InvalidDataException {
        return Optional.absent();
    }

    @Override
    public RepresentedItemData create() {
        return new SpongeRepresentedItemData();
    }

    @Override
    public Optional<RepresentedItemData> createFrom(DataHolder dataHolder) {
        if (dataHolder instanceof EntityItem) {
            final ItemStack underlying = ((ItemStack) ((EntityItem) dataHolder).getEntityItem());
            if (underlying.getItem() == null) {
                ((net.minecraft.item.ItemStack) underlying).setItem((Item) ItemTypes.STONE);
            }
            final ItemStack clone = new SpongeItemStackBuilder().fromItemStack(underlying).build();
            final RepresentedItemData old = create();
            old.setValue(clone);
            return Optional.of(old);
        }
        return Optional.absent();
    }
}
