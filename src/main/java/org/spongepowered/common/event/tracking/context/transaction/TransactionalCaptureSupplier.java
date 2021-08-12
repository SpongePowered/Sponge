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
import com.google.common.collect.ImmutableMultimap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.TickNextTickData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.entity.SpawnType;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.common.accessor.world.damagesource.CombatEntryAccessor;
import org.spongepowered.common.accessor.world.damagesource.CombatTrackerAccessor;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.bridge.world.TrackedWorldBridge;
import org.spongepowered.common.bridge.world.level.TrackableBlockEventDataBridge;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.context.ICaptureSupplier;
import org.spongepowered.common.event.tracking.context.transaction.effect.EntityPerformingDropsEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.PrepareBlockDrops;
import org.spongepowered.common.event.tracking.context.transaction.type.TransactionType;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.world.BlockChange;
import org.spongepowered.common.world.SpongeBlockChangeFlag;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@SuppressWarnings("rawtypes")
public final class TransactionalCaptureSupplier implements ICaptureSupplier {

    // We made BlockTransaction a Node and this is a pseudo LinkedList due to the nature of needing
    // to be able to track what block states exist at the time of the transaction while other transactions
    // are processing (because future transactions performing logic based on what exists at that state,
    // will potentially get contaminated information based on the last transaction prior to transaction
    // processing). Example: When starting to perform neighbor notifications during piston movement, one
    // can feasibly see that the block state is changed already without being able to get the appropriate
    // block state.
    private @Nullable GameTransaction tail;
    private @Nullable GameTransaction head;
    private @Nullable ResultingTransactionBySideEffect effect;

    public TransactionalCaptureSupplier() {
    }


    /**
     * Returns {@code true} if there are no captured objects.
     *
     * @return {@code true} if empty
     */
    @Override
    public final boolean isEmpty() {
        return this.head == null;
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
        parentTransaction.addLast(effect);
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
        final ChangeBlock changeBlock = new ChangeBlock(
            originalBlockSnapshot, newState, (SpongeBlockChangeFlag) flags
        );
        this.logTransaction(changeBlock);
        return changeBlock;
    }

    public boolean logTileAddition(final BlockEntity tileEntity,
        final Supplier<ServerLevel> worldSupplier, final LevelChunk chunk
        ) {
        if (this.tail != null) {
            final boolean newRecorded = this.tail.acceptTileAddition(tileEntity);
            if (newRecorded) {
                return true;
            }
        }
        this.logTransaction(this.createTileAdditionTransaction(tileEntity, worldSupplier, chunk));

        return true;
    }

    @SuppressWarnings({"ConstantConditions", "unchecked"})
    public boolean logTileRemoval(final @Nullable BlockEntity tileentity, final Supplier<ServerLevel> worldSupplier) {
        if (tileentity == null) {
            return false;
        }
        if (this.tail != null) {
            final boolean newRecorded = this.tail.acceptTileRemoval(tileentity);
            if (newRecorded) {
                return true;
            }
            // Need to traverse children by "most recent" transactions to "oldest"
            // to verify which transaction could potentially absorb the tile removed
            if (this.tail.hasChildTransactions()) {
                final LinkedList<ResultingTransactionBySideEffect> sideEffects = this.tail.sideEffects;
                final Iterator<ResultingTransactionBySideEffect> iter = sideEffects.descendingIterator();
                // Nasty way at doing it with an iterator....
                for (ResultingTransactionBySideEffect sideEffect = iter.next(); iter.hasNext(); sideEffect = iter.next()) {
                    // Then we traverse our own manual doubly linked nodes.
                    @Nullable GameTransaction<@NonNull ?> pointer = sideEffect.tail;
                    while (pointer != null) {
                        if (pointer.acceptTileRemoval(tileentity)) {
                            return true;
                        }
                        pointer = pointer.previous;
                    }
                }
            }
        }
        this.logTransaction(this.createTileRemovalTransaction(tileentity, worldSupplier));
        return true;
    }

