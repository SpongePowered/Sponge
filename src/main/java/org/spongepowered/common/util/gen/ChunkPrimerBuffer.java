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
package org.spongepowered.common.util.gen;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.chunk.ChunkPrimer;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.world.extent.MutableBlockVolume;

/**
 * Makes a {@link ChunkPrimer} usable as a {@link MutableBlockVolume}.
 *
 */
public final class ChunkPrimerBuffer extends AbstractChunkBuffer {

    private final ChunkPrimer chunkPrimer;

    public ChunkPrimerBuffer(ChunkPrimer chunkPrimer, int chunkX, int chunkZ) {
        super(chunkX, chunkZ);
        this.chunkPrimer = chunkPrimer;
    }

    @Override
    public BlockState getBlock(int x, int y, int z) {
        checkRange(x, y, z);
        return (BlockState) this.chunkPrimer.getBlockState(x & 0xf, y, z & 0xf);
    }

    @Override
    public void setBlock(int x, int y, int z, BlockState block) {
        checkRange(x, y, z);
        this.chunkPrimer.setBlockState(x & 0xf, y, z & 0xF, (IBlockState) block);
    }
}
