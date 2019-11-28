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
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableComparatorData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutablePoweredData;
import org.spongepowered.api.data.type.ComparatorType;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeComparatorData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongePoweredData;

import java.util.Optional;
import net.minecraft.block.ComparatorBlock;

@Mixin(ComparatorBlock.class)
public abstract class BlockRedstoneComparatorMixin extends BlockMixin {

    @Override
    public ImmutableList<ImmutableDataManipulator<?, ?>> bridge$getManipulators(final net.minecraft.block.BlockState blockState) {
        return ImmutableList.<ImmutableDataManipulator<?, ?>>builder()
                .addAll(super.bridge$getManipulators(blockState))
                .add(impl$getComparatorTypeFor(blockState))
                .add(impl$getIsPoweredFor(blockState))
                .build();
    }

    @Override
    public boolean bridge$supports(final Class<? extends ImmutableDataManipulator<?, ?>> immutable) {
        return super.bridge$supports(immutable) || ImmutableComparatorData.class.isAssignableFrom(immutable) || ImmutablePoweredData.class.isAssignableFrom(immutable);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Optional<BlockState> bridge$getStateWithData(final net.minecraft.block.BlockState blockState, final ImmutableDataManipulator<?, ?> manipulator) {
        if (manipulator instanceof ImmutableComparatorData) {
            final ComparatorBlock.Mode comparatorType =
                    (ComparatorBlock.Mode) (Object) ((ImmutableComparatorData) manipulator).type().get();
            return Optional.of((BlockState) blockState.withProperty(ComparatorBlock.MODE, comparatorType));
        }
        if (manipulator instanceof ImmutablePoweredData) {
            return Optional.of((BlockState) blockState.withProperty(ComparatorBlock.POWERED, ((ImmutablePoweredData) manipulator).powered()
                    .get()));
        }
        return super.bridge$getStateWithData(blockState, manipulator);
    }

    @Override
    public <E> Optional<BlockState> bridge$getStateWithValue(final net.minecraft.block.BlockState blockState, final Key<? extends BaseValue<E>> key, final E value) {
        if (key.equals(Keys.COMPARATOR_TYPE)) {
            final ComparatorBlock.Mode comparatorType = (ComparatorBlock.Mode) value;
            return Optional.of((BlockState) blockState.withProperty(ComparatorBlock.MODE, comparatorType));
        }
        if (key.equals(Keys.POWERED)) {
            return Optional.of((BlockState) blockState.withProperty(ComparatorBlock.POWERED, (Boolean) value));
        }
        return super.bridge$getStateWithValue(blockState, key, value);
    }

    @SuppressWarnings("ConstantConditions")
    private ImmutableComparatorData impl$getComparatorTypeFor(final net.minecraft.block.BlockState blockState) {
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeComparatorData.class,
                (ComparatorType) (Object) blockState.get(ComparatorBlock.MODE));
    }

    private ImmutablePoweredData impl$getIsPoweredFor(final net.minecraft.block.BlockState blockState) {
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongePoweredData.class, blockState.get(ComparatorBlock.POWERED));
    }

}
