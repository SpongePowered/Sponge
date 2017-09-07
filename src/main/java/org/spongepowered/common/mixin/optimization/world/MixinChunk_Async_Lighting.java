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

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
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
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.interfaces.world.gen.IMixinChunkProviderServer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(value = Chunk.class, priority = 1002)
public abstract class MixinChunk_Async_Lighting implements IMixinChunk {

    // Keeps track of block positions in this chunk currently queued for sky light update
    private CopyOnWriteArrayList<Short> queuedSkyLightingUpdates = new CopyOnWriteArrayList<>();
    // Keeps track of block positions in this chunk currently queued for block light update
    private CopyOnWriteArrayList<Short> queuedBlockLightingUpdates = new CopyOnWriteArrayList<>();
    private AtomicInteger pendingLightUpdates = new AtomicInteger();
    private long lightUpdateTime;
    private ExecutorService lightExecutorService;
    private static final List<Chunk> EMPTY_LIST = new ArrayList<>();
    private static final BlockPos DUMMY_POS = new BlockPos(0, 0, 0);

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
    @Shadow private ConcurrentLinkedQueue<BlockPos> tileEntityPosQueue;

    @Shadow public abstract void checkLight();
    @Shadow public abstract void checkLightSide(EnumFacing facing);
    @Shadow public abstract int getTopFilledSegment();
    @Shadow protected abstract void recheckGaps(boolean isClient);
    @Shadow public abstract int getHeightValue(int x, int z);
    @Shadow protected abstract void checkSkylightNeighborHeight(int x, int z, int maxValue);
    @Shadow public abstract TileEntity getTileEntity(BlockPos pos, Chunk.EnumCreateEntityType type);
    @Shadow public abstract TileEntity createNewTileEntity(BlockPos pos);
    @Shadow public abstract IBlockState getBlockState(BlockPos pos);
    @Shadow public abstract void setSkylightUpdated();
    @Shadow public abstract int getBlockLightOpacity(int x, int y, int z);
    @Shadow public abstract void updateSkylightNeighborHeight(int x, int z, int startY, int endY);

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstruct(World worldIn, int x, int z, CallbackInfo ci) {
        if (!worldIn.isRemote) {
            this.lightExecutorService = ((IMixinWorldServer) worldIn).getLightingExecutor();
        }
    }

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

    @Inject(method = "onTick", at = @At("HEAD"), cancellable = true)
    private void onTickHead(boolean skipRecheckGaps, CallbackInfo ci)
    {
        if (!this.world.isRemote) {
            final List<Chunk> neighbors = this.getSurroundingChunks();
            if (this.isGapLightingUpdated && this.world.provider.hasSkyLight() && !skipRecheckGaps && !neighbors.isEmpty())
            {
                this.lightExecutorService.execute(() -> {
                    this.recheckGapsAsync(neighbors);
                });
                this.isGapLightingUpdated = false;
            }
    
            this.ticked = true;
    
            if (!this.isLightPopulated && this.isTerrainPopulated && !neighbors.isEmpty())
            {
                this.lightExecutorService.execute(() -> {
                    this.checkLightAsync(neighbors);
                });
                // set to true to avoid requeuing the same task when not finished
                this.isLightPopulated = true;
            }
    
            while (!this.tileEntityPosQueue.isEmpty())
            {
                BlockPos blockpos = (BlockPos)this.tileEntityPosQueue.poll();
    
                if (this.getTileEntity(blockpos, Chunk.EnumCreateEntityType.CHECK) == null && this.getBlockState(blockpos).getBlock().hasTileEntity())
                {
                    TileEntity tileentity = this.createNewTileEntity(blockpos);
                    this.world.setTileEntity(blockpos, tileentity);
                    this.world.markBlockRangeForRenderUpdate(blockpos, blockpos);
                }
            }
            ci.cancel();
        }
    }

