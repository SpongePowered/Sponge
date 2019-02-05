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
package org.spongepowered.common.mixin.core.block;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockLever;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableAxisData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableDirectionalData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutablePoweredData;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.util.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeAxisData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeDirectionalData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongePoweredData;
import org.spongepowered.common.data.util.DirectionResolver;

import java.util.Optional;

@Mixin(BlockLever.class)
public abstract class MixinBlockLever extends MixinBlock {

    @Override
    public ImmutableList<ImmutableDataManipulator<?, ?>> getManipulators(IBlockState blockState) {
        return ImmutableList.<ImmutableDataManipulator<?, ?>>of(getIsPoweredFor(blockState), getDirectionalData(blockState), getAxisData(blockState));
    }

    @Override
    public boolean supports(Class<? extends ImmutableDataManipulator<?, ?>> immutable) {
        return ImmutablePoweredData.class.isAssignableFrom(immutable) || ImmutableDirectionalData.class.isAssignableFrom(immutable)
                || ImmutableAxisData.class.isAssignableFrom(immutable);
    }

    @Override
    public Optional<BlockState> getStateWithData(IBlockState blockState, ImmutableDataManipulator<?, ?> manipulator) {
        if (manipulator instanceof ImmutablePoweredData) {
            return Optional.of((BlockState) blockState.withProperty(BlockLever.POWERED, ((ImmutablePoweredData) manipulator).powered().get()));
        }
        if (manipulator instanceof ImmutableDirectionalData) {
            final Direction dir = ((ImmutableDirectionalData) manipulator).direction().get();
            final Axis axis = this.getAxisFromOrientation(blockState.getValue(BlockLever.FACING));
            return Optional.of((BlockState) blockState.withProperty(BlockLever.FACING, DirectionResolver.getAsOrientation(dir, axis)));
        }
        if (manipulator instanceof ImmutableAxisData) {
            final Axis axis = ((ImmutableAxisData) manipulator).axis().get();
            final Direction dir = DirectionResolver.getFor(blockState.getValue(BlockLever.FACING));
            return Optional.of((BlockState) blockState.withProperty(BlockLever.FACING, DirectionResolver.getAsOrientation(dir, axis)));
        }
        return super.getStateWithData(blockState, manipulator);
    }

    @Override
    public <E> Optional<BlockState> getStateWithValue(IBlockState blockState, Key<? extends Value<E>> key, E value) {
        if (key.equals(Keys.POWERED)) {
            return Optional.of((BlockState) blockState.withProperty(BlockLever.POWERED, (Boolean) value));
        }
        if (key.equals(Keys.DIRECTION)) {
            final Direction dir = (Direction) value;
            final Axis axis = this.getAxisFromOrientation(blockState.getValue(BlockLever.FACING));
            return Optional.of((BlockState) blockState.withProperty(BlockLever.FACING, DirectionResolver.getAsOrientation(dir, axis)));
        }
        if (key.equals(Keys.AXIS)) {
            final Axis axis = (Axis) value;
            final Direction dir = DirectionResolver.getFor(blockState.getValue(BlockLever.FACING));
            return Optional.of((BlockState) blockState.withProperty(BlockLever.FACING, DirectionResolver.getAsOrientation(dir, axis)));
        }
        return super.getStateWithValue(blockState, key, value);
    }

    private ImmutablePoweredData getIsPoweredFor(IBlockState blockState) {
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongePoweredData.class, blockState.getValue(BlockLever.POWERED));
    }

    private ImmutableDirectionalData getDirectionalData(IBlockState blockState) {
        final BlockLever.EnumOrientation intDir = blockState.getValue(BlockLever.FACING);
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeDirectionalData.class, DirectionResolver.getFor(intDir));
    }

    public ImmutableAxisData getAxisData(IBlockState blockState) {
        final BlockLever.EnumOrientation orientation = blockState.getValue(BlockLever.FACING);
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeAxisData.class, this.getAxisFromOrientation(orientation));
    }

    private Axis getAxisFromOrientation(BlockLever.EnumOrientation orientation) {
        final Axis axis;
        switch (orientation) {
            case UP_X:
                axis = Axis.X;
                break;
            case DOWN_X:
                axis = Axis.X;
                break;
            case UP_Z:
                axis = Axis.Z;
                break;
            case DOWN_Z:
                axis = Axis.Z;
                break;
            default:
                axis = Axis.Y;
                break;
        }
        return axis;
    }
}
