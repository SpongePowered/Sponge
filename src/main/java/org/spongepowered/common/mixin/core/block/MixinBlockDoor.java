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
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.state.properties.DoubleBlockHalf;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.manipulator.immutable.ImmutableDirectionalData;
import org.spongepowered.api.data.manipulator.immutable.ImmutableHingeData;
import org.spongepowered.api.data.manipulator.immutable.ImmutableOpenData;
import org.spongepowered.api.data.manipulator.immutable.ImmutablePortionData;
import org.spongepowered.api.data.manipulator.immutable.ImmutablePoweredData;
import org.spongepowered.api.data.type.Hinge;
import org.spongepowered.api.data.type.PortionType;
import org.spongepowered.api.data.type.PortionTypes;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.util.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeDirectionalData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeHingeData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeOpenData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongePortionData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongePoweredData;
import org.spongepowered.common.data.util.DirectionChecker;
import org.spongepowered.common.data.util.DirectionResolver;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Optional;

@Mixin(BlockDoor.class)
public abstract class MixinBlockDoor extends MixinBlock {

    @Override
    public ImmutableList<ImmutableDataManipulator<?, ?>> getManipulators(IBlockState blockState) {
        return ImmutableList.<ImmutableDataManipulator<?, ?>>of(getHingeFor(blockState), getIsOpenFor(blockState),
                getIsPoweredFor(blockState), getDirectionalData(blockState), getPortionData(blockState));
    }

    @Override
    public boolean supports(Class<? extends ImmutableDataManipulator<?, ?>> immutable) {
        return ImmutableHingeData.class.isAssignableFrom(immutable) || ImmutableOpenData.class.isAssignableFrom(immutable)
                || ImmutablePoweredData.class.isAssignableFrom(immutable) || ImmutablePortionData.class.isAssignableFrom(immutable);
    }

    @Override
    public Optional<BlockState> getStateWithData(IBlockState blockState, ImmutableDataManipulator<?, ?> manipulator) {
        if (manipulator instanceof ImmutableHingeData) {
            final DoorHingeSide hinge = (DoorHingeSide) (Object) ((ImmutableHingeData) manipulator).type().get();
            return Optional.of((BlockState) blockState.with(BlockDoor.HINGE, hinge));
        }
        if (manipulator instanceof ImmutableOpenData) {
            final boolean isOpen = ((ImmutableOpenData) manipulator).open().get();
            return Optional.of((BlockState) blockState.with(BlockDoor.OPEN, isOpen));
        }
        if (manipulator instanceof ImmutablePoweredData) {
            return Optional.of((BlockState) blockState.with(BlockDoor.POWERED, ((ImmutablePoweredData) manipulator).powered().get()));
        }
        if (manipulator instanceof ImmutableDirectionalData) {
            final Direction dir = DirectionChecker.checkDirectionToHorizontal(((ImmutableDirectionalData) manipulator).direction().get());
            return Optional.of((BlockState) blockState.with(BlockDoor.FACING, DirectionResolver.getFor(dir)));
        }
        if (manipulator instanceof ImmutablePortionData) {
            return Optional.of((BlockState) blockState.with(BlockDoor.HALF,
                    convertPortionType(((ImmutablePortionData) manipulator).type().get())));
        }
        return super.getStateWithData(blockState, manipulator);
    }

    @Override
    public <E> Optional<BlockState> getStateWithValue(IBlockState blockState, Key<? extends Value<E>> key, E value) {
        if (key.equals(Keys.HINGE_POSITION)) {
            final DoorHingeSide hinge = (DoorHingeSide) value;
            return Optional.of((BlockState) blockState.with(BlockDoor.HINGE, hinge));
        }
        if (key.equals(Keys.OPEN)) {
            final boolean isOpen = (Boolean) value;
            return Optional.of((BlockState) blockState.with(BlockDoor.OPEN, isOpen));
        }
        if (key.equals(Keys.POWERED)) {
            return Optional.of((BlockState) blockState.with(BlockDoor.POWERED, (Boolean) value));
        }
        if (key.equals(Keys.DIRECTION)) {
            final Direction dir = DirectionChecker.checkDirectionToHorizontal((Direction) value);
            return Optional.of((BlockState) blockState.with(BlockDoor.FACING, DirectionResolver.getFor(dir)));
        }
        if (key.equals(Keys.PORTION_TYPE)) {
            return Optional.of((BlockState) blockState.with(BlockDoor.HALF, convertPortionType((PortionType) value)));
        }
        return super.getStateWithValue(blockState, key, value);
    }

    private DoubleBlockHalf convertPortionType(PortionType portionType) {
        return portionType == PortionTypes.BOTTOM ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER;
    }

    private ImmutableHingeData getHingeFor(IBlockState blockState) {
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeHingeData.class, (Hinge) (Object) blockState.get(BlockDoor.HINGE));
    }

    private ImmutableOpenData getIsOpenFor(IBlockState blockState) {
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeOpenData.class, blockState.get(BlockDoor.OPEN));
    }

    private ImmutablePoweredData getIsPoweredFor(IBlockState blockState) {
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongePoweredData.class, blockState.get(BlockDoor.POWERED));
    }

    private ImmutableDirectionalData getDirectionalData(IBlockState blockState) {
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeDirectionalData.class,
                DirectionResolver.getFor(blockState.get(BlockDoor.FACING)));
    }

    private ImmutablePortionData getPortionData(IBlockState blockState) {
        DoubleBlockHalf half = blockState.get(BlockDoor.HALF);
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongePortionData.class,
                half == DoubleBlockHalf.LOWER ? PortionTypes.BOTTOM : PortionTypes.TOP);
    }

    @Override
    public Translation getTranslation() {
        return new SpongeTranslation(this.getTranslationKey().replaceAll("tile", "item") + ".name");
    }
}