    @Redirect(method = "checkSkylightNeighborHeight", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getHeight(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/BlockPos;"))
    private BlockPos onCheckSkylightGetHeight(World world, BlockPos pos) {
        final Chunk chunk = this.getLightChunk(pos.getX() >> 4, pos.getZ() >> 4, null);
        if (chunk == null) {
            return DUMMY_POS;
        }

        return new BlockPos(pos.getX(), chunk.getHeightValue(pos.getX() & 15, pos.getZ() & 15), pos.getZ());
    }

    @Redirect(method = "updateSkylightNeighborHeight", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isAreaLoaded(Lnet/minecraft/util/math/BlockPos;I)Z"))
    private boolean onAreaLoadedSkyLightNeighbor(World world, BlockPos pos, int radius) {
        return this.isAreaLoaded();
    }

    @Redirect(method = "updateSkylightNeighborHeight", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;checkLightFor(Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean onCheckLightForSkylightNeighbor(World world, EnumSkyBlock enumSkyBlock, BlockPos pos) {
        if (world.isRemote) {
            return world.checkLightFor(enumSkyBlock, pos);
        }

        return this.checkWorldLightFor(enumSkyBlock, pos);
    }

    /**
     * Rechecks chunk gaps async.
     * 
     * @param neighbors A thread-safe list of surrounding neighbor chunks
     */
    private void recheckGapsAsync(List<Chunk> neighbors) {
        //this.world.profiler.startSection("recheckGaps"); Sponge - don't use profiler off of main thread

        for (int i = 0; i < 16; ++i)
        {
            for (int j = 0; j < 16; ++j)
            {
                if (this.updateSkylightColumns[i + j * 16])
                {
                    this.updateSkylightColumns[i + j * 16] = false;
                    int k = this.getHeightValue(i, j);
                    int l = this.x * 16 + i;
                    int i1 = this.z * 16 + j;
                    int j1 = Integer.MAX_VALUE;

                    for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL)
                    {
                        final Chunk chunk = this.getLightChunk((l + enumfacing.getFrontOffsetX()) >> 4, (i1 + enumfacing.getFrontOffsetZ()) >> 4, neighbors);
                        if (chunk == null || chunk.unloadQueued) {
                            continue;
                        }
                        j1 = Math.min(j1, chunk.getLowestHeight());
                    }

                    this.checkSkylightNeighborHeight(l, i1, j1);

                    for (EnumFacing enumfacing1 : EnumFacing.Plane.HORIZONTAL)
                    {
                        this.checkSkylightNeighborHeight(l + enumfacing1.getFrontOffsetX(), i1 + enumfacing1.getFrontOffsetZ(), k);
                    }
                }
            }

           // this.isGapLightingUpdated = false;
        }

        // this.world.profiler.endSection(); Sponge - don't use profiler off of the main thread
    }

    @Redirect(method = "enqueueRelightChecks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;"))
    private IBlockState onRelightChecksGetBlockState(World world, BlockPos pos) {
        Chunk chunk = ((IMixinChunkProviderServer) world.getChunkProvider()).getLoadedChunkWithoutMarkingActive(pos.getX() >> 4, pos.getZ() >> 4);

        final IMixinChunk spongeChunk = (IMixinChunk) chunk;
        if (chunk == null || chunk.unloadQueued || !spongeChunk.areNeighborsLoaded()) {
            return Blocks.AIR.getDefaultState();
        }

        return chunk.getBlockState(pos);
    }

    @Redirect(method = "enqueueRelightChecks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;checkLight(Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean onRelightChecksCheckLight(World world, BlockPos pos) {
        if (!this.world.isRemote) {
            return this.checkWorldLight(pos);
        }

        return world.checkLight(pos);
    }

    // Avoids grabbing chunk async during light check
    @Redirect(method = "checkLight(II)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;checkLight(Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean onCheckLightWorld(World world, BlockPos pos) {
        if (!world.isRemote) {
            return this.checkWorldLight(pos);
        }
        return world.checkLight(pos);
    }

    @Inject(method = "checkLight", at = @At("HEAD"), cancellable = true)
    private void checkLightHead(CallbackInfo ci) {
        if (!this.world.isRemote) {
            if (this.world.getMinecraftServer().isServerStopped() || this.lightExecutorService.isShutdown()) {
                return;
            }

            if (this.isQueuedForUnload()) {
                return;
            }
            final List<Chunk> neighborChunks = this.getSurroundingChunks();
            if (neighborChunks.isEmpty()) {
                this.isLightPopulated = false;
                return;
            }

            if (SpongeImpl.getServer().isCallingFromMinecraftThread()) {
                this.lightExecutorService.execute(() -> {
                    this.checkLightAsync(neighborChunks);
                });
            } else {
                this.checkLightAsync(neighborChunks);
            }
            ci.cancel();
        }
    }

    /**
     * Checks light async.
     * 
     * @param neighbors A thread-safe list of surrounding neighbor chunks
     */
    private void checkLightAsync(List<Chunk> neighbors)
    {
        this.isTerrainPopulated = true;
        this.isLightPopulated = true;
        BlockPos blockpos = new BlockPos(this.x << 4, 0, this.z << 4);

        if (this.world.provider.hasSkyLight())
        {
            label44:

            for (int i = 0; i < 16; ++i)
            {
                for (int j = 0; j < 16; ++j)
                {
                    if (!this.checkLightAsync(i, j, neighbors))
                    {
                        this.isLightPopulated = false;
                        break label44;
                    }
                }
            }

            if (this.isLightPopulated)
            {
                for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL)
                {
                    int k = enumfacing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? 16 : 1;
                    final BlockPos pos = blockpos.offset(enumfacing, k);
                    final Chunk chunk = this.getLightChunk(pos.getX() >> 4, pos.getZ() >> 4, neighbors);
                    if (chunk == null) {
                        continue;
                    }
                    chunk.checkLightSide(enumfacing.getOpposite());
                }

                this.setSkylightUpdated();
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
    private boolean checkLightAsync(int x, int z, List<Chunk> neighbors)
    {
        int i = this.getTopFilledSegment();
        boolean flag = false;
        boolean flag1 = false;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos((this.x << 4) + x, 0, (this.z << 4) + z);

        for (int j = i + 16 - 1; j > this.world.getSeaLevel() || j > 0 && !flag1; --j)
        {
            blockpos$mutableblockpos.setPos(blockpos$mutableblockpos.getX(), j, blockpos$mutableblockpos.getZ());
            int k = this.getBlockState(blockpos$mutableblockpos).getLightOpacity();

            if (k == 255 && blockpos$mutableblockpos.getY() < this.world.getSeaLevel())
            {
                flag1 = true;
            }

            if (!flag && k > 0)
            {
                flag = true;
            }
            else if (flag && k == 0 && !this.checkWorldLight(blockpos$mutableblockpos, neighbors))
            {
                return false;
            }
        }

        for (int l = blockpos$mutableblockpos.getY(); l > 0; --l)
        {
            blockpos$mutableblockpos.setPos(blockpos$mutableblockpos.getX(), l, blockpos$mutableblockpos.getZ());

            if (this.getBlockState(blockpos$mutableblockpos).getLightValue() > 0)
            {
                this.checkWorldLight(blockpos$mutableblockpos, neighbors);
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
    private Chunk getLightChunk(int chunkX, int chunkZ, List<Chunk> neighbors) {
        final Chunk currentChunk = (Chunk)(Object) this;
        if (currentChunk.isAtLocation(chunkX, chunkZ)) {
            if (currentChunk.unloadQueued) {
                return null;
            }
            return currentChunk;
        }
        if (neighbors == null) {
            neighbors = this.getSurroundingChunks();
            if (neighbors.isEmpty()) {
                return null;
            }
        }
        for (net.minecraft.world.chunk.Chunk neighbor : neighbors) {
            if (neighbor.isAtLocation(chunkX, chunkZ)) {
                if (neighbor.unloadQueued) {
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
    private boolean isAreaLoaded() {
        if (!this.areNeighborsLoaded()) {
            return false;
        }

        // add diagonal chunks
        final Chunk southEastChunk = ((IMixinChunk) this.getNeighborChunk(0)).getNeighborChunk(2);
        if (southEastChunk == null) {
            return false;
        }

        final Chunk southWestChunk = ((IMixinChunk) this.getNeighborChunk(0)).getNeighborChunk(3);
        if (southWestChunk == null) {
            return false;
        }

        final Chunk northEastChunk = ((IMixinChunk) this.getNeighborChunk(1)).getNeighborChunk(2);
        if (northEastChunk == null) {
            return false;
        }

        final Chunk northWestChunk = ((IMixinChunk) this.getNeighborChunk(1)).getNeighborChunk(3);
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
    private List<Chunk> getSurroundingChunks() {
        if (!this.areNeighborsLoaded()) {
            return EMPTY_LIST;
        }

        // add diagonal chunks
        final Chunk southEastChunk = ((IMixinChunk) this.getNeighborChunk(0)).getNeighborChunk(2);
        if (southEastChunk == null) {
            return EMPTY_LIST;
        }

        final Chunk southWestChunk = ((IMixinChunk) this.getNeighborChunk(0)).getNeighborChunk(3);
        if (southWestChunk == null) {
            return EMPTY_LIST;
        }

        final Chunk northEastChunk = ((IMixinChunk) this.getNeighborChunk(1)).getNeighborChunk(2);
        if (northEastChunk == null) {
            return EMPTY_LIST;
        }

        final Chunk northWestChunk = ((IMixinChunk) this.getNeighborChunk(1)).getNeighborChunk(3);
        if (northWestChunk == null) {
            return EMPTY_LIST;
        }

        List<Chunk> chunkList = new ArrayList<>();
        chunkList = this.getNeighbors();
        chunkList.add(southEastChunk);
        chunkList.add(southWestChunk);
        chunkList.add(northEastChunk);
        chunkList.add(northWestChunk);
        return chunkList;
    }

    @Inject(method = "relightBlock", at = @At("HEAD"), cancellable = true)
    private void onRelightBlock(int x, int y, int z, CallbackInfo ci) {
        if (!this.world.isRemote) {
            this.lightExecutorService.execute(() -> {
                this.relightBlockAsync(x, y, z);
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
    private void relightBlockAsync(int x, int y, int z)
    {
        int i = this.heightMap[z << 4 | x] & 255;
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
            this.markBlocksDirtyVerticalAsync(x + this.x * 16, z + this.z * 16, j, i);
            this.heightMap[z << 4 | x] = j;
            int k = this.x * 16 + x;
            int l = this.z * 16 + z;

            if (this.world.provider.hasSkyLight())
            {
                if (j < i)
                {
                    for (int j1 = j; j1 < i; ++j1)
                    {
                        ExtendedBlockStorage extendedblockstorage2 = this.storageArrays[j1 >> 4];

                        if (extendedblockstorage2 != Chunk.NULL_BLOCK_STORAGE)
                        {
                            extendedblockstorage2.setSkyLight(x, j1 & 15, z, 15);
                            this.world.notifyLightSet(new BlockPos((this.x << 4) + x, j1, (this.z << 4) + z));
                        }
                    }
                }
                else
                {
                    for (int i1 = i; i1 < j; ++i1)
                    {
                        ExtendedBlockStorage extendedblockstorage = this.storageArrays[i1 >> 4];

                        if (extendedblockstorage != Chunk.NULL_BLOCK_STORAGE)
                        {
                            extendedblockstorage.setSkyLight(x, i1 & 15, z, 0);
                            this.world.notifyLightSet(new BlockPos((this.x << 4) + x, i1, (this.z << 4) + z));
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

                    ExtendedBlockStorage extendedblockstorage1 = this.storageArrays[j >> 4];

                    if (extendedblockstorage1 != Chunk.NULL_BLOCK_STORAGE)
                    {
                        extendedblockstorage1.setSkyLight(x, j & 15, z, k1);
                    }
                }
            }

            int l1 = this.heightMap[z << 4 | x];
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

            if (this.world.provider.hasSkyLight())
            {
                for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL)
                {
                    this.updateSkylightNeighborHeight(k + enumfacing.getFrontOffsetX(), l + enumfacing.getFrontOffsetZ(), j2, k2);
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
    private void markBlocksDirtyVerticalAsync(int x1, int z1, int x2, int z2)
    {
        if (x2 > z2)
        {
            int i = z2;
            z2 = x2;
            x2 = i;
        }

        if (this.world.provider.hasSkyLight())
        {
            for (int j = x2; j <= z2; ++j)
            {
                final BlockPos pos = new BlockPos(x1, j, z1);
                final Chunk chunk = this.getLightChunk(pos.getX() >> 4, pos.getZ() >> 4, null);
                if (chunk == null) {
                    continue;
                }
                ((IMixinWorldServer) this.world).updateLightAsync(EnumSkyBlock.SKY, new BlockPos(x1, j, z1), (Chunk)(Object) chunk);
            }
        }

        this.world.markBlockRangeForRenderUpdate(x1, x2, z1, x1, z2, z1);
    }

    /**
     * Checks world light thread-safe.
     * 
     * @param lightType The type of light to check
     * @param pos The block position
     * @return True if light update was successful, false if not
     */
    private boolean checkWorldLightFor(EnumSkyBlock lightType, BlockPos pos) {
        final Chunk chunk = this.getLightChunk(pos.getX() >> 4, pos.getZ() >> 4, null);
        if (chunk == null) {
            return false;
        }

        return ((IMixinWorldServer) this.world).updateLightAsync(lightType, pos, (Chunk)(Object) chunk);
    }

    private boolean checkWorldLight(BlockPos pos) {
        return this.checkWorldLight(pos, null);
    }

    /**
     * Checks world light async.
     * 
     * @param pos The block position
     * @param neighbors A thread-safe list of surrounding neighbor chunks
     * @return True if light update was successful, false if not
     */
    private boolean checkWorldLight(BlockPos pos, List<Chunk> neighbors) {
        boolean flag = false;
        final Chunk chunk = this.getLightChunk(pos.getX() >> 4, pos.getZ() >> 4, neighbors);
        if (chunk == null) {
            return false;
        }

        if (this.world.provider.hasSkyLight())
        {
            flag |= ((IMixinWorldServer) this.world).updateLightAsync(EnumSkyBlock.SKY, pos, (Chunk)(Object) chunk);
        }

        flag = flag | ((IMixinWorldServer) this.world).updateLightAsync(EnumSkyBlock.BLOCK, pos, (Chunk)(Object) chunk);
        return flag;
    }

    /**
     * Gets the list of block positions currently queued for lighting updates.
     * 
     * @param type The light type
     * @return The list of queued block positions, empty if none
     */
    @Override
    public CopyOnWriteArrayList<Short> getQueuedLightingUpdates(EnumSkyBlock type) {
        if (type == EnumSkyBlock.SKY) {
            return this.queuedSkyLightingUpdates;
        }
        return this.queuedBlockLightingUpdates;
    }
}
