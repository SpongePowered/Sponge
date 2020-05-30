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
package org.spongepowered.common.util;

import com.google.common.io.Files;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SChunkDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.SaveHandler;
import org.spongepowered.api.world.ChunkRegenerateFlag;
import org.spongepowered.api.world.chunk.Chunk;
import org.spongepowered.common.bridge.world.chunk.ServerChunkProviderBridge;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.generation.GenerationPhase;
import org.spongepowered.math.vector.Vector3i;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import javax.annotation.Nullable;

public class ChunkUtil {

    private ChunkUtil() {
    }

    public static Optional<Chunk> regenerateChunk(org.spongepowered.api.world.server.ServerWorld world, int cx, int cy, int cz, ChunkRegenerateFlag flag) {
        Chunk spongeChunk;
        try (final PhaseContext<?> context = GenerationPhase.State.CHUNK_REGENERATING_LOAD_EXISTING.createPhaseContext(PhaseTracker.SERVER)
                .world((World)(Object) world)) {
            context.buildAndSwitch();
            spongeChunk = world.loadChunk(cx, cy, cz, false).orElse(null);
        }

        if (spongeChunk == null) {
            if (!flag.create()) {
                return Optional.empty();
            }
            // This should generate a chunk so there won't be a need to regenerate one
            return world.loadChunk(cx, cy, cz, true);
        }

        final net.minecraft.world.chunk.Chunk chunk = (net.minecraft.world.chunk.Chunk) spongeChunk;
        final boolean keepEntities = flag.entities();
        try (final PhaseContext<?> context = GenerationPhase.State.CHUNK_REGENERATING.createPhaseContext(PhaseTracker.SERVER).chunk(chunk)) {
            context.buildAndSwitch();
            // If we reached this point, an existing chunk was found so we need to regen
            for (final ClassInheritanceMultiMap<Entity> multiEntityList : chunk.getEntityLists()) {
                for (final Entity entity : multiEntityList) {
                    if (!keepEntities && !(entity instanceof ServerPlayerEntity)) {
                        entity.remove();
                    }
                }
            }

            final ServerChunkProvider chunkProviderServer = (ServerChunkProvider) chunk.getWorld().getChunkProvider();
            ((ServerChunkProviderBridge) chunkProviderServer).bridge$unloadChunkAndSave(chunk);

            File saveFolder = Files.createTempDir();
            // register this just in case something goes wrong
            // normally it should be deleted at the end of this method
            saveFolder.deleteOnExit();
            try {
                ServerWorld originalWorld = (ServerWorld) (Object) world;

                MinecraftServer server = originalWorld.getServer();
                SaveHandler saveHandler = new SaveHandler(saveFolder, originalWorld.getSaveHandler().getWorldDirectory().getName(), server, server.getDataFixer());
                try (World freshWorld = new ServerWorld(server, server.getBackgroundExecutor(), saveHandler, originalWorld.getWorldInfo(),
                        originalWorld.dimension.getType(), originalWorld.getProfiler(), new NoOpChunkStatusListener())) {

                    // Also generate chunks around this one
                    for (int z = cz - 1; z <= cz + 1; z++) {
                        for (int x = cx - 1; x <= cx + 1; x++) {
                            freshWorld.getChunk(x, z);
                        }
                    }

                    Vector3i blockMin = spongeChunk.getBlockMin();
                    Vector3i blockMax = spongeChunk.getBlockMax();

                    for (int z = blockMin.getZ(); z <= blockMax.getZ(); z++) {
                        for (int y = blockMin.getY(); y <= blockMax.getY(); y++) {
                            for (int x = blockMin.getX(); x <= blockMax.getX(); x++) {
                                final BlockPos pos = new BlockPos(x, y, z);
                                chunk.setBlockState(pos, freshWorld.getBlockState(pos), false);
                                // TODO performance? will this send client updates?
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } finally {
                saveFolder.delete();
            }

            chunkProviderServer.chunkManager.getTrackingPlayers(new ChunkPos(cx, cz), false).forEach(player -> {
                // We deliberately send two packets, to avoid sending a 'fullChunk' packet
                // (a changedSectionFilter of 65535). fullChunk packets cause the client to
                // completely overwrite its current chunk with a new chunk instance. This causes
                // weird issues, such as making any entities in that chunk invisible (until they leave it
                // for a new chunk)
                // - Aaron1011
                player.connection.sendPacket(new SChunkDataPacket(chunk, 65534));
                player.connection.sendPacket(new SChunkDataPacket(chunk, 1));
            });

            return Optional.of((Chunk) chunk);
        }
    }

    public static class NoOpChunkStatusListener implements IChunkStatusListener {

        @Override
        public void start(ChunkPos center) {
        }

        @Override
        public void statusChanged(ChunkPos p_219508_1_, @Nullable ChunkStatus p_219508_2_) {
        }

        @Override
        public void stop() {
        }
    }
}
