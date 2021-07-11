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
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.common.event.tracking.context.transaction.TransactionalCaptureSupplier;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.List;

public final class SwitchHotbarScrollState extends BasicInventoryPacketState {

    @Override
    public void populateContext(final ServerPlayer playerMP, final Packet<?> packet,
        final InventoryPacketContext context) {
        context.setOldHighlightedSlot(playerMP.inventory.selected);

        final TransactionalCaptureSupplier transactor = context.getTransactor();
        transactor.logPlayerInventoryChange(playerMP);
    }

    @Override
    public ChangeInventoryEvent createInventoryEvent(final InventoryPacketContext context, final Cause cause, final Inventory inventory,
            final List<SlotTransaction> slotTransactions, final List<Entity> capturedEntities) {
        final ServerPlayer player = context.getPacketPlayer();
        final ServerboundSetCarriedItemPacket itemChange = context.getPacket();

        final int previousSlot = context.getOldHighlightedSlotId();
        final net.minecraft.world.inventory.AbstractContainerMenu inventoryContainer = player.containerMenu;
        final int preHotbarSize = player.inventory.items.size() - net.minecraft.world.entity.player.Inventory.getSelectionSize() + inventory.armor.size() + 4 + 1; // Crafting Grid & Result
        final Slot sourceSlot = inventoryContainer.getSlot(previousSlot + preHotbarSize);
        final Slot targetSlot = inventoryContainer.getSlot(itemChange.getSlot() + preHotbarSize);

        final ItemStackSnapshot sourceSnapshot = ItemStackUtil.snapshotOf(sourceSlot.getItem());
        final ItemStackSnapshot targetSnapshot = ItemStackUtil.snapshotOf(targetSlot.getItem());
        final org.spongepowered.api.item.inventory.Slot slotPrev = (org.spongepowered.api.item.inventory.Slot) sourceSlot;
        final SlotTransaction sourceTransaction = new SlotTransaction(slotPrev, sourceSnapshot, sourceSnapshot);
        final org.spongepowered.api.item.inventory.Slot slotNew = (org.spongepowered.api.item.inventory.Slot) targetSlot;
        final SlotTransaction targetTransaction = new SlotTransaction(slotNew, targetSnapshot, targetSnapshot);
        // TODO generated slot transactions
        slotTransactions = new ImmutableList.Builder<SlotTransaction>().add(sourceTransaction).add(targetTransaction).build();

        final ChangeInventoryEvent.Held event =
                SpongeEventFactory.createChangeInventoryEventHeld(cause, slotNew, (Inventory) inventoryContainer, slotPrev, slotTransactions);
        return event;
    }

}
