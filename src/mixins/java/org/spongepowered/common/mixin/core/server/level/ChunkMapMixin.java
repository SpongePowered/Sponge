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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.world.chunk.ChunkEvent;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.level.storage.PrimaryLevelDataBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.math.vector.Vector3i;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {

    // @formatter:off
    @Shadow @Final private ServerLevel level;
    // @formatter:on

    @Redirect(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/village/poi/PoiManager;flush(Lnet/minecraft/world/level/ChunkPos;)V"))
    private void impl$useSerializationBehaviorForPOI(PoiManager pointOfInterestManager, ChunkPos p_219112_1_) {
        final PrimaryLevelDataBridge infoBridge = (PrimaryLevelDataBridge) this.level.getLevelData();
        final SerializationBehavior serializationBehavior = infoBridge.bridge$serializationBehavior().orElse(SerializationBehavior.AUTOMATIC);
        if (serializationBehavior == SerializationBehavior.AUTOMATIC || serializationBehavior == SerializationBehavior.MANUAL) {
            pointOfInterestManager.flush(p_219112_1_);
        }
    }

    @Redirect(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/storage/ChunkSerializer;write(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;)Lnet/minecraft/nbt/CompoundTag;"))
    private CompoundTag impl$useSerializationBehaviorForChunkSave(ServerLevel worldIn, ChunkAccess chunkIn) {
        final PrimaryLevelDataBridge infoBridge = (PrimaryLevelDataBridge) this.level.getLevelData();
        final SerializationBehavior serializationBehavior = infoBridge.bridge$serializationBehavior().orElse(SerializationBehavior.AUTOMATIC);
        if (serializationBehavior == SerializationBehavior.AUTOMATIC || serializationBehavior == SerializationBehavior.MANUAL) {
            return ChunkSerializer.write(worldIn, chunkIn);
        }

        return null;
    }

    @Redirect(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkMap;write(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/nbt/CompoundTag;)V"))
    private void impl$doNotWriteIfWeHaveNoData(ChunkMap chunkManager, ChunkPos pos, CompoundTag compound) {
        if (compound == null) {
            return;
        }

        chunkManager.write(pos, compound);
    }

    @Redirect(method = "*",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;unload(Lnet/minecraft/world/level/chunk/LevelChunk;)V"),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkMap;save(Lnet/minecraft/world/level/chunk/ChunkAccess;)Z")
            )
    )
    private void impl$onSetUnloaded(final ServerLevel level, final LevelChunk chunk) {
        level.unload(chunk);
        final Vector3i chunkPos = new Vector3i(chunk.getPos().x, 0, chunk.getPos().z);
        final ChunkEvent.Unload event = SpongeEventFactory.createChunkEventUnload(PhaseTracker.getInstance().currentCause(), chunkPos, (ResourceKey) (Object) this.level.dimension().location());
        SpongeCommon.postEvent(event);
    }

    @Redirect(method = "*",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkHolder;replaceProtoChunk(Lnet/minecraft/world/level/chunk/ImposterProtoChunk;)V")
    )
    private void impl$onReplaceProto(final ChunkHolder holder, final ImposterProtoChunk chunk) {
        holder.replaceProtoChunk(chunk);

        final Vector3i chunkPos = new Vector3i(chunk.getPos().x, 0, chunk.getPos().z);
        final ChunkEvent.Generated event = SpongeEventFactory.createChunkEventGenerated(PhaseTracker.getInstance().currentCause(), chunkPos, (ResourceKey) (Object) this.level.dimension().location());
        SpongeCommon.postEvent(event);
    }

    @Inject(method = "save", at = @At(value = "RETURN"))
    private void impl$onSaved(final ChunkAccess var1, final CallbackInfoReturnable<Boolean> cir) {
        final Vector3i chunkPos = new Vector3i(var1.getPos().x, 0, var1.getPos().z);
        final ChunkEvent.Save.Post postSave = SpongeEventFactory.createChunkEventSavePost(PhaseTracker.getInstance().currentCause(), chunkPos,
                        (ResourceKey) (Object) this.level.dimension().location());
        SpongeCommon.postEvent(postSave);
    }

    @Inject(method = "save", at = @At(value = "HEAD"), cancellable = true)
    private void impl$onSave(final ChunkAccess var1, final CallbackInfoReturnable<Boolean> cir) {
        if (var1 instanceof Chunk) {
            final Vector3i chunkPos = new Vector3i(var1.getPos().x, 0, var1.getPos().z);
            final ChunkEvent.Save.Pre postSave = SpongeEventFactory.createChunkEventSavePre(PhaseTracker.getInstance().currentCause(),
                    chunkPos, (ResourceKey) (Object) this.level.dimension().location(), ((Chunk) var1));
            SpongeCommon.postEvent(postSave);
            if (postSave.isCancelled()) {
                cir.setReturnValue(false);
            }
        }
    }

    @Redirect(method = "*",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;setLoaded(Z)V"),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/longs/LongSet;add(J)Z"),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addAllPendingBlockEntities(Ljava/util/Collection;)V")
            )
    )
    private void impl$onLoad(final LevelChunk levelChunk, final boolean loaded) {
        levelChunk.setLoaded(true);
        final Vector3i chunkPos = new Vector3i(levelChunk.getPos().x, 0, levelChunk.getPos().z);
        final ChunkEvent.Load loadEvent = SpongeEventFactory.createChunkEventLoad(PhaseTracker.getInstance().currentCause(),
                chunkPos, (ResourceKey) (Object) this.level.dimension().location(), ((Chunk) levelChunk));
        SpongeCommon.postEvent(loadEvent);
    }

}
