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
package org.spongepowered.common.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.network.play.client.CPacketCreativeInventoryAction;
import net.minecraft.network.play.client.CPacketPlayerBlockPlacement;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketUpdateSign;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumHand;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.util.StaticMixinHelper;

public class PacketUtil {

    public static void onProcessPacket(Packet packetIn, INetHandler netHandler) {
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
            if (StaticMixinHelper.packetPlayer.theItemInWorldManager.isCreative() && (packetIn instanceof CPacketClientStatus
                  && ((CPacketClientStatus) packetIn).getStatus() == CPacketClientStatus.State.OPEN_INVENTORY_ACHIEVEMENT)) {
                StaticMixinHelper.lastInventoryOpenPacketTimeStamp = System.currentTimeMillis();
            } else if (creativeCheck(packetIn)) {

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
                                                                                                       : ((org.spongepowered.api.item.inventory.ItemStack) StaticMixinHelper.packetPlayer.inventory
                                                                                                               .getItemStack()).createSnapshot();
            StaticMixinHelper.lastCursor = cursor;

            IMixinWorld world = (IMixinWorld) StaticMixinHelper.packetPlayer.worldObj;
            if (StaticMixinHelper.packetPlayer.getHeldItem(EnumHand.MAIN_HAND) != null
                && (packetIn instanceof CPacketPlayerDigging || packetIn instanceof CPacketPlayerBlockPlacement)) {
                StaticMixinHelper.prePacketProcessItem = ItemStack.copyItemStack(StaticMixinHelper.packetPlayer.getHeldItem(EnumHand.MAIN_HAND));
            }

            world.getCauseTracker().setProcessingCaptureCause(true);
            packetIn.processPacket(netHandler);
            ((IMixinWorld) StaticMixinHelper.packetPlayer.worldObj)
                .getCauseTracker().handlePostTickCaptures(Cause.of(NamedCause.source(StaticMixinHelper.packetPlayer)));
            world.getCauseTracker().setProcessingCaptureCause(false);
            resetStaticData();
        } else { // client
            packetIn.processPacket(netHandler);
        }
    }

    private static boolean creativeCheck(Packet packet) {
        return packet instanceof CPacketCreativeInventoryAction;
    }

    public static void resetStaticData() {
        StaticMixinHelper.packetPlayer = null;
        StaticMixinHelper.processingPacket = null;
        StaticMixinHelper.lastCursor = null;
        StaticMixinHelper.lastOpenContainer = null;
        StaticMixinHelper.prePacketProcessItem = null;
        StaticMixinHelper.ignoreCreativeInventoryPacket = false;
    }

    public static boolean processSignPacket(CPacketUpdateSign packetIn, CallbackInfo ci, TileEntitySign tileentitysign, EntityPlayerMP playerEntity) {
        // TODO: Check if this is still actually necessary

        /*if (!SpongeImpl.getGlobalConfig().getConfig().getExploits().isPreventSignExploit()) {
            return true;
        }
        // Sign command exploit fix
        for (int i = 0; i < packetIn.getLines().length; ++i) {
            TextStyl chatstyle = packetIn.getLines()[i] == null ? null : packetIn.getLines()[i].getChatStyle();

            if (chatstyle != null && chatstyle.getChatClickEvent() != null) {
                ClickEvent clickevent = chatstyle.getChatClickEvent();

                if (clickevent.getAction() == ClickEvent.Action.RUN_COMMAND) {
                    if (!MinecraftServer.getServer().getConfigurationManager().canSendCommands(playerEntity.getGameProfile())) {
                        SpongeHooks.logExploitSignCommandUpdates(playerEntity, tileentitysign, clickevent.getValue());
                        playerEntity.playerNetServerHandler.kickPlayerFromServer("You have been kicked for attempting to perform a sign command exploit.");
                        ci.cancel();
                        return false;
                    }
                }
            }
            packetIn.getLines()[i] = new ChatComponentText(SpongeHooks.getTextWithoutFormattingCodes(packetIn.getLines()[i].getUnformattedText()));
        }*/
        return true;

    }
}
