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
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkGenerator;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.volume.entity.ReadableEntityVolume;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.world.chunk.ActiveChunkReferantBridge;
import org.spongepowered.common.bridge.world.chunk.CacheKeyBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.world.gen.WorldGenConstants;
import org.spongepowered.math.GenericMath;
import org.spongepowered.math.vector.Vector3d;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Mixin(net.minecraft.world.chunk.Chunk.class)
public abstract class ChunkMixin implements ChunkBridge, CacheKeyBridge {

    // @formatter:off
    @Shadow @Final private World world;
    @Shadow @Final public int x;
    @Shadow @Final public int z;
    @Shadow @Final private ExtendedBlockStorage[] storageArrays;
    @Shadow @Final private int[] precipitationHeightMap;
    @Shadow @Final private int[] heightMap;
    @Shadow @Final private ClassInheritanceMultiMap<Entity>[] entityLists;
    @Shadow @Final private Map<BlockPos, TileEntity> tileEntities;
    @Shadow private boolean loaded;
    @Shadow private boolean dirty;
    @Shadow public boolean unloadQueued;

    @Shadow @Nullable public abstract TileEntity getTileEntity(BlockPos pos, net.minecraft.world.chunk.Chunk.CreateEntityType p_177424_2_);
    @Shadow public abstract void generateSkylightMap();
    @Shadow public abstract int getLightFor(LightType p_177413_1_, BlockPos pos);
    @Shadow public abstract BlockState getBlockState(BlockPos pos);
    @Shadow public abstract BlockState getBlockState(int x, int y, int z);
    @Shadow public abstract Biome getBiome(BlockPos pos, BiomeProvider chunkManager);
    @Shadow private void propagateSkylightOcclusion(final int x, final int z) { }
    @Shadow private void relightBlock(final int x, final int y, final int z) { }
    @Shadow protected abstract void populate(ChunkGenerator generator);

    // @formatter:on

    private long impl$scheduledForUnload = -1; // delay chunk unloads
    private boolean impl$persistedChunk = false;
    private boolean impl$isSpawning = false;
    private final net.minecraft.world.chunk.Chunk[] impl$neighbors = new net.minecraft.world.chunk.Chunk[4];
    private long impl$cacheKey;

    @Inject(method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/util/math/ChunkPos;[Lnet/minecraft/world/biome/Biome;)V",
            at = @At("RETURN"))
    private void impl$onConstruct(World worldIn, ChunkPos p_i49945_2_, Biome[] p_i49945_3_, CallbackInfo ci) {
        this.impl$cacheKey = ChunkPos.asLong(x, z);
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
        return this.unloadQueued;
    }

    @Override
    public boolean bridge$isPersistedChunk() {
        return this.impl$persistedChunk;
    }

    @Override
    public void bridge$setPersistedChunk(final boolean flag) {
        this.impl$persistedChunk = flag;
        // update persisted status for entities and TE's
        for (final TileEntity tileEntity : this.tileEntities.values()) {
            ((ActiveChunkReferantBridge) tileEntity).bridge$setActiveChunk(this);
        }
        for (final ClassInheritanceMultiMap<Entity> entityList : this.entityLists) {
            for (final Entity entity : entityList) {
                ((ActiveChunkReferantBridge) entity).bridge$setActiveChunk(this);
            }
        }
    }

    @Override
    public boolean bridge$isSpawning() {
        return this.impl$isSpawning;
    }

    @Override
    public void bridge$setIsSpawning(final boolean spawning) {
        this.impl$isSpawning = spawning;
    }

    @Inject(method = "addEntity", at = @At("RETURN"))
    private void impl$SetActiveChunkOnEntityAdd(final Entity entityIn, final CallbackInfo ci) {
        ((ActiveChunkReferantBridge) entityIn).bridge$setActiveChunk(this);
    }

    @Inject(method = "removeEntityAtIndex", at = @At("RETURN"))
    private void impl$ResetEntityActiveChunk(final Entity entityIn, final int index, final CallbackInfo ci) {
        ((ActiveChunkReferantBridge) entityIn).bridge$setActiveChunk(null);
    }

