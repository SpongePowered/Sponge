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
package org.spongepowered.common.event.tracking.phase.function;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketClientSettings;
import net.minecraft.network.play.client.CPacketCreativeInventoryAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerBlockPlacement;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketResourcePackStatus;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.SPacketHeldItemChange;
import net.minecraft.network.play.server.SPacketOpenWindow;
import net.minecraft.world.IInteractionObject;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.PlayerChangeClientSettingsEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.ResourcePackStatusEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.CreativeInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.chat.ChatVisibility;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.EventConsumer;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.PacketPhase;
import org.spongepowered.common.event.tracking.phase.util.PacketPhaseUtil;
import org.spongepowered.common.event.tracking.phase.util.PhaseUtil;
import org.spongepowered.common.interfaces.IMixinContainer;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.network.IMixinNetHandlerPlayServer;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.item.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.item.inventory.util.ContainerUtil;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;
import org.spongepowered.common.util.LanguageUtil;
import org.spongepowered.common.util.SkinUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@FunctionalInterface
public interface PacketFunction {

    PacketFunction IGNORED = (packet, state, player, context) -> {};
    PacketFunction HANDLED_EXTERNALLY = IGNORED;

    PacketFunction USE_ENTITY = (packet, state, player, context) -> {
        final CPacketUseEntity useEntityPacket = (CPacketUseEntity) packet;
        final net.minecraft.entity.Entity entity = useEntityPacket.getEntityFromWorld(player.worldObj);
        final Optional<ItemStack> itemStack = context.firstNamed(InternalNamedCauses.Packet.ITEM_USED, ItemStack.class);

        if (state == PacketPhase.General.ATTACK_ENTITY) {

            if (entity.isDead) {
                // We only have to throw the destruction event now. everything else should be already handled separately as they are processed.
                MessageChannel originalChannel = MessageChannel.TO_NONE;
                final DestructEntityEvent destructEntityEvent;
                final Cause.Builder builder = Cause.source(player);
                itemStack.map(ItemStack::createSnapshot).ifPresent(item -> builder.named(InternalNamedCauses.EventNamedKeys.ITEM_USED, item));
                if (!(entity instanceof EntityLivingBase)) {
                    destructEntityEvent = SpongeEventFactory.createDestructEntityEvent(builder.build(), originalChannel, Optional.of(originalChannel), new MessageEvent.MessageFormatter(), EntityUtil.fromNative(entity), true);
                } else {
                    destructEntityEvent = SpongeEventFactory.createDestructEntityEventDeath(builder.build(), originalChannel, Optional.of(originalChannel), new MessageEvent.MessageFormatter(), EntityUtil.fromNativeToLiving(entity), true);
                }

                EventConsumer.event(destructEntityEvent)
                        .nonCancelled(event -> {
                            if (!event.isMessageCancelled()) {
                                event.getChannel().ifPresent(channel -> channel.send(entity, event.getMessage()));
                            }
                        })
                        .process();
                context.getCapturedItemsSupplier().get().ifPresentAndNotEmpty(items -> {
                    // For destruction, this should be empty, however, some times, it may not be?
                    final PrettyPrinter printer = new PrettyPrinter(60);
                    printer.add("Processing Attack Entity").centre().hr();
                    printer.add("There are some captured items after the entity was destructed!");
                    printer.addWrapped(60, "%s : %s", "Items captured", items);
                    printer.add("Stacktrace:");
                    printer.add(new Exception("Stack trace"));
                    printer.trace(System.err, SpongeImpl.getLogger(), Level.TRACE);
                });
                context.getCapturedBlockSupplier().get().ifPresentAndNotEmpty(blocks -> {
                    GeneralFunctions.processBlockCaptures(blocks, ((IMixinWorldServer) player.worldObj).getCauseTracker(), state, context);
                });
                context.getCapturedEntityDrops().ifPresent(map -> {
                    if (map.isEmpty()) {
                        return;
                    }
                    final PrettyPrinter printer = new PrettyPrinter(80);
                    printer.add("Processing Attack Entity").centre().hr();
                    printer.add("The item stacks captured are: ");
                    map.asMap().entrySet().forEach(entry -> {
                        printer.add("  - Entity with UUID: %s", entry.getKey());
                        for (ItemStack stack : entry.getValue()) {
                            printer.add("    - %s", stack);
                        }
                    });
                    printer.trace(System.err);
                });


            }
        } else if (state == PacketPhase.General.INTERACT_ENTITY) {
            context.getCapturedBlockSupplier().get().ifPresentAndNotEmpty(blocks -> {
                 final PrettyPrinter printer = new PrettyPrinter(80);
                 printer.add("Processing Interact Entity").centre().hr();
                 printer.add("The blocks captured are:");
                 for (BlockSnapshot blockSnapshot : blocks) {
                     printer.add("  Block: %s", blockSnapshot);
                 }
                 printer.trace(System.err);
            });
            context.getCapturedEntitySupplier().get().ifPresentAndNotEmpty(entities -> {
                final PrettyPrinter printer = new PrettyPrinter(80);
                printer.add("Processing Interact Entity").centre().hr();
                printer.add("The entities captured are:");
                for (Entity capturedEntity: entities) {
                    printer.add("  Entity: %s", capturedEntity);
                }
                printer.trace(System.err);
            });
            context.getCapturedItemsSupplier().get().ifPresentAndNotEmpty(entities -> {
                final PrettyPrinter printer = new PrettyPrinter(80);
                printer.add("Processing Interact Entity").centre().hr();
                printer.add("The items captured are:");
                for (Entity capturedEntity: entities) {
                    printer.add("  Item: %s", capturedEntity);
                }
                printer.trace(System.err);
            });
            context.getCapturedEntityDropSupplier().get().ifPresentAndNotEmpty(map -> {
                final PrettyPrinter printer = new PrettyPrinter(80);
                printer.add("Processing Interact Entity").centre().hr();
                printer.add("The item stacks captured are: ");

                map.asMap().entrySet().forEach(entry -> {
                    printer.add("  - Entity with UUID: %s", entry.getKey());
                    for (ItemStack stack : entry.getValue()) {
                        printer.add("    - %s", stack);
                    }
                });
                printer.trace(System.err);
            });

        } else if (state == PacketPhase.General.INTERACT_AT_ENTITY) {
            context.getCapturedBlockSupplier().get().ifPresentAndNotEmpty(blocks -> {
                final PrettyPrinter printer = new PrettyPrinter(80);
                printer.add("Processing Interact At Entity").centre().hr();
                printer.add("The blocks captured are:");
                for (BlockSnapshot blockSnapshot : blocks) {
                    printer.add("  Block: %s", blockSnapshot);
                }
                printer.trace(System.err);
            });
            context.getCapturedEntitySupplier().get().ifPresentAndNotEmpty(entities -> {
                final PrettyPrinter printer = new PrettyPrinter(80);
                printer.add("Processing Interact At Entity").centre().hr();
                printer.add("The entities captured are:");
                for (Entity capturedEntity: entities) {
                    printer.add("  Entity: %s", capturedEntity);
                }
                printer.trace(System.err);
            });
            context.getCapturedItemsSupplier().get().ifPresentAndNotEmpty(entities -> {
                final PrettyPrinter printer = new PrettyPrinter(80);
                printer.add("Processing Interact At Entity").centre().hr();
                printer.add("The items captured are:");
                for (Entity capturedEntity: entities) {
                    printer.add("  Item: %s", capturedEntity);
                }
                printer.trace(System.err);
            });
            context.getCapturedEntityDropSupplier().get().ifPresentAndNotEmpty(map -> {
                final PrettyPrinter printer = new PrettyPrinter(80);
                printer.add("Processing Interact At Entity").centre().hr();
                printer.add("The item stacks captured are: ");

                map.asMap().entrySet().forEach(entry -> {
                    printer.add("  - Entity with UUID: %s", entry.getKey());
                    for (ItemStack stack : entry.getValue()) {
                        printer.add("    - %s", stack);
                    }
                });
                printer.trace(System.err);
            });
        }
        context.getCapturedBlockSupplier().get()
                .ifPresentAndNotEmpty(snapshots ->
                GeneralFunctions.processBlockCaptures(snapshots, ((IMixinWorldServer) player.worldObj).getCauseTracker(), state, context));
    };

