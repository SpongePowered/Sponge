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
import net.minecraft.block.HugeMushroomBlock;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.data.provider.util.DirectionalUtils;

public final class HugeMushroomData {

    private HugeMushroomData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asImmutable(BlockState.class)
                    .create(Keys.HAS_PORES_DOWN)
                        .get(h -> h.get(HugeMushroomBlock.DOWN))
                        .set((h, v) -> h.with(HugeMushroomBlock.DOWN, v))
                        .supports(h -> h.getBlock() instanceof HugeMushroomBlock)
                    .create(Keys.HAS_PORES_EAST)
                        .get(h -> h.get(HugeMushroomBlock.EAST))
                        .set((h, v) -> h.with(HugeMushroomBlock.EAST, v))
                        .supports(h -> h.getBlock() instanceof HugeMushroomBlock)
                    .create(Keys.HAS_PORES_NORTH)
                        .get(h -> h.get(HugeMushroomBlock.NORTH))
                        .set((h, v) -> h.with(HugeMushroomBlock.NORTH, v))
                        .supports(h -> h.getBlock() instanceof HugeMushroomBlock)
                    .create(Keys.HAS_PORES_SOUTH)
                        .get(h -> h.get(HugeMushroomBlock.SOUTH))
                        .set((h, v) -> h.with(HugeMushroomBlock.SOUTH, v))
                        .supports(h -> h.getBlock() instanceof HugeMushroomBlock)
                    .create(Keys.HAS_PORES_UP)
                        .get(h -> h.get(HugeMushroomBlock.UP))
                        .set((h, v) -> h.with(HugeMushroomBlock.UP, v))
                        .supports(h -> h.getBlock() instanceof HugeMushroomBlock)
                    .create(Keys.HAS_PORES_WEST)
                        .get(h -> h.get(HugeMushroomBlock.WEST))
                        .set((h, v) -> h.with(HugeMushroomBlock.WEST, v))
                        .supports(h -> h.getBlock() instanceof HugeMushroomBlock)
                    .create(Keys.PORES)
                        .get(h -> DirectionalUtils.getHorizontalUpDownFrom(h, HugeMushroomBlock.EAST, HugeMushroomBlock.WEST, HugeMushroomBlock.NORTH,
                                HugeMushroomBlock.SOUTH, HugeMushroomBlock.UP, HugeMushroomBlock.DOWN))
                        .set((h, v) -> DirectionalUtils.setHorizontalUpDownFor(h, v, HugeMushroomBlock.EAST, HugeMushroomBlock.WEST,
                                HugeMushroomBlock.NORTH, HugeMushroomBlock.SOUTH, HugeMushroomBlock.UP, HugeMushroomBlock.DOWN))
                        .supports(h -> h.getBlock() instanceof HugeMushroomBlock);
    }
    // @formatter:on
}
