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
package org.spongepowered.common.bridge.world.chunk;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkGenerator;
import org.spongepowered.common.world.gen.SpongeChunkGenerator;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

/**
 * Specific bridge for the {@link ChunkProviderServer}, with a direct
 * pairing to {@link WorldServer} as a hard requirement.
 */
public interface ChunkProviderServerBridge {

    // TODO Mixin 0.8
    @Nullable
    @Deprecated
    ChunkGenerator accessor$getChunkGenerator();

    // TODO Mixin 0.8
    @Deprecated
    void accessor$setChunkGenerator(ChunkGenerator spongeGen);

    // TODO Mixin 0.8
    @Deprecated
    Long2ObjectMap<Chunk> accessor$getLoadedChunks();

    CompletableFuture<Boolean> bridge$doesChunkExistSync(Vector3i chunkCoords);

    boolean bridge$getForceChunkRequests();

    void bridge$setDenyChunkRequests(boolean flag);

    void bridge$setForceChunkRequests(boolean flag);

    void bridge$unloadChunkAndSave(Chunk chunk);

    long bridge$getChunkUnloadDelay();

    /**
     * Used strictly for implementation, because this method
     * is used in various other places, SpongeForge needs to
     * override this specifically for forge's async chunk loading
     * and SpongeVanilla has to use this specially from it's own
     * ported implementation.
     *
     * @return The chunk loaded forcefully
     * look at ChunkProviderServerMixin#impl$ProvideChunkForced(Chunk, int, int)
     */
    Chunk bridge$loadChunkForce(final int x, final int z);
}
