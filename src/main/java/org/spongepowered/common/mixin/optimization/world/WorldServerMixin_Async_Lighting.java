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
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumLightType;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.world.WorldServerBridge_AsyncLighting;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge_AsyncLighting;
import org.spongepowered.common.bridge.world.chunk.ChunkProviderBridge;
import org.spongepowered.common.bridge.util.math.BlockPosBridge;
import org.spongepowered.common.mixin.core.world.WorldMixin;
import org.spongepowered.common.util.Constants;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nullable;

@Mixin(value = WorldServer.class)
public abstract class WorldServerMixin_Async_Lighting extends WorldMixin implements WorldServerBridge_AsyncLighting {

    private ExecutorService asyncLightingImpl$lightExecutorService =
                Executors.newFixedThreadPool(SpongeImpl.getGlobalConfigAdapter().getConfig().getOptimizations().getAsyncLightingCategory().getNumThreads(), new ThreadFactoryBuilder().setNameFormat("Sponge - Async Light Thread").build());

    @Override
    public boolean checkLightFor(final EnumLightType lightType, final BlockPos pos) {
        return this.asyncLightingBridge$updateLightAsync(lightType, pos, null);
    }

    @Override
    public boolean asyncLightingBridge$checkLightAsync(
        final EnumLightType lightType, final BlockPos pos, final net.minecraft.world.chunk.Chunk currentChunk, final List<Chunk> neighbors) {
        // Sponge - This check is not needed as neighbors are checked in bridge$updateLightAsync
        if (false && !this.isAreaLoaded(pos, 17, false)) {
            return false;
        } else {
            final ChunkBridge_AsyncLighting spongeChunk = (ChunkBridge_AsyncLighting) currentChunk;
            int i = 0;
            int j = 0;
            //this.theProfiler.startSection("getBrightness"); // Sponge - don't use profiler off of main thread
            final int k = this.asyncLightingImpl$getLightForAsync(lightType, pos, currentChunk, neighbors); // Sponge - use thread safe method
            final int l = this.asyncLightingImpl$getRawBlockLightAsync(lightType, pos, currentChunk, neighbors); // Sponge - use thread safe method
            final int i1 = pos.func_177958_n();
            final int j1 = pos.func_177956_o();
            final int k1 = pos.func_177952_p();

            if (l > k) {
                this.lightUpdateBlockList[j++] = 133152;
            } else if (l < k) {
                this.lightUpdateBlockList[j++] = 133152 | k << 18;

                while (i < j) {
                    final int l1 = this.lightUpdateBlockList[i++];
                    final int i2 = (l1 & 63) - 32 + i1;
                    final int j2 = (l1 >> 6 & 63) - 32 + j1;
                    final int k2 = (l1 >> 12 & 63) - 32 + k1;
                    final int l2 = l1 >> 18 & 15;
                    final BlockPos blockpos = new BlockPos(i2, j2, k2);
                    int i3 = this.asyncLightingImpl$getLightForAsync(lightType, blockpos, currentChunk, neighbors); // Sponge - use thread safe method

                    if (i3 == l2) {
                        this.asyncLightingImpl$setLightForAsync(lightType, blockpos, 0, currentChunk, neighbors); // Sponge - use thread safe method

                        if (l2 > 0) {
                            final int j3 = MathHelper.func_76130_a(i2 - i1);
                            final int k3 = MathHelper.func_76130_a(j2 - j1);
                            final int l3 = MathHelper.func_76130_a(k2 - k1);

                            if (j3 + k3 + l3 < 17) {
                                final BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.func_185346_s();

                                for (final EnumFacing enumfacing : EnumFacing.values()) {
                                    final int i4 = i2 + enumfacing.func_82601_c();
                                    final int j4 = j2 + enumfacing.func_96559_d();
                                    final int k4 = k2 + enumfacing.func_82599_e();
                                    blockpos$pooledmutableblockpos.func_181079_c(i4, j4, k4);
                                    // Sponge start - get chunk safely
                                    final Chunk pooledChunk = this.asyncLightingImpl$getLightChunk(blockpos$pooledmutableblockpos, currentChunk, neighbors);
                                    if (pooledChunk == null) {
                                        continue;
                                    }
                                    final int l4 = Math.max(1, pooledChunk.func_177435_g(blockpos$pooledmutableblockpos).func_185891_c());
                                    i3 = this.asyncLightingImpl$getLightForAsync(lightType, blockpos$pooledmutableblockpos, currentChunk, neighbors);
                                    // Sponge end

                                    if (i3 == l2 - l4 && j < this.lightUpdateBlockList.length) {
                                        this.lightUpdateBlockList[j++] = i4 - i1 + 32 | j4 - j1 + 32 << 6 | k4 - k1 + 32 << 12 | l2 - l4 << 18;
                                    }
                                }

                                blockpos$pooledmutableblockpos.func_185344_t();
                            }
                        }
                    }
                }

                i = 0;
            }

            //this.theProfiler.endSection(); // Sponge - don't use profiler off of main thread
            //this.theProfiler.startSection("checkedPosition < toCheckCount"); // Sponge - don't use profiler off of main thread

            while (i < j) {
                final int i5 = this.lightUpdateBlockList[i++];
                final int j5 = (i5 & 63) - 32 + i1;
                final int k5 = (i5 >> 6 & 63) - 32 + j1;
                final int l5 = (i5 >> 12 & 63) - 32 + k1;
                final BlockPos blockpos1 = new BlockPos(j5, k5, l5);
                final int i6 = this.asyncLightingImpl$getLightForAsync(lightType, blockpos1, currentChunk, neighbors); // Sponge - use thread safe method
                final int j6 = this.asyncLightingImpl$getRawBlockLightAsync(lightType, blockpos1, currentChunk, neighbors); // Sponge - use thread safe method

                if (j6 != i6) {
                    this.asyncLightingImpl$setLightForAsync(lightType, blockpos1, j6, currentChunk, neighbors); // Sponge - use thread safe method

                    if (j6 > i6) {
                        final int k6 = Math.abs(j5 - i1);
                        final int l6 = Math.abs(k5 - j1);
                        final int i7 = Math.abs(l5 - k1);
                        final boolean flag = j < this.lightUpdateBlockList.length - 6;

                        if (k6 + l6 + i7 < 17 && flag) {
                            // Sponge start - use thread safe method asyncLightingImpl$getLightForAsync
                            if (this.asyncLightingImpl$getLightForAsync(lightType, blockpos1.func_177976_e(), currentChunk, neighbors) < j6) {
                                this.lightUpdateBlockList[j++] = j5 - 1 - i1 + 32 + (k5 - j1 + 32 << 6) + (l5 - k1 + 32 << 12);
                            }

                            if (this.asyncLightingImpl$getLightForAsync(lightType, blockpos1.func_177974_f(), currentChunk, neighbors) < j6) {
                                this.lightUpdateBlockList[j++] = j5 + 1 - i1 + 32 + (k5 - j1 + 32 << 6) + (l5 - k1 + 32 << 12);
                            }

                            if (this.asyncLightingImpl$getLightForAsync(lightType, blockpos1.func_177977_b(), currentChunk, neighbors) < j6) {
                                this.lightUpdateBlockList[j++] = j5 - i1 + 32 + (k5 - 1 - j1 + 32 << 6) + (l5 - k1 + 32 << 12);
                            }

                            if (this.asyncLightingImpl$getLightForAsync(lightType, blockpos1.func_177984_a(), currentChunk, neighbors) < j6) {
                                this.lightUpdateBlockList[j++] = j5 - i1 + 32 + (k5 + 1 - j1 + 32 << 6) + (l5 - k1 + 32 << 12);
                            }

                            if (this.asyncLightingImpl$getLightForAsync(lightType, blockpos1.func_177978_c(), currentChunk, neighbors) < j6) {
                                this.lightUpdateBlockList[j++] = j5 - i1 + 32 + (k5 - j1 + 32 << 6) + (l5 - 1 - k1 + 32 << 12);
                            }

                            if (this.asyncLightingImpl$getLightForAsync(lightType, blockpos1.func_177968_d(), currentChunk, neighbors) < j6) {
                                this.lightUpdateBlockList[j++] = j5 - i1 + 32 + (k5 - j1 + 32 << 6) + (l5 + 1 - k1 + 32 << 12);
                            }
                            // Sponge end
                        }
                    }
                }
            }

            // Sponge start - Asynchronous light updates
            spongeChunk.asyncLightingBridge$getQueuedLightingUpdates(lightType).remove((Short) this.asyncLightingImpl$blockPosToShort(pos));
            spongeChunk.asyncLightingBridge$getPendingLightUpdates().decrementAndGet();
            for (final net.minecraft.world.chunk.Chunk neighborChunk : neighbors) {
                final ChunkBridge_AsyncLighting neighbor = (ChunkBridge_AsyncLighting) neighborChunk;
                neighbor.asyncLightingBridge$getPendingLightUpdates().decrementAndGet();
            }

            // Sponge end
            //this.theProfiler.endSection(); // Sponge - don't use profiler off of main thread
            return true;
        }
    }

