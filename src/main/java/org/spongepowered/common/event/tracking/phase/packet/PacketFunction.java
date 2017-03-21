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
import net.minecraft.network.play.client.CPacketClientSettings;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketResourcePackStatus;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.SPacketHeldItemChange;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.PlayerChangeClientSettingsEvent;
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
import org.spongepowered.api.text.chat.ChatVisibility;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.ItemDropData;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.interfaces.IMixinContainer;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayerMP;
import org.spongepowered.common.interfaces.network.IMixinNetHandlerPlayServer;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.item.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.item.inventory.util.ContainerUtil;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.registry.type.ItemTypeRegistryModule;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;
import org.spongepowered.common.util.LanguageUtil;
import org.spongepowered.common.util.SkinUtil;
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

    PacketFunction USE_ENTITY = (packet, state, player, context) -> {
        final CPacketUseEntity useEntityPacket = (CPacketUseEntity) packet;
        final net.minecraft.entity.Entity entity = useEntityPacket.getEntityFromWorld(player.worldObj);
        if (entity == null) {
            // Something happened?
        }
        //final Optional<ItemStack> itemStack = context.firstNamed(InternalNamedCauses.Packet.ITEM_USED, ItemStack.class);
        final IMixinWorldServer mixinWorldServer = (IMixinWorldServer) player.worldObj;
        final CauseTracker causeTracker = mixinWorldServer.getCauseTracker();
        EntityUtil.toMixin(entity).setNotifier(player.getUniqueID());

        if (state == PacketPhase.General.ATTACK_ENTITY) {
            context.getCapturedItemsSupplier()
                    .ifPresentAndNotEmpty(items -> {
                        // For destruction, this should be empty, however, some times, it may not be?
                        final PrettyPrinter printer = new PrettyPrinter(60);
                        printer.add("Processing Attack Entity").centre().hr();
                        printer.add("There are some captured items after the entity was destructed!");
                        printer.addWrapped(60, "%s : %s", "Items captured", items);
                        printer.add("Stacktrace:");
                        printer.add(new Exception("Stack trace"));
                        printer.trace(System.err, SpongeImpl.getLogger(), Level.TRACE);
                    });
            context.getCapturedBlockSupplier()
                    .ifPresentAndNotEmpty(blocks ->
                            TrackingUtil.processBlockCaptures(blocks, causeTracker, state, context));
            context.getCapturedEntityDropSupplier().ifPresentAndNotEmpty(map -> {
                for (Map.Entry<UUID, Collection<ItemDropData>> entry : map.asMap().entrySet()) {
                    final UUID key = entry.getKey();
                    final Optional<Entity> affectedEntity = causeTracker.getWorld().getEntity(key);
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
                        final List<Entity> itemEntities = items.stream()
                                .map(data -> data.create(causeTracker.getMinecraftWorld()))
                                .map(EntityUtil::fromNative)
                                .collect(Collectors.toList());
                        final Cause cause = Cause.source(EntitySpawnCause.builder()
                                .entity(affectedEntity.get())
                                .type(InternalSpawnTypes.DROPPED_ITEM)
                                .build()
                        )
                                .named(NamedCause.of("Attacker", player))
                                .build();
                        final DropItemEvent.Destruct
                                destruct =
                                SpongeEventFactory.createDropItemEventDestruct(cause, itemEntities, causeTracker.getWorld());
                        SpongeImpl.postEvent(destruct);
                        if (!destruct.isCancelled()) {
                            processSpawnedEntities(player, causeTracker, destruct);

                        }
                    }
                }
            });
            context.getCapturedEntityItemDropSupplier().ifPresentAndNotEmpty(map -> {
                for (Map.Entry<UUID, Collection<EntityItem>> entry : map.asMap().entrySet()) {
                    final UUID key = entry.getKey();
                    final Optional<Entity> attackedEntities = causeTracker.getWorld().getEntity(key);
                    if (!attackedEntities.isPresent()) {
                        continue;
                    }
                    final List<Entity> items = entry.getValue().stream().map(EntityUtil::fromNative).collect(Collectors.toList());
                    final Cause cause = Cause.source(EntitySpawnCause.builder()
                            .entity(EntityUtil.fromNative(player))
                            .type(InternalSpawnTypes.DROPPED_ITEM)
                            .build()
                    )
                            .named(NamedCause.of("Attacker", player))
                            .build();
                    final DropItemEvent.Destruct
                            destruct =
                            SpongeEventFactory.createDropItemEventDestruct(cause, items, causeTracker.getWorld());
                    SpongeImpl.postEvent(destruct);
                    if (!destruct.isCancelled()) {
                        processSpawnedEntities(player, causeTracker, destruct);

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
            context.getCapturedItemsSupplier().ifPresentAndNotEmpty(entities -> {
                final List<Entity> items = entities.stream().map(EntityUtil::fromNative).collect(Collectors.toList());
                final Cause cause = Cause.source(EntitySpawnCause.builder()
                        .entity((Player) player)
                        .type(InternalSpawnTypes.PLACEMENT)
                        .build()
                ).build();
                SpawnEntityEvent event = SpongeEventFactory.createSpawnEntityEvent(cause, items, (World) mixinWorldServer);
                SpongeImpl.postEvent(event);
                if (!event.isCancelled()) {
                    for (Entity spawnedEntity : event.getEntities()) {
                        spawnedEntity.setCreator(player.getUniqueID());
                        mixinWorldServer.forceSpawnEntity(spawnedEntity);
                    }
                }
            });
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
            context.getCapturedEntitySupplier().ifPresentAndNotEmpty(entities -> {
                final Cause cause = Cause.source(EntitySpawnCause.builder()
                        .entity((Player) player)
                        .type(InternalSpawnTypes.PLACEMENT)
                        .build()
                ).build();
                SpawnEntityEvent event = SpongeEventFactory.createSpawnEntityEvent(cause, entities, (World) mixinWorldServer);
                SpongeImpl.postEvent(event);
                if (!event.isCancelled()) {
                    for (Entity spawnedEntity : event.getEntities()) {
                        spawnedEntity.setCreator(player.getUniqueID());
                        mixinWorldServer.forceSpawnEntity(spawnedEntity);
                    }
                }
            });
            context.getCapturedItemsSupplier().ifPresentAndNotEmpty(entities -> {
                final List<Entity> items = entities.stream().map(EntityUtil::fromNative).collect(Collectors.toList());
                final Cause cause = Cause.source(EntitySpawnCause.builder()
                        .entity((Player) player)
                        .type(InternalSpawnTypes.PLACEMENT)
                        .build()
                ).build();
                SpawnEntityEvent event = SpongeEventFactory.createSpawnEntityEvent(cause, items, (World) mixinWorldServer);
                SpongeImpl.postEvent(event);
                if (!event.isCancelled()) {
                    for (Entity spawnedEntity : event.getEntities()) {
                        spawnedEntity.setCreator(player.getUniqueID());
                        mixinWorldServer.forceSpawnEntity(spawnedEntity);
                    }
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
                    final net.minecraft.entity.Entity entityFromUuid = causeTracker.getMinecraftWorld().getEntityFromUuid(entityUuid);
                    final Entity affectedEntity = EntityUtil.fromNative(entityFromUuid);
                    if (entityFromUuid != null) {
                        final Cause cause = Cause.source(
                                EntitySpawnCause.builder()
                                        .entity(affectedEntity)
                                        .type(InternalSpawnTypes.PLACEMENT)
                                        .build()
                        ).named(NamedCause.notifier(player))
                                .build();
                        final List<Entity> entities = entry.getValue()
                                .stream()
                                .map(EntityUtil::fromNative)
                                .collect(Collectors.toList());
                        if (!entities.isEmpty()) {
                            DropItemEvent.Custom event = SpongeEventFactory.createDropItemEventCustom(cause, entities, causeTracker.getWorld());
                            SpongeImpl.postEvent(event);
                            if (!event.isCancelled()) {
                                for (Entity droppedItem : event.getEntities()) {
                                    droppedItem.setCreator(player.getUniqueID());
                                    mixinWorldServer.forceSpawnEntity(droppedItem);
                                }
                            }
                        }
                    }
                }
            });
            context.getCapturedItemStackSupplier().ifPresentAndNotEmpty(drops -> {
                final List<EntityItem>
                        items =
                        drops.stream().map(drop -> drop.create(causeTracker.getMinecraftWorld())).collect(Collectors.toList());
                final Cause cause = Cause.source(
                        EntitySpawnCause.builder()
                                .entity((Entity) entity)
                                .type(InternalSpawnTypes.PLACEMENT)
                                .build()
                ).named(NamedCause.notifier(player))
                        .build();
                final List<Entity> entities = items
                        .stream()
                        .map(EntityUtil::fromNative)
                        .collect(Collectors.toList());
                if (!entities.isEmpty()) {
                    DropItemEvent.Custom event = SpongeEventFactory.createDropItemEventCustom(cause, entities, causeTracker.getWorld());
                    SpongeImpl.postEvent(event);
                    if (!event.isCancelled()) {
                        for (Entity droppedItem : event.getEntities()) {
                            droppedItem.setCreator(player.getUniqueID());
                            mixinWorldServer.forceSpawnEntity(droppedItem);
                        }
                    }
                }

            });
        }
        context.getCapturedBlockSupplier()
                .ifPresentAndNotEmpty(snapshots ->
                        TrackingUtil.processBlockCaptures(snapshots, causeTracker, state, context)
                );
    };

    @SuppressWarnings("unchecked") PacketFunction ACTION = (packet, state, player, context) -> {
        final CauseTracker causeTracker = ((IMixinWorldServer) player.worldObj).getCauseTracker();
        final ItemStack usedStack = context.firstNamed(InternalNamedCauses.Packet.ITEM_USED, ItemStack.class)
                .orElse(null);
        final ItemStackSnapshot usedSnapshot = ItemStackUtil.snapshotOf(usedStack);
        final Entity spongePlayer = EntityUtil.fromNative(player);
        if (state == PacketPhase.Inventory.DROP_ITEM_WITH_HOTKEY) {
            context.getCapturedBlockSupplier()
                    .ifPresentAndNotEmpty(blocks ->
                            TrackingUtil.processBlockCaptures(blocks, causeTracker, state, context)
                    );
            context.getCapturedItemsSupplier()
                    .ifPresentAndNotEmpty(items -> {
                        final Cause cause = Cause.source(EntitySpawnCause.builder()
                                .entity(spongePlayer)
                                .type(InternalSpawnTypes.DROPPED_ITEM)
                                .build())
                                .build();
                        final ArrayList<Entity> entities = new ArrayList<>();
                        for (EntityItem item : items) {
                            entities.add(EntityUtil.fromNative(item));
                        }
                        final DropItemEvent.Dispense
                                dropItemEvent =
                                SpongeEventFactory.createDropItemEventDispense(cause, entities, causeTracker.getWorld());
                        SpongeImpl.postEvent(dropItemEvent);
                        if (!dropItemEvent.isCancelled()) {
                            processSpawnedEntities(player, causeTracker, dropItemEvent);
                        } else {
                            ((IMixinEntityPlayerMP) player).restorePacketItem(EnumHand.MAIN_HAND);
                        }
                    });
            context.getCapturedEntityDropSupplier()
                    .ifPresentAndNotEmpty(itemMapping -> {

                    });
        } else if (state == PacketPhase.Inventory.DROP_INVENTORY) {

            context.getCapturedBlockSupplier()
                    .ifPresentAndNotEmpty(blocks -> TrackingUtil.processBlockCaptures(blocks, causeTracker, state, context));

            context.getCapturedItemsSupplier()
                    .ifPresentAndNotEmpty(items -> {
                        final Cause cause = Cause.source(EntitySpawnCause.builder()
                                .entity(spongePlayer)
                                .type(InternalSpawnTypes.DROPPED_ITEM)
                                .build())
                                .build();
                        final ArrayList<Entity> entities = new ArrayList<>();
                        for (EntityItem item : items) {
                            entities.add(EntityUtil.fromNative(item));
                        }
                        final DropItemEvent.Dispense
                                dropItemEvent =
                                SpongeEventFactory.createDropItemEventDispense(cause, entities, causeTracker.getWorld());
                        SpongeImpl.postEvent(dropItemEvent);
                        if (!dropItemEvent.isCancelled()) {
                            processSpawnedEntities(player, causeTracker, dropItemEvent);
                        }
                    });

        } else if (state == PacketPhase.General.INTERACTION) {
            if (!context.getCapturedBlockSupplier().isEmpty()) {
                TrackingUtil.processBlockCaptures(context.getCapturedBlockSupplier().get(), causeTracker, state, context);
            } else {
                context.getBlockItemDropSupplier().ifPresentAndNotEmpty(map -> {
                    final List<BlockSnapshot> capturedBlocks = context.getCapturedBlocks();
                    if (ShouldFire.DROP_ITEM_EVENT_DESTRUCT) {
                        final Cause cause = Cause.source(EntitySpawnCause.builder()
                                .entity(spongePlayer)
                                .type(InternalSpawnTypes.DROPPED_ITEM)
                                .build())
                                .build();
                        for (BlockSnapshot blockChange : capturedBlocks) {
                            final Location<World> location = blockChange.getLocation().get();
                            final Vector3d position = location.getPosition();
                            final BlockPos blockPos = VecHelper.toBlockPos(position);
                            final Collection<EntityItem> entityItems = map.get(blockPos);
                            if (!entityItems.isEmpty()) {
                                final List<Entity> items = entityItems.stream().map(EntityUtil::fromNative).collect(Collectors.toList());
                                final DropItemEvent.Destruct event =
                                        SpongeEventFactory.createDropItemEventDestruct(cause, items, causeTracker.getWorld());
                                SpongeImpl.postEvent(event);
                                if (!event.isCancelled()) {
                                    processSpawnedEntities(player, causeTracker, event);
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
                                processEntities(player, causeTracker, (Collection<Entity>) (Collection<?>) entityItems);
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
                        final Cause cause = Cause.source(EntitySpawnCause.builder()
                                .entity(spongePlayer)
                                .type(InternalSpawnTypes.DROPPED_ITEM)
                                .build())
                                .build();
                        final ArrayList<Entity> entities = new ArrayList<>();
                        for (EntityItem item : items) {
                            entities.add(EntityUtil.fromNative(item));
                        }
                        final DropItemEvent.Dispense
                                dispense =
                                SpongeEventFactory.createDropItemEventDispense(cause, entities, causeTracker.getWorld());
                        SpongeImpl.postEvent(dispense);
                        if (!dispense.isCancelled()) {
                            processSpawnedEntities(player, causeTracker, dispense);
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
                        final Cause cause = Cause.source(
                                EntitySpawnCause.builder()
                                        .entity(spongePlayer)
                                        .type(InternalSpawnTypes.PROJECTILE)
                                        .build())
                                .named(NamedCause.of("UsedItem", usedSnapshot))
                                .owner(player)
                                .build();
                        final SpawnEntityEvent event = SpongeEventFactory.createSpawnEntityEvent(cause, projectiles, causeTracker.getWorld());
                        if (!SpongeImpl.postEvent(event)) {
                            processSpawnedEntities(player, causeTracker, event);
                        }
                    } else {
                        processEntities(player, causeTracker, projectiles);
                    }
                }
                if (!spawnEggs.isEmpty()) {
                    if (ShouldFire.SPAWN_ENTITY_EVENT) {
                        final Cause cause = Cause.source(
                                EntitySpawnCause.builder()
                                        .entity(spongePlayer)
                                        .type(InternalSpawnTypes.SPAWN_EGG)
                                        .build())
                                .named(NamedCause.of("UsedItem", usedSnapshot))
                                .owner(player)
                                .build();
                        final SpawnEntityEvent event = SpongeEventFactory.createSpawnEntityEvent(cause, spawnEggs, causeTracker.getWorld());
                        if (!SpongeImpl.postEvent(event)) {
                            processSpawnedEntities(player, causeTracker, event);
                        }
                    } else {
                        processEntities(player, causeTracker, spawnEggs);
                    }
                }
                if (!items.isEmpty()) {
                    if (ShouldFire.DROP_ITEM_EVENT_DISPENSE) {
                        final Cause cause = Cause.source(
                                EntitySpawnCause.builder()
                                        .entity(spongePlayer)
                                        .type(InternalSpawnTypes.DROPPED_ITEM)
                                        .build())
                                .owner(player)
                                .build();
                        final DropItemEvent.Dispense dispense = SpongeEventFactory.createDropItemEventDispense(cause, items, causeTracker.getWorld());
                        if (!SpongeImpl.postEvent(dispense)) {
                            processSpawnedEntities(player, causeTracker, dispense);
                        }
                    } else {
                        processEntities(player, causeTracker, items);
                    }
                }
                if (!normalPlacement.isEmpty()) {
                    if (ShouldFire.SPAWN_ENTITY_EVENT) {
                        final Cause cause = Cause.source(
                                EntitySpawnCause.builder()
                                        .entity(spongePlayer)
                                        .type(InternalSpawnTypes.PLACEMENT)
                                        .build())
                                .build();
                        final SpawnEntityEvent event = SpongeEventFactory.createSpawnEntityEvent(cause, normalPlacement, causeTracker.getWorld());
                        if (!SpongeImpl.postEvent(event)) {
                            processSpawnedEntities(player, causeTracker, event);
                        }
                    } else {
                        processEntities(player, causeTracker, normalPlacement);
                    }
                }
            });
        }
    };

    static void processSpawnedEntities(EntityPlayerMP player, CauseTracker causeTracker, SpawnEntityEvent event) {
        List<Entity> entities = event.getEntities();
        processEntities(player, causeTracker, entities);
    }

    static void processEntities(EntityPlayerMP player, CauseTracker causeTracker, Collection<Entity> entities) {
        for (Entity entity : entities) {
            entity.setCreator(player.getUniqueID());
            causeTracker.getMixinWorld().forceSpawnEntity(entity);
        }
    }

    PacketFunction CREATIVE = (packet, state, player, context) -> {
        final CauseTracker causeTracker = ((IMixinWorldServer) player.worldObj).getCauseTracker();
        context.getCapturedItemsSupplier()
                .ifPresentAndNotEmpty(items -> {
                    if (items.isEmpty()) {
                        return;
                    }
                    final Cause cause = Cause.source(EntitySpawnCause.builder()
                            .entity((Entity) player)
                            .type(InternalSpawnTypes.DROPPED_ITEM)
                            .build())
                            .build();
                    final ArrayList<Entity> entities = new ArrayList<>();
                    for (EntityItem item : items) {
                        entities.add(EntityUtil.fromNative(item));
                    }
                    final DropItemEvent.Dispense
                            dispense =
                            SpongeEventFactory.createDropItemEventDispense(cause, entities, causeTracker.getWorld());
                    SpongeImpl.postEvent(dispense);
                    if (!dispense.isCancelled()) {
                        processSpawnedEntities(player, causeTracker, dispense);
                    }
                });
    };

    PacketFunction ENTITY_ACTION = (packet, state, player, context) -> {
        if (state == PacketPhase.General.STOP_SLEEPING) {
            context.getCapturedBlockSupplier()
                    .ifPresentAndNotEmpty(
                            originalBlocks -> TrackingUtil
                                    .processBlockCaptures(originalBlocks, ((IMixinWorldServer) player.worldObj).getCauseTracker(), state, context));
        }

    };

    PacketFunction INVENTORY = (packet, state, player, context) -> {
        // The server will disable the player's crafting after receiving a client packet
        // that did not pass validation (server click item != packet click item)
        // The server then sends a SPacketConfirmTransaction and waits for a
        // CPacketConfirmTransaction to re-enable crafting confirming that the client
        // acknowledged the denied transaction.
        // To detect when this happens, we turn off capturing so we can avoid firing
        // invalid events.
        // See MixinNetHandlerPlayServer processClickWindow redirect for rest of fix.
        // --bloodmc
        final IMixinContainer mixinContainer = ContainerUtil.toMixin(player.openContainer);
        if (!mixinContainer.capturingInventory()) {
            mixinContainer.getCapturedTransactions().clear();
            return;
        }

        final CPacketClickWindow packetIn = context.firstNamed(InternalNamedCauses.Packet.CAPTURED_PACKET, CPacketClickWindow.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected to be capturing the packet used, but no packet was captured!", context));
        final ItemStackSnapshot lastCursor = context.firstNamed(InternalNamedCauses.Packet.CURSOR, ItemStackSnapshot.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected to be capturing the cursor item in use, but found none.", context));
        final ItemStackSnapshot newCursor = ItemStackUtil.snapshotOf(player.inventory.getItemStack());
        final Transaction<ItemStackSnapshot> transaction = new Transaction<>(lastCursor, newCursor);

        final Container openContainer = player.openContainer;
        final List<SlotTransaction> slotTransactions = mixinContainer.getCapturedTransactions();

        final int usedButton = packetIn.getUsedButton();
        final List<Entity> capturedItems = new ArrayList<>();
        for (EntityItem entityItem : context.getCapturedItems()) {
            capturedItems.add(EntityUtil.fromNative(entityItem));
        }
        final Cause cause = Cause.of(NamedCause.source(player), NamedCause.of("Container", openContainer));
        final ClickInventoryEvent inventoryEvent;
        if (state instanceof BasicInventoryPacketState) {
            inventoryEvent =
                    ((BasicInventoryPacketState) state)
                            .createInventoryEvent(player, ContainerUtil.fromNative(openContainer), transaction, Lists.newArrayList(slotTransactions), capturedItems,
                                    cause, usedButton);
        } else {
            inventoryEvent = null;
        }

        if (inventoryEvent != null) {
            // The client sends several packets all at once for drag events - we only care about the last one.
            // Therefore, we never add any 'fake' transactions, as the final packet has everything we want.
            if (!(inventoryEvent instanceof ClickInventoryEvent.Drag)) {
                PacketPhaseUtil.validateCapturedTransactions(packetIn.getSlotId(), openContainer, inventoryEvent.getTransactions());
            }

            SpongeImpl.postEvent(inventoryEvent);
            if (inventoryEvent.isCancelled()) {
                if (inventoryEvent instanceof ClickInventoryEvent.Drop) {
                    capturedItems.clear();
                }

                // Restore cursor
                PacketPhaseUtil.handleCustomCursor(player, inventoryEvent.getCursorTransaction().getOriginal());

                // Restore target slots
                PacketPhaseUtil.handleSlotRestore(player, openContainer, inventoryEvent.getTransactions(), true, inventoryEvent);
            } else {
                PacketPhaseUtil.handleSlotRestore(player, openContainer, inventoryEvent.getTransactions(), false, inventoryEvent);

                // Custom cursor
                if (inventoryEvent.getCursorTransaction().getCustom().isPresent()) {
                    PacketPhaseUtil.handleCustomCursor(player, inventoryEvent.getCursorTransaction().getFinal());
                }
                if (inventoryEvent instanceof SpawnEntityEvent) {
                    processSpawnedEntities(player, ((IMixinWorldServer) player.getServerWorld()).getCauseTracker(), (SpawnEntityEvent) inventoryEvent);
                } else if (!context.getCapturedEntitySupplier().isEmpty()) {
                    SpawnEntityEvent spawnEntityEvent = SpongeEventFactory.createSpawnEntityEvent(cause, context.getCapturedEntities(), (World) player.getServerWorld());
                    SpongeImpl.postEvent(spawnEntityEvent);
                    if (!spawnEntityEvent.isCancelled()) {
                        processSpawnedEntities(player, ((IMixinWorldServer) player.getServerWorld()).getCauseTracker(), spawnEntityEvent);
                    }
                }
            }
        }
        slotTransactions.clear();
        mixinContainer.setCaptureInventory(false);
    };
    PacketFunction USE_ITEM = ((packet, state, player, context) -> {
        final IMixinWorldServer mixinWorld = (IMixinWorldServer) player.worldObj;
        final World spongeWorld = (World) mixinWorld;

        final ItemStack itemStack = context.firstNamed(InternalNamedCauses.Packet.ITEM_USED, ItemStack.class).orElse(null);
        final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(itemStack);
        context.getCapturedEntitySupplier()
                .ifPresentAndNotEmpty(entities -> {
                    final Cause cause = Cause.source(EntitySpawnCause.builder()
                            .entity(EntityUtil.fromNative(player))
                            .type(itemStack.getItem() == ItemTypes.SPAWN_EGG ? InternalSpawnTypes.SPAWN_EGG : InternalSpawnTypes.PLACEMENT)
                            .build())
                            .named(NamedCause.of(InternalNamedCauses.Packet.ITEM_USED, snapshot))
                            .build();
                    final SpawnEntityEvent spawnEntityEvent = SpongeEventFactory.createSpawnEntityEvent(cause, entities, spongeWorld);
                    SpongeImpl.postEvent(spawnEntityEvent);
                    if (!spawnEntityEvent.isCancelled()) {
                        processSpawnedEntities(player, mixinWorld.getCauseTracker(), spawnEntityEvent);
                    }
                });
        context.getCapturedBlockSupplier()
                .ifPresentAndNotEmpty(
                        originalBlocks -> TrackingUtil.processBlockCaptures(originalBlocks, mixinWorld.getCauseTracker(), state, context));

        context.getCapturedItemsSupplier().ifPresentAndNotEmpty(items -> {
            final Cause spawnCause = Cause.source(
                    EntitySpawnCause.builder()
                            .entity((Entity) player)
                            .type(InternalSpawnTypes.PLACEMENT)
                            .build()
            ).named(NamedCause.notifier(player))
                    .build();
            final List<Entity> entities = items
                    .stream()
                    .map(EntityUtil::fromNative)
                    .collect(Collectors.toList());
            if (!entities.isEmpty()) {
                DropItemEvent.Custom drop = SpongeEventFactory.createDropItemEventCustom(spawnCause, entities, (World) player.getServerWorld());
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
            final Cause spawnCause = Cause.source(
                    EntitySpawnCause.builder()
                            .entity((Entity) player)
                            .type(InternalSpawnTypes.PLACEMENT)
                            .build()
            ).named(NamedCause.notifier(player))
                    .build();
            final List<Entity> entities = items
                    .stream()
                    .map(EntityUtil::fromNative)
                    .collect(Collectors.toList());
            if (!entities.isEmpty()) {
                DropItemEvent.Custom drop = SpongeEventFactory.createDropItemEventCustom(spawnCause, entities, (World) player.getServerWorld());
                SpongeImpl.postEvent(drop);
                if (!drop.isCancelled()) {
                    for (Entity droppedItem : drop.getEntities()) {
                        droppedItem.setCreator(player.getUniqueID());
                        ((IMixinWorldServer) player.getServerWorld()).forceSpawnEntity(droppedItem);
                    }
                }
            }

        });

    });
    PacketFunction PLACE_BLOCK = (packet, state, player, context) -> {
        if (state == PacketPhase.General.INVALID) { // This basically is an out of world place, and nothing should occur here.
            return;
        }
        final IMixinWorldServer mixinWorld = (IMixinWorldServer) player.worldObj;
        final World spongeWorld = (World) mixinWorld;
        final CauseTracker causeTracker = mixinWorld.getCauseTracker();

        // Note - CPacketPlayerTryUseItem is swapped with CPacketPlayerBlockPlacement
        final ItemStack itemStack = context.firstNamed(InternalNamedCauses.Packet.ITEM_USED, ItemStack.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected the used item stack to place a block, but got nothing!", context));
        final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(itemStack);
        context.getCapturedEntitySupplier()
                .ifPresentAndNotEmpty(entities -> {
                    final Cause cause = Cause.source(EntitySpawnCause.builder()
                            .entity(EntityUtil.fromNative(player))
                            .type(InternalSpawnTypes.SPAWN_EGG)
                            .build())
                            .named(NamedCause.of(InternalNamedCauses.Packet.ITEM_USED, snapshot))
                            .build();
                    final SpawnEntityEvent spawnEntityEvent = SpongeEventFactory.createSpawnEntityEvent(cause, entities, spongeWorld);
                    SpongeImpl.postEvent(spawnEntityEvent);
                    if (!spawnEntityEvent.isCancelled()) {
                        processSpawnedEntities(player, mixinWorld.getCauseTracker(), spawnEntityEvent);

                    }
                });
        context.getCapturedBlockSupplier()
                .ifPresentAndNotEmpty(
                        originalBlocks -> {
                            boolean success = TrackingUtil.processBlockCaptures(originalBlocks, mixinWorld.getCauseTracker(), state,
                                    context);
                            if (!success && snapshot != ItemTypeRegistryModule.NONE_SNAPSHOT) {
                                EnumHand hand = ((CPacketPlayerTryUseItemOnBlock) packet).getHand();
                                PacketPhaseUtil.handlePlayerSlotRestore(player, (net.minecraft.item.ItemStack) itemStack, hand);
                            }
                        });
        context.getCapturedItemStackSupplier().ifPresentAndNotEmpty(drops -> {
            final List<EntityItem>
                    items =
                    drops.stream().map(drop -> drop.create(causeTracker.getMinecraftWorld())).collect(Collectors.toList());
            final Cause cause = Cause.source(
                    EntitySpawnCause.builder()
                            .entity((Entity) player)
                            .type(InternalSpawnTypes.PLACEMENT)
                            .build()
            ).named(NamedCause.notifier(player))
                    .build();
            final List<Entity> entities = items
                    .stream()
                    .map(EntityUtil::fromNative)
                    .collect(Collectors.toList());
            if (!entities.isEmpty()) {
                DropItemEvent.Custom event = SpongeEventFactory.createDropItemEventCustom(cause, entities, causeTracker.getWorld());
                SpongeImpl.postEvent(event);
                if (!event.isCancelled()) {
                    for (Entity droppedItem : event.getEntities()) {
                        droppedItem.setCreator(player.getUniqueID());
                        mixinWorld.forceSpawnEntity(droppedItem);
                    }
                }
            }

        });
    };
    PacketFunction HELD_ITEM_CHANGE = ((packet, state, player, context) -> {
        final CPacketHeldItemChange itemChange = (CPacketHeldItemChange) packet;
        final int previousSlot = context.firstNamed(InternalNamedCauses.Packet.PREVIOUS_HIGHLIGHTED_SLOT, Integer.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected a previous highlighted slot, got nothing.", context));
        final Container inventoryContainer = player.inventoryContainer;
        final InventoryPlayer inventory = player.inventory;
        final Slot sourceSlot = inventoryContainer.getSlot(previousSlot + inventory.mainInventory.length);
        final Slot targetSlot = inventoryContainer.getSlot(itemChange.getSlotId() + inventory.mainInventory.length);

        ItemStackSnapshot sourceSnapshot = ItemStackUtil.snapshotOf(sourceSlot.getStack());
        ItemStackSnapshot targetSnapshot = ItemStackUtil.snapshotOf(targetSlot.getStack());
        SlotTransaction sourceTransaction = new SlotTransaction(new SlotAdapter(sourceSlot), sourceSnapshot, sourceSnapshot);
        SlotTransaction targetTransaction = new SlotTransaction(new SlotAdapter(targetSlot), targetSnapshot, targetSnapshot);
        ImmutableList<SlotTransaction>
                transactions =
                new ImmutableList.Builder<SlotTransaction>().add(sourceTransaction).add(targetTransaction).build();
        final ChangeInventoryEvent.Held changeInventoryEventHeld = SpongeEventFactory
                .createChangeInventoryEventHeld(Cause.of(NamedCause.source(player)), (Inventory) inventoryContainer, transactions);
        Container openContainer = player.openContainer;
        SpongeImpl.postEvent(changeInventoryEventHeld);
        if (changeInventoryEventHeld.isCancelled()) {
            player.connection.sendPacket(new SPacketHeldItemChange(previousSlot));
        } else {
            PacketPhaseUtil.handleSlotRestore(player, openContainer, changeInventoryEventHeld.getTransactions(), false, false);
            inventory.currentItem = itemChange.getSlotId();
            player.markPlayerActive();
        }
    });
    PacketFunction CLOSE_WINDOW = ((packet, state, player, context) -> {
        final Container container = context.firstNamed(InternalNamedCauses.Packet.OPEN_CONTAINER, Container.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected the open container object, but had nothing!", context));
        ItemStackSnapshot lastCursor = context.firstNamed(InternalNamedCauses.Packet.CURSOR, ItemStackSnapshot.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected a cursor item stack, but had nothing!", context));
        ItemStackSnapshot newCursor = ItemStackUtil.snapshotOf(player.inventory.getItemStack());
        final Cause cause = Cause.source(player).build();
        InteractInventoryEvent.Close event = SpongeCommonEventFactory.callInteractInventoryCloseEvent(cause, container, player, lastCursor, newCursor, true);
        if (!event.isCancelled()) {
            // Non-merged items
            context.getCapturedItemsSupplier().ifPresentAndNotEmpty(items -> {
                final Cause spawnCause = Cause.source(
                        EntitySpawnCause.builder()
                                .entity((Entity) player)
                                .type(InternalSpawnTypes.PLACEMENT)
                                .build()
                ).named(NamedCause.notifier(player))
                        .build();
                final List<Entity> entities = items
                        .stream()
                        .map(EntityUtil::fromNative)
                        .collect(Collectors.toList());
                if (!entities.isEmpty()) {
                    DropItemEvent.Dispense drop = SpongeEventFactory.createDropItemEventDispense(spawnCause, entities, (World) player.getServerWorld());
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
                final Cause spawnCause = Cause.source(
                        EntitySpawnCause.builder()
                                .entity((Entity) player)
                                .type(InternalSpawnTypes.PLACEMENT)
                                .build()
                ).named(NamedCause.notifier(player))
                        .build();
                final List<Entity> entities = items
                        .stream()
                        .map(EntityUtil::fromNative)
                        .collect(Collectors.toList());
                if (!entities.isEmpty()) {
                    DropItemEvent.Dispense drop = SpongeEventFactory.createDropItemEventDispense(spawnCause, entities, (World) player.getServerWorld());
                    SpongeImpl.postEvent(drop);
                    if (!drop.isCancelled()) {
                        for (Entity droppedItem : drop.getEntities()) {
                            droppedItem.setCreator(player.getUniqueID());
                            ((IMixinWorldServer) player.getServerWorld()).forceSpawnEntity(droppedItem);
                        }
                    }
                }

            });
            context.getCapturedBlockSupplier()
                    .ifPresentAndNotEmpty(blocks ->
                            TrackingUtil.processBlockCaptures(blocks, ((IMixinWorldServer) player.worldObj).getCauseTracker(), state, context));
        }
    });
    PacketFunction ENCHANTMENT = ((packet, state, player, context) -> {
    });
    PacketFunction CLIENT_SETTINGS = ((packet, state, player, context) -> {
        final CPacketClientSettings settings = (CPacketClientSettings) packet;
        PlayerChangeClientSettingsEvent event = SpongeEventFactory.createPlayerChangeClientSettingsEvent(Cause.of(NamedCause.source(player)),
                (ChatVisibility) (Object) settings.getChatVisibility(), SkinUtil.fromFlags(settings.getModelPartFlags()),
                LanguageUtil.LOCALE_CACHE.getUnchecked(settings.getLang()), (Player) player, settings.isColorsEnabled(), settings.view);
        SpongeImpl.postEvent(event);
    });
    PacketFunction CLIENT_STATUS = ((packet, state, player, context) -> {
        if (state == PacketPhase.Inventory.OPEN_INVENTORY) {
            final ItemStackSnapshot lastCursor = context.firstNamed(InternalNamedCauses.Packet.CURSOR, ItemStackSnapshot.class)
                    .orElseThrow(TrackingUtil.throwWithContext("Expected a cursor item stack, but had nothing!", context));
            final ItemStackSnapshot newCursor = ItemStackUtil.snapshotOf(player.inventory.getItemStack());
            final Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(lastCursor, newCursor);
            final InteractInventoryEvent.Open
                    event =
                    SpongeEventFactory.createInteractInventoryEventOpen(Cause.source(player).build(), cursorTransaction,
                            ContainerUtil.fromNative(player.openContainer));
            SpongeImpl.postEvent(event);
            if (event.isCancelled()) {
                player.closeScreen();
            } else {
                // Custom cursor
                if (event.getCursorTransaction().getCustom().isPresent()) {
                    PacketPhaseUtil.handleCustomCursor(player, event.getCursorTransaction().getFinal());
                }
            }
        }
    });
    PacketFunction RESOURCE_PACKET = ((packet, state, player, context) -> {
        final NetHandlerPlayServer connection = player.connection;
        final IMixinNetHandlerPlayServer mixinHandler = (IMixinNetHandlerPlayServer) connection;
        final CPacketResourcePackStatus resource = (CPacketResourcePackStatus) packet;
        //final String hash = resource.hash; // TODO
        final String hash = "";
        final ResourcePackStatusEvent.ResourcePackStatus status;
        final ResourcePack pack = mixinHandler.getSentResourcePacks().get(hash);
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
        if (status.wasSuccessful().isPresent()) {
            mixinHandler.getSentResourcePacks().remove(hash);
        }
        final Cause cause = Cause.of(NamedCause.source(SpongeImpl.getGame()));
        final ResourcePackStatusEvent event = SpongeEventFactory.createResourcePackStatusEvent(cause, pack, (Player) player, status);
        SpongeImpl.postEvent(event);
    });
    PacketFunction MOVEMENT = (packet, state, player, context) -> {
        final IMixinWorldServer mixinWorldServer = (IMixinWorldServer) player.worldObj;
        context.getCapturedBlockSupplier().ifPresentAndNotEmpty(blocks -> TrackingUtil
                .processBlockCaptures(blocks, mixinWorldServer.getCauseTracker(), state, context));
    };

    PacketFunction UNKNOWN_PACKET = (packet, state, player, context) -> {
        final IMixinWorldServer mixinWorldServer = (IMixinWorldServer) player.getServerWorld();
        final CauseTracker causeTracker = mixinWorldServer.getCauseTracker();
        context.getCapturedBlockSupplier().ifPresentAndNotEmpty(blocks -> TrackingUtil.processBlockCaptures(blocks, causeTracker, state, context));
        context.getCapturedEntitySupplier().ifPresentAndNotEmpty(entities -> {
            final Cause cause = Cause.source(EntitySpawnCause.builder()
                    .entity((Player) player)
                    .type(InternalSpawnTypes.PLACEMENT)
                    .build()
            ).build();
            SpawnEntityEvent event = SpongeEventFactory.createSpawnEntityEvent(cause, entities, (World) mixinWorldServer);
            SpongeImpl.postEvent(event);
            if (!event.isCancelled()) {
                for (Entity spawnedEntity : event.getEntities()) {
                    spawnedEntity.setCreator(player.getUniqueID());
                    mixinWorldServer.forceSpawnEntity(spawnedEntity);
                }
            }
        });
        context.getCapturedItemsSupplier().ifPresentAndNotEmpty(entities -> {
            final List<Entity> items = entities.stream().map(EntityUtil::fromNative).collect(Collectors.toList());
            final Cause cause = Cause.source(EntitySpawnCause.builder()
                    .entity((Player) player)
                    .type(InternalSpawnTypes.PLACEMENT)
                    .build()
            ).build();
            SpawnEntityEvent event = SpongeEventFactory.createSpawnEntityEvent(cause, items, (World) mixinWorldServer);
            SpongeImpl.postEvent(event);
            if (!event.isCancelled()) {
                for (Entity spawnedEntity : event.getEntities()) {
                    spawnedEntity.setCreator(player.getUniqueID());
                    mixinWorldServer.forceSpawnEntity(spawnedEntity);
                }
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
                final net.minecraft.entity.Entity entityFromUuid = causeTracker.getMinecraftWorld().getEntityFromUuid(entityUuid);
                final Entity affectedEntity = EntityUtil.fromNative(entityFromUuid);
                if (entityFromUuid != null) {
                    final Cause cause = Cause.source(
                            EntitySpawnCause.builder()
                                    .entity(affectedEntity)
                                    .type(InternalSpawnTypes.CUSTOM)
                                    .build()
                    ).named(NamedCause.notifier(player))
                            .build();
                    final List<Entity> entities = entry.getValue()
                            .stream()
                            .map(EntityUtil::fromNative)
                            .collect(Collectors.toList());
                    if (!entities.isEmpty()) {
                        DropItemEvent.Custom event = SpongeEventFactory.createDropItemEventCustom(cause, entities, causeTracker.getWorld());
                        SpongeImpl.postEvent(event);
                        if (!event.isCancelled()) {
                            for (Entity droppedItem : event.getEntities()) {
                                droppedItem.setCreator(player.getUniqueID());
                                mixinWorldServer.forceSpawnEntity(droppedItem);
                            }
                        }
                    }
                }
            }
        });
        context.getCapturedItemStackSupplier().ifPresentAndNotEmpty(drops -> {
            final List<EntityItem>
                    items =
                    drops.stream().map(drop -> drop.create(causeTracker.getMinecraftWorld())).collect(Collectors.toList());
            final Cause cause = Cause.source(
                    EntitySpawnCause.builder()
                            .entity((Entity) player)
                            .type(InternalSpawnTypes.CUSTOM)
                            .build()
            ).named(NamedCause.notifier(player))
                    .build();
            final List<Entity> entities = items
                    .stream()
                    .map(EntityUtil::fromNative)
                    .collect(Collectors.toList());
            if (!entities.isEmpty()) {
                DropItemEvent.Custom event = SpongeEventFactory.createDropItemEventCustom(cause, entities, causeTracker.getWorld());
                SpongeImpl.postEvent(event);
                if (!event.isCancelled()) {
                    for (Entity droppedItem : event.getEntities()) {
                        droppedItem.setCreator(player.getUniqueID());
                        mixinWorldServer.forceSpawnEntity(droppedItem);
                    }
                }
            }

        });

    };

    PacketFunction HANDLED_EXTERNALLY = UNKNOWN_PACKET;

    void unwind(Packet<?> packet, IPacketState state, EntityPlayerMP player, PhaseContext context);

}