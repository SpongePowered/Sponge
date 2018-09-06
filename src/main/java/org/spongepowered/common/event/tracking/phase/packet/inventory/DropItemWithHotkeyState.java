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
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.packet.PacketConstants;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.interfaces.IMixinContainer;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayerMP;
import org.spongepowered.common.item.inventory.util.ContainerUtil;

import java.util.ArrayList;
import java.util.List;

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
        final Entity spongePlayer = EntityUtil.fromNative(player);
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(spongePlayer);
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);

            context.getCapturedBlockSupplier()
                .acceptAndClearIfNotEmpty(blocks -> TrackingUtil.processBlockCaptures(blocks, this, context));
            context.getCapturedItemsSupplier()
                .acceptAndClearIfNotEmpty(items -> {

                    final ArrayList<Entity> entities = new ArrayList<>();
                    for (EntityItem item : items) {
                        entities.add(EntityUtil.fromNative(item));
                    }

                    int usedButton = 0;
                    if (context.getPacket() instanceof CPacketPlayerDigging) {
                        final CPacketPlayerDigging packetIn = context.getPacket();
                        usedButton = packetIn.getAction() == CPacketPlayerDigging.Action.DROP_ITEM ? PacketConstants.PACKET_BUTTON_PRIMARY_ID : 1;
                    } else {
                        final CPacketClickWindow packetIn = context.getPacket();
                        usedButton = packetIn.getUsedButton();
                    }

                    Transaction<ItemStackSnapshot> cursorTrans = new Transaction<>(ItemStackSnapshot.NONE, ItemStackSnapshot.NONE);
                    final IMixinContainer mixinContainer = ContainerUtil.toMixin( player.openContainer);
                    List<SlotTransaction> slotTrans = mixinContainer.getCapturedTransactions();
                    ClickInventoryEvent.Drop dropItemEvent = this
                        .createInventoryEvent(player, ContainerUtil.fromNative(player.openContainer), cursorTrans, Lists.newArrayList(slotTrans), entities,  usedButton);

                    SpongeImpl.postEvent(dropItemEvent);
                    if (dropItemEvent.isCancelled() || PacketPhaseUtil.allTransactionsInvalid(dropItemEvent.getTransactions())) {
                        ((IMixinEntityPlayerMP) player).restorePacketItem(EnumHand.MAIN_HAND);
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
            final IMixinContainer mixinContainer = ContainerUtil.toMixin(player.openContainer);
            mixinContainer.setCaptureInventory(false);
            mixinContainer.getCapturedTransactions().clear();
        }
    }

    @Override
    public ClickInventoryEvent.Drop createInventoryEvent(EntityPlayerMP playerMP, Container openContainer, Transaction<ItemStackSnapshot> transaction,
            List<SlotTransaction> slotTransactions, List<Entity> capturedEntities, int usedButton) {
        try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
            for (Entity currentEntity : capturedEntities) {
                currentEntity.setCreator(playerMP.getUniqueID());
            }

            // A 'primary click' is used by the game to indicate a single drop (e.g. pressing 'q' without holding
            // 'control')
            ClickInventoryEvent.Drop event = usedButton == PacketConstants.PACKET_BUTTON_PRIMARY_ID
                    ? SpongeEventFactory.createClickInventoryEventDropSingle(Sponge.getCauseStackManager().getCurrentCause(), transaction,
                            capturedEntities, openContainer, slotTransactions)
                    : SpongeEventFactory.createClickInventoryEventDropFull(Sponge.getCauseStackManager().getCurrentCause(), transaction, capturedEntities,
                            openContainer, slotTransactions);
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
