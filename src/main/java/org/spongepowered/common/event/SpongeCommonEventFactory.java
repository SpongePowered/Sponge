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
package org.spongepowered.common.event;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.client.C10PacketCreativeInventoryAction;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.network.play.server.S09PacketHeldItemChange;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.world.IInteractionObject;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.item.inventory.AffectSlotEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.CreativeInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.world.World;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.interfaces.IMixinContainer;
import org.spongepowered.common.interfaces.IMixinWorld;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.util.StaticMixinHelper;

import java.util.Iterator;

public class SpongeCommonEventFactory {

    // Inventory static fields
    public final static int BUTTON_PRIMARY = 0;
    public final static int BUTTON_SECONDARY = 1;
    public final static int BUTTON_MIDDLE = 2;
    public final static int CLICK_DRAG_LEFT = 2;
    public final static int CLICK_DRAG_RIGHT = 6;
    public final static int CLICK_OUTSIDE = -999;
    public final static int CLICK_OUTSIDE_CREATIVE = -1;

    public final static int MODE_CLICK = 0;
    public final static int MODE_SHIFT_CLICK = 1;
    public final static int MODE_HOTBAR = 2;
    public final static int MODE_PICKBLOCK = 3;
    public final static int MODE_DROP = 4;
    public final static int MODE_DRAG = 5;
    public final static int MODE_DOUBLE_CLICK = 6;

    public final static int DRAG_MODE_SPLIT_ITEMS = 0;
    public final static int DRAG_MODE_ONE_ITEM = 1;
    public final static int DRAG_STATUS_STARTED = 0;
    public final static int DRAG_STATUS_ADD_SLOT = 1;
    public final static int DRAG_STATUS_STOPPED = 2;

    // Create Sponge Events

