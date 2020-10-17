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
package org.spongepowered.common.mixin.optimization.mcp.world;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.IFluidState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.IBlockReaderBridge;

@Mixin(value = IBlockReader.class, priority = 1500)
public interface IBlockReaderMixin_RayTraceChunkLoadOptimizations {

    // @formatter:off
    @Shadow BlockState shadow$getBlockState(BlockPos pos);
    @Shadow IFluidState shadow$getFluidState(BlockPos pos);
    @Shadow BlockRayTraceResult shadow$rayTraceBlocks(Vec3d startVec, Vec3d endVec, BlockPos pos, VoxelShape shape, BlockState state);
    // @formatter:on

    /**
     * Can be modified later when
     * <a href="https://github.com/SpongePowered/Mixin/issues/355">the mixin feature</a>
     * is resolved.
     * <p>
     * Check if a chunk is loaded before attempting to check the state of the block
     * return null if the chunk is not loaded. Based on
     * <a href="https://github.com/PaperMC/Paper/blob/ver/1.15.2/Spigot-Server-Patches/0331-Prevent-rayTrace-from-loading-chunks.patch#L16">a Paper patch</a>
     *
     * @author gabizou - Minecraft 1.15.2 - October 16th, 2020
     * @reason Because this patch requires a lambda injection, I don't want
     * to risk the breakages that can be caused by injecting into an interface.
     */
    @Overwrite
    default BlockRayTraceResult rayTraceBlocks(final RayTraceContext context) {
        return IBlockReader.doRayTrace(context, (p_217297_1_, p_217297_2_) -> {

            // Sponge start - check if the blockstate is loaded/null
            // final BlockState blockstate = this.shadow$getBlockState(p_217297_2_); // Vanilla
            final @Nullable BlockState blockstate = ((IBlockReaderBridge) this).bridge$getBlockIfLoaded(p_217297_2_);
            if (blockstate == null) {
                // copied the last function parameter (listed below)
                final Vec3d vec3d = p_217297_1_.getStartVec().subtract(p_217297_1_.getEndVec());
                return BlockRayTraceResult.createMiss(
                    context.getEndVec(),
                    Direction.getFacingFromVector(vec3d.x, vec3d.y, vec3d.z),
                    new BlockPos(p_217297_1_.getEndVec())
                );
            }
            // Sponge end
            final IFluidState ifluidstate = this.shadow$getFluidState(p_217297_2_);
            final Vec3d vec3d = p_217297_1_.getStartVec();
            final Vec3d vec3d1 = p_217297_1_.getEndVec();
            final VoxelShape voxelshape = p_217297_1_.getBlockShape(blockstate, (IBlockReader) (Object) this, p_217297_2_);
            final BlockRayTraceResult blockraytraceresult = this.shadow$rayTraceBlocks(vec3d, vec3d1, p_217297_2_, voxelshape, blockstate);
            final VoxelShape voxelshape1 = p_217297_1_.getFluidShape(ifluidstate, (IBlockReader) (Object) this, p_217297_2_);
            final BlockRayTraceResult blockraytraceresult1 = voxelshape1.rayTrace(vec3d, vec3d1, p_217297_2_);
            final double d0 = blockraytraceresult == null
                ? Double.MAX_VALUE
                : p_217297_1_.getStartVec().squareDistanceTo(blockraytraceresult.getHitVec());
            final double d1 = blockraytraceresult1 == null
                ? Double.MAX_VALUE
                : p_217297_1_.getStartVec().squareDistanceTo(blockraytraceresult1.getHitVec());
            return d0 <= d1 ? blockraytraceresult : blockraytraceresult1;
        }, (p_217302_0_) -> {
            final Vec3d vec3d = p_217302_0_.getStartVec().subtract(p_217302_0_.getEndVec());
            return BlockRayTraceResult.createMiss(
                p_217302_0_.getEndVec(),
                Direction.getFacingFromVector(vec3d.x, vec3d.y, vec3d.z),
                new BlockPos(p_217302_0_.getEndVec())
            );
        });
    }

}
