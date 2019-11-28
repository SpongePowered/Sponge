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
import net.minecraft.block.BlockColored;
import net.minecraft.block.CarpetBlock;
import net.minecraft.block.ConcretePowderBlock;
import net.minecraft.block.StainedGlassBlock;
import net.minecraft.block.StainedGlassPaneBlock;
import net.minecraft.block.properties.PropertyEnum;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.manipulator.immutable.ImmutableDyeableData;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.block.DyeableBlockBridge;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeDyeableData;

import java.util.List;
import java.util.Optional;

@Mixin({CarpetBlock.class, BlockColored.class, StainedGlassBlock.class, StainedGlassPaneBlock.class, ConcretePowderBlock.class})
public abstract class DyeableBlockMixin extends BlockMixin implements DyeableBlockBridge {

    private PropertyEnum<net.minecraft.item.DyeColor> bridge$ColorProperty;

    @Override
    public void bridge$setColorPropertyEnum(final PropertyEnum<net.minecraft.item.DyeColor> property) {
        this.bridge$ColorProperty = property;
    }

    @Override
    public List<ImmutableDataManipulator<?, ?>> bridge$getManipulators(final net.minecraft.block.BlockState blockState) {
        return ImmutableList.<ImmutableDataManipulator<?, ?>>builder()
                .addAll(super.bridge$getManipulators(blockState))
                .add(this.impl$getDyeableData(blockState))
                .build();
    }

    @Override
    public boolean bridge$supports(final Class<? extends ImmutableDataManipulator<?, ?>> immutable) {
        return ImmutableDyeableData.class.isAssignableFrom(immutable) || super.bridge$supports(immutable);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Optional<BlockState> bridge$getStateWithData(final net.minecraft.block.BlockState blockState, final ImmutableDataManipulator<?, ?> manipulator) {
        if (manipulator instanceof ImmutableDyeableData) {
            final DyeColor color = ((ImmutableDyeableData) manipulator).type().get();
            return Optional.of((BlockState) blockState.func_177226_a(this.bridge$ColorProperty, (net.minecraft.item.DyeColor) (Object) color));
        }
        return super.bridge$getStateWithData(blockState, manipulator);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public <E> Optional<BlockState> bridge$getStateWithValue(final net.minecraft.block.BlockState blockState, final Key<? extends BaseValue<E>> key, final E value) {
        if (key.equals(Keys.DYE_COLOR)) {
            final DyeColor color = (DyeColor) value;
            return Optional.of((BlockState) blockState.func_177226_a(this.bridge$ColorProperty, (net.minecraft.item.DyeColor) (Object) color));
        }
        return super.bridge$getStateWithValue(blockState, key, value);
    }

    @SuppressWarnings("ConstantConditions")
    private ImmutableDyeableData impl$getDyeableData(final net.minecraft.block.BlockState blockState) {
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeDyeableData.class,
                (DyeColor) (Object) blockState.func_177229_b(this.bridge$ColorProperty));
    }

}
