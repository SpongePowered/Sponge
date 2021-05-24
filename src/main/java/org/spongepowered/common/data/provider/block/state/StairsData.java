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

import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StairsShape;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.StairShape;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.DirectionUtil;
import org.spongepowered.common.util.PortionTypeUtil;

public final class StairsData {

    private StairsData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asImmutable(BlockState.class)
                    .create(Keys.DIRECTION)
                        .get(h -> DirectionUtil.getFor(h.getValue(StairBlock.FACING)))
                        .set((h, v) -> DirectionUtil.set(h, v, StairBlock.FACING))
                        .supports(h -> h.getBlock() instanceof StairBlock)
                    .create(Keys.PORTION_TYPE)
                        .get(h -> PortionTypeUtil.getFromHalfBlock(h, StairBlock.HALF))
                        .set((h, v) -> PortionTypeUtil.setForHalfBlock(h, v, StairBlock.HALF))
                        .supports(h -> h.getBlock() instanceof StairBlock)
                    .create(Keys.STAIR_SHAPE)
                        .get(h -> (StairShape) (Object) h.getValue(StairBlock.SHAPE))
                        .set((h, v) -> h.setValue(StairBlock.SHAPE, (StairsShape) (Object) v))
                        .supports(h -> h.getBlock() instanceof StairBlock);
    }
    // @formatter:on
}
