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
import net.minecraft.block.BlockDirt;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableDirtData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableSnowedData;
import org.spongepowered.api.data.type.DirtType;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeDirtData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeSnowedData;

import java.util.Optional;

@Mixin(BlockDirt.class)
public abstract class BlockDirtMixin extends BlockMixin {

    @SuppressWarnings("RedundantTypeArguments") // some java compilers will not calculate this generic correctly
    @Override
    public ImmutableList<ImmutableDataManipulator<?, ?>> bridge$getManipulators(final IBlockState blockState) {
        return ImmutableList.<ImmutableDataManipulator<?, ?>>of(impl$getDirtTypeFor(blockState), impl$getIsSnowedFor(blockState));
    }

    @Override
    public boolean bridge$supports(final Class<? extends ImmutableDataManipulator<?, ?>> immutable) {
        return ImmutableDirtData.class.isAssignableFrom(immutable) || ImmutableSnowedData.class.isAssignableFrom(immutable);
    }

    @Override
    public Optional<BlockState> bridge$getStateWithData(final IBlockState blockState, final ImmutableDataManipulator<?, ?> manipulator) {
        if (manipulator instanceof ImmutableDirtData) {
            final BlockDirt.DirtType dirtType = (BlockDirt.DirtType) (Object) ((ImmutableDirtData) manipulator).type().get();
            return Optional.of((BlockState) blockState.func_177226_a(BlockDirt.field_176386_a, dirtType));
        }
        if (manipulator instanceof ImmutableSnowedData) {
            return Optional.of((BlockState) blockState);
        }
        return super.bridge$getStateWithData(blockState, manipulator);
    }

    @Override
    public <E> Optional<BlockState> bridge$getStateWithValue(final IBlockState blockState, final Key<? extends BaseValue<E>> key, final E value) {
        if (key.equals(Keys.DIRT_TYPE)) {
            final BlockDirt.DirtType dirtType = (BlockDirt.DirtType) value;
            return Optional.of((BlockState) blockState.func_177226_a(BlockDirt.field_176386_a, dirtType));
        }
        if (key.equals(Keys.SNOWED)) {
            return Optional.of((BlockState) blockState);
        }
        return super.bridge$getStateWithValue(blockState, key, value);
    }

    private ImmutableDirtData impl$getDirtTypeFor(final IBlockState blockState) {
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeDirtData.class, (DirtType) (Object) blockState.func_177229_b(BlockDirt.field_176386_a));
    }

    private ImmutableSnowedData impl$getIsSnowedFor(final IBlockState blockState) {
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeSnowedData.class, blockState.func_177229_b(BlockDirt.field_176385_b));
    }
}
