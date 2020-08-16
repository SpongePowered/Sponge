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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.server.ServerWorld;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKey;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.bridge.tileentity.TileEntityBridge;
import org.spongepowered.common.bridge.world.TrackedWorldBridge;
import org.spongepowered.common.event.tracking.BlockChangeFlagManager;
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
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.PrettyPrinter;
import org.spongepowered.common.world.BlockChange;
import org.spongepowered.common.world.SpongeBlockChangeFlag;
import org.spongepowered.common.world.SpongeLocatableBlockBuilder;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@DefaultQualifier(NonNull.class)
public abstract class BlockTransaction<E extends Event & Cancellable> {
    private static final int EVENT_COUNT = BlockChange.values().length + 1;
    private static final int MULTI_CHANGE_INDEX = BlockChange.values().length;
    private static final Function<ImmutableList.Builder<Transaction<BlockSnapshot>>[], Consumer<Transaction<BlockSnapshot>>> TRANSACTION_PROCESSOR =
        builders ->
            transaction -> {
                final BlockChange blockChange = ((SpongeBlockSnapshot) transaction.getOriginal()).blockChange;
                builders[blockChange.ordinal()].add(transaction);
                builders[BlockTransaction.MULTI_CHANGE_INDEX].add(transaction);
            }
        ;
    static final Function<SpongeBlockSnapshot, Optional<Transaction<BlockSnapshot>>> TRANSACTION_CREATION =
        (blockSnapshot) -> blockSnapshot.getServerWorld().map(worldServer -> {
            final BlockPos targetPos = blockSnapshot.getBlockPos();
            final SpongeBlockSnapshot replacement = ((TrackedWorldBridge) worldServer).bridge$createSnapshot(targetPos, BlockChangeFlags.NONE);
            return new Transaction<>(blockSnapshot, replacement);
        });

    // State definitions
    final BlockPos affectedPosition;
    final BlockState originalState;
    boolean cancelled = false;

    // Children Definitions
    @Nullable LinkedList<ResultingTransactionBySideEffect> sideEffects;

    // LinkedList node definitions
    @Nullable BlockTransaction<@NonNull ?> previous;
    @Nullable BlockTransaction<@NonNull ?> next;

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

    public abstract Optional<BiConsumer<PhaseContext<?>, CauseStackManager.StackFrame>> getFrameMutator();

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

    public abstract E generateEvent(PhaseContext<@NonNull ?> context, ImmutableList<BlockTransaction<E>> transactions, Cause currentCause);

    public abstract void restore();

    public abstract boolean canBatchWith(@Nullable final BlockTransaction<?> next);

    public boolean avoidsEvent() {
        return false;
    }

    public void markCancelled() {
        this.cancelled = true;
    }

    public abstract boolean markCancelledTransactions(E event, ImmutableList<? extends BlockTransaction<E>> transactions);

    static abstract class BlockEventBasedTransaction extends BlockTransaction<ChangeBlockEvent> {