    @Redirect(method = "removeTileEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntity;remove()V"))
    private void impl$resetTileEntityActiveChunk(final TileEntity tileEntityIn) {
        ((ActiveChunkReferantBridge) tileEntityIn).bridge$setActiveChunk(null);
        tileEntityIn.remove();
    }

    // These methods are enabled in ChunkMixin_OwnershipTracked as a Mixin plugin

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
    public Optional<User> bridge$getBlockOwner(final BlockPos pos) {
        return Optional.empty();
    }

    @Override
    public Optional<UUID> bridge$getBlockOwnerUUID(final BlockPos pos) {
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
        this.impl$neighbors[SpongeImpl.directionToIndex(direction)] = neighbor;
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
    public void bridge$getIntersectingEntities(final Vector3d start, final Vector3d direction, final double distance,
            final java.util.function.Predicate<? super ReadableEntityVolume.EntityHit> filter, final double entryY, final double exitY,
            final Set<? super ReadableEntityVolume.EntityHit> intersections) {
        // Order the entry and exit y coordinates by magnitude
        final double yMin = Math.min(entryY, exitY);
        final double yMax = Math.max(entryY, exitY);
        // Added offset matches the one in Chunk.getEntitiesWithinAABBForEntity
        final int lowestSubChunk = GenericMath.clamp(GenericMath.floor((yMin - 2) / 16D), 0, this.entityLists.length - 1);
        final int highestSubChunk = GenericMath.clamp(GenericMath.floor((yMax + 2) / 16D), 0, this.entityLists.length - 1);
        // For each sub-chunk, perform intersections in its entity list
        for (int i = lowestSubChunk; i <= highestSubChunk; i++) {
            this.impl$getIntersectingEntities(this.entityLists[i], start, direction, distance, filter, intersections);
        }
    }

    private void impl$getIntersectingEntities(final Collection<? extends Entity> entities, final Vector3d start, final Vector3d direction,
            final double distance, final java.util.function.Predicate<? super ReadableEntityVolume.EntityHit> filter,
            final Set<? super ReadableEntityVolume.EntityHit> intersections) {
        // Check each entity in the list
        for (final net.minecraft.entity.Entity entity : entities) {
            final org.spongepowered.api.entity.Entity spongeEntity = (org.spongepowered.api.entity.Entity) entity;
            final Optional<AABB> box = spongeEntity.getBoundingBox();
            // Can't intersect if the entity doesn't have a bounding box
            if (!box.isPresent()) {
                continue;
            }
            // Ignore entities that didn't intersect
            final Optional<Tuple<Vector3d, Vector3d>> optionalIntersection = box.get().intersects(start, direction);
            if (!optionalIntersection.isPresent()) {
                continue;
            }
            // Check that the entity isn't too far away
            final Tuple<Vector3d, Vector3d> intersection = optionalIntersection.get();
            final double distanceSquared = intersection.getFirst().sub(start).lengthSquared();
            if (distanceSquared > distance * distance) {
                continue;
            }
            // Now test the filter on the entity and intersection
            final ReadableEntityVolume.EntityHit hit = new ReadableEntityVolume.EntityHit(spongeEntity, intersection.getFirst(), intersection.getSecond(), Math.sqrt(distanceSquared));
            if (!filter.test(hit)) {
                continue;
            }
            // If everything passes we have an intersection!
            intersections.add(hit);
            // If the entity has part, recurse on these
            final net.minecraft.entity.Entity[] parts = entity.getParts();
            if (parts != null && parts.length > 0) {
                this.impl$getIntersectingEntities(Arrays.asList(parts), start, direction, distance, filter, intersections);
            }
        }
    }

    @Inject(method = "generateSkylightMap", at = @At("HEAD"), cancellable = true)
    private void impl$IfLightingEnabledCancel(final CallbackInfo ci) {
        if (!WorldGenConstants.lightingEnabled) {
            ci.cancel();
        }
    }

    @Override
    public void bridge$fill(final ChunkPrimer primer) {
        final boolean flag = this.world.dimension.hasSkyLight();
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                for (int y = 0; y < 256; ++y) {
                    final BlockState block = primer.getBlockState(x, y, z);
                    if (block.getMaterial() != Material.AIR) {
                        final int section = y >> 4;
                        if (this.storageArrays[section] == net.minecraft.world.chunk.Chunk.EMPTY_SECTION) {
                            this.storageArrays[section] = new ExtendedBlockStorage(section << 4, flag);
                        }
                        this.storageArrays[section].setBlockState(x, y & 15, z, block);
                    }
                }
            }
        }
    }

    @Redirect(
            method = "addTileEntity(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/tileentity/TileEntity;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntity;invalidate()V"))
    private void redirectInvalidate(final TileEntity te) {
        SpongeImplHooks.onTileEntityInvalidate(te);
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
                .add("Position", this.x + this.z)
                .toString();
    }

    @Override
    public long bridge$getCacheKey() {
        return this.impl$cacheKey;
    }
}
