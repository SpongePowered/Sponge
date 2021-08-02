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
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.common.bridge.CreatorTrackedBridge;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.util.Constants;

import net.minecraft.network.protocol.Packet;
import java.util.List;
import java.util.Optional;

public final class DropItemOutsideWindowState extends BasicInventoryPacketState {

    public DropItemOutsideWindowState(final int stateid) {
        super(stateid);
    }

    @Override
    public void populateContext(final net.minecraft.server.level.ServerPlayer playerMP, final Packet<?> packet,
        final InventoryPacketContext context) {
        super.populateContext(playerMP, packet, context);
    }

    @Override
    public ClickContainerEvent createInventoryEvent(final net.minecraft.server.level.ServerPlayer playerMP, final Container openContainer,
        final Transaction<ItemStackSnapshot> transaction,
        final List<SlotTransaction> slotTransactions, final List<Entity> capturedEntities, final int usedButton,
        final @Nullable Slot slot) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);

            for (final Entity currentEntity : capturedEntities) {
                if (currentEntity instanceof CreatorTrackedBridge) {
                    ((CreatorTrackedBridge) currentEntity).tracked$setTrackedUUID(PlayerTracker.Type.CREATOR, ((ServerPlayer) playerMP).uniqueId());
                } else {
                    currentEntity.offer(Keys.CREATOR, playerMP.getUUID());
                }
            }
            if (usedButton == Constants.Networking.PACKET_BUTTON_PRIMARY_ID) {
                return SpongeEventFactory.createClickContainerEventDropOutsidePrimary(frame.currentCause(),
                    openContainer, transaction, capturedEntities,
                    Optional.ofNullable(slot), slotTransactions);
            } else {
                return SpongeEventFactory.createClickContainerEventDropOutsideSecondary(frame.currentCause(),
                    openContainer, transaction, capturedEntities,
                    Optional.ofNullable(slot), slotTransactions);
            }
        }
    }

}
