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
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.client.CPacketEnchantItem;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.AffectEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.inventory.ContainerBridge;
import org.spongepowered.common.bridge.inventory.TrackedInventoryBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.item.inventory.util.ContainerUtil;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;

public final class EnchantItemPacketState extends BasicInventoryPacketState {

    @Override
    public void unwind(InventoryPacketContext context) {
        // TODO - Pre changes of merging PacketFunction into the phase states, enchantments did NOT have any processing....
        final EntityPlayerMP player = context.getPacketPlayer();

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
        final TrackedInventoryBridge trackedInventory = (TrackedInventoryBridge) player.field_71070_bA;
        if (!trackedInventory.bridge$capturingInventory()) {
            trackedInventory.bridge$getCapturedSlotTransactions().clear();
            return;
        }

        // TODO clear this shit out of the context
        final CPacketEnchantItem packetIn = context.getPacket();
        final ItemStackSnapshot lastCursor = context.getCursor();
        final ItemStackSnapshot newCursor = ItemStackUtil.snapshotOf(player.field_71071_by.func_70445_o());
        final Transaction<ItemStackSnapshot> transaction = new Transaction<>(lastCursor, newCursor);

        final net.minecraft.inventory.Container openContainer = player.field_71070_bA;
        final List<SlotTransaction> slotTransactions = trackedInventory.bridge$getCapturedSlotTransactions();

        final int usedButton = packetIn.func_149537_d();
        final List<Entity> capturedItems = new ArrayList<>();
        for (EntityItem entityItem : context.getCapturedItems()) {
            capturedItems.add((Entity) entityItem);
        }
        context.getCapturedItems().clear();
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            Sponge.getCauseStackManager().pushCause(player);
            Sponge.getCauseStackManager().pushCause(openContainer);
            final ClickInventoryEvent inventoryEvent;
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
                if (inventoryEvent instanceof AffectEntityEvent && ((AffectEntityEvent) inventoryEvent).getEntities().isEmpty()) {
                    slotTransactions.clear();
                    trackedInventory.bridge$setCaptureInventory(false);
                    return;
                }

                // The client sends several packets all at once for drag events
                // - we
                // only care about the last one.
                // Therefore, we never add any 'fake' transactions, as the final
                // packet has everything we want.
                if (!(inventoryEvent instanceof ClickInventoryEvent.Drag)) {
                    PacketPhaseUtil.validateCapturedTransactions(packetIn.func_149539_c(), openContainer, inventoryEvent.getTransactions());
                }

                SpongeImpl.postEvent(inventoryEvent);
                if (inventoryEvent.isCancelled() || PacketPhaseUtil.allTransactionsInvalid(inventoryEvent.getTransactions())) {
                    if (inventoryEvent instanceof ClickInventoryEvent.Drop) {
                        capturedItems.clear();
                    }

                    // Restore cursor
                    PacketPhaseUtil.handleCustomCursor(player, inventoryEvent.getCursorTransaction().getOriginal());

                    // Restore target slots
                    PacketPhaseUtil.handleSlotRestore(player, openContainer, inventoryEvent.getTransactions(), true);
                } else {
                    PacketPhaseUtil.handleSlotRestore(player, openContainer, inventoryEvent.getTransactions(), false);

                    // Handle cursor
                    if (!inventoryEvent.getCursorTransaction().isValid()) {
                        PacketPhaseUtil.handleCustomCursor(player, inventoryEvent.getCursorTransaction().getOriginal());
                    } else if (inventoryEvent.getCursorTransaction().getCustom().isPresent()) {
                        PacketPhaseUtil.handleCustomCursor(player, inventoryEvent.getCursorTransaction().getFinal());
                    }
                    if (inventoryEvent instanceof SpawnEntityEvent) {
                        processSpawnedEntities(player, (SpawnEntityEvent) inventoryEvent);
                    } else {
                        context.getCapturedEntitySupplier().acceptAndClearIfNotEmpty(entities -> {
                            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
                            SpongeCommonEventFactory.callSpawnEntity(entities, context);
                        });
                    }
                }
            }
        }
        slotTransactions.clear();
        trackedInventory.bridge$setCaptureInventory(false);
    }

    // TODO EnchantItemEvent
}
