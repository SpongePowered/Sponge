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
package org.spongepowered.common.mixin.core.world.gen;

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.util.LongHashMap;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.world.storage.ChunkDataStream;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.api.world.storage.WorldStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.event.CauseTracker;
import org.spongepowered.common.interfaces.world.IMixinAnvilChunkLoader;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.interfaces.world.gen.IMixinChunkProviderServer;
import org.spongepowered.common.world.storage.SpongeChunkDataStream;
import org.spongepowered.common.world.storage.WorldStorageUtil;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

@Mixin(value = ChunkProviderServer.class, priority = 1000)
public abstract class MixinChunkProviderServer implements WorldStorage, IMixinChunkProviderServer {

    @Shadow public WorldServer worldObj;
    @Shadow private IChunkLoader chunkLoader;
    @Shadow private LongHashMap<Chunk> id2ChunkMap;
    @Shadow public IChunkProvider serverChunkGenerator;
    @Shadow private Set<Long> droppedChunksSet;
    @Shadow public boolean chunkLoadOverride;
    @Shadow private Chunk dummyChunk;

    @Shadow public abstract Chunk loadChunk(int chunkX, int chunkZ);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(WorldServer world, IChunkLoader chunkLoader, IChunkProvider serverChunkGenerator, CallbackInfo ci) {
        // Avoid various chunks loading and unloading repeatedly during chunk cleanup.
        // Note: This will most likely have side effects with mods, we just need to try and do our best to handle them to maintain this performance boost.
        this.chunkLoadOverride = !((IMixinWorld) world).getActiveConfig().getConfig().getWorld().getDenyChunkRequests();
    }

    @Nullable
    @Override
    public Chunk getChunkIfLoaded(int x, int z) {
        return this.id2ChunkMap.getValueByKey(ChunkCoordIntPair.chunkXZ2Int(x, z));
    }

    /**
     * @author blood - May 9th, 2016
     * @reason Control terrain gen flag here to avoid leaking block captures during populate.
     *
     * @param chunkProvider The chunk provider
     * @param x The x coordinate of chunk
     * @param z The z coordinate of chunk
     */
    @Overwrite
    public void populate(IChunkProvider chunkProvider, int x, int z) {
        Chunk chunk = this.provideChunk(x, z);

        if (!chunk.isTerrainPopulated()) {
            chunk.func_150809_p();

            if (this.serverChunkGenerator != null) {
                IMixinWorld world = (IMixinWorld) this.worldObj;
                final CauseTracker causeTracker = world.getCauseTracker();
                boolean capturingTerrain = causeTracker.isCapturingTerrainGen();
                boolean captureBlocks = causeTracker.isCapturingBlocks();
                causeTracker.setCapturingTerrainGen(true);
                causeTracker.setCaptureBlocks(false);
                this.serverChunkGenerator.populate(chunkProvider, x, z);
                chunk.setChunkModified();
                causeTracker.setCapturingTerrainGen(capturingTerrain);
                causeTracker.setCaptureBlocks(captureBlocks);
            }
        }
    }

    @Override
    public ChunkDataStream getGeneratedChunks() {
        if (!(this.chunkLoader instanceof IMixinAnvilChunkLoader)) {
            throw new UnsupportedOperationException("unknown chunkLoader");
        }
        return new SpongeChunkDataStream(((IMixinAnvilChunkLoader) this.chunkLoader).getWorldDir());
    }

    @Override
    public CompletableFuture<Boolean> doesChunkExist(Vector3i chunkCoords) {
        return WorldStorageUtil.doesChunkExist(this.worldObj, this.chunkLoader, chunkCoords);
    }

    @Override
    public CompletableFuture<Optional<DataContainer>> getChunkData(Vector3i chunkCoords) {
        return WorldStorageUtil.getChunkData(this.worldObj, this.chunkLoader, chunkCoords);
    }

    @Override
    public WorldProperties getWorldProperties() {
        return (WorldProperties) this.worldObj.getWorldInfo();
    }

    /**
     * @author blood - June 15th, 2016
     * @reason Make sure world gen is allowed to load chunks
     *
     * @return The chunk if loaded or dummy chunk if not
     */
    @Overwrite
    public Chunk provideChunk(int x, int z) {
        Chunk chunk = (Chunk)this.id2ChunkMap.getValueByKey(ChunkCoordIntPair.chunkXZ2Int(x, z));
        if (chunk == null) {
            IMixinWorld world = (IMixinWorld) this.worldObj;
            final CauseTracker causeTracker = world.getCauseTracker();
            if (!this.worldObj.isFindingSpawnPoint() && !this.chunkLoadOverride && !causeTracker.isCapturingTerrainGen()) {
                return this.dummyChunk;
            } else {
                return this.loadChunk(x, z);
            }
        } else {
            return chunk;
        }
    }

    @Inject(method = "unloadQueuedChunks", at = @At("HEAD"))
    public void onUnloadQueuedChunksStart(CallbackInfoReturnable<Boolean> ci) {
        IMixinWorld spongeWorld = (IMixinWorld) this.worldObj;
        spongeWorld.getTimingsHandler().doChunkUnload.startTiming();
    }

    @Inject(method = "unloadQueuedChunks", at = @At("RETURN"))
    public void onUnloadQueuedChunksEnd(CallbackInfoReturnable<Boolean> ci) {
        IMixinWorld spongeWorld = (IMixinWorld) this.worldObj;
        spongeWorld.getTimingsHandler().doChunkUnload.stopTiming();
    }

    @Override
    public Set<Long> getChunksQueuedForUnload() {
        return this.droppedChunksSet;
    }
}
