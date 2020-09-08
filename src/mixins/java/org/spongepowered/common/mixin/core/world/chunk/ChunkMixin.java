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
package org.spongepowered.common.mixin.core.world.chunk;

import com.google.common.base.MoreObjects;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.palette.UpgradeData;
import net.minecraft.world.ITickList;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.util.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.chunk.CacheKeyBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
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

@Mixin(net.minecraft.world.chunk.Chunk.class)
public abstract class ChunkMixin implements ChunkBridge, CacheKeyBridge {

    // @formatter:off
    @Shadow @Final private World world;
    @Shadow @Final private ChunkPos pos;
    @Shadow @Final private ClassInheritanceMultiMap<Entity>[] entityLists;
    @Shadow @Final private Map<BlockPos, TileEntity> tileEntities;
    @Shadow private boolean loaded;
    @Shadow private boolean dirty;

    @Shadow @Nullable public abstract TileEntity getTileEntity(BlockPos pos, net.minecraft.world.chunk.Chunk.CreateEntityType p_177424_2_);

    @Shadow public abstract BlockState getBlockState(BlockPos pos);
    // @formatter:on

    private long impl$scheduledForUnload = -1; // delay chunk unloads
    private boolean impl$persistedChunk = false;
    private boolean impl$isSpawning = false;
    private final net.minecraft.world.chunk.Chunk[] impl$neighbors = new net.minecraft.world.chunk.Chunk[4];
    private long impl$cacheKey;

    @Inject(method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/world/biome/BiomeContainer;Lnet/minecraft/util/palette/UpgradeData;Lnet/minecraft/world/ITickList;Lnet/minecraft/world/ITickList;J[Lnet/minecraft/world/chunk/ChunkSection;Ljava/util/function/Consumer;)V",
            at = @At("RETURN"))
    private void impl$onConstruct(World p_i225781_1_, ChunkPos p_i225781_2_, BiomeContainer p_i225781_3_, UpgradeData p_i225781_4_,
            ITickList<Block> p_i225781_5_, ITickList<Fluid> p_i225781_6_, long p_i225781_7_, ChunkSection[] p_i225781_9_,
            Consumer<Chunk> p_i225781_10_, CallbackInfo ci) {
        this.impl$cacheKey = ChunkPos.asLong(p_i225781_2_.x, p_i225781_2_.z);
    }

    @Override
    public net.minecraft.world.chunk.Chunk[] bridge$getNeighborArray() {
        return Arrays.copyOf(this.impl$neighbors, this.impl$neighbors.length);
    }

    @Override
    public void bridge$markChunkDirty() {
        this.dirty = true;
    }

    @Override
    public boolean bridge$isQueuedForUnload() {
        throw new UnsupportedOperationException("implement me");
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
    public void bridge$setNeighborChunk(final int index, @Nullable final net.minecraft.world.chunk.Chunk chunk) {
        this.impl$neighbors[index] = chunk;
    }

    @Nullable
    @Override
    public net.minecraft.world.chunk.Chunk bridge$getNeighborChunk(final int index) {
        return this.impl$neighbors[index];
    }

    @Override
    public List<net.minecraft.world.chunk.Chunk> bridge$getNeighbors() {
        final List<net.minecraft.world.chunk.Chunk> neighborList = new ArrayList<>();
        for (final net.minecraft.world.chunk.Chunk neighbor : this.impl$neighbors) {
            if (neighbor != null) {
                neighborList.add(neighbor);
            }
        }
        return neighborList;
    }

    @Override
    public boolean bridge$areNeighborsLoaded() {
        for (int i = 0; i < 4; i++) {
            if (this.impl$neighbors[i] == null) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void bridge$setNeighbor(final Direction direction, @Nullable final net.minecraft.world.chunk.Chunk neighbor) {
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
                .add("World", this.world)
                .add("Position", this.pos.x + this.pos.z)
                .toString();
    }

    @Override
    public long bridge$getCacheKey() {
        return this.impl$cacheKey;
    }
}
