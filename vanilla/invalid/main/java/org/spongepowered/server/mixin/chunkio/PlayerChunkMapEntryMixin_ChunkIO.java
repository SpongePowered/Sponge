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

import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.chunkio.ChunkIOExecutor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.server.bridge.world.chunkio.ChunkIOProviderBridge_Vanilla;

import java.util.function.Consumer;

import javax.annotation.Nullable;

@Mixin(PlayerChunkMapEntry.class)
public abstract class PlayerChunkMapEntryMixin_ChunkIO implements Consumer<Chunk> {

    @Shadow @Final public PlayerChunkMap playerChunkMap;
    @Shadow @Final public ChunkPos pos;
    @Shadow @Nullable public Chunk chunk;

    private boolean chunkIO$loading;

    @Nullable
    private Chunk chunkIO$loadChunkAsync(ChunkProviderServer provider, int x, int z) {
        if (this.chunkIO$loading) {
            return null;
        }

        this.chunkIO$loading = true;
        return ((ChunkIOProviderBridge_Vanilla) provider).vanillaBridge$loadChunk(x, z, this);
    }

    @Override
    public void accept(@Nullable Chunk chunk) {
        this.chunk = chunk;
        this.chunkIO$loading = false;

        if (chunk != null) {
            ((ChunkBridge) chunk).bridge$setScheduledForUnload(-1);
        }
    }

    @Nullable
    @Redirect(method = "<init>",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/ChunkProviderServer;loadChunk(II)Lnet/minecraft/world/chunk/Chunk;"))
    private Chunk chunkIO$onLoadChunkInit(ChunkProviderServer provider, int x, int z) {
        return chunkIO$loadChunkAsync(provider, x, z);
    }

    @Inject(method = "removePlayer",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerChunkMap;removeEntry"
            + "(Lnet/minecraft/server/management/PlayerChunkMapEntry;)V"))
    private void chunkIO$onRemoveEntry(CallbackInfo ci) {
        if (this.chunkIO$loading) {
            // Don't load the chunk if we haven't loaded it yet
            ChunkIOExecutor.dropQueuedChunkLoad(this.playerChunkMap.getWorldServer(), this.pos.x, this.pos.z, this);
        }
    }

    @Nullable
    @Redirect(method = "providePlayerChunk",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/ChunkProviderServer;provideChunk(II)Lnet/minecraft/world/chunk/Chunk;"))
    private Chunk chunkIO$onProvideChunk(ChunkProviderServer provider, int x, int z) {
        return this.chunkIO$loading ? null : provider.provideChunk(x, z); // Don't try to generate while still attempting to load
    }

    @Nullable
    @Redirect(method = "providePlayerChunk",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/ChunkProviderServer;loadChunk(II)Lnet/minecraft/world/chunk/Chunk;"))
    private Chunk chunkIO$onLoadChunkProvide(ChunkProviderServer provider, int x, int z) {
        return chunkIO$loadChunkAsync(provider, x, z);
    }

}
