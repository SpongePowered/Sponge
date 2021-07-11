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
package org.spongepowered.common.event.tracking.context.transaction;

import com.google.common.collect.ImmutableList;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.event.tracking.phase.packet.inventory.InventoryPacketContext;
import org.spongepowered.common.event.tracking.phase.packet.inventory.SwitchHotbarScrollState;

import java.util.List;
import java.util.Optional;

public class PlayerInventoryTransaction extends InventoryBasedTransaction {

    private final ServerPlayer player;

    public PlayerInventoryTransaction(final Player player) {
        super(((ServerWorld) player.level).key(), (Inventory) player.inventory);
        this.player = (ServerPlayer) player;
    }

    @Override
    Optional<ChangeInventoryEvent> createInventoryEvent(
        final List<SlotTransaction> slotTransactions, final ImmutableList<Entity> entities,
        final PhaseContext<@NonNull ?> context,
        final Cause cause
    ) {
        @Nullable final ChangeInventoryEvent event = context.createInventoryEvent(cause, this.inventory, slotTransactions, entities);
        return Optional.ofNullable(event);
    }

    @Override
    public void restore(PhaseContext<@NonNull ?> context, ChangeInventoryEvent event) {
        // TODO post-transaction handling
        PacketPhaseUtil.handleSlotRestore(player, null, event.transactions(), event.isCancelled());

        if (context.getState() instanceof SwitchHotbarScrollState) {
            if (event.isCancelled()) {
                final int prevSlot = ((InventoryPacketContext) context).getOldHighlightedSlotId();
                player.connection.send(new ClientboundSetCarriedItemPacket(prevSlot));
                player.inventory.selected = prevSlot;
            } else {
                // TODO check if needed:
                player.resetLastActionTime();
            }
        }
    }

    @Override
    Optional<SlotTransaction> getSlotTransaction() {
        return Optional.empty();
    }

    @Override
    List<Entity> getEntitiesSpawned() {
        return this.sideEffects != null ? this.sideEffects.stream().flatMap(e -> e.stream());
    }

}
