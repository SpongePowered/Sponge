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

import net.minecraft.server.management.PlayerManager.PlayerInstance;
import net.minecraft.world.gen.ChunkProviderServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerInstance.class)
public class MixinPlayerInstance {

    @Redirect(method = "removePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/ChunkProviderServer;dropChunk(II)V"))
    public void onPlayerDropChunk(ChunkProviderServer chunkProviderServer, int chunkX, int chunkZ) {
        // We remove the ability for a PlayerInstance to queue chunks for unload to prevent chunk thrashing
        // where the same chunks repeatedly unload and load. This is caused by a player moving in and out of the same chunks.
        // Instead, the Chunk GC will now be responsible for going through loaded chunks and queuing any chunk where no player
        // is within view distance or a spawn chunk is force loaded.
        // -- blood

        // chunkProviderServer.dropChunk(chunkX, chunkZ);
    }
}
