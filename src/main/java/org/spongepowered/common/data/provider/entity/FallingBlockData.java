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

import net.minecraft.world.entity.item.FallingBlockEntity;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.accessor.world.entity.item.FallingBlockEntityAccessor;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.SpongeTicks;

public final class FallingBlockData {

    private FallingBlockData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(FallingBlockEntity.class)
                    .create(Keys.CAN_DROP_AS_ITEM)
                        .get(h -> h.dropItem)
                        .set((h, v) -> h.dropItem = v)
                .asMutable(FallingBlockEntityAccessor.class)
                    .create(Keys.BLOCK_STATE)
                        .get(h -> (BlockState) h.accessor$blockState())
                        .set((h, v) -> h.accessor$blockState((net.minecraft.world.level.block.state.BlockState) v))
                    .create(Keys.CAN_PLACE_AS_BLOCK)
                        .get(h -> !h.accessor$cancelDrop())
                        .set((h, v) -> h.accessor$cancelDrop(!v))
                    .create(Keys.CAN_HURT_ENTITIES)
                        .get(FallingBlockEntityAccessor::accessor$hurtEntities)
                        .set(FallingBlockEntityAccessor::accessor$hurtEntities)
                    .create(Keys.DAMAGE_PER_BLOCK)
                        .get(h -> (double) h.accessor$fallDamagePerDistance())
                        .set((h, v) -> h.accessor$fallDamagePerDistance(v.floatValue()))
                    .create(Keys.FALL_TIME)
                        .get(x -> new SpongeTicks(x.accessor$time()))
                        .setAnd((h, v) -> {
                            final int ticks = SpongeTicks.toSaturatedIntOrInfinite(v);
                            if (v.isInfinite() || ticks < 0) {
                                return false;
                            }
                            h.accessor$time(ticks);
                            return true;
                        })
                    .create(Keys.MAX_FALL_DAMAGE)
                        .get(h -> (double) h.accessor$fallDamageMax())
                        .setAnd((h, v) -> {
                            if (v < 0) {
                                return false;
                            }
                            h.accessor$fallDamageMax(v.intValue());
                            return true;
                        });
    }
    // @formatter:on
}