    public boolean logTileReplacement(final BlockPos pos, final @Nullable BlockEntity existing, final @Nullable BlockEntity proposed, final Supplier<ServerLevel> worldSupplier) {
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

    public void logNeighborNotification(final Supplier<ServerLevel> serverWorldSupplier, final BlockPos immutableFrom, final Block blockIn,
        final BlockPos immutableTarget, final BlockState targetBlockState,
        final @Nullable BlockEntity existingTile
    ) {
        final NeighborNotification notificationTransaction = new NeighborNotification(serverWorldSupplier, targetBlockState, immutableTarget, blockIn, immutableFrom, existingTile);
        this.logTransaction(notificationTransaction);
    }

    @SuppressWarnings({"ConstantConditions"})
    public void logEntitySpawn(final PhaseContext<@NonNull ?> current, final TrackedWorldBridge serverWorld,
        final Entity entityIn) {
        final WeakReference<ServerLevel> worldRef = new WeakReference<>((ServerLevel) serverWorld);
        final Supplier<ServerLevel> worldSupplier = () -> Objects.requireNonNull(worldRef.get(), "ServerWorld dereferenced");
        final Supplier<SpawnType> contextualType = current.getSpawnTypeForTransaction(entityIn);
        final SpawnEntityTransaction transaction = new SpawnEntityTransaction(worldSupplier, entityIn, contextualType);
        this.logTransaction(transaction);
    }
    private GameTransaction createTileReplacementTransaction(final BlockPos pos, final @Nullable BlockEntity existing,
        final BlockEntity proposed, final Supplier<ServerLevel> worldSupplier
    ) {
        final BlockState currentState = worldSupplier.get().getBlockState(pos);
        final SpongeBlockSnapshot snapshot = TrackingUtil.createPooledSnapshot(
            currentState,
            pos,
            BlockChangeFlags.NONE,
            Constants.World.DEFAULT_BLOCK_CHANGE_LIMIT,
            existing,
            worldSupplier,
            Optional::empty, Optional::empty
        );
        snapshot.blockChange = BlockChange.MODIFY;

        return new ReplaceBlockEntity(proposed, existing, snapshot);
    }

    @SuppressWarnings("ConstantConditions")
    public EffectTransactor logBlockDrops(
        final PhaseContext<@NonNull ?> context, final Level serverWorld, final BlockPos pos, final BlockState state,
        final @Nullable BlockEntity tileEntity) {
        final WeakReference<ServerLevel> worldRef = new WeakReference<>((ServerLevel) serverWorld);
        final Supplier<ServerLevel> worldSupplier = () -> Objects.requireNonNull(worldRef.get(), "ServerWorld dereferenced");
        final SpongeBlockSnapshot original = TrackingUtil.createPooledSnapshot(
            state,
            pos,
            BlockChangeFlags.NONE,
            Constants.World.DEFAULT_BLOCK_CHANGE_LIMIT,
            tileEntity,
            worldSupplier,
            Optional::empty, Optional::empty
        );
        original.blockChange = BlockChange.MODIFY;
        final PrepareBlockDropsTransaction transaction = new PrepareBlockDropsTransaction(pos, state, original);
        if (this.tail == null || !this.tail.acceptDrops(transaction)) {
            this.logTransaction(transaction);
        }
        return this.pushEffect(new ResultingTransactionBySideEffect(PrepareBlockDrops.getInstance()));
    }

    public void logBlockEvent(final BlockState state, final TrackedWorldBridge serverWorld, final BlockPos pos,
        final TrackableBlockEventDataBridge blockEvent
    ) {
        final WeakReference<ServerLevel> worldRef = new WeakReference<>((ServerLevel) serverWorld);
        final Supplier<ServerLevel> worldSupplier = () -> Objects.requireNonNull(worldRef.get(), "ServerWorld dereferenced");
        final @Nullable BlockEntity tileEntity = ((ServerLevel) serverWorld).getBlockEntity(pos);
        final SpongeBlockSnapshot original = TrackingUtil.createPooledSnapshot(
            state,
            pos,
            BlockChangeFlags.NONE,
            Constants.World.DEFAULT_BLOCK_CHANGE_LIMIT,
            tileEntity,
            worldSupplier,
            Optional::empty, Optional::empty
        );
        original.blockChange = BlockChange.MODIFY;
        final AddBlockEventTransaction transaction = new AddBlockEventTransaction(original, blockEvent);
        this.logTransaction(transaction);
    }

    public void logScheduledUpdate(final ServerLevel serverWorld, final TickNextTickData<?> data) {
        final WeakReference<ServerLevel> worldRef = new WeakReference<>((ServerLevel) serverWorld);
        final Supplier<ServerLevel> worldSupplier = () -> Objects.requireNonNull(worldRef.get(), "ServerWorld dereferenced");
        final @Nullable BlockEntity tileEntity = serverWorld.getBlockEntity(data.pos);
        final BlockState existing = serverWorld.getBlockState(data.pos);
        final SpongeBlockSnapshot original = TrackingUtil.createPooledSnapshot(
            existing,
            data.pos,
            BlockChangeFlags.NONE,
            Constants.World.DEFAULT_BLOCK_CHANGE_LIMIT,
            tileEntity,
            worldSupplier,
            Optional::empty, Optional::empty
        );
        original.blockChange = BlockChange.MODIFY;
        final ScheduleUpdateTransaction transaction = new ScheduleUpdateTransaction(original, data);
        this.logTransaction(transaction);
    }

    @SuppressWarnings({"ConstantConditions"})
    public @Nullable EffectTransactor ensureEntityDropTransactionEffect(final Entity entity) {
        if (this.tail != null) {
            if (this.tail.acceptEntityDrops(entity)) {
                return null;
            }
        }
        final WeakReference<ServerLevel> worldRef = new WeakReference<>((ServerLevel) entity.level);
        final Supplier<ServerLevel> worldSupplier = () -> Objects.requireNonNull(worldRef.get(), "ServerWorld dereferenced");
        final CompoundTag tag = new CompoundTag();
        entity.saveWithoutId(tag);
        final @Nullable DamageSource lastAttacker;
        if (entity instanceof LivingEntity) {
            final CombatEntry entry = ((CombatTrackerAccessor) ((LivingEntity) entity).getCombatTracker()).invoker$getMostSignificantFall();
            if (entry != null) {
                lastAttacker = ((CombatEntryAccessor) entry).accessor$source();
            } else {
                lastAttacker = null;
            }
        } else {
            lastAttacker = null;
        }
        final WeakReference<@Nullable DamageSource> ref = new WeakReference<>(lastAttacker);
        final Supplier<Optional<DamageSource>> attacker = () -> {
            final @Nullable DamageSource damageSource = ref.get();
            // Yes, I know, we're effectively
            if (damageSource == null) {
                return Optional.empty();
            }
            return Optional.of(damageSource);
        };
        final EntityPerformingDropsTransaction transaction = new EntityPerformingDropsTransaction(worldSupplier, entity, tag, attacker);
        this.logTransaction(transaction);
        return this.pushEffect(new ResultingTransactionBySideEffect(EntityPerformingDropsEffect.getInstance()));
    }

    public void completeBlockDrops(final @Nullable EffectTransactor context) {
        if (this.effect != null) {
            if (this.effect.effect == PrepareBlockDrops.getInstance()) {
                if (context != null) {
                    context.close();
                }
            }
        }
    }

    private RemoveBlockEntity createTileRemovalTransaction(final BlockEntity tileentity,
        final Supplier<ServerLevel> worldSupplier
    ) {
        final BlockState currentState = tileentity.getBlockState();
        final SpongeBlockSnapshot snapshot = TrackingUtil.createPooledSnapshot(
            currentState,
            tileentity.getBlockPos().immutable(),
            BlockChangeFlags.NONE,
            Constants.World.DEFAULT_BLOCK_CHANGE_LIMIT,
            tileentity,
            worldSupplier,
            Optional::empty, Optional::empty
        );
        snapshot.blockChange = BlockChange.MODIFY;

        return new RemoveBlockEntity(tileentity, snapshot);
    }

    private AddTileEntity createTileAdditionTransaction(final BlockEntity tileentity,
        final Supplier<ServerLevel> worldSupplier, final LevelChunk chunk
    ) {
        final BlockPos pos = tileentity.getBlockPos().immutable();
        final BlockState currentBlock = chunk.getBlockState(pos);
        final @Nullable BlockEntity existingTile = chunk.getBlockEntity(pos, LevelChunk.EntityCreationType.CHECK);

        final SpongeBlockSnapshot added = TrackingUtil.createPooledSnapshot(
            currentBlock,
            pos,
            BlockChangeFlags.NONE,
            Constants.World.DEFAULT_BLOCK_CHANGE_LIMIT,
            tileentity,
            worldSupplier,
            Optional::empty, Optional::empty
        );
        final SpongeBlockSnapshot existing = TrackingUtil.createPooledSnapshot(
            currentBlock,
            pos,
            BlockChangeFlags.NONE,
            Constants.World.DEFAULT_BLOCK_CHANGE_LIMIT,
            existingTile,
            worldSupplier,
            Optional::empty,
            Optional::empty
        );
        existing.blockChange = BlockChange.MODIFY;

        return new AddTileEntity(tileentity, added, existing);
    }

    public void clear() {
        this.head = null;
        this.tail = null;
        this.effect = null;
    }

    @SuppressWarnings("unchecked")
    public boolean processTransactions(final PhaseContext<@NonNull ?> context) {
        if ((GameTransaction<@NonNull ?>) this.head == null) {
            return false;
        }
        final ImmutableMultimap.Builder<TransactionType, ? extends Event> builder = ImmutableMultimap.builder();
        final ImmutableList<EventByTransaction<@NonNull ?>> batched = TransactionalCaptureSupplier.batchTransactions(
            this.head, this.head, context, builder
        );
        boolean cancelledAny = false;
        for (final EventByTransaction<@NonNull ?> eventWithTransactions : batched) {
            final Event event = eventWithTransactions.event;
            if (eventWithTransactions.isParentOrDeciderCancelled()) {
                cancelledAny = true;
                eventWithTransactions.markCancelled();
                continue;
            }
            Sponge.eventManager().post(event);
            if (event instanceof Cancellable && ((Cancellable) event).isCancelled()) {
                eventWithTransactions.markCancelled();
                cancelledAny = true;
            }
            if (((GameTransaction) eventWithTransactions.decider).markCancelledTransactions(event, eventWithTransactions.transactions)) {
                cancelledAny = true;
            }
            for (final GameTransaction<@NonNull ?> transaction : eventWithTransactions.transactions) {
                if (transaction.cancelled) {
                    ((GameTransaction) transaction).markEventAsCancelledIfNecessary(eventWithTransactions.event);
                }
                if (!transaction.cancelled) {
                    ((GameTransaction) transaction).postProcessEvent(context, event);
                }
            }
        }
        if (cancelledAny) {
            for (final EventByTransaction<@NonNull ?> eventByTransaction : batched.reverse()) {
                if (eventByTransaction.decider.cancelled) {
                    ((GameTransaction) eventByTransaction.decider).markEventAsCancelledIfNecessary(eventByTransaction.event);
                }
                for (final GameTransaction<@NonNull ?> gameTransaction : eventByTransaction.transactions.reverse()) {
                    if (gameTransaction.cancelled) {
                        gameTransaction.restore();
                    }
                }
            }
        }
        builder.build().asMap()
            .forEach((transactionType, events) -> transactionType.createAndProcessPostEvents(context, events));
        return !cancelledAny;
    }

    @SuppressWarnings("unchecked")
    static ImmutableList<EventByTransaction<@NonNull ?>> batchTransactions(
        final GameTransaction head,
        final GameTransaction parent,
        final PhaseContext<@NonNull ?> context,
        final ImmutableMultimap.Builder<TransactionType, ? extends Event> transactionPostEventBuilder
    ) {
        final ImmutableList.Builder<EventByTransaction<@NonNull ?>> builder = ImmutableList.builder();
        @Nullable GameTransaction pointer = head;
        ImmutableList.Builder<GameTransaction> accumilator = ImmutableList.builder();
        @MonotonicNonNull GameTransaction batchDecider = null;
        while (pointer != null) {
            if (batchDecider == null) {
                batchDecider = pointer;
            }
            if (batchDecider.shouldBuildEventAndRestartBatch(pointer, context)) {
                final ImmutableList<GameTransaction> transactions = accumilator.build();
                accumilator = ImmutableList.builder();
                TransactionalCaptureSupplier.generateEventForTransaction(
                    batchDecider,
                    parent,
                    context,
                    builder,
                    (ImmutableList) transactions,
                    transactionPostEventBuilder
                );
                accumilator.add(pointer);
                batchDecider = pointer;
                continue;
            } else if (pointer.hasAnyPrimaryChildrenTransactions() || pointer.isUnbatchable() || pointer.next == null) {
                accumilator.add(pointer);
                final ImmutableList<GameTransaction> transactions = accumilator.build();
                accumilator = ImmutableList.builder();
                batchDecider = pointer.next;
                TransactionalCaptureSupplier.generateEventForTransaction(
                    pointer,
                    parent,
                    context,
                    builder,
                    (ImmutableList) transactions,
                    transactionPostEventBuilder
                );
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
                (ImmutableList) remaining,
                transactionPostEventBuilder
            );
        }
        return builder.build();
    }

    @SuppressWarnings("unchecked")
    private static <E extends Event & Cancellable> void generateEventForTransaction(
        final @NonNull GameTransaction<E> pointer,
        final @Nullable GameTransaction<@NonNull ?> parent,
        final PhaseContext<@NonNull ?> context,
        final ImmutableList.Builder<EventByTransaction<@NonNull ?>> builder,
        final ImmutableList<GameTransaction<E>> transactions,
        final ImmutableMultimap.Builder<TransactionType, ? extends Event> transactionPostEventBuilder
    ) {
        final Optional<BiConsumer<PhaseContext<@NonNull ?>, CauseStackManager.StackFrame>> frameMutator = pointer.getFrameMutator(parent);
        final PhaseTracker instance = PhaseTracker.getInstance();
        try (
            final CauseStackManager.StackFrame frame = frameMutator
                .map(mutator -> {
                    final CauseStackManager.StackFrame transactionFrame = instance.pushCauseFrame();
                    mutator.accept(context, transactionFrame);
                    return transactionFrame;
                })
                .orElseGet(instance::pushCauseFrame)
        ) {
            final Optional<E> generatedEvent = pointer.generateEvent(context, parent, transactions, instance.currentCause());
            generatedEvent
                // It's not guaranteed that a transaction has a valid world or some other artifact,
                // and in those cases, we don't want to treat the transaction as being "cancellable"
                .ifPresent(e -> {
                    final EventByTransaction<E> element = new EventByTransaction<>(e, transactions, parent, pointer);
                    builder.add(element);
                    ((ImmutableMultimap.Builder) transactionPostEventBuilder).put(pointer.getTransactionType(), e);

                });

            for (final GameTransaction<E> transaction : transactions) {
                if (transaction.sideEffects == null || transaction.sideEffects.isEmpty()) {
                    continue;
                }
                generatedEvent.ifPresent(frame::pushCause);
                for (final ResultingTransactionBySideEffect sideEffect : transaction.sideEffects) {
                    if (sideEffect.head == null) {
                        continue;
                    }
                    builder.addAll(TransactionalCaptureSupplier.batchTransactions(sideEffect.head, pointer, context, transactionPostEventBuilder));
                }
            }
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.head);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        final TransactionalCaptureSupplier other = (TransactionalCaptureSupplier) obj;
        return Objects.equals(this.head, other.head);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", TransactionalCaptureSupplier.class.getSimpleName() + "[", "]")
            .add("tail=" + this.tail)
            .add("head=" + this.head)
            .add("effect=" + this.effect)
            .toString();
    }

    public void reset() {
        if (this.head != null) {
            this.head = null;
            this.tail = null;
        }
        if (this.effect != null) {
            this.effect = null;
        }
    }

}
