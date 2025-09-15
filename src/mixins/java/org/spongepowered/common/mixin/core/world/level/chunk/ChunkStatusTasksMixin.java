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
package org.spongepowered.common.mixin.core.world.level.chunk;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStatusTasks;
import net.minecraft.world.level.chunk.status.ChunkStep;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.world.chunk.ChunkEvent;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.chunk.BlockChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.level.chunk.LevelChunkBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.DirectionUtil;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.level.chunk.SpongeUnloadedChunkException;
import org.spongepowered.math.vector.Vector3i;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkStatusTasks.class)
public abstract class ChunkStatusTasksMixin {

    /**
     * @author aromaa - December 17th, 2023 - 1.19.4
     * @author aromaa - December 18th, 2023 - Updated to 1.20.2
     * @author aromaa - May 14th, 2024 - Updated to 1.20.6
     * @author aromaa - June 1st, 2024 - Updated to 1.21
     * @reason Fixes a deadlock when the world is unloading/unloaded.
     * The Blender#of method calls to the IOWorker#isOldChunkAround which
     * submits a task to the main thread while blocking the current thread
     * to wait for a response. This fails when the IOWorker has been closed
     * as it no longer responds to further work and causes the thread to block
     * indefinitely. Fixes this by special casing the IOWorker#isOldChunkAround
     * to throw a special exception when the IOWorker has finished up its work
     * and catches it here to convert it to a CompletableFuture.
     *
     * In previous versions you were able to return ChunkResult here but this
     * is no longer the case.
     *
     * See IOWorkerMixin#createOldDataForRegion and GenerationChunkHolderMixin#impl$guardForUnloadedChunkOnGenerate
     */
    @Overwrite
    static CompletableFuture<ChunkAccess> generateBiomes(final WorldGenContext context, final ChunkStep step, final StaticCache2D<GenerationChunkHolder> cache, final ChunkAccess chunkAccess) {
        ServerLevel level = context.level();
        WorldGenRegion region = new WorldGenRegion(level, cache, step, chunkAccess);
        try { //Sponge: Add try
            return context.generator().createBiomes(level.getChunkSource().randomState(), Blender.of(region), level.structureManager().forWorldGenRegion(region), chunkAccess);
        } catch (final Exception e) { //Sponge start: Add catch
            if (e.getCause() != SpongeUnloadedChunkException.INSTANCE) {
                throw e;
            }

            return SpongeUnloadedChunkException.INSTANCE_FUTURE;
        } //Sponge end
    }

    @Inject(method = "lambda$full$2", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;setLoaded(Z)V"))
    private static void impl$onLoad(final CallbackInfoReturnable<ChunkAccess> cir, @Local() final LevelChunk levelChunk) {
        final Vector3i chunkPos = VecHelper.toVector3i(levelChunk.getPos());
        if (ShouldFire.CHUNK_EVENT_BLOCKS_LOAD) {
            final ChunkEvent.Blocks.Load loadEvent = SpongeEventFactory.createChunkEventBlocksLoad(PhaseTracker.getInstance().currentCause(),
                    ((BlockChunk) levelChunk), chunkPos, (ResourceKey) (Object) levelChunk.getLevel().dimension().location());
            SpongeCommon.post(loadEvent);
        }

        for (final Direction dir : Constants.Chunk.CARDINAL_DIRECTIONS) {
            final Vector3i neighborPos = chunkPos.add(dir.asBlockOffset());
            ChunkAccess neighbor = levelChunk.getLevel().getChunk(neighborPos.x(), neighborPos.z(), ChunkStatus.EMPTY, false);
            if (neighbor instanceof ImposterProtoChunk) {
                neighbor = ((ImposterProtoChunk) neighbor).getWrapped();
            }
            if (neighbor instanceof LevelChunk) {
                final int index = DirectionUtil.directionToIndex(dir);
                final int oppositeIndex = DirectionUtil.directionToIndex(dir.opposite());
                ((LevelChunkBridge) levelChunk).bridge$setNeighborChunk(index, (LevelChunk) neighbor);
                ((LevelChunkBridge) neighbor).bridge$setNeighborChunk(oppositeIndex, levelChunk);
            }
        }
    }
}
