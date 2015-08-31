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
package org.spongepowered.common.data.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.common.data.BlockDataProcessor;

import java.util.Optional;

public final class BlockDataProcessorDelegate<I extends ImmutableDataManipulator<I, ?>> implements BlockDataProcessor<I> {

    private final ImmutableList<BlockDataProcessor<I>> processors;

    public BlockDataProcessorDelegate(ImmutableList<BlockDataProcessor<I>> processors) {
        this.processors = processors;
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Optional<I> fromBlockPos(World world, BlockPos blockPos) {
        for (BlockDataProcessor<I> processor : this.processors) {
            final Optional<I> optional = processor.fromBlockPos(world, blockPos);
            if (optional.isPresent()) {
                return optional;
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<I> createFrom(IBlockState blockState) {
        for (BlockDataProcessor<I> processor : this.processors) {
            final Optional<I> optional = processor.createFrom(blockState);
            if (optional.isPresent()) {
                return optional;
            }
        }
        return Optional.empty();
    }

    @Override
    public java.util.Optional<BlockState> withData(IBlockState blockState, I manipulator) {
        for (BlockDataProcessor<I> processor : this.processors) {
            final Optional<BlockState> optional = processor.withData(blockState, manipulator);
            if (optional.isPresent()) {
                return optional;
            }
        }
        return Optional.empty();
    }

    @Override
    public DataTransactionResult setData(World world, BlockPos blockPos, I manipulator) {
        for (BlockDataProcessor<I> processor : this.processors) {
            final DataTransactionResult result = processor.setData(world, blockPos, manipulator);
            if (!result.getType().equals(DataTransactionResult.Type.FAILURE)) {
                return result;
            }
        }
        return DataTransactionBuilder.failResult(manipulator.getValues());
    }

    @Override
    public java.util.Optional<BlockState> removeFrom(IBlockState blockState) {
        for (BlockDataProcessor<I> processor : this.processors) {
            final Optional<BlockState> optional = processor.removeFrom(blockState);
            if (optional.isPresent()) {
                return optional;
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean remove(World world, BlockPos blockPos) {
        for (BlockDataProcessor<I> processor : this.processors) {
            final boolean optional = processor.remove(world, blockPos);
            if (optional) {
                return true;
            }
        }
        return false;
    }
}