    PacketFunction ACTION = (packet, state, player, context) -> {
        final CauseTracker causeTracker = ((IMixinWorldServer) player.worldObj).getCauseTracker();
        if (state == PacketPhase.Inventory.DROP_ITEM) {
            context.getCapturedBlockSupplier().get().ifPresentAndNotEmpty(blocks ->
                    GeneralFunctions.processBlockCaptures(blocks, causeTracker, state, context));
        } else if (state == PacketPhase.Inventory.DROP_INVENTORY) {
            context.getCapturedBlockSupplier().get().ifPresentAndNotEmpty(blocks -> {
                GeneralFunctions.processBlockCaptures(blocks, causeTracker, state, context);
            });
            context.getCapturedItemsSupplier().get().ifPresentAndNotEmpty(items -> {
                final List<EntitySnapshot> snapshots = items.stream().map(Entity::createSnapshot).collect(Collectors.toList());
                final Cause cause = Cause.source(EntitySpawnCause.builder()
                        .entity(EntityUtil.fromNative(player))
                        .type(InternalSpawnTypes.DROPPED_ITEM)
                        .build())
                        .build();
                EventConsumer.event(SpongeEventFactory.createDropItemEventDispense(cause, items, snapshots, causeTracker.getWorld()))
                        .nonCancelled(event ->
                                event.getEntities().forEach(entity -> {
                                    EntityUtil.toMixin(entity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, player.getUniqueID());
                                    causeTracker.getMixinWorld().forceSpawnEntity(entity);
                                })
                        )
                        .process();
            });

        } else if (state == PacketPhase.General.INTERACTION) {
            System.err.printf("You remind me of the babe... %n");
            context.getCapturedBlockSupplier().get().ifPresentAndNotEmpty(blocks -> {
                System.err.printf("What babe? %n");
                GeneralFunctions.processBlockCaptures(blocks, causeTracker, state, context);

                    }
            );
            context.getCapturedItemsSupplier().get().ifPresentAndNotEmpty(items -> {
                if (items.isEmpty()) {
                    return;
                }
                final List<EntitySnapshot> snapshots = items.stream().map(Entity::createSnapshot).collect(Collectors.toList());
                final Cause cause = Cause.source(EntitySpawnCause.builder()
                                .entity(EntityUtil.fromNative(player))
                                .type(InternalSpawnTypes.DROPPED_ITEM)
                                .build())
                        .build();
                EventConsumer.event(SpongeEventFactory.createDropItemEventDispense(cause, items, snapshots, causeTracker.getWorld()))
                        .nonCancelled(event ->
                                event.getEntities().forEach(entity -> {
                                    EntityUtil.toMixin(entity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, player.getUniqueID());
                                    causeTracker.getMixinWorld().forceSpawnEntity(entity);
                                })
                        )
                        .process();
            });
            context.getCapturedEntityDrops().ifPresent(map -> {
                if (map.isEmpty()) {
                    return;
                }
                final PrettyPrinter printer = new PrettyPrinter(80);
                printer.add("Processing Interaction").centre().hr();
                printer.add("The item stacks captured are: ");
                map.asMap().entrySet().forEach(entry -> {
                    printer.add("  - Entity with UUID: %s", entry.getKey());
                    for (ItemStack stack : entry.getValue()) {
                        printer.add("    - %s", stack);
                    }
                });
                printer.trace(System.err);
            });

        }
    };

