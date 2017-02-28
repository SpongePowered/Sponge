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

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

import java.util.concurrent.atomic.AtomicInteger;

@Mixin(value = Chunk.class)
public abstract class MixinChunk_Async_Lighting implements IMixinChunk {

    public AtomicInteger pendingLightUpdates = new AtomicInteger();
    public long lightUpdateTime;

    @Shadow @Final private World world;
    @Shadow private boolean isTerrainPopulated;
    @Shadow private boolean isLightPopulated;
    @Shadow private boolean chunkTicked;
    @Shadow @Final public int xPosition;
    @Shadow @Final public int zPosition;
    @Shadow @Final private boolean[] updateSkylightColumns;
    @Shadow private boolean isGapLightingUpdated;

    @Shadow protected abstract void recheckGaps(boolean isClient);
    @Shadow public abstract int getHeightValue(int x, int z);
    @Shadow protected abstract void checkSkylightNeighborHeight(int x, int z, int maxValue);

    @Override
    public AtomicInteger getPendingLightUpdates() {
        return this.pendingLightUpdates;
    }

    @Override
    public long getLightUpdateTime() {
        return this.lightUpdateTime;
    }

    @Override
    public void setLightUpdateTime(long time) {
        this.lightUpdateTime = time;
    }

    @Redirect(method = "updateSkylightNeighborHeight", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;checkLightFor(Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean onCheckLightForSkylight(World world, EnumSkyBlock enumSkyBlock, BlockPos pos) {
        if (world.isRemote) {
            return world.checkLightFor(enumSkyBlock, pos);
        }

        return ((IMixinWorldServer) world).updateLightAsync(enumSkyBlock, pos);
    }

    @Inject(method = "recheckGaps", at = @At("HEAD"), cancellable = true)
    private void onRecheckGaps(boolean onlyOnce, CallbackInfo ci) {
        if (!this.world.isRemote) {
            ((IMixinWorldServer) this.world).getLightingExecutor().execute(() -> {
                this.recheckGapsAsync(onlyOnce);
            });
            ci.cancel();
        }
    }

    private void recheckGapsAsync(boolean p_150803_1_) {
        //this.worldObj.theProfiler.startSection("recheckGaps"); Sponge - don't use profiler off of main thread

        if (this.world.isAreaLoaded(new BlockPos(this.xPosition * 16 + 8, 0, this.zPosition * 16 + 8), 16))
        {
            for (int i = 0; i < 16; ++i)
            {
                for (int j = 0; j < 16; ++j)
                {
                    if (this.updateSkylightColumns[i + j * 16])
                    {
                        this.updateSkylightColumns[i + j * 16] = false;
                        int k = this.getHeightValue(i, j);
                        int l = this.xPosition * 16 + i;
                        int i1 = this.zPosition * 16 + j;
                        int j1 = Integer.MAX_VALUE;

                        for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL)
                        {
                            j1 = Math.min(j1, this.world.getChunksLowestHorizon(l + enumfacing.getFrontOffsetX(), i1 + enumfacing.getFrontOffsetZ()));
                        }

                        this.checkSkylightNeighborHeight(l, i1, j1);

                        for (EnumFacing enumfacing1 : EnumFacing.Plane.HORIZONTAL)
                        {
                            this.checkSkylightNeighborHeight(l + enumfacing1.getFrontOffsetX(), i1 + enumfacing1.getFrontOffsetZ(), k);
                        }


                        if (p_150803_1_)
                        {
                            // this.worldObj.theProfiler.endSection(); Sponge - don't use profiler off of the main thread
                            return;
                        }
                    }
                }
            }

            this.isGapLightingUpdated = false;
        }

        // this.world.profiler.endSection(); Sponge - don't use profiler off of the main thread
    }

    /**
     * @author blood - February 20th, 2017
     * @reason Since lighting updates run async, we need to always return true to send client updates.
     *
     * @param pos The position to get the light for
     * @return Whether block position can see sky
     */
    @Overwrite
    public boolean isPopulated() {
        if (this.world.isRemote) {
            return this.chunkTicked && this.isTerrainPopulated && this.isLightPopulated;
        }

        return true;
    }
}