        BlockEventBasedTransaction(final BlockPos affectedPosition, final BlockState originalState) {
            super(affectedPosition, originalState);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public final ChangeBlockEvent generateEvent(final PhaseContext<@NonNull ?> context,
            final ImmutableList<BlockTransaction<ChangeBlockEvent>> transactions,
            final Cause currentCause
        ) {
            final ListMultimap<BlockPos, SpongeBlockSnapshot> positions = LinkedListMultimap.create();
            for (final BlockTransaction<@NonNull ?> transaction : transactions) {
                if (!positions.containsKey(transaction.affectedPosition)) {
                    positions.put(transaction.affectedPosition, ((BlockEventBasedTransaction) transaction).getOriginalSnapshot());
                }
                positions.put(transaction.affectedPosition, ((BlockEventBasedTransaction) transaction).getResultingSnapshot());
            }

            final ImmutableList<Transaction<BlockSnapshot>>[] transactionArrays = new ImmutableList[BlockTransaction.EVENT_COUNT];
            final ImmutableList.Builder<Transaction<BlockSnapshot>>[] transactionBuilders = new ImmutableList.Builder[BlockTransaction.EVENT_COUNT];
            for (int i = 0; i < BlockTransaction.EVENT_COUNT; i++) {
                transactionBuilders[i] = new ImmutableList.Builder<>();
            }
            // Bug is here- use the multimap
            final ImmutableList<Transaction<BlockSnapshot>> eventTransactions = transactions.stream()
                .map(transaction -> {
                    final List<SpongeBlockSnapshot> snapshots = positions.get(transaction.affectedPosition);
                    final SpongeBlockSnapshot original = snapshots.get(0);
                    final SpongeBlockSnapshot result = snapshots.get(snapshots.size() - 1);
                    final ImmutableList<BlockSnapshot> intermediary;
                    if (snapshots.size() > 2) {
                        intermediary = ImmutableList.copyOf(snapshots.subList(1, snapshots.size() - 2));
                    } else {
                        intermediary = ImmutableList.of();
                    }
                    final Transaction<BlockSnapshot> eventTransaction = new Transaction<>(original, result, intermediary);
                    transactionBuilders[original.blockChange.ordinal()].add(eventTransaction);
                    return eventTransaction;
                }).collect(ImmutableList.toImmutableList());
            for (int i = 0; i < BlockTransaction.EVENT_COUNT; i++) {
                transactionArrays[i] = transactionBuilders[i].build();
            }
            final @Nullable ChangeBlockEvent[] mainEvents = new ChangeBlockEvent[BlockChange.values().length];
            for (final BlockChange blockChange : BlockChange.values()) {
                if (blockChange == BlockChange.DECAY) { // Decay takes place after.
                    continue;
                }
                if (!transactionArrays[blockChange.ordinal()].isEmpty()) {
                    final ChangeBlockEvent event = blockChange.createEvent(currentCause, transactionArrays[blockChange.ordinal()]);
                    mainEvents[blockChange.ordinal()] = event;
                    Sponge.getEventManager().post(event);
                }
            }
            if (!transactionArrays[BlockChange.DECAY.ordinal()].isEmpty()) { // Needs to be placed into iterateChangeBlockEvents
                final ChangeBlockEvent event = BlockChange.DECAY.createEvent(currentCause, transactionArrays[BlockChange.DECAY.ordinal()]);
                mainEvents[BlockChange.DECAY.ordinal()] = event;
                Sponge.getEventManager().post(event);
            }
            final Cause causeToUse;
            if (((IPhaseState) context.state).shouldProvideModifiers(context)) {
                final Cause.Builder builder = Cause.builder().from(currentCause);
                final EventContext.Builder modified = EventContext.builder();
                modified.from(currentCause.getContext());
                for (final BlockChange blockChange : BlockChange.values()) {
                    final @Nullable ChangeBlockEvent mainEvent = mainEvents[blockChange.ordinal()];
                    if (mainEvent != null) {
                        builder.append(mainEvent);
                        modified.add((EventContextKey<? super ChangeBlockEvent>) blockChange.getKey(), mainEvent);
                    }
                }
                causeToUse = builder.build(modified.build());
            } else {
                causeToUse = currentCause;
            }
            return SpongeEventFactory.createChangeBlockEventPost(
                causeToUse,
                eventTransactions
            );
        }

        protected abstract SpongeBlockSnapshot getResultingSnapshot();

        protected abstract SpongeBlockSnapshot getOriginalSnapshot();

        @Override
        public boolean canBatchWith(final @Nullable BlockTransaction<@NonNull ?> next) {
            return next instanceof BlockEventBasedTransaction;
        }

        @Override
        public final boolean markCancelledTransactions(final ChangeBlockEvent event,
            final ImmutableList<? extends BlockTransaction<ChangeBlockEvent>> blockTransactions
        ) {
            boolean cancelledAny = false;
            for (final Transaction<BlockSnapshot> transaction: event.getTransactions()) {
                if (!transaction.isValid()) {
                    cancelledAny = true;
                    for (final BlockTransaction<ChangeBlockEvent> blockTransaction : blockTransactions) {
                        final Vector3i position = transaction.getOriginal().getPosition();
                        final BlockPos affectedPosition = blockTransaction.affectedPosition;
                        if (position.getX() == affectedPosition.getX()
                            && position.getY() == affectedPosition.getY()
                            && position.getZ() == affectedPosition.getZ()
                        ) {
                            blockTransaction.markCancelled();
                        }
                    }
                }
            }

            return cancelledAny;
        }
    }

