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
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;
import org.spongepowered.api.world.SerializationBehaviors;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.WorldInfoBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkProviderBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkProviderServerBridge;
import org.spongepowered.common.config.category.WorldCategory;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.generation.GenerationPhase;
import org.spongepowered.common.util.CachedLong2ObjectMap;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.world.SpongeEmptyChunk;
import org.spongepowered.common.world.storage.WorldStorageUtil;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

@Mixin(ChunkProviderServer.class)
public abstract class ChunkProviderServerMixin implements ChunkProviderServerBridge, ChunkProviderBridge {

    @Nullable private SpongeEmptyChunk impl$EMPTY_CHUNK;
    private boolean impl$denyChunkRequests = true;
    private boolean impl$forceChunkRequests = false;
    private long impl$chunkUnloadDelay = Constants.World.DEFAULT_CHUNK_UNLOAD_DELAY;
    private int impl$maxChunkUnloads = Constants.World.MAX_CHUNK_UNLOADS;

    @Shadow @Final private WorldServer world;
    @Shadow @Final private IChunkLoader chunkLoader;
    @Shadow @Final @Mutable private IChunkGenerator chunkGenerator;
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Shadow @Final @Mutable private Long2ObjectMap<Chunk> loadedChunks = new CachedLong2ObjectMap();

    @Shadow @Nullable public abstract Chunk getLoadedChunk(int x, int z);
    @Shadow @Nullable public abstract Chunk loadChunk(int x, int z);
    @Shadow protected abstract void saveChunkExtraData(Chunk chunkIn);
    @Shadow protected abstract void saveChunkData(Chunk chunkIn);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void impl$setUpCommonFields(
        final WorldServer worldObjIn, final IChunkLoader chunkLoaderIn, final IChunkGenerator chunkGeneratorIn, final CallbackInfo ci) {
        if (((WorldBridge) worldObjIn).bridge$isFake()) {
            return;
        }
        this.impl$EMPTY_CHUNK = new SpongeEmptyChunk(worldObjIn, 0, 0);
        final WorldCategory worldCategory = ((WorldInfoBridge) this.world.func_72912_H()).bridge$getConfigAdapter().getConfig().getWorld();

        ((WorldServerBridge) worldObjIn).bridge$updateConfigCache();

        this.impl$denyChunkRequests = worldCategory.getDenyChunkRequests();
        this.impl$chunkUnloadDelay = worldCategory.getChunkUnloadDelay() * 1000;
        this.impl$maxChunkUnloads = worldCategory.getMaxChunkUnloads();
    }

    @Override
    public void accessor$setChunkGenerator(final IChunkGenerator spongeGen) {
        this.chunkGenerator = spongeGen;
    }

    @Override
    public Long2ObjectMap<Chunk> accessor$getLoadedChunks() {
        return this.loadedChunks;
    }

    @Override
    public IChunkGenerator accessor$getChunkGenerator() {
        return this.chunkGenerator;
    }

    @Override
    public CompletableFuture<Boolean> bridge$doesChunkExistSync(final Vector3i chunkCoords) {
        return WorldStorageUtil.doesChunkExistSync(this.world, this.chunkLoader, chunkCoords);
    }

    /**
     * @author blood - October 25th, 2016
     * @reason Removes usage of droppedChunksSet in favor of unloaded flag.
     *
     * @param chunkIn The chunk to queue
     */
    @Overwrite
    public void queueUnload(final Chunk chunkIn)
    {
        if (!((ChunkBridge) chunkIn).bridge$isPersistedChunk() && this.world.field_73011_w.func_186056_c(chunkIn.field_76635_g, chunkIn.field_76647_h))
        {
            // Sponge - we avoid using the queue and simply check the unloaded flag during unloads
            //this.droppedChunksSet.add(Long.valueOf(ChunkPos.asLong(chunkIn.x, chunkIn.z)));
            chunkIn.field_189550_d = true;
        }
    }



