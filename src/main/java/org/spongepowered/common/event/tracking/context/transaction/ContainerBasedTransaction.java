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
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.item.inventory.CraftItemEvent;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.crafting.CraftingOutput;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.context.transaction.type.TransactionTypes;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.PrettyPrinter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

abstract class ContainerBasedTransaction extends GameTransaction<ClickContainerEvent> {
    private static Set<Class<?>> containersFailedCapture = new ReferenceOpenHashSet<>();

    final AbstractContainerMenu menu;
    @MonotonicNonNull List<net.minecraft.world.entity.Entity> entities;

    protected ContainerBasedTransaction(
        final ResourceKey worldKey,
        final AbstractContainerMenu menu
    ) {
        super(TransactionTypes.CLICK_CONTAINER_EVENT.get(), worldKey);
        this.menu = menu;
    }


    @Override
    public Optional<BiConsumer<PhaseContext<@NonNull ?>, CauseStackManager.StackFrame>> getFrameMutator(
        @Nullable final GameTransaction<@NonNull ?> parent
    ) {
        return Optional.of((context, frame) -> {
            frame.pushCause(this.menu);
        });
    }

    @Override
    public boolean acceptEntitySpawn(
        final PhaseContext<@NonNull ?> current, final net.minecraft.world.entity.Entity entityIn
    ) {
        if (current.doesContainerCaptureEntitySpawn(entityIn)) {
            if (this.entities == null) {
                this.entities = new LinkedList<>();
            }
            this.entities.add(entityIn);
        }
        return super.acceptEntitySpawn(current, entityIn);
    }

    @Override
    public Optional<ClickContainerEvent> generateEvent(
        final PhaseContext<@NonNull ?> context, @Nullable final GameTransaction<@NonNull ?> parent,
        final ImmutableList<GameTransaction<ClickContainerEvent>> gameTransactions, final Cause currentCause
    ) {
        final ImmutableList<ContainerBasedTransaction> containerBasedTransactions = gameTransactions.stream()
            .filter(tx -> tx instanceof ContainerBasedTransaction)
            .map(tx -> (ContainerBasedTransaction) tx).collect(ImmutableList.toImmutableList());
        if (containerBasedTransactions.stream().map(c -> c.isContainerEventAllowed(context))
            .filter(b -> !b)
            .findAny()
            .orElse(false)) {
            return Optional.empty();
        }
        // todo - detect !((TrackedContainerBridge) this.menu).isCapturePossible
//        if (ContainerBasedTransaction.containersFailedCapture.add(this.menu.getClass())) {
//            SpongeCommon.logger()
//                .warn("Changes in modded Container were not captured. Inventory events will not fire for this. Container: " + this.menu.getClass());
//        }
        final ImmutableList<Entity> entities = containerBasedTransactions.stream()
            .map(ContainerBasedTransaction::getEntitiesSpawned)
            .flatMap(List::stream)
            .collect(ImmutableList.toImmutableList());

        final List<SlotTransaction> slotTransactions = containerBasedTransactions
            .stream()
            .map(ContainerBasedTransaction::getSlotTransaction)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());

        return containerBasedTransactions.stream()
            .map(t -> t.createInventoryEvent(slotTransactions, entities, context, currentCause))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
    }

    abstract Optional<SlotTransaction> getSlotTransaction();

    @SuppressWarnings({"unchecked", "rawtypes"})
    List<Entity> getEntitiesSpawned() {
        return this.entities == null ? Collections.emptyList() : (List<Entity>) (List) this.entities;
    }

    boolean isContainerEventAllowed(final PhaseContext<@NonNull ?> context) {
        return true;
    }

    Optional<ClickContainerEvent> createInventoryEvent(
        final List<SlotTransaction> slotTransactions,
        final ImmutableList<Entity> entities,
        final PhaseContext<@NonNull ?> context,
        final Cause currentCause
    ) {
        return Optional.empty();
    }

    @Override
    public void restore(
        final PhaseContext<@NonNull ?> context,
        final ClickContainerEvent event
    ) {

    }

    @Override
    public boolean markCancelledTransactions(
        final ClickContainerEvent event,
        final ImmutableList<? extends GameTransaction<ClickContainerEvent>> gameTransactions
    ) {
        if (event.isCancelled()) {
            event.transactions().forEach(SlotTransaction::invalidate);
            event.cursorTransaction().invalidate();
            if (event instanceof CraftItemEvent.Preview) {
                ((CraftItemEvent.Preview) event).preview().invalidate();
            }
            return true;
        }
        boolean cancelledAny = false;
        for (final SlotTransaction transaction : event.transactions()) {
            if (!transaction.isValid()) {
                cancelledAny = true;
                for (final GameTransaction<ClickContainerEvent> gameTransaction : gameTransactions) {
                    ((ContainerBasedTransaction) gameTransaction).getSlotTransaction()
                        .ifPresent(tx -> {
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
    public boolean acceptSlotTransaction(
        final SlotTransaction newTransaction, final Object menu
    ) {
        if (menu instanceof AbstractContainerMenu) {
            // TODO
        }
        return super.acceptSlotTransaction(newTransaction, menu);
    }

    protected void handleEventResults(Player player, ClickContainerEvent event) {
        PacketPhaseUtil.handleSlotRestore(player, this.menu, event.transactions(), event.isCancelled());
        PacketPhaseUtil.handleCursorRestore(player, event.cursorTransaction());
        if (event.isCancelled() && event instanceof SpawnEntityEvent) {
            ((SpawnEntityEvent) event).entities().forEach(e ->
                    ((ServerLevel) e.world()).despawn((net.minecraft.world.entity.Entity) e));
        }

        if (player instanceof ServerPlayer && event instanceof CraftItemEvent.Preview) {
            final SlotTransaction preview = ((CraftItemEvent.Preview) event).preview();
            // Resend modified output if needed
            if (!preview.isValid()) {
                ((ServerPlayer) player).connection.send(new ClientboundContainerSetSlotPacket(0, 0,
                        ItemStackUtil.fromSnapshotToNative(((CraftItemEvent.Preview) event).preview().original())));
            } else if (preview.custom().isPresent()) {
                ((ServerPlayer) player).connection.send(new ClientboundContainerSetSlotPacket(0, 0,
                        ItemStackUtil.fromSnapshotToNative(((CraftItemEvent.Preview) event).preview().finalReplacement())));
            }
        }
    }

    protected SlotTransaction getPreviewTransaction(final CraftingOutput result, final List<SlotTransaction> slotTransactions) {
        SlotTransaction preview = null;
        for (SlotTransaction slotTransaction : slotTransactions) {
            if (result.equals(slotTransaction.slot())) {
                preview = slotTransaction; // get last transaction
            }
        }
        if (preview == null) {
            return new SlotTransaction(result, ItemStackSnapshot.empty(), ItemStackUtil.snapshotOf(result.peek()));
        }
        return preview;
    }

    @Override
    public void addToPrinter(final PrettyPrinter printer) {

    }

}
