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

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.block.IMixinBlock;

import java.util.UUID;

public final class BlockUtil {

    public static final UUID INVALID_WORLD_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public static IBlockReader readerOf(IBlockState state, BlockPos pos) {
        return new SingleBlockReader(state, pos);
    }

    public static boolean setBlockState(World world, BlockPos position, BlockState state, boolean notifyNeighbors) {
        return world.setBlockState(position, toNative(state), notifyNeighbors ? 3 : 2);
    }

    public static boolean setBlockState(Chunk chunk, int x, int y, int z, BlockState state, boolean notifyNeighbors) {
        return setBlockState(chunk, new BlockPos(x, y, z), state, notifyNeighbors);
    }

    public static boolean setBlockState(Chunk chunk, BlockPos position, BlockState state, boolean notifyNeighbors) {
        if (notifyNeighbors) { // delegate to world
            return setBlockState(chunk.getWorld(), position, state, true);
        }
        return ((IMixinChunk) chunk).setBlockState(position, toNative(state), chunk.getBlockState(position), null, BlockChangeFlags.ALL.withUpdateNeighbors(notifyNeighbors)) != null;
    }

    public static IBlockState toNative(BlockState state) {
        if (state instanceof IBlockState) {
            return (IBlockState) state;
        }
        // TODO: Need to figure out what is sensible for other BlockState
        // implementing classes.
        throw new UnsupportedOperationException("Custom BlockState implementations are not supported");
    }

    public static BlockState fromNative(IBlockState blockState) {
        if (blockState instanceof BlockState) {
            return (BlockState) blockState;
        }
        // TODO: Need to figure out what is sensible for other BlockState
        // implementing classes.
        throw new UnsupportedOperationException("Custom BlockState implementations are not supported");
    }

    public static BlockType toBlock(IBlockState state) {
        return fromNative(state).getType();
    }

    public static Block toBlock(BlockState state) {
        return toNative(state).getBlock();
    }

    public static IMixinBlock toMixin(BlockState blockState) {
        return (IMixinBlock) toNative(blockState).getBlock();
    }

    public static IMixinBlock toMixin(IBlockState blockState) {
        return (IMixinBlock) blockState.getBlock();
    }

    private BlockUtil() {
    }

}
