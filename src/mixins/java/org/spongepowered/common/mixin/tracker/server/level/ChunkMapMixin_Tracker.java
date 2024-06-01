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

import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.apache.logging.log4j.Level;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.applaunch.config.core.SpongeConfigs;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhasePrinter;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.generation.GenerationPhase;
import org.spongepowered.common.util.PrettyPrinter;


@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin_Tracker {

    @Shadow @Final ServerLevel level;

    @Redirect(method = "addEntity(Lnet/minecraft/world/entity/Entity;)V",
        at = @At(value = "NEW", args = "class=java/lang/IllegalStateException", remap = false))
    private IllegalStateException tracker$reportEntityAlreadyTrackedWithWorld(final String string, final Entity entityIn) {
        final IllegalStateException exception = new IllegalStateException(String.format("Entity %s is already tracked for world: %s", entityIn,
                ((org.spongepowered.api.world.server.ServerWorld) this.level).key()));
        if (SpongeConfigs.getCommon().get().phaseTracker.verboseErrors) {
            PhasePrinter.printMessageWithCaughtException(PhaseTracker.getInstance(), "Exception tracking entity", "An entity that was already tracked was added to the tracker!", exception);
        }
        return exception;
    }

    @Redirect(method = "lambda$prepareTickingChunk$27",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;startTickingChunk(Lnet/minecraft/world/level/chunk/LevelChunk;)V"))
    private void tracker$wrapUnpackTicks(final ServerLevel level, final LevelChunk chunk) {
        if (!PhaseTracker.SERVER.onSidedThread()) {
            new PrettyPrinter(60).add("Illegal Async Chunk Unpacking").centre().hr()
                .addWrapped("Someone is attempting to unpack chunk scheduled updates while off the main thread, this is" +
                    "generally unsupported and Sponge would appreciate a report about this. Please attach " +
                    "the generated classes output as a zip file after enabling -Dmixin.debug.export=true " +
                    "and request triage support on discord. Do NOT report as an issue on GitHub.")
                .add()
                .add(" %s : %s", "Chunk Pos", chunk.getPos().toString())
                .add()
                .add(new Exception("Async Chunk Scheduling Detected"))
                .log(SpongeCommon.logger(), Level.ERROR);
            return;
        }
        if (PhaseTracker.getInstance().getCurrentState() == GenerationPhase.State.CHUNK_LOADING) {
            return;
        }
        try (final PhaseContext<@NonNull ?> ctx = GenerationPhase.State.CHUNK_LOADING.createPhaseContext(PhaseTracker.getInstance())
            .source(chunk)
            .world(level)
            .chunk(chunk)) {
            ctx.buildAndSwitch();
            chunk.unpackTicks(level.getLevelData().getGameTime());
        }

    }

}
