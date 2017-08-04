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

import com.google.common.collect.Lists;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumHand;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.interfaces.IMixinContainer;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayerMP;
import org.spongepowered.common.item.inventory.util.ContainerUtil;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;

import java.util.ArrayList;
import java.util.List;

final class DropItemInGameWithHotkeyState extends BasicInventoryPacketState {

    @Override
    public boolean doesCaptureEntityDrops() {
        return true;
    }

    @Override
    public void populateContext(EntityPlayerMP playerMP, Packet<?> packet, PhaseContext context) {
        super.populateContext(playerMP, packet, context);
        // unused, to be removed and re-located when phase context is cleaned up
        //context.add(NamedCause.of(InternalNamedCauses.General.DESTRUCT_ITEM_DROPS, false));
    }

    @Override
    public void unwind(Packet<?> packet, EntityPlayerMP player, PhaseContext context) {
        final ItemStack usedStack = context.firstNamed(InternalNamedCauses.Packet.ITEM_USED, ItemStack.class)
                .orElse(null);
        final ItemStackSnapshot usedSnapshot = ItemStackUtil.snapshotOf(usedStack);
        final Entity spongePlayer = EntityUtil.fromNative(player);
        context.getCapturedBlockSupplier()
                .ifPresentAndNotEmpty(blocks ->
                        TrackingUtil.processBlockCaptures(blocks, this, context)
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

                    final CPacketPlayerDigging packetIn =
                            context.firstNamed(InternalNamedCauses.Packet.CAPTURED_PACKET, CPacketPlayerDigging.class)
                                    .orElseThrow(TrackingUtil
                                            .throwWithContext("Expected to be capturing the packet used, but no packet was captured!",
                                                    context));

                    CPacketPlayerDigging.Action action = packetIn.getAction();

                    final int usedButton = action == CPacketPlayerDigging.Action.DROP_ITEM ? PacketPhase.PACKET_BUTTON_PRIMARY_ID : 1;

                    Transaction<ItemStackSnapshot> cursorTrans = new Transaction<>(ItemStackSnapshot.NONE, ItemStackSnapshot.NONE);
                    final IMixinContainer mixinContainer = ContainerUtil.toMixin(player.openContainer);
                    List<SlotTransaction> slotTrans = mixinContainer.getCapturedTransactions();
                    ClickInventoryEvent.Drop dropItemEvent = createDropItemEvent(player, ContainerUtil.fromNative(player.openContainer), cursorTrans,
                                    Lists.newArrayList(slotTrans), entities, cause, usedButton);

                    SpongeImpl.postEvent(dropItemEvent);
                    if (!dropItemEvent.isCancelled() || PacketPhaseUtil.allTransactionsInvalid(dropItemEvent.getTransactions())) {
                        PacketPhaseUtil.processSpawnedEntities(player, dropItemEvent);
                    } else {
                        ((IMixinEntityPlayerMP) player).restorePacketItem(EnumHand.MAIN_HAND);
                    }
                    slotTrans.clear();
                    mixinContainer.setCaptureInventory(false);
                });
        context.getCapturedEntityDropSupplier()
                .ifPresentAndNotEmpty(itemMapping -> {

                });

        final IMixinContainer mixinContainer = ContainerUtil.toMixin(player.openContainer);
        mixinContainer.setCaptureInventory(false);
        mixinContainer.getCapturedTransactions().clear();
    }

    static ClickInventoryEvent.Drop createDropItemEvent(EntityPlayerMP playerMP, Container openContainer, Transaction<ItemStackSnapshot> transaction,
            List<SlotTransaction> slotTransactions, List<Entity> capturedEntities, Cause cause, int usedButton) {
        final Cause spawnCause = Cause.source(EntitySpawnCause.builder()
                .entity(EntityUtil.fromNative(playerMP))
                .type(InternalSpawnTypes.DROPPED_ITEM)
                .build())
                .named(NamedCause.of("Container", openContainer))
                .build();

        for (Entity currentEntity : capturedEntities) {
            currentEntity.setCreator(playerMP.getUniqueID());
        }

        // A 'primary click' is used by the game to indicate a single drop (e.g. pressing 'q' without holding 'control')
        return usedButton == PacketPhase.PACKET_BUTTON_PRIMARY_ID ?
                SpongeEventFactory.createClickInventoryEventDropSingle(spawnCause, transaction, capturedEntities, openContainer, slotTransactions) :
                SpongeEventFactory.createClickInventoryEventDropFull(spawnCause, transaction, capturedEntities, openContainer, slotTransactions);

    }

    @Override
    public boolean ignoresItemPreMerges() {
        return true;
    }

}
