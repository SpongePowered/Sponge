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
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.entity.AffectEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.event.tracking.phase.packet.PacketState;
import org.spongepowered.common.inventory.util.ContainerUtil;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.server.level.ServerPlayer;

public final class EnchantItemPacketState extends BasicInventoryPacketState {

    @Override
    public void unwind(InventoryPacketContext context) {
        // TODO - Pre changes of merging PacketFunction into the phase states, enchantments did NOT have any processing....
        final ServerPlayer player = context.getPacketPlayer();

        // The server will disable the player's crafting after receiving a
        // client packet
        // that did not pass validation (server click item != packet click item)
        // The server then sends a SPacketConfirmTransaction and waits for a
        // CPacketConfirmTransaction to re-enable crafting confirming that the
        // client
        // acknowledged the denied transaction.
        // To detect when this happens, we turn off capturing so we can avoid
        // firing
        // invalid events.
        // See NetHandlerPlayServerMixin processClickWindow redirect for rest of
        // fix.
        // --bloodmc
        final TrackedInventoryBridge trackedInventory = (TrackedInventoryBridge) player.containerMenu;
        if (!trackedInventory.bridge$capturingInventory()) {
            trackedInventory.bridge$getCapturedSlotTransactions().clear();
            return;
        }

        // TODO clear this shit out of the context
        final ServerboundContainerButtonClickPacket packetIn = context.getPacket();
        final ItemStackSnapshot lastCursor = context.getCursor();
        final ItemStackSnapshot newCursor = ItemStackUtil.snapshotOf(player.inventory.getCarried());
        final Transaction<ItemStackSnapshot> transaction = new Transaction<>(lastCursor, newCursor);

        final net.minecraft.world.inventory.AbstractContainerMenu openContainer = player.containerMenu;
        final List<SlotTransaction> slotTransactions = trackedInventory.bridge$getCapturedSlotTransactions();

        final int usedButton = packetIn.getButtonId();
        final List<Entity> capturedItems = new ArrayList<>();
        final CauseStackManager causeStackManager = PhaseTracker.getCauseStackManager();
        try (CauseStackManager.StackFrame frame = causeStackManager.pushCauseFrame()) {
            causeStackManager.pushCause(player);
            causeStackManager.pushCause(openContainer);
            final ClickContainerEvent inventoryEvent;
            inventoryEvent = this.createInventoryEvent(player, ContainerUtil.fromNative(openContainer), transaction,
                        Lists.newArrayList(slotTransactions),
                        capturedItems,
                        usedButton, null);

            // Some mods may override container detectAndSendChanges method and prevent captures
            // If this happens and we captured no entities, avoid firing events
            if (trackedInventory.bridge$getCapturedSlotTransactions().isEmpty() && capturedItems.isEmpty()) {
                trackedInventory.bridge$setCaptureInventory(false);
                return;
            }if (inventoryEvent != null) {
                // Don't fire inventory drop events when there are no entities
                if (inventoryEvent instanceof AffectEntityEvent && ((AffectEntityEvent) inventoryEvent).entities().isEmpty()) {
                    slotTransactions.clear();
                    trackedInventory.bridge$setCaptureInventory(false);
                    return;
                }

                // The client sends several packets all at once for drag events
                // - we
                // only care about the last one.
                // Therefore, we never add any 'fake' transactions, as the final
                // packet has everything we want.
                if (!(inventoryEvent instanceof ClickContainerEvent.Drag)) {
                    PacketPhaseUtil.validateCapturedTransactions(packetIn.getContainerId(), openContainer, inventoryEvent.transactions());
                }

                SpongeCommon.post(inventoryEvent);
                if (inventoryEvent.isCancelled() || PacketPhaseUtil.allTransactionsInvalid(inventoryEvent.transactions())) {
                    if (inventoryEvent instanceof ClickContainerEvent.Drop) {
                        capturedItems.clear();
                    }

                    // Restore cursor
                    PacketPhaseUtil.handleCustomCursor(player, inventoryEvent.cursorTransaction().original());

                    // Restore target slots
                    PacketPhaseUtil.handleSlotRestore(player, openContainer, inventoryEvent.transactions(), true);
                } else {
                    PacketPhaseUtil.handleSlotRestore(player, openContainer, inventoryEvent.transactions(), false);

                    // Handle cursor
                    if (!inventoryEvent.cursorTransaction().isValid()) {
                        PacketPhaseUtil.handleCustomCursor(player, inventoryEvent.cursorTransaction().original());
                    } else if (inventoryEvent.cursorTransaction().custom().isPresent()) {
                        PacketPhaseUtil.handleCustomCursor(player, inventoryEvent.cursorTransaction().finalReplacement());
                    }
                    if (inventoryEvent instanceof SpawnEntityEvent) {
                        PacketState.processSpawnedEntities(player, (SpawnEntityEvent) inventoryEvent);
                    } else {
//                        context.getCapturedEntitySupplier().acceptAndClearIfNotEmpty(entities -> {
//                            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
//                            SpongeCommonEventFactory.callSpawnEntity(entities, context);
//                        });
                    }
                }
            }
        }
        slotTransactions.clear();
        trackedInventory.bridge$setCaptureInventory(false);
    }

    // TODO EnchantItemEvent
}
