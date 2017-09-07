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
package org.spongepowered.common.interfaces;

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.ChunkPrimer;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.extent.EntityUniverse;
import org.spongepowered.common.entity.PlayerTracker;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import javax.annotation.Nullable;

public interface IMixinChunk {

    Map<Short, PlayerTracker> getTrackedShortPlayerPositions();

    Map<Integer, PlayerTracker> getTrackedIntPlayerPositions();

    Optional<User> getBlockOwner(BlockPos pos);

    Optional<User> getBlockNotifier(BlockPos pos);

    @Nullable
    IBlockState setBlockState(BlockPos pos, IBlockState newState, IBlockState currentState, @Nullable BlockSnapshot originalBlockSnapshot);

    @Nullable
    IBlockState setBlockState(BlockPos pos, IBlockState newState, IBlockState currentState, @Nullable BlockSnapshot originalBlockSnapshot, BlockChangeFlag flag);

    void setBlockNotifier(BlockPos pos, UUID uuid);

    void setBlockCreator(BlockPos pos, UUID uuid);

    void addTrackedBlockPosition(Block block, BlockPos pos, User user, PlayerTracker.Type trackerType);

    void setTrackedIntPlayerPositions(Map<Integer, PlayerTracker> trackedPlayerPositions);

    void setTrackedShortPlayerPositions(Map<Short, PlayerTracker> trackedPlayerPositions);

    void setNeighbor(Direction direction, Chunk neighbor);

    void setNeighborChunk(int index, @Nullable net.minecraft.world.chunk.Chunk chunk);

    @Nullable
    net.minecraft.world.chunk.Chunk getNeighborChunk(int index);

    boolean areNeighborsLoaded();

    long getScheduledForUnload();

    void setScheduledForUnload(long scheduled);

    void getIntersectingEntities(Vector3d start, Vector3d direction, double distance, Predicate<EntityUniverse.EntityHit> filter,
            double entryY, double exitY, Set<EntityUniverse.EntityHit> intersections);

    boolean isPersistedChunk();

    void setPersistedChunk(boolean flag);

    void fill(ChunkPrimer primer);

    boolean isSpawning();

    void setIsSpawning(boolean spawning);

    AtomicInteger getPendingLightUpdates();

    long getLightUpdateTime();

    void setLightUpdateTime(long time);

    List<net.minecraft.world.chunk.Chunk> getNeighbors();

    boolean isChunkLoaded();

    boolean isQueuedForUnload();

    CopyOnWriteArrayList<Short> getQueuedLightingUpdates(EnumSkyBlock type);
}