    public static InteractInventoryEvent.Held callInteractInventoryHeldEvent(EntityPlayerMP player,
            C09PacketHeldItemChange packetIn) {
        net.minecraft.item.ItemStack currentItem = player.getCurrentEquippedItem();
        ItemStackSnapshot originalSnapshot =
                currentItem != null ? ((org.spongepowered.api.item.inventory.ItemStack) currentItem).createSnapshot() : ItemStackSnapshot.NONE;
        org.spongepowered.api.item.inventory.ItemStack itemStack =
                ((org.spongepowered.api.item.inventory.ItemStack) player.inventory.mainInventory[packetIn.getSlotId()]);
        ItemStackSnapshot newSnapshot = itemStack != null ? itemStack.createSnapshot() : ItemStackSnapshot.NONE;
        Slot slot = player.openContainer.getSlot(packetIn.getSlotId());
        SlotTransaction transaction = new SlotTransaction((org.spongepowered.api.item.inventory.Slot) slot, originalSnapshot, newSnapshot);
        ImmutableList<SlotTransaction> transactions = new ImmutableList.Builder<SlotTransaction>().add(transaction).build();
        ItemStackSnapshot newCursor =
                player.inventory.getItemStack() == null ? ItemStackSnapshot.NONE
                        : ((org.spongepowered.api.item.inventory.ItemStack) player.inventory.getItemStack()).createSnapshot();
        Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<ItemStackSnapshot>(StaticMixinHelper.lastCursor, newCursor);
        InteractInventoryEvent.Held event =
                SpongeEventFactory.createInteractInventoryEventHeld(Sponge.getGame(), Cause.of(player), cursorTransaction,
                        (Container) player.inventoryContainer, transactions);
        Sponge.getGame().getEventManager().post(event);

        if (event.isCancelled()) {
            player.playerNetServerHandler.sendPacket(new S09PacketHeldItemChange(player.inventory.currentItem));
        } else {
            for (SlotTransaction slotTransaction : event.getTransactions()) {
                if (slotTransaction.isValid() && slotTransaction.getCustom().isPresent()) {
                    Slot currentSlot = (net.minecraft.inventory.Slot) slotTransaction.getSlot();
                    ItemStack customStack =
                            slotTransaction.getFinal() == ItemStackSnapshot.NONE ? null : (net.minecraft.item.ItemStack) slotTransaction
                                    .getFinal().createStack();
                    currentSlot.putStack(customStack);
                    player.playerNetServerHandler
                            .sendPacket(new S2FPacketSetSlot(player.openContainer.windowId, currentSlot.slotNumber, customStack));
                }
            }

            // Custom cursor
            if (event.getCursorTransaction().getCustom().isPresent()) {
                ItemStack cursor =
                        event.getCursorTransaction().getFinal() == ItemStackSnapshot.NONE ? null
                                : (net.minecraft.item.ItemStack) event.getCursorTransaction().getOriginal()
                                        .createStack();
                player.inventory.setItemStack(cursor);
                player.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(-1, -1, cursor));
            }

            player.inventory.currentItem = packetIn.getSlotId();
            player.markPlayerActive();
        }
        return event;
    }

    // Open/Close
    public static void handleInteractInventoryOpenCloseEvent(Cause cause, EntityPlayerMP player, Packet packetIn) {
        if ((!(player.openContainer instanceof ContainerPlayer) && (StaticMixinHelper.lastOpenContainer instanceof ContainerPlayer)
                || (packetIn instanceof C16PacketClientStatus
                        && ((C16PacketClientStatus) packetIn).getStatus() == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT))) {
            ItemStackSnapshot newCursor =
                    player.inventory.getItemStack() == null ? ItemStackSnapshot.NONE
                            : ((org.spongepowered.api.item.inventory.ItemStack) player.inventory.getItemStack()).createSnapshot();
            Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<ItemStackSnapshot>(StaticMixinHelper.lastCursor, newCursor);
            InteractInventoryEvent.Open event =
                    SpongeEventFactory.createInteractInventoryEventOpen(Sponge.getGame(), cause, cursorTransaction,
                            (org.spongepowered.api.item.inventory.Container) player.openContainer);
            Sponge.getGame().getEventManager().post(event);
            if (event.isCancelled()) {
                player.closeScreen();
            } else {
                // Custom cursor
                if (event.getCursorTransaction().getCustom().isPresent()) {
                    ItemStack cursor =
                            event.getCursorTransaction().getFinal() == ItemStackSnapshot.NONE ? null
                                    : (net.minecraft.item.ItemStack) event.getCursorTransaction().getOriginal()
                                            .createStack();
                    player.inventory.setItemStack(cursor);
                    player.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(-1, -1, cursor));
                }
            }
        } else if (player.openContainer instanceof ContainerPlayer && !(StaticMixinHelper.lastOpenContainer instanceof ContainerPlayer)) {
            ItemStackSnapshot newCursor =
                    player.inventory.getItemStack() == null ? ItemStackSnapshot.NONE
                            : ((org.spongepowered.api.item.inventory.ItemStack) player.inventory.getItemStack()).createSnapshot();
            Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<ItemStackSnapshot>(StaticMixinHelper.lastCursor, newCursor);
            InteractInventoryEvent.Close event =
                    SpongeEventFactory.createInteractInventoryEventClose(Sponge.getGame(), cause, cursorTransaction,
                            (org.spongepowered.api.item.inventory.Container) StaticMixinHelper.lastOpenContainer);
            Sponge.getGame().getEventManager().post(event);
            if (event.isCancelled()) {
                if (StaticMixinHelper.lastOpenContainer.getSlot(0) != null) {
                    player.openContainer = StaticMixinHelper.lastOpenContainer;
                    Slot slot = player.openContainer.getSlot(0);
                    String guiId = "unknown";
                    if (slot.inventory instanceof IInteractionObject) {
                        guiId = ((IInteractionObject) slot.inventory).getGuiID();
                    }
                    slot.inventory.openInventory(player);
                    player.playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(player.openContainer.windowId, guiId, slot.inventory
                            .getDisplayName(), slot.inventory.getSizeInventory()));
                    // resync data to client
                    player.sendContainerToPlayer(player.openContainer);
                }
            } else {
                // Custom cursor
                if (event.getCursorTransaction().getCustom().isPresent()) {
                    ItemStack cursor =
                            event.getCursorTransaction().getFinal() == ItemStackSnapshot.NONE ? null
                                    : (net.minecraft.item.ItemStack) event.getCursorTransaction().getOriginal()
                                            .createStack();
                    player.inventory.setItemStack(cursor);
                    player.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(-1, -1, cursor));
                }
            }
        }
    }

    public static void handleCreativeClickInventoryEvent(Cause cause, EntityPlayerMP player, C10PacketCreativeInventoryAction packetIn) {
        IMixinWorld world = ((IMixinWorld) player.worldObj);
        ItemStackSnapshot newCursor =
                player.inventory.getItemStack() == null ? ItemStackSnapshot.NONE
                        : ((org.spongepowered.api.item.inventory.ItemStack) player.inventory.getItemStack()).createSnapshot();
        Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<ItemStackSnapshot>(StaticMixinHelper.lastCursor, newCursor);
        CreativeInventoryEvent event = null;
        if (packetIn.getSlotId() == -1 && world.getCapturedEntityItems().size() > 0) {
            Iterator<Entity> iterator = world.getCapturedEntityItems().iterator();
            ImmutableList.Builder<EntitySnapshot> entitySnapshotBuilder = new ImmutableList.Builder<EntitySnapshot>();
            while (iterator.hasNext()) {
                Entity currentEntity = iterator.next();
                SpongeHooks.setCreatorEntityNbt(((IMixinEntity) currentEntity).getSpongeData(), player.getUniqueID());
                entitySnapshotBuilder.add(currentEntity.createSnapshot());
            }
            event = SpongeEventFactory.createCreativeInventoryEventDrop(Sponge.getGame(), cause, cursorTransaction, world.getCapturedEntityItems(),
                    entitySnapshotBuilder.build(), (org.spongepowered.api.item.inventory.Container) player.openContainer, (World) player.worldObj,
                    ((IMixinContainer) player.openContainer).getCapturedTransactions());
        } else {
            if (((IMixinContainer) player.openContainer).getCapturedTransactions().size() == 0 && packetIn.getSlotId() >= 0) {
                Slot slot = player.openContainer.getSlot(packetIn.getSlotId());
                if (slot != null) {
                    SlotTransaction slotTransaction =
                            new SlotTransaction((org.spongepowered.api.item.inventory.Slot) slot, ItemStackSnapshot.NONE, ItemStackSnapshot.NONE);
                    ((IMixinContainer) player.openContainer).getCapturedTransactions().add(slotTransaction);
                }
            }
            event = SpongeEventFactory.createCreativeInventoryEventClick(Sponge.getGame(), cause, cursorTransaction,
                    (org.spongepowered.api.item.inventory.Container) player.openContainer,
                    ((IMixinContainer) player.openContainer).getCapturedTransactions());
        }
        Sponge.getGame().getEventManager().post(event);
        if (event.isCancelled()) {
            if (event instanceof CreativeInventoryEvent.Drop) {
                world.getCapturedEntityItems().clear();
            }

            // Restore cursor
            ItemStack cursor =
                    ((ClickInventoryEvent) event).getCursorTransaction().getOriginal() == ItemStackSnapshot.NONE ? null
                            : (net.minecraft.item.ItemStack) ((ClickInventoryEvent) event).getCursorTransaction().getOriginal()
                                    .createStack();
            player.inventory.setItemStack(cursor);
            player.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(-1, -1, cursor));

            // Restore target slots
            Iterator<SlotTransaction> iterator = event.getTransactions().iterator();
            while (iterator.hasNext()) {
                SlotTransaction slotTransaction = iterator.next();
                Slot slot = (net.minecraft.inventory.Slot) slotTransaction.getSlot();
                ItemStack originalStack =
                        slotTransaction.getOriginal() == ItemStackSnapshot.NONE ? null : (net.minecraft.item.ItemStack) slotTransaction
                                .getOriginal().createStack();
                slot.putStack(originalStack);
                player.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(player.openContainer.windowId, slot.slotNumber, originalStack));
                iterator.remove();
            }
        } else {
            for (SlotTransaction slotTransaction : event.getTransactions()) {
                if (slotTransaction.isValid() && slotTransaction.getCustom().isPresent()) {
                    Slot slot = (net.minecraft.inventory.Slot) slotTransaction.getSlot();
                    ItemStack customStack =
                            slotTransaction.getFinal() == ItemStackSnapshot.NONE ? null : (net.minecraft.item.ItemStack) slotTransaction
                                    .getFinal().createStack();
                    slot.putStack(customStack);
                    player.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(player.openContainer.windowId, slot.slotNumber, customStack));
                }
            }

            // Custom cursor
            if (((ClickInventoryEvent) event).getCursorTransaction().getCustom().isPresent()) {
                ItemStack cursor =
                        ((ClickInventoryEvent) event).getCursorTransaction().getFinal() == ItemStackSnapshot.NONE ? null
                                : (net.minecraft.item.ItemStack) ((ClickInventoryEvent) event).getCursorTransaction().getOriginal()
                                        .createStack();
                player.inventory.setItemStack(cursor);
                player.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(-1, -1, cursor));
            }
        }

        ((IMixinContainer) player.openContainer).getCapturedTransactions().clear();
    }

    public static void handleClickInteractInventoryEvent(Cause cause, EntityPlayerMP player, C0EPacketClickWindow packetIn) {
        IMixinWorld world = ((IMixinWorld) player.worldObj);
        ItemStackSnapshot newCursor =
                player.inventory.getItemStack() == null ? ItemStackSnapshot.NONE
                        : ((org.spongepowered.api.item.inventory.ItemStack) player.inventory.getItemStack()).createSnapshot();
        Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<ItemStackSnapshot>(StaticMixinHelper.lastCursor, newCursor);
        AffectSlotEvent clickEvent = null;
        // Handle empty slot clicks
        if (((IMixinContainer) player.openContainer).getCapturedTransactions().size() == 0 && packetIn.getSlotId() >= 0) {
            Slot slot = player.openContainer.getSlot(packetIn.getSlotId());
            if (slot != null) {
                SlotTransaction slotTransaction =
                        new SlotTransaction((org.spongepowered.api.item.inventory.Slot) slot, ItemStackSnapshot.NONE, ItemStackSnapshot.NONE);
                ((IMixinContainer) player.openContainer).getCapturedTransactions().add(slotTransaction);
            }
        }
        if (packetIn.getMode() == MODE_CLICK || packetIn.getMode() == MODE_PICKBLOCK) {
            if (packetIn.getUsedButton() == BUTTON_PRIMARY) {
                if (packetIn.getSlotId() == CLICK_OUTSIDE) {
                    Iterator<Entity> iterator = world.getCapturedEntityItems().iterator();
                    ImmutableList.Builder<EntitySnapshot> entitySnapshotBuilder = new ImmutableList.Builder<EntitySnapshot>();
                    while (iterator.hasNext()) {
                        Entity currentEntity = iterator.next();
                        SpongeHooks.setCreatorEntityNbt(((IMixinEntity) currentEntity).getSpongeData(), player.getUniqueID());
                        entitySnapshotBuilder.add(currentEntity.createSnapshot());
                    }
                    clickEvent =
                            SpongeEventFactory.createClickInventoryEventDropFull(Sponge.getGame(), cause, cursorTransaction,
                                    world.getCapturedEntities(), entitySnapshotBuilder.build(),
                                    (org.spongepowered.api.item.inventory.Container) player.openContainer, (World) world,
                                    ((IMixinContainer) player.openContainer).getCapturedTransactions());
                } else {
                    clickEvent =
                            SpongeEventFactory.createClickInventoryEventPrimary(Sponge.getGame(), cause, cursorTransaction,
                                    (org.spongepowered.api.item.inventory.Container) player.openContainer,
                                    ((IMixinContainer) player.openContainer).getCapturedTransactions());
                }
            } else if (packetIn.getUsedButton() == BUTTON_SECONDARY) {
                if (packetIn.getSlotId() == CLICK_OUTSIDE) {
                    Iterator<Entity> iterator = world.getCapturedEntityItems().iterator();
                    ImmutableList.Builder<EntitySnapshot> entitySnapshotBuilder = new ImmutableList.Builder<EntitySnapshot>();
                    while (iterator.hasNext()) {
                        Entity currentEntity = iterator.next();
                        SpongeHooks.setCreatorEntityNbt(((IMixinEntity) currentEntity).getSpongeData(), player.getUniqueID());
                        entitySnapshotBuilder.add(currentEntity.createSnapshot());
                    }
                    clickEvent =
                            SpongeEventFactory.createClickInventoryEventDropSingle(Sponge.getGame(), cause, cursorTransaction,
                                    world.getCapturedEntities(), entitySnapshotBuilder.build(),
                                    (org.spongepowered.api.item.inventory.Container) player.openContainer, (World) world,
                                    ((IMixinContainer) player.openContainer).getCapturedTransactions());
                } else {
                    clickEvent =
                            SpongeEventFactory.createClickInventoryEventSecondary(Sponge.getGame(), cause, cursorTransaction,
                                    (org.spongepowered.api.item.inventory.Container) player.openContainer,
                                    ((IMixinContainer) player.openContainer).getCapturedTransactions());
                }
            } else if (packetIn.getUsedButton() == BUTTON_MIDDLE) {
                clickEvent =
                        SpongeEventFactory.createClickInventoryEventMiddle(Sponge.getGame(), cause, cursorTransaction,
                                (org.spongepowered.api.item.inventory.Container) player.openContainer,
                                ((IMixinContainer) player.openContainer).getCapturedTransactions());
            }
        } else if (packetIn.getMode() == MODE_SHIFT_CLICK) {
            if (packetIn.getUsedButton() == BUTTON_PRIMARY) {
                clickEvent =
                        SpongeEventFactory.createClickInventoryEventShiftPrimary(Sponge.getGame(), cause, cursorTransaction,
                                (org.spongepowered.api.item.inventory.Container) player.openContainer,
                                ((IMixinContainer) player.openContainer).getCapturedTransactions());
            } else {
                clickEvent =
                        SpongeEventFactory.createClickInventoryEventShiftSecondary(Sponge.getGame(), cause, cursorTransaction,
                                (org.spongepowered.api.item.inventory.Container) player.openContainer,
                                ((IMixinContainer) player.openContainer).getCapturedTransactions());
            }
        } else if (packetIn.getMode() == MODE_HOTBAR) {
            clickEvent =
                    SpongeEventFactory.createInteractInventoryEventNumberPress(Sponge.getGame(), cause, cursorTransaction,
                            (org.spongepowered.api.item.inventory.Container) player.openContainer,
                            ((IMixinContainer) player.openContainer).getCapturedTransactions(), packetIn.getUsedButton());
        } else if (packetIn.getMode() == MODE_DROP) {
            if (packetIn.getUsedButton() == BUTTON_PRIMARY) {
                clickEvent =
                        SpongeEventFactory.createClickInventoryEventPrimary(Sponge.getGame(), cause, cursorTransaction,
                                (org.spongepowered.api.item.inventory.Container) player.openContainer,
                                ((IMixinContainer) player.openContainer).getCapturedTransactions());
            } else if (packetIn.getUsedButton() == BUTTON_SECONDARY) {
                clickEvent =
                        SpongeEventFactory.createClickInventoryEventSecondary(Sponge.getGame(), cause, cursorTransaction,
                                (org.spongepowered.api.item.inventory.Container) player.openContainer,
                                ((IMixinContainer) player.openContainer).getCapturedTransactions());
            }
        } else if (packetIn.getMode() == MODE_DRAG) {
            if (packetIn.getSlotId() == CLICK_OUTSIDE) {
                if (packetIn.getUsedButton() == CLICK_DRAG_LEFT) {
                    clickEvent =
                            SpongeEventFactory.createClickInventoryEventDragPrimary(Sponge.getGame(), cause, cursorTransaction,
                                    (org.spongepowered.api.item.inventory.Container) player.openContainer,
                                    ((IMixinContainer) player.openContainer).getCapturedTransactions());
                } else if (packetIn.getUsedButton() == CLICK_DRAG_RIGHT) {
                    clickEvent =
                            SpongeEventFactory.createClickInventoryEventDragSecondary(Sponge.getGame(), cause, cursorTransaction,
                                    (org.spongepowered.api.item.inventory.Container) player.openContainer,
                                    ((IMixinContainer) player.openContainer).getCapturedTransactions());
                }
            }
            if (clickEvent == null) {
                return; // continue capturing drag
            }
        } else if (packetIn.getMode() == MODE_DOUBLE_CLICK) {
            clickEvent =
                    SpongeEventFactory.createClickInventoryEventDouble(Sponge.getGame(), cause, cursorTransaction,
                            (org.spongepowered.api.item.inventory.Container) player.openContainer,
                            ((IMixinContainer) player.openContainer).getCapturedTransactions());
        }

        Sponge.getGame().getEventManager().post(clickEvent);

        if (clickEvent.isCancelled()) {
            if (clickEvent instanceof ClickInventoryEvent.Drop) {
                world.getCapturedEntityItems().clear();
            }

            // Restore cursor
            ItemStack cursor =
                    ((InteractInventoryEvent) clickEvent).getCursorTransaction().getOriginal() == ItemStackSnapshot.NONE ? null
                            : (net.minecraft.item.ItemStack) ((InteractInventoryEvent) clickEvent).getCursorTransaction().getOriginal()
                                    .createStack();
            player.inventory.setItemStack(cursor);
            player.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(-1, -1, cursor));

            // Restore target slots
            Iterator<SlotTransaction> iterator = clickEvent.getTransactions().iterator();
            while (iterator.hasNext()) {
                SlotTransaction slotTransaction = iterator.next();
                Slot slot = (net.minecraft.inventory.Slot) slotTransaction.getSlot();
                ItemStack originalStack =
                        slotTransaction.getOriginal() == ItemStackSnapshot.NONE ? null : (net.minecraft.item.ItemStack) slotTransaction
                                .getOriginal().createStack();
                slot.putStack(originalStack);
                player.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(packetIn.getWindowId(), slot.slotNumber, originalStack));
                iterator.remove();
            }
        } else {
            for (SlotTransaction slotTransaction : clickEvent.getTransactions()) {
                if (slotTransaction.isValid() && slotTransaction.getCustom().isPresent()) {
                    Slot slot = (net.minecraft.inventory.Slot) slotTransaction.getSlot();
                    ItemStack customStack =
                            slotTransaction.getFinal() == ItemStackSnapshot.NONE ? null : (net.minecraft.item.ItemStack) slotTransaction
                                    .getFinal().createStack();
                    slot.putStack(customStack);
                    player.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(packetIn.getWindowId(), slot.slotNumber, customStack));
                }
            }

            // Custom cursor
            if (((InteractInventoryEvent) clickEvent).getCursorTransaction().getCustom().isPresent()) {
                ItemStack cursor =
                        ((InteractInventoryEvent) clickEvent).getCursorTransaction().getFinal() == ItemStackSnapshot.NONE ? null
                                : (net.minecraft.item.ItemStack) ((InteractInventoryEvent) clickEvent).getCursorTransaction().getOriginal()
                                        .createStack();
                player.inventory.setItemStack(cursor);
                player.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(-1, -1, cursor));
            }
        }

        ((IMixinContainer) player.openContainer).getCapturedTransactions().clear();
    }
}
