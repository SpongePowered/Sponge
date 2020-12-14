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
import org.spongepowered.common.bridge.entity.item.minecart.MinecartEntityBridge;
import org.spongepowered.common.data.provider.DataProviderRegistrator;

public final class AbstractMinecartData {

    private AbstractMinecartData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(AbstractMinecartEntity.class)
                    .create(Keys.BLOCK_STATE)
                        .get(h -> h.hasCustomDisplay() ? (BlockState) h.getDisplayBlockState() : null)
                        .set((h, v) -> h.setDisplayBlockState((net.minecraft.block.BlockState) v))
                        .delete(h -> h.setCustomDisplay(false))
                    .create(Keys.IS_ON_RAIL)
                        .get(h -> {
                            final BlockPos pos = h.blockPosition();
                            if (h.level.getBlockState(pos).is(BlockTags.RAILS)) {
                                return true;
                            }
                            final BlockPos posBelow = pos.offset(0, -1, 0);
                            return h.level.getBlockState(posBelow).is(BlockTags.RAILS);
                        })
                    .create(Keys.MINECART_BLOCK_OFFSET)
                        .get(AbstractMinecartEntity::getDisplayOffset)
                        .setAnd(AbstractMinecartData::setBlockOffset)
                        .deleteAnd(h -> AbstractMinecartData.setBlockOffset(h, h.getDefaultDisplayOffset()))
                .asMutable(MinecartEntityBridge.class)
                    .create(Keys.AIRBORNE_VELOCITY_MODIFIER)
                        .get(MinecartEntityBridge::bridge$getAirborneMod)
                        .set(MinecartEntityBridge::bridge$setAirborneMod)
                    .create(Keys.SLOWS_UNOCCUPIED)
                        .get(MinecartEntityBridge::bridge$getSlowWhenEmpty)
                        .set(MinecartEntityBridge::bridge$setSlowWhenEmpty)
                    .create(Keys.DERAILED_VELOCITY_MODIFIER)
                        .get(MinecartEntityBridge::bridge$getDerailedMod)
                        .set(MinecartEntityBridge::bridge$setDerailedMod)
                    .create(Keys.POTENTIAL_MAX_SPEED)
                        .get(MinecartEntityBridge::bridge$getMaxSpeed)
                        .set(MinecartEntityBridge::bridge$setMaxSpeed)
                    ;
    }
    // @formatter:on

    private static boolean setBlockOffset(final AbstractMinecartEntity holder, final Integer value) {
        if (!holder.hasCustomDisplay()) {
            return false;
        }
        holder.setDisplayOffset(value);
        return true;
    }
}
