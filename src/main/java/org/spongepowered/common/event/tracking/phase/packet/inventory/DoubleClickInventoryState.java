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

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.common.bridge.inventory.ContainerBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.tracking.phase.packet.drag.DragInventoryStopState;
import org.spongepowered.common.util.Constants;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

public final class DoubleClickInventoryState extends BasicInventoryPacketState {

    public DoubleClickInventoryState() {
        super(Constants.Networking.MODE_DOUBLE_CLICK | Constants.Networking.BUTTON_PRIMARY | Constants.Networking.BUTTON_SECONDARY, Constants.Networking.MASK_MODE | Constants.Networking.MASK_BUTTON);
    }

    @Override
    protected boolean shouldFire() {
        return ShouldFire.CLICK_INVENTORY_EVENT_DOUBLE;
    }

    @Override
    public ClickInventoryEvent createInventoryEvent(EntityPlayerMP playerMP, Container openContainer, Transaction<ItemStackSnapshot> transaction,
            List<SlotTransaction> slotTransactions, List<Entity> capturedEntities, int usedButton, @Nullable Slot slot) {
        return SpongeEventFactory.createClickInventoryEventDouble(Sponge.getCauseStackManager().getCurrentCause(), transaction,
                Optional.ofNullable(slot), openContainer, slotTransactions);
    }

    @Override
    public void populateContext(EntityPlayerMP playerMP, Packet<?> packet, InventoryPacketContext context) {
        super.populateContext(playerMP, packet, context);
        ((ContainerBridge) playerMP.field_71070_bA).bridge$setFirePreview(false);
    }

    @Override
    public void unwind(InventoryPacketContext context) {
        DragInventoryStopState.unwindCraftPreview(context);
        super.unwind(context);
    }

}