    PacketFunction CREATIVE = (packet, state, player, context) -> {
        ((IMixinContainer) player.inventoryContainer).setCaptureInventory(false);
        boolean ignoringCreative = context.firstNamed(InternalNamedCauses.Packet.IGNORING_CREATIVE, Boolean.class).orElse(false);
        if (!ignoringCreative) {
            final CPacketCreativeInventoryAction packetIn = ((CPacketCreativeInventoryAction) packet);
            final ItemStackSnapshot lastCursor = context.firstNamed(InternalNamedCauses.Packet.CURSOR, ItemStackSnapshot.class).get();
            final ItemStackSnapshot newCursor = ItemStackUtil.snapshotOf(player.inventory.getItemStack());
            final Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(lastCursor, newCursor);
            final List<Entity> capturedEntityItems = context.getCapturedItems().orElse(new ArrayList<>());
            final Cause cause;
            final Container openContainer = player.openContainer;
            final List<SlotTransaction> capturedTransactions = ((IMixinContainer) openContainer).getCapturedTransactions();
            final CreativeInventoryEvent event;
            if (packetIn.getSlotId() == -1 && capturedEntityItems.size() > 0) {
                Iterator<Entity> iterator = capturedEntityItems.iterator();
                ImmutableList.Builder<EntitySnapshot> entitySnapshotBuilder = new ImmutableList.Builder<>();
                while (iterator.hasNext()) {
                    Entity currentEntity = iterator.next();
                    ((IMixinEntity) currentEntity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, player.getUniqueID());
                    entitySnapshotBuilder.add(currentEntity.createSnapshot());
                }
                cause = Cause.source(EntitySpawnCause.builder().entity(EntityUtil.fromNative(player)).type(InternalSpawnTypes.DROPPED_ITEM).build()).build();
                event = SpongeEventFactory.createCreativeInventoryEventDrop(cause, cursorTransaction, capturedEntityItems,
                        entitySnapshotBuilder.build(), (org.spongepowered.api.item.inventory.Container) openContainer, (World) player.worldObj,
                        capturedTransactions);
            } else {
                cause = Cause.of(NamedCause.source(player), NamedCause.of("Container", ""));
                PacketPhaseUtil.validateCapturedTransactions(packetIn.getSlotId(), openContainer, capturedTransactions);
                event = SpongeEventFactory.createCreativeInventoryEventClick(cause, cursorTransaction, ContainerUtil.fromNative(openContainer), capturedTransactions);
            }
            EventConsumer.event(event)
                    .cancelled(creativeInventoryEvent -> {
                        if (creativeInventoryEvent instanceof CreativeInventoryEvent.Drop) {
                            capturedEntityItems.clear();
                        }

                        // Restore cursor
                        PacketPhaseUtil.handleCustomCursor(player, creativeInventoryEvent.getCursorTransaction().getOriginal());

                        // Restore target slots
                        PacketPhaseUtil.handleSlotRestore(player, creativeInventoryEvent.getTransactions());
                    })
                    .nonCancelled(creativeInventoryEvent -> {
                        PacketPhaseUtil.handleCustomSlot(player, creativeInventoryEvent.getTransactions());

                        // Custom cursor
                        if (event.getCursorTransaction().getCustom().isPresent()) {
                            PacketPhaseUtil.handleCustomCursor(player, creativeInventoryEvent.getCursorTransaction().getFinal());
                        }
                        if (creativeInventoryEvent instanceof CreativeInventoryEvent.Drop) {
                            ((CreativeInventoryEvent.Drop) creativeInventoryEvent).getEntities()
                                    .forEach(entity ->{
                                        if (entity != null) {
                                            TrackingUtil.associateEntityCreator(context, EntityUtil.toNative(entity), player.worldObj);
                                            ((IMixinWorldServer) player.worldObj).forceSpawnEntity(entity);
                                        }
                                    });
                        }
                    })
                    .process();
            capturedTransactions.clear();
        }

    };

