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
package org.spongepowered.common.mixin.optimization.world.chunk;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.world.WorldServerBridge_AsyncLighting;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge_AsyncLighting;
import org.spongepowered.common.bridge.world.chunk.ChunkProviderBridge;
import org.spongepowered.common.util.Constants;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

@Mixin(value = Chunk.class, priority = 1002)
public abstract class ChunkMixin_Async_Lighting implements ChunkBridge_AsyncLighting {

    // Keeps track of block positions in this chunk currently queued for sky light update
    private Set<Short> asyncLighting$queuedSkyLightingUpdates = ConcurrentHashMap.newKeySet();
    // Keeps track of block positions in this chunk currently queued for block light update
    private Set<Short> asyncLighting$queuedBlockLightingUpdates = ConcurrentHashMap.newKeySet();
    private AtomicInteger asyncLighting$pendingLightUpdates = new AtomicInteger();
    private long asyncLighting$lightUpdateTime;
    private ExecutorService asyncLighting$lightExecutorService;
    private boolean asyncLighting$isServerChunk;

    @Shadow @Final private World world;
    @Shadow @Final private int[] heightMap;
    @Shadow @Final private ExtendedBlockStorage[] storageArrays;
    @Shadow private boolean isTerrainPopulated;
    @Shadow private boolean isLightPopulated;
    @Shadow private boolean ticked;
    @Shadow private boolean dirty;
    @Shadow @Final public int x;
    @Shadow @Final public int z;
    @Shadow @Final private boolean[] updateSkylightColumns;
    @Shadow private boolean isGapLightingUpdated;
    @Shadow private int heightMapMinimum;
    @Shadow @Final private ConcurrentLinkedQueue<BlockPos> tileEntityPosQueue;

    @Shadow public abstract int getTopFilledSegment();
    @Shadow public abstract int getHeightValue(int x, int z);
    @Shadow protected abstract void checkSkylightNeighborHeight(int x, int z, int maxValue);
    @Shadow @Nullable public abstract TileEntity getTileEntity(BlockPos pos, Chunk.CreateEntityType type);
    @Shadow protected abstract TileEntity createNewTileEntity(BlockPos pos);
    @Shadow public abstract BlockState getBlockState(BlockPos pos);
    @Shadow protected abstract int getBlockLightOpacity(int x, int y, int z);
    @Shadow protected abstract void updateSkylightNeighborHeight(int x, int z, int startY, int endY);
    @Shadow protected abstract void checkLightSide(Direction facing);

    @Inject(method = "<init>(Lnet/minecraft/world/World;II)V", at = @At("RETURN"))
    private void asyncLighting$initializeFields(final World worldIn, final int x, final int z, final CallbackInfo ci) {
        this.asyncLighting$isServerChunk = !((WorldBridge) worldIn).bridge$isFake();
        if (this.asyncLighting$isServerChunk) {
            this.asyncLighting$lightExecutorService = ((WorldServerBridge_AsyncLighting) worldIn).asyncLightingBridge$getLightingExecutor();
        }
    }

    @Override
    public AtomicInteger asyncLightingBridge$getPendingLightUpdates() {
        return this.asyncLighting$pendingLightUpdates;
    }

    @Override
    public long asyncLightingBridge$getLightUpdateTime() {
        return this.asyncLighting$lightUpdateTime;
    }

    @Override
    public void asyncLightingBridge$setLightUpdateTime(final long time) {
        this.asyncLighting$lightUpdateTime = time;
    }

