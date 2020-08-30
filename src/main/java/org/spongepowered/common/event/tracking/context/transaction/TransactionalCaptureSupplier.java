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
import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.entity.SpawnType;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.bridge.world.TrackedWorldBridge;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.context.ICaptureSupplier;
import org.spongepowered.common.world.BlockChange;
import org.spongepowered.common.world.SpongeBlockChangeFlag;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@SuppressWarnings("rawtypes")
public final class TransactionalCaptureSupplier implements ICaptureSupplier {

    @Nullable private LinkedListMultimap<BlockPos, SpongeBlockSnapshot> multimap;
    @Nullable private ListMultimap<BlockPos, BlockEventData> scheduledEvents;
    @Nullable private List<SpongeBlockSnapshot> snapshots;
    @Nullable private Set<BlockPos> usedBlocks;
    // We made BlockTransaction a Node and this is a pseudo LinkedList due to the nature of needing
    // to be able to track what block states exist at the time of the transaction while other transactions
    // are processing (because future transactions performing logic based on what exists at that state,
    // will potentially get contaminated information based on the last transaction prior to transaction
    // processing). Example: When starting to perform neighbor notifications during piston movement, one
    // can feasibly see that the block state is changed already without being able to get the appropriate
    // block state.
    @Nullable private GameTransaction tail;
    @Nullable private GameTransaction head;
    @Nullable private ResultingTransactionBySideEffect effect;

    public TransactionalCaptureSupplier() {
    }

    /**
     * Captures the provided {@link BlockSnapshot} into a {@link Multimap} backed collection.
     * The premise is that each {@link BlockPos} normally has a single {@link BlockChange},
     * with the exceptions of certain few cases where multiple changes can occur for the same
     * position. The larger issue is that while the multiple changes are tracked, the desired
     * flag of changes does not result in a valid {@link BlockChange}, and therefor an invalid
     * {@link ChangeBlockEvent} is generated, potentially leading to duplication bugs with
     * protection plugins. As a result, the consuming {@link BlockSnapshot} is placed into
     * a {@link ListMultimap} keyed by the {@link BlockPos}, and if there are multiple snapshots
     * per {@link BlockPos}, has multiple changes will be {@code true}, and this method
     * will return {@code true}.
     *
     * @param snapshot The snapshot being captured
     * @param newState The most current new IBlockState to calculate the BlockChange flag
     * @return True if the block position has previously not been modified or captured yet
     */
    public boolean put(final BlockSnapshot snapshot, final BlockState newState) {
        // Start by figuring out the backing snapshot. In all likelyhood, we could just cast, but we want to be safe
        final SpongeBlockSnapshot backingSnapshot = this.getBackingSnapshot(snapshot);
        // Get the key of the block position, we know this is a pure block pos and not a mutable one too.
        final BlockPos blockPos = backingSnapshot.getBlockPos();
        if (this.usedBlocks == null) { // Means we have a first usage. All three fields are null
            // At this point, we know we have not captured anything and
            // can just populate the normal list.
            this.usedBlocks = new HashSet<>();

            this.usedBlocks.add(blockPos);
            this.addSnapshot(backingSnapshot);
            return true;
        }
        // This isn't our first rodeo...
        final boolean added = this.usedBlocks.add(blockPos); // add it to the set of positions already used and use the boolean
        if (this.multimap != null) {
            // Means we've already got multiple changes per position once before.
            // Likewise, the used blocks, snapshots and multimap will NOT be null.
            // more fasts, we know we have multiple block positions.
            // And we can find out if this is the first time we
            if (added) {
                // If the position hasn't been captured yet, that means we need to add it as an original
                // snapshot being changed, for the list usage.
                this.addSnapshot(backingSnapshot);
            }
            // we don't have to
            this.multimap.put(blockPos, backingSnapshot);

            // If the position is duplicated, we need to update the original snapshot of the now incoming block change
            // in relation to the original state (so if a block was set to air, then afterwards set to piston head, it should go from break to modify)
            if (!added) {
                this.associateBlockChangeForPosition(newState, blockPos);
            }
            return added;
        }
        // We have not yet checked if this incoming snapshot is a duplicate position
        if (!added) {
            // Ok, means we have a multi change on a same position, now to use the multimap
            // for the first time.
            this.multimap = LinkedListMultimap.create(); // LinkedListMultimap is insertion order respective, so the backed lists per
            // Now to populate it from the previously used list of snapshots...
            for (final SpongeBlockSnapshot existing : this.snapshots) { // Ignore snapshots potentially being null, it will never be null at this point.
                this.multimap.put(existing.getBlockPos(), existing);
            }
            // And place the snapshot into the multimap.
            this.multimap.put(blockPos, backingSnapshot);
            // Now we can re-evaluate the modified block position
            // If the position is duplicated, we need to update the original snapshot of the now incoming block change
            // in relation to the original state (so if a block was set to air, then afterwards set to piston head, it should go from break to modify)
            this.associateBlockChangeForPosition(newState, blockPos);
            return false;
        }
        // At this point, we haven't captured the block position yet.
        // and we can check if the list is null.
        this.addSnapshot(backingSnapshot);
        // And this is the only time that we return true, if we have not caught multiple transactions per position before.
        return true;
    }

