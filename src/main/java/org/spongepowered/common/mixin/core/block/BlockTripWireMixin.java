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
import net.minecraft.block.BlockTripWire;
import net.minecraft.block.BlockWall;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableAttachedData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableConnectedDirectionData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableDisarmedData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutablePoweredData;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.util.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeAttachedData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeConnectedDirectionData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeDisarmedData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongePoweredData;
import org.spongepowered.common.util.Constants;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Mixin(BlockTripWire.class)
public abstract class BlockTripWireMixin extends BlockMixin {

    @Override
    public ImmutableList<ImmutableDataManipulator<?, ?>> bridge$getManipulators(final IBlockState blockState) {
        return ImmutableList.<ImmutableDataManipulator<?, ?>>of(impl$getIsDisarmedFor(blockState),
                impl$getIsAttachedFor(blockState), impl$getIsPoweredFor(blockState), impl$getConnectedDirectionData(blockState));
    }

    @Override
    public boolean bridge$supports(final Class<? extends ImmutableDataManipulator<?, ?>> immutable) {
        return ImmutableDisarmedData.class.isAssignableFrom(immutable)
                || ImmutableAttachedData.class.isAssignableFrom(immutable) || ImmutablePoweredData.class.isAssignableFrom(immutable)
                || ImmutableConnectedDirectionData.class.isAssignableFrom(immutable);
    }

    private IBlockState impl$applyConnectedDirections(final IBlockState blockState, final Set<Direction> directions) {
        final Map<PropertyBool, Boolean> facingStates = new HashMap<>();
        for (PropertyBool property : Constants.DirectionFunctions.TripWire.ALL_DIRECTION_PROPERTIES) {
            facingStates.put(property, false);
        }
        for (Direction connectedDirection : directions) {
            final Optional<PropertyBool> facingPropertyBox = Constants.DirectionFunctions.TripWire.getPropertyFromDirection(connectedDirection);
            facingPropertyBox.ifPresent(facingProperty -> facingStates.put(facingProperty, true));
        }
        IBlockState resultBlockState = blockState;
        for (PropertyBool property : facingStates.keySet()) {
            resultBlockState = resultBlockState.withProperty(property, facingStates.get(property));
        }
        return resultBlockState;
    }

    @Override
    public Optional<BlockState> bridge$getStateWithData(final IBlockState blockState, final ImmutableDataManipulator<?, ?> manipulator) {
        if (manipulator instanceof ImmutableDisarmedData) {
            final boolean disarmed = ((ImmutableDisarmedData) manipulator).disarmed().get();
            return Optional.of((BlockState) blockState.withProperty(BlockTripWire.DISARMED, disarmed));
        }
        if (manipulator instanceof ImmutableAttachedData) {
            return Optional.of((BlockState) blockState);
        }
        if (manipulator instanceof ImmutablePoweredData) {
            return Optional.of((BlockState) blockState.withProperty(BlockTripWire.POWERED, ((ImmutablePoweredData) manipulator).powered().get()));
        }
        if (manipulator instanceof ImmutableConnectedDirectionData) {
            final ImmutableConnectedDirectionData connectedDirectionData = (ImmutableConnectedDirectionData) manipulator;
            return Optional.of((BlockState) impl$applyConnectedDirections(blockState, connectedDirectionData.connectedDirections().get()));
        }
        return super.bridge$getStateWithData(blockState, manipulator);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> Optional<BlockState> bridge$getStateWithValue(final IBlockState blockState, final Key<? extends BaseValue<E>> key, final E value) {
        if (key.equals(Keys.DISARMED)) {
            final boolean disarmed = (Boolean) value;
            return Optional.of((BlockState) blockState.withProperty(BlockTripWire.DISARMED, disarmed));
        }
        if (key.equals(Keys.SUSPENDED)) {
            return Optional.of((BlockState) blockState);
        }
        if (key.equals(Keys.ATTACHED)) {
            return Optional.of((BlockState) blockState);
        }
        if (key.equals(Keys.POWERED)) {
            return Optional.of((BlockState) blockState.withProperty(BlockTripWire.POWERED, (Boolean) value));
        }
        if (key.equals(Keys.CONNECTED_DIRECTIONS)) {
            return Optional.of((BlockState) impl$applyConnectedDirections(blockState, (Set<Direction>) value));
        } else if (key.equals(Keys.CONNECTED_EAST)) {
            return Optional.of((BlockState) blockState.withProperty(BlockWall.EAST, (Boolean) value));
        } else if (key.equals(Keys.CONNECTED_NORTH)) {
            return Optional.of((BlockState) blockState.withProperty(BlockWall.NORTH, (Boolean) value));
        } else if (key.equals(Keys.CONNECTED_SOUTH)) {
            return Optional.of((BlockState) blockState.withProperty(BlockWall.SOUTH, (Boolean) value));
        } else if (key.equals(Keys.CONNECTED_WEST)) {
            return Optional.of((BlockState) blockState.withProperty(BlockWall.WEST, (Boolean) value));
        }
        return super.bridge$getStateWithValue(blockState, key, value);
    }

    private ImmutableDisarmedData impl$getIsDisarmedFor(final IBlockState blockState) {
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeDisarmedData.class, blockState.getValue(BlockTripWire.DISARMED));
    }

    private ImmutableAttachedData impl$getIsAttachedFor(final IBlockState blockState) {
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeAttachedData.class, blockState.getValue(BlockTripWire.ATTACHED));
    }

    private ImmutablePoweredData impl$getIsPoweredFor(final IBlockState blockState) {
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongePoweredData.class, blockState.getValue(BlockTripWire.POWERED));
    }

    private ImmutableConnectedDirectionData impl$getConnectedDirectionData(final IBlockState blockState) {
        final Set<Direction> directions = EnumSet.noneOf(Direction.class);
        final Boolean north = blockState.getValue(BlockTripWire.NORTH);
        final Boolean east = blockState.getValue(BlockTripWire.EAST);
        final Boolean west = blockState.getValue(BlockTripWire.WEST);
        final Boolean south = blockState.getValue(BlockTripWire.SOUTH);
        if (north) {
            directions.add(Direction.NORTH);
        }
        if (south) {
            directions.add(Direction.SOUTH);
        }
        if (west) {
            directions.add(Direction.WEST);
        }
        if (east) {
            directions.add(Direction.EAST);
        }
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeConnectedDirectionData.class, directions);
    }
}
