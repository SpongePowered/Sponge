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

import com.google.common.collect.Lists;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.CreatorTrackedBridge;
import org.spongepowered.common.bridge.world.entity.EntityBridge;
import org.spongepowered.common.bridge.server.level.ServerPlayerBridge;
import org.spongepowered.common.bridge.world.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.event.tracking.phase.packet.PacketState;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.util.ContainerUtil;
import org.spongepowered.common.util.Constants;

import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class DropItemWithHotkeyState extends BasicInventoryPacketState {

    public DropItemWithHotkeyState() {
        super(
            Constants.Networking.MODE_DROP | Constants.Networking.BUTTON_PRIMARY | Constants.Networking.BUTTON_SECONDARY | Constants.Networking.CLICK_INSIDE_WINDOW);
    }

    @Override
    public void unwind(final InventoryPacketContext context) {
        final net.minecraft.server.level.ServerPlayer player = context.getPacketPlayer();
        final Entity spongePlayer = (Entity) player;
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {

            TrackingUtil.processBlockCaptures(context);
            { // TODO - figure this out with transactions
                final List<ItemEntity> items = new ArrayList<>();

                final ArrayList<Entity> entities = new ArrayList<>();
                for (final ItemEntity item : items) {
                    entities.add((Entity) item);
                }

                final int usedButton;
                final Slot slot;
                if (context.getPacket() instanceof ServerboundPlayerActionPacket) {
                    final ServerboundPlayerActionPacket packetIn = context.getPacket();
                    usedButton = packetIn.getAction() == ServerboundPlayerActionPacket.Action.DROP_ITEM
                        ? Constants.Networking.PACKET_BUTTON_PRIMARY_ID
                        : 1;
                    slot = ((PlayerInventory) player.inventory).equipment().slot(
                        EquipmentTypes.MAIN_HAND).orElse(null);
                } else {
                    final ServerboundContainerClickPacket packetIn = context.getPacket();
                    usedButton = packetIn.getButtonNum();
                    slot = ((InventoryAdapter) player.inventory).inventoryAdapter$getSlot(
                        packetIn.getSlotNum()).orElse(null);
                }

                final Transaction<ItemStackSnapshot> cursorTrans = new Transaction<>(ItemStackSnapshot.empty(),
                    ItemStackSnapshot.empty());
                final TrackedInventoryBridge mixinContainer = (TrackedInventoryBridge) player.containerMenu;
                final List<SlotTransaction> slotTrans = mixinContainer.bridge$getCapturedSlotTransactions();
                final ClickContainerEvent.Drop dropItemEvent = this.createInventoryEvent(player,
                    ContainerUtil.fromNative(player.containerMenu),
                    cursorTrans, Lists.newArrayList(slotTrans), entities, usedButton, slot);

                SpongeCommon.post(dropItemEvent);
                if (dropItemEvent.isCancelled() || PacketPhaseUtil.allTransactionsInvalid(
                    dropItemEvent.transactions())) {
                    ((ServerPlayerBridge) player).bridge$restorePacketItem(InteractionHand.MAIN_HAND);
                    PacketPhaseUtil.handleSlotRestore(player, player.containerMenu, dropItemEvent.transactions(),
                        true);
                } else {
                    PacketState.processSpawnedEntities(player, dropItemEvent);
                }
                for (Entity entity : entities) {
                    if (((EntityBridge) entity).bridge$isConstructing()) {
                        ((EntityBridge) entity).bridge$fireConstructors();
                    }
                }
                slotTrans.clear();
                mixinContainer.bridge$setCaptureInventory(false);
            }

            final TrackedInventoryBridge mixinContainer = (TrackedInventoryBridge) player.containerMenu;
            mixinContainer.bridge$setCaptureInventory(false);
            mixinContainer.bridge$getCapturedSlotTransactions().clear();
        }
    }

    @Override
    public ClickContainerEvent.Drop createInventoryEvent(final net.minecraft.server.level.ServerPlayer serverPlayer,
        final Container openContainer, final Transaction<ItemStackSnapshot> transaction,
        final List<SlotTransaction> slotTransactions, final List<Entity> capturedEntities, final int usedButton,
        final @Nullable Slot slot) {
        try (final StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
            for (final Entity currentEntity : capturedEntities) {
                if (currentEntity instanceof CreatorTrackedBridge) {
                    ((CreatorTrackedBridge) currentEntity).tracked$setCreatorReference(((ServerPlayer) serverPlayer).user());
                } else {
                    currentEntity.offer(Keys.CREATOR, serverPlayer.getUUID());
                }
            }

            // A 'primary click' is used by the game to indicate a single drop (e.g. pressing 'q' without holding 'control')
            final ClickContainerEvent.Drop event;
            if (usedButton == Constants.Networking.PACKET_BUTTON_PRIMARY_ID) {
                event = SpongeEventFactory.createClickContainerEventDropSingle(
                    frame.currentCause(),
                    openContainer, transaction, capturedEntities, Optional.ofNullable(slot), slotTransactions);
            } else {
                event = SpongeEventFactory.createClickContainerEventDropFull(
                    frame.currentCause(),
                    openContainer, transaction, capturedEntities, Optional.ofNullable(slot), slotTransactions);
            }
            // TODO the nature of how this event is handled prevents the cause information being preserved through
            // the event call, somehow should not release this frame until after the event is posted
            return event;
        }
    }

}
