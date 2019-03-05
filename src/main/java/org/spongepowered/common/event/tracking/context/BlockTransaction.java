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
package org.spongepowered.common.event.tracking.context;

import com.google.common.base.MoreObjects;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServerMulti;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.world.World;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.world.BlockChange;
import org.spongepowered.common.world.SpongeBlockChangeFlag;

import java.util.Map;

import javax.annotation.Nullable;

public abstract class BlockTransaction {

    final int transactionIndex;
    final int snapshotIndex;
    boolean isCancelled = false;

    BlockTransaction(int i, int snapshotIndex) {
        this.transactionIndex = i;
        this.snapshotIndex = snapshotIndex;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .toString();
    }

    abstract void cancel(WorldServer worldServer, BlockPos blockPos);

    abstract void process(Transaction<BlockSnapshot> eventTransaction, IPhaseState phaseState, PhaseContext<?> phaseContext,
        Map<BlockPos, IBlockState> processingBlocks, int currentDepth);

    @Nullable
    public IBlockState getBlockState(BlockPos pos) {
        return null;
    }

    static class TileEntityAdd extends BlockTransaction {

        final TileEntity added;
        final SpongeBlockSnapshot addedSnapshot;
        final IBlockState newState;

        TileEntityAdd(int i, int snapshotIndex, TileEntity added, SpongeBlockSnapshot attachedSnapshot, IBlockState newState) {
            super(i, snapshotIndex);

            this.added = added;
            addedSnapshot = attachedSnapshot;
            this.newState = newState;
        }

        @Override
        void cancel(WorldServer worldServer, BlockPos blockPos) {

        }

        @Override
        void process(Transaction<BlockSnapshot> eventTransaction, IPhaseState phaseState, PhaseContext<?> phaseContext,
            Map<BlockPos, IBlockState> processingBlocks, int currentDepth) {
            final WorldServer worldServer = this.addedSnapshot.getWorldServer();
            ((IMixinWorldServer) worldServer).setProxyAccess(this);
        }

        @Nullable
        @Override
        public IBlockState getBlockState(BlockPos pos) {
            return this.addedSnapshot.getBlockPos().equals(pos) ? this.newState : null;
        }
    }

    static class RemoveTileEntity extends BlockTransaction {

        final TileEntity removed;
        final SpongeBlockSnapshot tileSnapshot;
        final IBlockState newState;

        RemoveTileEntity(int i, int snapshotIndex, TileEntity removed, SpongeBlockSnapshot attachedSnapshot, IBlockState newState) {
            super(i, snapshotIndex);
            this.removed = removed;
            tileSnapshot = attachedSnapshot;
            this.newState = newState;
        }

        @Override
        void cancel(WorldServer worldServer, BlockPos blockPos) {

        }

        @Override
        void process(Transaction<BlockSnapshot> eventTransaction, IPhaseState phaseState, PhaseContext<?> phaseContext,
            Map<BlockPos, IBlockState> processingBlocks, int currentDepth) {
            final BlockPos targetPosition = this.tileSnapshot.getBlockPos();
            final WorldServer worldServer = this.tileSnapshot.getWorldServer();
            ((IMixinWorldServer) worldServer).setProxyAccess(this);
            worldServer.updateComparatorOutputLevel(targetPosition, newState.getBlock());

        }

        @Nullable
        @Override
        public IBlockState getBlockState(BlockPos pos) {
            return this.tileSnapshot.getBlockPos().equals(pos) ? this.newState : null;
        }
    }

    static class ReplaceTileEntity extends BlockTransaction {

        final TileEntity added;
        final TileEntity removed;
        final SpongeBlockSnapshot removedSnapshot;

        ReplaceTileEntity(int i, int snapshotIndex, TileEntity added, TileEntity removed, SpongeBlockSnapshot attachedSnapshot) {
            super(i, snapshotIndex);
            this.added = added;
            this.removed = removed;
            removedSnapshot = attachedSnapshot;
        }

        @Override
        void cancel(WorldServer worldServer, BlockPos blockPos) {

        }

        @Override
        void process(Transaction<BlockSnapshot> eventTransaction, IPhaseState phaseState, PhaseContext<?> phaseContext,
            Map<BlockPos, IBlockState> processingBlocks, int currentDepth) {


        }
    }

    static class ChangeBlock extends BlockTransaction {

        final SpongeBlockSnapshot original;
        final IBlockState newState;
        final SpongeBlockChangeFlag blockChangeFlag;

