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
package org.spongepowered.common.event.tracking.context.transaction;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkSection;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.bridge.tileentity.TileEntityBridge;
import org.spongepowered.common.bridge.world.TrackedWorldBridge;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.context.transaction.effect.BlockAddedEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.CheckBlockPostPlacementIsSameEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.ChunkChangeCompleteEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.OldBlockOnReplaceEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.RefreshOldTileEntityOnChunkChangeEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.SetBlockToChunkSectionEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.UpdateChunkLightManagerEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.UpdateHeightMapEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.UpdateOrCreateNewTileEntityPostPlacementEffect;
import org.spongepowered.common.event.tracking.context.transaction.pipeline.ChunkPipeline;
import org.spongepowered.common.util.PrettyPrinter;
import org.spongepowered.common.world.SpongeBlockChangeFlag;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Consumer;


public abstract class BlockTransaction {

    final int transactionIndex;
    final int snapshotIndex;
    boolean isCancelled = false;
    boolean appliedPreChange;
    // State defintions
    final BlockPos affectedPosition;
    final BlockState originalState;
    @Nullable Map<BlockPos, TileEntity> tilesAtTransaction;
    @Nullable Map<BlockPos, BlockState> blocksNotAffected;

    // Children Defintions
    @Nullable LinkedList<ResultingTransactionBySideEffect> sideEffects;