    @Override
    public boolean asyncLightingBridge$updateLightAsync(final EnumLightType lightType, final BlockPos pos, @Nullable Chunk currentChunk) {
        if (this.getMinecraftServer().func_71241_aa() || this.asyncLightingImpl$lightExecutorService.isShutdown()) {
            return false;
        }

        if (currentChunk == null) {
            currentChunk = ((ChunkProviderBridge) this.chunkProvider).bridge$getLoadedChunkWithoutMarkingActive(pos.func_177958_n() >> 4, pos.func_177952_p() >> 4);
        }

        final ChunkBridge_AsyncLighting spongeChunk = (ChunkBridge_AsyncLighting) currentChunk;
        if (currentChunk == null || currentChunk.field_189550_d || !spongeChunk.bridge$areNeighborsLoaded()) {
            return false;
        }

        final short shortPos = this.asyncLightingImpl$blockPosToShort(pos);
        if (spongeChunk.asyncLightingBridge$getQueuedLightingUpdates(lightType).contains(shortPos)) {
            return false;
        }

        final Chunk chunk = currentChunk;
        spongeChunk.asyncLightingBridge$getQueuedLightingUpdates(lightType).add(shortPos);
        spongeChunk.asyncLightingBridge$getPendingLightUpdates().incrementAndGet();
        spongeChunk.asyncLightingBridge$setLightUpdateTime(chunk.func_177412_p().func_82737_E());

        final List<Chunk> neighbors = spongeChunk.bridge$getNeighbors();

        // add diagonal chunks
        final ChunkBridge southChunk = (ChunkBridge) spongeChunk.bridge$getNeighborChunk(0);
        if (southChunk != null) {
            final Chunk southEastChunk = southChunk.bridge$getNeighborChunk(2);
            final Chunk southWestChunk = southChunk.bridge$getNeighborChunk(3);
            if (southEastChunk != null) {
                neighbors.add(southEastChunk);
            }
            if (southWestChunk != null) {
                neighbors.add(southWestChunk);
            }
        }
        final ChunkBridge northChunk = (ChunkBridge) spongeChunk.bridge$getNeighborChunk(1);
        if (northChunk != null) {
            final Chunk northEastChunk = northChunk.bridge$getNeighborChunk(2);
            final Chunk northWestChunk = northChunk.bridge$getNeighborChunk(3);
            if (northEastChunk != null) {
                neighbors.add(northEastChunk);
            }
            if (northWestChunk != null) {
                neighbors.add(northWestChunk);
            }
        }

        for (final net.minecraft.world.chunk.Chunk neighborChunk : neighbors) {
            final ChunkBridge_AsyncLighting neighbor = (ChunkBridge_AsyncLighting) neighborChunk;
            neighbor.asyncLightingBridge$getPendingLightUpdates().incrementAndGet();
            neighbor.asyncLightingBridge$setLightUpdateTime(chunk.func_177412_p().func_82737_E());
        }

        //System.out.println("size = " + ((ThreadPoolExecutor) this.asyncLightingImpl$lightExecutorService).getQueue().size());
        if (SpongeImpl.getServer().func_152345_ab()) {
            this.asyncLightingImpl$lightExecutorService.execute(() -> {
                this.asyncLightingBridge$checkLightAsync(lightType, pos, chunk, neighbors);
            });
        } else {
            this.asyncLightingBridge$checkLightAsync(lightType, pos, chunk, neighbors);
        }

        return true;
    }

