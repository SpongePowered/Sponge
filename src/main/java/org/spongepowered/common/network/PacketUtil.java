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
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketClientSettings;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.network.play.client.CPacketCreativeInventoryAction;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.Humanoid;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.living.humanoid.AnimateHandEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.BlockUtil;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.packet.IPacketState;
import org.spongepowered.common.event.tracking.phase.TrackingPhases;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhase;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

public class PacketUtil {

    private static final PhaseContext EMPTY_INVALID = PhaseContext.start().complete();
    private static long lastInventoryOpenPacketTimeStamp = 0;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void onProcessPacket(Packet packetIn, INetHandler netHandler) {
        if (netHandler instanceof NetHandlerPlayServer) {
            EntityPlayerMP packetPlayer = ((NetHandlerPlayServer) netHandler).playerEntity;

            // Fire packet events and return if cancelled
            if (firePreEvents(packetIn, packetPlayer)) {
                return;
            }
            boolean ignoreCreative = false;

            // This is another horrible hack required since the client sends a C10 packet for every slot
            // containing an itemstack after a C16 packet in the following scenarios :
            // 1. Opening creative inventory after initial server join.
            // 2. Opening creative inventory again after making a change in previous inventory open.
            //
            // This is done in order to sync client inventory to server and would be fine if the C10 packet
            // included an Enum of some sort that defined what type of sync was happening.
            if (packetPlayer.interactionManager.isCreative() && (packetIn instanceof CPacketClientStatus && ((CPacketClientStatus) packetIn).getStatus() == CPacketClientStatus.State.OPEN_INVENTORY_ACHIEVEMENT)) {
                lastInventoryOpenPacketTimeStamp = System.currentTimeMillis();
            } else if (creativeCheck(packetIn, packetPlayer)) {

                long packetDiff = System.currentTimeMillis() - lastInventoryOpenPacketTimeStamp;
                // If the time between packets is small enough, mark the current packet to be ignored for our event handler.
                if (packetDiff < 100) {
                    ignoreCreative = true;
                }
            }

            if (!CauseTracker.ENABLED && (packetIn instanceof CPacketAnimation || packetIn instanceof CPacketClientSettings)) {
                packetIn.processPacket(netHandler);
            } else {
                final ItemStackSnapshot cursor = ItemStackUtil.snapshotOf(packetPlayer.inventory.getItemStack());
                final IMixinWorldServer world = (IMixinWorldServer) packetPlayer.worldObj;
                final CauseTracker causeTracker = world.getCauseTracker();
                final IPacketState packetState = TrackingPhases.PACKET.getStateForPacket(packetIn);
                if (packetState == null) {
                    throw new IllegalArgumentException("Found a null packet phase for packet: " + packetIn.getClass());
                }
                if (!TrackingPhases.PACKET.isPacketInvalid(packetIn, packetPlayer, packetState)) {
                    PhaseContext context = PhaseContext.start()
                            .add(NamedCause.source(packetPlayer))
                            .add(NamedCause.of(InternalNamedCauses.Packet.PACKET_PLAYER, packetPlayer))
                            .add(NamedCause.of(InternalNamedCauses.Packet.CAPTURED_PACKET, packetIn))
                            .add(NamedCause.of(InternalNamedCauses.Packet.CURSOR, cursor))
                            .add(NamedCause.of(InternalNamedCauses.Packet.IGNORING_CREATIVE, ignoreCreative));

                    TrackingPhases.PACKET.populateContext(packetIn, packetPlayer, packetState, context);
                    context.owner((Player) packetPlayer);
                    context.notifier((Player) packetPlayer);
                    context.complete();
                    causeTracker.switchToPhase(packetState, context);
                } else {
                    causeTracker.switchToPhase(PacketPhase.General.INVALID, EMPTY_INVALID);
                }
                packetIn.processPacket(netHandler);
                causeTracker.completePhase();
            }
        } else { // client
            packetIn.processPacket(netHandler);
        }
    }

