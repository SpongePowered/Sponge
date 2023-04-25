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
package org.spongepowered.common.mixin.core.world.level.chunk.storage;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.IOWorker;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.world.chunk.ChunkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.level.chunk.storage.IOWorkerBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.math.vector.Vector3i;

@Mixin(IOWorker.class)
public abstract class IOWorkerMixin implements IOWorkerBridge {

    @MonotonicNonNull private ResourceKey<Level> impl$dimension; //We only set this for chunk related IO workers

    @Override
    public void bridge$setDimension(ResourceKey<Level> dimension) {
        this.impl$dimension = dimension;
    }

    @Inject(method = "runStore", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Ljava/util/concurrent/CompletableFuture;complete(Ljava/lang/Object;)Z"))
    private void impl$onSaved(final ChunkPos param0, final @Coerce Object param1, final CallbackInfo ci) {
        if (this.impl$dimension == null) {
            return;
        }

        if (ShouldFire.CHUNK_EVENT_SAVE_POST) {
            final Vector3i chunkPos = new Vector3i(param0.x, 0, param0.z);
            final ChunkEvent.Save.Post postSave = SpongeEventFactory.createChunkEventSavePost(PhaseTracker.getInstance().currentCause(), chunkPos,
                    (org.spongepowered.api.ResourceKey) (Object) this.impl$dimension.location());
            SpongeCommon.post(postSave);
        }
    }
}
