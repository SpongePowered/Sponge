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
import net.minecraft.network.play.server.SPacketChunkData;
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
import org.spongepowered.common.util.Constants;

@Mixin(PlayerChunkMapEntry.class)
public abstract class MixinPlayerChunkMapEntry implements PlayerChunkMapEntryBridge {

    @Shadow @Final public PlayerChunkMap playerChunkMap;
    @Shadow @Final public ChunkPos pos;
    @Shadow public int changes;
    @Shadow public int changedSectionFilter;
    @Shadow public abstract void sendPacket(Packet<?> packetIn);

    private boolean updateBiomes;

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void impl$UpdateBimoes(CallbackInfo ci) {
        final Chunk chunk = this.playerChunkMap.getWorldServer().getChunk(this.pos.x, this.pos.z);
        if (this.updateBiomes) {
            this.sendPacket(new SPacketChunkData(chunk, Constants.Networking.Packets.CHANGED_SECTION_FILTER_ALL));
            this.changes = 0;
            this.changedSectionFilter = 0;
            this.updateBiomes = false;
            ci.cancel();
        }
    }

    @Override
    public void bridge$markBiomesForUpdate() {
        this.updateBiomes = true;
        this.playerChunkMap.dirtyEntries.add((PlayerChunkMapEntry) (Object) this);
    }
}
