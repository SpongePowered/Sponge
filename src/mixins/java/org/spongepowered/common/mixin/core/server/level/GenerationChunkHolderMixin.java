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
package org.spongepowered.common.mixin.core.server.level;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStep;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.world.chunk.ChunkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.server.level.ChunkMapAccessor;
import org.spongepowered.common.bridge.server.level.ServerLevelBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.level.chunk.SpongeUnloadedChunkException;
import org.spongepowered.math.vector.Vector3i;

import java.util.concurrent.CompletableFuture;

@Mixin(GenerationChunkHolder.class)
public abstract class GenerationChunkHolderMixin {

    /**
     * See IOWorkerMixin#createOldDataForRegion and ChunkStatusTasksMixin#generateBiomes
     */
    @Inject(method = "lambda$applyStep$0", at = @At("HEAD"), cancellable = true)
    private void impl$guardForUnloadedChunkOnGenerate(final ChunkStep $$0x, final ChunkAccess $$1x, final Throwable $$2x, final CallbackInfoReturnable<ChunkResult<ChunkAccess>> cir) {
        if ($$2x != null && $$2x.getCause() == SpongeUnloadedChunkException.INSTANCE) {
            cir.setReturnValue(GenerationChunkHolder.UNLOADED_CHUNK);
        }
    }

    @Inject(method = "replaceProtoChunk(Lnet/minecraft/world/level/chunk/ImposterProtoChunk;)V", at = @At("TAIL"))
    private void impl$throwChunkGeneratedEvent(final ImposterProtoChunk imposter, final CallbackInfo ci) {
        if (!ShouldFire.CHUNK_EVENT_GENERATED) {
            return;
        }
        final LevelChunk chunk = imposter.getWrapped();
        final Vector3i chunkPos = VecHelper.toVector3i(chunk.getPos());
        final ChunkEvent.Generated event = SpongeEventFactory.createChunkEventGenerated(
                PhaseTracker.getInstance().currentCause(), chunkPos,
                (ResourceKey) (Object) chunk.getLevel().dimension().location()
        );
        SpongeCommon.post(event);
    }

    @Inject(method = "scheduleChunkGenerationTask", at = @At("HEAD"), cancellable = true)
    private void impl$onGetOrScheduleFuture(final ChunkStatus $$0, final ChunkMap chunkMap, final CallbackInfoReturnable<CompletableFuture<ChunkResult<ChunkAccess>>> cir) {
        if (!((ServerLevelBridge) ((ChunkMapAccessor) chunkMap).accessor$level()).bridge$isLoaded()) {
            cir.setReturnValue(ChunkHolder.UNLOADED_CHUNK_FUTURE);
        }
    }

}
