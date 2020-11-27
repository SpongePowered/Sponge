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
package org.spongepowered.common.data.provider.block.state;

import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.state.properties.ChestType;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.ChestAttachmentType;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.DirectionUtil;
import org.spongepowered.common.util.Constants;

import java.util.Collections;

public final class ChestData {

    private ChestData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asImmutable(BlockState.class)
                    .create(Keys.CHEST_ATTACHMENT_TYPE)
                        .get(h -> (ChestAttachmentType) (Object) h.get(ChestBlock.TYPE))
                        .set((h, v) -> h.with(ChestBlock.TYPE, (ChestType) (Object) v))
                        .supports(h -> h.getBlock() instanceof ChestBlock)
                    .create(Keys.CONNECTED_DIRECTIONS)
                        .get(h -> {
                            if (h.get(ChestBlock.TYPE) == ChestType.SINGLE) {
                                return null;
                            }
                            return Collections.singleton(Constants.DirectionFunctions.getFor(ChestBlock.getDirectionToAttached(h)));
                        })
                        .supports(h -> h.getBlock() instanceof ChestBlock)
                    .create(Keys.DIRECTION)
                        .get(h -> DirectionUtil.getFor(h.get(ChestBlock.FACING)))
                        .set((h, v) -> DirectionUtil.set(h, v, ChestBlock.FACING))
                        .supports(h -> h.getBlock() instanceof ChestBlock)
                    .create(Keys.IS_CONNECTED_EAST)
                        .get(h -> {
                            if (h.get(ChestBlock.TYPE) == ChestType.SINGLE) {
                                return null;
                            }
                            return Constants.DirectionFunctions.getFor(ChestBlock.getDirectionToAttached(h)) == Direction.EAST;
                        })
                        .supports(h -> h.getBlock() instanceof ChestBlock)
                    .create(Keys.IS_CONNECTED_NORTH)
                        .get(h -> {
                            if (h.get(ChestBlock.TYPE) == ChestType.SINGLE) {
                                return null;
                            }
                            return Constants.DirectionFunctions.getFor(ChestBlock.getDirectionToAttached(h)) == Direction.NORTH;
                        })
                        .supports(h -> h.getBlock() instanceof ChestBlock)
                    .create(Keys.IS_CONNECTED_SOUTH)
                        .get(h -> {
                            if (h.get(ChestBlock.TYPE) == ChestType.SINGLE) {
                                return null;
                            }
                            return Constants.DirectionFunctions.getFor(ChestBlock.getDirectionToAttached(h)) == Direction.SOUTH;
                        })
                        .supports(h -> h.getBlock() instanceof ChestBlock)
                    .create(Keys.IS_CONNECTED_WEST)
                        .get(h -> {
                            if (h.get(ChestBlock.TYPE) == ChestType.SINGLE) {
                                return null;
                            }
                            return Constants.DirectionFunctions.getFor(ChestBlock.getDirectionToAttached(h)) == Direction.WEST;
                        })
                        .supports(h -> h.getBlock() instanceof ChestBlock)
                    .create(Keys.IS_WATERLOGGED)
                        .get(h -> h.get(ChestBlock.WATERLOGGED))
                        .set((h, v) -> h.with(ChestBlock.WATERLOGGED, v))
                        .supports(h -> h.getBlock() instanceof ChestBlock);
    }
    // @formatter:on
}
