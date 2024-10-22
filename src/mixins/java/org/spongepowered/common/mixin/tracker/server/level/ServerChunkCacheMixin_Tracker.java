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

import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.LevelChunk;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.generation.GenerationPhase;

import java.util.List;

@Mixin(ServerChunkCache.class)
public abstract class ServerChunkCacheMixin_Tracker {

    @Redirect(
        method = "tickChunks(Lnet/minecraft/util/profiling/ProfilerFiller;JLjava/util/List;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/NaturalSpawner;spawnForChunk(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/NaturalSpawner$SpawnState;Ljava/util/List;)V"
        )
    )
    private void tracker$wrapEntitySpawner(
        final ServerLevel serverWorld,
        final LevelChunk targetChunk,
        final NaturalSpawner.SpawnState spawnState,
        final List<MobCategory> mobCategories
    ) {
        try (final PhaseContext<@NonNull ?> context = GenerationPhase.State.WORLD_SPAWNER_SPAWNING.createPhaseContext(PhaseTracker.SERVER)
            .world(serverWorld)) {
            context.buildAndSwitch();
            NaturalSpawner.spawnForChunk(serverWorld, targetChunk, spawnState, mobCategories);
        }
    }

    @Redirect(
        method = "tickChunks(Lnet/minecraft/util/profiling/ProfilerFiller;JLjava/util/List;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;tickCustomSpawners(ZZ)V"
        )
    )
    private void tracker$wrapGeneratorEntitySpawner(
        final ServerLevel serverWorld,
        final boolean spawnHostileMobs,
        final boolean spawnPeacefulMobs
    ) {
        try (final PhaseContext<@NonNull ?> context = GenerationPhase.State.WORLD_SPAWNER_SPAWNING.createPhaseContext(PhaseTracker.SERVER)
            .world(serverWorld)) {
            context.buildAndSwitch();
            serverWorld.tickCustomSpawners(spawnHostileMobs, spawnPeacefulMobs);
        }
    }
}
