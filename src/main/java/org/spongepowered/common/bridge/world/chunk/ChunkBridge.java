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
package org.spongepowered.common.bridge.world.chunk;

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkGenerator;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.extent.EntityUniverse;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.context.BlockTransaction;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import javax.annotation.Nullable;

public interface ChunkBridge {

    Map<Short, PlayerTracker> bridge$getTrackedShortPlayerPositions();

    Map<Integer, PlayerTracker> bridge$getTrackedIntPlayerPositions();

    Optional<User> bridge$getBlockOwner(BlockPos pos);

    Optional<UUID> bridge$getBlockOwnerUUID(BlockPos pos);

    Optional<User> bridge$getBlockNotifier(BlockPos pos);

    Optional<UUID> bridge$getBlockNotifierUUID(BlockPos pos);

    @Nullable
    BlockState bridge$setBlockState(BlockPos pos, BlockState newState, BlockState currentState, BlockChangeFlag flag);

    void bridge$setBlockNotifier(BlockPos pos, UUID uuid);

    void bridge$setBlockCreator(BlockPos pos, UUID uuid);

    void bridge$addTrackedBlockPosition(Block block, BlockPos pos, User user, PlayerTracker.Type trackerType);

    void bridge$setTrackedIntPlayerPositions(Map<Integer, PlayerTracker> trackedPlayerPositions);

    void bridge$setTrackedShortPlayerPositions(Map<Short, PlayerTracker> trackedPlayerPositions);

    void bridge$setNeighbor(Direction direction, Chunk neighbor);

    void bridge$setNeighborChunk(int index, @Nullable Chunk chunk);

    @Nullable
    Chunk bridge$getNeighborChunk(int index);

    boolean bridge$areNeighborsLoaded();

    long bridge$getScheduledForUnload();

    void bridge$setScheduledForUnload(long scheduled);

    void bridge$getIntersectingEntities(Vector3d start, Vector3d direction, double distance, Predicate<? super EntityUniverse.EntityHit> filter,
            double entryY, double exitY, Set<? super EntityUniverse.EntityHit> intersections);

    boolean bridge$isPersistedChunk();

    void bridge$setPersistedChunk(boolean flag);

    void bridge$fill(ChunkPrimer primer);

    boolean bridge$isSpawning();

    void bridge$setIsSpawning(boolean spawning);

    List<Chunk> bridge$getNeighbors();

    boolean bridge$isQueuedForUnload();

    void bridge$markChunkDirty();

    boolean bridge$isActive();

    void bridge$removeTileEntity(TileEntity removed);

    /**
     * Specifically similar to {@link Chunk#addTileEntity(BlockPos, TileEntity)}
     * except without the validation check of {@link Chunk#getBlockState(BlockPos)}
     * equality due to delayed tracking. This will allow the tracker to perform delayed tile entity additions
     * and removals with physics without causing issues. Should not be called in any other fashion except from
     * {@link BlockTransaction#process(Transaction, IPhaseState, PhaseContext, int)}.
     *
     * @param targetPos
     * @param added
     */
    void bridge$setTileEntity(BlockPos targetPos, TileEntity added);

    Chunk[] bridge$getNeighborArray();

    // TODO Mixin 0.8
    @Deprecated
    void accessor$populate(ChunkGenerator generator);
}
