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

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStatusTasks;
import net.minecraft.world.level.chunk.status.ToFullChunk;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.common.world.level.chunk.SpongeUnloadedChunkException;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ChunkStatusTasks.class)
public abstract class ChunkStatusTasksMixin {

    /**
     * @author aromaa - December 17th, 2023 - 1.19.4
     * @author aromaa - December 18th, 2023 - Updated to 1.20.2
     * @author aromaa - May 14th, 2024 - Updated to 1.20.6
     * @reason Fixes a deadlock when the world is unloading/unloaded.
     * The Blender#of method calls to the IOWorker#isOldChunkAround which
     * submits a task to the main thread while blocking the current thread
     * to wait for a response. This fails when the IOWorker has been closed
     * as it no longer responds to further work and causes the thread to block
     * indefinitely. Fixes this by special casing the IOWorker#isOldChunkAround
     * to throw a special exception when the IOWorker has finished up its work
     * and catches it here to convert it to a ChunkLoadingFailure.
     *
     * In previous versions you were able to return ChunkResult here but this
     * is no longer the case, so we instead return null here which
     * needs to be checking for in ChunkMap.
     *
     * See IOWorkerMixin#createOldDataForRegion
     */
    @Overwrite
    static CompletableFuture<ChunkAccess> generateBiomes(final WorldGenContext $$0, final ChunkStatus $$1, final Executor $$2,
            final ToFullChunk $$3, final List<ChunkAccess> $$4, final ChunkAccess $$5) {
        ServerLevel $$6 = $$0.level();
        WorldGenRegion $$7 = new WorldGenRegion($$6, $$4, $$1, -1);
        try { //Sponge: Add try
            return $$0.generator().createBiomes($$2, $$6.getChunkSource().randomState(), Blender.of($$7), $$6.structureManager().forWorldGenRegion($$7), $$5);
        } catch (final Exception e) { //Sponge start: Add catch
            if (e.getCause() != SpongeUnloadedChunkException.INSTANCE) {
                throw e;
            }

            return null;
        } //Sponge end
    }
}
