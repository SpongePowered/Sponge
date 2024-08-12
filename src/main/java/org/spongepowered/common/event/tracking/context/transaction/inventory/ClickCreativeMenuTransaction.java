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

import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.packet.inventory.InventoryPacketContext;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.List;
import java.util.Optional;

public class ClickCreativeMenuTransaction extends ContainerBasedTransaction {

    private final ServerPlayer player;
    private final int slotNum;
    private final @Nullable Slot slot;
    private final ItemStackSnapshot creativeStack;
    private final ItemStackSnapshot originalCursor;

    public ClickCreativeMenuTransaction(final Player player, final int slotNum, final ItemStackSnapshot creativeStack) {
        super(player.containerMenu);
        this.player = (ServerPlayer) player;
        this.slotNum = slotNum;
        this.creativeStack = creativeStack;
        this.originalCursor = ItemStackUtil.snapshotOf(player.containerMenu.getCarried());
        this.slot = slotNum >= 0 ? ((InventoryAdapter) this.menu).inventoryAdapter$getSlot(slotNum).orElse(null) : null;
    }

    @Override
    Optional<ClickContainerEvent> createInventoryEvent(
        final List<SlotTransaction> slotTransactions, final List<Entity> entities,
        final PhaseContext<@NonNull ?> context,
        final Cause cause
    ) {
        if (slotTransactions.isEmpty() && this.slotNum >= 0 && this.slot != null) {
            // No SlotTransaction was captured. So we add the clicked slot as a transaction with the creative stack
            final ItemStackSnapshot item = this.slot.peek().asImmutable();
            slotTransactions.add(new SlotTransaction(this.slot, item, this.creativeStack));
        }

        // Creative doesn't inform server of cursor status so there is no way of knowing what the final stack is
        final Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(this.originalCursor, ItemStackSnapshot.empty());
        if (entities.isEmpty()) {
            return Optional.of(SpongeEventFactory.createClickContainerEventCreativeSet(cause, (Container) this.menu,
                        cursorTransaction, Optional.ofNullable(this.slot), slotTransactions));
        }

        final Cause causeWithSpawnType = Cause.builder().from(cause).build(
                EventContext.builder().from(cause.context()).add(
                        EventContextKeys.SPAWN_TYPE,
                        SpawnTypes.DROPPED_ITEM.get()
                ).build());

        return Optional.of(SpongeEventFactory.createClickContainerEventCreativeDrop(causeWithSpawnType, (Container) this.menu,
                cursorTransaction, this.creativeStack, entities, Optional.ofNullable(this.slot), slotTransactions));
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
    boolean isContainerEventAllowed(final PhaseContext<@Nullable ?> context) {
        if (!(context instanceof InventoryPacketContext)) {
            return false;
        }
        final int containerId = ((InventoryPacketContext) context).<ServerboundContainerClickPacket>getPacket().getContainerId();
        return containerId != this.player.containerMenu.containerId;
    }

}
