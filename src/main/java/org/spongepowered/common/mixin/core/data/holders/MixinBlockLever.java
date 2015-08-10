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
package org.spongepowered.common.mixin.core.data.holders;

import static org.spongepowered.common.data.ImmutableDataCachingUtil.getManipulator;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockLever;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableAxisData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableDirectionalData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutablePoweredData;
import org.spongepowered.api.data.manipulator.mutable.block.AxisData;
import org.spongepowered.api.data.manipulator.mutable.block.DirectionalData;
import org.spongepowered.api.data.manipulator.mutable.block.PoweredData;
import org.spongepowered.api.util.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeDirectionalData;
import org.spongepowered.common.interfaces.block.IMixinBlockAxisOriented;
import org.spongepowered.common.interfaces.block.IMixinBlockDirectional;
import org.spongepowered.common.interfaces.block.IMixinPoweredHolder;
import org.spongepowered.common.mixin.core.block.MixinBlock;

@Mixin(BlockLever.class)
public abstract class MixinBlockLever extends MixinBlock implements IMixinBlockDirectional, IMixinBlockAxisOriented, IMixinPoweredHolder {

    @Override
    public ImmutableDirectionalData getDirectionalData(IBlockState blockState) {
        final BlockLever.EnumOrientation intDir = (BlockLever.EnumOrientation) (Object) blockState.getValue(BlockLever.FACING);
        final ImmutableDirectionalData directionalData = getManipulator(ImmutableSpongeDirectionalData.class,
                Direction.values()[((intDir.ordinal() - 1) + 8) % 16]);
        return directionalData;
    }

    @Override
    public DataTransactionResult setDirectionalData(DirectionalData directionalData, World world, BlockPos blockPos) {
        return null;
    }

    @Override
    public void resetAxis(World world, BlockPos blockPos) {

    }

    @Override
    public ImmutableAxisData getAxisData(IBlockState blockState) {
        return null;
    }

    @Override
    public DataTransactionResult setAxisData(AxisData axisData, World world, BlockPos blockPos) {
        return null;
    }

    @Override
    public ImmutableList<ImmutableDataManipulator<?, ?>> getManipulators(World world, BlockPos blockPos) {
        return getManipulators(world.getBlockState(blockPos));
    }

    @Override
    public ImmutableList<ImmutableDataManipulator<?, ?>> getManipulators(IBlockState blockState) {
        return ImmutableList.<ImmutableDataManipulator<?, ?>>of(getAxisData(blockState), getDirectionalData(blockState), getPoweredData(blockState));
    }

    @Override
    public BlockState resetDirectionData(BlockState blockState) {
        return null;
    }

    @Override
    public ImmutablePoweredData getPoweredData(IBlockState blockState) {
        return null;
    }

    @Override
    public DataTransactionResult setPoweredData(PoweredData poweredData, World world, BlockPos blockPos) {
        final ImmutablePoweredData data = getPoweredData(world.getBlockState(blockPos));
        return null;
    }

    @Override
    public BlockState resetPoweredData(BlockState blockState) {
        return (BlockState) ((IBlockState)blockState).withProperty(BlockLever.POWERED, false);
    }

}
