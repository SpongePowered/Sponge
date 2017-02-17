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
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.world.SerializationBehaviors;
import org.spongepowered.api.world.storage.ChunkDataStream;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.api.world.storage.WorldStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.TrackingPhases;
import org.spongepowered.common.event.tracking.phase.entity.EntityPhase;
import org.spongepowered.common.event.tracking.phase.generation.GenerationPhase;
import org.spongepowered.common.event.tracking.phase.plugin.PluginPhase;
import org.spongepowered.common.event.tracking.phase.tick.TickPhase;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.world.IMixinAnvilChunkLoader;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.interfaces.world.gen.IMixinChunkProviderServer;
import org.spongepowered.common.util.CachedLong2ObjectMap;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.world.SpongeEmptyChunk;
import org.spongepowered.common.world.storage.SpongeChunkDataStream;
import org.spongepowered.common.world.storage.WorldStorageUtil;

import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Mixin(ChunkProviderServer.class)
public abstract class MixinChunkProviderServer implements WorldStorage, IMixinChunkProviderServer {

    private SpongeEmptyChunk EMPTY_CHUNK;
    private boolean denyChunkRequests = true;
    private boolean forceChunkRequests = false;
    private long chunkUnloadDelay = 15000;
    private int maxChunkUnloads = 100;

    @Shadow @Final public WorldServer worldObj;
    @Shadow @Final private IChunkLoader chunkLoader;
    @Shadow public IChunkGenerator chunkGenerator;
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Shadow @Final @Mutable public Long2ObjectMap<Chunk> id2ChunkMap = new CachedLong2ObjectMap();

    @Shadow public abstract Chunk getLoadedChunk(int x, int z);
    @Shadow public abstract Chunk loadChunk(int x, int z);
    @Shadow public abstract Chunk loadChunkFromFile(int x, int z);
    @Shadow public abstract Chunk provideChunk(int x, int z);
    @Shadow public abstract void saveChunkExtraData(Chunk chunkIn);
    @Shadow public abstract void saveChunkData(Chunk chunkIn);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(WorldServer worldObjIn, IChunkLoader chunkLoaderIn, IChunkGenerator chunkGeneratorIn, CallbackInfo ci) {
        EMPTY_CHUNK = new SpongeEmptyChunk(worldObjIn, 0, 0);
        SpongeConfig<?> spongeConfig = SpongeHooks.getActiveConfig(worldObjIn);
        ((IMixinWorldServer) worldObjIn).setActiveConfig(spongeConfig);
        this.denyChunkRequests = spongeConfig.getConfig().getWorld().getDenyChunkRequests();
        this.chunkUnloadDelay = spongeConfig.getConfig().getWorld().getChunkUnloadDelay() * 1000;
        this.maxChunkUnloads = spongeConfig.getConfig().getWorld().getMaxChunkUnloads();
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
     * @author blood - October 25th, 2016
     * @reason Removes usage of droppedChunksSet in favor of unloaded flag.
     *
     * @param chunkIn The chunk to queue
     */
    @Overwrite
    public void unload(Chunk chunkIn)
    {
        if (!((IMixinChunk) chunkIn).isPersistedChunk() && this.worldObj.provider.canDropChunk(chunkIn.xPosition, chunkIn.zPosition))
        {
            // Sponge - we avoid using the queue and simply check the unloaded flag during unloads
            //this.droppedChunksSet.add(Long.valueOf(ChunkPos.chunkXZ2Int(chunkIn.xPosition, chunkIn.zPosition)));
            chunkIn.unloaded = true;
        }
    }

    // split from loadChunk to avoid 2 lookups with our inject
    private Chunk loadChunkForce(int x, int z) {
        Chunk chunk = this.loadChunkFromFile(x, z);

        if (chunk != null)
        {
            this.id2ChunkMap.put(ChunkPos.chunkXZ2Int(x, z), chunk);
            chunk.onChunkLoad();
            chunk.populateChunk((ChunkProviderServer)(Object) this, this.chunkGenerator);
        }

        return chunk;
    }

    @Redirect(method = "provideChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/ChunkProviderServer;loadChunk(II)Lnet/minecraft/world/chunk/Chunk;"))
    public Chunk onProvideChunkHead(ChunkProviderServer chunkProviderServer, int x, int z) {
        if (!this.denyChunkRequests || this.forceChunkRequests) {
            return this.loadChunk(x, z);
        }

        Chunk chunk = this.getLoadedChunk(x, z);
        if (chunk == null && this.canDenyChunkRequest()) {
            return EMPTY_CHUNK;
        }

        if (chunk == null) {
            chunk = this.loadChunkForce(x, z);
        }

        return chunk;
    }

