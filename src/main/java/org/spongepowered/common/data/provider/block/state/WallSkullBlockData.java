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

import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SkullBlock;
import net.minecraft.block.WallSkullBlock;
import net.minecraft.util.registry.Registry;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.accessor.block.AbstractSkullBlockAccessor;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.StateUtil;
import org.spongepowered.common.util.Constants;

import java.util.HashMap;
import java.util.Map;

public final class WallSkullBlockData {

    private WallSkullBlockData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asImmutable(BlockState.class)
                    .create(Keys.DIRECTION)
                        .get(h -> Constants.DirectionFunctions.getFor(h.get(WallSkullBlock.FACING)))
                        .set((h, v) -> h.with(WallSkullBlock.FACING, Constants.DirectionFunctions.getFor(v)))
                        .supports(h -> h.getBlock() instanceof WallSkullBlock)
                    .create(Keys.IS_ATTACHED)
                        .get(h -> h.getBlock() instanceof WallSkullBlock)
                        .set((h, v) -> {
                            final Map<SkullBlock.ISkullType, Pair> wallAndGroundPairs = new HashMap<>();
                            final AbstractSkullBlock block = (AbstractSkullBlock) h.getBlock();
                            final boolean isWallBlock = block instanceof WallSkullBlock;
                            if (v == isWallBlock) {
                                return h;
                            }
                            final SkullBlock.ISkullType type = ((AbstractSkullBlockAccessor) block).accessor$type();
                            // Find the ground/wall pair based on the skull type
                            final Pair pair = wallAndGroundPairs.computeIfAbsent(type, type1 -> {
                                final SkullBlock groundBlock = (SkullBlock) Registry.BLOCK.stream()
                                        .filter(b -> b instanceof SkullBlock && ((AbstractSkullBlockAccessor) b).accessor$type() == type)
                                        .findFirst().orElse(null);
                                if (groundBlock == null) {
                                    return null;
                                }
                                final WallSkullBlock wallBlock = (WallSkullBlock) Registry.BLOCK.stream()
                                        .filter(b -> b instanceof WallSkullBlock && ((AbstractSkullBlockAccessor) b).accessor$type() == type)
                                        .findFirst().orElse(null);
                                if (wallBlock == null) {
                                    return null;
                                }
                                return new Pair(groundBlock, wallBlock);
                            });
                            if (pair == null) {
                                return h;
                            }
                            final Block newType = v ? pair.wallBlock : pair.groundBlock;
                            return StateUtil.copyStatesFrom(newType.getDefaultState(), h);
                        })
                        .supports(h -> h.getBlock() instanceof WallSkullBlock);
    }
    // @formatter:on

    private static final class Pair {

        final SkullBlock groundBlock;
        final WallSkullBlock wallBlock;

        private Pair(final SkullBlock groundBlock, final WallSkullBlock wallBlock) {
            this.groundBlock = groundBlock;
            this.wallBlock = wallBlock;
        }
    }
}
