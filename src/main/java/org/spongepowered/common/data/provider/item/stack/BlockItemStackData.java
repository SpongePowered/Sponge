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

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.LockCode;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.bridge.block.DyeColorBlockBridge;
import org.spongepowered.common.data.provider.DataProviderRegistrator;

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
                            var component = h.getOrDefault(DataComponents.LOCK, LockCode.NO_LOCK);
                            return component.key().isEmpty() ? null : component.key();
                        })
                        .set((h, v) -> {
                            if (v.isEmpty()) {
                                h.remove(DataComponents.LOCK);
                                return;
                            }
                            h.set(DataComponents.LOCK, new LockCode(v));
                        })
                        .delete(h -> h.remove(DataComponents.LOCK))
                        .supports(h -> {
                            if (!(h.getItem() instanceof BlockItem)) {
                                return false;
                            }
                            final Block block = ((BlockItem) h.getItem()).getBlock();
                            if (!(block instanceof EntityBlock)) {
                                return false;
                            }
                            try {
                                final BlockEntity tile = ((EntityBlock) block).newBlockEntity(null, null);
                                return tile instanceof BaseContainerBlockEntity;
                            } catch (final NullPointerException ex) {
                                return false;
                            }
                        });
    }
    // @formatter:on

}
