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
import net.minecraft.item.BannerItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.LockCode;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.bridge.block.DyeColorBlockBridge;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.Constants;

public final class BlockItemStackData {

    private BlockItemStackData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(ItemStack.class)
                    .create(Keys.DYE_COLOR)
                        .get(h -> {
                            final Block block = ((BlockItem) h.getItem()).getBlock();
                            return ((DyeColorBlockBridge) block).bridge$getDyeColor().orElse(null);
                        })
                        .supports(h -> h.getItem() instanceof BlockItem && !(h.getItem() instanceof BannerItem))
                    .create(Keys.LOCK_TOKEN)
                        .get(h -> {
                            final CompoundNBT tag = h.getChildTag(Constants.Item.BLOCK_ENTITY_TAG);
                            if (tag != null) {
                                final String lock = tag.getString(Constants.Item.LOCK);
                                if (!lock.isEmpty()) {
                                    return lock;
                                }
                            }
                            return null;
                        })
                        .set((h, v) -> {
                            if (v.isEmpty()) {
                                deleteLockToken(h);
                                return;
                            }
                            new LockCode(v).write(h.getOrCreateChildTag(Constants.Item.BLOCK_ENTITY_TAG));
                        })
                        .delete(BlockItemStackData::deleteLockToken)
                        .supports(h -> {
                            if (!(h.getItem() instanceof BlockItem)) {
                                return false;
                            }
                            final Block block = ((BlockItem) h.getItem()).getBlock();
                            if (!(block instanceof ITileEntityProvider)) {
                                return false;
                            }
                            try {
                                final TileEntity tile = ((ITileEntityProvider) block).createNewTileEntity(null);
                                return tile instanceof LockableTileEntity;
                            } catch (final NullPointerException ex) {
                                return false;
                            }
                        });
    }
    // @formatter:on

    private static void deleteLockToken(final ItemStack stack) {
        final CompoundNBT tag = stack.getChildTag(Constants.Item.BLOCK_ENTITY_TAG);
        if (tag != null) {
            tag.remove(Constants.Item.LOCK);
        }
    }
}
