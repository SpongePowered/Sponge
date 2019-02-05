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
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableDoublePlantData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutablePortionData;
import org.spongepowered.api.data.type.DoublePlantType;
import org.spongepowered.api.data.type.PortionType;
import org.spongepowered.api.data.type.PortionTypes;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeDoublePlantData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongePortionData;

import java.util.Optional;

@Mixin(BlockDoublePlant.class)
public abstract class MixinBlockDoublePlant extends MixinBlock {

    @Override
    public ImmutableList<ImmutableDataManipulator<?, ?>> getManipulators(IBlockState blockState) {
        return ImmutableList.<ImmutableDataManipulator<?, ?>>of(getDoublePlantTypeFor(blockState), getPortionData(blockState));
    }

    @Override
    public boolean supports(Class<? extends ImmutableDataManipulator<?, ?>> immutable) {
        return ImmutableDoublePlantData.class.isAssignableFrom(immutable) || ImmutablePortionData.class.isAssignableFrom(immutable);
    }

    @Override
    public Optional<BlockState> getStateWithData(IBlockState blockState, ImmutableDataManipulator<?, ?> manipulator) {
        if (manipulator instanceof ImmutableDoublePlantData) {
            final BlockDoublePlant.EnumPlantType doublePlantType =
                    (BlockDoublePlant.EnumPlantType) (Object) ((ImmutableDoublePlantData) manipulator).type().get();
            return Optional.of((BlockState) blockState.withProperty(BlockDoublePlant.VARIANT, doublePlantType));
        } else if (manipulator instanceof ImmutablePortionData) {
            return Optional.of((BlockState) blockState.withProperty(BlockDoublePlant.HALF,
                    convertPortionType(((ImmutablePortionData) manipulator).type().get())));
        }
        return super.getStateWithData(blockState, manipulator);
    }

    @Override
    public <E> Optional<BlockState> getStateWithValue(IBlockState blockState, Key<? extends Value<E>> key, E value) {
        if (key.equals(Keys.DOUBLE_PLANT_TYPE)) {
            final BlockDoublePlant.EnumPlantType doublePlantType = (BlockDoublePlant.EnumPlantType) value;
            return Optional.of((BlockState) blockState.withProperty(BlockDoublePlant.VARIANT, doublePlantType));
        }
        if (key.equals(Keys.PORTION_TYPE)) {
            return Optional.of((BlockState) blockState.withProperty(BlockDoublePlant.HALF, convertPortionType((PortionType) value)));
        }
        return super.getStateWithValue(blockState, key, value);
    }

    private BlockDoublePlant.EnumBlockHalf convertPortionType(PortionType portionType) {
        return portionType == PortionTypes.BOTTOM ? BlockDoublePlant.EnumBlockHalf.LOWER : BlockDoublePlant.EnumBlockHalf.UPPER;
    }

    private ImmutableDoublePlantData getDoublePlantTypeFor(IBlockState blockState) {
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeDoublePlantData.class,
                (DoublePlantType) (Object) blockState.getValue(BlockDoublePlant.VARIANT));
    }

    private ImmutablePortionData getPortionData(IBlockState blockState) {
        BlockDoublePlant.EnumBlockHalf half = blockState.getValue(BlockDoublePlant.HALF);
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongePortionData.class,
                half == BlockDoublePlant.EnumBlockHalf.LOWER ? PortionTypes.BOTTOM : PortionTypes.TOP);
    }
}
