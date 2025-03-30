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
package org.spongepowered.common.event.tracking.context.transaction.inventory;

import com.google.common.collect.ImmutableList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.item.inventory.container.InteractContainerEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.context.transaction.GameTransaction;
import org.spongepowered.common.event.tracking.context.transaction.type.TransactionTypes;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.PrettyPrinter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class CloseMenuTransaction extends MenuBasedTransaction<InteractContainerEvent.Close.Post> {

    private final ServerPlayer player;
    private final ItemStackSnapshot cursor;
    private @MonotonicNonNull List<SlotTransaction> slotTransactions;

    public CloseMenuTransaction(final Player player) {
        super(TransactionTypes.INTERACT_CONTAINER_EVENT.get(), player.containerMenu);
        this.player = (ServerPlayer) player;
        this.cursor = ItemStackUtil.snapshotOf(player.containerMenu.getCarried());
    }

    @Override
    public Optional<BiConsumer<PhaseContext<@NonNull ?>, CauseStackManager.StackFrame>> getFrameMutator(
            @Nullable final GameTransaction<@NonNull ?> parent
    ) {
        return Optional.empty();
    }

    @Override
    public Optional<InteractContainerEvent.Close.Post> generateEvent(
        final PhaseContext<@NonNull ?> context, @Nullable final GameTransaction<@NonNull ?> parent,
        final ImmutableList<GameTransaction<InteractContainerEvent.Close.Post>> gameTransactions, final Cause currentCause
    ) {
        final ItemStackSnapshot resultingCursor = ItemStackUtil.snapshotOf(this.player.containerMenu.getCarried());
        final Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(this.cursor, resultingCursor);
        final InteractContainerEvent.Close.Post event = SpongeEventFactory.createInteractContainerEventClosePost(currentCause, (Container) this.menu,
                cursorTransaction, (Container) this.menu, this.slotTransactions == null ? Collections.emptyList() : this.slotTransactions);
        return Optional.of(event);
    }

    @Override
    public void restore(final PhaseContext<@NonNull ?> context, final InteractContainerEvent.Close.Post event) {
        PacketPhaseUtil.handleCursorRestore(this.player, event.cursorTransaction(), event.isCancelled());
        PacketPhaseUtil.handleSlotRestore(this.player, this.menu, event.transactions(), event.isCancelled());

        this.player.inventoryMenu.broadcastChanges(); // prevent double capture
    }

    @Override
    public boolean absorbSlotTransaction(
        final ContainerSlotTransaction slotTransaction
    ) {
        if (this.menu != slotTransaction.menu) {
            return this.player.inventoryMenu == slotTransaction.menu;
        }
        if (this.slotTransactions == null) {
            this.slotTransactions = new ArrayList<>();
        }
        this.slotTransactions.add(slotTransaction.transaction);
        return true;
    }

    @Override
    public void postProcessEvent(final PhaseContext<@NonNull ?> context, final InteractContainerEvent.Close.Post event) {
        // And restore cursor if needed
        PacketPhaseUtil.handleCursorRestore(this.player, event.cursorTransaction(), event.isCancelled());
        PacketPhaseUtil.handleSlotRestore(this.player, this.menu, event.transactions(), event.isCancelled());

        this.player.inventoryMenu.broadcastChanges(); // prevent double capture
    }

    @Override
    public boolean markCancelledTransactions(final InteractContainerEvent.Close.Post event,
            final ImmutableList<? extends GameTransaction<InteractContainerEvent.Close.Post>> gameTransactions) {
        if (event.isCancelled()) {
            event.cursorTransaction().invalidate();
            return true;
        }
        return !event.cursorTransaction().isValid();
    }

    @Override
    public void addToPrinter(final PrettyPrinter printer) {

    }
}
