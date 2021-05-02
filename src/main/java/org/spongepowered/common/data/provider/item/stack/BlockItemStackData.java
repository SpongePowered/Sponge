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

import org.spongepowered.api.data.Keys;
import org.spongepowered.common.bridge.block.DyeColorBlockBridge;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.Constants;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.LockCode;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

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
                            final CompoundTag tag = h.getTagElement(Constants.Item.BLOCK_ENTITY_TAG);
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
                                BlockItemStackData.deleteLockToken(h);
                                return;
                            }
                            new LockCode(v).addToTag(h.getOrCreateTagElement(Constants.Item.BLOCK_ENTITY_TAG));
                        })
                        .delete(BlockItemStackData::deleteLockToken)
                        .supports(h -> {
                            if (!(h.getItem() instanceof BlockItem)) {
                                return false;
                            }
                            final Block block = ((BlockItem) h.getItem()).getBlock();
                            if (!(block instanceof EntityBlock)) {
                                return false;
                            }
                            try {
                                final BlockEntity tile = ((EntityBlock) block).newBlockEntity(null);
                                return tile instanceof BaseContainerBlockEntity;
                            } catch (final NullPointerException ex) {
                                return false;
                            }
                        });
    }
    // @formatter:on

    private static void deleteLockToken(final ItemStack stack) {
        final CompoundTag tag = stack.getTagElement(Constants.Item.BLOCK_ENTITY_TAG);
        if (tag != null) {
            tag.remove(Constants.Item.LOCK);
        }
    }
}
