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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.world.level.chunk.LevelChunkBridge;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.util.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@Mixin(ChunkSerializer.class)
public abstract class ChunkSerializerMixin_Tracker {

    @Inject(method = "write", at = @At(value = "RETURN"))
    private static void impl$writeSpongeLevelData(final ServerLevel param0, final ChunkAccess param1, final CallbackInfoReturnable<CompoundTag> cir) {
        if (!(param1 instanceof LevelChunk)) {
            return;
        }
        final LevelChunkBridge chunk = (LevelChunkBridge) param1;
        if (!chunk.bridge$getTrackedIntPlayerPositions().isEmpty()) {
            final CompoundTag level = (CompoundTag) cir.getReturnValue().get("Level");
            final CompoundTag trackedNbt = new CompoundTag();
            final ListTag positions = new ListTag();
            trackedNbt.put(Constants.Sponge.SPONGE_BLOCK_POS_TABLE, positions);
            level.put(Constants.Sponge.Data.V2.SPONGE_DATA, trackedNbt);

            ChunkSerializerMixin_Tracker.impl$writeMap(positions, chunk.bridge$getTrackedShortPlayerPositions(), (nbt, pos) -> nbt.putShort("pos", pos));
            ChunkSerializerMixin_Tracker.impl$writeMap(positions, chunk.bridge$getTrackedIntPlayerPositions(), (nbt, pos) -> nbt.putInt("ipos", pos));
        }
    }

    private static <T> void impl$writeMap(ListTag positions, Map<T, PlayerTracker> map, BiConsumer<CompoundTag, T> consumer) {
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

    @Redirect(method = "read",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkAccess;setLightCorrect(Z)V"))
    private static void impl$readSpongeLevelData(final ChunkAccess chunkAccess, final boolean var1, final ServerLevel param0, final StructureManager param1, final PoiManager param2, final ChunkPos param3, final CompoundTag param4) {
        if (!(chunkAccess instanceof LevelChunk)) {
            return;
        }
        final CompoundTag level = (CompoundTag) param4.get("Level");
        final CompoundTag spongeData = level.getCompound(Constants.Sponge.Data.V2.SPONGE_DATA);
        if (spongeData.isEmpty()) {
            return;
        }
        final Map<Integer, PlayerTracker> trackedIntPlayerPositions = new HashMap<>();
        final Map<Short, PlayerTracker> trackedShortPlayerPositions = new HashMap<>();

        final ListTag list = spongeData.getList(Constants.Sponge.SPONGE_BLOCK_POS_TABLE, 10);
        final LevelChunkBridge chunk = (LevelChunkBridge) chunkAccess;
        for (Tag tag : list) {
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
                    trackedShortPlayerPositions.put(data.getShort("pos"), tracker);
                } else {
                    trackedIntPlayerPositions.put(data.getInt("ipos"), tracker);
                }
            }
        }
        chunk.bridge$setTrackedIntPlayerPositions(trackedIntPlayerPositions);
        chunk.bridge$setTrackedShortPlayerPositions(trackedShortPlayerPositions);
    }
}
