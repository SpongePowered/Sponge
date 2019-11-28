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
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.DetectorRailBlock;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.block.RailBlock;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
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

import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(AbstractRailBlock.class)
public abstract class BlockRailBaseMixin extends BlockMixin {

    @Override
    public ImmutableList<ImmutableDataManipulator<?, ?>> bridge$getManipulators(final net.minecraft.block.BlockState blockState) {
        final ImmutableRailDirectionData railDirection = impl$getRailDirectionFor(blockState);
        if (railDirection == null) { // Extra safety check
            return ImmutableList.of();
        }
        return ImmutableList.of(railDirection);
    }

    @Override
    public boolean bridge$supports(final Class<? extends ImmutableDataManipulator<?, ?>> immutable) {
        return ImmutableRailDirectionData.class.isAssignableFrom(immutable);
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    @Override
    public Optional<BlockState> bridge$getStateWithData(final net.minecraft.block.BlockState blockState, final ImmutableDataManipulator<?, ?> manipulator) {
        if (manipulator instanceof ImmutableRailDirectionData) {
            final RailDirection apiDirection = ((ImmutableRailDirectionData) manipulator).type().get();
            final AbstractRailBlock.EnumRailDirection railDirection = (AbstractRailBlock.EnumRailDirection) (Object) apiDirection;
            final Optional<BlockState> state = impl$getStateForDirection(blockState, railDirection);
            if (state.isPresent()) {
                return state;
            }
            // For mods that extend BlockRailBase
            for (final Map.Entry<IProperty<?>, Comparable<?>> entry :  blockState.func_177228_b().entrySet
                    ()) {
                if (entry.getValue() instanceof AbstractRailBlock.EnumRailDirection) {
                    if (entry.getKey().getAllowedValues().contains(railDirection)) {
                        final PropertyEnum<AbstractRailBlock.EnumRailDirection> property = (PropertyEnum<AbstractRailBlock.EnumRailDirection>) entry.getKey();
                        final net.minecraft.block.BlockState newState = blockState.func_177226_a(property, railDirection);
                        return Optional.of((BlockState) newState);
                    }
                }
            }
        }
        return super.bridge$getStateWithData(blockState, manipulator);
    }

    @Override
    public <E> Optional<BlockState> bridge$getStateWithValue(final net.minecraft.block.BlockState blockState, final Key<? extends BaseValue<E>> key, final E value) {
        if (key.equals(Keys.RAIL_DIRECTION)) {
            final AbstractRailBlock.EnumRailDirection railDirection = (AbstractRailBlock.EnumRailDirection) value;
            final Optional<BlockState> x = impl$getStateForDirection(blockState, railDirection);
            if (x.isPresent()) {
                return x;
            }

        }
        return super.bridge$getStateWithValue(blockState, key, value);
    }

    private Optional<BlockState> impl$getStateForDirection(final net.minecraft.block.BlockState blockState, final AbstractRailBlock.EnumRailDirection railDirection) {
        if (blockState.getBlock() instanceof RailBlock) {
            return Optional.of((BlockState) blockState.func_177226_a(RailBlock.SHAPE, railDirection));
        }
        if (blockState.getBlock() instanceof PoweredRailBlock) {
            if (!PoweredRailBlock.SHAPE.getAllowedValues().contains(railDirection)) {
                return Optional.empty();
            }
            return Optional.of((BlockState) blockState.func_177226_a(PoweredRailBlock.SHAPE, railDirection));
        }
        if (blockState.getBlock() instanceof DetectorRailBlock) {
            if (!DetectorRailBlock.SHAPE.getAllowedValues().contains(railDirection)) {
                return Optional.empty();
            }
            return Optional.of((BlockState) blockState.func_177226_a(DetectorRailBlock.SHAPE, railDirection));
        }
        return Optional.empty();
    }

    @Nullable
    private ImmutableRailDirectionData impl$getRailDirectionFor(final net.minecraft.block.BlockState blockState) {
        if (blockState.getBlock() instanceof RailBlock) {
            return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeRailDirectionData.class, blockState.get(RailBlock.SHAPE));
        }
        if (blockState.getBlock() instanceof PoweredRailBlock) {
            return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeRailDirectionData.class, blockState.get(PoweredRailBlock.SHAPE));
        }
        if (blockState.getBlock() instanceof DetectorRailBlock) {
            return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeRailDirectionData.class, blockState.get(DetectorRailBlock.SHAPE));
        } // For mods extending BlockRailBase
        for (final Map.Entry<IProperty<?>, Comparable<?>> entry :  blockState.func_177228_b().entrySet()) {
            if (entry.getValue() instanceof AbstractRailBlock.EnumRailDirection) {
                return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeRailDirectionData.class, entry.getValue());
            }
        }
        return null;
    }
}
