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
import net.minecraft.block.DoorBlock;
import net.minecraft.state.properties.DoorHingeSide;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.DoorHinges;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.DirectionUtil;
import org.spongepowered.common.util.PortionTypeUtil;

public final class DoorData {

    private DoorData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asImmutable(BlockState.class)
                    .create(Keys.DOOR_HINGE)
                        .get(h -> h.get(DoorBlock.HINGE) == DoorHingeSide.LEFT ? DoorHinges.LEFT.get() : DoorHinges.RIGHT.get())
                        .set((h, v) -> {
                            final DoorHingeSide side = v == DoorHinges.LEFT.get() ? DoorHingeSide.LEFT : DoorHingeSide.RIGHT;
                            return h.with(DoorBlock.HINGE, side);
                        })
                        .supports(h -> h.getBlock() instanceof DoorBlock)
                    .create(Keys.DIRECTION)
                        .get(h -> DirectionUtil.getFor(h.get(DoorBlock.FACING)))
                        .set((h, v) -> DirectionUtil.set(h, v, DoorBlock.FACING))
                        .supports(h -> h.getBlock() instanceof DoorBlock)
                    .create(Keys.IS_OPEN)
                        .get(h -> h.get(DoorBlock.OPEN))
                        .set((h, v) -> h.with(DoorBlock.OPEN, v))
                        .supports(h -> h.getBlock() instanceof DoorBlock)
                    .create(Keys.IS_POWERED)
                        .get(h -> h.get(DoorBlock.POWERED))
                        .set((h, v) -> h.with(DoorBlock.POWERED, v))
                        .supports(h -> h.getBlock() instanceof DoorBlock)
                    .create(Keys.PORTION_TYPE)
                        .get(h -> PortionTypeUtil.getFromDoubleBlock(h, DoorBlock.HALF))
                        .set((h, v) -> PortionTypeUtil.setForDoubleBlock(h, v, DoorBlock.HALF))
                        .supports(h -> h.getBlock() instanceof DoorBlock);
    }
    // @formatter:on
}
