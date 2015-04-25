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

import net.minecraft.block.Block;
import net.minecraft.block.BlockLever;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.manipulators.blocks.AxisData;
import org.spongepowered.api.data.manipulators.blocks.DirectionalData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.interfaces.blocks.IMixinAxisHolder;
import org.spongepowered.common.interfaces.blocks.IMixinDirectionalHolder;
import org.spongepowered.common.interfaces.blocks.IMixinPoweredHolder;

@Mixin(BlockLever.class)
public abstract class MixinBlockLever extends Block implements IMixinDirectionalHolder, IMixinAxisHolder, IMixinPoweredHolder {

    public MixinBlockLever(Material materialIn) {
        super(materialIn);
    }

    @Override
    public DirectionalData getDirectionalData(IBlockState blockState) {

        return null;
    }

    @Override
    public DataTransactionResult setDirectionalData(DirectionalData directionalData, World world, BlockPos blockPos, DataPriority priority) {
        return null;
    }

    @Override
    public void reset(World world, BlockPos blockPos) {

    }

    @Override
    public AxisData getAxisData(IBlockState blockState) {
        return null;
    }

    @Override
    public DataTransactionResult setAxisData(AxisData axisData, World world, BlockPos blockPos) {
        return null;
    }


    @Override
    public boolean isCurrentlyPowered(IBlockState blockState) {
        return ((Boolean) blockState.getValue(BlockLever.POWERED)).booleanValue();
    }

    @Override
    public DataTransactionResult setPowered(World world, BlockPos blockPos, boolean powered) {
    	final IBlockState oldBlockState = world.getBlockState(blockPos);
    	world.setBlockState(blockPos, oldBlockState.withProperty(BlockLever.POWERED, powered));
        return null;
    }
}
