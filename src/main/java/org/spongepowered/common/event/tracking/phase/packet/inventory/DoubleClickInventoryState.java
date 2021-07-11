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

import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.common.bridge.world.inventory.container.TrackedContainerBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.tracking.phase.packet.drag.DragInventoryStopState;
import org.spongepowered.common.util.Constants;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Optional;

public final class DoubleClickInventoryState extends BasicInventoryPacketState {

    public DoubleClickInventoryState() {
        super(
            Constants.Networking.MODE_DOUBLE_CLICK | Constants.Networking.BUTTON_PRIMARY | Constants.Networking.BUTTON_SECONDARY,
            Constants.Networking.MASK_MODE | Constants.Networking.MASK_BUTTON);
    }

    @Override
    protected boolean shouldFire() {
        return ShouldFire.CLICK_CONTAINER_EVENT_DOUBLE;
    }

    @Override
    public ClickContainerEvent createContainerEvent(
        final InventoryPacketContext ctx, final Cause cause, final ServerPlayer serverPlayer,
        final Container openContainer,
        final Transaction<ItemStackSnapshot> transaction,
        final List<SlotTransaction> slotTransactions, final List<Entity> capturedEntities, final int usedButton,
        final @Nullable Slot slot
    ) {
        return SpongeEventFactory.createClickContainerEventDouble(cause,
            openContainer, transaction,
            Optional.ofNullable(slot), slotTransactions);
    }

    @Override
    public void populateContext(final ServerPlayer playerMP, final Packet<?> packet,
        final InventoryPacketContext context) {
        super.populateContext(playerMP, packet, context);
        ((TrackedContainerBridge) playerMP.containerMenu).bridge$setFirePreview(false);
    }

    @Override
    public void unwind(final InventoryPacketContext context) {
        DragInventoryStopState.unwindCraftPreview(context);
        super.unwind(context);
    }

}
