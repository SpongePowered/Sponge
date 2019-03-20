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
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.world.World;
import org.spongepowered.common.block.BlockUtil;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.interfaces.block.tile.IMixinTileEntity;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.world.SpongeBlockChangeFlag;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nullable;

@SuppressWarnings("rawtypes")
public abstract class BlockTransaction {

    final int transactionIndex;
    final int snapshotIndex;
    boolean isCancelled = false;
    boolean appliedPreChange = false;
    final BlockPos affectedPosition;
    @Nullable Map<BlockPos, TileEntity> tilesAtTransaction;
    @Nullable Map<BlockPos, IBlockState> blocksNotAffected;
    @Nullable BlockTransaction previous;
    @Nullable BlockTransaction next;

    BlockTransaction(int i, int snapshotIndex, BlockPos affectedPosition) {
        this.transactionIndex = i;
        this.snapshotIndex = snapshotIndex;
        this.affectedPosition = affectedPosition;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .toString();
    }

    abstract void cancel(WorldServer worldServer, BlockPos blockPos);

    abstract void process(Transaction<BlockSnapshot> eventTransaction, IPhaseState phaseState, PhaseContext<?> phaseContext,
        int currentDepth);

    void provideExistingBlockState(BlockTransaction prevChange, IBlockState newState) {
        if (prevChange.blocksNotAffected == null) {
            prevChange.blocksNotAffected = new LinkedHashMap<>();
        }
        if (this.appliedPreChange) {
            return;
        }
        final IBlockState iBlockState = prevChange.blocksNotAffected.putIfAbsent(this.affectedPosition, newState);
        if (newState == iBlockState) {
            this.appliedPreChange = true;
        }
    }

    public void enqueueChanges(SpongeProxyBlockAccess proxyBlockAccess, MultiBlockCaptureSupplier supplier) {
        supplier.getProxyOrCreate(proxyBlockAccess.getWorld());
        supplier.queuePreviousStates(this);
    }

    @Nullable
    public SpongeProxyBlockAccess.Proxy getProxy(IMixinWorldServer mixinWorldServer) {
        return null;
    }

    public void provideUnchangedStates(BlockTransaction prevChange) { }


    @SuppressWarnings("rawtypes")
    public static final class AddTileEntity extends BlockTransaction {

        final TileEntity added;
        final SpongeBlockSnapshot addedSnapshot;
        final IBlockState newState;

        AddTileEntity(int i, int snapshotIndex, TileEntity added, SpongeBlockSnapshot attachedSnapshot, IBlockState newState) {
            super(i, snapshotIndex, attachedSnapshot.getBlockPos());

            this.added = added;
            this.addedSnapshot = attachedSnapshot;
            this.newState = newState;
        }

        @Override
        void cancel(WorldServer worldServer, BlockPos blockPos) {

        }

        @Override
        void process(Transaction<BlockSnapshot> eventTransaction, IPhaseState phaseState, PhaseContext<?> phaseContext,
            int currentDepth) {
            final WorldServer worldServer = this.addedSnapshot.getWorldServer();

            final SpongeProxyBlockAccess proxyAccess = ((IMixinWorldServer) worldServer).getProxyAccess();
            final BlockPos targetPos = this.addedSnapshot.getBlockPos();
            proxyAccess.proceed(targetPos, this.newState);
            proxyAccess.proceedWithAdd(targetPos, this.added, this.newState);
            ((IMixinTileEntity) this.added).setCaptured(false);
        }

        @Override
        public void provideUnchangedStates(BlockTransaction prevChange) {
            if (this.appliedPreChange) {
                return;
            }
            if (prevChange.tilesAtTransaction == null) {
                prevChange.tilesAtTransaction = new LinkedHashMap<>();
            }
            if (!prevChange.tilesAtTransaction.containsKey(this.affectedPosition)) {
                // We want to have a null tile entity, because the tile is going to be added to the proxy
                prevChange.tilesAtTransaction.put(this.affectedPosition, this.added);
                provideExistingBlockState(prevChange, this.newState);
                this.appliedPreChange = true;
            }
        }

        @Override
        public void enqueueChanges(SpongeProxyBlockAccess proxyBlockAccess, MultiBlockCaptureSupplier supplier) {
            super.enqueueChanges(proxyBlockAccess, supplier);
            proxyBlockAccess.queueTileAddition(this.addedSnapshot.getBlockPos(), this.added);
        }

