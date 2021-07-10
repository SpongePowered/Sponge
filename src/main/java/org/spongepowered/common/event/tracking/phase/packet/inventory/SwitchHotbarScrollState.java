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
package org.spongepowered.common.event.tracking.phase.packet.inventory;

import com.google.common.collect.ImmutableList;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.item.util.ItemStackUtil;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import java.util.List;
import java.util.Optional;

public final class SwitchHotbarScrollState extends BasicInventoryPacketState {

    @Override
    public void populateContext(final ServerPlayer playerMP, final Packet<?> packet,
        final InventoryPacketContext context) {
        super.populateContext(playerMP, packet, context);
        context.setOldHighlightedSlot(playerMP.inventory.selected);
    }

    @Override
    public ClickContainerEvent createInventoryEvent(final ServerPlayer playerMP, final Container openContainer,
        final Transaction<ItemStackSnapshot> transaction, final List<SlotTransaction> slotTransactions,
        final List<Entity> capturedEntities,
        final int usedButton, final org.spongepowered.api.item.inventory.@Nullable Slot slot) {
        return SpongeEventFactory.createClickContainerEventNumberPress(
            PhaseTracker.getCauseStackManager().currentCause(),
            openContainer, transaction,
            Optional.ofNullable(slot), slotTransactions, usedButton);
    }

    @Override
    public void unwind(final InventoryPacketContext context) {

        final ServerPlayer player = context.getPacketPlayer();
        final ServerboundSetCarriedItemPacket itemChange = context.getPacket();
        final int previousSlot = context.getOldHighlightedSlotId();
        final net.minecraft.world.inventory.AbstractContainerMenu inventoryContainer = player.containerMenu;
        final net.minecraft.world.entity.player.Inventory inventory = player.inventory;
        final int preHotbarSize = inventory.items.size() - net.minecraft.world.entity.player.Inventory.getSelectionSize() + inventory.armor.size() + 4 + 1; // Crafting Grid & Result
        final Slot sourceSlot = inventoryContainer.getSlot(previousSlot + preHotbarSize);
        final Slot targetSlot = inventoryContainer.getSlot(itemChange.getSlot() + preHotbarSize);

        final ItemStackSnapshot sourceSnapshot = ItemStackUtil.snapshotOf(sourceSlot.getItem());
        final ItemStackSnapshot targetSnapshot = ItemStackUtil.snapshotOf(targetSlot.getItem());
        final org.spongepowered.api.item.inventory.Slot slotPrev = (org.spongepowered.api.item.inventory.Slot) inventoryContainer.getSlot(previousSlot + preHotbarSize);
        final SlotTransaction sourceTransaction = new SlotTransaction(slotPrev, sourceSnapshot, sourceSnapshot);
        final org.spongepowered.api.item.inventory.Slot slotNew = (org.spongepowered.api.item.inventory.Slot) inventoryContainer.getSlot(itemChange.getSlot() + preHotbarSize);
        final SlotTransaction targetTransaction = new SlotTransaction(slotNew, targetSnapshot, targetSnapshot);
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(player);
            final ImmutableList<SlotTransaction> transactions =
                new ImmutableList.Builder<SlotTransaction>().add(sourceTransaction).add(targetTransaction).build();
            final ChangeInventoryEvent.Held changeInventoryEventHeld = SpongeEventFactory
                .createChangeInventoryEventHeld(frame.currentCause(), slotNew, (Inventory) inventoryContainer,
                    slotPrev, transactions);
            final net.minecraft.world.inventory.AbstractContainerMenu openContainer = player.containerMenu;
            SpongeCommon.post(changeInventoryEventHeld);
            if (changeInventoryEventHeld.isCancelled() || PacketPhaseUtil.allTransactionsInvalid(
                changeInventoryEventHeld.transactions())) {
                player.connection.send(new ClientboundSetCarriedItemPacket(previousSlot));
                inventory.selected = previousSlot;
            } else {
                PacketPhaseUtil.handleSlotRestore(player, openContainer, changeInventoryEventHeld.transactions(),
                    false);
                inventory.selected = itemChange.getSlot();
                player.resetLastActionTime();
            }
            ((TrackedInventoryBridge) openContainer).bridge$setCaptureInventory(false);
        }
    }
}
