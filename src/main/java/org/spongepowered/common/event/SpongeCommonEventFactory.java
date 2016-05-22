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

import static com.google.common.base.Preconditions.checkArgument;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Items;
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
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.CombatEntry;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.Ageable;
import org.spongepowered.api.entity.living.Humanoid;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.CollideBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.BlockSpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.DisplaceEntityEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.CreativeInventoryEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.event.message.MessageEvent.DefaultBodyApplier;
import org.spongepowered.api.event.message.MessageEvent.MessageFormatter;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.PortalAgent;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.entity.teleport.SpongePortalTeleportCause;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.IMixinContainer;
import org.spongepowered.common.interfaces.IMixinServerConfigurationManager;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.network.IMixinNetHandlerPlayServer;
import org.spongepowered.common.interfaces.world.IMixinTeleporter;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.item.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.registry.provider.DirectionFacingProvider;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.StaticMixinHelper;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.gen.InternalPopulatorTypes;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

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
    public static void handleInteractInventoryOpenCloseEvent(Cause cause, EntityPlayerMP player, Packet<?> packetIn) {
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
                    handleCustomCursor(player, event.getCursorTransaction().getFinal());
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
                    handleCustomCursor(player, event.getCursorTransaction().getFinal());
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
        CauseTracker causeTracker = world.getCauseTracker();
        CreativeInventoryEvent event = null;
        if (packetIn.getSlotId() == -1 && causeTracker.getCapturedSpawnedEntityItems().size() > 0) {
            Iterator<Entity> iterator = causeTracker.getCapturedSpawnedEntityItems().iterator();
            ImmutableList.Builder<EntitySnapshot> entitySnapshotBuilder = new ImmutableList.Builder<>();
            while (iterator.hasNext()) {
                Entity currentEntity = iterator.next();
                ((IMixinEntity) currentEntity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, player.getUniqueID());
                entitySnapshotBuilder.add(currentEntity.createSnapshot());
            }

            SpawnCause spawnCause = EntitySpawnCause.builder()
                    .entity((Entity) player)
                    .type(SpawnTypes.DROPPED_ITEM)
                    .build();
            event = SpongeEventFactory.createCreativeInventoryEventDrop(Cause.of(NamedCause.source(spawnCause)), cursorTransaction, causeTracker.getCapturedSpawnedEntityItems(),
                    entitySnapshotBuilder.build(), (org.spongepowered.api.item.inventory.Container) player.openContainer, (World) player.worldObj,
                    ((IMixinContainer) player.openContainer).getCapturedTransactions());
        } else {
            if (((IMixinContainer) player.openContainer).getCapturedTransactions().size() == 0 && packetIn.getSlotId() >= 0
                    && packetIn.getSlotId() < player.openContainer.inventorySlots.size()) {
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
                causeTracker.getCapturedSpawnedEntityItems().clear();
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

    public static void handleClickInteractInventoryEvent(Cause cause, EntityPlayerMP player, C0EPacketClickWindow packetIn) {
        IMixinWorld world = ((IMixinWorld) player.worldObj);
        CauseTracker causeTracker = world.getCauseTracker();
        ItemStackSnapshot newCursor = player.inventory.getItemStack() == null ? ItemStackSnapshot.NONE
                        : ((org.spongepowered.api.item.inventory.ItemStack) player.inventory.getItemStack()).createSnapshot();
        Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(StaticMixinHelper.lastCursor, newCursor);
        ClickInventoryEvent clickEvent = null;
        // Handle empty slot clicks
        if (((IMixinContainer) player.openContainer).getCapturedTransactions().size() == 0 && packetIn.getSlotId() >= 0
                && packetIn.getSlotId() < player.openContainer.inventorySlots.size()) {
            Slot slot = player.openContainer.getSlot(packetIn.getSlotId());
            if (slot != null && !slot.getHasStack()) {
                SlotTransaction slotTransaction =
                        new SlotTransaction(new SlotAdapter(slot), ItemStackSnapshot.NONE, ItemStackSnapshot.NONE);
                ((IMixinContainer) player.openContainer).getCapturedTransactions().add(slotTransaction);
            }
        }
        if (packetIn.getMode() == MODE_CLICK || packetIn.getMode() == MODE_PICKBLOCK) {
            if (packetIn.getUsedButton() == BUTTON_PRIMARY) {
                if (packetIn.getSlotId() == CLICK_OUTSIDE) {
                    Iterator<Entity> iterator = causeTracker.getCapturedSpawnedEntityItems().iterator();
                    ImmutableList.Builder<EntitySnapshot> entitySnapshotBuilder = new ImmutableList.Builder<>();
                    while (iterator.hasNext()) {
                        Entity currentEntity = iterator.next();
                        ((IMixinEntity) currentEntity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, player.getUniqueID());
                        entitySnapshotBuilder.add(currentEntity.createSnapshot());
                    }

                    SpawnCause spawnCause = EntitySpawnCause.builder()
                            .entity((Entity) player)
                            .type(SpawnTypes.DROPPED_ITEM)
                            .build();
                    clickEvent =
                            SpongeEventFactory.createClickInventoryEventDropFull(Cause.of(NamedCause.source(spawnCause)), cursorTransaction,
                                    causeTracker.getCapturedSpawnedEntityItems(), entitySnapshotBuilder.build(),
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
                    Iterator<Entity> iterator = causeTracker.getCapturedSpawnedEntityItems().iterator();
                    ImmutableList.Builder<EntitySnapshot> entitySnapshotBuilder = new ImmutableList.Builder<>();
                    while (iterator.hasNext()) {
                        Entity currentEntity = iterator.next();
                        ((IMixinEntity) currentEntity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, player.getUniqueID());
                        entitySnapshotBuilder.add(currentEntity.createSnapshot());
                    }

                    SpawnCause spawnCause = EntitySpawnCause.builder()
                            .entity((Entity) player)
                            .type(SpawnTypes.DROPPED_ITEM)
                            .build();
                    clickEvent =
                            SpongeEventFactory.createClickInventoryEventDropSingle(Cause.of(NamedCause.source(spawnCause)), cursorTransaction,
                                    causeTracker.getCapturedSpawnedEntityItems(), entitySnapshotBuilder.build(),
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

        // TODO - properly handle unknown flags (this will come with the CauseTracking refactor)
        if (clickEvent == null) {
            ((IMixinContainer) player.openContainer).getCapturedTransactions().clear();
            return;
        }

        SpongeImpl.postEvent(clickEvent);

        if (clickEvent.isCancelled()) {
            if (clickEvent instanceof ClickInventoryEvent.Drop) {
                causeTracker.getCapturedSpawnedEntityItems().clear();
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

        ((IMixinContainer) player.openContainer).getCapturedTransactions().clear();
    }

    private static void handleSlotRestore(EntityPlayerMP player, List<SlotTransaction> slotTransactions) {
        for (SlotTransaction slotTransaction : slotTransactions) {
            SlotAdapter slot = (SlotAdapter) slotTransaction.getSlot();
            int slotNumber = slot.slotNumber;
            ItemStack originalStack =
                    slotTransaction.getOriginal() == ItemStackSnapshot.NONE ? null : (net.minecraft.item.ItemStack) slotTransaction
                            .getOriginal().createStack();

            // TODO: fix below
            /*if (originalStack == null) {
                slot.clear();
            } else {
                slot.offer((org.spongepowered.api.item.inventory.ItemStack) originalStack);
            }*/

            Slot nmsSlot = player.openContainer.getSlot(slotNumber);
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

                Slot nmsSlot = player.openContainer.getSlot(slotNumber);
                if (nmsSlot != null) {
                    nmsSlot.putStack(customStack);
                }

                player.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(player.openContainer.windowId, slotNumber, customStack));
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static CollideEntityEvent callCollideEntityEvent(net.minecraft.world.World world, @Nullable net.minecraft.entity.Entity sourceEntity,
                                                            List<net.minecraft.entity.Entity> entities) {
        IMixinWorld spongeWorld = (IMixinWorld) world;
        CauseTracker causeTracker = spongeWorld.getCauseTracker();
        ImmutableList<Entity> originalEntities = ImmutableList.copyOf((List<Entity>) (List<?>) entities);
        CollideEntityEvent event = SpongeEventFactory.createCollideEntityEvent(causeTracker.getCurrentCause(), originalEntities, (List<Entity>) (List<?>) entities,
                (World) world);
        SpongeImpl.postEvent(event);
        return event;
    }

    @SuppressWarnings("rawtypes")
    public static NotifyNeighborBlockEvent callNotifyNeighborEvent(World world, BlockPos pos, EnumSet notifiedSides) {
        Map<Direction, BlockState> neighbors = new HashMap<>();
        for (Object obj : notifiedSides) {
            EnumFacing notifiedSide = (EnumFacing) obj;
            BlockPos offset = pos.offset(notifiedSide);
            Direction direction = DirectionFacingProvider.getInstance().getKey(notifiedSide).get();
            Location<World> location = new Location<>(world, VecHelper.toVector(offset));
            if (location.getBlockY() >= 0 && location.getBlockY() <= 255) {
                neighbors.put(direction, location.getBlock());
            }
        }

        ImmutableMap<Direction, BlockState> originalNeighbors = ImmutableMap.copyOf(neighbors);
        final CauseTracker causeTracker = ((IMixinWorld) world).getCauseTracker();
        Cause parentCause = causeTracker.getCurrentCause();
        Cause notifyCause = null;
        if (parentCause == null || !parentCause.first(BlockSnapshot.class).isPresent() || !(((SpongeBlockSnapshot)parentCause.first(BlockSnapshot.class).get()).getBlockPos().equals(pos))) {
            BlockSnapshot sourceBlockSnapshot = world.createSnapshot(pos.getX(), pos.getY(), pos.getZ());
            List<NamedCause> namedCauses = new ArrayList<>();
            namedCauses.add(NamedCause.source(sourceBlockSnapshot));
            if (parentCause != null) {
                namedCauses.add(NamedCause.of("ParentSource", parentCause.root()));
            }
            if (causeTracker.hasNotifier()) {
                namedCauses.add(NamedCause.notifier(causeTracker.getCurrentNotifier().get()));
            } else if (StaticMixinHelper.packetPlayer != null) {
                namedCauses.add(NamedCause.owner(StaticMixinHelper.packetPlayer));
            }
            notifyCause = Cause.of(namedCauses);
        }

        NotifyNeighborBlockEvent event = SpongeEventFactory.createNotifyNeighborBlockEvent(notifyCause != null ? notifyCause : causeTracker.getCurrentCause(), originalNeighbors, neighbors);
        StaticMixinHelper.processingInternalForgeEvent = true;
        SpongeImpl.postEvent(event);
        StaticMixinHelper.processingInternalForgeEvent = false;
        return event;
    }

    public static InteractBlockEvent.Secondary callInteractBlockEventSecondary(Cause cause, Optional<Vector3d> interactionPoint, BlockSnapshot targetBlock, Direction targetSide) {
        return callInteractBlockEventSecondary(cause, Tristate.UNDEFINED, Tristate.UNDEFINED, Tristate.UNDEFINED, Tristate.UNDEFINED, interactionPoint, targetBlock, targetSide);
    }

    public static InteractBlockEvent.Secondary callInteractBlockEventSecondary(Cause cause, Tristate originalUseBlockResult, Tristate useBlockResult, Tristate originalUseItemResult, Tristate useItemResult, Optional<Vector3d> interactionPoint, BlockSnapshot targetBlock, Direction targetSide) {
        InteractBlockEvent.Secondary event = SpongeEventFactory.createInteractBlockEventSecondary(cause, originalUseBlockResult, useBlockResult, originalUseItemResult, useItemResult, interactionPoint, targetBlock, targetSide);
        SpongeImpl.postEvent(event);
        return event;
    }

    public static DestructEntityEvent.Death callDestructEntityEventDeath(EntityLivingBase entity, DamageSource source) {
        final MessageFormatter formatter = new MessageFormatter();
        MessageChannel originalChannel;
        MessageChannel channel;
        Text originalMessage;
        Optional<User> sourceCreator = Optional.empty();
        boolean messageCancelled = false;

        if (entity instanceof EntityPlayerMP) {
            Player player = (Player) entity;
            originalChannel = player.getMessageChannel();
            channel = player.getMessageChannel();
        } else {
            originalChannel = MessageChannel.TO_NONE;
            channel = MessageChannel.TO_NONE;
        }
        if (source instanceof EntityDamageSource) {
            EntityDamageSource damageSource = (EntityDamageSource) source;
            IMixinEntity spongeEntity = (IMixinEntity) damageSource.getSourceOfDamage();
            sourceCreator = spongeEntity.getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR);
        }

        originalMessage = SpongeTexts.toText(entity.getCombatTracker().getDeathMessage());
        formatter.getBody().add(new DefaultBodyApplier(originalMessage));
        List<NamedCause> causes = new ArrayList<>();
        causes.add(NamedCause.of("Attacker", source));
        if (sourceCreator.isPresent()) {
            causes.add(NamedCause.owner(sourceCreator.get()));
        }

        Cause cause = Cause.of(causes);
        DestructEntityEvent.Death event = SpongeEventFactory.createDestructEntityEventDeath(cause, originalChannel, Optional.of(channel), formatter, (Living) entity, messageCancelled);
        SpongeImpl.postEvent(event);
        Text message = event.getMessage();
        if (!event.isMessageCancelled() && !message.isEmpty()) {
            event.getChannel().ifPresent(eventChannel -> eventChannel.send(entity, event.getMessage()));
        }
        return event;
    }

    @SuppressWarnings("unchecked")
    public static DropItemEvent.Destruct callDropItemEventDestruct(net.minecraft.entity.Entity entity, DamageSource source, List<EntityItem> itemDrops) {
        Optional<User> sourceCreator = Optional.empty();
        
        if (source instanceof EntityDamageSource) {
            EntityDamageSource damageSource = (EntityDamageSource) source;
            IMixinEntity spongeEntity = (IMixinEntity) damageSource.getSourceOfDamage();
            sourceCreator = spongeEntity.getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR);
        }

        List<NamedCause> causes = new ArrayList<>();
        causes.add(NamedCause.source(EntitySpawnCause.builder()
                .entity((Entity) entity)
                .type(SpawnTypes.DROPPED_ITEM)
                .build()));
        causes.add(NamedCause.of("Attacker", source));
        causes.add(NamedCause.of("Victim", entity));
        if (sourceCreator.isPresent()) {
            causes.add(NamedCause.owner(sourceCreator.get()));
        }
        Iterator<EntityItem> iter = itemDrops.iterator();
        ImmutableList.Builder<EntitySnapshot> entitySnapshotBuilder = new ImmutableList.Builder<>();
        while (iter.hasNext()) {
            EntityItem currentEntityItem = iter.next();
            entitySnapshotBuilder.add(((org.spongepowered.api.entity.Entity) currentEntityItem).createSnapshot());
        }

        List<EntitySnapshot> entitySnapshots = entitySnapshotBuilder.build();
        DropItemEvent.Destruct event = SpongeEventFactory.createDropItemEventDestruct(Cause.of(causes), (List<org.spongepowered.api.entity.Entity>)(List<?>) itemDrops, entitySnapshots, (World) entity.worldObj);
        SpongeImpl.postEvent(event);
        return event;
    }

    @SuppressWarnings("unchecked")
    public static DropItemEvent.Dispense callDropItemEventDispenseSingle(net.minecraft.entity.Entity entity, EntityItem droppedItem) {
        List<EntityItem> droppedItems = new ArrayList<>();
        droppedItems.add(droppedItem);
        ImmutableList.Builder<EntitySnapshot> entitySnapshotBuilder = new ImmutableList.Builder<>();
        List<EntitySnapshot> entitySnapshots = entitySnapshotBuilder.add(((org.spongepowered.api.entity.Entity) droppedItem).createSnapshot()).build();

        SpawnCause spawnCause = EntitySpawnCause.builder()
                .entity((Entity) entity)
                .type(SpawnTypes.DROPPED_ITEM)
                .build();
        DropItemEvent.Dispense event = SpongeEventFactory.createDropItemEventDispense(Cause.of(NamedCause.source(spawnCause)), (List<org.spongepowered.api.entity.Entity>)(List<?>) droppedItems, entitySnapshots, (World) entity.worldObj);
        SpongeImpl.postEvent(event);
        return event;
    }

    public static boolean handleCollideBlockEvent(Block block, net.minecraft.world.World world, BlockPos pos, IBlockState state, net.minecraft.entity.Entity entity, Direction direction) {
        Cause cause = Cause.of(NamedCause.of(NamedCause.PHYSICAL, entity));
        IMixinWorld spongeWorld = (IMixinWorld) world;
        final CauseTracker causeTracker = spongeWorld.getCauseTracker();
        if (!(entity instanceof EntityPlayer)) {
            if (causeTracker.hasNotifier()) {
                cause = cause.with(NamedCause.source(causeTracker.getCurrentNotifier()));
            }
        }

        // TODO: Add target side support
        CollideBlockEvent event = SpongeEventFactory.createCollideBlockEvent(cause, (BlockState) state, new Location<World>((World) world, VecHelper.toVector(pos)), direction);
        boolean cancelled = SpongeImpl.postEvent(event);
        if (!cancelled) {
            IMixinEntity spongeEntity = (IMixinEntity) entity;
            if (!pos.equals(spongeEntity.getLastCollidedBlockPos())) {
                if (causeTracker.hasNotifier()) {
                    IMixinChunk spongeChunk = (IMixinChunk) world.getChunkFromBlockCoords(pos);
                    spongeChunk.addTrackedBlockPosition(block, pos, causeTracker.getCurrentNotifier().get(), PlayerTracker.Type.NOTIFIER);
                }
            }
        }

        return cancelled;
    }

    public static boolean handleCollideImpactEvent(net.minecraft.entity.Entity projectile, @Nullable ProjectileSource projectileSource,
            MovingObjectPosition movingObjectPosition) {
        MovingObjectType movingObjectType = movingObjectPosition.typeOfHit;
        Cause cause = Cause.source(projectile).named("ProjectileSource", projectileSource == null ? ProjectileSource.UNKNOWN : projectileSource).build();
        IMixinWorld spongeWorld = (IMixinWorld) projectile.worldObj;
        final CauseTracker causeTracker = spongeWorld.getCauseTracker();
        if (causeTracker.hasNotifier()) {
            cause = cause.with(NamedCause.owner(causeTracker.getCurrentNotifier()));
        }

        Location<World> impactPoint = new Location<>((World) projectile.worldObj, VecHelper.toVector(movingObjectPosition.hitVec));
        boolean cancelled = false;

        if (movingObjectType == MovingObjectType.BLOCK) {
            BlockSnapshot targetBlock = ((World) projectile.worldObj).createSnapshot(VecHelper.toVector(movingObjectPosition.getBlockPos()));
            Direction side = Direction.NONE;
            if (movingObjectPosition.sideHit != null) {
                side = DirectionFacingProvider.getInstance().getKey(movingObjectPosition.sideHit).get();
            }

            CollideBlockEvent.Impact event = SpongeEventFactory.createCollideBlockEventImpact(cause, impactPoint, targetBlock.getState(),
                    targetBlock.getLocation().get(), side);
            cancelled = SpongeImpl.postEvent(event);
            // Track impact block if event is not cancelled
            if (!cancelled && causeTracker.hasNotifier()) {
                BlockPos targetPos = VecHelper.toBlockPos(impactPoint.getBlockPosition());
                IMixinChunk spongeChunk = (IMixinChunk) projectile.worldObj.getChunkFromBlockCoords(targetPos);
                spongeChunk.addTrackedBlockPosition((Block) targetBlock.getState().getType(), targetPos, causeTracker.getCurrentNotifier().get(), PlayerTracker.Type.NOTIFIER);
            }
        } else if (movingObjectPosition.entityHit != null) { // entity
            ImmutableList.Builder<Entity> entityBuilder = new ImmutableList.Builder<>();
            ArrayList<Entity> entityList = new ArrayList<>();
            entityList.add((Entity) movingObjectPosition.entityHit);
            CollideEntityEvent.Impact event = SpongeEventFactory.createCollideEntityEventImpact(cause,
                    entityBuilder.add((Entity) movingObjectPosition.entityHit).build(), entityList, impactPoint, (World) projectile.worldObj);
            cancelled = SpongeImpl.postEvent(event);
        }

        return cancelled;
    }

    public static DisplaceEntityEvent.Portal handleDisplaceEntityPortalEvent(net.minecraft.entity.Entity entityIn, int targetDimensionId, Teleporter teleporter) {
        SpongeImplHooks.registerPortalAgentType(teleporter);
        MinecraftServer mcServer = MinecraftServer.getServer();
        IMixinServerConfigurationManager scm = (IMixinServerConfigurationManager) mcServer.getConfigurationManager();
        Entity spongeEntity = (Entity) entityIn;
        Transform<World> fromTransform = spongeEntity.getTransform();
        int fromDimensionId = entityIn.dimension;
        WorldServer fromWorld = mcServer.worldServerForDimension(fromDimensionId);
        WorldServer toWorld = mcServer.worldServerForDimension(targetDimensionId);
        if (teleporter == null) {
            teleporter = toWorld.getDefaultTeleporter();
        }
        SpongeConfig<?> activeConfig = ((IMixinWorld) fromWorld).getActiveConfig();
        String worldName = "";
        String teleporterClassName = teleporter.getClass().getName();

        // check for new destination in config
        if (teleporterClassName.equals("net.minecraft.world.Teleporter")) {
            if (toWorld.provider instanceof WorldProviderHell) {
                worldName = activeConfig.getConfig().getWorld().getPortalAgents().get("minecraft:default_nether");
            } else if (toWorld.provider instanceof WorldProviderEnd) {
                worldName = activeConfig.getConfig().getWorld().getPortalAgents().get("minecraft:default_the_end");
            }
        } else { // custom
            worldName = activeConfig.getConfig().getWorld().getPortalAgents().get("minecraft:" + teleporter.getClass().getSimpleName());
        }

        if (worldName != null && !worldName.equals("")) {
            for (WorldProperties worldProperties : Sponge.getServer().getAllWorldProperties()) {
                if (worldProperties.getWorldName().equalsIgnoreCase(worldName)) {
                    Optional<World> spongeWorld = Sponge.getServer().loadWorld(worldProperties);
                    if (spongeWorld.isPresent()) {
                        toWorld = (WorldServer) spongeWorld.get();
                        teleporter = toWorld.getDefaultTeleporter();
                        ((IMixinTeleporter) teleporter).setPortalType(targetDimensionId);
                    }
                }
            }
        }

        Transform<World> preTeleportTransform = scm.getTeleporterTransform(entityIn, fromWorld, toWorld, teleporter);
        if (entityIn instanceof EntityPlayerMP) {
            // disable packets from being sent to clients to avoid syncing issues, this is re-enabled before the event
            ((IMixinNetHandlerPlayServer) ((EntityPlayerMP) entityIn).playerNetServerHandler).setAllowClientLocationUpdate(false);
        }
        spongeEntity.setTransform(preTeleportTransform);
        // need to use placeInExistingPortal to support mods
        teleporter.placeInExistingPortal(entityIn, entityIn.rotationYaw);
        Transform<World> portalExitTransform = preTeleportTransform.setLocation(spongeEntity.getLocation()).setExtent((World) toWorld);
        Cause teleportCause = Cause.of(NamedCause.source(new SpongePortalTeleportCause((PortalAgent) teleporter)));
        Cause currentCause = ((IMixinWorld) fromWorld).getCauseTracker().getCurrentCause();
        if (currentCause != null) {
            teleportCause = teleportCause.merge(currentCause);
        }
        DisplaceEntityEvent.Portal event = SpongeEventFactory.createDisplaceEntityEventPortal(teleportCause, fromTransform, portalExitTransform, (PortalAgent) teleporter, spongeEntity, true, true);
        SpongeImpl.postEvent(event);
        if (entityIn instanceof EntityPlayerMP) {
            ((IMixinNetHandlerPlayServer) ((EntityPlayerMP) entityIn).playerNetServerHandler).setAllowClientLocationUpdate(true);
        }
        if (event.isCancelled()) {
            spongeEntity.setTransform(fromTransform);
            return event;
        }

        if (!portalExitTransform.equals(event.getToTransform())) {
            // if plugin set to same world, just set the transform
            if (fromWorld == event.getToTransform().getExtent()) {
                spongeEntity.setTransform(event.getToTransform());
                // force cancel so we know to skip remaining logic
                event.setCancelled(true);
                return event;
            }
        }

        spongeEntity.setTransform(preTeleportTransform);
        return event;
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

    public static void checkSpawnEvent(Entity entity, Cause cause) {
        checkArgument(cause.root() instanceof SpawnCause, "The cause does not have a SpawnCause! It has instead: {}", cause.root().toString());
        checkArgument(cause.containsNamed(NamedCause.SOURCE), "The cause does not have a \"Source\" named object!");
        checkArgument(cause.get(NamedCause.SOURCE, SpawnCause.class).isPresent(), "The SpawnCause is not the \"Source\" of the cause!");

    }

    public static Cause getEntitySpawnCause(net.minecraft.entity.Entity nmsEntity) {
        World world = (World) nmsEntity.worldObj;
        Entity entity = (Entity) nmsEntity;
        List<NamedCause> list = new ArrayList<>();
        final CauseTracker causeTracker = ((IMixinWorld) world).getCauseTracker();
        if (nmsEntity.worldObj.isRemote || nmsEntity instanceof EntityPlayer || causeTracker.isWorldSpawnerRunning()) {
            return Cause.of(NamedCause.source(SpawnCause.builder().type(InternalSpawnTypes.CUSTOM).build()));
        }
        if (StaticMixinHelper.runningGenerator != null) {
            PopulatorType type = StaticMixinHelper.runningGenerator;
            if (InternalPopulatorTypes.ANIMAL.equals(type)) {
                list.add(NamedCause.source(SpawnCause.builder().type(InternalSpawnTypes.WORLD_SPAWNER).build()));
                list.add(NamedCause.of("AnimalSpawner", StaticMixinHelper.runningGenerator));
            } else {
                list.add(NamedCause.source(SpawnCause.builder().type(InternalSpawnTypes.STRUCTURE).build()));
                list.add(NamedCause.of("Structure", StaticMixinHelper.runningGenerator));
            }
        } else {
            final Optional<Entity> currentTickEntity = causeTracker.getCurrentTickEntity();
            final Optional<BlockSnapshot> currentTickBlock = causeTracker.getCurrentTickBlock();
            final Optional<TileEntity> currentTickTileEntity = causeTracker.getCurrentTickTileEntity();

            if (StaticMixinHelper.dispenserDispensing) {
                if (currentTickBlock.isPresent()) {
                    BlockSpawnCause blockSpawnCause = BlockSpawnCause.builder()
                            .block(currentTickBlock.get())
                            .type(InternalSpawnTypes.DISPENSE)
                            .build();
                    list.add(NamedCause.source(blockSpawnCause));
                } else if (currentTickTileEntity.isPresent()) {
                    BlockSpawnCause blockSpawnCause = BlockSpawnCause.builder()
                            .block(currentTickTileEntity.get().getLocation().createSnapshot())
                            .type(InternalSpawnTypes.DISPENSE)
                            .build();
                    list.add(NamedCause.source(blockSpawnCause));
                } else if (currentTickEntity.isPresent()) {
                    if  (currentTickEntity.get() == entity) {
                        SpawnCause cause = SpawnCause.builder()
                                .type(InternalSpawnTypes.DISPENSE)
                                .build();
                        list.add(NamedCause.source(cause));
                    } else {
                        EntitySpawnCause cause = EntitySpawnCause.builder()
                                .entity(currentTickEntity.get())
                                .type(InternalSpawnTypes.DISPENSE)
                                .build();
                        list.add(NamedCause.source(cause));
                    }
                }
            } else if (nmsEntity instanceof EntityItem) {
                if (causeTracker.isCapturingTerrainGen()) {
                    // Just default to the structures placing it.
                    list.add(NamedCause.source(SpawnCause.builder().type(InternalSpawnTypes.STRUCTURE).build()));
                    return Cause.of(list);
                }
                if (currentTickBlock.isPresent()) {
                    BlockSpawnCause blockSpawnCause = BlockSpawnCause.builder()
                            .block(currentTickBlock.get())
                            .type(InternalSpawnTypes.BLOCK_SPAWNING)
                            .build();
                    list.add(NamedCause.source(blockSpawnCause));
                } else if (currentTickTileEntity.isPresent()) {
                    BlockSpawnCause blockSpawnCause = BlockSpawnCause.builder()
                            .block(currentTickTileEntity.get().getLocation().createSnapshot())
                            .type(InternalSpawnTypes.BLOCK_SPAWNING)
                            .build();
                    list.add(NamedCause.source(blockSpawnCause));
                } else if (currentTickEntity.isPresent()) {
                    if  (currentTickEntity.get() == entity) {
                        SpawnCause cause = SpawnCause.builder()
                                .type(InternalSpawnTypes.CUSTOM)
                                .build();
                        list.add(NamedCause.source(cause));
                    } else {
                        EntitySpawnCause cause = EntitySpawnCause.builder()
                                .entity(currentTickEntity.get())
                                .type(InternalSpawnTypes.PASSIVE)
                                .build();
                        list.add(NamedCause.source(cause));
                    }
                } else if (StaticMixinHelper.packetPlayer != null) {
                    EntitySpawnCause cause = EntitySpawnCause.builder()
                            .entity((Entity) StaticMixinHelper.packetPlayer)
                            .type(InternalSpawnTypes.DISPENSE)
                            .build();
                    list.add(NamedCause.source(cause));
                }
            } else if (nmsEntity instanceof EntityXPOrb) {
                // This is almost always ALWAYS guaranteed to be experience, otherwise, someone
                // can open a ticket to correct us with proof otherwise.
                if (currentTickEntity.isPresent()) {
                    Entity currentEntity = currentTickEntity.get();
                    EntitySpawnCause spawnCause = EntitySpawnCause.builder()
                            .entity(currentEntity)
                            .type(InternalSpawnTypes.EXPERIENCE)
                            .build();
                    list.add(NamedCause.source(spawnCause));
                    if (isEntityDead(currentEntity)) {
                        if (currentEntity instanceof EntityLivingBase) {
                            CombatEntry entry = ((EntityLivingBase) currentEntity).getCombatTracker().func_94544_f();
                            if (entry != null) {
                                if (entry.damageSrc != null) {
                                    list.add(NamedCause.of("LastDamageSource", entry.damageSrc));
                                }
                            }
                        }
                    }
                } else if (currentTickBlock.isPresent()) {
                    BlockSpawnCause spawnCause = BlockSpawnCause.builder()
                            .block(currentTickBlock.get())
                            .type(InternalSpawnTypes.EXPERIENCE)
                            .build();
                    list.add(NamedCause.source(spawnCause));
                } else if (currentTickTileEntity.isPresent()) {
                    SpawnCause spawnCause = BlockSpawnCause.builder()
                            .block(currentTickTileEntity.get().getLocation().createSnapshot())
                            .type(InternalSpawnTypes.EXPERIENCE)
                            .build();
                    list.add(NamedCause.source(spawnCause));
                } else {
                    SpawnCause spawnCause = SpawnCause.builder()
                            .type(InternalSpawnTypes.EXPERIENCE)
                            .build();
                    list.add(NamedCause.source(spawnCause));
                }
            } else if (StaticMixinHelper.prePacketProcessItem != null) {
                SpawnCause cause;
                if (entity instanceof Projectile || entity instanceof EntityThrowable) {
                    cause = EntitySpawnCause.builder()
                            .entity(((Entity) StaticMixinHelper.packetPlayer))
                            .type(InternalSpawnTypes.PROJECTILE)
                            .build();
                } else if (StaticMixinHelper.prePacketProcessItem.getItem() == Items.spawn_egg) {
                    cause = EntitySpawnCause.builder()
                            .entity((Entity) StaticMixinHelper.packetPlayer)
                            .type(InternalSpawnTypes.SPAWN_EGG)
                            .build();
                } else {
                    cause = EntitySpawnCause.builder()
                            .entity((Entity) StaticMixinHelper.packetPlayer)
                            .type(InternalSpawnTypes.PLACEMENT)
                            .build();
                }
                list.add(NamedCause.source(cause));
                list.add(NamedCause.of("UsedItem", ItemStackUtil.fromNative(StaticMixinHelper.prePacketProcessItem).createSnapshot()));
                list.add(NamedCause.owner(StaticMixinHelper.packetPlayer));
            } else if (currentTickBlock.isPresent()) { // We've exhausted our possibilities, now we just associate blindly
                BlockSpawnCause cause = BlockSpawnCause.builder().block(currentTickBlock.get())
                        .type(InternalSpawnTypes.BLOCK_SPAWNING)
                        .build();
                list.add(NamedCause.source(cause));
            } else if (currentTickEntity.isPresent()) {
                Entity sourceEntity = currentTickEntity.get();
                if (sourceEntity instanceof Ageable && sourceEntity.getClass() == entity.getClass()) { // We should assume breeding
                    EntitySpawnCause spawnCause = EntitySpawnCause.builder().entity(sourceEntity).type(InternalSpawnTypes.BREEDING).build();
                    list.add(NamedCause.source(spawnCause));
                    if (sourceEntity instanceof EntityAnimal) {
                        if (((EntityAnimal) sourceEntity).getPlayerInLove() != null) {
                            list.add(NamedCause.of("Player", ((EntityAnimal) sourceEntity).getPlayerInLove()));
                        }
                    }
                } else if (nmsEntity instanceof Projectile) {
                    EntitySpawnCause cause = EntitySpawnCause.builder().entity(sourceEntity).type(InternalSpawnTypes.PROJECTILE).build();
                    list.add(NamedCause.source(cause));
                } else {
                    EntitySpawnCause cause = EntitySpawnCause.builder().entity(sourceEntity).type(InternalSpawnTypes.CUSTOM).build();
                    list.add(NamedCause.source(cause));
                }
            } else {
                list.add(NamedCause.source(SpawnCause.builder().type(InternalSpawnTypes.CUSTOM).build()));
            }
        }
        if (list.isEmpty()) {
            list.add(NamedCause.source(SpawnCause.builder().type(InternalSpawnTypes.CUSTOM).build()));
        }

        boolean foundUser = false;
        for (NamedCause namedCause : list) {
            if (namedCause.getCauseObject() instanceof User) {
                foundUser = true;
                break;
            }
        }

        if (!foundUser) {
            User currentUser = StaticMixinHelper.packetPlayer != null ? (User) StaticMixinHelper.packetPlayer : causeTracker.getCurrentNotifier().orElse(null);
            if (currentUser != null) {
                list.add(NamedCause.owner(currentUser));
            }
        }

        return Cause.of(list);
    }

    public static Cause handleExtraCustomCauses(net.minecraft.entity.Entity spawning, Cause current) {
        EntityLivingBase specialCause = null;
        String causeName = "";
        // Special case for throwables
        if (spawning instanceof EntityThrowable) {
            EntityThrowable throwable = (EntityThrowable) spawning;
            specialCause = throwable.getThrower();

            if (specialCause != null) {
                causeName = NamedCause.THROWER;
                if (specialCause instanceof Player) {
                    Player player = (Player) specialCause;
                    ((IMixinEntity) spawning).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, player.getUniqueId());
                }
            }
        }
        // Special case for TNT
        else if (spawning instanceof EntityTNTPrimed) {
            EntityTNTPrimed tntEntity = (EntityTNTPrimed) spawning;
            specialCause = tntEntity.getTntPlacedBy();
            causeName = NamedCause.IGNITER;

            if (specialCause instanceof Player) {
                Player player = (Player) specialCause;
                ((IMixinEntity) spawning).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, player.getUniqueId());
            }
        }
        // Special case for Tameables
        else if (spawning instanceof EntityTameable) {
            EntityTameable tameable = (EntityTameable) spawning;
            if (tameable.getOwner() != null) {
                specialCause = tameable.getOwner();
                causeName = NamedCause.OWNER;
            }
        }

        if (specialCause != null && !current.containsNamed(causeName)) {
            return SpongeCommonEventFactory.withExtra(current, causeName, specialCause);
        } else {
            return current;
        }

    }

    public static Cause withExtra(Cause cause, String name, Object extra) {
        return cause.with(NamedCause.of(name, extra));
    }

    public static Event throwItemDropEvent(Cause cause, List<Entity> entities, ImmutableList<EntitySnapshot> snapshots, World world) {
        return SpongeEventFactory.createDropItemEventCustom(cause, entities, snapshots, world);
    }

    public static Event throwEntitySpawnCustom(Cause cause, List<Entity> entities, ImmutableList<EntitySnapshot> snapshots, World world) {
        return SpongeEventFactory.createSpawnEntityEventCustom(cause, entities, snapshots, world);
    }

    private static boolean isEntityDead(Entity entity) {
        return isEntityDead((net.minecraft.entity.Entity) entity);
    }

    private static boolean isEntityDead(net.minecraft.entity.Entity entity) {
        if (entity instanceof EntityLivingBase) {
            EntityLivingBase base = (EntityLivingBase) entity;
            return base.getHealth() <= 0 || base.deathTime > 0 || base.dead;
        } else {
            return entity.isDead;
        }
    }

    public static Cause handleEntityCreatedByPlayerCause(Cause cause) {
        final Object root = cause.root();
        Cause newCause;
        if (!(root instanceof SpawnCause)) {
            SpawnCause spawnCause;
            if (StaticMixinHelper.packetPlayer == null) {
                spawnCause = SpawnCause.builder().type(InternalSpawnTypes.PLACEMENT).build();
            } else {
                spawnCause = EntitySpawnCause.builder()
                        .entity((Entity) StaticMixinHelper.packetPlayer)
                        .type(InternalSpawnTypes.PLACEMENT)
                        .build();
            }
            List<NamedCause> causes = new ArrayList<>();
            causes.add(NamedCause.source(spawnCause));
            newCause = moveNewCausesUp(cause, causes);
        } else {
            newCause = cause;
        }
        return newCause;
    }

    public static Cause handleDropCause(Cause cause) {
        final Object root = cause.root();
        Cause newCause;
        if (!(root instanceof SpawnCause)) {
            SpawnCause spawnCause;
            if (StaticMixinHelper.packetPlayer == null) {
                spawnCause = SpawnCause.builder().type(InternalSpawnTypes.DROPPED_ITEM).build();
            } else {
                spawnCause = EntitySpawnCause.builder()
                        .entity((Entity) StaticMixinHelper.packetPlayer)
                        .type(InternalSpawnTypes.DROPPED_ITEM)
                        .build();
            }
            List<NamedCause> causes = new ArrayList<>();
            causes.add(NamedCause.source(spawnCause));
            newCause = moveNewCausesUp(cause, causes);
        } else {
            newCause = cause;
        }
        return newCause;
    }

    private static Cause moveNewCausesUp(Cause currentCause, List<NamedCause> newCauses) {
        int index = 0;
        for (Map.Entry<String, Object> entry : currentCause.getNamedCauses().entrySet()) {
            String entryName = entry.getKey();
            if ("Source".equals(entryName)) {
                newCauses.add(NamedCause.of("AdditionalSource", entry.getValue()));
            } else if ("AdditionalSource".equals(entryName)) {
                newCauses.add(NamedCause.of("PreviousSource", entry.getValue()));
            } else if (entryName.startsWith("PreviousSource")) {
                newCauses.add(NamedCause.of(entryName + index++, entry.getValue()));
            } else {
                newCauses.add(NamedCause.of(entryName, entry.getValue()));
            }
        }
        return Cause.of(newCauses);
    }
}
