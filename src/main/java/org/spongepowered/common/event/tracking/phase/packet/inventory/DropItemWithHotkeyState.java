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
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumHand;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.OwnershipTrackedBridge;
import org.spongepowered.common.bridge.inventory.ContainerBridge;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.packet.PacketConstants;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.util.ContainerUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

public final class DropItemWithHotkeyState extends BasicInventoryPacketState {

    public DropItemWithHotkeyState() {
        super(PacketConstants.MODE_DROP | PacketConstants.BUTTON_PRIMARY | PacketConstants.BUTTON_SECONDARY | PacketConstants.CLICK_INSIDE_WINDOW);
    }

    @Override
    public boolean doesCaptureEntityDrops(InventoryPacketContext context) {
        return true;
    }

    @Override
    public void unwind(InventoryPacketContext context) {
        final EntityPlayerMP player = context.getPacketPlayer();
        //final ItemStack usedStack = context.getItemUsed();
        //final ItemStackSnapshot usedSnapshot = ItemStackUtil.snapshotOf(usedStack);
        final Entity spongePlayer = (Entity) player;
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(spongePlayer);
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);

            // TODO - Determine if we need to pass the supplier or perform some parameterized
            //  process if not empty method on the capture object.
            TrackingUtil.processBlockCaptures(this, context);
            context.getCapturedItemsSupplier()
                .acceptAndClearIfNotEmpty(items -> {

                    final ArrayList<Entity> entities = new ArrayList<>();
                    for (EntityItem item : items) {
                        entities.add((Entity) item);
                    }

                    int usedButton;
                    Slot slot;
                    if (context.getPacket() instanceof CPacketPlayerDigging) {
                        final CPacketPlayerDigging packetIn = context.getPacket();
                        usedButton = packetIn.getAction() == CPacketPlayerDigging.Action.DROP_ITEM ? PacketConstants.PACKET_BUTTON_PRIMARY_ID : 1;
                        slot = ((PlayerInventory) player.inventory).getEquipment().getSlot(EquipmentTypes.MAIN_HAND).orElse(null);
                    } else {
                        final CPacketClickWindow packetIn = context.getPacket();
                        usedButton = packetIn.getUsedButton();
                        slot = ((InventoryAdapter) player.inventory).getSlot(packetIn.getSlotId()).orElse(null);
                    }

                    Transaction<ItemStackSnapshot> cursorTrans = new Transaction<>(ItemStackSnapshot.NONE, ItemStackSnapshot.NONE);
                    final ContainerBridge mixinContainer = ContainerUtil.toMixin( player.openContainer);
                    List<SlotTransaction> slotTrans = mixinContainer.bridge$getCapturedSlotTransactions();
                    ClickInventoryEvent.Drop dropItemEvent = this.createInventoryEvent(player, ContainerUtil.fromNative(player.openContainer),
                            cursorTrans, Lists.newArrayList(slotTrans), entities,  usedButton, slot);

                    SpongeImpl.postEvent(dropItemEvent);
                    if (dropItemEvent.isCancelled() || PacketPhaseUtil.allTransactionsInvalid(dropItemEvent.getTransactions())) {
                        ((ServerPlayerEntityBridge) player).bridge$restorePacketItem(EnumHand.MAIN_HAND);
                        PacketPhaseUtil.handleSlotRestore(player, player.openContainer, dropItemEvent.getTransactions(), true);
                    } else {
                        processSpawnedEntities(player, dropItemEvent);
                    }
                    slotTrans.clear();
                    mixinContainer.setCaptureInventory(false);
                });
            context.getPerEntityItemDropSupplier()
                .acceptAndClearIfNotEmpty(itemMapping -> {

                });
            final ContainerBridge mixinContainer = ContainerUtil.toMixin(player.openContainer);
            mixinContainer.setCaptureInventory(false);
            mixinContainer.bridge$getCapturedSlotTransactions().clear();
        }
    }

    @Override
    public ClickInventoryEvent.Drop createInventoryEvent(EntityPlayerMP playerMP, Container openContainer, Transaction<ItemStackSnapshot> transaction,
            List<SlotTransaction> slotTransactions, List<Entity> capturedEntities, int usedButton, @Nullable Slot slot) {
        try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
            for (Entity currentEntity : capturedEntities) {
                if (currentEntity instanceof OwnershipTrackedBridge) {
                    ((OwnershipTrackedBridge) currentEntity).tracked$setOwnerReference((Player) playerMP);
                } else {
                    currentEntity.setCreator(playerMP.getUniqueID());
                }            }

            // A 'primary click' is used by the game to indicate a single drop (e.g. pressing 'q' without holding 'control')
            ClickInventoryEvent.Drop event;
            if (usedButton == PacketConstants.PACKET_BUTTON_PRIMARY_ID) {
                event = SpongeEventFactory.createClickInventoryEventDropSingle(Sponge.getCauseStackManager().getCurrentCause(),
                        transaction, capturedEntities, Optional.ofNullable(slot), openContainer, slotTransactions);
            } else {
                event = SpongeEventFactory.createClickInventoryEventDropFull(Sponge.getCauseStackManager().getCurrentCause(),
                        transaction, capturedEntities, Optional.ofNullable(slot), openContainer, slotTransactions);
            }
            // TODO the nature of how this event is handled prevents the cause information being preserved through
            // the event call, somehow should not release this frame until after the event is posted
            return event;
        }
    }

    @Override
    public boolean ignoresItemPreMerging() {
        return true;
    }

}
