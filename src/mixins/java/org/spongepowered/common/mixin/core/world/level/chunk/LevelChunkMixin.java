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
package org.spongepowered.common.mixin.core.world.level.chunk;

import com.google.common.base.MoreObjects;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkBiomeContainer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.util.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.level.chunk.CacheKeyBridge;
import org.spongepowered.common.bridge.world.level.chunk.LevelChunkBridge;
import org.spongepowered.common.entity.PlayerTracker;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@Mixin(net.minecraft.world.level.chunk.LevelChunk.class)
public abstract class LevelChunkMixin implements LevelChunkBridge, CacheKeyBridge {

    // @formatter:off
    @Shadow @Final private Level level;
    @Shadow @Final private ChunkPos chunkPos;
    @Shadow @Final private ClassInstanceMultiMap<Entity>[] entitySections;
    @Shadow @Final private Map<BlockPos, BlockEntity> blockEntities;
    @Shadow private boolean loaded;
    @Shadow private boolean unsaved;

    @Shadow @Nullable public abstract BlockEntity getBlockEntity(BlockPos pos, net.minecraft.world.level.chunk.LevelChunk.EntityCreationType p_177424_2_);

    @Shadow public abstract BlockState getBlockState(BlockPos pos);
    // @formatter:on
    private long impl$scheduledForUnload = -1; // delay chunk unloads
    private boolean impl$persistedChunk = false;
    private boolean impl$isSpawning = false;
    private final net.minecraft.world.level.chunk.LevelChunk[] impl$neighbors = new net.minecraft.world.level.chunk.LevelChunk[4];
    private long impl$cacheKey;

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/ChunkBiomeContainer;Lnet/minecraft/world/level/chunk/UpgradeData;Lnet/minecraft/world/level/TickList;Lnet/minecraft/world/level/TickList;J[Lnet/minecraft/world/level/chunk/LevelChunkSection;Ljava/util/function/Consumer;)V",
            at = @At("RETURN"))
    private void impl$onConstruct(Level p_i225781_1_, ChunkPos p_i225781_2_, ChunkBiomeContainer p_i225781_3_, UpgradeData p_i225781_4_,
            TickList<Block> p_i225781_5_, TickList<Fluid> p_i225781_6_, long p_i225781_7_, LevelChunkSection[] p_i225781_9_,
            Consumer<LevelChunk> p_i225781_10_, CallbackInfo ci) {
        this.impl$cacheKey = ChunkPos.asLong(p_i225781_2_.x, p_i225781_2_.z);
    }

    @Override
    public net.minecraft.world.level.chunk.LevelChunk[] bridge$getNeighborArray() {
        return Arrays.copyOf(this.impl$neighbors, this.impl$neighbors.length);
    }

    @Override
    public void bridge$markChunkDirty() {
        this.unsaved = true;
    }

    @Override
    public boolean bridge$isQueuedForUnload() {
        return false; // TODO actually implement this
    }

    @Override
    public boolean bridge$isPersistedChunk() {
        return this.impl$persistedChunk;
    }

    @Override
    public boolean bridge$isSpawning() {
        return this.impl$isSpawning;
    }

    @Override
    public void bridge$setIsSpawning(final boolean spawning) {
        this.impl$isSpawning = spawning;
    }


    // These methods are enabled in ChunkMixin_CreatorTracked as a Mixin plugin

    @Override
    public void bridge$addTrackedBlockPosition(final Block block, final BlockPos pos, final User user, final PlayerTracker.Type trackerType) {
    }

    @Override
    public Map<Integer, PlayerTracker> bridge$getTrackedIntPlayerPositions() {
        return Collections.emptyMap();
    }

    @Override
    public Map<Short, PlayerTracker> bridge$getTrackedShortPlayerPositions() {
        return Collections.emptyMap();
    }

    @Override
    public Optional<User> bridge$getBlockCreator(final BlockPos pos) {
        return Optional.empty();
    }

    @Override
    public Optional<UUID> bridge$getBlockCreatorUUID(final BlockPos pos) {
        return Optional.empty();
    }

    @Override
    public Optional<User> bridge$getBlockNotifier(final BlockPos pos) {
        return Optional.empty();
    }

    @Override
    public Optional<UUID> bridge$getBlockNotifierUUID(final BlockPos pos) {
        return Optional.empty();
    }

    @Override
    public void bridge$setBlockNotifier(final BlockPos pos, @Nullable final UUID uuid) {
    }

    @Override
    public void bridge$setBlockCreator(final BlockPos pos, @Nullable final UUID uuid) {
    }

    @Override
    public void bridge$setTrackedIntPlayerPositions(final Map<Integer, PlayerTracker> trackedPositions) {
    }

    @Override
    public void bridge$setTrackedShortPlayerPositions(final Map<Short, PlayerTracker> trackedPositions) {
    }


    // Fast neighbor methods for internal use
    @Override
    public void bridge$setNeighborChunk(final int index, @Nullable final net.minecraft.world.level.chunk.LevelChunk chunk) {
        this.impl$neighbors[index] = chunk;
    }

    @Nullable
    @Override
    public net.minecraft.world.level.chunk.LevelChunk bridge$getNeighborChunk(final int index) {
        return this.impl$neighbors[index];
    }

    @Override
    public List<net.minecraft.world.level.chunk.LevelChunk> bridge$getNeighbors() {
        final List<net.minecraft.world.level.chunk.LevelChunk> neighborList = new ArrayList<>();
        for (final net.minecraft.world.level.chunk.LevelChunk neighbor : this.impl$neighbors) {
            if (neighbor != null) {
                neighborList.add(neighbor);
            }
        }
        return neighborList;
    }

    @Override
    public boolean bridge$areNeighborsLoaded() {
        // TODO neighbors are never set
//        for (int i = 0; i < 4; i++) {
//            if (this.impl$neighbors[i] == null) {
//                return false;
//            }
//        }

        return true;
    }

    @Override
    public void bridge$setNeighbor(final Direction direction, @Nullable final net.minecraft.world.level.chunk.LevelChunk neighbor) {
        this.impl$neighbors[SpongeCommon.directionToIndex(direction)] = neighbor;
    }

    @Override
    public long bridge$getScheduledForUnload() {
        return this.impl$scheduledForUnload;
    }

    @Override
    public void bridge$setScheduledForUnload(final long scheduled) {
        this.impl$scheduledForUnload = scheduled;
    }

    @Override
    public boolean bridge$isActive() {
        if (this.bridge$isPersistedChunk()) {
            return true;
        }
        return this.loaded && !this.bridge$isQueuedForUnload() && this.bridge$getScheduledForUnload() == -1;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("World", this.level)
                .add("Position", this.chunkPos.x + this.chunkPos.z)
                .toString();
    }

    @Override
    public long bridge$getCacheKey() {
        return this.impl$cacheKey;
    }

}
