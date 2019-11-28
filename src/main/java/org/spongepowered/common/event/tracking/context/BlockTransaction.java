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
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.bridge.tileentity.TileEntityBridge;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.context.BlockTransaction.TransactionContext;
import org.spongepowered.common.event.tracking.context.BlockTransaction.TransactionProcessState;
import org.spongepowered.common.mixin.core.world.WorldServerMixin;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.world.SpongeBlockChangeFlag;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

@SuppressWarnings("rawtypes")
public abstract class BlockTransaction {

    final int transactionIndex;
    final int snapshotIndex;
    boolean isCancelled = false;
    boolean appliedPreChange;
    final BlockPos affectedPosition;
    final BlockState originalState;
    @Nullable Map<BlockPos, TileEntity> tilesAtTransaction;
    @Nullable Map<BlockPos, BlockState> blocksNotAffected;
    @Nullable BlockTransaction previous;
    @Nullable BlockTransaction next;

    BlockTransaction(final int i, final int snapshotIndex, final BlockPos affectedPosition, final BlockState originalState) {
        this.transactionIndex = i;
        this.snapshotIndex = snapshotIndex;
        this.affectedPosition = affectedPosition;
        this.originalState = originalState;
        this.provideExistingBlockState(this, originalState);
        this.appliedPreChange = false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .toString();
    }

    abstract Optional<WorldServerBridge> getWorldServer();

    abstract void cancel(ServerWorld worldServer, BlockPos blockPos, SpongeProxyBlockAccess proxyBlockAccess);

    abstract void process(Transaction<BlockSnapshot> eventTransaction, IPhaseState phaseState, PhaseContext<?> phaseContext,
        int currentDepth);

    boolean applyTileAtTransaction(final BlockPos affectedPosition, final TileEntity queuedRemoval) {
        if (this.tilesAtTransaction == null) {
            this.tilesAtTransaction = new LinkedHashMap<>();
        }
        if (!this.tilesAtTransaction.containsKey(affectedPosition)) {
            this.tilesAtTransaction.put(affectedPosition, queuedRemoval);
            return true;
        }
        return false;
    }

    void provideExistingBlockState(final BlockTransaction prevChange, final BlockState newState) {
        if (newState == null) {
            return;
        }
        if (prevChange.affectedPosition.equals(this.affectedPosition)) {
            return;
        }
        if (prevChange.blocksNotAffected == null) {
            prevChange.blocksNotAffected = new LinkedHashMap<>();
        }
        final BlockState iBlockState = prevChange.blocksNotAffected.putIfAbsent(this.affectedPosition, newState);
        if (iBlockState == null) {
            this.appliedPreChange = true;
        }
    }

    public void enqueueChanges(final SpongeProxyBlockAccess proxyBlockAccess, final MultiBlockCaptureSupplier supplier) {
        supplier.getProxyOrCreate(proxyBlockAccess.getWorld());
        supplier.queuePreviousStates(this);
    }

    @Nullable
    public SpongeProxyBlockAccess.Proxy getProxy(final WorldServerBridge mixinWorldServer) {
        return null;
    }

    public void provideUnchangedStates(final BlockTransaction prevChange) { }

    public abstract void addToPrinter(PrettyPrinter printer);

    public void postProcessBlocksAffected(final SpongeProxyBlockAccess proxyAccess) {
    }

    public boolean equalsSnapshot(final SpongeBlockSnapshot snapshot) {
        return false;
    }

    public boolean acceptChunkChange(final BlockPos pos, final BlockState newState) {
        return this.blocksNotAffected != null && !this.blocksNotAffected.isEmpty() && !this.affectedPosition.equals(pos);
    }


    @SuppressWarnings("rawtypes")
    public static final class AddTileEntity extends BlockTransaction {

        final TileEntity added;
        final SpongeBlockSnapshot addedSnapshot;

        AddTileEntity(final int i, final int snapshotIndex, final TileEntity added, final SpongeBlockSnapshot attachedSnapshot) {
            super(i, snapshotIndex, attachedSnapshot.getBlockPos(), null);
            this.added = added;
            this.addedSnapshot = attachedSnapshot;
        }

        @Override
        void cancel(final ServerWorld worldServer, final BlockPos blockPos, final SpongeProxyBlockAccess proxyBlockAccess) {
            proxyBlockAccess.unQueueTileAddition(this.added.func_174877_v(), this.added);
        }

