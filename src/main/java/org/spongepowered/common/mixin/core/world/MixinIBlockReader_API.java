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
package org.spongepowered.common.mixin.core.world;

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.block.state.IBlockState;
import net.minecraft.fluid.IFluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.fluid.FluidState;
import org.spongepowered.api.world.volume.Volume;
import org.spongepowered.api.world.volume.composite.PrimitiveGameVolume;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.block.BlockUtil;
import org.spongepowered.common.fluid.FluidUtil;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(IBlockReader.class)
public interface MixinIBlockReader_API extends PrimitiveGameVolume {

    @Shadow @Nullable net.minecraft.tileentity.TileEntity getTileEntity(BlockPos pos);
    @Shadow IBlockState getBlockState(BlockPos pos);
    @Shadow IFluidState getFluidState(BlockPos pos);

    @Override
    default Optional<TileEntity> getTileEntity(int x, int y, int z) {
        return Optional.ofNullable((TileEntity) this.getTileEntity(new BlockPos(x, y, z)));
    }

    @Override
    default BlockState getBlock(int x, int y, int z) {
        return BlockUtil.fromNative(getBlockState(new BlockPos(x, y, z)));
    }

    @Override
    default FluidState getFluidState(int x, int y, int z) {
        return FluidUtil.fromNative(getFluidState(new BlockPos(x, y, z)));
    }

    @Override
    default Vector3i getBlockMin() {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IBlockReader that isn't part of Sponge API");
    }

    @Override
    default Vector3i getBlockMax() {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IBlockReader that isn't part of Sponge API");
    }

    @Override
    default Vector3i getBlockSize() {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IBlockReader that isn't part of Sponge API");
    }

    @Override
    default boolean containsBlock(int x, int y, int z) {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IBlockReader that isn't part of Sponge API");
    }

    @Override
    default boolean isAreaAvailable(int x, int y, int z) {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IBlockReader that isn't part of Sponge API");
    }
    @Override
    default Volume getView(Vector3i newMin, Vector3i newMax) {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IBlockReader that isn't part of Sponge API");
    }
}
