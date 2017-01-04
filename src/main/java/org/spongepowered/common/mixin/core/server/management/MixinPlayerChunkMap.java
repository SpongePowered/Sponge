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
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.server.management.IMixinPlayerChunkMap;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

import javax.annotation.Nullable;

@Mixin(PlayerChunkMap.class)
public abstract class MixinPlayerChunkMap implements IMixinPlayerChunkMap {

    @Shadow @Final private WorldServer theWorldServer;

    @Shadow @Nullable public abstract PlayerChunkMapEntry getEntry(int chunkX, int chunkZ);

    private boolean isUpdatingInstances = false;

    @Override
    public boolean isChunkInUse(int x, int z) {
        PlayerChunkMapEntry playerInstance = this.getEntry(x, z);
        return playerInstance != null && playerInstance.players.size() > 0;
    }

    @Redirect(method = "removeEntry", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/ChunkProviderServer;"
            + "unload(Lnet/minecraft/world/chunk/Chunk;)V"))
    private void onUnloadChunk(ChunkProviderServer chunkProvider, Chunk chunk) {
        // We remove the ability for a PlayerChunkMap to queue chunks for unload to prevent chunk thrashing
        // where the same chunks repeatedly unload and load. This is caused by a player moving in and out of the same chunks.
        // Instead, the Chunk GC will now be responsible for going through loaded chunks and queuing any chunk where no player
        // is within view distance or a spawn chunk is force loaded. However, if the Chunk GC is disabled then we will fall back to vanilla
        // and queue the chunk to be unloaded.
        // -- blood

        if (((IMixinWorldServer) this.theWorldServer).getChunkGCTickInterval() <= 0
                || ((IMixinWorldServer) this.theWorldServer).getChunkUnloadDelay() <= 0) {
            chunkProvider.unload(chunk);
        } else {
            ((IMixinChunk) chunk).setScheduledForUnload(System.currentTimeMillis());
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/Set;iterator()Ljava/util/Iterator;", ordinal = 0))
    public void onStartUpdateLoop(CallbackInfo ci) {
        this.isUpdatingInstances = true;
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/Set;clear()V"))
    public void onEndUpdateLoop(CallbackInfo ci) {
        this.isUpdatingInstances = false;
    }

    @Inject(method = "addEntry", at = @At("HEAD"))
    public void onAddEntry(PlayerChunkMapEntry entry, CallbackInfo ci) {
        this.checkConcurrentModification(entry);
    }

    @Inject(method = "removeEntry", at = @At("HEAD"))
    public void onRemoveEntry(PlayerChunkMapEntry entry, CallbackInfo ci) {
        this.checkConcurrentModification(entry);
    }

    private void checkConcurrentModification(PlayerChunkMapEntry entry) {
        if (this.isUpdatingInstances) {
            SpongeImpl.getLogger().error("Attempting to concurrently modify PlayerChunkMapEntry for chunk {}", entry.getPos());
            Thread.dumpStack();
        }
    }

}
