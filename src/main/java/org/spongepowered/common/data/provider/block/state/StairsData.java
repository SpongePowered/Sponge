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
import net.minecraft.block.StairsBlock;
import net.minecraft.state.properties.StairsShape;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.StairShape;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.data.provider.util.DirectionUtils;
import org.spongepowered.common.data.provider.util.PortionTypeUtils;

public final class StairsData {

    private StairsData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asImmutable(BlockState.class)
                    .create(Keys.DIRECTION)
                        .get(h -> DirectionUtils.getFor(h.get(StairsBlock.FACING)))
                        .set((h, v) -> DirectionUtils.set(h, v, StairsBlock.FACING))
                        .supports(h -> h.getBlock() instanceof StairsBlock)
                    .create(Keys.IS_WATERLOGGED)
                        .get(h -> h.get(StairsBlock.WATERLOGGED))
                        .set((h, v) -> h.with(StairsBlock.WATERLOGGED, v))
                        .supports(h -> h.getBlock() instanceof StairsBlock)
                    .create(Keys.PORTION_TYPE)
                        .get(h -> PortionTypeUtils.getFromHalfBlock(h, StairsBlock.HALF))
                        .set((h, v) -> PortionTypeUtils.setForHalfBlock(h, v, StairsBlock.HALF))
                        .supports(h -> h.getBlock() instanceof StairsBlock)
                    .create(Keys.STAIR_SHAPE)
                        .get(h -> (StairShape) (Object) h.get(StairsBlock.SHAPE))
                        .set((h, v) -> h.with(StairsBlock.SHAPE, (StairsShape) (Object) v))
                        .supports(h -> h.getBlock() instanceof StairsBlock);
    }
    // @formatter:on
}
