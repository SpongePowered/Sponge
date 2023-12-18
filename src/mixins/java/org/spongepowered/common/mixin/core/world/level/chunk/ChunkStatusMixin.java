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

import com.mojang.datafixers.util.Either;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.common.world.level.chunk.SpongeUnloadedChunkException;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

@Mixin(ChunkStatus.class)
public abstract class ChunkStatusMixin {

    /**
     * @author aromaa - December 17th, 2023 - 1.19.4
     * @author aromaa - December 18th, 2023 - Updated to 1.20.2
     * @reason Fixes a deadlock when the world is unloading/unloaded.
     * The Blender#of method calls to the IOWorker#isOldChunkAround which
     * submits a task to the main thread while blocking the current thread
     * to wait for a response. This fails when the IOWorker has been closed
     * as it no longer responds to further work and causes the thread to block
     * indefinitely. Fixes this by special casing the IOWorker#isOldChunkAround
     * to throw a special exception when the IOWorker has finished up its work
     * and catches it here to convert it to a ChunkLoadingFailure.
     *
     * See IOWorkerMixin#createOldDataForRegion
     */
    @Overwrite
    private static CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> lambda$static$6(
            final ChunkStatus $$0, final Executor $$1, final ServerLevel $$2, final ChunkGenerator $$3, final StructureTemplateManager $$4,
            final ThreadedLevelLightEngine $$5, final Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> $$6,
            final List<ChunkAccess> $$7, final ChunkAccess $$8) {
            final WorldGenRegion $$9 = new WorldGenRegion($$2, $$7, $$0, -1);
        try { //Sponge: Add try
            return $$3.createBiomes($$1, $$2.getChunkSource().randomState(), Blender.of($$9), $$2.structureManager().forWorldGenRegion($$9), $$8)
                    .thenApply($$0x -> Either.left($$0x));
        } catch (final Exception e) { //Sponge start: Add catch
            if (e.getCause() != SpongeUnloadedChunkException.INSTANCE) {
                throw e;
            }

            return ChunkHolder.UNLOADED_CHUNK_FUTURE;
        } //Sponge end
    }
}