        @Override
        void process(final Transaction<BlockSnapshot> eventTransaction, final IPhaseState phaseState, final PhaseContext<?> phaseContext,
            final int currentDepth) {
            final Optional<ServerWorld> maybeWorld = this.addedSnapshot.getWorldServer();
            if (!maybeWorld.isPresent()) {
                // Emit a log warning about a missing world
                final String transactionForLogging = MoreObjects.toStringHelper("Tile Added")
                    .add("World", this.addedSnapshot.getWorldUniqueId())
                    .add("Position", this.addedSnapshot.getBlockPos())
                    .add("Original State", this.addedSnapshot.getState())
                    .add("Tile Entity", this.added)
                    .toString();
                SpongeImpl.getLogger().warn("Unloaded/Missing World for a captured Tile Entity adding! Skipping change: " + transactionForLogging);
                //noinspection ConstantConditions
                this.added.func_145834_a(null);
                this.added.func_145843_s();
                return;
            }
            final ServerWorld worldServer = maybeWorld.get();
            final SpongeProxyBlockAccess proxyAccess = ((WorldServerBridge) worldServer).bridge$getProxyAccess();
            final BlockPos targetPos = this.addedSnapshot.getBlockPos();
            proxyAccess.proceedWithAdd(targetPos, this.added);
            ((TileEntityBridge) this.added).bridge$setCaptured(false);
        }

        @Override
        public void provideUnchangedStates(final BlockTransaction prevChange) {
            if (prevChange.applyTileAtTransaction(this.affectedPosition, null)) {
                this.appliedPreChange = true;
            }
        }

        @Override
        public void enqueueChanges(final SpongeProxyBlockAccess proxyBlockAccess, final MultiBlockCaptureSupplier supplier) {
            super.enqueueChanges(proxyBlockAccess, supplier);
            proxyBlockAccess.queueTileAddition(this.addedSnapshot.getBlockPos(), this.added);
        }

        @Nullable
        @Override
        public SpongeProxyBlockAccess.Proxy getProxy(final WorldServerBridge mixinWorldServer) {
            final SpongeProxyBlockAccess proxyAccess = mixinWorldServer.bridge$getProxyAccess();
            return proxyAccess.pushProxy();
        }

        @SuppressWarnings("unchecked")
        @Override
        Optional<WorldServerBridge> getWorldServer() {
            return (Optional<WorldServerBridge>) (Optional<?>) this.addedSnapshot.getWorldServer();
        }

        @Override
        public void addToPrinter(final PrettyPrinter printer) {
            printer.add("AddTileEntity")
                .addWrapped(120, " %s : %s", this.affectedPosition, ((TileEntityBridge) this.added).bridge$getPrettyPrinterString());
        }
    }

    @SuppressWarnings("rawtypes")
    public static final class RemoveTileEntity extends BlockTransaction {

        final TileEntity removed;
        final SpongeBlockSnapshot tileSnapshot;

        RemoveTileEntity(final int i, final int snapshotIndex, final TileEntity removed, final SpongeBlockSnapshot attachedSnapshot) {
            super(i, snapshotIndex, attachedSnapshot.getBlockPos(), null);
            this.removed = removed;
            this.tileSnapshot = attachedSnapshot;
            this.applyTileAtTransaction(this.affectedPosition, this.removed);
            this.appliedPreChange = false;
        }

        @Override
        void cancel(final ServerWorld worldServer, final BlockPos blockPos, final SpongeProxyBlockAccess proxyBlockAccess) {
            proxyBlockAccess.unmarkRemoval(this.removed.func_174877_v(), this.removed);

        }

