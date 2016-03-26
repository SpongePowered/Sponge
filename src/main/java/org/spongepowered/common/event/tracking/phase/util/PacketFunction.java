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
package org.spongepowered.common.event.tracking.phase.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.client.C10PacketCreativeInventoryAction;
import net.minecraft.network.play.client.C15PacketClientSettings;
import net.minecraft.network.play.client.C19PacketResourcePackStatus;
import net.minecraft.network.play.server.S09PacketHeldItemChange;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.world.IInteractionObject;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.living.humanoid.player.PlayerChangeClientSettingsEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.ResourcePackStatusEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.CreativeInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.chat.ChatVisibility;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.event.EventConsumer;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.PacketPhase;
import org.spongepowered.common.interfaces.IMixinContainer;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.network.IMixinNetHandlerPlayServer;
import org.spongepowered.common.item.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.item.inventory.util.ContainerUtil;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.util.LanguageUtil;
import org.spongepowered.common.util.SkinUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@FunctionalInterface
public interface PacketFunction {

    PacketFunction IGNORED = (packet, state, player, context) -> {};
    PacketFunction HANDLED_EXTERNALLY = IGNORED;

    PacketFunction USE_ENTITY = (packet, state, player, context) -> {
        if (state == PacketPhase.General.ATTACK_ENTITY) {
            final C02PacketUseEntity useEntityPacket = (C02PacketUseEntity) packet;
            net.minecraft.entity.Entity entity = useEntityPacket.getEntityFromWorld(player.worldObj);

            if (entity != null && entity.isDead && !(entity instanceof EntityLivingBase)) {
                MessageChannel originalChannel = MessageChannel.TO_NONE;

                EventConsumer.supplyEvent(() -> SpongeEventFactory.createDestructEntityEvent(Cause.source(player).build(), originalChannel, Optional.of(originalChannel), new MessageEvent.MessageFormatter(), (Entity) entity, true))
                    .nonCancelled(event -> {
                        if (!event.isMessageCancelled()) {
                            event.getChannel().ifPresent(channel -> channel.send(entity, event.getMessage()));
                        }
                    })
                    .buildAndPost();
            }
        } else if (state == PacketPhase.General.INTERACT_ENTITY) {

        } else if (state == PacketPhase.General.INTERACT_AT_ENTITY) {

        }
    };

    PacketFunction ACTION = (packet, state, player, context) -> {
        if (state == PacketPhase.Inventory.DROP_ITEM) {

        } else if (state == PacketPhase.Inventory.DROP_INVENTORY) {

        } else if (state == PacketPhase.General.INTERACTION) {

        }
    };

