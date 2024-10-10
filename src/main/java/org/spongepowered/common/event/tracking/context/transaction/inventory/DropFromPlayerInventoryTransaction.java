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

import net.minecraft.server.level.ServerPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;

import java.util.List;
import java.util.Optional;

public class DropFromPlayerInventoryTransaction extends InventoryBasedTransaction {

    private final ServerPlayer player;
    private final boolean dropAll;
    private final @Nullable Slot slot;

    public DropFromPlayerInventoryTransaction(final ServerPlayer player, final boolean dropAll) {
        super((Inventory) player.getInventory());
        this.player = player;
        this.dropAll = dropAll;
        this.slot = ((PlayerInventory) player.getInventory()).equipment().slot(EquipmentTypes.MAINHAND).orElse(null);
    }

    @Override
    Optional<ChangeInventoryEvent> createInventoryEvent(final List<SlotTransaction> slotTransactions,
            final List<Entity> entities, final PhaseContext<@NonNull ?> context,
            final Cause currentCause) {
        TrackingUtil.setCreatorReference(entities, this.player);

        final Cause causeWithSpawnType = Cause.builder().from(currentCause).build(
                EventContext.builder().from(currentCause.context()).add(
                        EventContextKeys.SPAWN_TYPE,
                        SpawnTypes.DROPPED_ITEM.get()
                ).build());

        if (this.dropAll) {
            return Optional.of(SpongeEventFactory.createChangeInventoryEventDropFull(causeWithSpawnType,
                    entities, this.inventory, this.slot, slotTransactions));
        }
        return Optional.of(SpongeEventFactory.createChangeInventoryEventDropSingle(causeWithSpawnType,
                entities, this.inventory, this.slot, slotTransactions));
    }

    @Override
    public void restore(final PhaseContext<@NonNull ?> context, final ChangeInventoryEvent event) {
        this.handleEventResults(this.player, event);
    }

    @Override
    public void postProcessEvent(PhaseContext<@NonNull ?> context, ChangeInventoryEvent event) {
        this.handleEventResults(this.player, event);
    }

}
