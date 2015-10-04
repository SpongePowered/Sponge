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

import static org.spongepowered.common.data.util.DirectionResolver.getFor;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockLever;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableAxisData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableDirectionalData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutablePoweredData;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.util.Axis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeAxisData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeDirectionalData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongePoweredData;
import org.spongepowered.common.mixin.core.block.MixinBlock;

import java.util.Optional;

@Mixin(BlockLever.class)
public abstract class MixinBlockLever extends MixinBlock {

    public ImmutableDirectionalData getDirectionalData(IBlockState blockState) {
        final BlockLever.EnumOrientation intDir = (BlockLever.EnumOrientation) (Object) blockState.getValue(BlockLever.FACING);
        final ImmutableDirectionalData directionalData = ImmutableDataCachingUtil.getManipulator(ImmutableSpongeDirectionalData.class, getFor(intDir));
        return directionalData;
    }

    protected ImmutablePoweredData getPoweredData(IBlockState blockState) {
        final ImmutablePoweredData poweredData = ImmutableDataCachingUtil.getManipulator(ImmutableSpongePoweredData.class,
                                                                                         (Boolean) blockState.getValue(BlockLever.POWERED));
        return poweredData;
    }

    public ImmutableAxisData getAxisData(IBlockState blockState) {
        final BlockLever.EnumOrientation orientation = (BlockLever.EnumOrientation) blockState.getValue(BlockLever.FACING);
        final Axis axis;
        switch (orientation) {
            case UP_X:
            case DOWN_X:
                axis = Axis.X;
                break;
            case UP_Z:
            case DOWN_Z:
                axis = Axis.Z;
                break;
            default:
                axis = Axis.Y;
                break;
        }
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeAxisData.class, axis);
    }

    @Override
    public boolean supports(Class<? extends ImmutableDataManipulator<?, ?>> immutable) {
        return super.supports(immutable);
    }

    @Override
    public Optional<BlockState> getStateWithData(IBlockState blockState, ImmutableDataManipulator<?, ?> manipulator) {
        final ImmutableDataManipulator rawManipulator = manipulator;
        if (rawManipulator instanceof ImmutableDirectionalData) {
            final BlockLever.EnumOrientation orientatio = null;
        }
        return super.getStateWithData(blockState, manipulator);
    }

    @Override
    public <E> Optional<BlockState> getStateWithValue(IBlockState blockState, Key<? extends BaseValue<E>> key, E value) {
        return super.getStateWithValue(blockState, key, value);
    }

    @Override
    public ImmutableList<ImmutableDataManipulator<?, ?>> getManipulators(IBlockState blockState) {
        return ImmutableList.<ImmutableDataManipulator<?, ?>>of(getAxisData(blockState), getDirectionalData(blockState), getPoweredData(blockState));
    }

}
