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
package org.spongepowered.common.event.tracking.context.transaction.inventory;

import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.event.tracking.PhaseContext;

import java.util.List;
import java.util.Optional;

public class SetCarriedItemTransaction extends InventoryBasedTransaction {

    private final ServerPlayer player;
    @Nullable private final Slot prevSlot;
    @Nullable private final Slot newSlot;
    private final int prevSlotId;

    public SetCarriedItemTransaction(final Player player, int newSlot) {
        super((Inventory) player.getInventory());
        this.player = (ServerPlayer) player;
        final PlayerInventory inventory = (PlayerInventory) this.player.getInventory();
        this.prevSlotId = this.player.getInventory().selected;
        this.prevSlot = inventory.hotbar().slot(this.prevSlotId).orElse(null);
        this.newSlot = inventory.hotbar().slot(newSlot).orElse(null);
    }

    @Override
    Optional<ChangeInventoryEvent> createInventoryEvent(final List<SlotTransaction> slotTransactions,
            final List<Entity> entities, final PhaseContext<@NonNull ?> context,
            final Cause cause) {
        if (!entities.isEmpty()) {
            SpongeCommon.logger().warn("Entities are being captured but not being processed");
        }
        if (this.newSlot == null || this.prevSlot == null) {
            return Optional.empty();
        }
        final ChangeInventoryEvent.Held event = SpongeEventFactory
                .createChangeInventoryEventHeld(cause, this.newSlot, (Inventory) this.player.getInventory(), this.prevSlot, slotTransactions);
        return Optional.of(event);
    }

    @Override
    public void restore(final PhaseContext<@NonNull ?> context, final ChangeInventoryEvent event) {
        this.player.connection.send(new ClientboundSetHeldSlotPacket(this.prevSlotId));
        this.player.getInventory().selected = this.prevSlotId;
        this.handleEventResults(this.player, event);
    }

    @Override
    public void postProcessEvent(final PhaseContext<@NonNull ?> context, final ChangeInventoryEvent event) {
        this.handleEventResults(this.player, event);
    }

}
