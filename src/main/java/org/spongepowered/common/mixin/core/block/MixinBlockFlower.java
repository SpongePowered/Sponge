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
import net.minecraft.block.BlockFlower;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.manipulator.block.FlowerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.processor.block.SpongePlantProcessor;

import java.util.Collection;

@Mixin(BlockFlower.class)
public abstract class MixinBlockFlower extends MixinBlock {

    @Override
    public Collection<DataManipulator<?>> getManipulators(World world, BlockPos blockPos) {
        return getManipulators(world.getBlockState(blockPos));
    }

    @Override
    public ImmutableList<DataManipulator<?>> getManipulators(IBlockState blockState) {
        SpongePlantProcessor processor = (SpongePlantProcessor) Sponge.getSpongeRegistry().getManipulatorRegistry().getBuilder(FlowerData.class).get();
        return ImmutableList.<DataManipulator<?>>of(processor.createFrom(blockState).get());
    }
}