        @Override
        void process(final Transaction<BlockSnapshot> eventTransaction, final IPhaseState phaseState, final PhaseContext<?> phaseContext,
            final int currentDepth) {
            final BlockPos targetPosition = this.tileSnapshot.getBlockPos();
            final Optional<ServerWorld> maybeWorld = this.tileSnapshot.getWorldServer();
            if (!maybeWorld.isPresent()) {
                // Emit a log warning about a missing world
                final String transactionForLogging = MoreObjects.toStringHelper("Tile Removed")
                    .add("World", this.tileSnapshot.getWorldUniqueId())
                    .add("Position", this.tileSnapshot.getBlockPos())
                    .add("Original State", this.tileSnapshot.getState())
                    .add("Tile Entity", this.removed)
                    .toString();
                SpongeImpl.getLogger().warn("Unloaded/Missing World for a captured Tile Entity removal! Skipping change: " + transactionForLogging);
                //noinspection ConstantConditions
                this.removed.func_145834_a(null);
                this.removed.func_145843_s();
                return;
            }
            final ServerWorld worldServer = maybeWorld.get();
            final SpongeProxyBlockAccess proxyAccess = ((WorldServerBridge) worldServer).bridge$getProxyAccess();
            ((TileEntityBridge) this.removed).bridge$setCaptured(false); // Disable the capture logic in other places.
            proxyAccess.proceedWithRemoval(targetPosition, this.removed);
            // Reset captured state since we want it to be removed
            worldServer.func_175666_e(targetPosition, worldServer.func_180495_p(targetPosition).func_177230_c());
        }

        @Override
        public void addToPrinter(final PrettyPrinter printer) {
            printer.add("RemoveTileEntity")
                .add(" %s : %s", this.affectedPosition, ((TileEntityBridge) this.removed).bridge$getPrettyPrinterString())
                .add(" %s : %s", this.affectedPosition, this.originalState)
            ;
        }

        @Override
        public void provideUnchangedStates(final BlockTransaction prevChange) {
            if (prevChange.applyTileAtTransaction(this.affectedPosition, this.removed)) {
                this.appliedPreChange = true;
            }
        }

        @Override
        public void enqueueChanges(final SpongeProxyBlockAccess proxyBlockAccess, final MultiBlockCaptureSupplier supplier) {
            super.enqueueChanges(proxyBlockAccess, supplier);
            proxyBlockAccess.queueRemoval(this.removed);
        }

        @Nullable
        @Override
        public SpongeProxyBlockAccess.Proxy getProxy(final WorldServerBridge mixinWorldServer) {
            return mixinWorldServer.bridge$getProxyAccess().pushProxy();
        }

        @SuppressWarnings("unchecked")
        @Override
        Optional<WorldServerBridge> getWorldServer() {
            return (Optional<WorldServerBridge>) (Optional<?>) this.tileSnapshot.getWorldServer();
        }

    }

    @SuppressWarnings("rawtypes")
    public static final class ReplaceTileEntity extends BlockTransaction {

        final TileEntity added;
        final TileEntity removed;
        final SpongeBlockSnapshot removedSnapshot;

        ReplaceTileEntity(final int i, final int snapshotIndex, final TileEntity added, final TileEntity removed, final SpongeBlockSnapshot attachedSnapshot) {
            super(i, snapshotIndex, attachedSnapshot.getBlockPos(), null);
            this.added = added;
            this.removed = removed;
            this.removedSnapshot = attachedSnapshot;
            this.applyTileAtTransaction(this.affectedPosition, this.removed);
            this.appliedPreChange = false;
        }

        @Override
        void cancel(final ServerWorld worldServer, final BlockPos blockPos,
            final SpongeProxyBlockAccess proxyBlockAccess) {
            proxyBlockAccess.unQueueTileAddition(this.removed.func_174877_v(), this.added);
            proxyBlockAccess.unmarkRemoval(this.removed.func_174877_v(), this.removed);

        }

        @Override
        void process(final Transaction<BlockSnapshot> eventTransaction, final IPhaseState phaseState, final PhaseContext<?> phaseContext,
            final int currentDepth) {
            final WorldServerBridge mixinWorldServer = (WorldServerBridge) this.added.func_145831_w();
            final BlockPos position = this.added.func_174877_v();
            final SpongeProxyBlockAccess proxyAccess = mixinWorldServer.bridge$getProxyAccess();
            ((TileEntityBridge) this.removed).bridge$setCaptured(false);
            proxyAccess.proceedWithRemoval(position, this.removed);
            ((TileEntityBridge) this.added).bridge$setCaptured(false);
            proxyAccess.proceedWithAdd(position, this.added);
        }

        @Override
        public void provideUnchangedStates(final BlockTransaction prevChange) {
            if (prevChange.applyTileAtTransaction(this.affectedPosition, this.removed)) {
                this.appliedPreChange = true;
            }
        }

