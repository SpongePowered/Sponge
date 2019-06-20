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
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.common.bridge.block.BlockBridge;

import java.util.UUID;

public final class BlockUtil {

    public static final UUID INVALID_WORLD_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

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

    public static BlockType toBlockType(IBlockState state) {
        return fromNative(state).getType();
    }

    public static Block toBlock(BlockState state) {
        return toNative(state).getBlock();
    }

    public static Block toBlock(SpongeBlockSnapshot spongeSnapshot) {
        return toNative(spongeSnapshot.getState()).getBlock();
    }

    public static BlockBridge toMixin(BlockState blockState) {
        return (BlockBridge) toNative(blockState).getBlock();
    }

    public static BlockBridge toMixin(IBlockState blockState) {
        return (BlockBridge) blockState.getBlock();
    }

    private BlockUtil() {
    }

    public static IBlockState toNative(SpongeBlockSnapshot snapshot) {
        return toNative(snapshot.getState());
    }
}
