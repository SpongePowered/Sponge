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
package org.spongepowered.common.event.tracking.context.transaction.type;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.transaction.BlockTransaction;
import org.spongepowered.api.block.transaction.BlockTransactionReceipt;
import org.spongepowered.api.block.transaction.Operation;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeServer;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public final class BlockTransactionType extends TransactionType<ChangeBlockEvent.All> {

    public BlockTransactionType() {
        super(true, "BLOCK");
    }

    @Override
    protected void consumeEventsAndMarker(
        PhaseContext<@NonNull ?> context,
        final Collection<? extends ChangeBlockEvent.All> changeBlockEvents
    ) {

        final Multimap<ResourceKey, ChangeBlockEvent.All> eventsByWorld = LinkedListMultimap.create();
        changeBlockEvents.forEach(event -> eventsByWorld.put(event.world().key(), event));

        eventsByWorld.asMap().forEach((key, events) -> {
            final Optional<ServerWorld> serverWorld = ((SpongeServer) SpongeCommon.server()).worldManager().world(key);
            if (!serverWorld.isPresent()) {
                return;
            }
            final ListMultimap<BlockPos, SpongeBlockSnapshot> positions = LinkedListMultimap.create();
            // Gather transactions that were valid
            events.stream()
                .filter(event -> !event.isCancelled())
                .flatMap(event -> event.transactions().stream())
                .filter(BlockTransaction::isValid)
                .forEach(transactions -> {
                    // Then "put" the most recent transactions such that we have a complete rebuild of
                    // each position according to what originally existed and then
                    // the ultimate final block on that position
                    final SpongeBlockSnapshot original = (SpongeBlockSnapshot) transactions.original();
                    positions.put(original.getBlockPos(), original);
                    positions.put(original.getBlockPos(), (SpongeBlockSnapshot) transactions.finalReplacement());
                });

            // Do not bother turning the positions into receipts if it's empty
            // just return.
            if (positions.isEmpty()) {
                return;
            }
            final ImmutableList<BlockTransactionReceipt> transactions = positions.asMap()
                .values()
                .stream()
                .map(spongeBlockSnapshots -> {
                    final List<SpongeBlockSnapshot> snapshots = new ArrayList<>(spongeBlockSnapshots);
                    if (snapshots.isEmpty() || snapshots.size() < 2) {
                        // Error case
                        return Optional.<BlockTransactionReceipt>empty();
                    }
                    final SpongeBlockSnapshot original = snapshots.get(0);
                    final SpongeBlockSnapshot result = snapshots.get(snapshots.size() - 1);
                    final Operation operation = context.getBlockOperation(original, result);
                    final BlockTransactionReceipt eventTransaction = new BlockTransactionReceipt(original, result, operation);
                    context.postBlockTransactionApplication(original.blockChange, eventTransaction);
                    return Optional.of(eventTransaction);
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(ImmutableList.toImmutableList());

            final Cause cause = PhaseTracker.getInstance().currentCause();

            SpongeCommon.post(SpongeEventFactory.createChangeBlockEventPost(cause, transactions, serverWorld.get()));
        });
    }
}
