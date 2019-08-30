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

import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.bridge.inventory.ContainerBridge;
import org.spongepowered.common.bridge.inventory.TrackedInventoryBridge;
import org.spongepowered.common.event.tracking.phase.packet.PacketContext;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhase;
import org.spongepowered.common.event.tracking.phase.packet.PacketState;

public class InventoryPacketContext extends PacketContext<InventoryPacketContext> {

    private int oldHighlightedSlotId;

    public InventoryPacketContext(final PacketState<? extends InventoryPacketContext> state) {
        super(state);
    }

    public int getOldHighlightedSlotId() {
        return this.oldHighlightedSlotId;
    }

    public InventoryPacketContext setOldHighlightedSlot(final int highlightedSlotId) {
        this.oldHighlightedSlotId = highlightedSlotId;
        return this;
    }

    @Override
    public boolean hasCaptures() {
        if (!((ContainerBridge) this.packetPlayer.openContainer).bridge$getPreviewTransactions().isEmpty()) {
            return true;
        }
        if (!((TrackedInventoryBridge) this.packetPlayer.openContainer).bridge$getCapturedSlotTransactions().isEmpty()) {
            return true;
        }
        if (this.state == PacketPhase.Inventory.DROP_ITEMS) {
            return true;
        }
        if (this.state == PacketPhase.Inventory.DROP_ITEM_OUTSIDE_WINDOW) {
            return true;
        }
        if (this.state == PacketPhase.Inventory.OPEN_INVENTORY) {
            return true;
        }
        if (this.state == PacketPhase.Inventory.PLACE_RECIPE) {
            return true;
        }
        if (this.state == PacketPhase.Inventory.SWAP_HAND_ITEMS) {
            return true;
        }
        if (this.state == PacketPhase.Inventory.SWITCH_HOTBAR_SCROLL) {
            return true;
        }
        // Fire events even without captures
        if (this.state == PacketPhase.Inventory.PRIMARY_INVENTORY_CLICK
         || this.state == PacketPhase.Inventory.SECONDARY_INVENTORY_CLICK
         || this.state == PacketPhase.Inventory.MIDDLE_INVENTORY_CLICK
         || this.state == PacketPhase.Inventory.DROP_ITEM_WITH_HOTKEY
         || this.state == PacketPhase.Inventory.PRIMARY_INVENTORY_SHIFT_CLICK
         || this.state == PacketPhase.Inventory.SECONDARY_INVENTORY_SHIFT_CLICK
         || this.state == PacketPhase.Inventory.SWITCH_HOTBAR_NUMBER_PRESS
         || this.state == PacketPhase.Inventory.DROP_ITEM_OUTSIDE_WINDOW_NOOP
        ) {
            return true;
        }
        ((TrackedInventoryBridge) this.packetPlayer.openContainer).bridge$setCaptureInventory(false);

        return super.hasCaptures();
    }

    @Override
    public PrettyPrinter printCustom(final PrettyPrinter printer, final int indent) {
        final String s = String.format("%1$" + indent + "s", "");
        return super.printCustom(printer, indent)
            .add(s + "- %s: %s", "HighlightedSlotId", this.oldHighlightedSlotId);
    }

    @Override
    protected void reset() {
        super.reset();
        this.oldHighlightedSlotId = 0;
    }
}
