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
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableConnectedDirectionData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableRedstonePoweredData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableWireAttachmentData;
import org.spongepowered.api.data.type.WireAttachmentType;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.util.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeConnectedDirectionData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeRedstonePoweredData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeWireAttachmentData;
import org.spongepowered.common.util.Constants;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Mixin(BlockRedstoneWire.class)
public abstract class BlockRedstoneWireMixin extends BlockMixin {

    @Override
    public ImmutableList<ImmutableDataManipulator<?, ?>> bridge$getManipulators(final IBlockState blockState) {
        return ImmutableList.of(impl$getPowerFor(blockState), impl$getConnectedDirectionData(blockState), impl$getWireAttachmentData(blockState));
    }

    @Override
    public boolean bridge$supports(final Class<? extends ImmutableDataManipulator<?, ?>> immutable) {
        return ImmutableRedstonePoweredData.class.isAssignableFrom(immutable) || ImmutableConnectedDirectionData.class.isAssignableFrom(immutable)
                || ImmutableWireAttachmentData.class.isAssignableFrom(immutable);
    }

    private IBlockState impl$applyConnectedDirections(final IBlockState blockState, final Set<Direction> directions) {
        final Map<PropertyEnum<BlockRedstoneWire.EnumAttachPosition>, BlockRedstoneWire.EnumAttachPosition> facingStates = new HashMap<>();
        for (PropertyEnum<BlockRedstoneWire.EnumAttachPosition> property : Constants.DirectionFunctions.RedstoneWire.ALL_DIRECTION_PROPERTIES) {
            facingStates.put(property, BlockRedstoneWire.EnumAttachPosition.NONE);
        }
        for (Direction connectedDirection : directions) {
            final Optional<PropertyEnum<BlockRedstoneWire.EnumAttachPosition>> facingPropertyBox =
                    Constants.DirectionFunctions.RedstoneWire.getPropertyFromDirection(connectedDirection);
            facingPropertyBox.ifPresent(facingProperty -> facingStates.put(facingProperty, blockState.getValue(facingProperty)));
        }
        IBlockState resultBlockState = blockState;
        for (PropertyEnum<BlockRedstoneWire.EnumAttachPosition> property : facingStates.keySet()) {
            resultBlockState = resultBlockState.withProperty(property, facingStates.get(property));
        }
        return resultBlockState;
    }

    @Override
    public Optional<BlockState> bridge$getStateWithData(final IBlockState blockState, final ImmutableDataManipulator<?, ?> manipulator) {
        if (manipulator instanceof ImmutableRedstonePoweredData) {
            return Optional.of((BlockState) blockState);
        }
        if (manipulator instanceof ImmutableConnectedDirectionData) {
            final ImmutableConnectedDirectionData connectedDirectionData = (ImmutableConnectedDirectionData) manipulator;
            return Optional.of((BlockState) impl$applyConnectedDirections(blockState, connectedDirectionData.connectedDirections().get()));
        }
        if (manipulator instanceof ImmutableWireAttachmentData) {
            return Optional.of((BlockState) blockState);
        }
        return super.bridge$getStateWithData(blockState, manipulator);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> Optional<BlockState> bridge$getStateWithValue(final IBlockState blockState, final Key<? extends BaseValue<E>> key, final E value) {
        if (key.equals(Keys.POWER)) {
            return Optional.of((BlockState) blockState);
        }
        if (key.equals(Keys.WIRE_ATTACHMENTS) || key.equals(Keys.WIRE_ATTACHMENT_NORTH) || key.equals(Keys.WIRE_ATTACHMENT_SOUTH)
                || key.equals(Keys.WIRE_ATTACHMENT_EAST) || key.equals(Keys.WIRE_ATTACHMENT_WEST)) {
            return Optional.of((BlockState) blockState);
        }
        if (key.equals(Keys.CONNECTED_DIRECTIONS)) {
            return Optional.of((BlockState) impl$applyConnectedDirections(blockState, (Set<Direction>) value));
        } else if (key.equals(Keys.CONNECTED_EAST)) {
            return Optional.of((BlockState) blockState.withProperty(BlockRedstoneWire.EAST, blockState.getValue(BlockRedstoneWire.EAST)));
        } else if (key.equals(Keys.CONNECTED_NORTH)) {
            return Optional.of((BlockState) blockState.withProperty(BlockRedstoneWire.NORTH, blockState.getValue(BlockRedstoneWire.NORTH)));
        } else if (key.equals(Keys.CONNECTED_SOUTH)) {
            return Optional.of((BlockState) blockState.withProperty(BlockRedstoneWire.SOUTH, blockState.getValue(BlockRedstoneWire.SOUTH)));
        } else if (key.equals(Keys.CONNECTED_WEST)) {
            return Optional.of((BlockState) blockState.withProperty(BlockRedstoneWire.WEST, blockState.getValue(BlockRedstoneWire.WEST)));
        }
        return super.bridge$getStateWithValue(blockState, key, value);
    }

    private ImmutableRedstonePoweredData impl$getPowerFor(final IBlockState blockState) {
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeRedstonePoweredData.class, blockState.getValue(BlockRedstoneWire.POWER));
    }

    private ImmutableConnectedDirectionData impl$getConnectedDirectionData(final IBlockState blockState) {
        final Set<Direction> directions = EnumSet.noneOf(Direction.class);
        final IStringSerializable north = blockState.getValue(BlockRedstoneWire.NORTH);
        final IStringSerializable east = blockState.getValue(BlockRedstoneWire.EAST);
        final IStringSerializable west = blockState.getValue(BlockRedstoneWire.WEST);
        final IStringSerializable south = blockState.getValue(BlockRedstoneWire.SOUTH);
        if (!north.getName().matches("none")) {
            directions.add(Direction.NORTH);
        }
        if (!east.getName().matches("none")) {
            directions.add(Direction.EAST);
        }
        if (!west.getName().matches("none")) {
            directions.add(Direction.WEST);
        }
        if (!south.getName().matches("none")) {
            directions.add(Direction.SOUTH);
        }
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeConnectedDirectionData.class, directions);
    }

    @SuppressWarnings("ConstantConditions")
    private ImmutableWireAttachmentData impl$getWireAttachmentData(final IBlockState blockState) {
        final Map<Direction, WireAttachmentType> data = new EnumMap<>(Direction.class);
        data.put(Direction.NORTH, (WireAttachmentType) (Object) blockState.getValue(BlockRedstoneWire.NORTH));
        data.put(Direction.SOUTH, (WireAttachmentType) (Object) blockState.getValue(BlockRedstoneWire.SOUTH));
        data.put(Direction.EAST, (WireAttachmentType) (Object) blockState.getValue(BlockRedstoneWire.EAST));
        data.put(Direction.WEST, (WireAttachmentType) (Object) blockState.getValue(BlockRedstoneWire.WEST));
        return new ImmutableSpongeWireAttachmentData(data);
    }
}