    private void addSnapshot(final SpongeBlockSnapshot backingSnapshot) {
        if (this.snapshots == null) {
            this.snapshots = new ArrayList<>();
        }
        this.snapshots.add(backingSnapshot);
    }

    /**
     * Associates the desired block state {@link BlockChange} in comparison to the
     * already guaranteed original {@link SpongeBlockSnapshot} for proper event
     * creation when multiple block changes exist for the provided {@link BlockPos}.
     *
     * @param newState The incoming block change to compare to change
     * @param blockPos The block position to get the backing list from the multimap
     */
    @SuppressWarnings("unchecked")
    private void associateBlockChangeForPosition(final BlockState newState, final BlockPos blockPos) {
        final List<SpongeBlockSnapshot> list = this.multimap.get(blockPos);
        if (list != null && !list.isEmpty()) {
            final SpongeBlockSnapshot originalSnapshot = list.get(0);
            final PhaseContext<?> peek = PhaseTracker.getInstance().getPhaseContext();
            final BlockState currentState = (BlockState) originalSnapshot.getState();
            originalSnapshot.blockChange = ((IPhaseState) peek.state).associateBlockChangeWithSnapshot(
                peek,
                newState,
                newState.getBlock(),
                currentState,
                originalSnapshot,
                currentState.getBlock()
            );
        }
    }


    private SpongeBlockSnapshot getBackingSnapshot(final BlockSnapshot snapshot) {
        final SpongeBlockSnapshot backingSnapshot;
        if (!(snapshot instanceof SpongeBlockSnapshot)) {
            backingSnapshot = SpongeBlockSnapshotBuilder.pooled().from(snapshot).build();
        } else {
            backingSnapshot = (SpongeBlockSnapshot) snapshot;
        }
        return backingSnapshot;
    }

    /**
     * Returns {@code true} if there are no captured objects.
     *
     * @return {@code true} if empty
     */
    @Override
    public final boolean isEmpty() {
        return (this.snapshots == null || this.snapshots.isEmpty()) && this.head == null;
    }

    /*
    Begin the more enhanced block tracking. This is only used by states that absolutely need to be able to track certain changes
    that involve more "physics" related transactions, such as neighbor notification tracking, tile entity tracking, and
    normally, intermediary transaction tracking. Because of these states, we need to envelop the information relating to:
    - The most recent block change, if it has been a change that was applied
    - The most recent tile entity being captured
    - The most recent neighbor notification in the order in which it is being applied to in comparison with the most recent block change

    In some rare cases, some block changes may take place after a neighbor notification is submitted, or a tile entity is being replaced.
    To acommodate this, when such cases arise, we attempt to snapshot any potential transactions that may take place prior to their
    blocks being changed, allowing us to take full snapshots of tile entities in the event a complete restoration is required.
    This is achieved through captureNeighborNotification and logTileChange.
     */

    @SuppressWarnings("unchecked")
    public EffectTransactor pushEffect(final ResultingTransactionBySideEffect effect) {
        final GameTransaction parentTransaction = Optional.ofNullable(this.effect)
            .map(child -> child.tail)
            .orElse(Objects.requireNonNull(this.tail));
        final EffectTransactor effectTransactor = new EffectTransactor(effect, parentTransaction, this.effect, this);
        this.effect = effect;
        parentTransaction.getEffects().addLast(effect);
        return effectTransactor;
    }

