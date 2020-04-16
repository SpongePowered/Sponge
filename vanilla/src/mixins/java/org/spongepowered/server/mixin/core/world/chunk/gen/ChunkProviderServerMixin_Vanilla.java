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
package org.spongepowered.server.mixin.core.world.chunk.gen;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.chunk.ChunkProviderServerBridge;

import javax.annotation.Nullable;

/**
 * Only needed to implement {@link #bridge$loadChunkForce(int, int)} since
 * Forge has a different implementation required for this.
 */
@Mixin(ChunkProviderServer.class)
public abstract class ChunkProviderServerMixin_Vanilla implements ChunkProviderServerBridge {

    @Shadow @Final public Long2ObjectMap<Chunk> loadedChunks;
    @Shadow public IChunkGenerator chunkGenerator;

    @Shadow @Nullable protected abstract Chunk loadChunkFromFile(int x, int z);

    @Override
    public Chunk bridge$loadChunkForce(int x, int z) {
        Chunk chunk = this.loadChunkFromFile(x, z);

        if (chunk != null)
        {
            this.loadedChunks.put(ChunkPos.asLong(x, z), chunk);
            chunk.onLoad();
            chunk.populate((ChunkProviderServer) (Object) this, this.chunkGenerator);
        }

        return chunk;
    }
}
