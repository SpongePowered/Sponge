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
package org.spongepowered.common.data.processor.block;

import com.google.common.base.Optional;
import net.minecraft.block.BlockLever;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.block.PoweredData;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.DataTransactionBuilder;
import org.spongepowered.common.data.SpongeBlockProcessor;
import org.spongepowered.common.data.SpongeDataProcessor;
import org.spongepowered.common.data.manipulator.block.SpongePoweredData;
import org.spongepowered.common.interfaces.block.IMixinPoweredHolder;

public class SpongePoweredProcessor implements SpongeDataProcessor<PoweredData>, SpongeBlockProcessor<PoweredData> {

    @Override
    public Optional<PoweredData> fillData(DataHolder dataHolder, PoweredData manipulator, DataPriority priority) {
        return null;
    }

    @Override
    public DataTransactionResult setData(DataHolder dataHolder, PoweredData manipulator, DataPriority priority) {
        return null;
    }

    @Override
    public boolean remove(DataHolder dataHolder) {
        return false;
    }

    @Override
    public Optional<PoweredData> build(DataView container) throws InvalidDataException {
        return null;
    }

    @Override
    public PoweredData create() {
        return new SpongePoweredData();
    }

    @Override
    public Optional<PoweredData> createFrom(DataHolder dataHolder) {
        return null;
    }

    @Override
    public Optional<PoweredData> fromBlockPos(World world, BlockPos blockPos) {
        IBlockState blockState = world.getBlockState(blockPos);
        if (blockState.getBlock() instanceof IMixinPoweredHolder && ((IMixinPoweredHolder) blockState.getBlock()).isCurrentlyPowered(blockState)) {
            return Optional.of(create());
        }
        return Optional.absent();
    }

    @Override
    public DataTransactionResult setData(World world, BlockPos blockPos, PoweredData manipulator, DataPriority priority) {
        IBlockState blockState = world.getBlockState(blockPos);
        world.setBlockState(blockPos, blockState.withProperty(BlockLever.POWERED, true));
        return DataTransactionBuilder.successNoData();
    }

    @Override
    public boolean remove(World world, BlockPos blockPos) {
        IBlockState blockState = world.getBlockState(blockPos);
        return world.setBlockState(blockPos, blockState.withProperty(BlockLever.POWERED, false));
    }

    @Override
    public Optional<PoweredData> createFrom(IBlockState blockState) {
        if (blockState.getBlock() instanceof IMixinPoweredHolder) {
            return ((IMixinPoweredHolder) blockState.getBlock()).isCurrentlyPowered(blockState) ? Optional.of(create())
                    : Optional.<PoweredData>absent();
        }
        return Optional.absent();
    }

    @Override
    public Optional<BlockState> removeFrom(IBlockState blockState) {
        if (blockState.getBlock() instanceof IMixinPoweredHolder) {
            return Optional.of(((IMixinPoweredHolder) blockState.getBlock()).setUnpowered(blockState));
        }
        return Optional.absent();
    }

    @Override
    public Optional<PoweredData> getFrom(DataHolder dataHolder) {
        return Optional.absent();
    }
}
