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
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.context.transaction.type.TransactionTypes;
import org.spongepowered.common.util.PrettyPrinter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

abstract class InventoryBasedTransaction extends GameTransaction<ChangeInventoryEvent> {

    final Inventory inventory;
    @MonotonicNonNull private List<SlotTransaction> acceptedTransactions;
    protected boolean used = false;

    protected InventoryBasedTransaction(
        final ResourceKey worldKey,
        final Inventory inventory
    ) {
        super(TransactionTypes.CHANGE_INVENTORY_EVENT.get(), worldKey);
        this.inventory = inventory;
    }

    @Override
    public Optional<BiConsumer<PhaseContext<@NonNull ?>, CauseStackManager.StackFrame>> getFrameMutator(
        @Nullable final GameTransaction<@NonNull ?> parent
    ) {
        return Optional.of((context, frame) -> {
            frame.pushCause(this.inventory);
        });
    }

    @Override
    public Optional<ChangeInventoryEvent> generateEvent(
        final PhaseContext<@NonNull ?> context, @Nullable final GameTransaction<@NonNull ?> parent,
        final ImmutableList<GameTransaction<ChangeInventoryEvent>> gameTransactions, final Cause currentCause
    ) {
        final ImmutableList<InventoryBasedTransaction> containerBasedTransactions = gameTransactions.stream()
            .filter(tx -> tx instanceof InventoryBasedTransaction)
            .map(tx -> (InventoryBasedTransaction) tx)
            .filter(tx -> !tx.used)
            .collect(ImmutableList.toImmutableList());

        final List<SlotTransaction> slotTransactions = containerBasedTransactions
            .stream()
            .map(InventoryBasedTransaction::getSlotTransactions)
            .flatMap(List::stream)
            .collect(Collectors.toList());
        for (InventoryBasedTransaction transaction : containerBasedTransactions) {
            transaction.used = true;
        }
        return containerBasedTransactions.stream()
            .map(t -> t.createInventoryEvent(slotTransactions, context, currentCause))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
    }

    List<SlotTransaction> getSlotTransactions() {
        return this.acceptedTransactions == null ? Collections.emptyList() : this.acceptedTransactions;
    }

    Optional<ChangeInventoryEvent> createInventoryEvent(
        final List<SlotTransaction> slotTransactions,
        final PhaseContext<@NonNull ?> context,
        final Cause currentCause
    ) {
        return Optional.empty();
    }

    @Override
    public void restore(
        final PhaseContext<@NonNull ?> context,
        final ChangeInventoryEvent event
    ) {

    }

    @Override
    public boolean markCancelledTransactions(
        final ChangeInventoryEvent event,
        final ImmutableList<? extends GameTransaction<ChangeInventoryEvent>> gameTransactions
    ) {
        if (event.isCancelled()) {
            event.transactions().forEach(SlotTransaction::invalidate);
            return true;
        }
        boolean cancelledAny = false;
        for (final SlotTransaction transaction : event.transactions()) {
            if (!transaction.isValid()) {
                cancelledAny = true;
                for (final GameTransaction<ChangeInventoryEvent> gameTransaction : gameTransactions) {
                    ((InventoryBasedTransaction) gameTransaction).getSlotTransactions()
                        .forEach(tx -> {
                            if (tx == transaction) {
                                gameTransaction.markCancelled();
                            }
                        });
                }
            }
        }
        return cancelledAny;
    }

    @Override
    public boolean acceptSlotTransaction(final SlotTransaction newTransaction, final Object inventory) {
        if (this.inventory == inventory) {
            if (this.acceptedTransactions == null) {
                this.acceptedTransactions = new ArrayList<>();
            }
            this.acceptedTransactions.add(newTransaction);
            return true;
        }
        return false;
    }


    @Override
    public void addToPrinter(final PrettyPrinter printer) {

    }

}
