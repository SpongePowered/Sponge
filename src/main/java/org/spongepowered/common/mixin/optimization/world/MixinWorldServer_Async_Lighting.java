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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.interfaces.world.gen.IMixinChunkProviderServer;
import org.spongepowered.common.mixin.core.world.MixinWorld;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Mixin(value = WorldServer.class)
public abstract class MixinWorldServer_Async_Lighting extends MixinWorld implements IMixinWorldServer {

    private ExecutorService lightExecutorService =
            Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("Sponge - Async Light Thread").build());

    @Override
    public boolean checkLightFor(EnumSkyBlock lightType, BlockPos pos) {
        return this.updateLightAsync(lightType, pos);
    }

    @Override
    public boolean checkLightAsync(EnumSkyBlock lightType, BlockPos pos, net.minecraft.world.chunk.Chunk chunk) {
        // Sponge - This check is not needed as neighbors are checked in updateLightSync
        if (false || !this.isAreaLoaded(pos, 17, false)) {
            return false;
        } else {
            final IMixinChunk spongeChunk = (IMixinChunk) chunk;
            int i = 0;
            int j = 0;
            //this.theProfiler.startSection("getBrightness"); // Sponge - don't use profiler off of main thread
            int k = this.getLightFor(lightType, pos);
            int l = this.getRawBlockLight(pos, lightType);
            int i1 = pos.getX();
            int j1 = pos.getY();
            int k1 = pos.getZ();

            if (l > k) {
                this.lightUpdateBlockList[j++] = 133152;
            } else if (l < k) {
                this.lightUpdateBlockList[j++] = 133152 | k << 18;

                while (i < j) {
                    int l1 = this.lightUpdateBlockList[i++];
                    int i2 = (l1 & 63) - 32 + i1;
                    int j2 = (l1 >> 6 & 63) - 32 + j1;
                    int k2 = (l1 >> 12 & 63) - 32 + k1;
                    int l2 = l1 >> 18 & 15;
                    BlockPos blockpos = new BlockPos(i2, j2, k2);
                    int i3 = this.getLightFor(lightType, blockpos);

                    if (i3 == l2) {
                        this.setLightFor(lightType, blockpos, 0);

                        if (l2 > 0) {
                            int j3 = MathHelper.abs_int(i2 - i1);
                            int k3 = MathHelper.abs_int(j2 - j1);
                            int l3 = MathHelper.abs_int(k2 - k1);

                            if (j3 + k3 + l3 < 17) {
                                BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

                                for (EnumFacing enumfacing : EnumFacing.values()) {
                                    int i4 = i2 + enumfacing.getFrontOffsetX();
                                    int j4 = j2 + enumfacing.getFrontOffsetY();
                                    int k4 = k2 + enumfacing.getFrontOffsetZ();
                                    blockpos$pooledmutableblockpos.setPos(i4, j4, k4);
                                    int l4 = Math.max(1, this.getBlockState(blockpos$pooledmutableblockpos).getLightOpacity());
                                    i3 = this.getLightFor(lightType, blockpos$pooledmutableblockpos);

                                    if (i3 == l2 - l4 && j < this.lightUpdateBlockList.length) {
                                        this.lightUpdateBlockList[j++] = i4 - i1 + 32 | j4 - j1 + 32 << 6 | k4 - k1 + 32 << 12 | l2 - l4 << 18;
                                    }
                                }

                                blockpos$pooledmutableblockpos.release();
                            }
                        }
                    }
                }

                i = 0;
            }

            //this.theProfiler.endSection(); // Sponge - don't use profiler off of main thread
            //this.theProfiler.startSection("checkedPosition < toCheckCount"); // Sponge - don't use profiler off of main thread

            while (i < j) {
                int i5 = this.lightUpdateBlockList[i++];
                int j5 = (i5 & 63) - 32 + i1;
                int k5 = (i5 >> 6 & 63) - 32 + j1;
                int l5 = (i5 >> 12 & 63) - 32 + k1;
                BlockPos blockpos1 = new BlockPos(j5, k5, l5);
                int i6 = this.getLightFor(lightType, blockpos1);
                int j6 = this.getRawBlockLight(blockpos1, lightType);

                if (j6 != i6) {
                    this.setLightFor(lightType, blockpos1, j6);

                    if (j6 > i6) {
                        int k6 = Math.abs(j5 - i1);
                        int l6 = Math.abs(k5 - j1);
                        int i7 = Math.abs(l5 - k1);
                        boolean flag = j < this.lightUpdateBlockList.length - 6;

                        if (k6 + l6 + i7 < 17 && flag) {
                            if (this.getLightFor(lightType, blockpos1.west()) < j6) {
                                this.lightUpdateBlockList[j++] = j5 - 1 - i1 + 32 + (k5 - j1 + 32 << 6) + (l5 - k1 + 32 << 12);
                            }

                            if (this.getLightFor(lightType, blockpos1.east()) < j6) {
                                this.lightUpdateBlockList[j++] = j5 + 1 - i1 + 32 + (k5 - j1 + 32 << 6) + (l5 - k1 + 32 << 12);
                            }

                            if (this.getLightFor(lightType, blockpos1.down()) < j6) {
                                this.lightUpdateBlockList[j++] = j5 - i1 + 32 + (k5 - 1 - j1 + 32 << 6) + (l5 - k1 + 32 << 12);
                            }

                            if (this.getLightFor(lightType, blockpos1.up()) < j6) {
                                this.lightUpdateBlockList[j++] = j5 - i1 + 32 + (k5 + 1 - j1 + 32 << 6) + (l5 - k1 + 32 << 12);
                            }

                            if (this.getLightFor(lightType, blockpos1.north()) < j6) {
                                this.lightUpdateBlockList[j++] = j5 - i1 + 32 + (k5 - j1 + 32 << 6) + (l5 - 1 - k1 + 32 << 12);
                            }

                            if (this.getLightFor(lightType, blockpos1.south()) < j6) {
                                this.lightUpdateBlockList[j++] = j5 - i1 + 32 + (k5 - j1 + 32 << 6) + (l5 + 1 - k1 + 32 << 12);
                            }
                        }
                    }
                }
            }

            // Sponge start - Asynchronous light updates
            if (SpongeImpl.getGlobalConfig().getConfig().getOptimizations().useAsyncLighting()) {
                spongeChunk.getPendingLightUpdates().decrementAndGet();
                for (net.minecraft.world.chunk.Chunk neighborChunk : spongeChunk.getNeighbors()) {
                    final IMixinChunk neighbor = (IMixinChunk) neighborChunk;
                    neighbor.getPendingLightUpdates().decrementAndGet();
                }
            }
            // Sponge end
            //this.theProfiler.endSection(); // Sponge - don't use profiler off of main thread
            return true;
        }
    }

    @Override
    public boolean updateLightAsync(EnumSkyBlock lightType, BlockPos pos) {
        final net.minecraft.world.chunk.Chunk chunk =
                ((IMixinChunkProviderServer) this.getChunkProvider()).getLoadedChunkWithoutMarkingActive(pos.getX() >> 4, pos.getZ() >> 4);
        IMixinChunk spongeChunk = (IMixinChunk) chunk;
        if (chunk == null || chunk.unloaded || !spongeChunk.areNeighborsLoaded()) {
            return false;
        }

        spongeChunk.getPendingLightUpdates().incrementAndGet();
        spongeChunk.setLightUpdateTime(chunk.getWorld().getTotalWorldTime());

        for (net.minecraft.world.chunk.Chunk neighborChunk : spongeChunk.getNeighbors()) {
            final IMixinChunk neighbor = (IMixinChunk) neighborChunk;
            neighbor.getPendingLightUpdates().incrementAndGet();
            neighbor.setLightUpdateTime(chunk.getWorld().getTotalWorldTime());
        }

        this.lightExecutorService.execute(() -> {
            this.checkLightAsync(lightType, pos, chunk);
        });

        return true;
    }

    @Override
    public ExecutorService getLightingExecutor() {
        return this.lightExecutorService;
    }
}