        @Nullable
        @Override
        public SpongeProxyBlockAccess.Proxy getProxy(IMixinWorldServer mixinWorldServer) {
            final SpongeProxyBlockAccess proxyAccess = mixinWorldServer.getProxyAccess();
            return proxyAccess.pushProxy();
        }
    }

    @SuppressWarnings("rawtypes")
    public static final class RemoveTileEntity extends BlockTransaction {

        final TileEntity removed;
        final SpongeBlockSnapshot tileSnapshot;
        final IBlockState newState;

        RemoveTileEntity(int i, int snapshotIndex, TileEntity removed, SpongeBlockSnapshot attachedSnapshot, IBlockState newState) {
            super(i, snapshotIndex, attachedSnapshot.getBlockPos());
            this.removed = removed;
            this.tileSnapshot = attachedSnapshot;
            this.newState = newState;
        }

        @Override
        void cancel(WorldServer worldServer, BlockPos blockPos) {

        }

        @Override
        void process(Transaction<BlockSnapshot> eventTransaction, IPhaseState phaseState, PhaseContext<?> phaseContext,
            int currentDepth) {
            final BlockPos targetPosition = this.tileSnapshot.getBlockPos();
            final WorldServer worldServer = this.tileSnapshot.getWorldServer();
            final SpongeProxyBlockAccess proxyAccess = ((IMixinWorldServer) worldServer).getProxyAccess();
            ((IMixinTileEntity) this.removed).setCaptured(false); // Disable the capture logic in other places.
            proxyAccess.proceed(targetPosition, this.newState);// Set the state back in, mimicing the chunk
            proxyAccess.proceedWithRemoval(targetPosition, this.removed);
            // Reset captured state since we want it to be removed
            worldServer.updateComparatorOutputLevel(targetPosition, this.newState.getBlock());
        }

        @Override
        public void provideUnchangedStates(BlockTransaction prevChange) {
            if (this.appliedPreChange) {
                return;
            }
            if (prevChange.tilesAtTransaction == null) {
                prevChange.tilesAtTransaction = new LinkedHashMap<>();
            }
            if (!prevChange.tilesAtTransaction.containsKey(this.affectedPosition)) {
                provideExistingBlockState(prevChange, this.newState);
                prevChange.tilesAtTransaction.put(this.affectedPosition, this.removed);
                this.appliedPreChange = true;
            }
        }

        @Override
        public void enqueueChanges(SpongeProxyBlockAccess proxyBlockAccess, MultiBlockCaptureSupplier supplier) {
            super.enqueueChanges(proxyBlockAccess, supplier);
            proxyBlockAccess.queueRemoval(this.removed);
        }

        @Nullable
        @Override
        public SpongeProxyBlockAccess.Proxy getProxy(IMixinWorldServer mixinWorldServer) {
            return mixinWorldServer.getProxyAccess().pushProxy();
        }
    }

    @SuppressWarnings("rawtypes")
    public static final class ReplaceTileEntity extends BlockTransaction {

        final TileEntity added;
        final TileEntity removed;
        final SpongeBlockSnapshot removedSnapshot;

        ReplaceTileEntity(int i, int snapshotIndex, TileEntity added, TileEntity removed, SpongeBlockSnapshot attachedSnapshot) {
            super(i, snapshotIndex, attachedSnapshot.getBlockPos());
            this.added = added;
            this.removed = removed;
            this.removedSnapshot = attachedSnapshot;
        }

        @Override
        void cancel(WorldServer worldServer, BlockPos blockPos) {

        }

        @Override
        void process(Transaction<BlockSnapshot> eventTransaction, IPhaseState phaseState, PhaseContext<?> phaseContext,
            int currentDepth) {
            final IMixinWorldServer mixinWorldServer = (IMixinWorldServer) this.added.getWorld();
            final BlockPos position = this.added.getPos();
            final SpongeProxyBlockAccess proxyAccess = mixinWorldServer.getProxyAccess();
            ((IMixinTileEntity) this.removed).setCaptured(false);
            proxyAccess.proceedWithRemoval(position, this.removed);
            ((IMixinTileEntity) this.added).setCaptured(false);
            proxyAccess.proceedWithAdd(position, this.added, (IBlockState) this.removedSnapshot.getState());
        }

        @Override
        public void provideUnchangedStates(BlockTransaction prevChange) {
            if (this.appliedPreChange) {
                return;
            }
            provideExistingBlockState(prevChange, (IBlockState) this.removedSnapshot.getState());
            if (prevChange.tilesAtTransaction == null) {
                prevChange.tilesAtTransaction = new LinkedHashMap<>();
            }
            // Basically, if the position is already populated with a tile entity, don't
            // override the change, because that'd be contaminating with a new change.
            if (!prevChange.tilesAtTransaction.containsKey(this.affectedPosition)) {
                prevChange.tilesAtTransaction.put(this.affectedPosition, this.removed);
                this.appliedPreChange = true;
            }

        }

