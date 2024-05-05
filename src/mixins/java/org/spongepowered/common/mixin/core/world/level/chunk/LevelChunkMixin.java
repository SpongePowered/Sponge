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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.ticks.LevelChunkTicks;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.chunk.BlockChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.accessor.server.level.ChunkMapAccessor;
import org.spongepowered.common.applaunch.config.core.SpongeConfigs;
import org.spongepowered.common.bridge.CreatorTrackedBridge;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.bridge.data.DataHolderProcessor;
import org.spongepowered.common.bridge.data.SpongeDataHolderBridge;
import org.spongepowered.common.bridge.world.level.LevelBridge;
import org.spongepowered.common.bridge.world.level.chunk.CacheKeyBridge;
import org.spongepowered.common.bridge.world.level.chunk.LevelChunkBridge;
import org.spongepowered.common.bridge.world.level.storage.PrimaryLevelDataBridge;
import org.spongepowered.common.data.holder.SpongeMutableDataHolder;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.DirectionUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Mixin(net.minecraft.world.level.chunk.LevelChunk.class)
public abstract class LevelChunkMixin extends ChunkAccess implements LevelChunkBridge, CacheKeyBridge, SpongeMutableDataHolder, SpongeDataHolderBridge, DataCompoundHolder, BlockChunk {

    // @formatter:off
    @Shadow @Final private Level level;
    @Shadow private boolean loaded;

    @Shadow @Nullable public abstract BlockEntity shadow$getBlockEntity(BlockPos pos, net.minecraft.world.level.chunk.LevelChunk.EntityCreationType p_177424_2_);
    @Shadow public abstract BlockState shadow$getBlockState(BlockPos pos);
    @Shadow public abstract void shadow$addEntity(net.minecraft.world.entity.Entity param0);
    // @formatter:on

    private long impl$scheduledForUnload = -1; // delay chunk unloads
    private boolean impl$persistedChunk = false;
    private boolean impl$isSpawning = false;
    private final net.minecraft.world.level.chunk.LevelChunk[] impl$neighbors = new net.minecraft.world.level.chunk.LevelChunk[4];
    private long impl$cacheKey;
    private Map<Integer, PlayerTracker> impl$trackedIntBlockPositions = new HashMap<>();
    private Map<Short, PlayerTracker> impl$trackedShortBlockPositions = new HashMap<>();
    private @Nullable CompoundTag impl$compound;

