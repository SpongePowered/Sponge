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
package org.spongepowered.common.event.tracking.phase.packet.player;

import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.common.bridge.CreatorTrackedBridge;
import org.spongepowered.common.bridge.server.level.ServerPlayerBridge;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.packet.BasicPacketContext;
import org.spongepowered.common.event.tracking.phase.packet.BasicPacketState;
import org.spongepowered.common.item.util.ItemStackUtil;

import javax.annotation.Nullable;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;

public final class InteractEntityPacketState extends BasicPacketState {

    @Override
    public boolean isPacketIgnored(Packet<?> packetIn, ServerPlayer packetPlayer) {
        final ServerboundInteractPacket useEntityPacket = (ServerboundInteractPacket) packetIn;
        // There are cases where a player is interacting with an entity that doesn't exist on the server.
        @Nullable net.minecraft.world.entity.Entity entity = useEntityPacket.getTarget(packetPlayer.level);
        return entity == null;
    }

    @Override
    public void populateContext(ServerPlayer playerMP, Packet<?> packet, BasicPacketContext context) {
        final ServerboundInteractPacket useEntityPacket = (ServerboundInteractPacket) packet;
        net.minecraft.world.entity.Entity entity = useEntityPacket.getTarget(playerMP.level);
        if (entity != null) {
            final ItemStack stack = ItemStackUtil.cloneDefensive(playerMP.getItemInHand(useEntityPacket.getHand()));
            if (stack != null) {
                context.itemUsed(stack);
            }
            final HandType handType = (HandType) (Object) useEntityPacket.getHand();
            context.handUsed(handType);
        }

    }

    @Override
    public void unwind(BasicPacketContext phaseContext) {

        final ServerPlayer player = phaseContext.getPacketPlayer();
        final ServerboundInteractPacket useEntityPacket = phaseContext.getPacket();
        final net.minecraft.world.entity.Entity entity = useEntityPacket.getTarget(player.level);
        if (entity == null) {
            // Something happened?
            return;
        }
        if (entity instanceof CreatorTrackedBridge) {
            ((CreatorTrackedBridge) entity).tracked$setCreatorReference(((ServerPlayerBridge) player).bridge$getUser());
        } else {
            ((Entity) entity).offer(Keys.NOTIFIER, player.getUUID());
        }
        TrackingUtil.processBlockCaptures(phaseContext);
    }

}
