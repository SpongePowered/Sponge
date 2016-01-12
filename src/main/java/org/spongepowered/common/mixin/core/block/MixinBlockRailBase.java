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
import net.minecraft.block.BlockRail;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRailDetector;
import net.minecraft.block.BlockRailPowered;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableRailDirectionData;
import org.spongepowered.api.data.type.RailDirection;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeRailDirectionData;

import java.util.Optional;

@Mixin(BlockRailBase.class)
public abstract class MixinBlockRailBase extends MixinBlock {

    @Override
    public ImmutableList<ImmutableDataManipulator<?, ?>> getManipulators(IBlockState blockState) {
        return ImmutableList.<ImmutableDataManipulator<?, ?>>of(getRailDirectionFor(blockState));
    }

    @Override
    public boolean supports(Class<? extends ImmutableDataManipulator<?, ?>> immutable) {
        return ImmutableRailDirectionData.class.isAssignableFrom(immutable);
    }

    @Override
    public Optional<BlockState> getStateWithData(IBlockState blockState, ImmutableDataManipulator<?, ?> manipulator) {
        if (manipulator instanceof ImmutableRailDirectionData) {
            if (blockState.getBlock() instanceof BlockRail) {
                final BlockRailBase.EnumRailDirection railDirection =
                        (BlockRailBase.EnumRailDirection) (Object) ((ImmutableRailDirectionData) manipulator).type().get();
                return Optional.of((BlockState) blockState.withProperty(BlockRail.SHAPE, railDirection));
            } else if (blockState.getBlock() instanceof BlockRailPowered) {
                final BlockRailBase.EnumRailDirection railDirection =
                        (BlockRailBase.EnumRailDirection) (Object) ((ImmutableRailDirectionData) manipulator).type().get();
                if (railDirection == BlockRailBase.EnumRailDirection.NORTH_EAST || railDirection != BlockRailBase.EnumRailDirection.NORTH_WEST
                        || railDirection == BlockRailBase.EnumRailDirection.SOUTH_EAST || railDirection == BlockRailBase.EnumRailDirection.SOUTH_WEST) {
                    return Optional.empty();
                }
                return Optional.of((BlockState) blockState.withProperty(BlockRailPowered.SHAPE, railDirection));
            } else if (blockState.getBlock() instanceof BlockRailDetector) {
                final BlockRailBase.EnumRailDirection railDirection =
                        (BlockRailBase.EnumRailDirection) (Object) ((ImmutableRailDirectionData) manipulator).type().get();
                if (railDirection == BlockRailBase.EnumRailDirection.NORTH_EAST || railDirection != BlockRailBase.EnumRailDirection.NORTH_WEST
                        || railDirection == BlockRailBase.EnumRailDirection.SOUTH_EAST || railDirection == BlockRailBase.EnumRailDirection.SOUTH_WEST) {
                    return Optional.empty();
                }
                return Optional.of((BlockState) blockState.withProperty(BlockRailDetector.SHAPE, railDirection));
            }
        }
        return super.getStateWithData(blockState, manipulator);
    }

    @Override
    public <E> Optional<BlockState> getStateWithValue(IBlockState blockState, Key<? extends BaseValue<E>> key, E value) {
        if (key.equals(Keys.RAIL_DIRECTION)) {
            if (blockState.getBlock() instanceof BlockRail) {
                final BlockRailBase.EnumRailDirection railDirection = (BlockRailBase.EnumRailDirection) value;
                return Optional.of((BlockState) blockState.withProperty(BlockRail.SHAPE, railDirection));
            } else if (blockState.getBlock() instanceof BlockRailPowered) {
                final BlockRailBase.EnumRailDirection railDirection = (BlockRailBase.EnumRailDirection) value;
                if (railDirection == BlockRailBase.EnumRailDirection.NORTH_EAST || railDirection != BlockRailBase.EnumRailDirection.NORTH_WEST
                        || railDirection == BlockRailBase.EnumRailDirection.SOUTH_EAST || railDirection == BlockRailBase.EnumRailDirection.SOUTH_WEST) {
                    return Optional.empty();
                }
                return Optional.of((BlockState) blockState.withProperty(BlockRailPowered.SHAPE, railDirection));
            } else if (blockState.getBlock() instanceof BlockRailDetector) {
                final BlockRailBase.EnumRailDirection railDirection = (BlockRailBase.EnumRailDirection) value;
                if (railDirection == BlockRailBase.EnumRailDirection.NORTH_EAST || railDirection != BlockRailBase.EnumRailDirection.NORTH_WEST
                        || railDirection == BlockRailBase.EnumRailDirection.SOUTH_EAST || railDirection == BlockRailBase.EnumRailDirection.SOUTH_WEST) {
                    return Optional.empty();
                }
                return Optional.of((BlockState) blockState.withProperty(BlockRailDetector.SHAPE, railDirection));
            }

        }
        return super.getStateWithValue(blockState, key, value);
    }

    private ImmutableRailDirectionData getRailDirectionFor(IBlockState blockState) {
        if (blockState.getBlock() instanceof BlockRail) {
            return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeRailDirectionData.class,
                    (RailDirection) (Object) blockState.getValue(BlockRail.SHAPE));
        } else if (blockState.getBlock() instanceof BlockRailPowered) {
            return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeRailDirectionData.class,
                    (RailDirection) (Object) blockState.getValue(BlockRailPowered.SHAPE));
        } else if (blockState.getBlock() instanceof BlockRailDetector) {
            return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeRailDirectionData.class,
                    (RailDirection) (Object) blockState.getValue(BlockRailDetector.SHAPE));
        }
        return null;
    }
}
