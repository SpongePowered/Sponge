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
package org.spongepowered.common.mixin.optimization.world.gen;

import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge_AsyncLighting;

@Mixin(value = ServerChunkProvider.class, priority = 1002)
public abstract class ChunkProviderServerMixin_Async_Lighting {

    @Shadow @Final private ServerWorld world;

    @Dynamic
    @Redirect(method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lorg/spongepowered/common/bridge/world/chunk/ChunkBridge;bridge$isPersistedChunk()Z",
            remap = false))
    private boolean asyncLighting$UsePendingLightUpdatesForAsyncChunk(final ChunkBridge chunk) {
        return chunk.bridge$isPersistedChunk()
               || ((ChunkBridge_AsyncLighting) chunk).asyncLightingBridge$getPendingLightUpdates().get() > 0
               || this.world.func_82737_E() - ((ChunkBridge_AsyncLighting) chunk).asyncLightingBridge$getLightUpdateTime() < 20;

    }
}
