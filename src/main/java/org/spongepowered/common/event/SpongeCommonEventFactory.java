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
import com.google.common.collect.ImmutableMap;
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
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IInteractionObject;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.CreativeInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.IMixinContainer;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.item.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.registry.provider.DirectionFacingProvider;
import org.spongepowered.common.util.StaticMixinHelper;
import org.spongepowered.common.util.VecHelper;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    public static ChangeInventoryEvent.Held callChangeInventoryHeldEvent(EntityPlayerMP player,
            C09PacketHeldItemChange packetIn) {
        Slot sourceSlot = player.inventoryContainer.getSlot(player.inventory.currentItem + player.inventory.mainInventory.length);
        Slot targetSlot = player.inventoryContainer.getSlot(packetIn.getSlotId() + player.inventory.mainInventory.length);
        if (sourceSlot == null || targetSlot == null) {
            return null; // should never happen but just in case it does
        }

        ItemStackSnapshot sourceSnapshot =
                sourceSlot.getStack() != null ? ((org.spongepowered.api.item.inventory.ItemStack) sourceSlot.getStack()).createSnapshot()
                        : ItemStackSnapshot.NONE;
        ItemStackSnapshot targetSnapshot = targetSlot.getStack() != null
                ? ((org.spongepowered.api.item.inventory.ItemStack) targetSlot.getStack()).createSnapshot() : ItemStackSnapshot.NONE;
        SlotTransaction sourceTransaction =
                new SlotTransaction(new SlotAdapter(sourceSlot), sourceSnapshot, sourceSnapshot);
        SlotTransaction targetTransaction =
                new SlotTransaction(new SlotAdapter(targetSlot), targetSnapshot, targetSnapshot);
        ImmutableList<SlotTransaction> transactions =
                new ImmutableList.Builder<SlotTransaction>().add(sourceTransaction).add(targetTransaction).build();
        ChangeInventoryEvent.Held event =
                SpongeEventFactory.createChangeInventoryEventHeld(Cause.of(NamedCause.source(player)),
                        (Inventory) player.inventoryContainer, transactions);
        SpongeImpl.postEvent(event);

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
            Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(StaticMixinHelper.lastCursor, newCursor);
            InteractInventoryEvent.Open event =
                    SpongeEventFactory.createInteractInventoryEventOpen(cause, cursorTransaction,
                            (org.spongepowered.api.item.inventory.Container) player.openContainer);
            SpongeImpl.postEvent(event);
            if (event.isCancelled()) {
                player.closeScreen();
            } else {
                // Custom cursor
                if (event.getCursorTransaction().getCustom().isPresent()) {
                    ItemStack cursor =
                            event.getCursorTransaction().getFinal() == ItemStackSnapshot.NONE ? null
                                    : (net.minecraft.item.ItemStack) event.getCursorTransaction().getFinal()
                                            .createStack();
                    player.inventory.setItemStack(cursor);
                    player.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(-1, -1, cursor));
                }
            }
        } else if (player.openContainer instanceof ContainerPlayer && !(StaticMixinHelper.lastOpenContainer instanceof ContainerPlayer)) {
            ItemStackSnapshot newCursor =
                    player.inventory.getItemStack() == null ? ItemStackSnapshot.NONE
                            : ((org.spongepowered.api.item.inventory.ItemStack) player.inventory.getItemStack()).createSnapshot();
            Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(StaticMixinHelper.lastCursor, newCursor);
            InteractInventoryEvent.Close event =
                    SpongeEventFactory.createInteractInventoryEventClose(cause, cursorTransaction,
                            (org.spongepowered.api.item.inventory.Container) StaticMixinHelper.lastOpenContainer);
            SpongeImpl.postEvent(event);
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
                                    : (net.minecraft.item.ItemStack) event.getCursorTransaction().getFinal()
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
        Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(StaticMixinHelper.lastCursor, newCursor);
        CreativeInventoryEvent event = null;
        if (packetIn.getSlotId() == -1 && world.getCapturedEntityItems().size() > 0) {
            Iterator<Entity> iterator = world.getCapturedEntityItems().iterator();
            ImmutableList.Builder<EntitySnapshot> entitySnapshotBuilder = new ImmutableList.Builder<>();
            while (iterator.hasNext()) {
                Entity currentEntity = iterator.next();
                ((IMixinEntity) currentEntity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, player.getUniqueID());
                entitySnapshotBuilder.add(currentEntity.createSnapshot());
            }
            event = SpongeEventFactory.createCreativeInventoryEventDrop(cause, cursorTransaction, world.getCapturedEntityItems(),
                    entitySnapshotBuilder.build(), (org.spongepowered.api.item.inventory.Container) player.openContainer, (World) player.worldObj,
                    ((IMixinContainer) player.openContainer).getCapturedTransactions());
        } else {
            if (((IMixinContainer) player.openContainer).getCapturedTransactions().size() == 0 && packetIn.getSlotId() >= 0) {
                Slot slot = player.openContainer.getSlot(packetIn.getSlotId());
                if (slot != null) {
                    SlotTransaction slotTransaction =
                            new SlotTransaction(new SlotAdapter(slot), ItemStackSnapshot.NONE, ItemStackSnapshot.NONE);
                    ((IMixinContainer) player.openContainer).getCapturedTransactions().add(slotTransaction);
                }
            }
            event = SpongeEventFactory.createCreativeInventoryEventClick(cause, cursorTransaction,
                    (org.spongepowered.api.item.inventory.Container) player.openContainer,
                    ((IMixinContainer) player.openContainer).getCapturedTransactions());
        }
        SpongeImpl.postEvent(event);
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
                                : (net.minecraft.item.ItemStack) ((ClickInventoryEvent) event).getCursorTransaction().getFinal()
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
        Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(StaticMixinHelper.lastCursor, newCursor);
        ClickInventoryEvent clickEvent = null;
        // Handle empty slot clicks
        if (((IMixinContainer) player.openContainer).getCapturedTransactions().size() == 0 && packetIn.getSlotId() >= 0) {
            Slot slot = player.openContainer.getSlot(packetIn.getSlotId());
            if (slot != null) {
                SlotTransaction slotTransaction =
                        new SlotTransaction(new SlotAdapter(slot), ItemStackSnapshot.NONE, ItemStackSnapshot.NONE);
                ((IMixinContainer) player.openContainer).getCapturedTransactions().add(slotTransaction);
            }
        }
        if (packetIn.getMode() == MODE_CLICK || packetIn.getMode() == MODE_PICKBLOCK) {
            if (packetIn.getUsedButton() == BUTTON_PRIMARY) {
                if (packetIn.getSlotId() == CLICK_OUTSIDE) {
                    Iterator<Entity> iterator = world.getCapturedEntityItems().iterator();
                    ImmutableList.Builder<EntitySnapshot> entitySnapshotBuilder = new ImmutableList.Builder<>();
                    while (iterator.hasNext()) {
                        Entity currentEntity = iterator.next();
                        ((IMixinEntity) currentEntity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, player.getUniqueID());
                        entitySnapshotBuilder.add(currentEntity.createSnapshot());
                    }
                    clickEvent =
                            SpongeEventFactory.createClickInventoryEventDropFull(cause, cursorTransaction,
                                    world.getCapturedEntities(), entitySnapshotBuilder.build(),
                                    (org.spongepowered.api.item.inventory.Container) player.openContainer, (World) world,
                                    ((IMixinContainer) player.openContainer).getCapturedTransactions());
                } else {
                    clickEvent =
                            SpongeEventFactory.createClickInventoryEventPrimary(cause, cursorTransaction,
                                    (org.spongepowered.api.item.inventory.Container) player.openContainer,
                                    ((IMixinContainer) player.openContainer).getCapturedTransactions());
                }
            } else if (packetIn.getUsedButton() == BUTTON_SECONDARY) {
                if (packetIn.getSlotId() == CLICK_OUTSIDE) {
                    Iterator<Entity> iterator = world.getCapturedEntityItems().iterator();
                    ImmutableList.Builder<EntitySnapshot> entitySnapshotBuilder = new ImmutableList.Builder<>();
                    while (iterator.hasNext()) {
                        Entity currentEntity = iterator.next();
                        ((IMixinEntity) currentEntity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, player.getUniqueID());
                        entitySnapshotBuilder.add(currentEntity.createSnapshot());
                    }
                    clickEvent =
                            SpongeEventFactory.createClickInventoryEventDropSingle(cause, cursorTransaction,
                                    world.getCapturedEntities(), entitySnapshotBuilder.build(),
                                    (org.spongepowered.api.item.inventory.Container) player.openContainer, (World) world,
                                    ((IMixinContainer) player.openContainer).getCapturedTransactions());
                } else {
                    clickEvent =
                            SpongeEventFactory.createClickInventoryEventSecondary(cause, cursorTransaction,
                                    (org.spongepowered.api.item.inventory.Container) player.openContainer,
                                    ((IMixinContainer) player.openContainer).getCapturedTransactions());
                }
            } else if (packetIn.getUsedButton() == BUTTON_MIDDLE) {
                clickEvent =
                        SpongeEventFactory.createClickInventoryEventMiddle(cause, cursorTransaction,
                                (org.spongepowered.api.item.inventory.Container) player.openContainer,
                                ((IMixinContainer) player.openContainer).getCapturedTransactions());
            }
        } else if (packetIn.getMode() == MODE_SHIFT_CLICK) {
            if (packetIn.getUsedButton() == BUTTON_PRIMARY) {
                clickEvent =
                        SpongeEventFactory.createClickInventoryEventShiftPrimary(cause, cursorTransaction,
                                (org.spongepowered.api.item.inventory.Container) player.openContainer,
                                ((IMixinContainer) player.openContainer).getCapturedTransactions());
            } else {
                clickEvent =
                        SpongeEventFactory.createClickInventoryEventShiftSecondary(cause, cursorTransaction,
                                (org.spongepowered.api.item.inventory.Container) player.openContainer,
                                ((IMixinContainer) player.openContainer).getCapturedTransactions());
            }
        } else if (packetIn.getMode() == MODE_HOTBAR) {
            clickEvent =
                    SpongeEventFactory.createClickInventoryEventNumberPress(cause, cursorTransaction,
                            (org.spongepowered.api.item.inventory.Container) player.openContainer,
                            ((IMixinContainer) player.openContainer).getCapturedTransactions(), packetIn.getUsedButton());
        } else if (packetIn.getMode() == MODE_DROP) {
            if (packetIn.getUsedButton() == BUTTON_PRIMARY) {
                clickEvent =
                        SpongeEventFactory.createClickInventoryEventPrimary(cause, cursorTransaction,
                                (org.spongepowered.api.item.inventory.Container) player.openContainer,
                                ((IMixinContainer) player.openContainer).getCapturedTransactions());
            } else if (packetIn.getUsedButton() == BUTTON_SECONDARY) {
                clickEvent =
                        SpongeEventFactory.createClickInventoryEventSecondary(cause, cursorTransaction,
                                (org.spongepowered.api.item.inventory.Container) player.openContainer,
                                ((IMixinContainer) player.openContainer).getCapturedTransactions());
            }
        } else if (packetIn.getMode() == MODE_DRAG) {
            if (packetIn.getSlotId() == CLICK_OUTSIDE) {
                if (packetIn.getUsedButton() == CLICK_DRAG_LEFT) {
                    clickEvent =
                            SpongeEventFactory.createClickInventoryEventDragPrimary(cause, cursorTransaction,
                                    (org.spongepowered.api.item.inventory.Container) player.openContainer,
                                    ((IMixinContainer) player.openContainer).getCapturedTransactions());
                } else if (packetIn.getUsedButton() == CLICK_DRAG_RIGHT) {
                    clickEvent =
                            SpongeEventFactory.createClickInventoryEventDragSecondary(cause, cursorTransaction,
                                    (org.spongepowered.api.item.inventory.Container) player.openContainer,
                                    ((IMixinContainer) player.openContainer).getCapturedTransactions());
                }
            }
            if (clickEvent == null) {
                return; // continue capturing drag
            }
        } else if (packetIn.getMode() == MODE_DOUBLE_CLICK) {
            clickEvent =
                    SpongeEventFactory.createClickInventoryEventDouble(cause, cursorTransaction,
                            (org.spongepowered.api.item.inventory.Container) player.openContainer,
                            ((IMixinContainer) player.openContainer).getCapturedTransactions());
        }

        SpongeImpl.postEvent(clickEvent);

        if (clickEvent.isCancelled()) {
            if (clickEvent instanceof ClickInventoryEvent.Drop) {
                world.getCapturedEntityItems().clear();
            }

            // Restore cursor
            ItemStack cursor =
                    clickEvent.getCursorTransaction().getOriginal() == ItemStackSnapshot.NONE ? null
                            : (net.minecraft.item.ItemStack) clickEvent.getCursorTransaction().getOriginal()
                                    .createStack();
            player.inventory.setItemStack(cursor);
            player.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(-1, -1, cursor));

            if (clickEvent instanceof ClickInventoryEvent.Double) {
                clickEvent.getTransactions().clear();
                return;
            }

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
            if (clickEvent.getCursorTransaction().getCustom().isPresent()) {
                ItemStack cursor =
                        clickEvent.getCursorTransaction().getFinal() == ItemStackSnapshot.NONE ? null
                                : (net.minecraft.item.ItemStack) clickEvent.getCursorTransaction().getFinal()
                                        .createStack();
                player.inventory.setItemStack(cursor);
                player.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(-1, -1, cursor));
            }
        }

        ((IMixinContainer) player.openContainer).getCapturedTransactions().clear();
    }

    @SuppressWarnings("unchecked")
    public static CollideEntityEvent callCollideEntityEvent(net.minecraft.world.World world, net.minecraft.entity.Entity sourceEntity,
            List<net.minecraft.entity.Entity> entities) {
        Cause cause = null;
        if (sourceEntity != null) {
            cause = Cause.of(NamedCause.source(sourceEntity));
        } else {
            IMixinWorld spongeWorld = (IMixinWorld) world;
            if (spongeWorld.getCurrentTickTileEntity().isPresent()) {
                cause = Cause.of(NamedCause.source(spongeWorld.getCurrentTickTileEntity().get()));
            } else if (spongeWorld.getCurrentTickBlock().isPresent()) {
                cause = Cause.of(NamedCause.source(spongeWorld.getCurrentTickBlock().get()));
            } else if (spongeWorld.getCurrentTickEntity().isPresent()) {
                cause = Cause.of(NamedCause.source(spongeWorld.getCurrentTickEntity().get()));
            }

            if (cause == null) {
                return null;
            }
        }

        ImmutableList<org.spongepowered.api.entity.Entity> originalEntities =
                ImmutableList.copyOf((List<org.spongepowered.api.entity.Entity>) (List<?>) entities);
        CollideEntityEvent event = SpongeEventFactory.createCollideEntityEvent(cause, originalEntities,
                (List<org.spongepowered.api.entity.Entity>) (List<?>) entities, (org.spongepowered.api.world.World) world);
        SpongeImpl.postEvent(event);
        return event;
    }

    @SuppressWarnings("rawtypes")
    public static NotifyNeighborBlockEvent callNotifyNeighborEvent(World world, BlockPos pos, EnumSet notifiedSides) {
        BlockSnapshot snapshot = world.createSnapshot(VecHelper.toVector(pos));
        Map<Direction, BlockState> neighbors = new HashMap<Direction, BlockState>();

        if (notifiedSides != null) {
            for (Object obj : notifiedSides) {
                EnumFacing notifiedSide = (EnumFacing) obj;
                BlockPos offset = pos.offset(notifiedSide);
                Direction direction = DirectionFacingProvider.getInstance().getKey(notifiedSide).get();
                Location<World> location = new Location<World>((World) world, VecHelper.toVector(offset));
                if (location.getBlockY() >= 0 && location.getBlockY() <= 255) {
                    neighbors.put(direction, location.getBlock());
                }
            }
        }

        ImmutableMap<Direction, BlockState> originalNeighbors = ImmutableMap.copyOf(neighbors);
        // Determine cause
        Cause cause = Cause.of(NamedCause.source(snapshot));
        net.minecraft.world.World nmsWorld = (net.minecraft.world.World) world;
        IMixinChunk spongeChunk = (IMixinChunk) nmsWorld.getChunkFromBlockCoords(pos);
        if (spongeChunk != null) {
            if (StaticMixinHelper.packetPlayer != null) {
                cause = Cause.of(NamedCause.source(snapshot)).with(NamedCause.notifier(StaticMixinHelper.packetPlayer));
            } else {
                Optional<User> notifier = spongeChunk.getBlockNotifier(pos);
                if (notifier.isPresent()) {
                    cause = Cause.of(NamedCause.source(snapshot)).with(NamedCause.notifier(notifier.get()));
                }
            }
            Optional<User> owner = spongeChunk.getBlockOwner(pos);
            if (owner.isPresent()) {
                cause = cause.with(NamedCause.owner(owner.get()));
            }
        }

        NotifyNeighborBlockEvent event = SpongeEventFactory.createNotifyNeighborBlockEvent(cause, originalNeighbors, neighbors);
        StaticMixinHelper.processingInternalForgeEvent = true;
        SpongeImpl.postEvent(event);
        StaticMixinHelper.processingInternalForgeEvent = false;
        return event;
    }
}