    @Inject(method = "onTick", at = @At("HEAD"), cancellable = true)
    private void asyncLighting$onTickHead(final boolean skipRecheckGaps, final CallbackInfo ci)
    {
        if (this.asyncLighting$isServerChunk) {
            final List<Chunk> neighbors = this.asyncLighting$getSurroundingChunks();
            if (this.isGapLightingUpdated && this.world.dimension.hasSkyLight() && !skipRecheckGaps && !neighbors.isEmpty())
            {
                this.asyncLighting$lightExecutorService.execute(() -> {
                    this.asyncLighting$recheckGapsAsync(neighbors);
                });
                this.isGapLightingUpdated = false;
            }

            this.ticked = true;

            if (!this.isLightPopulated && this.isTerrainPopulated && !neighbors.isEmpty())
            {
                this.asyncLighting$lightExecutorService.execute(() -> {
                    this.asyncLighting$checkLightAsync(neighbors);
                });
                // set to true to avoid requeuing the same task when not finished
                this.isLightPopulated = true;
            }

            while (!this.tileEntityPosQueue.isEmpty())
            {
                final BlockPos blockpos = this.tileEntityPosQueue.poll();

                if (this.getTileEntity(blockpos, Chunk.CreateEntityType.CHECK) == null && this.getBlockState(blockpos).getBlock().hasTileEntity())
                {
                    final TileEntity tileentity = this.createNewTileEntity(blockpos);
                    this.world.setTileEntity(blockpos, tileentity);
                    this.world.func_175704_b(blockpos, blockpos);
                }
            }
            ci.cancel();
        }
    }

