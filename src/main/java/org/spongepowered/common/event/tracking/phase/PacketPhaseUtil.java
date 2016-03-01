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
package org.spongepowered.common.event.tracking.phase;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.client.C10PacketCreativeInventoryAction;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.world.IInteractionObject;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.CreativeInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingHelper;
import org.spongepowered.common.interfaces.IMixinContainer;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.item.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.item.inventory.util.ContainerUtil;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class PacketPhaseUtil {

    static void handleInventoryEvents(IPhaseState phaseState, PhaseContext phaseContext) {
        final EntityPlayerMP player = phaseContext.firstNamed(TrackingHelper.PACKET_PLAYER, EntityPlayerMP.class).get();
        final C0EPacketClickWindow packetIn = phaseContext.firstNamed(TrackingHelper.CAPTURED_PACKET, C0EPacketClickWindow.class).get();
        final ItemStackSnapshot lastCursor = phaseContext.firstNamed(TrackingHelper.CURSOR, ItemStackSnapshot.class).get();
        final ItemStackSnapshot newCursor = ItemStackUtil.snapshotOf(player.inventory.getItemStack());
        final Transaction<ItemStackSnapshot> transaction = new Transaction<>(lastCursor, newCursor);

        final Container openContainer = player.openContainer;
        final List<SlotTransaction> slotTransactions = ContainerUtil.toMixin(openContainer).getCapturedTransactions();
        if (slotTransactions.size() == 0 && packetIn.getSlotId() >= 0) {
            Slot slot = openContainer.getSlot(packetIn.getSlotId());
            if (slot != null) {
                SlotTransaction slotTransaction = new SlotTransaction(new SlotAdapter(slot), ItemStackSnapshot.NONE, ItemStackSnapshot.NONE);
                slotTransactions.add(slotTransaction);
            }
        }
        final int usedButton = packetIn.getUsedButton();
        final List<Entity> capturedItems = phaseContext.getCapturedItems().get();
        final Cause cause = Cause.of(NamedCause.source(player), NamedCause.of("Container", openContainer));
        final ClickInventoryEvent clickEvent;
        if (phaseState instanceof PacketPhase.Inventory) {
            clickEvent = ((PacketPhase.Inventory) phaseState).createInventoryEvent(player, ContainerUtil.fromNative(openContainer), transaction,
                    slotTransactions, capturedItems, cause, usedButton);
        } else {
            clickEvent = null;
        }

        if (clickEvent != null) {
            SpongeImpl.postEvent(clickEvent);

            if (clickEvent.isCancelled()) {
                if (clickEvent instanceof ClickInventoryEvent.Drop) {
                    capturedItems.clear();
                }

                // Restore cursor
                handleCustomCursor(player, clickEvent.getCursorTransaction().getOriginal());

                if (clickEvent instanceof ClickInventoryEvent.Double) {
                    clickEvent.getTransactions().clear();
                    return;
                }

                // Restore target slots
                handleSlotRestore(player, clickEvent.getTransactions());
            } else {
                handleCustomSlot(player, clickEvent.getTransactions());

                // Custom cursor
                if (clickEvent.getCursorTransaction().getCustom().isPresent()) {
                    handleCustomCursor(player, clickEvent.getCursorTransaction().getFinal());
                }
            }
            slotTransactions.clear();
        }
    }


    // Open/Close
    static void handleInteractInventoryOpenCloseEvent(Cause cause, EntityPlayerMP player, Packet<?> packetIn, IPhaseState phaseState,
            PhaseContext context) {
        final Container lastOpenContainer = context.firstNamed(TrackingHelper.OPEN_CONTAINER, Container.class).orElse(null);
        if ((!(player.openContainer instanceof ContainerPlayer) && (lastOpenContainer instanceof ContainerPlayer)
             || (packetIn instanceof C16PacketClientStatus
                 && ((C16PacketClientStatus) packetIn).getStatus() == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT))) {
            ItemStackSnapshot lastCursor = context.firstNamed(TrackingHelper.CURSOR, ItemStackSnapshot.class).get();
            ItemStackSnapshot newCursor = ItemStackUtil.snapshotOf(player.inventory.getItemStack());
            Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(lastCursor, newCursor);
            InteractInventoryEvent.Open event =
                    SpongeEventFactory.createInteractInventoryEventOpen(cause, cursorTransaction,
                            (org.spongepowered.api.item.inventory.Container) player.openContainer);
            SpongeImpl.postEvent(event);
            if (event.isCancelled()) {
                player.closeScreen();
            } else {
                // Custom cursor
                if (event.getCursorTransaction().getCustom().isPresent()) {
                    handleCustomCursor(player, event.getCursorTransaction().getFinal());
                }
            }
        } else if (player.openContainer instanceof ContainerPlayer && !(lastOpenContainer instanceof ContainerPlayer)) {
            ItemStackSnapshot lastCursor = context.firstNamed(TrackingHelper.CURSOR, ItemStackSnapshot.class).get();
            ItemStackSnapshot newCursor = ItemStackUtil.snapshotOf(player.inventory.getItemStack());
            Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(lastCursor, newCursor);
            InteractInventoryEvent.Close event =
                    SpongeEventFactory.createInteractInventoryEventClose(cause, cursorTransaction, ContainerUtil.fromNative(lastOpenContainer));
            SpongeImpl.postEvent(event);
            if (event.isCancelled()) {
                if (lastOpenContainer.getSlot(0) != null) {
                    player.openContainer = lastOpenContainer;
                    final Slot slot = lastOpenContainer.getSlot(0);
                    String guiId = "unknown";
                    final IInventory slotInventory = slot.inventory;
                    if (slotInventory instanceof IInteractionObject) {
                        guiId = ((IInteractionObject) slotInventory).getGuiID();
                    }
                    slotInventory.openInventory(player);
                    player.playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(lastOpenContainer.windowId, guiId, slotInventory
                            .getDisplayName(), slotInventory.getSizeInventory()));
                    // resync data to client
                    player.sendContainerToPlayer(lastOpenContainer);
                }
            } else {
                // Custom cursor
                if (event.getCursorTransaction().getCustom().isPresent()) {
                    handleCustomCursor(player, event.getCursorTransaction().getFinal());
                }
            }
        }
    }

    static void handleCreativeClickInventoryEvent(IPhaseState phaseState, PhaseContext context) {
        final EntityPlayerMP player = context.firstNamed(TrackingHelper.PACKET_PLAYER, EntityPlayerMP.class).get();
        final C10PacketCreativeInventoryAction packetIn = context.firstNamed(TrackingHelper.CAPTURED_PACKET, C10PacketCreativeInventoryAction.class).get();
        final ItemStackSnapshot lastCursor = context.firstNamed(TrackingHelper.CURSOR, ItemStackSnapshot.class).get();
        final ItemStackSnapshot newCursor =
                player.inventory.getItemStack() == null ? ItemStackSnapshot.NONE
                                                        : ((org.spongepowered.api.item.inventory.ItemStack) player.inventory.getItemStack()).createSnapshot();
        final Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(lastCursor, newCursor);
        final CreativeInventoryEvent event;
        final List<Entity> capturedEntityItems = context.getCapturedItems().orElse(new ArrayList<>());
        final Cause cause = Cause.of(NamedCause.source(player), NamedCause.of("Container", ""));
        final Container openContainer = player.openContainer;
        final List<SlotTransaction> capturedTransactions = ((IMixinContainer) openContainer).getCapturedTransactions();
        if (packetIn.getSlotId() == -1 && capturedEntityItems.size() > 0) {
            Iterator<Entity> iterator = capturedEntityItems.iterator();
            ImmutableList.Builder<EntitySnapshot> entitySnapshotBuilder = new ImmutableList.Builder<>();
            while (iterator.hasNext()) {
                Entity currentEntity = iterator.next();
                ((IMixinEntity) currentEntity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, player.getUniqueID());
                entitySnapshotBuilder.add(currentEntity.createSnapshot());
            }
            event = SpongeEventFactory.createCreativeInventoryEventDrop(cause, cursorTransaction, capturedEntityItems,
                    entitySnapshotBuilder.build(), (org.spongepowered.api.item.inventory.Container) openContainer, (World) player.worldObj,
                    capturedTransactions);
        } else {
            if (capturedTransactions.size() == 0 && packetIn.getSlotId() >= 0) {
                Slot slot = openContainer.getSlot(packetIn.getSlotId());
                if (slot != null) {
                    SlotTransaction slotTransaction =
                            new SlotTransaction(new SlotAdapter(slot), ItemStackSnapshot.NONE, ItemStackSnapshot.NONE);
                    capturedTransactions.add(slotTransaction);
                }
            }
            event = SpongeEventFactory.createCreativeInventoryEventClick(cause, cursorTransaction, ContainerUtil.fromNative(openContainer), capturedTransactions);
        }
        SpongeImpl.postEvent(event);
        if (event.isCancelled()) {
            if (event instanceof CreativeInventoryEvent.Drop) {
                capturedEntityItems.clear();
            }

            // Restore cursor
            handleCustomCursor(player, event.getCursorTransaction().getOriginal());

            // Restore target slots
            handleSlotRestore(player, event.getTransactions());
        } else {
            handleCustomSlot(player, event.getTransactions());

            // Custom cursor
            if (event.getCursorTransaction().getCustom().isPresent()) {
                handleCustomCursor(player, event.getCursorTransaction().getFinal());
            }
        }

        capturedTransactions.clear();
    }

    private static void handleSlotRestore(EntityPlayerMP player, List<SlotTransaction> slotTransactions) {
        for (SlotTransaction slotTransaction : slotTransactions) {
            final SlotAdapter slot = (SlotAdapter) slotTransaction.getSlot();
            final int slotNumber = slot.slotNumber;
            final ItemStack originalStack = ItemStackUtil.fromSnapshotToNative(slotTransaction.getOriginal());

            // TODO: fix below
            /*if (originalStack == null) {
                slot.clear();
            } else {
                slot.offer((org.spongepowered.api.item.inventory.ItemStack) originalStack);
            }*/

            final Slot nmsSlot = player.inventoryContainer.getSlot(slotNumber);
            if (nmsSlot != null) {
                nmsSlot.putStack(originalStack);
            }

            player.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(player.openContainer.windowId, slotNumber, originalStack));
        }
    }

    private static void handleCustomCursor(EntityPlayerMP player, ItemStackSnapshot customCursor) {
        ItemStack cursor = ItemStackUtil.fromSnapshotToNative(customCursor);
        player.inventory.setItemStack(cursor);
        player.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(-1, -1, cursor));
    }

    public static void handleCustomSlot(EntityPlayerMP player, List<SlotTransaction> slotTransactions) {
        for (SlotTransaction slotTransaction : slotTransactions) {
            if (slotTransaction.isValid() && slotTransaction.getCustom().isPresent()) {
                final SlotAdapter slot = (SlotAdapter) slotTransaction.getSlot();
                final int slotNumber = slot.slotNumber;
                final ItemStack customStack = ItemStackUtil.fromSnapshotToNative(slotTransaction.getFinal());

                // TODO: fix below
                /*if (customStack == null) {
                    slot.clear();
                } else {
                    slot.offer((org.spongepowered.api.item.inventory.ItemStack) customStack);
                }*/

                final Slot nmsSlot = player.inventoryContainer.getSlot(slotNumber);
                if (nmsSlot != null) {
                    nmsSlot.putStack(customStack);
                }

                player.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(player.openContainer.windowId, slotNumber, customStack));
            }
        }
    }


}
