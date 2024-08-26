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
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.world.level.chunk.LevelChunkBridge;
import org.spongepowered.common.bridge.world.level.chunk.storage.SerializableChunkDataBridge;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.util.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@Mixin(SerializableChunkData.class)
public abstract class SerializableChunkDataMixin_Tracker implements SerializableChunkDataBridge {

    private Map<Short, PlayerTracker> tracker$shortPlayerPos = null;
    private Map<Integer, PlayerTracker> tracker$intPlayerPos = null;

    @Override
    public void bridge$setTrackerData(final LevelChunk chunkAccess) {
        final LevelChunkBridge chunk = (LevelChunkBridge) chunkAccess;
        if (!chunk.bridge$getTrackedShortPlayerPositions().isEmpty() || !chunk.bridge$getTrackedIntPlayerPositions().isEmpty()) {
            this.tracker$shortPlayerPos = chunk.bridge$getTrackedShortPlayerPositions();
            this.tracker$intPlayerPos = chunk.bridge$getTrackedIntPlayerPositions();
        }
    }

    @Override
    public void bridge$writeTrackerData(final CompoundTag level) {
        if (this.tracker$intPlayerPos == null && this.tracker$shortPlayerPos == null) {
            return;
        }
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

    @Override
    public void bridge$parseTrackerData(final CompoundTag fullTag) {
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

    @Override
    public void bridge$readTrackerDataFrom(final LevelChunk levelChunk) {
        final LevelChunkBridge chunk = (LevelChunkBridge) levelChunk;
        chunk.bridge$setTrackedIntPlayerPositions(this.tracker$intPlayerPos);
        chunk.bridge$setTrackedShortPlayerPositions(this.tracker$shortPlayerPos);
    }

}
