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

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.common.data.DataTransactionBuilder.builder;
import static org.spongepowered.common.data.DataTransactionBuilder.fail;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.block.BlockStandingSign;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.manipulator.block.DirectionalData;
import org.spongepowered.api.util.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.data.manipulator.block.SpongeDirectionalData;
import org.spongepowered.common.interfaces.block.IMixinBlockDirectional;
import org.spongepowered.common.mixin.core.block.MixinBlock;

import java.util.Collection;

@Mixin(BlockStandingSign.class)
public abstract class MixinBlockStandingSign extends MixinBlock implements IMixinBlockDirectional {

    @Override
    public DirectionalData getDirectionalData(IBlockState blockState) {
        final int intDir = (Integer) (Object) blockState.getValue(BlockStandingSign.ROTATION);
        final DirectionalData directionalData = new SpongeDirectionalData();
        directionalData.setValue(Direction.values()[(intDir + 8) % 16]);
        return directionalData;
    }

    @Override
    public DataTransactionResult setDirectionalData(DirectionalData directionalData, World world, BlockPos blockPos, DataPriority priority) {
        final Direction direction = checkNotNull(directionalData).getValue();
        if (!direction.isSecondaryOrdinal() || !direction.isCardinal() || !direction.isOrdinal()) {
            return fail(directionalData);
        }
        // TODO actually manipulate according to priority
        final IBlockState blockState = world.getBlockState(blockPos);
        final DirectionalData oldData = getDirectionalData(blockState);
        final int directionint = (direction.ordinal() + 8) % 16;
        final IBlockState newState = blockState.withProperty(BlockStandingSign.ROTATION, directionint);
        world.setBlockState(blockPos, newState);
        return builder().replace(oldData).result(DataTransactionResult.Type.SUCCESS).build();
    }

    @Override
    public BlockState resetDirectionData(BlockState blockState) {
        return ((BlockState) ((IBlockState) blockState).withProperty(BlockStandingSign.ROTATION, 0));
    }

    @Override
    public Collection<DataManipulator<?>> getManipulators(World world, BlockPos blockPos) {
        return Lists.<DataManipulator<?>>newArrayList(getDirectionalData(world.getBlockState(blockPos))); // TODO for now.
    }

    @Override
    public ImmutableList<DataManipulator<?>> getManipulators(IBlockState blockState) {
        return ImmutableList.<DataManipulator<?>>of(getDirectionalData(blockState)); // TODO for now.
    }

}