    @Redirect(method = "checkSkylightNeighborHeight", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getHeight(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/BlockPos;"))
    private BlockPos asyncLighting$onCheckSkylightGetHeight(final World world, final BlockPos pos) {
        final Chunk chunk = this.asyncLighting$getLightChunk(pos.getX() >> 4, pos.getZ() >> 4, null);
        if (chunk == null) {
            return Constants.DUMMY_POS;
        }

        return new BlockPos(pos.getX(), chunk.func_76611_b(pos.getX() & 15, pos.getZ() & 15), pos.getZ());
    }

    @Redirect(method = "updateSkylightNeighborHeight", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isAreaLoaded(Lnet/minecraft/util/math/BlockPos;I)Z"))
    private boolean asyncLighting$onAreaLoadedSkyLightNeighbor(final World world, final BlockPos pos, final int radius) {
        return this.asyncLighting$isAreaLoaded();
    }

    @Redirect(method = "updateSkylightNeighborHeight", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;checkLightFor(Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean asyncLighting$onCheckLightForSkylightNeighbor(final World world, final LightType enumSkyBlock, final BlockPos pos) {
        if (!this.asyncLighting$isServerChunk) {
            return world.func_180500_c(enumSkyBlock, pos);
        }

        return this.asyncLighting$checkWorldLightFor(enumSkyBlock, pos);
    }

    /**
     * Rechecks chunk gaps async.
     *
     * @param neighbors A thread-safe list of surrounding neighbor chunks
     */
    private void asyncLighting$recheckGapsAsync(final List<Chunk> neighbors) {
        //this.world.profiler.startSection("recheckGaps"); Sponge - don't use profiler off of main thread

        for (int i = 0; i < 16; ++i)
        {
            for (int j = 0; j < 16; ++j)
            {
                if (this.updateSkylightColumns[i + j * 16])
                {
                    this.updateSkylightColumns[i + j * 16] = false;
                    final int k = this.getHeightValue(i, j);
                    final int l = this.x * 16 + i;
                    final int i1 = this.z * 16 + j;
                    int j1 = Integer.MAX_VALUE;

                    for (final Direction enumfacing : Direction.Plane.HORIZONTAL)
                    {
                        final Chunk chunk = this.asyncLighting$getLightChunk((l + enumfacing.getXOffset()) >> 4, (i1 + enumfacing.getZOffset()) >> 4, neighbors);
                        if (chunk == null || chunk.field_189550_d) {
                            continue;
                        }
                        j1 = Math.min(j1, chunk.func_177442_v());
                    }

                    this.checkSkylightNeighborHeight(l, i1, j1);

                    for (final Direction enumfacing1 : Direction.Plane.HORIZONTAL)
                    {
                        this.checkSkylightNeighborHeight(l + enumfacing1.getXOffset(), i1 + enumfacing1.getZOffset(), k);
                    }
                }
            }

           // this.isGapLightingUpdated = false;
        }

        // this.world.profiler.endSection(); Sponge - don't use profiler off of the main thread
    }

    @Redirect(method = "enqueueRelightChecks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;"))
    private BlockState asyncLighting$onRelightChecksGetBlockState(final World world, final BlockPos pos) {
        final Chunk chunk = ((ChunkProviderBridge) world.getChunkProvider())
            .bridge$getLoadedChunkWithoutMarkingActive(pos.getX() >> 4, pos.getZ() >> 4);

        final ChunkBridge spongeChunk = (ChunkBridge) chunk;
        if (chunk == null || chunk.field_189550_d || !spongeChunk.bridge$areNeighborsLoaded()) {
            return Blocks.AIR.getDefaultState();
        }

        return chunk.func_177435_g(pos);
    }

    @Redirect(method = "enqueueRelightChecks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;checkLight(Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean asyncLighting$onRelightChecksCheckLight(final World world, final BlockPos pos) {
        if (this.asyncLighting$isServerChunk) {
            return this.asyncLighting$checkWorldLight(pos);
        }

        return world.func_175664_x(pos);
    }

    // Avoids grabbing chunk async during light check
    @Redirect(method = "checkLight(II)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;checkLight(Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean asyncLighting$onCheckLightWorld(final World world, final BlockPos pos) {
        if (this.asyncLighting$isServerChunk) {
            return this.asyncLighting$checkWorldLight(pos);
        }
        return world.func_175664_x(pos);
    }

    @Inject(method = "checkLight()V", at = @At("HEAD"), cancellable = true)
    private void asyncLighting$checkLightHead(final CallbackInfo ci) {
        if (this.asyncLighting$isServerChunk) {
            if (this.world.getServer().isServerStopped() || this.asyncLighting$lightExecutorService.isShutdown()) {
                return;
            }

            if (this.bridge$isQueuedForUnload()) {
                return;
            }
            final List<Chunk> neighborChunks = this.asyncLighting$getSurroundingChunks();
            if (neighborChunks.isEmpty()) {
                this.isLightPopulated = false;
                return;
            }

            if (SpongeImpl.getServer().func_152345_ab()) {
                try {
                    this.asyncLighting$lightExecutorService.execute(() -> {
                        this.asyncLighting$checkLightAsync(neighborChunks);
                    });
                } catch (RejectedExecutionException e) {
                    // This could happen if ServerHangWatchdog kills the server
                    // between the start of the method and the execute() call.
                    if (!this.world.getServer().isServerStopped() && !this.asyncLighting$lightExecutorService.isShutdown()) {
                        throw e;
                    }
                }
            } else {
                this.asyncLighting$checkLightAsync(neighborChunks);
            }
            ci.cancel();
        }
    }

    /**
     * Checks light async.
     *
     * @param neighbors A thread-safe list of surrounding neighbor chunks
     */
    @SuppressWarnings("ConstantConditions")
    private void asyncLighting$checkLightAsync(final List<Chunk> neighbors)
    {
        this.isTerrainPopulated = true;
        this.isLightPopulated = true;
        final BlockPos blockpos = new BlockPos(this.x << 4, 0, this.z << 4);

        if (this.world.dimension.hasSkyLight())
        {
            label44:

            for (int i = 0; i < 16; ++i)
            {
                for (int j = 0; j < 16; ++j)
                {
                    if (!this.asyncLighting$checkLightAsync(i, j, neighbors))
                    {
                        this.isLightPopulated = false;
                        break label44;
                    }
                }
            }

            if (this.isLightPopulated)
            {
                for (final Direction enumfacing : Direction.Plane.HORIZONTAL)
                {
                    final int k = enumfacing.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 16 : 1;
                    final BlockPos pos = blockpos.offset(enumfacing, k);
                    final Chunk chunk = this.asyncLighting$getLightChunk(pos.getX() >> 4, pos.getZ() >> 4, neighbors);
                    if (chunk == null) {
                        continue;
                    }
                    ((ChunkMixin_Async_Lighting) (Object) chunk).checkLightSide(enumfacing.getOpposite());
                }

                for (int i = 0; i < this.updateSkylightColumns.length; ++i)
                {
                    this.updateSkylightColumns[i] = true;
                }

                this.asyncLighting$recheckGapsAsync(neighbors);
            }
        }
    }

    /**
     * Checks light async.
     *
     * @param x The x position of chunk
     * @param z The z position of chunk
     * @param neighbors A thread-safe list of surrounding neighbor chunks
     * @return True if light update was successful, false if not
     */
    private boolean asyncLighting$checkLightAsync(final int x, final int z, final List<Chunk> neighbors)
    {
        final int i = this.getTopFilledSegment();
        boolean flag = false;
        boolean flag1 = false;
        final BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos((this.x << 4) + x, 0, (this.z << 4) + z);

        for (int j = i + 16 - 1; j > this.world.getSeaLevel() || j > 0 && !flag1; --j)
        {
            blockpos$mutableblockpos.setPos(blockpos$mutableblockpos.getX(), j, blockpos$mutableblockpos.getZ());
            final int k = this.getBlockState(blockpos$mutableblockpos).func_185891_c();

            if (k == 255 && blockpos$mutableblockpos.getY() < this.world.getSeaLevel())
            {
                flag1 = true;
            }

            if (!flag && k > 0)
            {
                flag = true;
            }
            else if (flag && k == 0 && !this.asyncLighting$checkWorldLight(blockpos$mutableblockpos, neighbors))
            {
                return false;
            }
        }

        for (int l = blockpos$mutableblockpos.getY(); l > 0; --l)
        {
            blockpos$mutableblockpos.setPos(blockpos$mutableblockpos.getX(), l, blockpos$mutableblockpos.getZ());

            if (this.getBlockState(blockpos$mutableblockpos).getLightValue() > 0)
            {
                this.asyncLighting$checkWorldLight(blockpos$mutableblockpos, neighbors);
            }
        }

        return true;
    }

    /**
     * Thread-safe method to retrieve a chunk during async light updates.
     *
     * @param chunkX The x position of chunk.
     * @param chunkZ The z position of chunk.
     * @param neighbors A thread-safe list of surrounding neighbor chunks
     * @return The chunk if available, null if not
     */
    @Nullable
    private Chunk asyncLighting$getLightChunk(final int chunkX, final int chunkZ, @Nullable List<Chunk> neighbors) {
        final Chunk currentChunk = (Chunk)(Object) this;
        if (currentChunk.func_76600_a(chunkX, chunkZ)) {
            if (currentChunk.field_189550_d) {
                return null;
            }
            return currentChunk;
        }
        if (neighbors == null) {
            neighbors = this.asyncLighting$getSurroundingChunks();
            if (neighbors.isEmpty()) {
                return null;
            }
        }
        for (final net.minecraft.world.chunk.Chunk neighbor : neighbors) {
            if (neighbor.func_76600_a(chunkX, chunkZ)) {
                if (neighbor.field_189550_d) {
                    return null;
                }
                return neighbor;
            }
        }

        return null;
    }

    /**
     * Checks if surrounding chunks are loaded thread-safe.
     *
     * @return True if surrounded chunks are loaded, false if not
     */
    private boolean asyncLighting$isAreaLoaded() {
        if (!this.bridge$areNeighborsLoaded()) {
            return false;
        }

        // add diagonal chunks
        final Chunk southEastChunk = ((ChunkBridge) this.bridge$getNeighborChunk(0)).bridge$getNeighborChunk(2);
        if (southEastChunk == null) {
            return false;
        }

        final Chunk southWestChunk = ((ChunkBridge) this.bridge$getNeighborChunk(0)).bridge$getNeighborChunk(3);
        if (southWestChunk == null) {
            return false;
        }

        final Chunk northEastChunk = ((ChunkBridge) this.bridge$getNeighborChunk(1)).bridge$getNeighborChunk(2);
        if (northEastChunk == null) {
            return false;
        }

        final Chunk northWestChunk = ((ChunkBridge) this.bridge$getNeighborChunk(1)).bridge$getNeighborChunk(3);
        if (northWestChunk == null) {
            return false;
        }

        return true;
    }

    /**
     * Gets surrounding chunks thread-safe.
     *
     * @return The list of surrounding chunks, empty list if not loaded
     */
    private List<Chunk> asyncLighting$getSurroundingChunks() {
        if (!this.bridge$areNeighborsLoaded()) {
            return Collections.emptyList();
        }

        // add diagonal chunks
        final Chunk southEastChunk = ((ChunkBridge) this.bridge$getNeighborChunk(0)).bridge$getNeighborChunk(2);
        if (southEastChunk == null) {
            return Collections.emptyList();
        }

        final Chunk southWestChunk = ((ChunkBridge) this.bridge$getNeighborChunk(0)).bridge$getNeighborChunk(3);
        if (southWestChunk == null) {
            return Collections.emptyList();
        }

        final Chunk northEastChunk = ((ChunkBridge) this.bridge$getNeighborChunk(1)).bridge$getNeighborChunk(2);
        if (northEastChunk == null) {
            return Collections.emptyList();
        }

        final Chunk northWestChunk = ((ChunkBridge) this.bridge$getNeighborChunk(1)).bridge$getNeighborChunk(3);
        if (northWestChunk == null) {
            return Collections.emptyList();
        }

        List<Chunk> chunkList;
        chunkList = this.bridge$getNeighbors();
        chunkList.add(southEastChunk);
        chunkList.add(southWestChunk);
        chunkList.add(northEastChunk);
        chunkList.add(northWestChunk);
        return chunkList;
    }

    @Inject(method = "relightBlock", at = @At("HEAD"), cancellable = true)
    private void asyncLighting$onRelightBlock(final int x, final int y, final int z, final CallbackInfo ci) {
        if (this.asyncLighting$isServerChunk) {
            this.asyncLighting$lightExecutorService.execute(() -> {
                this.asyncLighting$relightBlockAsync(x, y, z);
            });
            ci.cancel();
        }
    }

    /**
     * Relight's a block async.
     *
     * @param x The x position
     * @param y The y position
     * @param z The z position
     */
    private void asyncLighting$relightBlockAsync(final int x, final int y, final int z)
    {
        final int i = this.heightMap[z << 4 | x] & 255;
        int j = i;

        if (y > i)
        {
            j = y;
        }

        while (j > 0 && this.getBlockLightOpacity(x, j - 1, z) == 0)
        {
            --j;
        }

        if (j != i)
        {
            this.asyncLighting$markBlocksDirtyVerticalAsync(x + this.x * 16, z + this.z * 16, j, i);
            this.heightMap[z << 4 | x] = j;
            final int k = this.x * 16 + x;
            final int l = this.z * 16 + z;

            if (this.world.dimension.hasSkyLight())
            {
                if (j < i)
                {
                    for (int j1 = j; j1 < i; ++j1)
                    {
                        final ExtendedBlockStorage extendedblockstorage2 = this.storageArrays[j1 >> 4];

                        if (extendedblockstorage2 != Chunk.EMPTY_SECTION)
                        {
                            extendedblockstorage2.func_76657_c(x, j1 & 15, z, 15);
                            this.world.func_175679_n(new BlockPos((this.x << 4) + x, j1, (this.z << 4) + z));
                        }
                    }
                }
                else
                {
                    for (int i1 = i; i1 < j; ++i1)
                    {
                        final ExtendedBlockStorage extendedblockstorage = this.storageArrays[i1 >> 4];

                        if (extendedblockstorage != Chunk.EMPTY_SECTION)
                        {
                            extendedblockstorage.func_76657_c(x, i1 & 15, z, 0);
                            this.world.func_175679_n(new BlockPos((this.x << 4) + x, i1, (this.z << 4) + z));
                        }
                    }
                }

                int k1 = 15;

                while (j > 0 && k1 > 0)
                {
                    --j;
                    int i2 = this.getBlockLightOpacity(x, j, z);

                    if (i2 == 0)
                    {
                        i2 = 1;
                    }

                    k1 -= i2;

                    if (k1 < 0)
                    {
                        k1 = 0;
                    }

                    final ExtendedBlockStorage extendedblockstorage1 = this.storageArrays[j >> 4];

                    if (extendedblockstorage1 != Chunk.EMPTY_SECTION)
                    {
                        extendedblockstorage1.func_76657_c(x, j & 15, z, k1);
                    }
                }
            }

            final int l1 = this.heightMap[z << 4 | x];
            int j2 = i;
            int k2 = l1;

            if (l1 < i)
            {
                j2 = l1;
                k2 = i;
            }

            if (l1 < this.heightMapMinimum)
            {
                this.heightMapMinimum = l1;
            }

            if (this.world.dimension.hasSkyLight())
            {
                for (final Direction enumfacing : Direction.Plane.HORIZONTAL)
                {
                    this.updateSkylightNeighborHeight(k + enumfacing.getXOffset(), l + enumfacing.getZOffset(), j2, k2);
                }

                this.updateSkylightNeighborHeight(k, l, j2, k2);
            }

            this.dirty = true;
        }
    }

    /**
     * Marks a vertical line of blocks as dirty async.
     * Instead of calling world directly, we pass chunk safely for async light method.
     *
     * @param x1
     * @param z1
     * @param x2
     * @param z2
     */
    private void asyncLighting$markBlocksDirtyVerticalAsync(final int x1, final int z1, int x2, int z2)
    {
        if (x2 > z2)
        {
            final int i = z2;
            z2 = x2;
            x2 = i;
        }

        if (this.world.dimension.hasSkyLight())
        {
            for (int j = x2; j <= z2; ++j)
            {
                final BlockPos pos = new BlockPos(x1, j, z1);
                final Chunk chunk = this.asyncLighting$getLightChunk(pos.getX() >> 4, pos.getZ() >> 4, null);
                if (chunk == null) {
                    continue;
                }
                ((WorldServerBridge_AsyncLighting) this.world).asyncLightingBridge$updateLightAsync(LightType.SKY, new BlockPos(x1, j, z1), (Chunk)(Object) chunk);
            }
        }

        this.world.func_147458_c(x1, x2, z1, x1, z2, z1);
    }

    /**
     * Checks world light thread-safe.
     *
     * @param lightType The type of light to check
     * @param pos The block position
     * @return True if light update was successful, false if not
     */
    private boolean asyncLighting$checkWorldLightFor(final LightType lightType, final BlockPos pos) {
        final Chunk chunk = this.asyncLighting$getLightChunk(pos.getX() >> 4, pos.getZ() >> 4, null);
        if (chunk == null) {
            return false;
        }

        return ((WorldServerBridge_AsyncLighting) this.world).asyncLightingBridge$updateLightAsync(lightType, pos, (Chunk)(Object) chunk);
    }

    private boolean asyncLighting$checkWorldLight(final BlockPos pos) {
        return this.asyncLighting$checkWorldLight(pos, null);
    }

    /**
     * Checks world light async.
     *
     * @param pos The block position
     * @param neighbors A thread-safe list of surrounding neighbor chunks
     * @return True if light update was successful, false if not
     */
    private boolean asyncLighting$checkWorldLight(final BlockPos pos, final List<Chunk> neighbors) {
        boolean flag = false;
        final Chunk chunk = this.asyncLighting$getLightChunk(pos.getX() >> 4, pos.getZ() >> 4, neighbors);
        if (chunk == null) {
            return false;
        }

        if (this.world.dimension.hasSkyLight())
        {
            flag = ((WorldServerBridge_AsyncLighting) this.world).asyncLightingBridge$updateLightAsync(LightType.SKY, pos, (Chunk) (Object) chunk);
        }

        flag = flag | ((WorldServerBridge_AsyncLighting) this.world).asyncLightingBridge$updateLightAsync(LightType.BLOCK, pos, (Chunk)(Object) chunk);
        return flag;
    }

    /**
     * Gets the list of block positions currently queued for lighting updates.
     *
     * @param type The light type
     * @return The list of queued block positions, empty if none
     */
    @Override
    public Set<Short> asyncLightingBridge$getQueuedLightingUpdates(final LightType type) {
        if (type == LightType.SKY) {
            return this.asyncLighting$queuedSkyLightingUpdates;
        }
        return this.asyncLighting$queuedBlockLightingUpdates;
    }
}
