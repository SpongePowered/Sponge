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

import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.PortionTypeUtil;

public final class TrapDoorData {

    private TrapDoorData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asImmutable(BlockState.class)
                    .create(Keys.PORTION_TYPE)
                        .get(h -> PortionTypeUtil.getFromHalfBlock(h, TrapDoorBlock.HALF))
                        .set((h, v) -> PortionTypeUtil.setForHalfBlock(h, v, TrapDoorBlock.HALF))
                        .supports(h -> h.getBlock() instanceof TrapDoorBlock)
                    .create(Keys.IS_OPEN)
                        .get(h -> h.getValue(TrapDoorBlock.OPEN))
                        .set((h, v) -> h.setValue(TrapDoorBlock.OPEN, v))
                        .supports(h -> h.getBlock() instanceof TrapDoorBlock)
                    .create(Keys.IS_POWERED)
                        .get(h -> h.getValue(TrapDoorBlock.POWERED))
                        .set((h, v) -> h.setValue(TrapDoorBlock.POWERED, v))
                        .supports(h -> h.getBlock() instanceof TrapDoorBlock);
    }
    // @formatter:on
}