    // LinkedList node defintions
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
        return new StringJoiner(", ", BlockTransaction.class.getSimpleName() + "[", "]")
            .add("affectedPosition=" + this.affectedPosition)
            .add("originalState=" + this.originalState)
            .toString();
    }

    public abstract void populateChunkEffects(
        TransactionalCaptureSupplier blockTransactor,
        ChunkPipeline.Builder builder, ChunkSection chunksection
    );

    public List<ResultingTransactionBySideEffect> getEffects() {
        return this.sideEffects == null ? Collections.emptyList() : this.sideEffects;
    }

    public final boolean hasChildTransactions() {
        return this.sideEffects != null && this.sideEffects.stream().anyMatch(effect -> effect.child != null);
    }

    public abstract Optional<Consumer<CauseStackManager.StackFrame>> getFrameMutator();

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

    public void provideUnchangedStates(final BlockTransaction prevChange) { }

    public abstract void addToPrinter(PrettyPrinter printer);

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
        public void populateChunkEffects(final TransactionalCaptureSupplier blockTransactor,
            final ChunkPipeline.Builder builder,
            final ChunkSection chunksection
        ) {

        }

        @Override
        public Optional<Consumer<CauseStackManager.StackFrame>> getFrameMutator() {
            return Optional.empty();
        }

        @Override
        public void provideUnchangedStates(final BlockTransaction prevChange) {
            if (prevChange.applyTileAtTransaction(this.affectedPosition, null)) {
                this.appliedPreChange = true;
            }
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
        public void populateChunkEffects(final TransactionalCaptureSupplier blockTransactor,
            final ChunkPipeline.Builder builder,
            final ChunkSection chunksection
        ) {

        }

        @Override
        public Optional<Consumer<CauseStackManager.StackFrame>> getFrameMutator() {
            return Optional.empty();
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
        public void populateChunkEffects(final TransactionalCaptureSupplier blockTransactor,
            final ChunkPipeline.Builder builder,
            final ChunkSection chunksection
        ) {

        }

        @Override
        public Optional<Consumer<CauseStackManager.StackFrame>> getFrameMutator() {
            return Optional.empty();
        }

        @Override
        public void provideUnchangedStates(final BlockTransaction prevChange) {
            if (prevChange.applyTileAtTransaction(this.affectedPosition, this.removed)) {
                this.appliedPreChange = true;
            }
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
        final int originalOpacity;
        final BlockState newState;
        final SpongeBlockChangeFlag blockChangeFlag;
        @Nullable public TileEntity queuedRemoval;
        @Nullable public TileEntity queueTileSet;
        public boolean queueBreak = false;
        @Nullable

        ChangeBlock(
            final int i, final int snapshotIndex, final SpongeBlockSnapshot attachedSnapshot, final BlockState newState, final SpongeBlockChangeFlag blockChange) {
            super(i, snapshotIndex, attachedSnapshot.getBlockPos(), (BlockState) attachedSnapshot.getState());
            this.original = attachedSnapshot;
            this.newState = newState;
            this.blockChangeFlag = blockChange;
            this.originalOpacity = this.originalState.getOpacity(this.original.getServerWorld().get(), this.affectedPosition);
        }

        public BlockState getNewState() {
            return this.newState;
        }

        public SpongeBlockChangeFlag getBlockChangeFlag() {
            return this.blockChangeFlag;
        }

        @Override
        public void populateChunkEffects(final TransactionalCaptureSupplier blockTransactor,
            final ChunkPipeline.Builder builder,
            final ChunkSection chunksection
        ) {

            builder.addEffect(new SetBlockToChunkSectionEffect());
            builder.addEffect(new UpdateHeightMapEffect());
            builder.addEffect(new UpdateChunkLightManagerEffect());
            builder.addEffect(new OldBlockOnReplaceEffect());
            builder.addEffect(new CheckBlockPostPlacementIsSameEffect());
            builder.addEffect(new RefreshOldTileEntityOnChunkChangeEffect());
            builder.addEffect(new BlockAddedEffect());
            builder.addEffect(new UpdateOrCreateNewTileEntityPostPlacementEffect());
            builder.addEffect(new ChunkChangeCompleteEffect());
        }

        @Override
        public Optional<Consumer<CauseStackManager.StackFrame>> getFrameMutator() {
            return Optional.of(frame -> {
                // TODO - Build a Transaction for this particular change, since we only know if a *new* BlockEntity is being placed as a side effect,
                // we cannot construct the Transaction in the constructor, it gets populated as a result of the side effect.
                frame.pushCause(this.original);
            });
        }

        @Override
        public void provideUnchangedStates(final BlockTransaction prevChange) {
            this.provideExistingBlockState(prevChange, (BlockState) this.original.getState());
            if (prevChange.applyTileAtTransaction(this.affectedPosition, this.queuedRemoval)) {
                this.appliedPreChange = true;
            }
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

    }


    static final class NeighborNotification extends BlockTransaction {
        final TrackedWorldBridge worldBridge;
        final BlockState notifyState;
        final BlockPos notifyPos;
        final Block sourceBlock;
        final BlockPos sourcePos;

        NeighborNotification(final int transactionIndex, final int snapshotIndex, final TrackedWorldBridge worldBridge, final BlockState notifyState, final BlockPos notifyPos,
                             final Block sourceBlock, final BlockPos sourcePos, final BlockState sourceState) {
            super(transactionIndex, snapshotIndex, sourcePos, sourceState);
            this.worldBridge = worldBridge;
            this.notifyState = notifyState;
            this.notifyPos = notifyPos;
            this.sourceBlock = sourceBlock;
            this.sourcePos = sourcePos;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", NeighborNotification.class.getSimpleName() + "[", "]")
                .add("world=" + this.worldBridge)
                .add("notifyState=" + this.notifyState)
                .add("notifyPos=" + this.notifyPos)
                .add("sourceBlock=" + this.sourceBlock)
                .add("sourcePos=" + this.sourcePos)
                .add("actualSourceState=" + this.originalState)
                .toString();
        }

        @Override
        public void provideUnchangedStates(final BlockTransaction prevChange) {
            this.provideExistingBlockState(prevChange, this.originalState);
        }

        @Override
        public void populateChunkEffects(final TransactionalCaptureSupplier blockTransactor,
            final ChunkPipeline.Builder builder,
            final ChunkSection chunksection
        ) {
            
        }

        @Override
        public Optional<Consumer<CauseStackManager.StackFrame>> getFrameMutator() {
            return Optional.empty();
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

    }

    static final class TransactionProcessState implements IPhaseState<TransactionContext> {

        public static final TransactionProcessState TRANSACTION_PROCESS = new TransactionProcessState();

        private TransactionProcessState() {
        }

        @Override
        public TransactionContext createPhaseContext(final PhaseTracker server) {
            throw new IllegalStateException("Cannot create context");
        }

        @Override
        public void unwind(final TransactionContext phaseContext) {

        }

        @Override
        public boolean tracksCreatorsAndNotifiers() {
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
            super(TransactionProcessState.TRANSACTION_PROCESS, PhaseTracker.SERVER);
        }
    }
}