    @Inject(method = "provideChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/ChunkPos;chunkXZ2Int(II)J"))
    public void onProvideChunkStart(int x, int z, CallbackInfoReturnable<Chunk> cir) {
        if (CauseTracker.ENABLED) {
            final CauseTracker causeTracker = ((IMixinWorldServer) this.worldObj).getCauseTracker();
            causeTracker.switchToPhase(GenerationPhase.State.TERRAIN_GENERATION, PhaseContext.start()
                    .addCaptures()
                    .complete());
        }
    }

    @Inject(method = "provideChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;populateChunk(Lnet/minecraft/world/chunk/IChunkProvider;Lnet/minecraft/world/chunk/IChunkGenerator;)V", shift = Shift.AFTER))
    public void onProvideChunkEnd(int x, int z, CallbackInfoReturnable<Chunk> ci) {
        if (CauseTracker.ENABLED) {
            ((IMixinWorldServer) this.worldObj).getCauseTracker().completePhase(GenerationPhase.State.TERRAIN_GENERATION);
        }
    }

    @Inject(method = "provideChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/crash/CrashReport;makeCrashReport(Ljava/lang/Throwable;Ljava/lang/String;)Lnet/minecraft/crash/CrashReport;"))
    public void onError(CallbackInfoReturnable<Chunk> ci) {
        if (CauseTracker.ENABLED) {
            ((IMixinWorldServer) this.worldObj).getCauseTracker().completePhase(GenerationPhase.State.TERRAIN_GENERATION);
        }
    }

    private boolean canDenyChunkRequest() {
        if (CauseTracker.ENABLED) {
            final CauseTracker causeTracker = ((IMixinWorldServer) this.worldObj).getCauseTracker();
            final IPhaseState currentState = causeTracker.getCurrentState();
            // States that cannot deny chunks
            if (currentState == TickPhase.Tick.PLAYER
                    || currentState == TickPhase.Tick.DIMENSION
                    || currentState == EntityPhase.State.CHANGING_TO_DIMENSION
                    || currentState == EntityPhase.State.LEAVING_DIMENSION) {
                return false;
            }

            // States that can deny chunks
            if (currentState == GenerationPhase.State.WORLD_SPAWNER_SPAWNING
                    || currentState == PluginPhase.Listener.PRE_WORLD_TICK_LISTENER) {
                return true;
            }

            // Phases that can deny chunks
            if (currentState.getPhase() == TrackingPhases.BLOCK
                    || currentState.getPhase() == TrackingPhases.ENTITY
                    || currentState.getPhase() == TrackingPhases.TICK) {
                return true;
            }
        }


        return false;
    }

    @Override
    public void setMaxChunkUnloads(int maxUnloads) {
        this.maxChunkUnloads = maxUnloads;
    }

    @Override
    public void setForceChunkRequests(boolean flag) {
        this.forceChunkRequests = flag;
    }

    @Override
    public void setDenyChunkRequests(boolean flag) {
        this.denyChunkRequests = flag;
    }

    @Override
    public long getChunkUnloadDelay() {
        return this.chunkUnloadDelay;
    }

    /**
     * @author blood - October 20th, 2016
     * @reason Refactors entire method to not use the droppedChunksSet by
     * simply looping through all loaded chunks and determining whether it
     * can unload or not.
     *
     * @return true if unload queue was processed
     */
    @Overwrite
    public boolean unloadQueuedChunks()
    {
        if (!this.worldObj.disableLevelSaving)
        {
            ((IMixinWorldServer) this.worldObj).getTimingsHandler().doChunkUnload.startTiming();
            Iterator<Chunk> iterator = this.id2ChunkMap.values().iterator();
            int chunksUnloaded = 0;
            long now = System.currentTimeMillis();
            while (chunksUnloaded < this.maxChunkUnloads && iterator.hasNext()) {
                Chunk chunk = iterator.next();
                IMixinChunk spongeChunk = (IMixinChunk) chunk;
                if (chunk != null && chunk.unloaded && !spongeChunk.isPersistedChunk()) {
                    if (this.getChunkUnloadDelay() > 0) {
                        if ((now - spongeChunk.getScheduledForUnload()) < this.chunkUnloadDelay) {
                            continue;
                        }
                        spongeChunk.setScheduledForUnload(-1);
                    }
                    chunk.onChunkUnload();
                    this.saveChunkData(chunk);
                    this.saveChunkExtraData(chunk);
                    iterator.remove();
                    chunksUnloaded++;
                }
            }
            ((IMixinWorldServer) this.worldObj).getTimingsHandler().doChunkUnload.stopTiming();
        }

        this.chunkLoader.chunkTick();
        return false;
    }

    // Copy of getLoadedChunk without marking chunk active.
    // This allows the chunk to unload if currently queued.
    @Override
    public Chunk getLoadedChunkWithoutMarkingActive(int x, int z){
        long i = ChunkPos.chunkXZ2Int(x, z);
        Chunk chunk = this.id2ChunkMap.get(i);
        return chunk;
    }

    @Inject(method = "canSave", at = @At("HEAD"), cancellable = true)
    public void onCanSave(CallbackInfoReturnable<Boolean> cir) {
        if (((WorldProperties)this.worldObj.getWorldInfo()).getSerializationBehavior() == SerializationBehaviors.NONE) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "saveChunkData", at = @At("HEAD"), cancellable = true)
    public void onSaveChunkData(Chunk chunkIn, CallbackInfo ci) {
        if (((WorldProperties)this.worldObj.getWorldInfo()).getSerializationBehavior() == SerializationBehaviors.NONE) {
            ci.cancel();
        }
    }

    @Inject(method = "saveExtraData", at = @At("HEAD"), cancellable = true)
    public void onSaveExtraData(CallbackInfo ci) {
        if (((WorldProperties)this.worldObj.getWorldInfo()).getSerializationBehavior() == SerializationBehaviors.NONE) {
            ci.cancel();
        }
    }
}
