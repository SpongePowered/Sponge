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
package org.spongepowered.common.event.tracking.phase.packet;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketResourcePackStatus;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.SPacketHeldItemChange;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.AffectEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.ResourcePackStatusEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.ItemDropData;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.interfaces.IMixinContainer;
import org.spongepowered.common.interfaces.IMixinPacketResourcePackSend;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayerMP;
import org.spongepowered.common.interfaces.network.IMixinNetHandlerPlayServer;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.item.inventory.util.ContainerUtil;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.registry.type.ItemTypeRegistryModule;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;
import org.spongepowered.common.util.VecHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@FunctionalInterface
public interface PacketFunction {

    PacketFunction IGNORED = (packet, state, player, context) -> {
    };

    PacketFunction STOP_SLEEPING = (packet, state, player, context) -> {
        if (state == PacketPhase.General.STOP_SLEEPING) {
            final List<BlockSnapshot> capturedBlocks = context.getCapturedBlocks();
            TrackingUtil.processBlockCaptures(capturedBlocks, PacketPhase.General.STOP_SLEEPING, context);
        }
    };

    PacketFunction USE_ENTITY = (packet, state, player, context) -> {
        final CPacketUseEntity useEntityPacket = (CPacketUseEntity) packet;
        final net.minecraft.entity.Entity entity = useEntityPacket.getEntityFromWorld(player.world);
        if (entity == null) {
            // Something happened?
            return;
        }
        // final Optional<ItemStack> itemStack =
        // context.firstNamed(InternalNamedCauses.Packet.ITEM_USED,
        // ItemStack.class);
        final World spongeWorld = EntityUtil.getSpongeWorld(player);
        EntityUtil.toMixin(entity).setNotifier(player.getUniqueID());

        if (state == PacketPhase.General.ATTACK_ENTITY) {
            context.getCapturedItemsSupplier()
                    .ifPresentAndNotEmpty(items -> {
                        // For destruction, this should be empty, however, some
                        // times,
                        // it may not be?
                        final PrettyPrinter printer = new PrettyPrinter(60);
                        printer.add("Processing Attack Entity").centre().hr();
                        printer.add("There are some captured items after the entity was destructed!");
                        printer.addWrapped(60, "%s : %s", "Items captured", items);
                        printer.add("Stacktrace:");
                        printer.add(new Exception("Stack trace"));
                        printer.trace(System.err, SpongeImpl.getLogger(), Level.TRACE);
                    });
            context.getCapturedBlockSupplier()
                    .ifPresentAndNotEmpty(blocks -> TrackingUtil.processBlockCaptures(blocks, state, context));
            context.getCapturedEntityDropSupplier().ifPresentAndNotEmpty(map -> {
                for (Map.Entry<UUID, Collection<ItemDropData>> entry : map.asMap().entrySet()) {
                    final UUID key = entry.getKey();
                    final Optional<Entity> affectedEntity = spongeWorld.getEntity(key);
                    if (!affectedEntity.isPresent()) {
                        continue;
                    }
                    final Collection<ItemDropData> itemStacks = entry.getValue();
                    if (itemStacks.isEmpty()) {
                        return;
                    }
                    final List<ItemDropData> items = new ArrayList<>();
                    items.addAll(itemStacks);

                    if (!items.isEmpty()) {
                        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                            final List<Entity> itemEntities = items.stream()
                                    .map(data -> data.create(((WorldServer) player.world)))
                                    .map(EntityUtil::fromNative)
                                    .collect(Collectors.toList());
                            Sponge.getCauseStackManager().pushCause(player);
                            Sponge.getCauseStackManager().pushCause(affectedEntity.get());
                            Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, InternalSpawnTypes.DROPPED_ITEM);

                            final DropItemEvent.Destruct destruct =
                                    SpongeEventFactory.createDropItemEventDestruct(Sponge.getCauseStackManager().getCurrentCause(), itemEntities);
                            SpongeImpl.postEvent(destruct);
                            if (!destruct.isCancelled()) {
                                processSpawnedEntities(player, destruct);
                            }
                        }
                    }
                }
            });
            context.getCapturedEntityItemDropSupplier().ifPresentAndNotEmpty(map -> {
                try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                    Sponge.getCauseStackManager().pushCause(player);
                    Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, InternalSpawnTypes.DROPPED_ITEM);
                    for (Map.Entry<UUID, Collection<EntityItem>> entry : map.asMap().entrySet()) {
                        final UUID key = entry.getKey();
                        final Optional<Entity> attackedEntities = spongeWorld.getEntity(key);
                        if (!attackedEntities.isPresent()) {
                            continue;
                        }
                        final List<Entity> items = entry.getValue().stream().map(EntityUtil::fromNative).collect(Collectors.toList());

                        final DropItemEvent.Destruct destruct =
                                SpongeEventFactory.createDropItemEventDestruct(Sponge.getCauseStackManager().getCurrentCause(), items);
                        SpongeImpl.postEvent(destruct);
                        if (!destruct.isCancelled()) {
                            processSpawnedEntities(player, destruct);
                        }
                    }
                }
            });

        } else if (state == PacketPhase.General.INTERACT_ENTITY) {
            context.getCapturedBlockSupplier()
                    .ifPresentAndNotEmpty(blocks -> {
                        final PrettyPrinter printer = new PrettyPrinter(80);
                        printer.add("Processing Interact Entity").centre().hr();
                        printer.add("The blocks captured are:");
                        for (BlockSnapshot blockSnapshot : blocks) {
                            printer.add("  Block: %s", blockSnapshot);
                        }
                        printer.trace(System.err);
                    });
            context.getCapturedEntitySupplier()
                    .ifPresentAndNotEmpty(entities -> {
                        final PrettyPrinter printer = new PrettyPrinter(80);
                        printer.add("Processing Interact Entity").centre().hr();
                        printer.add("The entities captured are:");
                        for (Entity capturedEntity : entities) {
                            printer.add("  Entity: %s", capturedEntity);
                        }
                        printer.trace(System.err);
                    });
            try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                Sponge.getCauseStackManager().pushCause(player);
                Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, InternalSpawnTypes.PLACEMENT);
                context.getCapturedItemsSupplier().ifPresentAndNotEmpty(entities -> {
                    final List<Entity> items = entities.stream().map(EntityUtil::fromNative).collect(Collectors.toList());
                    SpawnEntityEvent event =
                            SpongeEventFactory.createSpawnEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), items);
                    SpongeImpl.postEvent(event);
                    if (!event.isCancelled()) {
                        processSpawnedEntities(player, event);

                    }
                });
            }
            context.getCapturedEntityDropSupplier()
                    .ifPresentAndNotEmpty(map -> {
                        final PrettyPrinter printer = new PrettyPrinter(80);
                        printer.add("Processing Interact Entity").centre().hr();
                        printer.add("The item stacks captured are: ");

                        for (Map.Entry<UUID, Collection<ItemDropData>> entry : map.asMap().entrySet()) {
                            printer.add("  - Entity with UUID: %s", entry.getKey());
                            for (ItemDropData stack : entry.getValue()) {
                                printer.add("    - %s", stack);
                            }
                        }
                        printer.trace(System.err);
                    });

        } else if (state == PacketPhase.General.INTERACT_AT_ENTITY) {
            try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                Sponge.getCauseStackManager().pushCause(player);
                Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, InternalSpawnTypes.PLACEMENT);
                context.getCapturedEntitySupplier().ifPresentAndNotEmpty(entities -> {

                    SpawnEntityEvent event = SpongeEventFactory.createSpawnEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), entities);
                    SpongeImpl.postEvent(event);
                    if (!event.isCancelled()) {
                        processSpawnedEntities(player, event);
                    }
                });
                context.getCapturedItemsSupplier().ifPresentAndNotEmpty(entities -> {
                    final List<Entity> items = entities.stream().map(EntityUtil::fromNative).collect(Collectors.toList());
                    SpawnEntityEvent event =
                            SpongeEventFactory.createSpawnEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), items);
                    SpongeImpl.postEvent(event);
                    if (!event.isCancelled()) {
                        processSpawnedEntities(player, event);
                    }
                });
                context.getCapturedEntityDropSupplier().ifPresentAndNotEmpty(map -> {
                    final PrettyPrinter printer = new PrettyPrinter(80);
                    printer.add("Processing Interact At Entity").centre().hr();
                    printer.add("The item stacks captured are: ");

                    for (Map.Entry<UUID, Collection<ItemDropData>> entry : map.asMap().entrySet()) {
                        printer.add("  - Entity with UUID: %s", entry.getKey());
                        for (ItemDropData stack : entry.getValue()) {
                            printer.add("    - %s", stack);
                        }
                    }
                    printer.trace(System.err);
                });
                context.getCapturedEntityItemDropSupplier().ifPresentAndNotEmpty(map -> {
                    for (Map.Entry<UUID, Collection<EntityItem>> entry : map.asMap().entrySet()) {
                        final UUID entityUuid = entry.getKey();
                        final net.minecraft.entity.Entity entityFromUuid = player.getServerWorld().getEntityFromUuid(entityUuid);
                        if (entityFromUuid != null) {
                            final List<Entity> entities = entry.getValue()
                                    .stream()
                                    .map(EntityUtil::fromNative)
                                    .collect(Collectors.toList());
                            if (!entities.isEmpty()) {
                                DropItemEvent.Custom event =
                                        SpongeEventFactory.createDropItemEventCustom(Sponge.getCauseStackManager().getCurrentCause(),
                                                entities);
                                SpongeImpl.postEvent(event);
                                if (!event.isCancelled()) {
                                    processSpawnedEntities(player, event);
                                }
                            }
                        }
                    }
                });
                context.getCapturedItemStackSupplier().ifPresentAndNotEmpty(drops -> {
                    final List<EntityItem> items =
                            drops.stream().map(drop -> drop.create(player.getServerWorld())).collect(Collectors.toList());
                    final List<Entity> entities = items
                            .stream()
                            .map(EntityUtil::fromNative)
                            .collect(Collectors.toList());
                    if (!entities.isEmpty()) {
                        DropItemEvent.Custom event = SpongeEventFactory.createDropItemEventCustom(Sponge.getCauseStackManager().getCurrentCause(),
                                entities);
                        SpongeImpl.postEvent(event);
                        if (!event.isCancelled()) {
                            processSpawnedEntities(player, event);
                        }
                    }

                });
            }
        }
        context.getCapturedBlockSupplier()
                .ifPresentAndNotEmpty(snapshots -> TrackingUtil.processBlockCaptures(snapshots, state, context));
    };

    @SuppressWarnings("unchecked")
    PacketFunction ACTION = (packet, state, player, context) -> {
        final ItemStack usedStack = context.getExtra(InternalNamedCauses.Packet.ITEM_USED, ItemStack.class);
        final ItemStackSnapshot usedSnapshot = ItemStackUtil.snapshotOf(usedStack);
        final Entity spongePlayer = EntityUtil.fromNative(player);
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            Sponge.getCauseStackManager().pushCause(spongePlayer);
            Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, InternalSpawnTypes.DROPPED_ITEM);
            if (state == PacketPhase.Inventory.DROP_ITEM_WITH_HOTKEY) {
                context.getCapturedBlockSupplier()
                        .ifPresentAndNotEmpty(blocks -> TrackingUtil.processBlockCaptures(blocks, state, context));
                context.getCapturedItemsSupplier()
                        .ifPresentAndNotEmpty(items -> {

                            final ArrayList<Entity> entities = new ArrayList<>();
                            for (EntityItem item : items) {
                                entities.add(EntityUtil.fromNative(item));
                            }

                            final CPacketPlayerDigging packetIn =
                                    context.getRequiredExtra(InternalNamedCauses.Packet.CAPTURED_PACKET, CPacketPlayerDigging.class);

                            CPacketPlayerDigging.Action action = packetIn.getAction();

                            final int usedButton = action == CPacketPlayerDigging.Action.DROP_ITEM ? PacketPhase.PACKET_BUTTON_PRIMARY_ID : 1;

                        Transaction<ItemStackSnapshot> cursorTrans = new Transaction<>(ItemStackSnapshot.NONE, ItemStackSnapshot.NONE);
                        final IMixinContainer mixinContainer = ContainerUtil.toMixin( player.openContainer);
                        List<SlotTransaction> slotTrans = mixinContainer.getCapturedTransactions();
                        ClickInventoryEvent.Drop dropItemEvent = ((DropItemWithHotkeyState) state)
                                .createInventoryEvent(player, ContainerUtil.fromNative(player.openContainer), cursorTrans, Lists.newArrayList(slotTrans), entities,  usedButton);

                            SpongeImpl.postEvent(dropItemEvent);
                            if (!dropItemEvent.isCancelled() || PacketPhaseUtil.allTransactionsInvalid(dropItemEvent.getTransactions())) {
                                processSpawnedEntities(player, dropItemEvent);
                            } else {
                                ((IMixinEntityPlayerMP) player).restorePacketItem(EnumHand.MAIN_HAND);
                            }
                            slotTrans.clear();
                        mixinContainer.setCaptureInventory(false);
                        });
                context.getCapturedEntityDropSupplier()
                        .ifPresentAndNotEmpty(itemMapping -> {

                        });
            } else if (state == PacketPhase.Inventory.DROP_INVENTORY) {

                context.getCapturedBlockSupplier()
                        .ifPresentAndNotEmpty(blocks -> TrackingUtil.processBlockCaptures(blocks, state, context));

                context.getCapturedItemsSupplier()
                        .ifPresentAndNotEmpty(items -> {

                            final ArrayList<Entity> entities = new ArrayList<>();
                            for (EntityItem item : items) {
                                entities.add(EntityUtil.fromNative(item));
                            }
                            final DropItemEvent.Dispense dropItemEvent =
                                    SpongeEventFactory.createDropItemEventDispense(Sponge.getCauseStackManager().getCurrentCause(), entities);
                            SpongeImpl.postEvent(dropItemEvent);
                            if (!dropItemEvent.isCancelled()) {
                                processSpawnedEntities(player, dropItemEvent);
                            }
                        });

        } else if (state == PacketPhase.General.INTERACTION) {
            if (!context.getCapturedBlockSupplier().isEmpty()) {
                if (!TrackingUtil.processBlockCaptures(context.getCapturedBlocks(), state, context)) {
                    // Stop entities like XP from being spawned
                    return;
                }
            } else {
                context.getBlockItemDropSupplier().ifPresentAndNotEmpty(map -> {
                    final List<BlockSnapshot> capturedBlocks = context.getCapturedBlocks();
                    if (ShouldFire.DROP_ITEM_EVENT_DESTRUCT) {

                        for (BlockSnapshot blockChange : capturedBlocks) {
                            final Location<World> location = blockChange.getLocation().get();
                            final Vector3d position = location.getPosition();
                            final BlockPos blockPos = VecHelper.toBlockPos(position);
                            final Collection<EntityItem> entityItems = map.get(blockPos);
                            if (!entityItems.isEmpty()) {
                                final List<Entity> items = entityItems.stream().map(EntityUtil::fromNative).collect(Collectors.toList());
                                final DropItemEvent.Destruct event =
                                        SpongeEventFactory.createDropItemEventDestruct(Sponge.getCauseStackManager().getCurrentCause(), items);
                                SpongeImpl.postEvent(event);
                                if (!event.isCancelled()) {
                                    processSpawnedEntities(player, event);
                                }
                            }
                        }
                    } else {
                        for (BlockSnapshot blockChange : capturedBlocks) {
                            final Location<World> location = blockChange.getLocation().get();
                            final Vector3d position = location.getPosition();
                            final BlockPos blockPos = VecHelper.toBlockPos(position);
                            final Collection<EntityItem> entityItems = map.get(blockPos);
                            if (!entityItems.isEmpty()) {
                                processEntities(player, (Collection<Entity>) (Collection<?>) entityItems);
                            }
                        }
                    }
                });

                }

                context.getCapturedItemsSupplier()
                        .ifPresentAndNotEmpty(items -> {
                            if (items.isEmpty()) {
                                return;
                            }
                            final ArrayList<Entity> entities = new ArrayList<>();
                            for (EntityItem item : items) {
                                entities.add(EntityUtil.fromNative(item));
                            }
                            final DropItemEvent.Dispense dispense =
                                    SpongeEventFactory.createDropItemEventDispense(Sponge.getCauseStackManager().getCurrentCause(), entities);
                            SpongeImpl.postEvent(dispense);
                            if (!dispense.isCancelled()) {
                                processSpawnedEntities(player, dispense);
                            }
                        });
                context.getCapturedEntityDropSupplier()
                        .ifPresentAndNotEmpty(map -> {
                            if (map.isEmpty()) {
                                return;
                            }
                            final PrettyPrinter printer = new PrettyPrinter(80);
                            printer.add("Processing Interaction").centre().hr();
                            printer.add("The item stacks captured are: ");
                            for (Map.Entry<UUID, Collection<ItemDropData>> entry : map.asMap().entrySet()) {
                                printer.add("  - Entity with UUID: %s", entry.getKey());
                                for (ItemDropData stack : entry.getValue()) {
                                    printer.add("    - %s", stack);
                                }
                            }
                            printer.trace(System.err);
                        });
                context.getCapturedEntitySupplier().ifPresentAndNotEmpty(entities -> {
                    final List<Entity> projectiles = new ArrayList<>(entities.size());
                    final List<Entity> spawnEggs = new ArrayList<>(entities.size());
                    final List<Entity> normalPlacement = new ArrayList<>(entities.size());
                    final List<Entity> items = new ArrayList<>(entities.size());
                    for (Entity entity : entities) {
                        if (entity instanceof Projectile || entity instanceof EntityThrowable) {
                            projectiles.add(entity);
                        } else if (usedSnapshot.getType() == ItemTypes.SPAWN_EGG) {
                            spawnEggs.add(entity);
                        } else if (entity instanceof EntityItem) {
                            items.add(entity);
                        } else {
                            normalPlacement.add(entity);
                        }
                    }
                    if (!projectiles.isEmpty()) {
                        if (ShouldFire.SPAWN_ENTITY_EVENT) {
                            try (CauseStackManager.StackFrame frame2 = Sponge.getCauseStackManager().pushCauseFrame()) {
                                Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, InternalSpawnTypes.PROJECTILE);
                                Sponge.getCauseStackManager().pushCause(usedSnapshot);
                                final SpawnEntityEvent event =
                                        SpongeEventFactory.createSpawnEntityEvent(Sponge.getCauseStackManager().getCurrentCause(),
                                                projectiles);
                                if (!SpongeImpl.postEvent(event)) {
                                    processSpawnedEntities(player, event);
                                }
                            }
                        } else {
                            processEntities(player, projectiles);
                        }
                    }
                    if (!spawnEggs.isEmpty()) {
                        if (ShouldFire.SPAWN_ENTITY_EVENT) {
                            try (StackFrame frame2 = Sponge.getCauseStackManager().pushCauseFrame()) {
                                Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, InternalSpawnTypes.PROJECTILE);
                                Sponge.getCauseStackManager().pushCause(usedSnapshot);
                                final SpawnEntityEvent event =
                                        SpongeEventFactory.createSpawnEntityEvent(Sponge.getCauseStackManager().getCurrentCause(),
                                                spawnEggs);
                                if (!SpongeImpl.postEvent(event)) {
                                    processSpawnedEntities(player, event);
                                }
                            }
                        } else {
                            processEntities(player, spawnEggs);
                        }
                    }
                    if (!items.isEmpty()) {
                        if (ShouldFire.DROP_ITEM_EVENT_DISPENSE) {
                            final DropItemEvent.Dispense dispense = SpongeEventFactory
                                    .createDropItemEventDispense(Sponge.getCauseStackManager().getCurrentCause(), items);
                            if (!SpongeImpl.postEvent(dispense)) {
                                processSpawnedEntities(player, dispense);
                            }
                        } else {
                            processEntities(player, items);
                        }
                    }
                    if (!normalPlacement.isEmpty()) {
                        if (ShouldFire.SPAWN_ENTITY_EVENT) {
                            final SpawnEntityEvent event = SpongeEventFactory.createSpawnEntityEvent(Sponge.getCauseStackManager().getCurrentCause(),
                                    normalPlacement);
                            if (!SpongeImpl.postEvent(event)) {
                                processSpawnedEntities(player, event);
                            }
                        } else {
                            processEntities(player, normalPlacement);
                        }
                    }
                });
            }

            final IMixinContainer mixinContainer = ContainerUtil.toMixin(player.openContainer);
            mixinContainer.setCaptureInventory(false);
            mixinContainer.getCapturedTransactions().clear();
        }
    };

    static void processSpawnedEntities(EntityPlayerMP player, SpawnEntityEvent event) {
        List<Entity> entities = event.getEntities();
        processEntities(player, entities);
    }

    static void processEntities(EntityPlayerMP player, Collection<Entity> entities) {
        for (Entity entity : entities) {
            entity.setCreator(player.getUniqueID());
            EntityUtil.getMixinWorld(entity).forceSpawnEntity(entity);
        }
    }

    PacketFunction CREATIVE = (packet, state, player, context) -> {
        context.getCapturedItemsSupplier()
                .ifPresentAndNotEmpty(items -> {
                    if (items.isEmpty()) {
                        return;
                    }
                    try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                        Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, InternalSpawnTypes.DROPPED_ITEM);
                        Sponge.getCauseStackManager().pushCause(player);
                        final ArrayList<Entity> entities = new ArrayList<>();
                        for (EntityItem item : items) {
                            entities.add(EntityUtil.fromNative(item));
                        }
                        final DropItemEvent.Dispense dispense =
                                SpongeEventFactory.createDropItemEventDispense(Sponge.getCauseStackManager().getCurrentCause(), entities);
                        SpongeImpl.postEvent(dispense);
                        if (!dispense.isCancelled()) {
                            processSpawnedEntities(player, dispense);
                        }
                    }
                });
    };

    PacketFunction INVENTORY = (packet, state, player, context) -> {
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
        // See MixinNetHandlerPlayServer processClickWindow redirect for rest of
        // fix.
        // --bloodmc
        final IMixinContainer mixinContainer = ContainerUtil.toMixin(player.openContainer);
        if (!mixinContainer.capturingInventory()) {
            mixinContainer.getCapturedTransactions().clear();
            return;
        }

        // TODO clear this shit out of the context
        final CPacketClickWindow packetIn = context.getRequiredExtra(InternalNamedCauses.Packet.CAPTURED_PACKET, CPacketClickWindow.class);
        final ItemStackSnapshot lastCursor = context.getRequiredExtra(InternalNamedCauses.Packet.CURSOR, ItemStackSnapshot.class);
        final ItemStackSnapshot newCursor = ItemStackUtil.snapshotOf(player.inventory.getItemStack());
        final Transaction<ItemStackSnapshot> transaction = new Transaction<>(lastCursor, newCursor);

        final Container openContainer = player.openContainer;
        final List<SlotTransaction> slotTransactions = mixinContainer.getCapturedTransactions();

        final int usedButton = packetIn.getUsedButton();
        final List<Entity> capturedItems = new ArrayList<>();
        for (EntityItem entityItem : context.getCapturedItems()) {
            capturedItems.add(EntityUtil.fromNative(entityItem));
        }
        try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            Sponge.getCauseStackManager().pushCause(player);
            Sponge.getCauseStackManager().pushCause(openContainer);
            final ClickInventoryEvent inventoryEvent;
            if (state instanceof BasicInventoryPacketState) {
                inventoryEvent =
                        ((BasicInventoryPacketState) state)
                                .createInventoryEvent(player, ContainerUtil.fromNative(openContainer), transaction,
                                        Lists.newArrayList(slotTransactions),
                                        capturedItems,
                                        usedButton);
            } else {
                inventoryEvent = null;
            }

        // Some mods may override container detectAndSendChanges method and prevent captures
        // If this happens and we captured no entities, avoid firing events
        if (mixinContainer.getCapturedTransactions().isEmpty() && capturedItems.isEmpty()) {
            mixinContainer.setCaptureInventory(false);
            return;
        }if (inventoryEvent != null) {
            // Don't fire inventory drop events when there are no entities
            if (inventoryEvent instanceof AffectEntityEvent && ((AffectEntityEvent) inventoryEvent).getEntities().isEmpty()) {
                slotTransactions.clear();
                mixinContainer.setCaptureInventory(false);
                return;
            }

                // The client sends several packets all at once for drag events
                // - we
                // only care about the last one.
                // Therefore, we never add any 'fake' transactions, as the final
                // packet has everything we want.
                if (!(inventoryEvent instanceof ClickInventoryEvent.Drag)) {
                    PacketPhaseUtil.validateCapturedTransactions(packetIn.getSlotId(), openContainer, inventoryEvent.getTransactions());
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
                } else if (inventoryEvent instanceof ClickInventoryEvent.Drag) {
                    int increment;

                    increment = slotTransactions.stream().filter((t) -> !t.isValid()).collect(Collectors.summingInt((t) -> t.getFinal()
                            .getQuantity()));

                    final ItemStack cursor = inventoryEvent.getCursorTransaction().getFinal().createStack();
                    cursor.setQuantity(cursor.getQuantity() + increment);
                    PacketPhaseUtil.handleCustomCursor(player, cursor.createSnapshot());
                } else if (inventoryEvent instanceof ClickInventoryEvent.Double && !(inventoryEvent instanceof ClickInventoryEvent.Shift)) {
                    int decrement;

                    decrement = slotTransactions.stream().filter((t) -> !t.isValid()).collect(Collectors.summingInt((t) -> t.getOriginal()
                            .getQuantity()));

                    final ItemStack cursor = inventoryEvent.getCursorTransaction().getFinal().createStack();
                    cursor.setQuantity(cursor.getQuantity() - decrement);
                    PacketPhaseUtil.handleCustomCursor(player, cursor.createSnapshot());
                }
                if (inventoryEvent instanceof SpawnEntityEvent) {
                    processSpawnedEntities(player, (SpawnEntityEvent) inventoryEvent);
                } else if (!context.getCapturedEntitySupplier().isEmpty()) {
                    SpawnEntityEvent spawnEntityEvent = SpongeEventFactory.createSpawnEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), context.getCapturedEntities());
                    SpongeImpl.postEvent(spawnEntityEvent);
                    if (!spawnEntityEvent.isCancelled()) {
                        processSpawnedEntities(player, spawnEntityEvent);}
                    }
                }
            }
        }
        slotTransactions.clear();
        mixinContainer.setCaptureInventory(false);
    };
    PacketFunction USE_ITEM = ((packet, state, player, context) -> {
        final ItemStack itemStack = context.getRequiredExtra(InternalNamedCauses.Packet.ITEM_USED, ItemStack.class);
        final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(itemStack);
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            Sponge.getCauseStackManager().pushCause(player);
            Sponge.getCauseStackManager().pushCause(snapshot);
            Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE,
                    itemStack.getType() == ItemTypes.SPAWN_EGG ? InternalSpawnTypes.SPAWN_EGG : InternalSpawnTypes.PLACEMENT);
            context.getCapturedEntitySupplier()
                    .ifPresentAndNotEmpty(entities -> {
                        final SpawnEntityEvent spawnEntityEvent =
                                SpongeEventFactory.createSpawnEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), entities);
                        SpongeImpl.postEvent(spawnEntityEvent);
                        if (!spawnEntityEvent.isCancelled()) {
                            processSpawnedEntities(player, spawnEntityEvent);
                        }
                    });
        }
        context.getCapturedBlockSupplier()
                .ifPresentAndNotEmpty(
                        originalBlocks -> TrackingUtil.processBlockCaptures(originalBlocks, state, context));

    });
    PacketFunction PLACE_BLOCK = (packet, state, player, context) -> {
        if (state == PacketPhase.General.INVALID) { // This basically is an out
                                                    // of world place, and
                                                    // nothing should occur
                                                    // here.
            return;
        }
        final IMixinWorldServer mixinWorld = (IMixinWorldServer) player.world;

        // Note - CPacketPlayerTryUseItem is swapped with
        // CPacketPlayerBlockPlacement
        final ItemStack itemStack = context.getRequiredExtra(InternalNamedCauses.Packet.ITEM_USED, ItemStack.class);
        final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(itemStack);
        context.getCapturedEntitySupplier()
                .ifPresentAndNotEmpty(entities -> {
                    try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                        Sponge.getCauseStackManager().pushCause(player);
                        Sponge.getCauseStackManager().pushCause(snapshot);
                        Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.SPAWN_EGG);
                        final SpawnEntityEvent spawnEntityEvent =
                                SpongeEventFactory.createSpawnEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), entities);
                        SpongeImpl.postEvent(spawnEntityEvent);
                        if (!spawnEntityEvent.isCancelled()) {
                            processSpawnedEntities(player, spawnEntityEvent);
                        }
                    }
                });
        context.getCapturedBlockSupplier()
                .ifPresentAndNotEmpty(
                        originalBlocks -> {
                            Sponge.getCauseStackManager().pushCause(player);
                            boolean success = TrackingUtil.processBlockCaptures(originalBlocks, state,
                                    context);
                            if (!success && snapshot != ItemTypeRegistryModule.NONE_SNAPSHOT) {
                                Sponge.getCauseStackManager().pushCause(player);
                                EnumHand hand = ((CPacketPlayerTryUseItemOnBlock) packet).getHand();
                                PacketPhaseUtil.handlePlayerSlotRestore(player, (net.minecraft.item.ItemStack) itemStack, hand);
                            }
                            Sponge.getCauseStackManager().popCause();
                        });
        context.getCapturedItemStackSupplier().ifPresentAndNotEmpty(drops -> {
            final List<Entity> entities =
                    drops.stream().map(drop -> drop.create(player.getServerWorld())).map(EntityUtil::fromNative)
                            .collect(Collectors.toList());
            if (!entities.isEmpty()) {
                try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                    Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLACEMENT);
                    Sponge.getCauseStackManager().pushCause(player);
                    DropItemEvent.Custom event =
                            SpongeEventFactory.createDropItemEventCustom(Sponge.getCauseStackManager().getCurrentCause(), entities);
                    SpongeImpl.postEvent(event);
                    if (!event.isCancelled()) {
                        for (Entity droppedItem : event.getEntities()) {
                            droppedItem.setCreator(player.getUniqueID());
                            mixinWorld.forceSpawnEntity(droppedItem);
                        }
                    }
                }
            }

        });

        final IMixinContainer mixinContainer = ContainerUtil.toMixin(player.openContainer);
        mixinContainer.setCaptureInventory(false);
        mixinContainer.getCapturedTransactions().clear();
    };
    PacketFunction HELD_ITEM_CHANGE = ((packet, state, player, context) -> {
        final CPacketHeldItemChange itemChange = (CPacketHeldItemChange) packet;
        final int previousSlot = context.getRequiredExtra(InternalNamedCauses.Packet.PREVIOUS_HIGHLIGHTED_SLOT, Integer.class);
        final Container inventoryContainer = player.inventoryContainer;
        final InventoryPlayer inventory = player.inventory;
        int preHotbarSize = inventory.mainInventory.size() - InventoryPlayer.getHotbarSize() + inventory.armorInventory.size() + 4 + 1; // Crafting Grid & Result
        final Slot sourceSlot = inventoryContainer.getSlot(previousSlot + preHotbarSize);
        final Slot targetSlot = inventoryContainer.getSlot(itemChange.getSlotId() + preHotbarSize);

        ItemStackSnapshot sourceSnapshot = ItemStackUtil.snapshotOf(sourceSlot.getStack());
        ItemStackSnapshot targetSnapshot = ItemStackUtil.snapshotOf(targetSlot.getStack());
        SlotTransaction sourceTransaction =
                new SlotTransaction(ContainerUtil.getSlotAdapter(inventoryContainer, previousSlot + preHotbarSize), sourceSnapshot, sourceSnapshot);
        SlotTransaction targetTransaction =
                new SlotTransaction(ContainerUtil.getSlotAdapter(inventoryContainer, itemChange.getSlotId() + preHotbarSize), targetSnapshot, targetSnapshot);
        try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            Sponge.getCauseStackManager().pushCause(player);
            ImmutableList<SlotTransaction> transactions =
                    new ImmutableList.Builder<SlotTransaction>().add(sourceTransaction).add(targetTransaction).build();
            final ChangeInventoryEvent.Held changeInventoryEventHeld = SpongeEventFactory
                    .createChangeInventoryEventHeld(Sponge.getCauseStackManager().getCurrentCause(), (Inventory) inventoryContainer, transactions);
            Container openContainer = player.openContainer;
            SpongeImpl.postEvent(changeInventoryEventHeld);
            if (changeInventoryEventHeld.isCancelled() || PacketPhaseUtil.allTransactionsInvalid(changeInventoryEventHeld.getTransactions())) {
                player.connection.sendPacket(new SPacketHeldItemChange(previousSlot));
            } else {
                PacketPhaseUtil.handleSlotRestore(player, openContainer, changeInventoryEventHeld.getTransactions(), false);
                inventory.currentItem = itemChange.getSlotId();
                player.markPlayerActive();
            }
        }
    });
    PacketFunction CLOSE_WINDOW = ((packet, state, player, context) -> {
        final Container container = context.getRequiredExtra(InternalNamedCauses.Packet.OPEN_CONTAINER, Container.class);
        ItemStackSnapshot lastCursor = context.getRequiredExtra(InternalNamedCauses.Packet.CURSOR, ItemStackSnapshot.class);
        ItemStackSnapshot newCursor = ItemStackUtil.snapshotOf(player.inventory.getItemStack());
        try (CauseStackManager.StackFrame frame1 = Sponge.getCauseStackManager().pushCauseFrame()) {
            Sponge.getCauseStackManager().pushCause(player);
            InteractInventoryEvent.Close event =
                    SpongeCommonEventFactory.callInteractInventoryCloseEvent(container, player, lastCursor, newCursor, true);
            if (event.isCancelled()) {
                return;
            }
        }
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            Sponge.getCauseStackManager().pushCause(player);
            Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLACEMENT);// Non-merged
                                                                                                        // items
            context.getCapturedItemsSupplier().ifPresentAndNotEmpty(items -> {
                final List<Entity> entities = items
                        .stream()
                        .map(EntityUtil::fromNative)
                        .collect(Collectors.toList());
                if (!entities.isEmpty()) {
                    DropItemEvent.Custom drop =
                            SpongeEventFactory.createDropItemEventCustom(Sponge.getCauseStackManager().getCurrentCause(), entities);
                    SpongeImpl.postEvent(drop);
                    if (!drop.isCancelled()) {
                        for (Entity droppedItem : drop.getEntities()) {
                            droppedItem.setCreator(player.getUniqueID());
                            ((IMixinWorldServer) player.getServerWorld()).forceSpawnEntity(droppedItem);
                        }
                    }
                }
            });
            // Pre-merged items
            context.getCapturedItemStackSupplier().ifPresentAndNotEmpty(stacks -> {
                final List<EntityItem> items = stacks.stream()
                        .map(drop -> drop.create(player.getServerWorld()))
                        .collect(Collectors.toList());
                final List<Entity> entities = items
                        .stream()
                        .map(EntityUtil::fromNative)
                        .collect(Collectors.toList());
                if (!entities.isEmpty()) {
                    DropItemEvent.Custom drop =
                            SpongeEventFactory.createDropItemEventCustom(Sponge.getCauseStackManager().getCurrentCause(), entities);
                    SpongeImpl.postEvent(drop);
                    if (!drop.isCancelled()) {
                        for (Entity droppedItem : drop.getEntities()) {
                            droppedItem.setCreator(player.getUniqueID());
                            ((IMixinWorldServer) player.getServerWorld()).forceSpawnEntity(droppedItem);
                        }
                    }
                }
            });
        }
        context.getCapturedBlockSupplier()
                .ifPresentAndNotEmpty(blocks -> TrackingUtil.processBlockCaptures(blocks, state, context));

    });
    PacketFunction ENCHANTMENT = ((packet, state, player, context) -> {
    });
    PacketFunction CLIENT_STATUS = ((packet, state, player, context) -> {
        if (state == PacketPhase.Inventory.OPEN_INVENTORY) {
            final ItemStackSnapshot lastCursor = context.getRequiredExtra(InternalNamedCauses.Packet.CURSOR, ItemStackSnapshot.class);
            final ItemStackSnapshot newCursor = ItemStackUtil.snapshotOf(player.inventory.getItemStack());
            final Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(lastCursor, newCursor);
            try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                Sponge.getCauseStackManager().pushCause(player);
                final InteractInventoryEvent.Open event =
                        SpongeEventFactory.createInteractInventoryEventOpen(Sponge.getCauseStackManager().getCurrentCause(), cursorTransaction,
                                ContainerUtil.fromNative(player.openContainer));
                SpongeImpl.postEvent(event);
                if (event.isCancelled()) {
                    player.closeScreen();
                } else {
                    // Custom cursor
                    if (!event.getCursorTransaction().isValid()) {
                    PacketPhaseUtil.handleCustomCursor(player, event.getCursorTransaction().getOriginal());
                } else if (event.getCursorTransaction().getCustom().isPresent()) {
                        PacketPhaseUtil.handleCustomCursor(player, event.getCursorTransaction().getFinal());
                    }
                }
            }
        }
    });
    PacketFunction RESOURCE_PACKET = (packet, state, player, context) -> {
        final NetHandlerPlayServer connection = player.connection;
        final IMixinNetHandlerPlayServer mixinHandler = (IMixinNetHandlerPlayServer) connection;
        final CPacketResourcePackStatus resource = (CPacketResourcePackStatus) packet;
        final ResourcePackStatusEvent.ResourcePackStatus status;
        ResourcePack pack = ((IMixinPacketResourcePackSend) mixinHandler.getPendingResourcePackQueue().peek()).getResourcePack();
        switch (resource.action) {
        case ACCEPTED:
            status = ResourcePackStatusEvent.ResourcePackStatus.ACCEPTED;
            break;
        case DECLINED:
            status = ResourcePackStatusEvent.ResourcePackStatus.DECLINED;
            break;
        case SUCCESSFULLY_LOADED:
            status = ResourcePackStatusEvent.ResourcePackStatus.SUCCESSFULLY_LOADED;
            break;
        case FAILED_DOWNLOAD:
            status = ResourcePackStatusEvent.ResourcePackStatus.FAILED;
            break;
        default:
            throw new AssertionError();
        }
        SpongeImpl.postEvent(
                SpongeEventFactory.createResourcePackStatusEvent(Sponge.getCauseStackManager().getCurrentCause(), pack, (Player) player, status));
        if (status.wasSuccessful().isPresent()) {
            mixinHandler.getPendingResourcePackQueue().remove();

            if (!mixinHandler.getPendingResourcePackQueue().isEmpty()) {

                while (mixinHandler.getPendingResourcePackQueue().size() > 1) {
                    // Fire events so other plugins know what happened to their
                    // resource packs.
                    pack = ((IMixinPacketResourcePackSend) mixinHandler.getPendingResourcePackQueue().remove()).getResourcePack();
                    if (status == ResourcePackStatusEvent.ResourcePackStatus.DECLINED) {
                        SpongeImpl.postEvent(SpongeEventFactory.createResourcePackStatusEvent(Sponge.getCauseStackManager().getCurrentCause(), pack,
                                (Player) player,
                                ResourcePackStatusEvent.ResourcePackStatus.DECLINED));
                    } else {
                        // Say it was successful even if it wasn't. Minecraft
                        // makes no guarantees, and I don't want to change the
                        // API.
                        // In addition, I would assume this would result in the
                        // expected behavior from plugins.
                        SpongeImpl.postEvent(SpongeEventFactory.createResourcePackStatusEvent(Sponge.getCauseStackManager().getCurrentCause(), pack,
                                (Player) player,
                                ResourcePackStatusEvent.ResourcePackStatus.ACCEPTED));
                        SpongeImpl.postEvent(SpongeEventFactory.createResourcePackStatusEvent(Sponge.getCauseStackManager().getCurrentCause(), pack,
                                (Player) player,
                                ResourcePackStatusEvent.ResourcePackStatus.SUCCESSFULLY_LOADED));
                    }
                }
                if (connection.getNetworkManager().isChannelOpen()) {
                    connection.sendPacket(mixinHandler.getPendingResourcePackQueue().element());
                }
            }
        }
    };
    PacketFunction MOVEMENT = (packet, state, player, context) -> {
        context.getCapturedBlockSupplier()
                .ifPresentAndNotEmpty(blocks -> TrackingUtil.processBlockCaptures(blocks, state, context));
    };

    PacketFunction UNKNOWN_PACKET = (packet, state, player, context) -> {
        try (CauseStackManager.StackFrame frame1 = Sponge.getCauseStackManager().pushCauseFrame()) {
            Sponge.getCauseStackManager().pushCause(player);
            Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLACEMENT);
            context.getCapturedBlockSupplier().ifPresentAndNotEmpty(blocks -> TrackingUtil.processBlockCaptures(blocks, state, context));
            context.getCapturedEntitySupplier().ifPresentAndNotEmpty(entities -> {
                SpawnEntityEvent event =
                        SpongeEventFactory.createSpawnEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), entities);
                SpongeImpl.postEvent(event);
                if (!event.isCancelled()) {
                    processSpawnedEntities(player, event);

                }
            });
            context.getCapturedItemsSupplier().ifPresentAndNotEmpty(entities -> {
                final List<Entity> items = entities.stream().map(EntityUtil::fromNative).collect(Collectors.toList());
                SpawnEntityEvent event =
                        SpongeEventFactory.createSpawnEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), items);
                SpongeImpl.postEvent(event);
                if (!event.isCancelled()) {
                    processSpawnedEntities(player, event);

                }
            });
        }
        context.getCapturedEntityDropSupplier().ifPresentAndNotEmpty(map -> {
            final PrettyPrinter printer = new PrettyPrinter(80);
            printer.add("Processing Interact At Entity").centre().hr();
            printer.add("The item stacks captured are: ");

            for (Map.Entry<UUID, Collection<ItemDropData>> entry : map.asMap().entrySet()) {
                printer.add("  - Entity with UUID: %s", entry.getKey());
                for (ItemDropData stack : entry.getValue()) {
                    printer.add("    - %s", stack);
                }
            }
            printer.trace(System.err);
        });
        context.getCapturedEntityItemDropSupplier().ifPresentAndNotEmpty(map -> {
            for (Map.Entry<UUID, Collection<EntityItem>> entry : map.asMap().entrySet()) {
                final UUID entityUuid = entry.getKey();
                final net.minecraft.entity.Entity entityFromUuid = player.getServerWorld().getEntityFromUuid(entityUuid);
                final Entity affectedEntity = EntityUtil.fromNative(entityFromUuid);
                if (entityFromUuid != null) {
                    final List<Entity> entities = entry.getValue()
                            .stream()
                            .map(EntityUtil::fromNative)
                            .collect(Collectors.toList());
                    if (!entities.isEmpty()) {
                        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                            Sponge.getCauseStackManager().pushCause(player);
                            Sponge.getCauseStackManager().pushCause(affectedEntity);
                            Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.CUSTOM);
                            DropItemEvent.Custom event = SpongeEventFactory.createDropItemEventCustom(Sponge.getCauseStackManager().getCurrentCause(),
                                    entities);
                            SpongeImpl.postEvent(event);
                            if (!event.isCancelled()) {
                                processSpawnedEntities(player, event);

                            }
                        }
                    }
                }
            }
        });
        context.getCapturedItemStackSupplier().ifPresentAndNotEmpty(drops -> {
            final List<EntityItem> items =
                    drops.stream().map(drop -> drop.create(player.getServerWorld())).collect(Collectors.toList());
            final List<Entity> entities = items
                    .stream()
                    .map(EntityUtil::fromNative)
                    .collect(Collectors.toList());
            if (!entities.isEmpty()) {
                try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                    Sponge.getCauseStackManager().pushCause(player);
                    Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.CUSTOM);
                    DropItemEvent.Custom event =
                            SpongeEventFactory.createDropItemEventCustom(Sponge.getCauseStackManager().getCurrentCause(), entities);
                    SpongeImpl.postEvent(event);
                    if (!event.isCancelled()) {
                        processSpawnedEntities(player, event);

                    }
                }
            }

        });

    };

    PacketFunction HANDLED_EXTERNALLY = UNKNOWN_PACKET;

    void unwind(Packet<?> packet, IPhaseState<?> state, EntityPlayerMP player, PhaseContext<?> context);

}