        @Override
        public void enqueueChanges(final SpongeProxyBlockAccess proxyBlockAccess, final MultiBlockCaptureSupplier supplier) {
            super.enqueueChanges(proxyBlockAccess, supplier);
            proxyBlockAccess.queueReplacement(this.added, this.removed);
        }

        @Nullable
        @Override
        public SpongeProxyBlockAccess.Proxy getProxy(final WorldServerBridge mixinWorldServer) {
            return mixinWorldServer.bridge$getProxyAccess().pushProxy();
        }

        @SuppressWarnings("unchecked")
        @Override
        Optional<WorldServerBridge> getWorldServer() {
            return (Optional<WorldServerBridge>) (Optional<?>) this.removedSnapshot.getWorldServer();
        }

        @Override
        public void addToPrinter(final PrettyPrinter printer) {
            printer.add("ReplaceTileEntity")
                .add(" %s : %s", "Position", this.affectedPosition)
                .add(" %s : %s", "Added", this.added)
                .add(" %s : %s", "Removed", this.removed)
            ;
        }
    }

    @SuppressWarnings("rawtypes")
    public static final class ChangeBlock extends BlockTransaction {

        final SpongeBlockSnapshot original;
        final BlockState newState;
        final SpongeBlockChangeFlag blockChangeFlag;
        @Nullable public TileEntity queuedRemoval;
        @Nullable public TileEntity queueTileSet;
        boolean ignoreBreakBlockLogic = false;
        public boolean queueBreak = false;
        public boolean queueOnAdd = false;

        ChangeBlock(
            final int i, final int snapshotIndex, final SpongeBlockSnapshot attachedSnapshot, final BlockState newState, final SpongeBlockChangeFlag blockChange) {
            super(i, snapshotIndex, attachedSnapshot.getBlockPos(), (BlockState) attachedSnapshot.getState());
            this.original = attachedSnapshot;
            this.newState = newState;
            this.blockChangeFlag = blockChange;
        }

        @Override
        void cancel(final ServerWorld worldServer, final BlockPos blockPos, final SpongeProxyBlockAccess proxyBlockAccess) {
            // Literally don't do anything, because the tile entity is being restored by the snapshot.
        }

