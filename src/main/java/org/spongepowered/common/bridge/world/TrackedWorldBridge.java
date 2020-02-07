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
package org.spongepowered.common.bridge.world;

import net.minecraft.block.BlockState;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.event.tracking.ScheduledBlockChange;
import org.spongepowered.common.event.tracking.context.MultiBlockCaptureSupplier;
import org.spongepowered.common.event.tracking.context.SpongeProxyBlockAccess;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A specialized {@link WorldBridge} or {@link ServerWorldBridge}
 * that has extra {@link org.spongepowered.common.event.tracking.PhaseTracker} related
 * methods that otherwise bear no other changes to the game.
 */
public interface TrackedWorldBridge {

    boolean bridge$forceSpawnEntity(Entity entity);

    SpongeProxyBlockAccess bridge$getProxyAccess();

    BlockingQueue<ScheduledBlockChange> bridge$getScheduledBlockChangeList();

    /**
     * Delegates to the {@link ServerWorld} to perform the lookup for a {@link Chunk}
     * such that if the target {@link BlockPos} results in a {@code false} for
     * {@link ServerWorld#isBlockLoaded(BlockPos)}, {@link BlockSnapshot#empty()}
     * will be returned. Likewise, optimizes the creation of the snapshot by performing
     * the {@link Chunk#getBlockState(BlockPos)} and {@link Chunk#getTileEntity(BlockPos, Chunk.CreateEntityType)}
     * lookup on the same chunk, avoiding an additional chunk lookup.
     *
     * <p>This should be used when the "known" {@link BlockState} for the target
     * position is not known. If it is known, use {@link #bridge$createSnapshot(BlockState, BlockPos, BlockChangeFlag)}</p>
     *
     * @param pos The target position to get the block snapshot for
     * @param flag The block change flag to associate with the snapshot.
     * @return The snapshot, or none if not loaded
     */
    SpongeBlockSnapshot bridge$createSnapshot(BlockPos pos, BlockChangeFlag flag);

    /**
     * Creates a {@link BlockSnapshot} but performs an additional {@link Chunk#getTileEntity(BlockPos, Chunk.CreateEntityType)}
     * lookup if the providing {@link BlockState#getBlock()} {@code instanceof} is
     * {@code true} for being an {@link ITileEntityProvider} or
     * {@link SpongeImplHooks#hasBlockTileEntity(BlockState)}, and associates
     * the resulting snapshot of said Tile with the snapshot. This is useful for in-progress
     * snapshot creation during transaction building for {@link MultiBlockCaptureSupplier}
     * or where sensitivity to the {@link SpongeProxyBlockAccess} is needed.
     *
     * <p>If the {@link TileEntity} is already known, and no lookups are needed, use
     * {@link #bridge$createSnapshotWithEntity(BlockState, BlockPos, BlockChangeFlag, TileEntity)} as it avoids
     * any further chunk lookups.</p>
     *
     * @param state The block state
     * @param pos The target position
     * @param updateFlag The update flag
     * @return The snapshot, never NONE
     */
    SpongeBlockSnapshot bridge$createSnapshot(BlockState state, BlockPos pos, BlockChangeFlag updateFlag);

    /**
     * Similar to {@link #bridge$createSnapshot(BlockState, BlockPos, BlockChangeFlag)},
     * but with the added avoidance of a {@link TileEntity} lookup during the creation of the resulting
     * {@link SpongeBlockSnapshot}.
     *
     * @param state The state
     * @param pos The position
     * @param updateFlag The update flag
     * @param tileEntity The tile entity to serialize, if available
     * @return The snapshot, never NONE
     */
    SpongeBlockSnapshot bridge$createSnapshotWithEntity(BlockState state, BlockPos pos, BlockChangeFlag updateFlag, @Nullable TileEntity tileEntity);

}