    @Override
    public ExecutorService asyncLightingBridge$getLightingExecutor() {
        return this.asyncLightingImpl$lightExecutorService;
    }

    // Thread safe methods to retrieve a chunk during async light updates
    // Each method avoids calling getLoadedChunk and instead accesses the passed neighbor chunk list to avoid concurrency issues
    private Chunk asyncLightingImpl$getLightChunk(final BlockPos pos, final Chunk currentChunk, final List<Chunk> neighbors) {
        if (currentChunk.func_76600_a(pos.func_177958_n() >> 4, pos.func_177952_p() >> 4)) {
            if (currentChunk.field_189550_d) {
                return null;
            }
            return currentChunk;
        }
        for (final net.minecraft.world.chunk.Chunk neighbor : neighbors) {
            if (neighbor.func_76600_a(pos.func_177958_n() >> 4, pos.func_177952_p() >> 4)) {
                if (neighbor.field_189550_d) {
                    return null;
                }
                return neighbor;
            }
        }

        return null;
    }

    private int asyncLightingImpl$getLightForAsync(final EnumLightType lightType, BlockPos pos, final Chunk currentChunk, final List<Chunk> neighbors) {
        if (pos.func_177956_o() < 0) {
            pos = new BlockPos(pos.func_177958_n(), 0, pos.func_177952_p());
        }
        if (!((BlockPosBridge) pos).bridge$isValidPosition()) {
            return lightType.field_77198_c;
        }

        final Chunk chunk = this.asyncLightingImpl$getLightChunk(pos, currentChunk, neighbors);
        if (chunk == null || chunk.field_189550_d) {
            return lightType.field_77198_c;
        }

        return chunk.func_177413_a(lightType, pos);
    }

