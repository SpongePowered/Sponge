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
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKey;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.world.BlockChange;
import org.spongepowered.math.vector.Vector3i;

import java.util.List;

abstract class BlockEventBasedTransaction extends GameTransaction<ChangeBlockEvent> {

    public static final int EVENT_COUNT = BlockChange.values().length + 1;
    // State definitions
    final BlockPos affectedPosition;
    final BlockState originalState;

    BlockEventBasedTransaction(final BlockPos affectedPosition, final BlockState originalState) {
        this.affectedPosition = affectedPosition;
        this.originalState = originalState;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public final ChangeBlockEvent generateEvent(final PhaseContext<@NonNull ?> context,
        final ImmutableList<GameTransaction<ChangeBlockEvent>> transactions,
        final Cause currentCause
    ) {
        final ListMultimap<BlockPos, SpongeBlockSnapshot> positions = LinkedListMultimap.create();
        for (final GameTransaction<@NonNull ?> transaction : transactions) {
            final BlockEventBasedTransaction blockTransaction = (BlockEventBasedTransaction) transaction;
            if (!positions.containsKey(blockTransaction.affectedPosition)) {
                positions.put(
                    blockTransaction.affectedPosition,
                    blockTransaction.getOriginalSnapshot()
                );
            }
            positions.put(
                blockTransaction.affectedPosition,
                blockTransaction.getResultingSnapshot()
            );
        }

        final ImmutableList<Transaction<BlockSnapshot>>[] transactionArrays = new ImmutableList[BlockEventBasedTransaction.EVENT_COUNT];
        final ImmutableList.Builder<Transaction<BlockSnapshot>>[] transactionBuilders = new ImmutableList.Builder[BlockEventBasedTransaction.EVENT_COUNT];
        for (int i = 0; i < BlockEventBasedTransaction.EVENT_COUNT; i++) {
            transactionBuilders[i] = new ImmutableList.Builder<>();
        }
        // Bug is here- use the multimap
        final ImmutableList<Transaction<BlockSnapshot>> eventTransactions = transactions.stream()
            .map(transaction -> (BlockEventBasedTransaction) transaction)
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
        for (int i = 0; i < BlockEventBasedTransaction.EVENT_COUNT; i++) {
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
    public boolean canBatchWith(final @Nullable GameTransaction<@NonNull ?> next) {
        return next instanceof org.spongepowered.common.event.tracking.context.transaction.BlockEventBasedTransaction;
    }

    @Override
    public final boolean markCancelledTransactions(final ChangeBlockEvent event,
        final ImmutableList<? extends GameTransaction<ChangeBlockEvent>> blockTransactions
    ) {
        boolean cancelledAny = false;
        for (final Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            if (!transaction.isValid()) {
                cancelledAny = true;
                for (final GameTransaction<ChangeBlockEvent> gameTransaction : blockTransactions) {
                    final BlockEventBasedTransaction blockTransaction = (BlockEventBasedTransaction) gameTransaction;
                    final Vector3i position = transaction.getOriginal().getPosition();
                    final BlockPos affectedPosition = blockTransaction.affectedPosition;
                    if (position.getX() == affectedPosition.getX()
                        && position.getY() == affectedPosition.getY()
                        && position.getZ() == affectedPosition.getZ()
                    ) {
                        gameTransaction.markCancelled();
                    }
                }
            }
        }

        return cancelledAny;
    }
}
