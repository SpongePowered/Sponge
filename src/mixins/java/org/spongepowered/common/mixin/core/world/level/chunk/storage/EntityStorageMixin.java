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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;
import net.minecraft.world.level.entity.ChunkEntities;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.world.chunk.ChunkEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.world.level.chunk.storage.SimpleRegionStorageAccessor;
import org.spongepowered.common.bridge.world.level.chunk.storage.IOWorkerBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.level.chunk.storage.SpongeEntityChunk;
import org.spongepowered.common.world.level.chunk.storage.SpongeIOWorkerType;
import org.spongepowered.math.vector.Vector3i;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

@Mixin(EntityStorage.class)
public abstract class EntityStorageMixin {

    // @formatter:off
    @Shadow @Final private ServerLevel level;
    // @formatter:on

    @Inject(method = "<init>", at = @At("RETURN"))
    private void impl$setIOWorkerDimension(final SimpleRegionStorage $$0, final ServerLevel $$1, final Executor $$2, final CallbackInfo ci) {
        ((IOWorkerBridge) ((SimpleRegionStorageAccessor) $$0).accessor$worker()).bridge$setDimension(SpongeIOWorkerType.ENTITY, this.level.dimension());
    }

    @Inject(method = "lambda$loadEntities$0", at = @At("RETURN"), cancellable = true)
    private void impl$onLoadEntities(final ChunkPos $$0x, final Optional<CompoundTag> $$1, final CallbackInfoReturnable<ChunkEntities<Entity>> cir) {
        if (!ShouldFire.CHUNK_EVENT_ENTITIES_LOAD) {
            return;
        }

        final Vector3i chunkPos = VecHelper.toVector3i($$0x);
        final SpongeEntityChunk entities = new SpongeEntityChunk(this.level, chunkPos, cir.getReturnValue().getEntities());
        final ChunkEvent.Entities.Load loadEvent = SpongeEventFactory.createChunkEventEntitiesLoad(PhaseTracker.getInstance().currentCause(),
                entities, chunkPos, (ResourceKey) (Object) this.level.dimension().location());

        SpongeCommon.post(loadEvent);

        final @Nullable List<Entity> newList = entities.buildIfChanged();
        if (newList != null) {
            cir.setReturnValue(new ChunkEntities<>($$0x, newList));
        }
    }

    @ModifyVariable(method = "storeEntities", at = @At("HEAD"), argsOnly = true)
    private ChunkEntities<Entity> impl$onStoreEntities(final ChunkEntities<Entity> $$0) {
        if (!ShouldFire.CHUNK_EVENT_ENTITIES_SAVE_PRE) {
            return $$0;
        }

        final Vector3i chunkPos = VecHelper.toVector3i($$0.getPos());

        final SpongeEntityChunk entities = new SpongeEntityChunk(this.level, chunkPos, $$0.getEntities());
        final ChunkEvent.Entities.Save.Pre saveEvent = SpongeEventFactory.createChunkEventEntitiesSavePre(PhaseTracker.getInstance().currentCause(),
                entities, chunkPos, (ResourceKey) (Object) this.level.dimension().location());

        if (SpongeCommon.post(saveEvent)) {
            return null;
        }

        final @Nullable List<Entity> newList = entities.buildIfChanged();
        if (newList != null) {
            return new ChunkEntities<>($$0.getPos(), newList);
        }

        return $$0;
    }

    @Inject(method = "storeEntities",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/entity/ChunkEntities;getPos()Lnet/minecraft/world/level/ChunkPos;"), cancellable = true)
    private void impl$onCancelledEntitySave(final ChunkEntities<Entity> $$0, final CallbackInfo ci) {
        if ($$0 == null) {
            ci.cancel();
        }
    }
}
