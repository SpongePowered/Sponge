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
package org.spongepowered.common.mixin.core.util;

import net.minecraft.item.ItemStack;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C10PacketCreativeInventoryAction;
import net.minecraft.network.play.client.C16PacketClientStatus;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.util.StaticMixinHelper;

@Mixin(targets = "net/minecraft/network/PacketThreadUtil$1")
public class MixinPacketThreadUtil {

    @Redirect(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Packet;processPacket(Lnet/minecraft/network/INetHandler;)V") , require = 1)
    public void onProcessPacket(Packet packetIn, INetHandler netHandler) {
        if (netHandler instanceof NetHandlerPlayServer) {
            StaticMixinHelper.processingPacket = packetIn;
            StaticMixinHelper.packetPlayer = ((NetHandlerPlayServer) netHandler).playerEntity;

            // This is another horrible hack required since the client sends a C10 packet for every slot
            // containing an itemstack after a C16 packet in the following scenarios :
            // 1. Opening creative inventory after initial server join.
            // 2. Opening creative inventory again after making a change in previous inventory open.
            //
            // This is done in order to sync client inventory to server and would be fine if the C10 packet
            // included an Enum of some sort that defined what type of sync was happening.
            if (StaticMixinHelper.packetPlayer.theItemInWorldManager.isCreative() && (packetIn instanceof C16PacketClientStatus
                    && ((C16PacketClientStatus) packetIn).getStatus() == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT)) {
                StaticMixinHelper.lastInventoryOpenPacketTimeStamp = System.currentTimeMillis();
            } else if (StaticMixinHelper.packetPlayer.theItemInWorldManager.isCreative() && packetIn instanceof C10PacketCreativeInventoryAction) {
                // Fix string overflow exploit in creative mode
                C10PacketCreativeInventoryAction creativePacket = (C10PacketCreativeInventoryAction) packetIn;
                ItemStack itemstack = creativePacket.getStack();
                if (itemstack != null && itemstack.getDisplayName().length() > 32767) {
                    SpongeHooks.logExploitItemNameOverflow(StaticMixinHelper.packetPlayer, itemstack.getDisplayName().length());
                    StaticMixinHelper.packetPlayer.playerNetServerHandler.kickPlayerFromServer("You have been kicked for attempting to perform an itemstack name overflow exploit.");
                    resetStaticData();
                    return;
                }

                long packetDiff = System.currentTimeMillis() - StaticMixinHelper.lastInventoryOpenPacketTimeStamp;
                // If the time between packets is small enough, mark the current packet to be ignored for our event handler.
                if (packetDiff < 100) {
                    StaticMixinHelper.ignoreCreativeInventoryPacket = true;
                }
            }

            // Fix invisibility respawn exploit
            // Disabled until it can be tested further
            /*if (packetIn instanceof C16PacketClientStatus) {
                C16PacketClientStatus statusPacket = (C16PacketClientStatus) packetIn;
                if (statusPacket.getStatus() == C16PacketClientStatus.EnumState.PERFORM_RESPAWN) {
                    if (!StaticMixinHelper.packetPlayer.isDead) {
                        SpongeHooks.logExploitRespawnInvisibility(StaticMixinHelper.packetPlayer);
                        StaticMixinHelper.packetPlayer.playerNetServerHandler.kickPlayerFromServer("You have been kicked for attempting to perform an invisibility respawn exploit.");
                        resetStaticData();
                        return;
                    }
                }

            }*/

            //System.out.println("RECEIVED PACKET " + packetIn);
            StaticMixinHelper.lastOpenContainer = StaticMixinHelper.packetPlayer.openContainer;
            ItemStackSnapshot cursor = StaticMixinHelper.packetPlayer.inventory.getItemStack() == null ? ItemStackSnapshot.NONE
                    : ((org.spongepowered.api.item.inventory.ItemStack) StaticMixinHelper.packetPlayer.inventory.getItemStack()).createSnapshot();
            StaticMixinHelper.lastCursor = cursor;

            IMixinWorld world = (IMixinWorld) StaticMixinHelper.packetPlayer.worldObj;
            if (StaticMixinHelper.packetPlayer.getHeldItem() != null
                    && (packetIn instanceof C07PacketPlayerDigging || packetIn instanceof C08PacketPlayerBlockPlacement)) {
                StaticMixinHelper.prePacketProcessItem = ItemStack.copyItemStack(StaticMixinHelper.packetPlayer.getHeldItem());
            }

            world.setProcessingCaptureCause(true);
            packetIn.processPacket(netHandler);
            ((IMixinWorld) StaticMixinHelper.packetPlayer.worldObj)
                .handlePostTickCaptures(Cause.of(NamedCause.source(StaticMixinHelper.packetPlayer)));
            world.setProcessingCaptureCause(false);
            resetStaticData();
        } else { // client
            packetIn.processPacket(netHandler);
        }
    }

    private void resetStaticData() {
        StaticMixinHelper.packetPlayer = null;
        StaticMixinHelper.processingPacket = null;
        StaticMixinHelper.lastCursor = null;
        StaticMixinHelper.lastOpenContainer = null;
        StaticMixinHelper.prePacketProcessItem = null;
        StaticMixinHelper.ignoreCreativeInventoryPacket = false;
    }
}
