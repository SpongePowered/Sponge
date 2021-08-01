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
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.packet.inventory.InventoryPacketContext;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.List;
import java.util.Optional;

public class DropFromPlayerInventoryTransaction extends ContainerBasedTransaction {

    private final ServerPlayer player;
    private final boolean dropAll;
    private final @Nullable Slot slot;
    private final ItemStackSnapshot originalCursor;

    public DropFromPlayerInventoryTransaction(final Player player, final boolean dropAll) {
        super(((ServerWorld) player.level).key(), player.containerMenu);
        this.player = (ServerPlayer) player;
        this.dropAll = dropAll;
        this.originalCursor = ItemStackUtil.snapshotOf(player.inventory.getCarried());
        this.slot = ((PlayerInventory) player.inventory).equipment().slot(EquipmentTypes.MAIN_HAND).orElse(null);
    }

    @Override
    Optional<ClickContainerEvent> createInventoryEvent(
        final List<SlotTransaction> slotTransactions, final ImmutableList<Entity> entities,
        final PhaseContext<@NonNull ?> context,
        final Cause cause
    ) {
        if (this.slot != null) {
            final ItemStackSnapshot item = this.slot.peek().createSnapshot();
            slotTransactions.add(new SlotTransaction(this.slot, item, item));
        }

        final Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(this.originalCursor, this.originalCursor);

        final @Nullable ClickContainerEvent event = context.createContainerEvent(cause, this.player, (Container) this.menu,
                cursorTransaction, slotTransactions, entities, dropAll ? 1 : 0, this.slot);

        return Optional.of(event);
    }

    @Override
    public void restore(final PhaseContext<@NonNull ?> context, final ClickContainerEvent event) {
        this.handleEventResults(this.player, event);
    }

    @Override
    public void postProcessEvent(PhaseContext<@NonNull ?> context, ClickContainerEvent event) {
        this.handleEventResults(this.player, event);
    }

    @Override
    boolean isContainerEventAllowed(final PhaseContext<@NonNull ?> context) {
        if (!(context instanceof InventoryPacketContext)) {
            return false;
        }
        final Packet<?> packet = ((InventoryPacketContext) context).getPacket();
        if (packet instanceof ServerboundContainerClickPacket) {
            final int containerId = ((InventoryPacketContext) context).<ServerboundContainerClickPacket>getPacket().getContainerId();
            return containerId != this.player.containerMenu.containerId;
        }
        if (packet instanceof ServerboundPlayerActionPacket) {
            return context.getSource(ServerPlayer.class).map(p -> p == this.player).orElse(false);
        }
        return false;
    }

}

