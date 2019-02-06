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
import net.minecraft.block.BlockHugeMushroom;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.manipulator.immutable.ImmutableBigMushroomPoresData;
import org.spongepowered.api.data.manipulator.mutable.BigMushroomPoresData;
import org.spongepowered.api.data.value.SetValue;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.util.Direction;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

@Mixin(BlockHugeMushroom.class)
public abstract class MixinBlockHugeMushroom extends MixinBlock {

    @Override
    public ImmutableList<ImmutableDataManipulator<?, ?>> getManipulators(IBlockState blockState) {
        // TODO: BigMushroomPoresData
        return ImmutableList.of();
    }

    @Override
    public boolean supports(Class<? extends ImmutableDataManipulator<?, ?>> immutable) {
        return ImmutableBigMushroomPoresData.class.isAssignableFrom(immutable);
    }

    @Override
    public Optional<BlockState> getStateWithData(IBlockState blockState, ImmutableDataManipulator<?, ?> manipulator) {
        if (manipulator instanceof BigMushroomPoresData) {
            final BigMushroomPoresData data = (BigMushroomPoresData) manipulator;
            return Optional.of((BlockState) blockState
                    .with(BlockHugeMushroom.UP, data.get(Keys.BIG_MUSHROOM_PORES_UP).get())
                    .with(BlockHugeMushroom.DOWN, data.get(Keys.BIG_MUSHROOM_PORES_DOWN).get())
                    .with(BlockHugeMushroom.EAST, data.get(Keys.BIG_MUSHROOM_PORES_EAST).get())
                    .with(BlockHugeMushroom.WEST, data.get(Keys.BIG_MUSHROOM_PORES_WEST).get())
                    .with(BlockHugeMushroom.NORTH, data.get(Keys.BIG_MUSHROOM_PORES_NORTH).get())
                    .with(BlockHugeMushroom.SOUTH, data.get(Keys.BIG_MUSHROOM_PORES_SOUTH).get()));
        }
        return super.getStateWithData(blockState, manipulator);
    }

    @Override
    public <E> Optional<BlockState> getStateWithValue(IBlockState blockState, Key<? extends Value<E>> key, E value) {
        if (key.equals(Keys.BIG_MUSHROOM_PORES_UP)) {
            return Optional.of((BlockState) blockState.with(BlockHugeMushroom.UP, (Boolean) value));
        } else if (key.equals(Keys.BIG_MUSHROOM_PORES_DOWN)) {
            return Optional.of((BlockState) blockState.with(BlockHugeMushroom.DOWN, (Boolean) value));
        } else if (key.equals(Keys.BIG_MUSHROOM_PORES_EAST)) {
            return Optional.of((BlockState) blockState.with(BlockHugeMushroom.EAST, (Boolean) value));
        } else if (key.equals(Keys.BIG_MUSHROOM_PORES_WEST)) {
            return Optional.of((BlockState) blockState.with(BlockHugeMushroom.WEST, (Boolean) value));
        } else if (key.equals(Keys.BIG_MUSHROOM_PORES_NORTH)) {
            return Optional.of((BlockState) blockState.with(BlockHugeMushroom.NORTH, (Boolean) value));
        } else if (key.equals(Keys.BIG_MUSHROOM_PORES_SOUTH)) {
            return Optional.of((BlockState) blockState.with(BlockHugeMushroom.SOUTH, (Boolean) value));
        } else if (key.equals(Keys.BIG_MUSHROOM_PORES)) {
            final SetValue<Direction> directions = (SetValue<Direction>) value;
            return Optional.of((BlockState) blockState
                    .with(BlockHugeMushroom.UP, directions.contains(Direction.UP))
                    .with(BlockHugeMushroom.DOWN, directions.contains(Direction.DOWN))
                    .with(BlockHugeMushroom.EAST, directions.contains(Direction.EAST))
                    .with(BlockHugeMushroom.WEST, directions.contains(Direction.WEST))
                    .with(BlockHugeMushroom.NORTH, directions.contains(Direction.NORTH))
                    .with(BlockHugeMushroom.SOUTH, directions.contains(Direction.SOUTH)));
        }
        return super.getStateWithValue(blockState, key, value);
    }
}