    PacketFunction INVENTORY = (packet, state, player, context) -> {
        ((IMixinContainer) player.openContainer).setCaptureInventory(false);
        final CPacketClickWindow packetIn = context.firstNamed(InternalNamedCauses.Packet.CAPTURED_PACKET, CPacketClickWindow.class).get();
        final ItemStackSnapshot lastCursor = context.firstNamed(InternalNamedCauses.Packet.CURSOR, ItemStackSnapshot.class).get();
        final ItemStackSnapshot newCursor = ItemStackUtil.snapshotOf(player.inventory.getItemStack());
        final Transaction<ItemStackSnapshot> transaction = new Transaction<>(lastCursor, newCursor);

        final Container openContainer = player.openContainer;
        final List<SlotTransaction> slotTransactions = ContainerUtil.toMixin(openContainer).getCapturedTransactions();
        PacketPhaseUtil.validateCapturedTransactions(packetIn.getSlotId(), openContainer, slotTransactions);
        final int usedButton = packetIn.getUsedButton();
        final List<Entity> capturedItems = context.getCapturedItems().get();
        final Cause cause = Cause.of(NamedCause.source(player), NamedCause.of("Container", openContainer));
        final InteractInventoryEvent inventoryEvent;
        if (state instanceof PacketPhase.Inventory) {
            inventoryEvent = ((PacketPhase.Inventory) state).createInventoryEvent(player, ContainerUtil.fromNative(openContainer), transaction, slotTransactions, capturedItems, cause, usedButton);
        } else {
            inventoryEvent = null;
        }

        if (inventoryEvent != null) {
            EventConsumer.event(inventoryEvent)
                .cancelled(event -> {
                    if (event instanceof ClickInventoryEvent.Drop) {
                        capturedItems.clear();
                    }

                    if (event instanceof ClickInventoryEvent) {
                        // Restore cursor
                        PacketPhaseUtil.handleCustomCursor(player, event.getCursorTransaction().getOriginal());
                    }
                    if (event instanceof ClickInventoryEvent.Double) {
                        ((ClickInventoryEvent.Double) inventoryEvent).getTransactions().clear();
                        return;
                    }

                    if (event instanceof ChangeInventoryEvent) {
                        // Restore target slots
                        PacketPhaseUtil.handleSlotRestore(player, ((ChangeInventoryEvent) event).getTransactions());
                    }
                })
                .nonCancelled(event -> {
                    if (inventoryEvent instanceof ChangeInventoryEvent) {
                        PacketPhaseUtil.handleCustomSlot(player, ((ChangeInventoryEvent) inventoryEvent).getTransactions());
                    }

                    // Custom cursor
                    if (inventoryEvent instanceof ClickInventoryEvent) {
                        final ClickInventoryEvent clickInventory = (ClickInventoryEvent) inventoryEvent;
                        if (clickInventory.getCursorTransaction().getCustom().isPresent()) {
                            PacketPhaseUtil.handleCustomCursor(player, clickInventory.getCursorTransaction().getFinal());
                        }
                    }
                }).post(event -> slotTransactions.clear())
                .process();
        }
    };
    PacketFunction USE_ITEM = ((packet, state, player, context) -> {
        final CPacketPlayerBlockPlacement useItemPacket = (CPacketPlayerBlockPlacement) packet;
        final IMixinWorldServer mixinWorld = (IMixinWorldServer) player.worldObj;
        final World spongeWorld = (World) mixinWorld;

        final ItemStack itemStack = context.firstNamed(InternalNamedCauses.Packet.ITEM_USED, ItemStack.class).orElse(null);
        final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(itemStack);
        context.getCapturedEntitySupplier().get().ifPresentAndNotEmpty(entities -> {
            final Cause cause = Cause.source(EntitySpawnCause.builder()
                    .entity(EntityUtil.fromNative(player))
                    .type(InternalSpawnTypes.SPAWN_EGG)
                    .build())
                    .named(NamedCause.of(InternalNamedCauses.Packet.ITEM_USED, snapshot))
                    .build();
            final List<EntitySnapshot> snapshots = entities.stream().map(Entity::createSnapshot).collect(Collectors.toList());
            EventConsumer.event(SpongeEventFactory.createSpawnEntityEvent(cause, entities, snapshots, spongeWorld))
                    .nonCancelled(event -> EntityListConsumer.FORCE_SPAWN.apply(event.getEntities(), mixinWorld.getCauseTracker()))
                    .process();
        });
        context.getCapturedBlockSupplier().get().ifPresentAndNotEmpty(originalBlocks -> {
            GeneralFunctions.processBlockCaptures(originalBlocks, mixinWorld.getCauseTracker(), state, context);
        });

    });
    PacketFunction PLACE_BLOCK = (packet, state, player, context) -> {
        final CPacketPlayerTryUseItem placePacket = (CPacketPlayerTryUseItem) packet;
        final IMixinWorldServer mixinWorld = (IMixinWorldServer) player.worldObj;
        final World spongeWorld = (World) mixinWorld;
        // Note - CPacketPlayerTryUseItem is swapped with CPacketPlayerBlockPlacement
        final ItemStack itemStack = context.firstNamed(InternalNamedCauses.Packet.ITEM_USED, ItemStack.class)
                .orElseThrow(PhaseUtil.throwWithContext("Expected the used item stack to place a block, but got nothing!", context));
        final ItemStackSnapshot snapshot = itemStack.createSnapshot();
        context.getCapturedEntitySupplier().get().ifPresentAndNotEmpty(entities -> {
            final Cause cause = Cause.source(EntitySpawnCause.builder()
                    .entity(EntityUtil.fromNative(player))
                    .type(InternalSpawnTypes.SPAWN_EGG)
                    .build())
                    .named(NamedCause.of(InternalNamedCauses.Packet.ITEM_USED, snapshot))
                    .build();
            final List<EntitySnapshot> snapshots = entities.stream().map(Entity::createSnapshot).collect(Collectors.toList());
            EventConsumer.event(SpongeEventFactory.createSpawnEntityEvent(cause, entities, snapshots, spongeWorld))
                    .nonCancelled(event -> EntityListConsumer.FORCE_SPAWN.apply(event.getEntities(), mixinWorld.getCauseTracker()))
                    .process();
        });
        context.getCapturedBlockSupplier().get().ifPresentAndNotEmpty(originalBlocks -> {
            GeneralFunctions.processBlockCaptures(originalBlocks, mixinWorld.getCauseTracker(), state, context);
        });
    };
    PacketFunction HELD_ITEM_CHANGE = ((packet, state, player, context) -> {
        final CPacketHeldItemChange itemChange = (CPacketHeldItemChange) packet;
        final int previousSlot = context.firstNamed(InternalNamedCauses.Packet.PREVIOUS_HIGHLIGHTED_SLOT, Integer.class)
                .orElseThrow(PhaseUtil.throwWithContext("Expected a previous highlighted slot, got nothing.", context));
        final Container inventoryContainer = player.inventoryContainer;
        final InventoryPlayer inventory = player.inventory;
        final Slot sourceSlot = inventoryContainer.getSlot(previousSlot + inventory.mainInventory.length);
        final Slot targetSlot = inventoryContainer.getSlot(itemChange.getSlotId() + inventory.mainInventory.length);
        if (sourceSlot == null || targetSlot == null) {
            return; // should never happen but just in case it does
        }

        ItemStackSnapshot sourceSnapshot = ItemStackUtil.snapshotOf(sourceSlot.getStack());
        ItemStackSnapshot targetSnapshot = targetSlot.getStack() != null
                                           ? ((org.spongepowered.api.item.inventory.ItemStack) targetSlot.getStack()).createSnapshot() : ItemStackSnapshot.NONE;
        SlotTransaction sourceTransaction = new SlotTransaction(new SlotAdapter(sourceSlot), sourceSnapshot, sourceSnapshot);
        SlotTransaction targetTransaction = new SlotTransaction(new SlotAdapter(targetSlot), targetSnapshot, targetSnapshot);
        ImmutableList<SlotTransaction> transactions = new ImmutableList.Builder<SlotTransaction>().add(sourceTransaction).add(targetTransaction).build();
        EventConsumer.event(SpongeEventFactory.createChangeInventoryEventHeld(Cause.of(NamedCause.source(player)), (Inventory) inventoryContainer, transactions))
            .cancelled(event -> player.playerNetServerHandler.sendPacket(new SPacketHeldItemChange(previousSlot)))
            .nonCancelled(event -> {
                PacketPhaseUtil.handleCustomSlot(player, event.getTransactions());
                inventory.currentItem = itemChange.getSlotId();
                player.markPlayerActive();
            })
            .process();
    });
    PacketFunction CLOSE_WINDOW = ((packet, state, player, context) -> {
        final Container container = context.firstNamed(InternalNamedCauses.Packet.OPEN_CONTAINER, Container.class)
                .orElseThrow(PhaseUtil.throwWithContext("Expected the open container object, but had nothing!", context));
        ItemStackSnapshot lastCursor = context.firstNamed(InternalNamedCauses.Packet.CURSOR, ItemStackSnapshot.class)
                .orElseThrow(PhaseUtil.throwWithContext("Expected a cursor item stack, but had nothing!", context));
        ItemStackSnapshot newCursor = ItemStackUtil.snapshotOf(player.inventory.getItemStack());
        Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(lastCursor, newCursor);
        final Cause cause = Cause.source(player).build();
        EventConsumer.event(SpongeEventFactory.createInteractInventoryEventClose(cause, cursorTransaction, ContainerUtil.fromNative(container)))
            .cancelled(event -> {
                if (container.getSlot(0) != null) {
                    player.openContainer = container;
                    final Slot slot = container.getSlot(0);
                    final String guiId;
                    final IInventory slotInventory = slot.inventory;
                    if (slotInventory instanceof IInteractionObject) {
                        guiId = ((IInteractionObject) slotInventory).getGuiID();
                    } else {
                        guiId = "unknown";
                    }
                    slotInventory.openInventory(player);
                    player.playerNetServerHandler.sendPacket(new SPacketOpenWindow(container.windowId, guiId, slotInventory
                        .getDisplayName(), slotInventory.getSizeInventory()));
                    // resync data to client
                    player.sendContainerToPlayer(container);
                }
            })
            .nonCancelled(event -> {
                // Custom cursor
                if (event.getCursorTransaction().getCustom().isPresent()) {
                    PacketPhaseUtil.handleCustomCursor(player, event.getCursorTransaction().getFinal());
                }
            })
            .process();
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
                    .orElseThrow(PhaseUtil.throwWithContext("Expected a cursor item stack, but had nothing!", context));
            final ItemStackSnapshot newCursor = ItemStackUtil.snapshotOf(player.inventory.getItemStack());
            final Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(lastCursor, newCursor);
            EventConsumer.event(SpongeEventFactory.createInteractInventoryEventOpen(Cause.source(player).build(), cursorTransaction, ContainerUtil.fromNative(player.openContainer)))
                .cancelled(event -> player.closeScreen())
                .nonCancelled(event -> {
                    // Custom cursor
                    if (event.getCursorTransaction().getCustom().isPresent()) {
                        PacketPhaseUtil.handleCustomCursor(player, event.getCursorTransaction().getFinal());
                    }
                })
                .process();
        }
    });
    PacketFunction RESOURCE_PACKET = ((packet, state, player, context) -> {
        final NetHandlerPlayServer playerNetServerHandler = player.playerNetServerHandler;
        final IMixinNetHandlerPlayServer mixinHandler = (IMixinNetHandlerPlayServer) playerNetServerHandler;
        final CPacketResourcePackStatus resource = (CPacketResourcePackStatus) packet;
        final String hash = resource.hash;
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

    void unwind(Packet<?> packet, PacketPhase.IPacketState state, EntityPlayerMP player, PhaseContext context);

}
