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
package org.spongepowered.common.interfaces.block;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.data.BlockDataProcessor;

/**
 * A quasi interface to mix into every possible {@link Block} such that their
 * acceptable {@link BlockState}s can be created, manipulated, and applied
 * with the safety of using these instance checks of the {@link IMixinBlock}.
 * The advantage of this is that a simple cast from {@link Block} to a
 * particular {@link IMixinBlock} to take advantage of particular {@link Value}
 * types, such as {@link IMixinBlockDirectional}, are really simple to perform.
 *
 * <p>It is important to note that when using this level of implementation,
 * it is already guaranteed that a particular {@link IMixinBlock} is capable
 * of a particular type thanks to {@link Mixin}s. All that is needed to handle
 * a particular type of {@link Value} or {@link ImmutableDataManipulator} is a
 * simple cast. This is particularly useful for {@link BlockDataProcessor}s as
 * they already know the type they need to focus on.</p>
 */
public interface IMixinBlock {

    /**
     * Gets all the applicable {@link ImmutableDataManipulator}s possible for
     * the {@link Block} type at he provided {@link BlockPos} and {@link World}.
     *
     * @param world
     * @param blockPos
     * @return
     */
    ImmutableList<ImmutableDataManipulator<?, ?>> getManipulators(World world, BlockPos blockPos);

    ImmutableList<ImmutableDataManipulator<?, ?>> getManipulators(IBlockState blockState);

    void resetBlockState(World world, BlockPos blockPos);

    BlockState getDefaultBlockState();

    // Automatically implemented by forge due to identical signature
    boolean isFlammable(IBlockAccess blockAccess, BlockPos pos, EnumFacing facing);

}
