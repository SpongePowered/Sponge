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
package org.spongepowered.common.data.provider.item.stack;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.LockCode;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.item.ItemStackDataProvider;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

public class ItemStackLockTokenProvider extends ItemStackDataProvider<String> {

    public ItemStackLockTokenProvider() {
        super(Keys.LOCK_TOKEN);
    }

    @Override
    protected boolean supports(Item item) {
        if (!(item instanceof BlockItem)) {
            return false;
        }
        final Block block = ((BlockItem) item).getBlock();
        if (!(block instanceof ITileEntityProvider)) {
            return false;
        }
        try {
            @SuppressWarnings("ConstantConditions")
            final TileEntity tile = ((ITileEntityProvider) block).createNewTileEntity(null);
            return tile instanceof LockableTileEntity;
        } catch (NullPointerException ex) {
            return false;
        }
    }

    @Override
    protected Optional<String> getFrom(ItemStack dataHolder) {
        @Nullable final CompoundNBT tag = dataHolder.getChildTag(Constants.Item.BLOCK_ENTITY_TAG);
        if (tag != null) {
            final String lock = tag.getString(Constants.Item.LOCK);
            if (!lock.isEmpty()) {
                return Optional.of(lock);
            }
        }
        return Optional.empty();
    }

    @Override
    protected boolean set(ItemStack dataHolder, String value) {
        if (value.isEmpty()) {
            return delete(dataHolder);
        }
        final LockCode code = new LockCode(value);
        code.write(dataHolder.getOrCreateChildTag(Constants.Item.BLOCK_ENTITY_TAG));
        return true;
    }

    @Override
    protected boolean delete(ItemStack dataHolder) {
        @Nullable final CompoundNBT tag = dataHolder.getChildTag(Constants.Item.BLOCK_ENTITY_TAG);
        if (tag != null) {
            tag.remove(Constants.Item.LOCK);
        }
        return true;
    }
}
