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

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.world.World;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;

import java.util.List;

final class DropItemWithHotkeyState extends BasicInventoryPacketState {

    DropItemWithHotkeyState() {
        super(PacketPhase.MODE_DROP | PacketPhase.BUTTON_PRIMARY | PacketPhase.BUTTON_SECONDARY | PacketPhase.CLICK_ANYWHERE);
    }

    @Override
    public boolean doesCaptureEntityDrops() {
        return true;
    }

    @Override
    public void populateContext(EntityPlayerMP playerMP, Packet<?> packet, PhaseContext context) {
        super.populateContext(playerMP, packet, context);
        context
                .add(NamedCause.of(InternalNamedCauses.General.DESTRUCT_ITEM_DROPS, false));
    }

    @Override
    public InteractInventoryEvent createInventoryEvent(EntityPlayerMP playerMP, Container openContainer, Transaction<ItemStackSnapshot> transaction,
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
        final World spongeWorld = (World) playerMP.worldObj;

        // A 'primary click' is used by the game to indicate a single drop (e.g. pressing 'q' without holding 'control')
        return usedButton == PacketPhase.PACKET_BUTTON_PRIMARY_ID ?
               SpongeEventFactory.createClickInventoryEventDropSingle(spawnCause, transaction, capturedEntities, openContainer, spongeWorld, slotTransactions) :
               SpongeEventFactory.createClickInventoryEventDropFull(spawnCause, transaction, capturedEntities, openContainer, spongeWorld, slotTransactions);

    }

    @Override
    public boolean ignoresItemPreMerges() {
        return true;
    }

}