    private static boolean creativeCheck(Packet<?> packetIn, EntityPlayerMP playerMP) {
        return packetIn instanceof CPacketCreativeInventoryAction;
    }

    private static boolean firePreEvents(Packet<?> packetIn, EntityPlayerMP playerMP) {
        if (packetIn instanceof CPacketAnimation) {
            CPacketAnimation packet = (CPacketAnimation) packetIn;
            SpongeCommonEventFactory.lastAnimationPacketTick = SpongeImpl.getServer().getTickCounter();
            SpongeCommonEventFactory.lastAnimationPlayer = playerMP;
            HandType handType = packet.getHand() == EnumHand.MAIN_HAND ? HandTypes.MAIN_HAND : HandTypes.OFF_HAND;
            AnimateHandEvent event = SpongeEventFactory.createAnimateHandEvent(Cause.of(NamedCause.source(playerMP)), handType, (Humanoid) playerMP);
            if (SpongeImpl.postEvent(event)) {
                return true;
            }
        } else if (packetIn instanceof CPacketPlayerDigging) {
            SpongeCommonEventFactory.lastPrimaryPacketTick = SpongeImpl.getServer().getTickCounter();
            CPacketPlayerDigging packet = (CPacketPlayerDigging) packetIn;
            ItemStack stack = playerMP.getHeldItemMainhand();
            if(stack != null && SpongeCommonEventFactory.callInteractItemEventPrimary(playerMP, stack, EnumHand.MAIN_HAND).isCancelled()) {
                BlockUtil.sendClientBlockChange(playerMP, packet.getPosition());
                return true;
            }

            switch (packet.getAction()) {
                case START_DESTROY_BLOCK:
                case ABORT_DESTROY_BLOCK:
                case STOP_DESTROY_BLOCK:
                    BlockPos pos = packet.getPosition();
                    double d0 = playerMP.posX - ((double)pos.getX() + 0.5D);
                    double d1 = playerMP.posY - ((double)pos.getY() + 0.5D) + 1.5D;
                    double d2 = playerMP.posZ - ((double)pos.getZ() + 0.5D);
                    double d3 = d0 * d0 + d1 * d1 + d2 * d2;
        
                    double dist = SpongeImplHooks.getBlockReachDistance(playerMP)+ 1;
                    dist *= dist;
        
                    if (d3 > dist) {
                        return true;
                    } else if (pos.getY() >= SpongeImpl.getServer().getBuildLimit()) {
                        return true;
                    }
                    if (packet.getAction() == CPacketPlayerDigging.Action.START_DESTROY_BLOCK) {
                        if (SpongeCommonEventFactory.callInteractBlockEventPrimary(playerMP, pos, EnumHand.MAIN_HAND, packet.getFacing()).isCancelled()) {
                            BlockUtil.sendClientBlockChange(playerMP, pos);
                            return true;
                        }
                    }
                default:
                    break;
            }
        } else if (packetIn instanceof CPacketPlayerTryUseItem) {
            CPacketPlayerTryUseItem packet = (CPacketPlayerTryUseItem) packetIn;
            SpongeCommonEventFactory.lastSecondaryPacketTick = SpongeImpl.getServer().getTickCounter();
            if(SpongeCommonEventFactory.callInteractItemEventSecondary(playerMP, playerMP.getHeldItem(packet.getHand()), packet.getHand()).isCancelled()) {
                return true;
            }
        } else if (packetIn instanceof CPacketPlayerTryUseItemOnBlock) {
            CPacketPlayerTryUseItemOnBlock packet = (CPacketPlayerTryUseItemOnBlock) packetIn;
            SpongeCommonEventFactory.lastSecondaryPacketTick = SpongeImpl.getServer().getTickCounter();
            if(SpongeCommonEventFactory.callInteractItemEventSecondary(playerMP, playerMP.getHeldItem(packet.getHand()), packet.getHand()).isCancelled()) {
                return true;
            }
        }

        return false;
    }
}
