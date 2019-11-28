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

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.entity.player.InventoryPlayerBridge;
import org.spongepowered.common.bridge.inventory.TrackedInventoryBridge;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;

import java.util.List;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;

public final class SwapHandItemsState extends BasicInventoryPacketState {

    public ChangeInventoryEvent.SwapHand createInventoryEvent(final Inventory inventory, final List<SlotTransaction> slotTransactions) {
        return SpongeEventFactory.createChangeInventoryEventSwapHand(Sponge.getCauseStackManager().getCurrentCause(), inventory, slotTransactions);
    }

    @Override
    public void populateContext(final ServerPlayerEntity playerMP, final IPacket<?> packet, final InventoryPacketContext context) {
        ((TrackedInventoryBridge) playerMP.field_71071_by).bridge$setCaptureInventory(true);
    }

    @Override
    public void unwind(final InventoryPacketContext context) {
        final ServerPlayerEntity player = context.getPacketPlayer();
        final Entity spongePlayer = (Entity) player;
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(spongePlayer);
            final TrackedInventoryBridge mixinInventory = ((TrackedInventoryBridge) player.field_71071_by);
            final List<SlotTransaction> trans = mixinInventory.bridge$getCapturedSlotTransactions();
            final ChangeInventoryEvent.SwapHand swapItemEvent = this.createInventoryEvent(((Inventory) player.field_71071_by), trans);
            SpongeImpl.postEvent(swapItemEvent);
            PacketPhaseUtil.handleSlotRestore(player, null, swapItemEvent.getTransactions(), swapItemEvent.isCancelled());
            mixinInventory.bridge$setCaptureInventory(false);
            mixinInventory.bridge$getCapturedSlotTransactions().clear();
        }
    }
}
