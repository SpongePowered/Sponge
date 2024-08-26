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
package org.spongepowered.common.mixin.tracker.world.level.chunk.storage;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.world.level.chunk.LevelChunkBridge;
import org.spongepowered.common.bridge.world.level.chunk.storage.SerializableChunkDataBridge;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.util.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@Mixin(SerializableChunkData.class)
public abstract class SerializableChunkDataMixin_Tracker implements SerializableChunkDataBridge {


    @Shadow @Final private boolean lightCorrect;
    private Map<Short, PlayerTracker> tracker$shortPlayerPos = null;
    private Map<Integer, PlayerTracker> tracker$intPlayerPos = null;

    @Inject(method = "copyOf", at = @At(value = "RETURN"))
    private static void impl$copySpongeLevelData(final ServerLevel $$0, final ChunkAccess $$1, final CallbackInfoReturnable<SerializableChunkData> cir) {
        final var chunkData = cir.getReturnValue();
        ((SerializableChunkDataBridge) (Object) chunkData).bridge$copySpongeData($$1);
    }

    @Override
    public void bridge$copySpongeData(final ChunkAccess chunkAccess) {
        if (!(chunkAccess instanceof LevelChunk)) {
            return;
        }
        final LevelChunkBridge chunk = (LevelChunkBridge) chunkAccess;
        if (!chunk.bridge$getTrackedShortPlayerPositions().isEmpty() || !chunk.bridge$getTrackedIntPlayerPositions().isEmpty()) {
            this.tracker$shortPlayerPos = chunk.bridge$getTrackedShortPlayerPositions();
            this.tracker$intPlayerPos = chunk.bridge$getTrackedIntPlayerPositions();
        }
    }

    @Inject(method = "write", at = @At(value = "RETURN"))
    private void tracker$writeSpongeLevelData(final CallbackInfoReturnable<CompoundTag> cir) {
        if (this.tracker$intPlayerPos == null && this.tracker$shortPlayerPos == null) {
            return;
        }
        final CompoundTag level = cir.getReturnValue();
        final CompoundTag trackedNbt = new CompoundTag();
        final ListTag positions = new ListTag();
        trackedNbt.put(Constants.Sponge.SPONGE_BLOCK_POS_TABLE, positions);
        level.put(Constants.Sponge.Data.V2.SPONGE_DATA, trackedNbt);

        SerializableChunkDataMixin_Tracker.tracker$writeMap(positions, this.tracker$shortPlayerPos, (nbt, pos) -> nbt.putShort("pos", pos));
        SerializableChunkDataMixin_Tracker.tracker$writeMap(positions, this.tracker$intPlayerPos, (nbt, pos) -> nbt.putInt("ipos", pos));
    }

    private static <T> void tracker$writeMap(final ListTag positions, final Map<T, PlayerTracker> map, final BiConsumer<CompoundTag, T> consumer) {
        for (final Map.Entry<T, PlayerTracker> mapEntry : map.entrySet()) {
            final T pos = mapEntry.getKey();
            final int ownerUniqueIdIndex = mapEntry.getValue().creatorindex;
            final int notifierUniqueIdIndex = mapEntry.getValue().notifierIndex;
            final CompoundTag valueNbt = new CompoundTag();
            valueNbt.putInt("owner", ownerUniqueIdIndex);
            valueNbt.putInt("notifier", notifierUniqueIdIndex);
            consumer.accept(valueNbt, pos);
            positions.add(valueNbt);
        }
    }


    @Inject(method = "parse", at = @At(value = "RETURN"))
    private static void impl$parseSpongeLevelData(LevelHeightAccessor $$0, RegistryAccess $$1, CompoundTag $$2, final CallbackInfoReturnable<SerializableChunkData> cir) {
        final var chunkData = cir.getReturnValue();
        ((SerializableChunkDataBridge) (Object) chunkData).bridge$parseSpongeData();
    }

    @Override
    public void bridge$parseSpongeData(final CompoundTag fullTag) {
        final CompoundTag spongeData = fullTag.getCompound(Constants.Sponge.Data.V2.SPONGE_DATA);
        if (spongeData.isEmpty()) {
            return;
        }
        this.tracker$intPlayerPos = new HashMap<>();
        this.tracker$shortPlayerPos = new HashMap<>();

        final ListTag list = spongeData.getList(Constants.Sponge.SPONGE_BLOCK_POS_TABLE, 10);
        for (final Tag tag : list) {
            final PlayerTracker tracker = new PlayerTracker();
            final CompoundTag data = (CompoundTag) tag;
            final boolean isShortPos = data.contains("pos");
            if (data.contains("owner")) {
                tracker.creatorindex = data.getInt("owner");
            }
            if (data.contains("notifier")) {
                tracker.notifierIndex = data.getInt("notifier");
            }
            if (tracker.notifierIndex != -1 || tracker.creatorindex != -1) {
                if (isShortPos) {
                    this.tracker$shortPlayerPos.put(data.getShort("pos"), tracker);
                } else {
                    this.tracker$intPlayerPos.put(data.getInt("ipos"), tracker);
                }
            }
        }
    }

    @Redirect(method = "read",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkAccess;setLightCorrect(Z)V"))
    private void impl$readSpongeLevelData(final ChunkAccess chunkAccess, final boolean lightCorrect) {
        chunkAccess.setLightCorrect(lightCorrect);
        if (!(chunkAccess instanceof LevelChunk)) {
            return;
        }

        final LevelChunkBridge chunk = (LevelChunkBridge) chunkAccess;
        chunk.bridge$setTrackedIntPlayerPositions(this.tracker$intPlayerPos);
        chunk.bridge$setTrackedShortPlayerPositions(this.tracker$shortPlayerPos);
    }
}