    @DefaultQualifier(NonNull.class)
    public static final class AddTileEntity extends BlockEventBasedTransaction {

        final TileEntity added;
        final SpongeBlockSnapshot oldSnapshot;
        final SpongeBlockSnapshot addedSnapshot;

        AddTileEntity(final TileEntity added,
            final SpongeBlockSnapshot attachedSnapshot,
            final SpongeBlockSnapshot existing
        ) {
            super(existing.getBlockPos(), (BlockState) existing.getState());
            this.added = added;
            this.addedSnapshot = attachedSnapshot;
            this.oldSnapshot = existing;
        }

        @Override
        public void populateChunkEffects(final TransactionalCaptureSupplier blockTransactor,
            final ChunkPipeline.Builder builder,
            final ChunkSection chunksection
        ) {

        }

        @Override
        public Optional<BiConsumer<PhaseContext<?>, CauseStackManager.StackFrame>> getFrameMutator() {
            return Optional.empty();
        }

        @Override
        public void addToPrinter(final PrettyPrinter printer) {
            printer.add("AddTileEntity")
                .addWrapped(120, " %s : %s", this.affectedPosition, ((TileEntityBridge) this.added).bridge$getPrettyPrinterString());
        }

        @Override
        public void restore() {
            this.oldSnapshot.restore(true, BlockChangeFlags.NONE);
        }

        @Override
        protected SpongeBlockSnapshot getResultingSnapshot() {
            return this.addedSnapshot;
        }

        @Override
        protected SpongeBlockSnapshot getOriginalSnapshot() {
            return this.addedSnapshot;
        }
    }

    @DefaultQualifier(NonNull.class)
    public static final class RemoveTileEntity extends BlockEventBasedTransaction {

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
        public Optional<BiConsumer<PhaseContext<?>, CauseStackManager.StackFrame>> getFrameMutator() {
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
        public void restore() {
            this.tileSnapshot.restore(true, BlockChangeFlags.NONE);
        }

        @Override
        protected SpongeBlockSnapshot getResultingSnapshot() {
            return SpongeBlockSnapshotBuilder.pooled()
                .world((ServerWorld) this.removed.getWorld())
                .position(new Vector3i(this.affectedPosition.getX(), this.affectedPosition.getY(), this.affectedPosition.getZ()))
                .blockState(this.originalState)
                .build()
                ;
        }

        @Override
        protected SpongeBlockSnapshot getOriginalSnapshot() {
            return this.tileSnapshot;
        }
    }

    @DefaultQualifier(NonNull.class)
    public static final class ReplaceTileEntity extends BlockEventBasedTransaction {

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
        public void restore() {
            this.removedSnapshot.restore(true, BlockChangeFlags.NONE);
        }

        @Override
        public Optional<BiConsumer<PhaseContext<?>, CauseStackManager.StackFrame>> getFrameMutator() {
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

        @Override
        protected SpongeBlockSnapshot getResultingSnapshot() {
            return SpongeBlockSnapshotBuilder.pooled()
                .from(this.removedSnapshot)
                .tileEntity(this.added)
                .build()
                ;
        }

        @Override
        protected SpongeBlockSnapshot getOriginalSnapshot() {
            return this.removedSnapshot;
        }
    }

