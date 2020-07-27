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

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.common.bridge.world.TrackedWorldBridge;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.context.BlockTransaction;
import org.spongepowered.common.event.tracking.context.transaction.pipeline.ChunkPipeline;
import org.spongepowered.common.world.SpongeBlockChangeFlag;

/**
 * Due to the split of implementation mixins implementing the {@link org.spongepowered.common.event.tracking.PhaseTracker}
 * and it's various hooks, the specific methods introduced to bridges mandates that
 * those methods are separated from the common core bridges.
 */
public interface TrackedChunkBridge {

    /**
     * TODO - document the pipeline
     *
     * @param pos The position
     * @param newState The new state
     * @param currentState The current block state
     * @param flag The change flags, for snapshot creation
     * @return The original block state at the position (redundant with currentState)
     */
    ChunkPipeline bridge$createChunkPipeline(BlockPos pos, BlockState newState, BlockState currentState, SpongeBlockChangeFlag flag);

    /**
     * A callback method for a tile entity being removed, only when the removal is being requested due
     * to a block change being processed by the {@link org.spongepowered.common.event.tracking.PhaseTracker}.
     * This is <strong>not</strong> to be mistaken for generalized removing of the tile entity by outside
     * consumers.
     *
     * @param removed The tile entity to remove from the internal maps
     */
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

    /**
     * Forge compatibility method
     * @param originalBlock
     * @param replacingBlock
     * @param replacingState
     * @return
     */
    default boolean bridge$replacingBlockHasTile(Block originalBlock, Block replacingBlock, BlockState replacingState) {
        return originalBlock != replacingBlock && originalBlock instanceof ITileEntityProvider;
    }

    default boolean bridge$shouldRefreshTile(Block oldBlock, Block newBlock, BlockState oldState, BlockState newState) {
        return oldBlock != newBlock && oldBlock instanceof ITileEntityProvider;
    }
}