    private int asyncLightingImpl$getRawBlockLightAsync(final EnumLightType lightType, final BlockPos pos, final Chunk currentChunk, final List<Chunk> neighbors) {
        final Chunk chunk = asyncLightingImpl$getLightChunk(pos, currentChunk, neighbors);
        if (chunk == null || chunk.field_189550_d) {
            return lightType.field_77198_c;
        }
        if (lightType == EnumLightType.SKY && chunk.func_177444_d(pos)) {
            return 15;
        } else {
            final IBlockState blockState = chunk.func_177435_g(pos);
            final int blockLight = SpongeImplHooks.getChunkPosLight(blockState, (net.minecraft.world.World) (Object) this, pos);
            int i = lightType == EnumLightType.SKY ? 0 : blockLight;
            int j = SpongeImplHooks.getBlockLightOpacity(blockState, (net.minecraft.world.World) (Object) this, pos);

            if (j >= 15 && blockLight > 0) {
                j = 1;
            }

            if (j < 1) {
                j = 1;
            }

            if (j >= 15) {
                return 0;
            } else if (i >= 14) {
                return i;
            } else {
                final BlockPos.PooledMutableBlockPos pooledBlockPos = BlockPos.PooledMutableBlockPos.func_185346_s();

                try {
                    for (final EnumFacing enumfacing : EnumFacing.values()) {
                        pooledBlockPos.func_189533_g(pos).func_189536_c(enumfacing);
                        final int k = this.asyncLightingImpl$getLightForAsync(lightType, pooledBlockPos, currentChunk, neighbors) - j;

                        if (k > i) {
                            i = k;
                        }

                        if (i >= 14) {
                            return i;
                        }
                    }

                    return i;
                } finally {
                    pooledBlockPos.func_185344_t();
                }
            }
        }
    }

    private void asyncLightingImpl$setLightForAsync(final EnumLightType type, final BlockPos pos, final int lightValue, final Chunk currentChunk, final List<Chunk> neighbors) {
        if (((BlockPosBridge) pos).bridge$isValidPosition()) {
            final Chunk chunk = this.asyncLightingImpl$getLightChunk(pos, currentChunk, neighbors);
            if (chunk != null && !chunk.field_189550_d) {
                chunk.func_177431_a(type, pos, lightValue);
                this.notifyLightSet(pos);
            }
        }
    }

    private short asyncLightingImpl$blockPosToShort(final BlockPos pos) {
        short serialized = (short) asyncLightingImpl$setNibble(0, pos.func_177958_n() & Constants.Chunk.XZ_MASK, 0, Constants.Chunk.NUM_XZ_BITS);
        serialized = (short) asyncLightingImpl$setNibble(serialized, pos.func_177956_o() & Constants.Chunk.Y_SHORT_MASK, 1, Constants.Chunk.NUM_SHORT_Y_BITS);
        serialized = (short) asyncLightingImpl$setNibble(serialized, pos.func_177952_p() & Constants.Chunk.XZ_MASK, 3, Constants.Chunk.NUM_XZ_BITS);
        return serialized;
    }

    /**
     * Modifies bits in an integer.
     *
     * @param num Integer to modify
     * @param data Bits of data to add
     * @param which Index of nibble to start at
     * @param bitsToReplace The number of bits to replace starting from nibble index
     * @return The modified integer
     */
    private int asyncLightingImpl$setNibble(final int num, final int data, final int which, final int bitsToReplace) {
        return (num & ~(bitsToReplace << (which * 4)) | (data << (which * 4)));
    }
}
