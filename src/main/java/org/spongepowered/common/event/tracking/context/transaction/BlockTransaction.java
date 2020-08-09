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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.bridge.tileentity.TileEntityBridge;
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

import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Consumer;

@DefaultQualifier(NonNull.class)
public abstract class BlockTransaction {

    // State definitions
    final BlockPos affectedPosition;
    final BlockState originalState;

    // Children Definitions
    @Nullable LinkedList<ResultingTransactionBySideEffect> sideEffects;

    // LinkedList node definitions
    @Nullable BlockTransaction previous;
    @Nullable BlockTransaction next;

    BlockTransaction(final BlockPos affectedPosition, final BlockState originalState) {
        this.affectedPosition = affectedPosition;
        this.originalState = originalState;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BlockTransaction.class.getSimpleName() + "[", "]")
            .add("affectedPosition=" + this.affectedPosition)
            .add("originalState=" + this.originalState)
            .toString();
    }

    final boolean addEffect(final ResultingTransactionBySideEffect effect) {
        if (this.sideEffects == null) {
            this.sideEffects = new LinkedList<>();
        }
        this.sideEffects.push(effect);
        return true;
    }

    public abstract void populateChunkEffects(
        TransactionalCaptureSupplier blockTransactor,
        ChunkPipeline.Builder builder, ChunkSection chunksection
    );

    Deque<ResultingTransactionBySideEffect> getEffects() {
        if (this.sideEffects == null) {
            this.sideEffects = new LinkedList<>();
        }
        return this.sideEffects;
    }

    public final boolean hasChildTransactions() {
        return this.sideEffects != null && this.sideEffects.stream().anyMatch(effect -> effect.head != null);
    }

    public abstract Optional<Consumer<CauseStackManager.StackFrame>> getFrameMutator();

    public abstract void addToPrinter(PrettyPrinter printer);

    public boolean acceptTileRemoval(final TileEntity tileentity) {
        return false;
    }

    public boolean acceptTileAddition(final TileEntity tileEntity) {
        return false;
    }

    public boolean acceptTileReplacement(final @Nullable TileEntity existing, final TileEntity proposed) {
        return false;
    }

    @DefaultQualifier(NonNull.class)
    public static final class AddTileEntity extends BlockTransaction {

        final TileEntity added;
        final SpongeBlockSnapshot addedSnapshot;

        AddTileEntity(final TileEntity added, final SpongeBlockSnapshot attachedSnapshot) {
            super(attachedSnapshot.getBlockPos(), (BlockState) attachedSnapshot.getState());
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
        public void addToPrinter(final PrettyPrinter printer) {
            printer.add("AddTileEntity")
                .addWrapped(120, " %s : %s", this.affectedPosition, ((TileEntityBridge) this.added).bridge$getPrettyPrinterString());
        }

    }

    @DefaultQualifier(NonNull.class)
    public static final class RemoveTileEntity extends BlockTransaction {

        final TileEntity removed;
        final SpongeBlockSnapshot tileSnapshot;

        RemoveTileEntity(final TileEntity removed, final SpongeBlockSnapshot attachedSnapshot) {
            super(attachedSnapshot.getBlockPos(), (BlockState) attachedSnapshot.getState());
            this.removed = removed;
            this.tileSnapshot = attachedSnapshot;
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

    }

    @DefaultQualifier(NonNull.class)
    public static final class ReplaceTileEntity extends BlockTransaction {

        final TileEntity added;
        final TileEntity removed;
        final SpongeBlockSnapshot removedSnapshot;

        ReplaceTileEntity(final TileEntity added, final TileEntity removed,
            final SpongeBlockSnapshot attachedSnapshot
        ) {
            super(attachedSnapshot.getBlockPos(), (BlockState) attachedSnapshot.getState());
            this.added = added;
            this.removed = removed;
            this.removedSnapshot = attachedSnapshot;
        }

        @Override
        public void populateChunkEffects(final TransactionalCaptureSupplier blockTransactor,
            final ChunkPipeline.Builder builder,
            final ChunkSection chunksection
        ) {

        }

        @Override
        public boolean acceptTileAddition(final TileEntity tileEntity) {
            if (this.added == tileEntity) {
                return true;
            }
            return super.acceptTileAddition(tileEntity);
        }

        @Override
        public Optional<Consumer<CauseStackManager.StackFrame>> getFrameMutator() {
            return Optional.empty();
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

    @DefaultQualifier(NonNull.class)
    public static final class ChangeBlock extends BlockTransaction {

        final SpongeBlockSnapshot original;
        final int originalOpacity;
        final BlockState newState;
        final SpongeBlockChangeFlag blockChangeFlag;
        @Nullable public TileEntity queuedRemoval;
        @Nullable public TileEntity queuedAdd;

        ChangeBlock(final SpongeBlockSnapshot attachedSnapshot, final BlockState newState,
            final SpongeBlockChangeFlag blockChange
        ) {
            super(attachedSnapshot.getBlockPos(), (BlockState) attachedSnapshot.getState());
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
        public boolean acceptTileAddition(final TileEntity tileEntity) {
            if (!this.affectedPosition.equals(tileEntity.getPos())) {
                return false;
            }
            if (this.queuedAdd != null) {
                return false;

            }
            this.queuedAdd = tileEntity;
            return true;
        }

        @Override
        public boolean acceptTileRemoval(final TileEntity tileentity) {
            if (!this.affectedPosition.equals(tileentity.getPos())) {
                return false;
            }
            if (this.queuedRemoval != null) {
                return false;
            }
            this.queuedRemoval = tileentity;
            return true;
        }

        @Override
        public boolean acceptTileReplacement(final @Nullable TileEntity existing, final TileEntity proposed) {
            return existing != null && this.acceptTileRemoval(existing) && this.acceptTileAddition(proposed);
        }

        @Override
        public void addToPrinter(final PrettyPrinter printer) {
            printer.add("ChangeBlock")
                .add(" %s : %s", "Original Block", this.original)
                .add(" %s : %s", "New State", this.newState)
                .add(" %s : %s", "RemovedTile", this.queuedRemoval)
                .add(" %s : %s", "AddedTile", this.queuedAdd)
                .add(" %s : %s", "ChangeFlag", this.blockChangeFlag);
        }

    }


    static final class NeighborNotification extends BlockTransaction {
        final BlockState original;
        final BlockPos notifyPos;
        final Block sourceBlock;
        final BlockPos sourcePos;

        NeighborNotification(final BlockState snapshot,
            final BlockState notifyState, final BlockPos notifyPos,
            final Block sourceBlock, final BlockPos sourcePos
        ) {
            super(sourcePos, notifyState);
            this.original = snapshot;
            this.notifyPos = notifyPos;
            this.sourceBlock = sourceBlock;
            this.sourcePos = sourcePos;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", NeighborNotification.class.getSimpleName() + "[", "]")
                .add("originalState=" + this.original)
                .add("notifyState=" + this.originalState)
                .add("notifyPos=" + this.notifyPos)
                .add("sourceBlock=" + this.sourceBlock)
                .add("sourcePos=" + this.sourcePos)
                .add("actualSourceState=" + this.originalState)
                .toString();
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
            printer.add("NeighborNotification")
                .add(" %s : %s, %s", "Source Pos", this.sourceBlock, this.sourcePos)
                .add(" %s : %s, %s", "Notification", this.originalState, this.notifyPos);
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
