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
package org.spongepowered.common.mixin.core.server.level;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.server.storage.ServerWorldProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.accessor.server.level.ChunkMapAccessor;

@Mixin(ServerChunkCache.class)
public abstract class ServerChunkCacheMixin {

    // @formatter:off
    @Shadow @Final private ServerLevel level;
    // @formatter:on
    
    @Redirect(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkMap;saveAllChunks(Z)V"))
    private void impl$useSerializationBehaviorWhenSaving(final ChunkMap chunkManager, final boolean flush) {
        final ServerLevel world = this.level;
        final SerializationBehavior serializationBehavior = ((ServerWorldProperties) world.getLevelData()).serializationBehavior();
        if (serializationBehavior == SerializationBehavior.AUTOMATIC || serializationBehavior == SerializationBehavior.MANUAL) {
            ((ChunkMapAccessor) chunkManager).invoker$saveAllChunks(flush);
        }
    }
}