    void popEffect(final EffectTransactor transactor) {
        this.effect = transactor.previousEffect;
    }

    private void logTransaction(final GameTransaction transaction) {
        if (this.head == null) {
            this.head = transaction;
            this.tail = transaction;
        } else if (this.effect != null) {
            this.effect.addChild(transaction);
        } else {
            transaction.previous = this.tail;
            if (this.tail != null) {
                this.tail.next = transaction;
            }
            this.tail = transaction;
        }
    }

    public ChangeBlock logBlockChange(final SpongeBlockSnapshot originalBlockSnapshot, final BlockState newState,
        final BlockChangeFlag flags
    ) {
        this.put(originalBlockSnapshot, newState); // Always update the snapshot index before the block change is tracked
        final ChangeBlock changeBlock = new ChangeBlock(
            originalBlockSnapshot, newState, (SpongeBlockChangeFlag) flags
        );
        this.logTransaction(changeBlock);
        return changeBlock;
    }

    public boolean logTileAddition(final TileEntity tileEntity,
        final Supplier<ServerWorld> worldSupplier, final Chunk chunk
        ) {
        if (tileEntity == null) {
            return false;
        }
        if (this.tail != null) {
            final boolean newRecorded = this.tail.acceptTileAddition(tileEntity);
            if (newRecorded) {
                return true;
            }
        }
        this.logTransaction(this.createTileAdditionTransaction(tileEntity, worldSupplier, chunk));

        return true;
    }

    public boolean logTileRemoval(@Nullable final TileEntity tileentity, final Supplier<ServerWorld> worldSupplier) {
        if (tileentity == null) {
            return false;
        }
        if (this.tail != null) {
            final boolean newRecorded = this.tail.acceptTileRemoval(tileentity);
            if (newRecorded) {
                return true;
            }
            this.logTransaction(this.createTileRemovalTransaction(tileentity, worldSupplier));
        } else {
            this.logTransaction(this.createTileRemovalTransaction(tileentity, worldSupplier));
        }
        return true;
    }

    public boolean logTileReplacement(final BlockPos pos, final @Nullable TileEntity existing, final TileEntity proposed, final Supplier<ServerWorld> worldSupplier) {
        if (proposed == null) {
            return false;
        }
        if (this.tail != null) {
            final boolean newRecorded = this.tail.acceptTileReplacement(existing, proposed);
            if (newRecorded) {
                return true;
            }
        }
        this.logTransaction(this.createTileReplacementTransaction(pos, existing, proposed, worldSupplier));
        return true;
    }

