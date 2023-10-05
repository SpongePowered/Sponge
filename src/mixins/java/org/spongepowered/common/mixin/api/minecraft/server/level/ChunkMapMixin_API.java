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
package org.spongepowered.common.mixin.api.minecraft.server.level;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.ChunkPos;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.ChunkRegenerateFlag;
import org.spongepowered.api.world.chunk.WorldChunk;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.server.Ticket;
import org.spongepowered.api.world.server.TicketType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.server.ChunkMapBridge;
import org.spongepowered.common.util.MissingImplementationException;
import org.spongepowered.math.vector.Vector2i;
import org.spongepowered.math.vector.Vector3i;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin_API implements org.spongepowered.api.world.server.ChunkManager {

    // @formatter:off
    @Shadow @Final private net.minecraft.server.level.ServerLevel level;

    // @formatter:on

    @Override
    public @NonNull ServerWorld world() {
        return (ServerWorld) this.level;
    }

    @Override
    public boolean valid(final @NonNull Ticket<?> ticket) {
        return ((ChunkMapBridge) this).bridge$distanceManager().bridge$checkTicketValid(ticket);
    }

    @Override
    public @NonNull Ticks timeLeft(final @NonNull Ticket<?> ticket) {
        return ((ChunkMapBridge) this).bridge$distanceManager().bridge$timeLeft(ticket);
    }

    @Override
    public <T> @NonNull Optional<Ticket<T>> requestTicket(final @NonNull TicketType<T> type, final @NonNull Vector3i chunkPosition,
            final @NonNull T value, final int radius) {
        if (!(type instanceof net.minecraft.server.level.TicketType)) {
            throw new IllegalArgumentException("TicketType must be a Minecraft TicketType");
        }
        if (radius < 1) {
            throw new IllegalArgumentException("The radius must be positive.");
        }
        return ((ChunkMapBridge) this).bridge$distanceManager()
                .bridge$registerTicket(this.world(), type, chunkPosition, value, radius);
    }

    @Override
    public boolean renewTicket(final @NonNull Ticket<?> ticket) {
        return ((ChunkMapBridge) this).bridge$distanceManager().bridge$renewTicket(ticket);
    }

    @Override
    public boolean releaseTicket(final @NonNull Ticket<?> ticket) {
        return ((ChunkMapBridge) this).bridge$distanceManager().bridge$releaseTicket(ticket);
    }

    @Override
    public <T> @NonNull Collection<Ticket<T>> findTickets(final @NonNull TicketType<T> type) {
        return ((ChunkMapBridge) this).bridge$distanceManager().bridge$tickets(type);
    }

    @Override
    public @NonNull CompletableFuture<Boolean> regenerateChunk(final int cx, final int cy, final int cz, final @NonNull ChunkRegenerateFlag flag) {
        final CompletableFuture<Boolean> cf = new CompletableFuture<>();
        cf.completeExceptionally(new MissingImplementationException("ServerWorld", "regenerateChunk"));
        return cf;
    }

    @Override
    public Path regionDirPath()
    {
        return ((ChunkMapBridge) this).bridge$dimensionPath().resolve("region");
    }

    @Override
    public Path regionFilePath(WorldChunk chunk)
    {
        final ChunkPos chunkPos = new ChunkPos(chunk.chunkPosition().x(), chunk.chunkPosition().z());
        return this.regionDirPath().resolve("r." + chunkPos.getRegionX() + "." + chunkPos.getRegionZ() + ".mca");
    }

    @Override
    public Vector3i maxChunkFromRegion(int rx, int rz) {
        final ChunkPos chunkPos = ChunkPos.maxFromRegion(rx, rz);
        return new Vector3i(chunkPos.x, 0, chunkPos.z);
    }

    @Override
    public Vector3i minChunkFromRegion(int rx, int rz) {
        final ChunkPos chunkPos = ChunkPos.minFromRegion(rx, rz);
        return new Vector3i(chunkPos.x, 0, chunkPos.z);
    }

    @Override
    public Map<Vector2i, Path> regionFiles()
    {
        Map<Vector2i, Path> paths = new HashMap<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(this.regionDirPath(), "*.mca")) {
            stream.forEach(path -> paths.put(this.api$pathToRegionPos(path), path));
        } catch (IOException ex) {
            SpongeCommon.logger().error("Could not find region files", ex);
        }
        return paths;
    }

    private Vector2i api$pathToRegionPos(Path regionPath)
    {
        final String[] split = regionPath.getFileName().toString().split("\\.");
        return new Vector2i(Integer.parseInt(split[1]), Integer.parseInt(split[2]));
    }

}
