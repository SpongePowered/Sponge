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
import net.minecraft.block.BlockSponge;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.manipulator.immutable.ImmutableWetData;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeWetData;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.List;
import java.util.Optional;

@Mixin(BlockSponge.class)
public abstract class MixinBlockSponge extends MixinBlock {

    private ImmutableWetData getWetData(IBlockState blockState) {
        final boolean isWet = blockState.getValue(BlockSponge.WET);
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeWetData.class, isWet);
    }

    @Override
    public boolean supports(Class<? extends ImmutableDataManipulator<?, ?>> immutable) {
        return super.supports(immutable) || ImmutableWetData.class.isAssignableFrom(immutable);
    }

    @Override
    public Optional<BlockState> getStateWithData(IBlockState blockState, ImmutableDataManipulator<?, ?> manipulator) {
        if (manipulator instanceof ImmutableWetData) {
            final boolean isWet = ((ImmutableWetData) manipulator).wet().get();
            return Optional.of((BlockState) blockState.withProperty(BlockSponge.WET, isWet));
        }
        return super.getStateWithData(blockState, manipulator);
    }

    @Override
    public <E> Optional<BlockState> getStateWithValue(IBlockState blockState, Key<? extends BaseValue<E>> key, E value) {
        if (key.equals(Keys.IS_WET)) {
            final boolean isWet = (Boolean) value;
            return Optional.of((BlockState) blockState.withProperty(BlockSponge.WET, isWet));
        }
        return super.getStateWithValue(blockState, key, value);
    }

    @Override
    public List<ImmutableDataManipulator<?, ?>> getManipulators(IBlockState blockState) {
        return ImmutableList.of(getWetData(blockState));
    }

    @Override
    public Translation getTranslation() {
        return new SpongeTranslation(getTranslationKey() + ".dry.name");
    }
}
