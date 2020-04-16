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
package org.spongepowered.server.mixin.chunkio;

import co.aikar.timings.Timing;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.chunkio.ChunkIOExecutor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkProviderServerBridge;
import org.spongepowered.server.bridge.world.chunkio.ChunkIOProviderBridge_Vanilla;

import java.util.function.Consumer;

import javax.annotation.Nullable;

@Mixin(value = ChunkProviderServer.class, priority = 1112)
public abstract class ChunkProviderServerMixin_ChunkIO implements IChunkProvider, ChunkIOProviderBridge_Vanilla, ChunkProviderServerBridge {

    @Shadow @Final private IChunkLoader chunkLoader;
    @Shadow @Final private WorldServer world;

    /**
     * @author Minecrell - May 28th, 2016
     * @reason Load chunk through asynchronous executor
     */
    @Nullable
    @Overwrite
    public Chunk loadChunk(int x, int z) {
        return vanillaBridge$loadChunk(x, z, null);
    }

    @Nullable
    @Override
    public Chunk vanillaBridge$loadChunk(int x, int z, @Nullable Consumer<Chunk> callback) {
        Chunk chunk = getLoadedChunk(x, z);

        if (chunk != null) {
            if (callback != null) {
                callback.accept(chunk);
            }

            return chunk;
        } else if (callback != null) {
            ChunkIOExecutor.queueChunkLoad(this.world, (AnvilChunkLoader) this.chunkLoader, (ChunkProviderServer) (Object) this, x, z, callback);
            return null;
        } else {
            return bridge$loadChunkForce(x, z); // Load chunk synchronously
        }
    }

    /**
     * @author Minecrell - October 25th, 2016
     * @reason Overwrite method in SpongeCommon to load chunks using the chunk IO executor
     */
    @Override
    public Chunk bridge$loadChunkForce(int x, int z) {
        Timing timing = ((WorldServerBridge) this.world).bridge$getTimingsHandler().syncChunkLoadDataTimer;
        try {
            return ChunkIOExecutor.syncChunkLoad(this.world, (AnvilChunkLoader) this.chunkLoader, (ChunkProviderServer) (Object) this, x, z);
        } finally {
            timing.stopTiming();
        }
    }

}