    public void logNeighborNotification(final Supplier<ServerWorld> serverWorldSupplier, final BlockPos immutableFrom, final Block blockIn,
        final BlockPos immutableTarget, final BlockState targetBlockState,
        @Nullable final TileEntity existingTile
    ) {
        final NeighborNotification notificationTransaction = new NeighborNotification(serverWorldSupplier, targetBlockState, immutableTarget, blockIn, immutableFrom);
        this.logTransaction(notificationTransaction);
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    public void logEntitySpawn(final PhaseContext<@NonNull ?> current, final TrackedWorldBridge serverWorld,
        final Entity entityIn) {
        final WeakReference<ServerWorld> worldRef = new WeakReference<>((ServerWorld) serverWorld);
        final Supplier<ServerWorld> worldSupplier = () -> Objects.requireNonNull(worldRef.get(), "ServerWorld dereferenced");
        final Supplier<SpawnType> contextualType = ((IPhaseState) current.state).getSpawnTypeForTransaction(current, entityIn);
        final SpawnEntityTransaction transaction = new SpawnEntityTransaction(worldSupplier, entityIn, contextualType);
        this.logTransaction(transaction);
    }
    private GameTransaction createTileReplacementTransaction(final BlockPos pos, final @Nullable TileEntity existing,
        final TileEntity proposed, final Supplier<ServerWorld> worldSupplier
    ) {
        final BlockState currentState = worldSupplier.get().getBlockState(pos);
        final SpongeBlockSnapshot snapshot = TrackingUtil.createPooledSnapshot(
            currentState,
            pos,
            BlockChangeFlags.NONE,
            existing,
            worldSupplier,
            Optional::empty, Optional::empty
        );
        this.put(snapshot, currentState); // Always update the snapshot index before the block change is tracked

        return new ReplaceTileEntity(proposed, existing, snapshot);
    }

    private RemoveTileEntity createTileRemovalTransaction(final TileEntity tileentity,
        final Supplier<ServerWorld> worldSupplier
    ) {
        final BlockState currentState = tileentity.getBlockState();
        final SpongeBlockSnapshot snapshot = TrackingUtil.createPooledSnapshot(
            currentState,
            tileentity.getPos(),
            BlockChangeFlags.NONE,
            tileentity,
            worldSupplier,
            Optional::empty, Optional::empty
        );
        this.put(snapshot, currentState); // Always update the snapshot index before the block change is tracked

        return new RemoveTileEntity(tileentity, snapshot);
    }

    private AddTileEntity createTileAdditionTransaction(final TileEntity tileentity,
        final Supplier<ServerWorld> worldSupplier, final Chunk chunk
    ) {
        final BlockPos pos = tileentity.getPos().toImmutable();
        final BlockState currentBlock = chunk.getBlockState(pos);
        final @Nullable TileEntity existingTile = chunk.getTileEntity(pos, Chunk.CreateEntityType.CHECK);

        final SpongeBlockSnapshot added = TrackingUtil.createPooledSnapshot(
            currentBlock,
            pos,
            BlockChangeFlags.NONE,
            tileentity,
            worldSupplier,
            Optional::empty, Optional::empty
        );
        final SpongeBlockSnapshot existing = TrackingUtil.createPooledSnapshot(
            currentBlock,
            pos,
            BlockChangeFlags.NONE,
            existingTile,
            worldSupplier,
            Optional::empty,
            Optional::empty
        );
        this.put(existing, currentBlock); // Always update the snapshot index before the block change is tracked

        return new AddTileEntity(tileentity, added, existing);
    }

    public void clear() {
        if (this.multimap != null) {
            this.multimap.clear();
            this.multimap = null;
        }
        if (this.snapshots != null) {
            this.snapshots.clear();
            this.snapshots = null;
        }
        if (this.usedBlocks != null) {
            this.usedBlocks.clear();
        }
        if (this.scheduledEvents != null) {
            this.scheduledEvents.clear();
        }
        this.effect = null;
    }

    @SuppressWarnings("unchecked")
    public boolean processTransactions(final PhaseContext<?> context) {
        if (this.head == null) {
            return true;
        }
        final ImmutableList<EventByTransaction<?>> batched = TransactionalCaptureSupplier.batchTransactions(this.head, this.head, context);
        boolean cancelledAny = false;
        for (final EventByTransaction<?> eventWithTransactions : batched) {
            final Event event = eventWithTransactions.event;
            if (eventWithTransactions.decider.cancelled) {
                cancelledAny = true;
                eventWithTransactions.markCancelled();
                continue;
            }
            Sponge.getEventManager().post(event);
            if (event instanceof Cancellable && ((Cancellable) event).isCancelled()) {
                eventWithTransactions.markCancelled();
                cancelledAny = true;
            }
            if (((GameTransaction) eventWithTransactions.decider).markCancelledTransactions(event, eventWithTransactions.transactions)) {
                cancelledAny = true;
            }
        }
        if (cancelledAny) {
            for (final EventByTransaction<@NonNull ?> eventByTransaction : batched.reverse()) {
                for (final GameTransaction<@NonNull ?> gameTransaction : eventByTransaction.transactions.reverse()) {
                    if (gameTransaction.cancelled) {
                        gameTransaction.restore();
                    }
                }
            }
        }
        return cancelledAny;
    }

    @SuppressWarnings("unchecked")
    private static ImmutableList<EventByTransaction<@NonNull ?>> batchTransactions(final GameTransaction head,
        final GameTransaction parent,
        final PhaseContext<?> context) {
        final ImmutableList.Builder<EventByTransaction<@NonNull ?>> builder = ImmutableList.builder();
        @Nullable GameTransaction pointer = head;
        ImmutableList.Builder<GameTransaction> accumilator = ImmutableList.builder();
        @MonotonicNonNull GameTransaction batchDecider = null;
        while (pointer != null) {
            if (pointer.avoidsEvent()) {
                pointer = pointer.next;
                continue;
            }
            if (batchDecider == null) {
                batchDecider = pointer;
            }
            // First - check if the side effects are empty
            if (pointer.hasAnyPrimaryChildrenTransactions()) {
                // If they're not, we need to basically build the event and whatever
                // has accumulated to this point as an event
                accumilator.add(pointer);
                final ImmutableList<GameTransaction> transactions = accumilator.build();
                accumilator = ImmutableList.builder();
                batchDecider = null;
                TransactionalCaptureSupplier.generateEventForTransaction(pointer, parent, context, builder, (ImmutableList) transactions);
            } else if (batchDecider != pointer && !batchDecider.canBatchWith(pointer)) {
                final ImmutableList<GameTransaction> transactions = accumilator.build();
                accumilator = ImmutableList.builder();
                TransactionalCaptureSupplier.generateEventForTransaction(batchDecider, parent, context, builder, (ImmutableList) transactions);
                accumilator.add(pointer);
                batchDecider = null;
            } else {
                accumilator.add(pointer);
            }
            pointer = pointer.next;
        }
        final ImmutableList<GameTransaction> remaining = accumilator.build();
        if (!remaining.isEmpty()) {
            TransactionalCaptureSupplier.generateEventForTransaction(
                Objects.requireNonNull(batchDecider, "BatchDeciding Transaction was null"),
                parent,
                context,
                builder,
                (ImmutableList) remaining
            );
        }
        return builder.build();
    }

    @SuppressWarnings("unchecked")
    private static <E extends Event & Cancellable> void generateEventForTransaction(
        @NonNull final GameTransaction<E> pointer,
        @Nullable final GameTransaction<?> parent,
        final PhaseContext<@NonNull ?> context,
        final ImmutableList.Builder<EventByTransaction<@NonNull ?>> builder,
        final ImmutableList<GameTransaction<E>> transactions
    ) {
        final Optional<BiConsumer<PhaseContext<@NonNull ?>, CauseStackManager.StackFrame>> frameMutator = pointer.getFrameMutator();
        final PhaseTracker instance = PhaseTracker.getInstance();
        try (
            final CauseStackManager.@Nullable StackFrame frame = frameMutator
                .map(mutator -> {
                    final CauseStackManager.StackFrame transactionFrame = instance.pushCauseFrame();
                    mutator.accept(context, transactionFrame);
                    return transactionFrame;
                })
                .orElse(null)
        ) {
            final E event = pointer.generateEvent(context, transactions, instance.getCurrentCause());

            final GameTransaction<E> decider = parent != null ? (GameTransaction<E>) parent : pointer;
            final EventByTransaction<E> element = new EventByTransaction<>(event, transactions, decider);
            builder.add(element);

            if (frame != null) {
                frame.pushCause(event);
            }
            pointer.getEffects().forEach(effect -> {
                if (effect.head != null) {
                    builder.addAll(TransactionalCaptureSupplier.batchTransactions(effect.head, pointer, context));
                }
            });
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.snapshots);
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        final TransactionalCaptureSupplier other = (TransactionalCaptureSupplier) obj;
        return Objects.equals(this.multimap, other.multimap);
    }

    @Override
    public String toString() {
        return com.google.common.base.MoreObjects.toStringHelper(this)
            .add("Captured", this.snapshots == null ? 0 : this.snapshots.size())
            .add("Head", this.head == null ? "null" : this.head)
            .toString();
    }

    public void reset() {
        if (this.multimap != null) {
            // shouldn't but whatever, it's the end of a phase.
            this.multimap.clear();
            this.multimap = null;
        }
        if (this.scheduledEvents != null) {
            this.scheduledEvents.clear();
        }
        if (this.snapshots != null) {
            this.snapshots.clear();
            this.snapshots = null;
        }
        if (this.usedBlocks != null) {
            this.usedBlocks.clear();
        }
        if (this.head != null) {
            this.head = null;
            this.tail = null;
        }
        if (this.effect != null) {
            this.effect = null;
        }

    }

}
