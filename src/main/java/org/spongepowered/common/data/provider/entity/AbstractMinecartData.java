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
package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.DataProviderRegistrator;

public final class AbstractMinecartData {

    private AbstractMinecartData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(AbstractMinecartEntity.class)
                    .create(Keys.BLOCK_STATE)
                        .get(h -> h.hasDisplayTile() ? (BlockState) h.getDisplayTile() : null)
                        .set((h, v) -> h.setDisplayTile((net.minecraft.block.BlockState) v))
                        .delete(h -> h.setHasDisplayTile(false))
                    .create(Keys.IS_ON_RAIL)
                        .get(h -> {
                            final BlockPos pos = h.getPosition();
                            if (h.getEntityWorld().getBlockState(pos).isIn(BlockTags.RAILS)) {
                                return true;
                            }
                            final BlockPos posBelow = pos.add(0, -1, 0);
                            return h.getEntityWorld().getBlockState(posBelow).isIn(BlockTags.RAILS);
                        })
                    .create(Keys.MINECART_BLOCK_OFFSET)
                        .get(AbstractMinecartEntity::getDisplayTileOffset)
                        .setAnd(AbstractMinecartData::setBlockOffset)
                        .deleteAnd(h -> setBlockOffset(h, h.getDefaultDisplayTileOffset()));
    }
    // @formatter:on

    private static boolean setBlockOffset(final AbstractMinecartEntity holder, final Integer value) {
        if (!holder.hasDisplayTile()) {
            return false;
        }
        holder.setDisplayTileOffset(value);
        return true;
    }
}
