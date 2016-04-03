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
import net.minecraft.event.ClickEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C10PacketCreativeInventoryAction;
import net.minecraft.network.play.client.C12PacketUpdateSign;
import net.minecraft.network.play.client.C15PacketClientSettings;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.PacketPhase;
import org.spongepowered.common.event.tracking.phase.TrackingPhases;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.util.SpongeHooks;

public class PacketUtil {

    public static long lastInventoryOpenPacketTimeStamp = 0;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void onProcessPacket(Packet packetIn, INetHandler netHandler) {
        if (netHandler instanceof NetHandlerPlayServer) {
            EntityPlayerMP packetPlayer = ((NetHandlerPlayServer) netHandler).playerEntity;

            boolean ignoreCreative = false;

            // This is another horrible hack required since the client sends a C10 packet for every slot
            // containing an itemstack after a C16 packet in the following scenarios :
            // 1. Opening creative inventory after initial server join.
            // 2. Opening creative inventory again after making a change in previous inventory open.
            //
            // This is done in order to sync client inventory to server and would be fine if the C10 packet
            // included an Enum of some sort that defined what type of sync was happening.
            if (packetPlayer.theItemInWorldManager.isCreative()
                && (packetIn instanceof C16PacketClientStatus
                    && ((C16PacketClientStatus) packetIn).getStatus() == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT)) {
                lastInventoryOpenPacketTimeStamp = System.currentTimeMillis();
            } else if (creativeCheck(packetIn, packetPlayer)) {

                long packetDiff = System.currentTimeMillis() - lastInventoryOpenPacketTimeStamp;
                // If the time between packets is small enough, mark the current packet to be ignored for our event handler.
                if (packetDiff < 100) {
                    ignoreCreative = true;
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
            final ItemStackSnapshot cursor = ItemStackUtil.snapshotOf(packetPlayer.inventory.getItemStack());

            final IMixinWorldServer world = (IMixinWorldServer) packetPlayer.worldObj;
            final ItemStack itemUsed;
            if ((packetIn instanceof C07PacketPlayerDigging || packetIn instanceof C08PacketPlayerBlockPlacement)) {
                itemUsed = ItemStackUtil.cloneDefensiveNative(packetPlayer.getHeldItem());
            } else {
                itemUsed = null;
            }

            final CauseTracker causeTracker = world.getCauseTracker();
            if (packetIn instanceof C03PacketPlayer || packetIn instanceof C0APacketAnimation || packetIn instanceof C15PacketClientSettings) {
                packetIn.processPacket(netHandler);
            } else {
                PhaseContext context = PhaseContext.start()
                        .add(NamedCause.source(packetPlayer))
                        .add(NamedCause.of(InternalNamedCauses.Packet.PACKET_PLAYER, packetPlayer))
                        .addCaptures()
                        .add(NamedCause.of(InternalNamedCauses.Packet.CAPTURED_PACKET, packetIn))
                        .add(NamedCause.of(InternalNamedCauses.Packet.CURSOR, cursor))
                        .add(NamedCause.of(InternalNamedCauses.Packet.IGNORING_CREATIVE, ignoreCreative));

                final PacketPhase.IPacketState packetState = TrackingPhases.PACKET.getStateForPacket(packetIn);
                TrackingPhases.PACKET.populateContext(packetIn, packetPlayer, packetState, context);
                context.complete();
                causeTracker.switchToPhase(TrackingPhases.PACKET, packetState, context);
                packetIn.processPacket(netHandler);
                causeTracker.completePhase();
            }
        } else { // client
            packetIn.processPacket(netHandler);
        }
    }

    private static boolean creativeCheck(Packet<?> packet, EntityPlayerMP playerMP) {
        return packet instanceof C10PacketCreativeInventoryAction;
    }


    public static boolean processSignPacket(C12PacketUpdateSign packetIn, CallbackInfo ci, TileEntitySign tileentitysign, EntityPlayerMP playerEntity) {
        if (!SpongeImpl.getGlobalConfig().getConfig().getExploits().isPreventSignExploit()) {
            return true;
        }
        // Sign command exploit fix
        for (int i = 0; i < packetIn.getLines().length; ++i) {
            ChatStyle chatstyle = packetIn.getLines()[i] == null ? null : packetIn.getLines()[i].getChatStyle();

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
        }
        return true;

    }
}
