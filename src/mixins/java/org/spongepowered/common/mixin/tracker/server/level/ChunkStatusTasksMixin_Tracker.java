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
package org.spongepowered.common.mixin.tracker.server.level;

import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatusTasks;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.level.LevelBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.generation.GenerationPhase;
import org.spongepowered.common.util.PrettyPrinter;


@Mixin(ChunkStatusTasks.class)
public abstract class ChunkStatusTasksMixin_Tracker {

    @Redirect(method = "lambda$full$2",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;runPostLoad()V"),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;setFullStatus(Ljava/util/function/Supplier;)V"),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;registerAllBlockEntitiesAfterLevelLoad()V")
            )
    )
    private static void tracker$startLoad(final LevelChunk chunk) {
        chunk.runPostLoad();
        final boolean isFake = ((LevelBridge) chunk.getLevel()).bridge$isFake();
        if (isFake) {
            return;
        }
        if (!PhaseTracker.SERVER.onSidedThread()) {
            new PrettyPrinter(60).add("Illegal Async Chunk Load").centre().hr()
                    .addWrapped("Sponge relies on knowing when chunks are being loaded as chunks add entities"
                            + " to the parented world for management. These operations are generally not"
                            + " threadsafe and shouldn't be considered a \"Sponge bug \". Adding/removing"
                            + " entities from another thread to the world is never ok.")
                    .add()
                    .add(" %s : %s", "Chunk Pos", chunk.getPos().toString())
                    .add()
                    .add(new Exception("Async Chunk Load Detected"))
                    .log(SpongeCommon.logger(), Level.ERROR);
            return;
        }
        if (PhaseTracker.getInstance().getCurrentState() == GenerationPhase.State.CHUNK_REGENERATING_LOAD_EXISTING) {
            return;
        }
        GenerationPhase.State.CHUNK_LOADING.createPhaseContext(PhaseTracker.getInstance())
                .source(chunk)
                .world((ServerLevel) chunk.getLevel())
                .chunk(chunk)
                .buildAndSwitch();
    }

    @Inject(method = "lambda$full$2",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;registerAllBlockEntitiesAfterLevelLoad()V", shift = At.Shift.BY, by = 2),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;runPostLoad()V")
            ),
            require = 1
    )
    private static void tracker$endLoad(final ChunkAccess $$0x, final WorldGenContext $$1x, final GenerationChunkHolder $$2x,
            final CallbackInfoReturnable<ChunkAccess> cir) {
        if (!((LevelBridge) $$1x.level()).bridge$isFake() && PhaseTracker.SERVER.onSidedThread()) {
            if (PhaseTracker.getInstance().getCurrentState() == GenerationPhase.State.CHUNK_REGENERATING_LOAD_EXISTING) {
                return;
            }
            // IF we're not on the main thread,
            PhaseTracker.getInstance().getPhaseContext().close();
        }
    }

}
