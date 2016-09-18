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
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.generation.GenerationPhase;
import org.spongepowered.common.interfaces.world.IMixinAnvilChunkLoader;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.interfaces.world.gen.IMixinChunkProviderServer;
import org.spongepowered.common.util.CachedLong2ObjectMap;
import org.spongepowered.common.world.storage.SpongeChunkDataStream;
import org.spongepowered.common.world.storage.WorldStorageUtil;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

@Mixin(ChunkProviderServer.class)
public abstract class MixinChunkProviderServer implements WorldStorage, IMixinChunkProviderServer {

    @Shadow @Final public WorldServer worldObj;
    @Shadow @Final private IChunkLoader chunkLoader;
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Shadow @Final @Mutable public Long2ObjectMap<Chunk> id2ChunkMap = new CachedLong2ObjectMap();

    @Shadow public abstract Chunk provideChunk(int x, int z);

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

//    private boolean canDenyChunkRequest() {
//        if (this.chunkLoadOverride || this.worldObj.isFindingSpawnPoint()) {
//            return false;
//        }
//
//        final CauseTracker causeTracker = ((IMixinWorld) this.worldObj).getCauseTracker();
//        if (causeTracker.isWorldSpawnerRunning() || causeTracker.isChunkSpawnerRunning() || causeTracker.isCapturingTerrainGen()) {
//            return true;
//        }
//
//        return false;
//    }

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
            ((IMixinWorldServer) this.worldObj).getCauseTracker().completePhase();
        }
    }

    @Inject(method = "unloadQueuedChunks", at = @At("HEAD"))
    public void onUnloadQueuedChunksStart(CallbackInfoReturnable<Boolean> ci) {
        ((IMixinWorldServer) this.worldObj).getTimingsHandler().doChunkUnload.startTiming();
    }

    @Inject(method = "unloadQueuedChunks", at = @At("RETURN"))
    public void onUnloadQueuedChunksEnd(CallbackInfoReturnable<Boolean> ci) {
        ((IMixinWorldServer) this.worldObj).getTimingsHandler().doChunkUnload.stopTiming();
    }


    private int maxChunkUnloads = -1;

    @ModifyConstant(method = "unloadQueuedChunks", constant = @Constant(intValue = 100))
    private int modifyUnloadCount(int original) {
        if (this.maxChunkUnloads == -1) {
            final int maxChunkUnloads = ((IMixinWorldServer) this.worldObj).getActiveConfig().getConfig().getWorld().getMaxChunkUnloads();
            this.maxChunkUnloads = maxChunkUnloads < 1 ? 1 : maxChunkUnloads;
        }
        return this.maxChunkUnloads;
    }

    @Override
    public void setMaxChunkUnloads(int maxUnloads) {
        this.maxChunkUnloads = maxUnloads;
    }

    @Redirect(method = "unloadQueuedChunks", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;get(Ljava/lang/Object;)Ljava/lang/Object;", remap = false))
    public Object onUnloadQueuedChunksGetChunk(Long2ObjectMap<Chunk> chunkMap, Object key) {
        Chunk chunk = chunkMap.get(key);
        if (chunk != null) {
            chunk.unloaded = true; // ignore unloaded flag
        }
        return chunk;
    }

    /**
     * @author blood - September 5th, 2016
     *
     * @reason Ignores the chunk unload flag to avoid chunks not unloading properly.
     */
    @Nullable
    @Overwrite
    public Chunk getLoadedChunk(int x, int z){
        long i = ChunkPos.chunkXZ2Int(x, z);
        Chunk chunk = this.id2ChunkMap.get(i);

        // Sponge start - Ignore the chunk unloaded flag as it causes
        // many issues with how we already handle unloads.
        /*if (chunk != null){
            chunk.unloaded = false;
        }*/
        // Sponge end

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
