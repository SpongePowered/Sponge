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
package org.spongepowered.common.data.builder.block.tileentity;

import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.StringTextComponent;
import org.spongepowered.api.block.entity.carrier.CarrierBlockEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.Constants;

import java.util.List;
import java.util.Optional;

public abstract class SpongeLockableBuilder<T extends CarrierBlockEntity> extends AbstractTileBuilder<T> {

    protected SpongeLockableBuilder(Class<T> clazz, int version) {
        super(clazz, version);
    }

    @Override
    protected Optional<T> buildContent(DataView container) throws InvalidDataException {
        return super.buildContent(container).flatMap(lockable -> {
            final Optional<List<DataView>> contents = container.getViewList(Constants.TileEntity.ITEM_CONTENTS);
            if (!contents.isPresent()) {
                return Optional.empty();
            }

            for (DataView content: contents.get()) {
                net.minecraft.item.ItemStack stack = ItemStackUtil.toNative(content.getSerializable(Constants.TileEntity.SLOT_ITEM, ItemStack.class).get());
                ((IInventory) lockable).setInventorySlotContents(content.getInt(Constants.TileEntity.SLOT).get(), stack);
            }

            container.getString(Constants.TileEntity.LOCK_CODE).ifPresent(token -> lockable.offer(Keys.LOCK_TOKEN, token));
            container.getString(Constants.TileEntity.CUSTOM_NAME).ifPresent(name -> ((LockableTileEntity) lockable).setCustomName(new StringTextComponent(name)));

            return Optional.of(lockable);
        });
    }
}
