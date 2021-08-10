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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.util.Constants;

import java.util.List;
import java.util.Optional;

public final class DropItemWithHotkeyState extends BasicInventoryPacketState {

    public DropItemWithHotkeyState() {
        super(Constants.Networking.MODE_DROP | Constants.Networking.BUTTON_PRIMARY | Constants.Networking.BUTTON_SECONDARY | Constants.Networking.CLICK_INSIDE_WINDOW);
    }

    @Override
    public ClickContainerEvent.Drop createContainerEvent(
        final InventoryPacketContext context, final Cause cause,
        final net.minecraft.server.level.ServerPlayer serverPlayer,
        final Container openContainer, final Transaction<ItemStackSnapshot> transaction,
        final List<SlotTransaction> slotTransactions, final List<Entity> capturedEntities, final int usedButton,
        final @Nullable Slot slot
    ) {
        TrackingUtil.setCreatorReference(capturedEntities, serverPlayer);

        // A 'primary click' is used by the game to indicate a single drop (e.g. pressing 'q' without holding 'control')
        final ClickContainerEvent.Drop event;
        if (usedButton == Constants.Networking.PACKET_BUTTON_PRIMARY_ID) {
            event = SpongeEventFactory.createClickContainerEventDropSingle(cause,
                openContainer, transaction, capturedEntities, Optional.ofNullable(slot), slotTransactions);
        } else {
            event = SpongeEventFactory.createClickContainerEventDropFull(cause,
                openContainer, transaction, capturedEntities, Optional.ofNullable(slot), slotTransactions);
        }
        return event;

    }

    @Override
    public boolean doesContainerCaptureEntitySpawn(final InventoryPacketContext context, final net.minecraft.world.entity.Entity entityIn) {
        return true;
    }
}
