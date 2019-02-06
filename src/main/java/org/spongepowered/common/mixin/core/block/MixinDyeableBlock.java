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
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockCarpet;
import net.minecraft.block.BlockConcretePowder;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.manipulator.immutable.ImmutableDyeableData;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeDyeableData;
import org.spongepowered.common.interfaces.block.IMixinDyedBlock;

import java.util.List;

@Mixin({BlockCarpet.class, BlockShulkerBox.class, BlockStainedGlass.class, BlockStainedGlassPane.class, BlockConcretePowder.class, BlockBed.class })
public abstract class MixinDyeableBlock extends MixinBlock implements IMixinDyedBlock {

    @Override
    public List<ImmutableDataManipulator<?, ?>> getManipulators(IBlockState blockState) {
        return ImmutableList.<ImmutableDataManipulator<?, ?>>builder()
                .addAll(super.getManipulators(blockState))
                .add(getDyeableData())
                .build();
    }

    @Override
    public boolean supports(Class<? extends ImmutableDataManipulator<?, ?>> immutable) {
        return ImmutableDyeableData.class.isAssignableFrom(immutable) || super.supports(immutable);
    }

    // TODO: Allow transformations?
    private ImmutableDyeableData getDyeableData() {
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeDyeableData.class, (DyeColor) (Object) getDyeColor());
    }
}
