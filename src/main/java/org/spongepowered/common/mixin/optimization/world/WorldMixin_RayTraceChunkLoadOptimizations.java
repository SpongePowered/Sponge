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
package org.spongepowered.common.mixin.optimization.world;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = World.class, priority = 1500)
public abstract class WorldMixin_RayTraceChunkLoadOptimizations {

    @Shadow public abstract boolean isBlockLoaded(BlockPos pos);

    /**
     * Check if a chunk is loaded before attempting to check the state of the block
     * return null if the chunk is not loaded
     * Based on https://github.com/PaperMC/Paper/blob/ver/1.12.2/Spigot-Server-Patches/0369-Prevent-rayTrace-from-loading-chunks.patch
     */
    @Inject(method = "rayTraceBlocks(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;ZZZ)Lnet/minecraft/util/math/RayTraceResult;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;"
            ), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true
    )
    private void checkChunkLoaded(Vec3d vec31, Vec3d vec32, boolean arg2, boolean arg3, boolean arg4,
                                  CallbackInfoReturnable<RayTraceResult> cir, int i, int j, int k, int l, int i1, int j1,
                                  BlockPos blockpos) {
        if (!this.isBlockLoaded(blockpos)) {
            cir.setReturnValue(null);
        }
    }

    @Surrogate
    private void checkChunkLoaded(Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox,
                                  boolean returnLastUncollidableBlock, CallbackInfoReturnable<RayTraceResult> cir, int i,
                                  int j, int k, int l, int i1, int j1, BlockPos blockpos, IBlockState iblockstate, Block block,
                                  RayTraceResult raytraceresult2, int k1, boolean flag2, boolean flag, boolean flag1, double d0,
                                  double d1, double d2, double d3, double d4, double d5, double d6, double d7, double d8, EnumFacing enumfacing) {
        if (!this.isBlockLoaded(blockpos)) {
            cir.setReturnValue(null);
        }
    }

}