        @Override
        public void enqueueChanges(SpongeProxyBlockAccess proxyBlockAccess, MultiBlockCaptureSupplier supplier) {
            super.enqueueChanges(proxyBlockAccess, supplier);
            proxyBlockAccess.queueReplacement(this.added, this.removed);
        }

        @Nullable
        @Override
        public SpongeProxyBlockAccess.Proxy getProxy(IMixinWorldServer mixinWorldServer) {
            return mixinWorldServer.getProxyAccess().pushProxy();
        }
    }

    @SuppressWarnings("rawtypes")
    public static final class ChangeBlock extends BlockTransaction {

        final SpongeBlockSnapshot original;
        final IBlockState newState;
        final SpongeBlockChangeFlag blockChangeFlag;
        @Nullable public TileEntity queuedRemoval;
        @Nullable public TileEntity queueTileSet;
        boolean ignoreBreakBlockLogic = false;
        public boolean queueBreak = false;
        public boolean queueOnAdd = false;

        ChangeBlock(int i, int snapshotIndex, SpongeBlockSnapshot attachedSnapshot, IBlockState newState, SpongeBlockChangeFlag blockChange) {
            super(i, snapshotIndex, attachedSnapshot.getBlockPos());
            this.original = attachedSnapshot;
            this.newState = newState;
            this.blockChangeFlag = blockChange;
            if (this.newState.getBlock() != BlockUtil.toNative(this.original).getBlock()) {
                this.queueBreak = true;
            }
        }

        @Override
        void cancel(WorldServer worldServer, BlockPos blockPos) {

        }

