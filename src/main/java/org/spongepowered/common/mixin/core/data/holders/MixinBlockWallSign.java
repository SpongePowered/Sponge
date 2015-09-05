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

import static org.spongepowered.api.data.DataTransactionBuilder.builder;
import static org.spongepowered.api.data.DataTransactionBuilder.failResult;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockStandingSign;
import net.minecraft.block.BlockWallSign;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableDirectionalData;
import org.spongepowered.api.data.manipulator.mutable.block.DirectionalData;
import org.spongepowered.api.util.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeDirectionalData;
import org.spongepowered.common.interfaces.block.IMixinBlockDirectional;
import org.spongepowered.common.mixin.core.block.MixinBlock;

@Mixin(BlockWallSign.class)
public abstract class MixinBlockWallSign extends MixinBlock implements IMixinBlockDirectional {

    @Override
    public ImmutableDirectionalData getDirectionalData(IBlockState blockState) {
        final EnumFacing facing = (EnumFacing) blockState.getValue(BlockWallSign.FACING);
        final int directOrd = (((facing.getHorizontalIndex()) + 2) % 4) * 4;
        final Direction direction = Direction.values()[directOrd];
        SpongeDirectionalData data = new SpongeDirectionalData();
        data.setValue(direction);
        return data.asImmutable();
    }

    @Override
    public DataTransactionResult setDirectionalData(DirectionalData directionalData, World world, BlockPos blockPos) {
        if (!directionalData.direction().get().isCardinal()) {
            return failResult(directionalData.direction().asImmutable());
        }
        // TODO actually manipulate according to priority
        final ImmutableDirectionalData oldData = getDirectionalData(world.getBlockState(blockPos));
        final IBlockState oldState = world.getBlockState(blockPos);
        final int reverseOrd = ((directionalData.direction().get().ordinal() + 8) % 16) / 16;
        world.setBlockState(blockPos, oldState.withProperty(BlockWallSign.FACING, reverseOrd));
        return builder().replace(oldData.direction()).result(DataTransactionResult.Type.SUCCESS).build();
    }

    @Override
    public BlockState resetDirectionData(BlockState blockState) {
        return ((BlockState) ((IBlockState) blockState).withProperty(BlockStandingSign.ROTATION, 0));
    }

    @Override
    public ImmutableList<ImmutableDataManipulator<?, ?>> getManipulators(IBlockState blockState) {
        return ImmutableList.<ImmutableDataManipulator<?, ?>>of(getDirectionalData(blockState)); // TODO for now.
    }

}
