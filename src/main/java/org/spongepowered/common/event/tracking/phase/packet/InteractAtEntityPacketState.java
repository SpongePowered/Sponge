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
import net.minecraft.network.play.client.CPacketUseEntity;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import javax.annotation.Nullable;

final class InteractAtEntityPacketState extends BasicPacketState {

    @Override
    public boolean ignoresItemPreMerges() {
        return true;
    }

    @Override
    public boolean isPacketIgnored(Packet<?> packetIn, EntityPlayerMP packetPlayer) {
        final CPacketUseEntity useEntityPacket = (CPacketUseEntity) packetIn;
        // There are cases where a player is interacting with an entity that doesn't exist on the server.
        @Nullable net.minecraft.entity.Entity entity = useEntityPacket.getEntityFromWorld(packetPlayer.world);
        return entity == null;
    }

    @Override
    public void populateContext(EntityPlayerMP playerMP, Packet<?> packet, PhaseContext context) {
        final CPacketUseEntity useEntityPacket = (CPacketUseEntity) packet;
        final ItemStack stack = ItemStackUtil.cloneDefensive(playerMP.getHeldItem(useEntityPacket.getHand()));
        if (stack != null) {
            context.addExtra(InternalNamedCauses.Packet.ITEM_USED, stack);
        }
        context.addEntityDropCaptures()
                .addEntityCaptures()
                .addBlockCaptures();
    }

    @Override
    public boolean doesCaptureEntityDrops() {
        return true;
    }
}
