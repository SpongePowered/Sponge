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

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingHelper;
import org.spongepowered.common.event.tracking.phase.PacketPhase;

import java.util.Optional;

@FunctionalInterface
public interface PacketFunction {

    PacketFunction USE_ENTITY = (packet, state, player, context) -> {
        if (state == PacketPhase.General.ATTACK_ENTITY) {
            final C02PacketUseEntity useEntityPacket = (C02PacketUseEntity) packet;
            net.minecraft.entity.Entity entity = useEntityPacket.getEntityFromWorld(player.worldObj);

            if (entity != null && entity.isDead && !(entity instanceof EntityLivingBase)) {
                MessageChannel originalChannel = MessageChannel.TO_NONE;

                DestructEntityEvent event = SpongeEventFactory.createDestructEntityEvent(Cause.source(player).build(), originalChannel, Optional.of(originalChannel), new MessageEvent.MessageFormatter(), (Entity) entity, true);
                SpongeImpl.getGame().getEventManager().post(event);
                if (!event.isMessageCancelled()) {
                    event.getChannel().ifPresent(channel -> channel.send(entity, event.getMessage()));
                }
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
            boolean ignoringCreative = context.firstNamed(TrackingHelper.IGNORING_CREATIVE, Boolean.class).orElse(false);
            if (!ignoringCreative) {
                PacketPhaseUtil.handleCreativeClickInventoryEvent(state, context);
            }
        }
    };

    PacketFunction INVENTORY = (packet, state, player, context) -> {
        PacketPhaseUtil.handleInventoryEvents(state, context);
    };

    void unwind(Packet<?> packet, PacketPhase.IPacketState state, EntityPlayerMP player, PhaseContext context);

}