        ChangeBlock(int i, int snapshotIndex, SpongeBlockSnapshot attachedSnapshot, IBlockState newState, SpongeBlockChangeFlag blockChange, @Nullable BlockChange changeFlag) {
            super(i, snapshotIndex);
            this.original = attachedSnapshot;
            this.newState = newState;
            this.blockChangeFlag = blockChange;
        }

        @Override
        void cancel(WorldServer worldServer, BlockPos blockPos) {

        }

        @Nullable
        @Override
        public IBlockState getBlockState(BlockPos pos) {
            return this.original.getBlockPos().equals(pos) ? this.newState : null;
        }

        @SuppressWarnings("unchecked")
        @Override
        void process(Transaction<BlockSnapshot> eventTransaction, IPhaseState phaseState, PhaseContext<?> phaseContext,
            Map<BlockPos, IBlockState> processingBlocks, int currentDepth) {
            final BlockPos targetPosition = original.getBlockPos();
            final WorldServer worldServer = original.getWorldServer();
            final SpongeBlockSnapshot newBlockSnapshot = (SpongeBlockSnapshot) eventTransaction.getFinal();

            TrackingUtil.performBlockEntitySpawns(phaseState, phaseContext, original, targetPosition);
            SpongeHooks.logBlockAction(worldServer, original.blockChange, eventTransaction);
            final IBlockState oldState = (IBlockState) original.getState();
            // Any requests to the world need to propogate to having the "changed" block, before
            // the block potentially changes from future changes.
            ((IMixinWorldServer) worldServer).setProxyAccess(this);
            processingBlocks.put(targetPosition, this.newState);


            oldState.getBlock().breakBlock(worldServer, targetPosition, oldState);

            // We call onBlockAdded here for blocks without a TileEntity.
            // MixinChunk#setBlockState will call onBlockAdded for blocks
            // with a TileEntity or when capturing is not being done.
            TrackingUtil.performOnBlockAdded(phaseState, phaseContext, currentDepth, targetPosition, worldServer, blockChangeFlag, oldState, newState);
            phaseState.postBlockTransactionApplication(original.blockChange, eventTransaction, phaseContext);

            if (blockChangeFlag.isNotifyClients()) { // Always try to notify clients of the change.
                worldServer.notifyBlockUpdate(targetPosition, oldState, newState, blockChangeFlag.getRawFlag());
            }

            TrackingUtil.performNeighborAndClientNotifications(phaseContext, currentDepth, original, newBlockSnapshot,
                ((IMixinWorldServer) worldServer), targetPosition, oldState, newState, blockChangeFlag);
            ((IMixinWorldServer) worldServer).setProxyAccess(null);
        }
    }

    static final class NeighborNotification extends BlockTransaction {
        final IMixinWorldServer worldServer;
        final IBlockState source;
        final BlockPos notifyPos;
        final Block sourceBlock;
        final BlockPos sourcePos;

        NeighborNotification(int transactionIndex, int snapshotIndex, IMixinWorldServer worldServer, IBlockState source, BlockPos notifyPos, Block sourceBlock,
            BlockPos sourcePos) {
            super(transactionIndex, snapshotIndex);
            this.worldServer = worldServer;
            this.source = source;
            this.notifyPos = notifyPos;
            this.sourceBlock = sourceBlock;
            this.sourcePos = sourcePos;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                .add("worldServer", ((World) this.worldServer).getProperties().getWorldName())
                .add("source", this.source)
                .add("notifyPos", this.notifyPos)
                .add("sourceBlock", this.sourceBlock)
                .add("sourcePos", this.sourcePos)
                .toString();
        }

        @Override
        void cancel(WorldServer worldServer, BlockPos blockPos) {
            // We don't do anything, we just ignore the neighbor notification at this point.
        }

        @Override
        void process(Transaction<BlockSnapshot> eventTransaction, IPhaseState phaseState, PhaseContext<?> phaseContext,
            Map<BlockPos, IBlockState> processingBlocks, int currentDepth) {
            // Otherwise, we have a neighbor notification to process.
            final IMixinWorldServer worldServer = this.worldServer;
            final BlockPos notifyPos = this.notifyPos;
            final Block sourceBlock = this.sourceBlock;
            final BlockPos sourcePos = this.sourcePos;
            PhaseTracker.getInstance().performNeighborNotificationOnTarget(worldServer, notifyPos, sourceBlock, sourcePos, this.source);
        }
    }
}
