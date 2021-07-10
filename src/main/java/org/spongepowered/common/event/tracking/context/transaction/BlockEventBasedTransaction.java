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
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.transaction.BlockTransaction;
import org.spongepowered.api.block.transaction.Operation;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeServer;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.context.transaction.type.TransactionTypes;
import org.spongepowered.math.vector.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

abstract class BlockEventBasedTransaction extends GameTransaction<ChangeBlockEvent.All> {

    final BlockPos affectedPosition;
    final BlockState originalState;

    BlockEventBasedTransaction(final BlockPos affectedPosition, final BlockState originalState, final ResourceKey worldKey) {
        super(TransactionTypes.BLOCK.get(), worldKey);
        this.affectedPosition = affectedPosition.immutable();
        this.originalState = originalState;
    }

    @Override
    public final Optional<ChangeBlockEvent.All> generateEvent(
        final PhaseContext<@NonNull ?> context,
        final @Nullable GameTransaction<@NonNull ?> parent,
        final ImmutableList<GameTransaction<ChangeBlockEvent.All>> transactions,
        final Cause currentCause
    ) {
        final Optional<ServerWorld> o = ((SpongeServer) SpongeCommon.server()).worldManager().world(this.worldKey);
        if (!o.isPresent()) {
            return Optional.empty();
        }
        final ListMultimap<BlockPos, SpongeBlockSnapshot> positions = LinkedListMultimap.create();
        for (final GameTransaction<@NonNull ?> transaction : transactions) {
            final BlockEventBasedTransaction blockTransaction = (BlockEventBasedTransaction) transaction;
            if (!positions.containsKey(blockTransaction.affectedPosition)) {
                positions.put(
                    blockTransaction.affectedPosition,
                    blockTransaction.getOriginalSnapshot()
                );
            }
            if (blockTransaction.getResultingSnapshot() != null) {
                positions.put(
                        blockTransaction.affectedPosition,
                        blockTransaction.getResultingSnapshot()
                );
            }
        }

        final ImmutableList<BlockTransaction> eventTransactions = positions.asMap().values()
            .stream()
            .map(spongeBlockSnapshots -> {
                final List<SpongeBlockSnapshot> snapshots = new ArrayList<>(spongeBlockSnapshots);
                if (snapshots.isEmpty() || snapshots.size() < 2) {
                    // Error case
                    return Optional.<BlockTransaction>empty();
                }
                final SpongeBlockSnapshot original = snapshots.get(0);
                final SpongeBlockSnapshot result = snapshots.get(snapshots.size() - 1);
                final ImmutableList<BlockSnapshot> intermediary;
                if (snapshots.size() > 2) {
                    intermediary = ImmutableList.copyOf(snapshots.subList(1, snapshots.size() - 2));
                } else {
                    intermediary = ImmutableList.of();
                }
                final Operation operation = context.getBlockOperation(original, result);
                final BlockTransaction eventTransaction = new BlockTransaction(original, result, intermediary, operation);
                return Optional.of(eventTransaction);
            })
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(ImmutableList.toImmutableList());

        if (eventTransactions.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(SpongeEventFactory.createChangeBlockEventAll(
            currentCause,
            eventTransactions,
            o.get()
        ));
    }

    protected abstract SpongeBlockSnapshot getResultingSnapshot();

    protected abstract SpongeBlockSnapshot getOriginalSnapshot();

    @Override
    public final boolean markCancelledTransactions(final ChangeBlockEvent.All event,
        final ImmutableList<? extends GameTransaction<ChangeBlockEvent.All>> blockTransactions
    ) {
        boolean cancelledAny = false;
        if (event.isCancelled()) {
            event.transactions().forEach(BlockTransaction::invalidate);
        }
        for (final Transaction<BlockSnapshot> transaction : event.transactions()) {
            if (!transaction.isValid()) {
                cancelledAny = true;
                for (final GameTransaction<ChangeBlockEvent.All> gameTransaction : blockTransactions) {
                    final BlockEventBasedTransaction blockTransaction = (BlockEventBasedTransaction) gameTransaction;
                    final Vector3i position = transaction.original().position();
                    final BlockPos affectedPosition = blockTransaction.affectedPosition;
                    if (position.x() == affectedPosition.getX()
                        && position.y() == affectedPosition.getY()
                        && position.z() == affectedPosition.getZ()
                    ) {
                        gameTransaction.markCancelled();
                    }
                }
            }
        }

        return cancelledAny;
    }

    @Override
    public void markEventAsCancelledIfNecessary(final ChangeBlockEvent.All event) {
        super.markEventAsCancelledIfNecessary(event);
        event.transactions().forEach(BlockTransaction::invalidate);
    }
}
