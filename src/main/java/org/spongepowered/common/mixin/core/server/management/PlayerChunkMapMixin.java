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
package org.spongepowered.common.mixin.core.server.management;

import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.server.management.PlayerChunkMapBridge;
import org.spongepowered.common.bridge.server.management.PlayerChunkMapEntryBridge;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;

import javax.annotation.Nullable;

@Mixin(PlayerChunkMap.class)
public abstract class PlayerChunkMapMixin implements PlayerChunkMapBridge {

    @Shadow @Final private ServerWorld world;

    @Shadow @Nullable public abstract PlayerChunkMapEntry getEntry(int chunkX, int chunkZ);

    @Shadow private int playerViewRadius;

    @Override
    public boolean bridge$isChunkInUse(final int x, final int z) {
        final PlayerChunkMapEntry playerInstance = this.getEntry(x, z);
        return playerInstance != null && ((PlayerChunkMapEntryBridge) playerInstance).accessor$getPlayers().size() > 0;
    }

    @Override
    public int accessor$getViewDistance() {
        return this.playerViewRadius;
    }

    @Redirect(method = "removeEntry", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/ChunkProviderServer;"
            + "queueUnload(Lnet/minecraft/world/chunk/Chunk;)V"))
    private void impl$ScheduleUnloadWithChunkGC(final ServerChunkProvider chunkProvider, final Chunk chunk) {
        // We remove the ability for a PlayerChunkMap to queue chunks for unload to prevent chunk thrashing
        // where the same chunks repeatedly unload and load. This is caused by a player moving in and out of the same chunks.
        // Instead, the Chunk GC will now be responsible for going through loaded chunks and queuing any chunk where no player
        // is within view distance or a spawn chunk is force loaded. However, if the Chunk GC is disabled then we will fall back to vanilla
        // and queue the chunk to be unloaded.
        // -- blood

        if (((WorldServerBridge) this.world).bridge$getChunkGCTickInterval() <= 0
                || ((WorldServerBridge) this.world).bridge$getChunkUnloadDelay() <= 0) {
            chunkProvider.func_189549_a(chunk);
        } else if (!((ChunkBridge) chunk).bridge$isPersistedChunk() && this.world.field_73011_w.func_186056_c(chunk.field_76635_g, chunk.field_76647_h)) {
            ((ChunkBridge) chunk).bridge$setScheduledForUnload(System.currentTimeMillis());
        }
    }
}
