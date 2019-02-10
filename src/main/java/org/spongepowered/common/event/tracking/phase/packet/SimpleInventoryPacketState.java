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
package org.spongepowered.common.event.tracking.phase.packet;

import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

abstract class SimpleInventoryPacketState extends BasicInventoryPacketState {

    private SimpleEventCreator eventCreator;

    public SimpleInventoryPacketState(SimpleEventCreator eventCreator) {
        this.eventCreator = eventCreator;
    }

    public SimpleInventoryPacketState(SimpleEventCreator eventCreator, int stateId) {
        super(stateId);
        this.eventCreator = eventCreator;
    }

    public SimpleInventoryPacketState(SimpleEventCreator eventCreator, int stateId, int stateMask) {
        super(stateId, stateMask);
        this.eventCreator = eventCreator;
    }

    public SimpleInventoryPacketState(SimpleEventCreatorNoButton eventCreator) {
        this.eventCreator = transform(eventCreator);
    }

    public SimpleInventoryPacketState(SimpleEventCreatorNoButton eventCreator, int stateId) {
        super(stateId);
        this.eventCreator = transform(eventCreator);
    }

    public SimpleInventoryPacketState(SimpleEventCreatorNoButton eventCreator, int stateId, int stateMask) {
        super(stateId, stateMask);
        this.eventCreator = transform(eventCreator);
    }

    static SimpleEventCreator transform(SimpleEventCreatorNoButton creator)
    {
        return (cause, container, cTrans, inv, slot, btn) -> creator.createEvent(cause, container, cTrans, inv, slot);
    }

    @Override
    public ClickContainerEvent createInventoryEvent(EntityPlayerMP playerMP, Container openContainer, Transaction<ItemStackSnapshot> transaction,
            List<SlotTransaction> slotTransactions, List<Entity> capturedEntities, int usedButton, @Nullable Slot slot) {
        return this.eventCreator.createEvent(Sponge.getCauseStackManager().getCurrentCause(), openContainer, transaction,
                Optional.ofNullable(slot), slotTransactions, usedButton);
    }

    @FunctionalInterface
    interface SimpleEventCreator {
        ClickContainerEvent createEvent(Cause cause, Container container, Transaction<ItemStackSnapshot> cursorTransaction,
                Optional<Slot> slot, List<SlotTransaction> transactions, int usedButton);
    }

    @FunctionalInterface
    interface SimpleEventCreatorNoButton {
        ClickContainerEvent createEvent(Cause cause, Container container, Transaction<ItemStackSnapshot> cursorTransaction,
                Optional<Slot> slot, List<SlotTransaction> transactions);
    }

}
