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
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableAxisData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableDirectionalData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutablePoweredData;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.util.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeAxisData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeDirectionalData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongePoweredData;
import org.spongepowered.common.util.Constants;

import java.util.Optional;
import net.minecraft.block.LeverBlock;

@Mixin(LeverBlock.class)
public abstract class BlockLeverMixin extends BlockMixin {

    @SuppressWarnings("RedundantTypeArguments") // some java compilers will not calculate this generic correctly
    @Override
    public ImmutableList<ImmutableDataManipulator<?, ?>> bridge$getManipulators(final net.minecraft.block.BlockState blockState) {
        return ImmutableList.<ImmutableDataManipulator<?, ?>>of(impl$getIsPoweredFor(blockState), impl$getDirectionalData(blockState), impl$getAxisData(blockState));
    }

    @Override
    public boolean bridge$supports(final Class<? extends ImmutableDataManipulator<?, ?>> immutable) {
        return ImmutablePoweredData.class.isAssignableFrom(immutable) || ImmutableDirectionalData.class.isAssignableFrom(immutable)
                || ImmutableAxisData.class.isAssignableFrom(immutable);
    }

    @Override
    public Optional<BlockState> bridge$getStateWithData(final net.minecraft.block.BlockState blockState, final ImmutableDataManipulator<?, ?> manipulator) {
        if (manipulator instanceof ImmutablePoweredData) {
            return Optional.of((BlockState) blockState.func_177226_a(LeverBlock.field_176359_b, ((ImmutablePoweredData) manipulator).powered().get()));
        }
        if (manipulator instanceof ImmutableDirectionalData) {
            final Direction dir = ((ImmutableDirectionalData) manipulator).direction().get();
            final Axis axis = this.impl$getAxisFromOrientation(blockState.func_177229_b(LeverBlock.field_176360_a));
            return Optional.of((BlockState) blockState.func_177226_a(LeverBlock.field_176360_a, Constants.DirectionFunctions.getAsOrientation(dir, axis)));
        }
        if (manipulator instanceof ImmutableAxisData) {
            final Axis axis = ((ImmutableAxisData) manipulator).axis().get();
            final Direction dir = Constants.DirectionFunctions.getFor(blockState.func_177229_b(LeverBlock.field_176360_a));
            return Optional.of((BlockState) blockState.func_177226_a(LeverBlock.field_176360_a, Constants.DirectionFunctions.getAsOrientation(dir, axis)));
        }
        return super.bridge$getStateWithData(blockState, manipulator);
    }

    @Override
    public <E> Optional<BlockState> bridge$getStateWithValue(final net.minecraft.block.BlockState blockState, final Key<? extends BaseValue<E>> key, final E value) {
        if (key.equals(Keys.POWERED)) {
            return Optional.of((BlockState) blockState.func_177226_a(LeverBlock.field_176359_b, (Boolean) value));
        }
        if (key.equals(Keys.DIRECTION)) {
            final Direction dir = (Direction) value;
            final Axis axis = this.impl$getAxisFromOrientation(blockState.func_177229_b(LeverBlock.field_176360_a));
            return Optional.of((BlockState) blockState.func_177226_a(LeverBlock.field_176360_a, Constants.DirectionFunctions.getAsOrientation(dir, axis)));
        }
        if (key.equals(Keys.AXIS)) {
            final Axis axis = (Axis) value;
            final Direction dir = Constants.DirectionFunctions.getFor(blockState.func_177229_b(LeverBlock.field_176360_a));
            return Optional.of((BlockState) blockState.func_177226_a(LeverBlock.field_176360_a, Constants.DirectionFunctions.getAsOrientation(dir, axis)));
        }
        return super.bridge$getStateWithValue(blockState, key, value);
    }

    private ImmutablePoweredData impl$getIsPoweredFor(final net.minecraft.block.BlockState blockState) {
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongePoweredData.class, blockState.func_177229_b(LeverBlock.field_176359_b));
    }

    private ImmutableDirectionalData impl$getDirectionalData(final net.minecraft.block.BlockState blockState) {
        final LeverBlock.EnumOrientation intDir = blockState.func_177229_b(LeverBlock.field_176360_a);
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeDirectionalData.class, Constants.DirectionFunctions.getFor(intDir));
    }

    private ImmutableAxisData impl$getAxisData(final net.minecraft.block.BlockState blockState) {
        final LeverBlock.EnumOrientation orientation = blockState.func_177229_b(LeverBlock.field_176360_a);
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeAxisData.class, this.impl$getAxisFromOrientation(orientation));
    }

    private Axis impl$getAxisFromOrientation(final LeverBlock.EnumOrientation orientation) {
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
