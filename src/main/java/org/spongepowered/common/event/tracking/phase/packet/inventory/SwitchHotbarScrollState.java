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
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.server.SPacketHeldItemChange;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.inventory.TrackedInventoryBridge;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.item.inventory.util.ContainerUtil;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

public final class SwitchHotbarScrollState extends BasicInventoryPacketState {

    @Override
    public void populateContext(final EntityPlayerMP playerMP, final Packet<?> packet, final InventoryPacketContext context) {
        super.populateContext(playerMP, packet, context);
        context.setOldHighlightedSlot(playerMP.field_71071_by.field_70461_c);
    }

    @Override
    public ClickInventoryEvent createInventoryEvent(final EntityPlayerMP playerMP, final Container openContainer,
        final Transaction<ItemStackSnapshot> transaction, final List<SlotTransaction> slotTransactions, final List<Entity> capturedEntities,
        final int usedButton, @Nullable final org.spongepowered.api.item.inventory.Slot slot) {
        return SpongeEventFactory.createClickInventoryEventNumberPress(Sponge.getCauseStackManager().getCurrentCause(), transaction,
                Optional.ofNullable(slot), openContainer, slotTransactions, usedButton);
    }

    @Override
    public void unwind(final InventoryPacketContext context) {

        final EntityPlayerMP player = context.getPacketPlayer();
        final CPacketHeldItemChange itemChange = context.getPacket();
        final int previousSlot = context.getOldHighlightedSlotId();
        final net.minecraft.inventory.Container inventoryContainer = player.field_71069_bz;
        final InventoryPlayer inventory = player.field_71071_by;
        final int preHotbarSize = inventory.field_70462_a.size() - InventoryPlayer.func_70451_h() + inventory.field_70460_b.size() + 4 + 1; // Crafting Grid & Result
        final Slot sourceSlot = inventoryContainer.func_75139_a(previousSlot + preHotbarSize);
        final Slot targetSlot = inventoryContainer.func_75139_a(itemChange.func_149614_c() + preHotbarSize);

        final ItemStackSnapshot sourceSnapshot = ItemStackUtil.snapshotOf(sourceSlot.func_75211_c());
        final ItemStackSnapshot targetSnapshot = ItemStackUtil.snapshotOf(targetSlot.func_75211_c());
        final org.spongepowered.api.item.inventory.Slot slotPrev = ContainerUtil.getSlot(inventoryContainer, previousSlot + preHotbarSize);
        final SlotTransaction sourceTransaction = new SlotTransaction(slotPrev, sourceSnapshot, sourceSnapshot);
        final org.spongepowered.api.item.inventory.Slot slotNew = ContainerUtil.getSlot(inventoryContainer, itemChange.func_149614_c() + preHotbarSize);
        final SlotTransaction targetTransaction = new SlotTransaction(slotNew, targetSnapshot, targetSnapshot);
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(player);
            final ImmutableList<SlotTransaction> transactions =
                new ImmutableList.Builder<SlotTransaction>().add(sourceTransaction).add(targetTransaction).build();
            final ChangeInventoryEvent.Held changeInventoryEventHeld = SpongeEventFactory
                .createChangeInventoryEventHeld(frame.getCurrentCause(), slotNew, slotPrev, (Inventory) inventoryContainer, transactions);
            final net.minecraft.inventory.Container openContainer = player.field_71070_bA;
            SpongeImpl.postEvent(changeInventoryEventHeld);
            if (changeInventoryEventHeld.isCancelled() || PacketPhaseUtil.allTransactionsInvalid(changeInventoryEventHeld.getTransactions())) {
                player.field_71135_a.func_147359_a(new SPacketHeldItemChange(previousSlot));
                inventory.field_70461_c = previousSlot;
            } else {
                PacketPhaseUtil.handleSlotRestore(player, openContainer, changeInventoryEventHeld.getTransactions(), false);
                inventory.field_70461_c = itemChange.func_149614_c();
                player.func_143004_u();
            }
            ((TrackedInventoryBridge) openContainer).bridge$setCaptureInventory(false);
        }
    }
}
