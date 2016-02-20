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

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
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
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.IInteractionObject;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.Humanoid;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.CollideBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.event.entity.DisplaceEntityEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.CreativeInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.TrackingHelper;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.IMixinContainer;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.item.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.registry.provider.DirectionFacingProvider;
import org.spongepowered.common.util.StaticMixinHelper;
import org.spongepowered.common.util.VecHelper;

import java.util.ArrayList;
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
            handleCustomSlot(player, event.getTransactions());
            player.inventory.currentItem = packetIn.getSlotId();
            player.markPlayerActive();
        }
        return event;
    }

    // Open/Close
    public static void handleInteractInventoryOpenCloseEvent(Cause cause, EntityPlayerMP player, Packet<?> packetIn, IPhaseState phaseState,
            PhaseContext context) {
        final Container lastOpenContainer = context.firstNamed(TrackingHelper.OPEN_CONTAINER, Container.class).orElse(null);
        if ((!(player.openContainer instanceof ContainerPlayer) && (lastOpenContainer instanceof ContainerPlayer)
             || (packetIn instanceof C16PacketClientStatus
                && ((C16PacketClientStatus) packetIn).getStatus() == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT))) {
            ItemStackSnapshot lastCursor = context.firstNamed(TrackingHelper.CURSOR, ItemStackSnapshot.class).get();
            ItemStackSnapshot newCursor =
                    player.inventory.getItemStack() == null ? ItemStackSnapshot.NONE
                            : ((org.spongepowered.api.item.inventory.ItemStack) player.inventory.getItemStack()).createSnapshot();
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
            ItemStackSnapshot newCursor =
                    player.inventory.getItemStack() == null ? ItemStackSnapshot.NONE
                            : ((org.spongepowered.api.item.inventory.ItemStack) player.inventory.getItemStack()).createSnapshot();
            Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(lastCursor, newCursor);
            InteractInventoryEvent.Close event =
                    SpongeEventFactory.createInteractInventoryEventClose(cause, cursorTransaction,
                            (org.spongepowered.api.item.inventory.Container) lastOpenContainer);
            SpongeImpl.postEvent(event);
            if (event.isCancelled()) {
                if (lastOpenContainer.getSlot(0) != null) {
                    player.openContainer = lastOpenContainer;
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
                    handleCustomCursor(player, event.getCursorTransaction().getFinal());
                }
            }
        }
    }

    public static void handleCreativeClickInventoryEvent(Cause cause, EntityPlayerMP player, C10PacketCreativeInventoryAction packetIn,
            IPhaseState phaseState, PhaseContext context) {
        IMixinWorld world = ((IMixinWorld) player.worldObj);
        ItemStackSnapshot lastCursor = context.firstNamed(TrackingHelper.CURSOR, ItemStackSnapshot.class).get();
        ItemStackSnapshot newCursor =
                player.inventory.getItemStack() == null ? ItemStackSnapshot.NONE
                        : ((org.spongepowered.api.item.inventory.ItemStack) player.inventory.getItemStack()).createSnapshot();
        Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(lastCursor, newCursor);
        CauseTracker causeTracker = world.getCauseTracker();
        CreativeInventoryEvent event = null;
        if (packetIn.getSlotId() == -1 && causeTracker.getCapturedEntityItems().size() > 0) {
            Iterator<Entity> iterator = causeTracker.getCapturedEntityItems().iterator();
            ImmutableList.Builder<EntitySnapshot> entitySnapshotBuilder = new ImmutableList.Builder<>();
            while (iterator.hasNext()) {
                Entity currentEntity = iterator.next();
                ((IMixinEntity) currentEntity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, player.getUniqueID());
                entitySnapshotBuilder.add(currentEntity.createSnapshot());
            }
            event = SpongeEventFactory.createCreativeInventoryEventDrop(cause, cursorTransaction, causeTracker.getCapturedEntityItems(),
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
                causeTracker.getCapturedEntityItems().clear();
            }

            // Restore cursor
            handleCustomCursor(player, ((ClickInventoryEvent) event).getCursorTransaction().getOriginal());

            // Restore target slots
            handleSlotRestore(player, event.getTransactions());
        } else {
            handleCustomSlot(player, event.getTransactions());

            // Custom cursor
            if (((ClickInventoryEvent) event).getCursorTransaction().getCustom().isPresent()) {
                handleCustomCursor(player, ((ClickInventoryEvent) event).getCursorTransaction().getFinal());
            }
        }

        ((IMixinContainer) player.openContainer).getCapturedTransactions().clear();
    }

    public static void handleClickInteractInventoryEvent(Cause cause, EntityPlayerMP player, C0EPacketClickWindow packetIn, IPhaseState phaseState,
            PhaseContext phaseContext) {
        IMixinWorld world = ((IMixinWorld) player.worldObj);
        CauseTracker causeTracker = world.getCauseTracker();
        ItemStackSnapshot lastCursor = phaseContext.firstNamed(TrackingHelper.CURSOR, ItemStackSnapshot.class).get();
        ItemStackSnapshot newCursor = player.inventory.getItemStack() == null ? ItemStackSnapshot.NONE
                        : ((org.spongepowered.api.item.inventory.ItemStack) player.inventory.getItemStack()).createSnapshot();
        Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(lastCursor, newCursor);
        ClickInventoryEvent clickEvent = null;
        // Handle empty slot clicks
        final Container openContainer = player.openContainer;
        final IMixinContainer mixinContainer = (IMixinContainer) openContainer;
        final List<SlotTransaction> capturedTransactions = mixinContainer.getCapturedTransactions();
        if (capturedTransactions.size() == 0 && packetIn.getSlotId() >= 0) {
            Slot slot = openContainer.getSlot(packetIn.getSlotId());
            if (slot != null) {
                SlotTransaction slotTransaction = new SlotTransaction(new SlotAdapter(slot), ItemStackSnapshot.NONE, ItemStackSnapshot.NONE);
                capturedTransactions.add(slotTransaction);
            }
        }
        final org.spongepowered.api.item.inventory.Container spongeContainer = (org.spongepowered.api.item.inventory.Container) openContainer;
        final int clickMode = packetIn.getMode();
        final int usedButton = packetIn.getUsedButton();
        if (clickMode == MODE_CLICK || clickMode == MODE_PICKBLOCK) {
            if (usedButton == BUTTON_PRIMARY) {
                if (packetIn.getSlotId() == CLICK_OUTSIDE) {
                    Iterator<Entity> iterator = causeTracker.getCapturedEntityItems().iterator();
                    ImmutableList.Builder<EntitySnapshot> entitySnapshotBuilder = new ImmutableList.Builder<>();
                    while (iterator.hasNext()) {
                        Entity currentEntity = iterator.next();
                        ((IMixinEntity) currentEntity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, player.getUniqueID());
                        entitySnapshotBuilder.add(currentEntity.createSnapshot());
                    }
                    clickEvent = SpongeEventFactory.createClickInventoryEventDropFull(cause, cursorTransaction, causeTracker.getCapturedEntities(),
                            entitySnapshotBuilder.build(), spongeContainer, (World) world, capturedTransactions);
                } else {
                    clickEvent =
                            SpongeEventFactory.createClickInventoryEventPrimary(cause, cursorTransaction,
                                    spongeContainer,
                                    capturedTransactions);
                }
            } else if (usedButton == BUTTON_SECONDARY) {
                if (packetIn.getSlotId() == CLICK_OUTSIDE) {
                    Iterator<Entity> iterator = causeTracker.getCapturedEntityItems().iterator();
                    ImmutableList.Builder<EntitySnapshot> entitySnapshotBuilder = new ImmutableList.Builder<>();
                    while (iterator.hasNext()) {
                        Entity currentEntity = iterator.next();
                        ((IMixinEntity) currentEntity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, player.getUniqueID());
                        entitySnapshotBuilder.add(currentEntity.createSnapshot());
                    }
                    clickEvent = SpongeEventFactory.createClickInventoryEventDropSingle(cause, cursorTransaction, causeTracker.getCapturedEntities(),
                            entitySnapshotBuilder.build(), spongeContainer, (World) world, capturedTransactions);
                } else {
                    clickEvent = SpongeEventFactory.createClickInventoryEventSecondary(cause, cursorTransaction, spongeContainer, capturedTransactions);
                }
            } else if (usedButton == BUTTON_MIDDLE) {
                clickEvent = SpongeEventFactory.createClickInventoryEventMiddle(cause, cursorTransaction, spongeContainer, capturedTransactions);
            }
        } else if (clickMode == MODE_SHIFT_CLICK) {
            if (usedButton == BUTTON_PRIMARY) {
                clickEvent = SpongeEventFactory.createClickInventoryEventShiftPrimary(cause, cursorTransaction, spongeContainer, capturedTransactions);
            } else {
                clickEvent = SpongeEventFactory.createClickInventoryEventShiftSecondary(cause, cursorTransaction, spongeContainer, capturedTransactions);
            }
        } else if (clickMode == MODE_HOTBAR) {
            clickEvent = SpongeEventFactory.createClickInventoryEventNumberPress(cause, cursorTransaction, spongeContainer,
                    capturedTransactions, usedButton);
        } else if (clickMode == MODE_DROP) {
            if (usedButton == BUTTON_PRIMARY) {
                clickEvent = SpongeEventFactory.createClickInventoryEventPrimary(cause, cursorTransaction, spongeContainer,
                        capturedTransactions);
            } else if (usedButton == BUTTON_SECONDARY) {
                clickEvent = SpongeEventFactory.createClickInventoryEventSecondary(cause, cursorTransaction, spongeContainer, capturedTransactions);
            }
        } else if (clickMode == MODE_DRAG) {
            if (packetIn.getSlotId() == CLICK_OUTSIDE) {
                if (usedButton == CLICK_DRAG_LEFT) {
                    clickEvent = SpongeEventFactory.createClickInventoryEventDragPrimary(cause, cursorTransaction, spongeContainer,
                            capturedTransactions);
                } else if (usedButton == CLICK_DRAG_RIGHT) {
                    clickEvent = SpongeEventFactory.createClickInventoryEventDragSecondary(cause, cursorTransaction, spongeContainer,
                            capturedTransactions);
                }
            }
            if (clickEvent == null) {
                return; // continue capturing drag
            }
        } else if (clickMode == MODE_DOUBLE_CLICK) {
            clickEvent = SpongeEventFactory.createClickInventoryEventDouble(cause, cursorTransaction, spongeContainer,
                    capturedTransactions);
        }

        SpongeImpl.postEvent(clickEvent);

        if (clickEvent.isCancelled()) {
            if (clickEvent instanceof ClickInventoryEvent.Drop) {
                causeTracker.getCapturedEntityItems().clear();
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

        capturedTransactions.clear();
    }

    private static void handleSlotRestore(EntityPlayerMP player, List<SlotTransaction> slotTransactions) {
        for (SlotTransaction slotTransaction : slotTransactions) {
            SlotAdapter slot = (SlotAdapter) slotTransaction.getSlot();
            int slotNumber = slot.slotNumber;
            ItemStack originalStack = slotTransaction.getOriginal() == ItemStackSnapshot.NONE
                                      ? null : (net.minecraft.item.ItemStack) slotTransaction.getOriginal().createStack();

            // TODO: fix below
            /*if (originalStack == null) {
                slot.clear();
            } else {
                slot.offer((org.spongepowered.api.item.inventory.ItemStack) originalStack);
            }*/

            Slot nmsSlot = player.inventoryContainer.getSlot(slotNumber);
            if (nmsSlot != null) {
                nmsSlot.putStack(originalStack);
            }

            player.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(player.openContainer.windowId, slotNumber, originalStack));
        }
    }

    private static void handleCustomCursor(EntityPlayerMP player, ItemStackSnapshot customCursor) {
        ItemStack cursor =
                customCursor == ItemStackSnapshot.NONE ? null
                        : (net.minecraft.item.ItemStack) customCursor
                        .createStack();
        player.inventory.setItemStack(cursor);
        player.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(-1, -1, cursor));
    }

    private static void handleCustomSlot(EntityPlayerMP player, List<SlotTransaction> slotTransactions) {
        for (SlotTransaction slotTransaction : slotTransactions) {
            if (slotTransaction.isValid() && slotTransaction.getCustom().isPresent()) {
                SlotAdapter slot = (SlotAdapter) slotTransaction.getSlot();
                int slotNumber = slot.slotNumber;
                ItemStack customStack =
                        slotTransaction.getFinal() == ItemStackSnapshot.NONE ? null : (net.minecraft.item.ItemStack) slotTransaction
                                .getFinal().createStack();

                // TODO: fix below
                /*if (customStack == null) {
                    slot.clear();
                } else {
                    slot.offer((org.spongepowered.api.item.inventory.ItemStack) customStack);
                }*/

                Slot nmsSlot = player.inventoryContainer.getSlot(slotNumber);
                if (nmsSlot != null) {
                    nmsSlot.putStack(customStack);
                }

                player.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(player.openContainer.windowId, slotNumber, customStack));
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static CollideEntityEvent callCollideEntityEvent(net.minecraft.world.World world, net.minecraft.entity.Entity sourceEntity,
                                                            List<net.minecraft.entity.Entity> entities) {
        Cause cause = null;
        if (sourceEntity != null) {
            cause = Cause.of(NamedCause.source(sourceEntity));
        } else {
            IMixinWorld spongeWorld = (IMixinWorld) world;
            CauseTracker causeTracker = spongeWorld.getCauseTracker();
            PhaseContext context = causeTracker.getPhases().peekContext();

            final Optional<BlockSnapshot> currentTickingBlock = context.firstNamed(TrackingHelper.CURRENT_TICK_BLOCK, BlockSnapshot.class);
            final Optional<TileEntity> currentTickingTileEntity = context.firstNamed(NamedCause.SOURCE, TileEntity.class);
            final Optional<Entity> currentTickingEntity = context.firstNamed(NamedCause.SOURCE, Entity.class);
            if (currentTickingBlock.isPresent()) {
                cause = Cause.of(NamedCause.source(currentTickingBlock.get()));
            } else if (currentTickingTileEntity.isPresent()) {
                cause = Cause.of(NamedCause.source(currentTickingTileEntity.get()));
            } else if (currentTickingEntity.isPresent()) {
                cause = Cause.of(NamedCause.source(currentTickingEntity.get()));
            }

            if (cause == null) {
                return null;
            }
        }

        ImmutableList<Entity> originalEntities =
                ImmutableList.copyOf((List<Entity>) (List<?>) entities);
        CollideEntityEvent event = SpongeEventFactory.createCollideEntityEvent(cause, originalEntities,
                (List<Entity>) (List<?>) entities, (World) world);
        SpongeImpl.postEvent(event);
        return event;
    }

    @SuppressWarnings("rawtypes")
    public static NotifyNeighborBlockEvent callNotifyNeighborEvent(World world, BlockPos pos, EnumSet notifiedSides) {
        final CauseTracker causeTracker = ((IMixinWorld) world).getCauseTracker();
        final PhaseData currentPhase = causeTracker.getPhases().peek();
        Optional<User> playerNotifier = currentPhase.getContext().firstNamed(TrackingHelper.PACKET_PLAYER, User.class);
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
            if (playerNotifier.isPresent()) {
                cause = Cause.of(NamedCause.source(snapshot)).with(NamedCause.notifier(playerNotifier));
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

    public static boolean handleImpactEvent(net.minecraft.entity.Entity projectile, ProjectileSource projectileSource, MovingObjectPosition
            movingObjectPosition) {
        MovingObjectType movingObjectType = movingObjectPosition.typeOfHit;
        Cause cause = Cause.of(projectile, projectileSource == null ? ProjectileSource.UNKNOWN : projectileSource);
        IMixinEntity spongeEntity = (IMixinEntity) projectile;
        Optional<User> owner = spongeEntity.getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR);
        if (owner.isPresent() && !cause.containsNamed(NamedCause.OWNER)) {
            cause = cause.with(NamedCause.of(NamedCause.OWNER, owner.get()));
        }

        Location<World> impactPoint = new Location<World>((World) projectile.worldObj, VecHelper.toVector(movingObjectPosition.hitVec));

        if (movingObjectType == MovingObjectType.BLOCK) {
            BlockSnapshot targetBlock = ((World) projectile.worldObj).createSnapshot(VecHelper.toVector(movingObjectPosition.getBlockPos()));
            Direction side = Direction.NONE;
            if (movingObjectPosition.sideHit != null) {
                side = DirectionFacingProvider.getInstance().getKey(movingObjectPosition.sideHit).get();
            }

            CollideBlockEvent.Impact event = SpongeEventFactory.createCollideBlockEventImpact(cause, impactPoint, targetBlock.getState(),
                    targetBlock.getLocation().get(), side);
            return SpongeImpl.postEvent(event);
        } else if (movingObjectPosition.entityHit != null) { // entity
            ImmutableList.Builder<Entity> entityBuilder = new ImmutableList.Builder<>();
            ArrayList<Entity> entityList = new ArrayList<>();
            entityList.add((Entity) movingObjectPosition.entityHit);
            CollideEntityEvent.Impact event = SpongeEventFactory.createCollideEntityEventImpact(cause,
                    entityBuilder.add((Entity) movingObjectPosition.entityHit).build(), entityList, impactPoint, (World) projectile.worldObj);
            return SpongeImpl.postEvent(event);
        }

        return false;
    }


    public static void handleEntityMovement(net.minecraft.entity.Entity entity) {
        if (entity instanceof Player) {
            return; // this is handled elsewhere
        }
        if (entity.lastTickPosX != entity.posX || entity.lastTickPosY != entity.posY || entity.lastTickPosZ != entity.posZ
            || entity.rotationPitch != entity.prevRotationPitch || entity.rotationYaw != entity.prevRotationYaw) {
            // yes we have a move event.
            final double currentPosX = entity.posX;
            final double currentPosY = entity.posY;
            final double currentPosZ = entity.posZ;
            final Vector3d currentPositionVector = new Vector3d(currentPosX, currentPosY, currentPosZ);
            final double currentRotPitch = entity.rotationPitch;
            final double currentRotYaw = entity.rotationYaw;
            Vector3d currentRotationVector = new Vector3d(currentRotPitch, currentRotYaw, 0);
            DisplaceEntityEvent.Move event;
            Transform<World> previous = new Transform<>(((World) entity.worldObj),
                    new Vector3d(entity.prevPosX, entity.prevPosY, entity.prevPosZ), new Vector3d(entity.prevRotationPitch, entity.prevRotationYaw,
                    0));
            Location<World> currentLocation = new Location<>(((World) entity.worldObj), currentPosX, currentPosY, currentPosZ);
            Transform<World> current = new Transform<>(currentLocation, currentRotationVector, ((Entity) entity).getScale());

            if (entity instanceof Humanoid) {
                event = SpongeEventFactory.createDisplaceEntityEventMoveTargetHumanoid(Cause.of(NamedCause.source(entity)), previous, current,
                        (Humanoid) entity);
            } else if (entity instanceof Living) {
                event = SpongeEventFactory.createDisplaceEntityEventMoveTargetLiving(Cause.of(NamedCause.source(entity)), previous, current,
                        (Living) entity);
            } else {
                event = SpongeEventFactory.createDisplaceEntityEventMove(Cause.of(NamedCause.source(entity)), previous, current,
                        (Entity) entity);
            }
            SpongeImpl.postEvent(event);
            if (event.isCancelled()) {
                entity.posX = entity.lastTickPosX;
                entity.posY = entity.lastTickPosY;
                entity.posZ = entity.lastTickPosZ;
                entity.rotationPitch = entity.prevRotationPitch;
                entity.rotationYaw = entity.prevRotationYaw;
            } else {
                Transform<World> worldTransform = event.getToTransform();
                Vector3d eventPosition = worldTransform.getPosition();
                Vector3d eventRotation = worldTransform.getRotation();
                if (!eventPosition.equals(currentPositionVector)) {
                    entity.posX = eventPosition.getX();
                    entity.posY = eventPosition.getY();
                    entity.posZ = eventPosition.getZ();
                }
                if (!eventRotation.equals(currentRotationVector)) {
                    entity.rotationPitch = (float) currentRotationVector.getX();
                    entity.rotationYaw = (float) currentRotationVector.getY();
                }
                //entity.setPositionAndRotation(position.getX(), position.getY(), position.getZ(), rotation.getFloorX(), rotation.getFloorY());
                /*
                Some thoughts from gabizou: The interesting thing here is that while this is only called
                in World.updateEntityWithOptionalForce, by default, it supposedly handles updating the rider entity
                of the entity being handled here. The interesting issue is that since we are setting the transform,
                the rider entity (and the rest of the rider entities) are being updated as well with the new position
                and potentially world, which results in a dirty world usage (since the world transfer is handled by
                us). Now, the thing is, the previous position is not updated either, and likewise, the current position
                is being set by us as well. So, there's some issue I'm sure that is bound to happen with this
                logic.
                 */
                //((Entity) entity).setTransform(event.getToTransform());
            }
        }
    }
}
