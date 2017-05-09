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
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.BlockRail;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRailDetector;
import net.minecraft.block.BlockRailPowered;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableRailDirectionData;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeRailDirectionData;

import java.util.Map;
import java.util.Optional;

@Mixin(BlockRailBase.class)
public abstract class MixinBlockRailBase extends MixinBlock {

    @Override
    public ImmutableList<ImmutableDataManipulator<?, ?>> getManipulators(IBlockState blockState) {
        final ImmutableRailDirectionData railDirection = getRailDirectionFor(blockState);
        if (railDirection == null) { // Extra safety check
            return ImmutableList.of();
        }
        return ImmutableList.of(railDirection);
    }

    @Override
    public boolean supports(Class<? extends ImmutableDataManipulator<?, ?>> immutable) {
        return ImmutableRailDirectionData.class.isAssignableFrom(immutable);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Optional<BlockState> getStateWithData(IBlockState blockState, ImmutableDataManipulator<?, ?> manipulator) {
        if (manipulator instanceof ImmutableRailDirectionData) {
            final BlockRailBase.EnumRailDirection railDirection =
                    (BlockRailBase.EnumRailDirection) (Object) ((ImmutableRailDirectionData) manipulator).type().get();
            if (blockState.getBlock() instanceof BlockRail) {
                return Optional.of((BlockState) blockState.withProperty(BlockRail.SHAPE, railDirection));
            } else if (blockState.getBlock() instanceof BlockRailPowered) {
                if (!BlockRailPowered.SHAPE.getAllowedValues().contains(railDirection)) {
                    return Optional.empty();
                }
                return Optional.of((BlockState) blockState.withProperty(BlockRailPowered.SHAPE, railDirection));
            } else if (blockState.getBlock() instanceof BlockRailDetector) {
                if (!BlockRailDetector.SHAPE.getAllowedValues().contains(railDirection)) {
                    return Optional.empty();
                }
                return Optional.of((BlockState) blockState.withProperty(BlockRailDetector.SHAPE, railDirection));
            } else { // For mods that extend BlockRailBase
                for (Map.Entry<IProperty, Comparable> entry
                        : (ImmutableSet<Map.Entry<IProperty, Comparable>>) (Object) blockState.getProperties().entrySet()) {
                    if (entry.getValue() instanceof BlockRailBase.EnumRailDirection) {
                        if (entry.getKey().getAllowedValues().contains(railDirection)) {
                            return Optional.of((BlockState) blockState.withProperty(entry.getKey(), railDirection));
                        }
                    }
                }
            }
        }
        return super.getStateWithData(blockState, manipulator);
    }

    @Override
    public <E> Optional<BlockState> getStateWithValue(IBlockState blockState, Key<? extends BaseValue<E>> key, E value) {
        if (key.equals(Keys.RAIL_DIRECTION)) {
            final BlockRailBase.EnumRailDirection railDirection = (BlockRailBase.EnumRailDirection) value;
            if (blockState.getBlock() instanceof BlockRail) {
                return Optional.of((BlockState) blockState.withProperty(BlockRail.SHAPE, railDirection));
            } else if (blockState.getBlock() instanceof BlockRailPowered) {
                if (!BlockRailPowered.SHAPE.getAllowedValues().contains(railDirection)) {
                    return Optional.empty();
                }
                return Optional.of((BlockState) blockState.withProperty(BlockRailPowered.SHAPE, railDirection));
            } else if (blockState.getBlock() instanceof BlockRailDetector) {
                if (!BlockRailDetector.SHAPE.getAllowedValues().contains(railDirection)) {
                    return Optional.empty();
                }
                return Optional.of((BlockState) blockState.withProperty(BlockRailDetector.SHAPE, railDirection));
            }

        }
        return super.getStateWithValue(blockState, key, value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private ImmutableRailDirectionData getRailDirectionFor(IBlockState blockState) {
        if (blockState.getBlock() instanceof BlockRail) {
            return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeRailDirectionData.class,
                    blockState.getValue(BlockRail.SHAPE));
        } else if (blockState.getBlock() instanceof BlockRailPowered) {
            return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeRailDirectionData.class,
                    blockState.getValue(BlockRailPowered.SHAPE));
        } else if (blockState.getBlock() instanceof BlockRailDetector) {
            return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeRailDirectionData.class,
                    blockState.getValue(BlockRailDetector.SHAPE));
        } else { // For mods extending BlockRailBase
            for (Map.Entry<IProperty, Comparable> entry
                    : (ImmutableSet<Map.Entry<IProperty, Comparable>>) (Object) blockState.getProperties().entrySet()) {
                if (entry.getValue() instanceof BlockRailBase.EnumRailDirection) {
                    return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeRailDirectionData.class,
                        entry.getValue());
                }
            }
        }
        return null;
    }
}
