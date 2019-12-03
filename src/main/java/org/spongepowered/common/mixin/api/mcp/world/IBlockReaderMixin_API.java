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
package org.spongepowered.common.mixin.api.mcp.world;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.IFluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.fluid.FluidState;
import org.spongepowered.api.world.volume.block.ImmutableBlockVolume;
import org.spongepowered.api.world.volume.block.UnmodifiableBlockVolume;
import org.spongepowered.api.world.volume.game.PrimitiveGameVolume;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(IBlockReader.class)
public interface IBlockReaderMixin_API extends PrimitiveGameVolume {
    @Shadow @Nullable TileEntity shadow$getTileEntity(BlockPos p_175625_1_);
    @Shadow BlockState shadow$getBlockState(BlockPos p_180495_1_);
    @Shadow IFluidState shadow$getFluidState(BlockPos p_204610_1_);
    @Shadow int shadow$getLightValue(BlockPos p_217298_1_);
    @Shadow int shadow$getMaxLightLevel();
    @Shadow int shadow$getHeight();

    @Override
    default PrimitiveGameVolume getView(Vector3i newMin, Vector3i newMax) {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IBlockReader that isn't part of Sponge API");
    }

    @Override
    default int getMaximumLight() {
        return shadow$getMaxLightLevel();
    }

    @Override
    default int getEmittedLight(Vector3i pos) {
        return shadow$getLightValue(new BlockPos(pos.getX(), pos.getY(), pos.getZ()));
    }

    @Override
    default int getEmittedLight(int x, int y, int z) {
        return shadow$getLightValue(new BlockPos(x, y, z));
    }

    @Override
    default int getHeight() {
        return shadow$getHeight();
    }

    @Override
    default Collection<? extends BlockEntity> getBlockEntities() {
        return Collections.emptyList();
    }

    @Override
    default Optional<? extends BlockEntity> getBlockEntity(int x, int y, int z) {
        return Optional.ofNullable((BlockEntity) shadow$getTileEntity(new BlockPos(x, y, z)));
    }

    @Override
    default org.spongepowered.api.block.BlockState getBlock(int x, int y, int z) {
        return (org.spongepowered.api.block.BlockState) shadow$getBlockState(new BlockPos(x, y, z));
    }

    @Override
    default FluidState getFluid(int x, int y, int z) {
        return (FluidState) shadow$getFluidState(new BlockPos(x, y, z));
    }

    @Override
    default UnmodifiableBlockVolume<?> asUnmodifiableBlockVolume() {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IBlockReader that isn't part of Sponge API");
    }

    @Override
    default ImmutableBlockVolume asImmutableBlockVolume() {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IBlockReader that isn't part of Sponge API");
    }

    @Override
    default int getHighestYAt(int x, int z) {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IBlockReader that isn't part of Sponge API");
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
}
