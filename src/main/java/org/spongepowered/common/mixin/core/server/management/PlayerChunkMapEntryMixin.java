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

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SChunkDataPacket;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.server.management.PlayerChunkMapEntryBridge;

import java.util.List;

import javax.annotation.Nullable;

@Mixin(PlayerChunkMapEntry.class)
public abstract class PlayerChunkMapEntryMixin implements PlayerChunkMapEntryBridge {

    @Shadow @Final private PlayerChunkMap playerChunkMap;
    @Shadow @Final private ChunkPos pos;
    @Shadow private int changes;
    @Shadow private int changedSectionFilter;
    @Shadow public abstract void sendPacket(IPacket<?> packetIn);

    @Shadow @Final private List<ServerPlayerEntity> players;
    @Shadow @Nullable private Chunk chunk;
    private boolean impl$updateBiomes;

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void impl$updateBiomes(final CallbackInfo ci) {
        if (this.impl$updateBiomes) {
            final Chunk chunk = this.playerChunkMap.func_72688_a().func_72964_e(this.pos.field_77276_a, this.pos.field_77275_b);
            this.sendPacket(new SChunkDataPacket(chunk, 65534));
            this.sendPacket(new SChunkDataPacket(chunk, 1));
            this.changes = 0;
            this.changedSectionFilter = 0;
            this.impl$updateBiomes = false;
            ci.cancel();
        }
    }

    @Override
    public void bridge$markBiomesForUpdate() {
        this.impl$updateBiomes = true;
        this.playerChunkMap.func_187304_a((PlayerChunkMapEntry) (Object) this);
    }

    @Override
    public List<ServerPlayerEntity> accessor$getPlayers() {
        return this.players;
    }

    @Override
    public void bridge$setChunk(Chunk newChunk) {
        this.chunk = newChunk;
    }
}
