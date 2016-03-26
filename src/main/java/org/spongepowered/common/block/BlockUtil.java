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
package org.spongepowered.common.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.api.block.BlockState;

public final class BlockUtil {

    public static void setBlockState(World world, int x, int y, int z, BlockState state, boolean notifyNeighbors) {
        setBlockState(world, new BlockPos(x, y, z), state, notifyNeighbors);
    }

    public static void setBlockState(World world, BlockPos position, BlockState state, boolean notifyNeighbors) {
        world.setBlockState(position, toBlockState(state), notifyNeighbors ? 3 : 2);
    }

    public static void setBlockState(Chunk chunk, int x, int y, int z, BlockState state, boolean notifyNeighbors) {
        setBlockState(chunk, new BlockPos(x, y, z), state, notifyNeighbors);
    }

    public static void setBlockState(Chunk chunk, BlockPos position, BlockState state, boolean notifyNeighbors) {
        if (notifyNeighbors) { // delegate to world
            setBlockState(chunk.getWorld(), position, state, true);
            return;
        }
        chunk.setBlockState(position, toBlockState(state));
    }

    private static IBlockState toBlockState(BlockState state) {
        if (state instanceof IBlockState) {
            return (IBlockState) state;
        } else {
            // TODO: Need to figure out what is sensible for other BlockState
            // implementing classes.
            throw new UnsupportedOperationException("Custom BlockState implementations are not supported");
        }
    }

    private BlockUtil() {
    }
}