    PacketFunction CREATIVE = (packet, state, player, context) -> {
        if (state == PacketPhase.General.CREATIVE_INVENTORY) {
            boolean ignoringCreative = context.firstNamed(TrackingUtil.IGNORING_CREATIVE, Boolean.class).orElse(false);
            if (!ignoringCreative) {
                final C10PacketCreativeInventoryAction packetIn = ((C10PacketCreativeInventoryAction) packet);
                final ItemStackSnapshot lastCursor = context.firstNamed(TrackingUtil.CURSOR, ItemStackSnapshot.class).get();
                final ItemStackSnapshot newCursor = ItemStackUtil.snapshotOf(player.inventory.getItemStack());
                final Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(lastCursor, newCursor);
                final List<Entity> capturedEntityItems = context.getCapturedItems().orElse(new ArrayList<>());
                final Cause cause = Cause.of(NamedCause.source(player), NamedCause.of("Container", ""));
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
                    event = SpongeEventFactory.createCreativeInventoryEventDrop(cause, cursorTransaction, capturedEntityItems,
                            entitySnapshotBuilder.build(), (org.spongepowered.api.item.inventory.Container) openContainer, (World) player.worldObj,
                            capturedTransactions);
                } else {
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
                    })
                    .buildAndPost();
                capturedTransactions.clear();
            }
        }
    };

    PacketFunction INVENTORY = (packet, state, player, context) -> {
        final C0EPacketClickWindow packetIn = context.firstNamed(TrackingUtil.CAPTURED_PACKET, C0EPacketClickWindow.class).get();
        final ItemStackSnapshot lastCursor = context.firstNamed(TrackingUtil.CURSOR, ItemStackSnapshot.class).get();
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
                .buildAndPost();
        }
    };
    PacketFunction USE_ITEM = ((packet, state, player, context) -> {
        final C08PacketPlayerBlockPlacement placePacket = (C08PacketPlayerBlockPlacement) packet;
//        context.getCapturedEntitySupplier().get().ifPresent(capturedEntities -> {
//            final Cause cause = Cause.source(EntitySpawnCause.builder().entity((Player) player).type(InternalSpawnTypes.SPAWN_EGG).build()).build();
//            final SpawnEntityEvent spawnEntityEvent = state.createEntityEvent(cause, ((IMixinWorldServer) player.worldObj).getCauseTracker(), context);
//            if (spawnEntityEvent != null && SpongeImpl.postEvent(spawnEntityEvent)) {
//                final List<Entity> entities = spawnEntityEvent.getEntities();
//            }
//        });

    });
    PacketFunction HELD_ITEM_CHANGE = ((packet, state, player, context) -> {
        final C09PacketHeldItemChange itemChange = (C09PacketHeldItemChange) packet;
        final int previousSlot = context.firstNamed(TrackingUtil.PREVIOUS_HIGHLIGHTED_SLOT, Integer.class).get();
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
        EventConsumer.supplyEvent(() -> SpongeEventFactory.createChangeInventoryEventHeld(Cause.of(NamedCause.source(player)), (Inventory) inventoryContainer, transactions))
            .cancelled(event -> player.playerNetServerHandler.sendPacket(new S09PacketHeldItemChange(previousSlot)))
            .nonCancelled(event -> {
                PacketPhaseUtil.handleCustomSlot(player, event.getTransactions());
                inventory.currentItem = itemChange.getSlotId();
                player.markPlayerActive();
            })
            .buildAndPost();
    });
    PacketFunction CLOSE_WINDOW = ((packet, state, player, context) -> {
        final Container container = context.firstNamed(TrackingUtil.OPEN_CONTAINER, Container.class).get();
        ItemStackSnapshot lastCursor = context.firstNamed(TrackingUtil.CURSOR, ItemStackSnapshot.class).get();
        ItemStackSnapshot newCursor = ItemStackUtil.snapshotOf(player.inventory.getItemStack());
        Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(lastCursor, newCursor);
        final Cause cause = Cause.source(player).build();
        EventConsumer.supplyEvent(() -> SpongeEventFactory.createInteractInventoryEventClose(cause, cursorTransaction, ContainerUtil.fromNative(container)))
            .cancelled(event -> {
                if (container.getSlot(0) != null) {
                    player.openContainer = container;
                    final Slot slot = container.getSlot(0);
                    String guiId = "unknown";
                    final IInventory slotInventory = slot.inventory;
                    if (slotInventory instanceof IInteractionObject) {
                        guiId = ((IInteractionObject) slotInventory).getGuiID();
                    }
                    slotInventory.openInventory(player);
                    player.playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(container.windowId, guiId, slotInventory
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
            .buildAndPost();
    });
    PacketFunction ENCHANTMENT = ((packet, state, player, context) -> {
    });
    PacketFunction CLIENT_SETTINGS = ((packet, state, player, context) -> {
        final C15PacketClientSettings settings = (C15PacketClientSettings) packet;
        PlayerChangeClientSettingsEvent event = SpongeEventFactory.createPlayerChangeClientSettingsEvent(Cause.of(NamedCause.source(player)),
                (ChatVisibility) (Object) settings.getChatVisibility(), SkinUtil.fromFlags(settings.getModelPartFlags()),
                LanguageUtil.LOCALE_CACHE.getUnchecked(settings.getLang()), (Player) player, settings.isColorsEnabled(), settings.view);
        SpongeImpl.postEvent(event);
    });
    PacketFunction CLIENT_STATUS = ((packet, state, player, context) -> {
        if (state == PacketPhase.Inventory.OPEN_INVENTORY) {
            final ItemStackSnapshot lastCursor = context.firstNamed(TrackingUtil.CURSOR, ItemStackSnapshot.class).get();
            final ItemStackSnapshot newCursor = ItemStackUtil.snapshotOf(player.inventory.getItemStack());
            final Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(lastCursor, newCursor);
            EventConsumer.supplyEvent(() -> SpongeEventFactory.createInteractInventoryEventOpen(Cause.source(player).build(), cursorTransaction, ContainerUtil.fromNative(player.openContainer)))
                .cancelled(event -> player.closeScreen())
                .nonCancelled(event -> {
                    // Custom cursor
                    if (event.getCursorTransaction().getCustom().isPresent()) {
                        PacketPhaseUtil.handleCustomCursor(player, event.getCursorTransaction().getFinal());
                    }
                })
                .buildAndPost();
        }
    });
    PacketFunction RESOURCE_PACKET = ((packet, state, player, context) -> {
        final NetHandlerPlayServer playerNetServerHandler = player.playerNetServerHandler;
        final IMixinNetHandlerPlayServer mixinHandler = (IMixinNetHandlerPlayServer) playerNetServerHandler;
        final C19PacketResourcePackStatus resource = (C19PacketResourcePackStatus) packet;
        final String hash = resource.hash;
        final ResourcePackStatusEvent.ResourcePackStatus status;
        final ResourcePack pack = mixinHandler.getSentResourcePacks().get(hash);
        switch (resource.status) {
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