        @Override
        public void enqueueChanges(SpongeProxyBlockAccess proxyBlockAccess, MultiBlockCaptureSupplier supplier) {
            super.enqueueChanges(proxyBlockAccess, supplier);
            BlockPos target = this.original.getBlockPos();
            proxyBlockAccess.proceed(target, this.newState);
            if (this.queuedRemoval != null) {
                if (this.queueTileSet != null) {
                    // Make sure the new tile entity has the correct position
                    this.queueTileSet.setPos(target);
                    proxyBlockAccess.queueReplacement(this.queueTileSet, this.queuedRemoval);
                } else {
                    proxyBlockAccess.queueRemoval(this.queuedRemoval);
                }
            } else if (this.queueTileSet != null) {
                proxyBlockAccess.queueTileAddition(target, this.queueTileSet);
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        void process(Transaction<BlockSnapshot> eventTransaction, IPhaseState phaseState, PhaseContext<?> phaseContext,
            int currentDepth) {
            final BlockPos targetPosition = this.original.getBlockPos();
            final WorldServer worldServer = this.original.getWorldServer();
            final SpongeBlockSnapshot newBlockSnapshot = (SpongeBlockSnapshot) eventTransaction.getFinal();

            TrackingUtil.performBlockEntitySpawns(phaseState, phaseContext, this.original, targetPosition);
            SpongeHooks.logBlockAction(worldServer, this.original.blockChange, eventTransaction);
            final IBlockState oldState = (IBlockState) this.original.getState();
            // Any requests to the world need to propogate to having the "changed" block, before
            // the block potentially changes from future changes.
            SpongeProxyBlockAccess proxyAccess = ((IMixinWorldServer) worldServer).getProxyAccess();
            proxyAccess.proceed(targetPosition, this.newState); // Set the block state before we start working on invalidating the tile entity
            // The proxy sets up the various objects needed to properly remove the tile entity, including but not withtanding
            // any tile entities that are already replaced at the position
            if (this.queuedRemoval != null) {
                proxyAccess.proceedWithRemoval(targetPosition, this.queuedRemoval);
            }

            // We can proceed to calling the break block logic since the new state has been "proxied" onto the world
            PhaseContext<?> currentContext = PhaseTracker.getInstance().getCurrentContext();

            if (!this.ignoreBreakBlockLogic && this.queueBreak) {
                BlockSnapshot currentNeighborSource = currentContext.neighborNotificationSource;
                currentContext.neighborNotificationSource = this.original;
                oldState.getBlock().breakBlock(worldServer, targetPosition, oldState);
                currentContext.neighborNotificationSource = currentNeighborSource;
            }


            // We call onBlockAdded here for blocks without a TileEntity.
            // MixinChunk#setBlockState will call onBlockAdded for blocks
            // with a TileEntity or when capturing is not being done.
            if (this.queueOnAdd) {
                this.newState.getBlock().onBlockAdded(worldServer, targetPosition, this.newState);
                phaseState.performOnBlockAddedSpawns(phaseContext, currentDepth + 1);
            }
            if (this.queueTileSet != null) {
                proxyAccess.proceedWithAdd(targetPosition, this.queueTileSet, this.newState);
            }
            phaseState.postBlockTransactionApplication(this.original.blockChange, eventTransaction, phaseContext);
            ((IPhaseState) currentContext.state).postProcessSpecificBlockChange(currentContext, this, currentDepth + 1);

            if (this.blockChangeFlag.isNotifyClients()) { // Always try to notify clients of the change.
                worldServer.notifyBlockUpdate(targetPosition, oldState, this.newState, this.blockChangeFlag.getRawFlag());
            }

            TrackingUtil.performNeighborAndClientNotifications(phaseContext, currentDepth, this.original, newBlockSnapshot,
                ((IMixinWorldServer) worldServer), targetPosition, oldState, this.newState, this.blockChangeFlag);
        }

        @Override
        public void provideUnchangedStates(BlockTransaction prevChange) {
            if (this.appliedPreChange) {
                return;
            }
            provideExistingBlockState(prevChange, (IBlockState) this.original.getState());
            if (prevChange.tilesAtTransaction == null) {
                prevChange.tilesAtTransaction = new LinkedHashMap<>();
            }
            // Basically, we don't want to override what position potentially had before.
            // if there was already an entry, some other state before this one has already populated it.
            if (!prevChange.tilesAtTransaction.containsKey(this.affectedPosition)) {
                prevChange.tilesAtTransaction.put(this.affectedPosition, this.queuedRemoval);
                this.appliedPreChange = true;
            }
        }

        @Nullable
        @Override
        public SpongeProxyBlockAccess.Proxy getProxy(IMixinWorldServer mixinWorldServer) {
            return mixinWorldServer.getProxyAccess().pushProxy();
        }
    }

    static final class NeighborNotification extends BlockTransaction {
        final IMixinWorldServer worldServer;
        final IBlockState notifyState;
        final BlockPos notifyPos;
        final Block sourceBlock;
        final BlockPos sourcePos;
        private final IBlockState actualSourceState;

        NeighborNotification(int transactionIndex, int snapshotIndex, IMixinWorldServer worldServer, IBlockState notifyState, BlockPos notifyPos,
            Block sourceBlock, BlockPos sourcePos, IBlockState sourceState) {
            super(transactionIndex, snapshotIndex, sourcePos);
            this.worldServer = worldServer;
            this.notifyState = notifyState;
            this.notifyPos = notifyPos;
            this.sourceBlock = sourceBlock;
            this.sourcePos = sourcePos;
            this.actualSourceState = sourceState;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                .add("worldServer", ((World) this.worldServer).getProperties().getWorldName())
                .add("notifyState", this.notifyState)
                .add("notifyPos", this.notifyPos)
                .add("sourceBlock", this.sourceBlock)
                .add("sourcePos", this.sourcePos)
                .add("actualSourceState", this.actualSourceState)
                .toString();
        }

        @Override
        public void provideUnchangedStates(BlockTransaction prevChange) {
            if (this.appliedPreChange) {
                return;
            }
            provideExistingBlockState(prevChange, this.actualSourceState);
        }

        @Override
        public void enqueueChanges(SpongeProxyBlockAccess proxyBlockAccess, MultiBlockCaptureSupplier supplier) {
            super.enqueueChanges(proxyBlockAccess, supplier);
        }

        @Override
        void cancel(WorldServer worldServer, BlockPos blockPos) {
            // We don't do anything, we just ignore the neighbor notification at this point.
        }

        @Override
        void process(Transaction<BlockSnapshot> eventTransaction, IPhaseState phaseState, PhaseContext<?> phaseContext,
            int currentDepth) {
            // Otherwise, we have a neighbor notification to process.
            final IMixinWorldServer worldServer = this.worldServer;
            final BlockPos notifyPos = this.notifyPos;
            final Block sourceBlock = this.sourceBlock;
            final BlockPos sourcePos = this.sourcePos;
            worldServer.getProxyAccess().proceed(notifyPos, this.actualSourceState);
            PhaseTracker.getInstance().performNeighborNotificationOnTarget(worldServer, this.notifyState, notifyPos, sourceBlock, sourcePos);
        }
    }
}