    @DefaultQualifier(NonNull.class)
    public static final class ChangeBlock extends BlockEventBasedTransaction {

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
        public Optional<BiConsumer<PhaseContext<?>, CauseStackManager.StackFrame>> getFrameMutator() {
            return Optional.of((context, frame) -> {
                context.addCreatorAndNotifierToCauseStack(frame);
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
        public void restore() {
            this.original.restore(true, BlockChangeFlagManager.fromNativeInt(Constants.BlockChangeFlags.FORCED_RESTORE));
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

        @Override
        protected SpongeBlockSnapshot getResultingSnapshot() {
            return SpongeBlockSnapshotBuilder.pooled()
                .world(this.original.getWorld())
                .position(this.original.getPosition())
                .blockState((org.spongepowered.api.block.BlockState) this.newState)
                .build()
                ;
        }

        @Override
        protected SpongeBlockSnapshot getOriginalSnapshot() {
            return this.original;
        }
    }


    static final class NeighborNotification extends BlockTransaction<NotifyNeighborBlockEvent> {
        final BlockState original;
        final BlockPos notifyPos;
        final Block sourceBlock;
        final BlockPos sourcePos;
        private final Supplier<ServerWorld> serverWorld;
        private Supplier<LocatableBlock> locatableBlock;

        NeighborNotification(final Supplier<ServerWorld> serverWorldSupplier,
            final BlockState notifyState, final BlockPos notifyPos,
            final Block sourceBlock, final BlockPos sourcePos
        ) {
            super(sourcePos, notifyState);
            this.serverWorld = serverWorldSupplier;
            this.notifyPos = notifyPos;
            this.sourceBlock = sourceBlock;
            this.sourcePos = sourcePos;
            this.original = serverWorldSupplier.get().getBlockState(sourcePos);
            // This is one way to have lazily initialized fields
            this.locatableBlock = () -> {
                final LocatableBlock locatableBlock = new SpongeLocatableBlockBuilder()
                    .world(this.serverWorld)
                    .position(this.sourcePos.getX(), this.sourcePos.getY(), this.sourcePos.getZ())
                    .state((org.spongepowered.api.block.BlockState) this.original)
                    .build();
                this.locatableBlock = () -> locatableBlock;
                return locatableBlock;
            };
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", NeighborNotification.class.getSimpleName() + "[", "]")
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
        public Optional<BiConsumer<PhaseContext<?>, CauseStackManager.StackFrame>> getFrameMutator() {
            return Optional.of((context, frame) -> {
                frame.pushCause(this.locatableBlock.get());
            });
        }

        @Override
        public void addToPrinter(final PrettyPrinter printer) {
            printer.add("NeighborNotification")
                .add(" %s : %s, %s", "Source Pos", this.sourceBlock, this.sourcePos)
                .add(" %s : %s, %s", "Notification", this.originalState, this.notifyPos);
        }

        @Override
        public NotifyNeighborBlockEvent generateEvent(final PhaseContext<@NonNull ?> context,
            final ImmutableList<BlockTransaction<NotifyNeighborBlockEvent>> transactions,
            final Cause currentCause
        ) {
            // TODO - for all neighbor notifications in the transactions find the direction of notification being used and pump into map.
            final NotifyNeighborBlockEvent neighborBlockEvent = SpongeEventFactory.createNotifyNeighborBlockEvent(
                currentCause,
                new EnumMap<>(Direction.class),
                Collections.emptyMap()
            );
            return neighborBlockEvent;
        }

        @Override
        public void restore() {

        }

        @Override
        public boolean canBatchWith(@Nullable final BlockTransaction<@NonNull ?> next) {
            return next instanceof NeighborNotification;
        }

        @Override
        public boolean avoidsEvent() {
            return true;
        }

        @Override
        public boolean markCancelledTransactions(final NotifyNeighborBlockEvent event,
            final ImmutableList<? extends BlockTransaction<NotifyNeighborBlockEvent>> blockTransactions
        ) {
            return false;
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