    public LevelChunkMixin(
        final ChunkPos $$0,
        final UpgradeData $$1,
        final LevelHeightAccessor $$2,
        final Registry<Biome> $$3,
        final long $$4,
        final LevelChunkSection[] $$5,
        final BlendingData $$6
    ) {
        super($$0, $$1, $$2, $$3, $$4, $$5, $$6);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/UpgradeData;Lnet/minecraft/world/ticks/LevelChunkTicks;Lnet/minecraft/world/ticks/LevelChunkTicks;J[Lnet/minecraft/world/level/chunk/LevelChunkSection;Lnet/minecraft/world/level/chunk/LevelChunk$PostLoadProcessor;Lnet/minecraft/world/level/levelgen/blending/BlendingData;)V",
            at = @At("RETURN"))
    private void impl$onConstruct(final Level $$0, final ChunkPos $$1, final UpgradeData $$2, final LevelChunkTicks $$3, final LevelChunkTicks $$4,
                                  final long $$5, final LevelChunkSection[] $$6, final LevelChunk.PostLoadProcessor $$7, final BlendingData $$8,
                                  final CallbackInfo ci) {
        this.impl$cacheKey = ChunkPos.asLong($$1.x, $$1.z);
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
        return ((ChunkMapAccessor) ((ServerChunkCache) this.level.getChunkSource()).chunkMap).accessor$pendingUnloads().containsKey(this.chunkPos.toLong());
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
    public Map<Integer, PlayerTracker> bridge$getTrackedIntPlayerPositions() {
        return this.impl$trackedIntBlockPositions;
    }

    @Override
    public Map<Short, PlayerTracker> bridge$getTrackedShortPlayerPositions() {
        return this.impl$trackedShortBlockPositions;
    }

    @Override
    public void bridge$setTrackedIntPlayerPositions(final Map<Integer, PlayerTracker> trackedPositions) {
        this.impl$trackedIntBlockPositions = trackedPositions;
    }

    @Override
    public void bridge$setTrackedShortPlayerPositions(final Map<Short, PlayerTracker> trackedPositions) {
        this.impl$trackedShortBlockPositions = trackedPositions;
    }

    @Override
    public void bridge$addTrackedBlockPosition(final Block block, final BlockPos pos, final UUID uuid, final PlayerTracker.Type trackerType) {
        if (((LevelBridge) this.level).bridge$isFake()) {
            return;
        }
        if (!PhaseTracker.getInstance().getPhaseContext().tracksCreatorsAndNotifiers()) {
            // Don't track chunk gen
            return;
        }

        // Update TE tracking cache
        // We must always check for a TE as a mod block may not implement ITileEntityProvider if a TE exists
        // Note: We do not check SpongeImplHooks.hasBlockTileEntity(block, state) as neighbor notifications do not include blockstate.
        final BlockEntity blockEntity = this.blockEntities.get(pos);
        if (blockEntity != null) {
            if (blockEntity instanceof CreatorTrackedBridge) {
                final CreatorTrackedBridge trackedBlockEntity = (CreatorTrackedBridge) blockEntity;
                if (trackerType == PlayerTracker.Type.NOTIFIER) {
                    if (Objects.equals(trackedBlockEntity.tracker$getNotifierUUID().orElse(null), uuid)) {
                        return;
                    }
                    trackedBlockEntity.tracker$setTrackedUUID(PlayerTracker.Type.NOTIFIER, uuid);
                } else {
                    if (Objects.equals(trackedBlockEntity.tracker$getCreatorUUID().orElse(null), uuid)) {
                        return;
                    }
                    trackedBlockEntity.tracker$setTrackedUUID(PlayerTracker.Type.CREATOR, uuid);
                }
            }
        }

        if (trackerType == PlayerTracker.Type.CREATOR) {
            this.impl$setTrackedUUID(pos, uuid, trackerType, (pt, idx) -> {
                pt.creatorindex = idx;
                pt.notifierIndex = idx;
            });
        } else {
            this.impl$setTrackedUUID(pos, uuid, trackerType, (pt, idx) -> pt.notifierIndex = idx);
        }
    }

    public Optional<UUID> bridge$trackedUUID(final BlockPos pos, final Function<PlayerTracker, Integer> func) {
        if (((LevelBridge) this.level).bridge$isFake()) {
            return Optional.empty();
        }

        final int key = Constants.Sponge.blockPosToInt(pos);
        final PlayerTracker intTracker = this.impl$trackedIntBlockPositions.get(key);
        if (intTracker != null) {
            final int ownerIndex = func.apply(intTracker);
            return this.impl$getValidatedUUID(key, ownerIndex);
        }
        final short shortKey = Constants.Sponge.blockPosToShort(pos);
        final PlayerTracker shortTracker = this.impl$trackedShortBlockPositions.get(shortKey);
        if (shortTracker != null) {
            final int ownerIndex = func.apply(shortTracker);
            return this.impl$getValidatedUUID(shortKey, ownerIndex);
        }
        return Optional.empty();
    }

    @Override
    public Optional<UUID> bridge$getBlockCreatorUUID(final BlockPos pos) {
       return this.bridge$trackedUUID(pos, pt -> pt.creatorindex);
    }

    @Override
    public Optional<UUID> bridge$getBlockNotifierUUID(final BlockPos pos) {
        return this.bridge$trackedUUID(pos, pt -> pt.notifierIndex);
    }

    private <T> void impl$computePlayerTracker(final Map<T, PlayerTracker> map, final T blockPos, final int index, final PlayerTracker.Type type, final BiConsumer<PlayerTracker, Integer> consumer) {
        final PlayerTracker tracker = map.get(blockPos);
        if (tracker != null) {
            consumer.accept(tracker, index);
        } else {
            map.put(blockPos, new PlayerTracker(index, type));
        }
    }

    private void impl$setTrackedUUID(final BlockPos pos, final UUID uuid, final PlayerTracker.Type type, final BiConsumer<PlayerTracker, Integer> consumer) {
        if (((LevelBridge) this.level).bridge$isFake()) {
            return;
        }
        final PrimaryLevelDataBridge worldInfo = (PrimaryLevelDataBridge) this.level.getLevelData();
        final int index = uuid == null ? -1 : worldInfo.bridge$getIndexForUniqueId(uuid);
        if (pos.getY() <= 255) {
            final short blockPos = Constants.Sponge.blockPosToShort(pos);
            this.impl$computePlayerTracker(this.impl$trackedShortBlockPositions, blockPos, index, type, consumer);
            return;
        }
        final int blockPos = Constants.Sponge.blockPosToInt(pos);
        this.impl$computePlayerTracker(this.impl$trackedIntBlockPositions, blockPos, index, type, consumer);
    }

    @Override
    public void bridge$setBlockNotifier(final BlockPos pos, @Nullable final UUID uuid) {
       this.impl$setTrackedUUID(pos, uuid, PlayerTracker.Type.NOTIFIER, (pt, idx) -> pt.notifierIndex = idx);
    }

    @Override
    public void bridge$setBlockCreator(final BlockPos pos, @Nullable final UUID uuid) {
        this.impl$setTrackedUUID(pos, uuid, PlayerTracker.Type.CREATOR, (pt, idx) -> pt.creatorindex = idx);
    }

    private Optional<UUID> impl$getValidatedUUID(final int key, final int ownerIndex) {
        final PrimaryLevelDataBridge worldInfo = (PrimaryLevelDataBridge) this.level.getLevelData();
        final UUID uuid = worldInfo.bridge$getUniqueIdForIndex(ownerIndex).orElse(null);
        if (uuid != null) {
            // Verify id is valid and not invalid
            if (SpongeConfigs.getCommon().get().world.invalidLookupUuids.contains(uuid)) {
                if (key <= Short.MAX_VALUE) {
                    this.impl$trackedShortBlockPositions.remove((short) key);
                }
                this.impl$trackedIntBlockPositions.remove(key);
                return Optional.empty();
            }

            // player is not online, get or create user from storage
            return Optional.of(uuid);
        }
        return Optional.empty();
    }

    // Fast neighbor methods for internal use
    @Override
    public void bridge$setNeighborChunk(final int index, final net.minecraft.world.level.chunk.@Nullable LevelChunk chunk) {
        this.impl$neighbors[index] = chunk;
    }

    @Override
    public net.minecraft.world.level.chunk.@Nullable LevelChunk bridge$getNeighborChunk(final int index) {
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
        for (int i = 0; i < 4; i++) {
            if (this.impl$neighbors[i] == null) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void bridge$setNeighbor(final Direction direction, final net.minecraft.world.level.chunk.@Nullable LevelChunk neighbor) {
        this.impl$neighbors[DirectionUtil.directionToIndex(direction)] = neighbor;
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
        return new StringJoiner(", ", LevelChunkMixin.class.getSimpleName() + "[", "]")
                .add("World=" + this.level)
                .add("Position=" + this.chunkPos.x + ":" + this.chunkPos.z)
                .add("super=" + super.toString())
                .toString();
    }

    @Override
    public long bridge$getCacheKey() {
        return this.impl$cacheKey;
    }

    @Override
    public boolean bridge$spawnEntity(final org.spongepowered.api.entity.Entity entity) {
        final net.minecraft.world.entity.Entity mcEntity = (net.minecraft.world.entity.Entity) entity;
        final BlockPos blockPos = mcEntity.blockPosition();
        if (this.chunkPos.x == blockPos.getX() >> 4 && this.chunkPos.z == blockPos.getZ() >> 4) {
            // Calling addEntity on the chunk only adds them to storage,
            // we need to redirect this to add to the world.
            //
            // See https://github.com/SpongePowered/Sponge/issues/3488
            this.level.addFreshEntity(mcEntity);
            return true;
        }
        return false;
    }

    @Override
    public CompoundTag data$getCompound() {
        return this.impl$compound;
    }

    @Override
    public void data$setCompound(final CompoundTag nbt) {
        this.impl$compound = nbt;
    }


    @Override
    public <E> DataTransactionResult bridge$offer(final Key<@NonNull ? extends Value<E>> key, final E value) {
        final DataTransactionResult result = DataHolderProcessor.bridge$offer(this, key, value);
        this.unsaved = true;
        return result;
    }

    @Override
    public <E> DataTransactionResult bridge$remove(final Key<@NonNull ? extends Value<E>> key) {
        final DataTransactionResult result = DataHolderProcessor.bridge$remove(this, key);
        this.unsaved = true;
        return result;
    }

}