    @Redirect(method = "provideChunk",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/gen/ChunkProviderServer;loadChunk(II)Lnet/minecraft/world/chunk/Chunk;"))
    @Nullable
    private Chunk impl$ProvideChunkForced(final ChunkProviderServer chunkProviderServer, final int x, final int z) {
        if (!this.impl$denyChunkRequests) {
            return this.loadChunk(x, z);
        }

        Chunk chunk = this.getLoadedChunk(x, z);
        if (chunk == null && this.impl$canDenyChunkRequest()) {
            return this.impl$EMPTY_CHUNK;
        }

        if (chunk == null) {
            chunk = this.bridge$loadChunkForce(x, z);
        }

        return chunk;
    }

    @Inject(method = "provideChunk",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/math/ChunkPos;asLong(II)J"))
    private void impl$StartTerrainGenerationPhase(final int x, final int z, final CallbackInfoReturnable<Chunk> cir) {
        GenerationPhase.State.TERRAIN_GENERATION.createPhaseContext()
            .world(this.world)
            .buildAndSwitch();
    }

    @Inject(method = "provideChunk",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/chunk/Chunk;populate(Lnet/minecraft/world/chunk/IChunkProvider;Lnet/minecraft/world/gen/IChunkGenerator;)V",
            shift = At.Shift.AFTER))
    private void impl$EndTerrainGenerationPhase(final int x, final int z, final CallbackInfoReturnable<Chunk> ci) {
        PhaseTracker.getInstance().getCurrentContext().close();
    }

    @Inject(method = "provideChunk",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/crash/CrashReportCategory;addCrashSection(Ljava/lang/String;Ljava/lang/Object;)V",
            ordinal = 2,
            shift = At.Shift.AFTER
        ),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void impl$StopGenerationPhaseFromError(final int x, final int z, final CallbackInfoReturnable<Chunk> cir, final Chunk ungenerated,
        final long chunkIndex, final Throwable error, final CrashReport report, final CrashReportCategory chunkGenerationCategory,
        @Coerce final Object provider, final int someVar, final int someOther) {

        final PhaseContext<?> currentContext = PhaseTracker.getInstance().getCurrentContext();
        report.func_85057_a("Current PhaseState", 1)
            .func_189529_a(currentContext.state.toString(), () -> {

                final PrettyPrinter printer = new PrettyPrinter(50);
                PhaseTracker.CONTEXT_PRINTER.accept(printer, currentContext);
                try (final PrintStream stream = new PrintStream(new ByteArrayOutputStream())) {
                    printer.print(stream);
                    return stream.toString();
                }
            });
        // Since we still want to complete the phase in the case we can recover, we still must close the current context.
        currentContext.close();
    }

    private boolean impl$canDenyChunkRequest() {
        if (!SpongeImpl.getServer().func_152345_ab()) {
            return true;
        }

        if (this.impl$forceChunkRequests) {
            return false;
        }

        final PhaseTracker phaseTracker = PhaseTracker.getInstance();
        final IPhaseState<?> currentState = phaseTracker.getCurrentState();
        return currentState.doesDenyChunkRequests();
    }

    @Override
    public boolean bridge$getForceChunkRequests() {
        return this.impl$forceChunkRequests;
    }

    @Override
    public void bridge$setMaxChunkUnloads(final int maxUnloads) {
        this.impl$maxChunkUnloads = maxUnloads;
    }

    @Override
    public void bridge$setForceChunkRequests(final boolean flag) {
        this.impl$forceChunkRequests = flag;
    }

    @Override
    public void bridge$setDenyChunkRequests(final boolean flag) {
        this.impl$denyChunkRequests = flag;
    }

    @Override
    public long bridge$getChunkUnloadDelay() {
        return this.impl$chunkUnloadDelay;
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
    public boolean tick()
    {
        if (!this.world.field_73058_d && !((WorldBridge) this.world).bridge$isFake())
        {
            ((WorldServerBridge) this.world).bridge$getTimingsHandler().doChunkUnload.startTiming();
            final Iterator<Chunk> iterator = this.loadedChunks.values().iterator();
            int chunksUnloaded = 0;
            final long now = System.currentTimeMillis();
            while (chunksUnloaded < this.impl$maxChunkUnloads && iterator.hasNext()) {
                final Chunk chunk = iterator.next();
                final ChunkBridge spongeChunk = (ChunkBridge) chunk;
                if (chunk != null && chunk.field_189550_d && !spongeChunk.bridge$isPersistedChunk()) {
                    if (this.bridge$getChunkUnloadDelay() > 0) {
                        if ((now - spongeChunk.bridge$getScheduledForUnload()) < this.impl$chunkUnloadDelay) {
                            continue;
                        }
                        spongeChunk.bridge$setScheduledForUnload(-1);
                    }
                    chunk.func_76623_d();
                    this.saveChunkData(chunk);
                    this.saveChunkExtraData(chunk);
                    iterator.remove();
                    chunksUnloaded++;
                }
            }
            ((WorldServerBridge) this.world).bridge$getTimingsHandler().doChunkUnload.stopTiming();
        }

        this.chunkLoader.func_75817_a();
        return false;
    }

    // Copy of getLoadedChunk without marking chunk active.
    // This allows the chunk to unload if currently queued.
    @Override
    public Chunk bridge$getLoadedChunkWithoutMarkingActive(final int x, final int z){
        final long i = ChunkPos.func_77272_a(x, z);
        return this.loadedChunks.get(i);
    }

    @Inject(method = "canSave", at = @At("HEAD"), cancellable = true)
    private void impl$IgnoreIfWorldSaveDisabled(final CallbackInfoReturnable<Boolean> cir) {
        if (((WorldProperties)this.world.func_72912_H()).getSerializationBehavior() == SerializationBehaviors.NONE) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "saveChunkData", at = @At("HEAD"), cancellable = true)
    private void impl$IgnoreIfWorldSaveDisabled(final Chunk chunkIn, final CallbackInfo ci) {
        if (((WorldProperties)this.world.func_72912_H()).getSerializationBehavior() == SerializationBehaviors.NONE) {
            ci.cancel();
        }
    }

    @Inject(method = "flushToDisk", at = @At("HEAD"), cancellable = true)
    private void impl$IgnoreIfWorldSaveDisabled(final CallbackInfo ci) {
        if (((WorldProperties)this.world.func_72912_H()).getSerializationBehavior() == SerializationBehaviors.NONE) {
            ci.cancel();
        }
    }

    @Override
    public void bridge$unloadChunkAndSave(final Chunk chunk) {
        boolean saveChunk = false;
        if (chunk.func_76601_a(true)) {
            saveChunk = true;
        }

        chunk.func_76623_d();

        if (saveChunk) {
            this.saveChunkData(chunk);
        }

        this.loadedChunks.remove(ChunkPos.func_77272_a(chunk.field_76635_g, chunk.field_76647_h));
        ((ChunkBridge) chunk).bridge$setScheduledForUnload(-1);
    }
}
