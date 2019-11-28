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
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableDoublePlantData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutablePortionData;
import org.spongepowered.api.data.type.DoublePlantType;
import org.spongepowered.api.data.type.PortionType;
import org.spongepowered.api.data.type.PortionTypes;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeDoublePlantData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongePortionData;

import java.util.Optional;
import net.minecraft.block.DoublePlantBlock;

@Mixin(DoublePlantBlock.class)
public abstract class BlockDoublePlantMixin extends BlockMixin {

    @SuppressWarnings("RedundantTypeArguments") // some java compilers will not calculate this generic correctly
    @Override
    public ImmutableList<ImmutableDataManipulator<?, ?>> bridge$getManipulators(final net.minecraft.block.BlockState blockState) {
        return ImmutableList.<ImmutableDataManipulator<?, ?>>of(impl$getDoublePlantTypeFor(blockState), impl$getPortionData(blockState));
    }

    @Override
    public boolean bridge$supports(final Class<? extends ImmutableDataManipulator<?, ?>> immutable) {
        return ImmutableDoublePlantData.class.isAssignableFrom(immutable) || ImmutablePortionData.class.isAssignableFrom(immutable);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Optional<BlockState> bridge$getStateWithData(final net.minecraft.block.BlockState blockState, final ImmutableDataManipulator<?, ?> manipulator) {
        if (manipulator instanceof ImmutableDoublePlantData) {
            final DoublePlantBlock.EnumPlantType doublePlantType =
                    (DoublePlantBlock.EnumPlantType) (Object) ((ImmutableDoublePlantData) manipulator).type().get();
            return Optional.of((BlockState) blockState.func_177226_a(DoublePlantBlock.field_176493_a, doublePlantType));
        } else if (manipulator instanceof ImmutablePortionData) {
            return Optional.of((BlockState) blockState.func_177226_a(DoublePlantBlock.HALF,
                    impl$convertPortionType(((ImmutablePortionData) manipulator).type().get())));
        }
        return super.bridge$getStateWithData(blockState, manipulator);
    }

    @Override
    public <E> Optional<BlockState> bridge$getStateWithValue(final net.minecraft.block.BlockState blockState, final Key<? extends BaseValue<E>> key, final E value) {
        if (key.equals(Keys.DOUBLE_PLANT_TYPE)) {
            final DoublePlantBlock.EnumPlantType doublePlantType = (DoublePlantBlock.EnumPlantType) value;
            return Optional.of((BlockState) blockState.func_177226_a(DoublePlantBlock.field_176493_a, doublePlantType));
        }
        if (key.equals(Keys.PORTION_TYPE)) {
            return Optional.of((BlockState) blockState.func_177226_a(DoublePlantBlock.HALF, impl$convertPortionType((PortionType) value)));
        }
        return super.bridge$getStateWithValue(blockState, key, value);
    }

    private DoublePlantBlock.EnumBlockHalf impl$convertPortionType(final PortionType portionType) {
        return portionType == PortionTypes.BOTTOM ? DoublePlantBlock.EnumBlockHalf.LOWER : DoublePlantBlock.EnumBlockHalf.UPPER;
    }

    @SuppressWarnings("ConstantConditions")
    private ImmutableDoublePlantData impl$getDoublePlantTypeFor(final net.minecraft.block.BlockState blockState) {
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeDoublePlantData.class,
                (DoublePlantType) (Object) blockState.get(DoublePlantBlock.field_176493_a));
    }

    private ImmutablePortionData impl$getPortionData(final net.minecraft.block.BlockState blockState) {
        final DoublePlantBlock.EnumBlockHalf half = blockState.get(DoublePlantBlock.HALF);
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongePortionData.class,
                half == DoublePlantBlock.EnumBlockHalf.LOWER ? PortionTypes.BOTTOM : PortionTypes.TOP);
    }
}