        @Override
        public void enqueueChanges(final SpongeProxyBlockAccess proxyBlockAccess, final MultiBlockCaptureSupplier supplier) {
            super.enqueueChanges(proxyBlockAccess, supplier);
            final BlockPos target = this.original.getBlockPos();
            proxyBlockAccess.proceed(target, this.newState, false);
            if (this.queuedRemoval != null) {
                if (this.queueTileSet != null) {
                    // Make sure the new tile entity has the correct position
                    this.queueTileSet.func_174878_a(target);
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
        void process(final Transaction<BlockSnapshot> eventTransaction, final IPhaseState phaseState, final PhaseContext<?> phaseContext,
            final int currentDepth) {
            final BlockPos targetPosition = this.original.getBlockPos();
            final Optional<ServerWorld> maybeWorld = this.original.getWorldServer();
            if (!maybeWorld.isPresent()) {
                // Emit a log warning about a missing world
                final String transactionForLogging = MoreObjects.toStringHelper("Transaction")
                    .add("World", this.original.getWorldUniqueId())
                    .add("Position", this.original.getBlockPos())
                    .add("Original State", this.original.getState())
                    .add("Changed State", this.newState)
                    .toString();
                SpongeImpl.getLogger().warn("Unloaded/Missing World for a captured block change! Skipping change: " + transactionForLogging);
                return;
            }
            final ServerWorld worldServer = maybeWorld.get();
            final SpongeBlockSnapshot newBlockSnapshot = (SpongeBlockSnapshot) eventTransaction.getFinal();

            TrackingUtil.performBlockEntitySpawns(phaseState, phaseContext, this.original, targetPosition);
            SpongeHooks.logBlockAction(worldServer, this.original.blockChange, eventTransaction);
            final BlockState oldState = (BlockState) this.original.getState();
            // Any requests to the world need to propogate to having the "changed" block, before
            // the block potentially changes from future changes.
            final SpongeProxyBlockAccess proxyAccess = ((WorldServerBridge) worldServer).bridge$getProxyAccess();

            // We can proceed to calling the break block logic since the new state has been "proxied" onto the world
            final PhaseContext<?> currentContext = PhaseTracker.getInstance().getCurrentContext();
            // Use the try to literally bypass any events, block notifications, neighbor switching, etc.
            // We can get away with making this phase switch because any tile entity accesses will
            // end up being ignored
            try (final TransactionContext context = new TransactionContext()) {
                context.buildAndSwitch();
                proxyAccess.proceed(targetPosition, this.newState, true); // Set the block state before we start working on invalidating the tile entity
            }

            // The proxy sets up the various objects needed to properly remove the tile entity, including but not withtanding
            // any tile entities that are already replaced at the position
            if (this.queuedRemoval != null) {
                proxyAccess.proceedWithRemoval(targetPosition, this.queuedRemoval);
            }

            // We call onBlockAdded here for blocks without a TileEntity.
            // ChunkMixin#bridge$setBlockState will call onBlockAdded for blocks
            // with a TileEntity or when capturing is not being done.
            if (this.queueOnAdd) {
                this.newState.func_177230_c().func_176213_c(worldServer, targetPosition, this.newState);
                phaseState.performOnBlockAddedSpawns(phaseContext, currentDepth + 1);
            }
            if (this.queueTileSet != null) {
                proxyAccess.proceedWithAdd(targetPosition, this.queueTileSet);
            }
            phaseState.postBlockTransactionApplication(this.original.blockChange, eventTransaction, phaseContext);
            ((IPhaseState) currentContext.state).postProcessSpecificBlockChange(currentContext, this, currentDepth + 1);

            if (this.blockChangeFlag.isNotifyClients()) { // Always try to notify clients of the change.
                worldServer.func_184138_a(targetPosition, oldState, this.newState, this.blockChangeFlag.getRawFlag());
            }

            TrackingUtil.performNeighborAndClientNotifications(phaseContext, currentDepth, newBlockSnapshot,
                ((WorldServerBridge) worldServer), targetPosition, this.newState, this.blockChangeFlag);
            // And perform any more additional spawns.
            TrackingUtil.performBlockEntitySpawns(phaseState, phaseContext, this.original, targetPosition);
        }

        @Override
        public void provideUnchangedStates(final BlockTransaction prevChange) {
            provideExistingBlockState(prevChange, (BlockState) this.original.getState());
            if (prevChange.applyTileAtTransaction(this.affectedPosition, this.queuedRemoval)) {
                this.appliedPreChange = true;
            }
        }

        @Nullable
        @Override
        public SpongeProxyBlockAccess.Proxy getProxy(final WorldServerBridge mixinWorldServer) {
            return mixinWorldServer.bridge$getProxyAccess().pushProxy();
        }

        @SuppressWarnings("unchecked")
        @Override
        Optional<WorldServerBridge> getWorldServer() {
            return (Optional<WorldServerBridge>) (Optional<?>) this.original.getWorldServer();
        }

        @Override
        public void addToPrinter(final PrettyPrinter printer) {
            printer.add("ChangeBlock")
                .add(" %s : %s", "Original Block", this.original)
                .add(" %s : %s", "New State", this.newState)
                .add(" %s : %s", "RemovedTile", this.queuedRemoval)
                .add(" %s : %s", "AddedTile", this.queueTileSet)
                .add(" %s : %s", "ChangeFlag", this.blockChangeFlag);
        }

        @Override
        public boolean equalsSnapshot(final SpongeBlockSnapshot snapshot) {
            return this.original.equals(snapshot);
        }
    }


    static final class NeighborNotification extends BlockTransaction {
        final WorldServerBridge worldServer;
        final BlockState notifyState;
        final BlockPos notifyPos;
        final Block sourceBlock;
        final BlockPos sourcePos;

        NeighborNotification(final int transactionIndex, final int snapshotIndex, final WorldServerBridge worldServer, final BlockState notifyState, final BlockPos notifyPos,
                             final Block sourceBlock, final BlockPos sourcePos, final BlockState sourceState) {
            super(transactionIndex, snapshotIndex, sourcePos, sourceState);
            this.worldServer = worldServer;
            this.notifyState = notifyState;
            this.notifyPos = notifyPos;
            this.sourceBlock = sourceBlock;
            this.sourcePos = sourcePos;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                .add("worldServer", ((World) this.worldServer).getProperties().getWorldName())
                .add("notifyState", this.notifyState)
                .add("notifyPos", this.notifyPos)
                .add("sourceBlock", this.sourceBlock)
                .add("sourcePos", this.sourcePos)
                .add("actualSourceState", this.originalState)
                .toString();
        }

        @Override
        public void provideUnchangedStates(final BlockTransaction prevChange) {
            provideExistingBlockState(prevChange, this.originalState);
        }

        @Override
        public void enqueueChanges(final SpongeProxyBlockAccess proxyBlockAccess, final MultiBlockCaptureSupplier supplier) {
            super.enqueueChanges(proxyBlockAccess, supplier);
        }

        @Override
        void cancel(final ServerWorld worldServer, final BlockPos blockPos,
            final SpongeProxyBlockAccess proxyBlockAccess) {
            // We don't do anything, we just ignore the neighbor notification at this point.
        }

        @Override
        void process(final Transaction<BlockSnapshot> eventTransaction, final IPhaseState phaseState, final PhaseContext<?> phaseContext,
            final int currentDepth) {
            // Otherwise, we have a neighbor notification to process.
            final WorldServerBridge worldServer = this.worldServer;
            final BlockPos notifyPos = this.notifyPos;
            final Block sourceBlock = this.sourceBlock;
            final BlockPos sourcePos = this.sourcePos;
            final SpongeProxyBlockAccess proxyAccess = worldServer.bridge$getProxyAccess();
            BlockState blockState = proxyAccess.func_180495_p(notifyPos);
            if (blockState == null) {
                blockState = ((ServerWorld) this.worldServer).func_180495_p(notifyPos);
            }
            final Chunk chunk = ((ServerWorld) this.worldServer).func_175726_f(sourcePos);
            final Block used = PhaseTracker.validateBlockForNeighborNotification((ServerWorld) this.worldServer, sourcePos, sourceBlock, notifyPos, chunk);

            PhaseTracker.getInstance().notifyBlockOfStateChange(worldServer, blockState, notifyPos, used, sourcePos);
        }

        @Override
        boolean applyTileAtTransaction(final BlockPos affectedPosition, final TileEntity queuedRemoval) {
            if (this.tilesAtTransaction == null) {
                this.tilesAtTransaction = new LinkedHashMap<>();
            }
            if (!this.tilesAtTransaction.containsKey(affectedPosition)) {
                this.tilesAtTransaction.put(affectedPosition, queuedRemoval);
                return true;
            }
            return false;
        }

        @Override
        public boolean acceptChunkChange(final BlockPos pos, final BlockState newState) {
            if (this.blocksNotAffected == null) {
                this.blocksNotAffected = new LinkedHashMap<>();
            }
            return true;
        }

        @Override
        public void addToPrinter(final PrettyPrinter printer) {
            printer.add("NeighborNotification")
                .add(" %s : %s, %s", "Source Pos", this.originalState, this.sourcePos)
                .add(" %s : %s, %s", "Notification", this.notifyState, this.notifyPos);
        }

        @Override
        Optional<WorldServerBridge> getWorldServer() {
            return Optional.of(this.worldServer);
        }
    }

    static final class TransactionProcessState implements IPhaseState<TransactionContext> {

        public static final TransactionProcessState TRANSACTION_PROCESS = new TransactionProcessState();

        private TransactionProcessState() {
        }

        @Override
        public TransactionContext createPhaseContext() {
            throw new IllegalStateException("Cannot create context");
        }

        @Override
        public void unwind(final TransactionContext phaseContext) {

        }

        @Override
        public boolean tracksOwnersAndNotifiers() {
            return false;
        }

        @Override
        public boolean isRestoring() {
            return true;
        }

        @Override
        public boolean doesBulkBlockCapture(final TransactionContext context) {
            return false;
        }

        @Override
        public boolean doesBlockEventTracking(final TransactionContext context) {
            return false;
        }

        @Override
        public boolean shouldCaptureBlockChangeOrSkip(
            final TransactionContext phaseContext, final BlockPos pos, final BlockState currentState, final BlockState newState,
            final BlockChangeFlag flags) {
            return false;
        }
    }

    static final class TransactionContext extends PhaseContext<TransactionContext> {

        protected TransactionContext() {
            super(TransactionProcessState.TRANSACTION_PROCESS);
        }
    }
}
