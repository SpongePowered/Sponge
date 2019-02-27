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
package org.spongepowered.common.event.tracking.context;

import com.google.common.base.MoreObjects;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.world.World;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

final class NeighborNotification {
    final IMixinWorldServer worldServer;
    final IBlockState source;
    final BlockPos notifyPos;
    final Block sourceBlock;
    final BlockPos sourcePos;

    NeighborNotification(IMixinWorldServer worldServer, IBlockState source, BlockPos notifyPos, Block sourceBlock,
        BlockPos sourcePos) {
        this.worldServer = worldServer;
        this.source = source;
        this.notifyPos = notifyPos;
        this.sourceBlock = sourceBlock;
        this.sourcePos = sourcePos;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("worldServer", ((World) this.worldServer).getProperties().getWorldName())
            .add("source", this.source)
            .add("notifyPos", this.notifyPos)
            .add("sourceBlock", this.sourceBlock)
            .add("sourcePos", this.sourcePos)
            .toString();
    }
}
