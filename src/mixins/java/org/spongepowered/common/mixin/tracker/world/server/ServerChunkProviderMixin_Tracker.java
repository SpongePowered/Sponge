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
package org.spongepowered.common.mixin.tracker.world.server;

import net.minecraft.entity.EntityClassification;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.WorldEntitySpawner;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.generation.GenerationPhase;

@Mixin(ServerChunkProvider.class)
public abstract class ServerChunkProviderMixin_Tracker {
    
    @Redirect(
        method = "*",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/spawner/WorldEntitySpawner;spawnEntitiesInChunk(Lnet/minecraft/entity/EntityClassification;Lnet/minecraft/world/server/ServerWorld;Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/util/math/BlockPos;)V"
        )
    )
    private void tracker$wrapEntitySpawner(
        final EntityClassification classification,
        final ServerWorld serverWorld,
        final Chunk targetChunk,
        final BlockPos targetPosition
    ) {
        try (final PhaseContext<@NonNull ?> context = GenerationPhase.State.WORLD_SPAWNER_SPAWNING.createPhaseContext(PhaseTracker.SERVER)
            .world(serverWorld)) {
            context.buildAndSwitch();
            WorldEntitySpawner.spawnEntitiesInChunk(classification, serverWorld, targetChunk, targetPosition);
        }
    }
    @Redirect(
        method = "tickChunks",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/gen/ChunkGenerator;spawnMobs(Lnet/minecraft/world/server/ServerWorld;ZZ)V"
        )
    )
    private void tracker$wrapGeneratorEntitySpawner(
        final ChunkGenerator<?> generator,
        final ServerWorld serverWorld,
        final boolean spawnHostileMobs,
        final boolean spawnPeacefulMobs
    ) {
        try (final PhaseContext<@NonNull ?> context = GenerationPhase.State.WORLD_SPAWNER_SPAWNING.createPhaseContext(PhaseTracker.SERVER)
            .world(serverWorld)) {
            context.buildAndSwitch();
            generator.spawnMobs(serverWorld, spawnHostileMobs, spawnPeacefulMobs);
        }
    }
}
