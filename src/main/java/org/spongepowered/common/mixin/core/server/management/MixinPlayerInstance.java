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

import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.server.management.PlayerManager.PlayerInstance;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.server.management.IMixinPlayerInstance;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

@Mixin(PlayerInstance.class)
public abstract class MixinPlayerInstance implements IMixinPlayerInstance {

    @Shadow(aliases = {"field_73265_a", "this$0"}) private PlayerManager thePlayerManager;
    @Shadow private ChunkCoordIntPair chunkCoords;
    @Shadow private int numBlocksToUpdate;
    @Shadow private int flagsYAreasToUpdate;
    @Shadow public abstract void sendToAllPlayersWatchingChunk(Packet<?> thePacket);

    private boolean updateBiomes;

    @Inject(method = "onUpdate", at = @At("HEAD"), cancellable = true)
    public void resendUpdatedBiomes(CallbackInfo ci) {
        final Chunk chunk = this.thePlayerManager.getWorldServer().getChunkFromChunkCoords(this.chunkCoords.chunkXPos,
                this.chunkCoords.chunkZPos);
        if (this.updateBiomes) {
            this.sendToAllPlayersWatchingChunk(new S21PacketChunkData(chunk, true, 65535));
            this.numBlocksToUpdate = 0;
            this.flagsYAreasToUpdate = 0;
            this.updateBiomes = false;
            ci.cancel();
        }
    }

    @Override
    public void markBiomesForUpdate() {
        this.updateBiomes = true;
        this.thePlayerManager.playerInstancesToUpdate.add((PlayerInstance) (Object) this);
    }
}
