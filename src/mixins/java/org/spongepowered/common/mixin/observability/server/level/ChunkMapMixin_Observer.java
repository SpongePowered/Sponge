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
package org.spongepowered.common.mixin.observability.server.level;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.hooks.PlatformHooks;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkMap.class)
public class ChunkMapMixin_Observer {

    @Shadow @Final private ServerLevel level;

    @Inject(
        method = "lambda$scheduleUnload$10",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;unload(Lnet/minecraft/world/level/chunk/LevelChunk;)V"
        )
    )
    private void observable$markChunkUnloaded(
        final ChunkHolder param0x, final CompletableFuture<ChunkAccess> saveChunkFuture,
        final long chunkCoordKey, final ChunkAccess param3,
        final CallbackInfo ci
    ) {
        PlatformHooks.INSTANCE.getTrackerHooks().updateChunkGauge(this.level);
    }

    @Inject(method = "lambda$null$25",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V",
            shift = At.Shift.AFTER
        )
    )
    private void observable$markChunkLoaded(
        final ChunkHolder holder,
        final ChunkAccess access,
        final CallbackInfoReturnable<ChunkAccess> cir
    ) {
        PlatformHooks.INSTANCE.getTrackerHooks().updateChunkGauge(this.level);
    }
}
