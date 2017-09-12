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

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketClientSettings;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.network.play.client.CPacketCreativeInventoryAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.Humanoid;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.entity.living.humanoid.AnimateHandEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.TrackingPhases;
import org.spongepowered.common.event.tracking.phase.packet.IPacketState;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhase;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayerMP;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.util.VecHelper;

import java.lang.ref.WeakReference;
import java.util.Optional;

public class PacketUtil {

    private static final PhaseContext EMPTY_INVALID = PhaseContext.start().complete();
    private static long lastInventoryOpenPacketTimeStamp = 0;
    private static long lastTryBlockPacketTimeStamp = 0;
    private static boolean lastTryBlockPacketItemResult = true;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void onProcessPacket(Packet packetIn, INetHandler netHandler) {
        if (CauseTracker.ENABLED && netHandler instanceof NetHandlerPlayServer) {
            try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                EntityPlayerMP packetPlayer = ((NetHandlerPlayServer) netHandler).player;
                Sponge.getCauseStackManager().pushCause(packetPlayer);
                // If true, logic was handled in Pre so return
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
                // TODO 1.12-pre2 is something here still needed
    //            if (packetPlayer.interactionManager.isCreative() && (packetIn instanceof CPacketClientStatus && ((CPacketClientStatus) packetIn).getStatus() == CPacketClientStatus.State.OPEN_INVENTORY_ACHIEVEMENT)) {
    //                lastInventoryOpenPacketTimeStamp = System.currentTimeMillis();
    //            } else
                if (creativeCheck(packetIn, packetPlayer)) {
    
                    long packetDiff = System.currentTimeMillis() - lastInventoryOpenPacketTimeStamp;
                    // If the time between packets is small enough, mark the current packet to be ignored for our event handler.
                    if (packetDiff < 100) {
                        ignoreCreative = true;
                    }
                }
    
                // Don't process movement capture logic if player hasn't moved
                boolean ignoreMovementCapture = false;
                if (packetIn instanceof CPacketPlayer) {
                    CPacketPlayer movingPacket = ((CPacketPlayer) packetIn);
                    if (movingPacket instanceof CPacketPlayer.Rotation) {
                        ignoreMovementCapture = true;
                    } else if (packetPlayer.posX == movingPacket.x && packetPlayer.posY == movingPacket.y && packetPlayer.posZ == movingPacket.z) {
                        ignoreMovementCapture = true;
                    }
                }
                if (ignoreMovementCapture || (packetIn instanceof CPacketClientSettings)) {
                    packetIn.processPacket(netHandler);
                } else {
                    final ItemStackSnapshot cursor = ItemStackUtil.snapshotOf(packetPlayer.inventory.getItemStack());
                    final CauseTracker causeTracker = CauseTracker.getInstance();
                    IPacketState packetState = TrackingPhases.PACKET.getStateForPacket(packetIn);
                    if (packetState == null) {
                        throw new IllegalArgumentException("Found a null packet phase for packet: " + packetIn.getClass());
                    }
                    PhaseContext context = EMPTY_INVALID;
                    if (!TrackingPhases.PACKET.isPacketInvalid(packetIn, packetPlayer, packetState)) {
                        context = PhaseContext.start()
                                .source(packetPlayer)
                                .addExtra(InternalNamedCauses.Packet.PACKET_PLAYER, packetPlayer)
                                .addExtra(InternalNamedCauses.Packet.CAPTURED_PACKET, packetIn)
                                .addExtra(InternalNamedCauses.Packet.CURSOR, cursor)
                                .addExtra(InternalNamedCauses.Packet.IGNORING_CREATIVE, ignoreCreative);
    
                        TrackingPhases.PACKET.populateContext(packetIn, packetPlayer, packetState, context);
                        context.owner((Player) packetPlayer);
                        context.notifier((Player) packetPlayer);
                        context.complete();
                    } else {
                        packetState = PacketPhase.General.INVALID;
                    }
                    causeTracker.switchToPhase(packetState, context, () -> {
                        packetIn.processPacket(netHandler);
                        return null;
                    });
    
                    if (packetIn instanceof CPacketClientStatus) {
                        // update the reference of player
                        packetPlayer = ((NetHandlerPlayServer) netHandler).player;
                    }
                    ((IMixinEntityPlayerMP) packetPlayer).setPacketItem(ItemStack.EMPTY);
                }
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
            SpongeCommonEventFactory.lastAnimationPlayer = new WeakReference<>(playerMP);
            HandType handType = packet.getHand() == EnumHand.MAIN_HAND ? HandTypes.MAIN_HAND : HandTypes.OFF_HAND;
            final ItemStack heldItem = playerMP.getHeldItem(packet.getHand());
            Sponge.getCauseStackManager().addContext(EventContextKeys.USED_ITEM, ItemStackUtil.snapshotOf(heldItem));
            AnimateHandEvent event =
                    SpongeEventFactory.createAnimateHandEvent(Sponge.getCauseStackManager().getCurrentCause(), handType, (Humanoid) playerMP);
            if (SpongeImpl.postEvent(event)) {
                return true;
            }
            return false;
        } else if (packetIn instanceof CPacketPlayerDigging) {
            SpongeCommonEventFactory.lastPrimaryPacketTick = SpongeImpl.getServer().getTickCounter();
            CPacketPlayerDigging packet = (CPacketPlayerDigging) packetIn;
            ItemStack stack = playerMP.getHeldItemMainhand();
            Sponge.getCauseStackManager().addContext(EventContextKeys.USED_ITEM, ItemStackUtil.snapshotOf(stack));
            switch (packet.getAction()) {
                case DROP_ITEM:
                case DROP_ALL_ITEMS:
                    if (!stack.isEmpty() && !playerMP.isSpectator()) {
                        ((IMixinEntityPlayerMP) playerMP).setPacketItem(stack.copy());
                    }
                    return false;
                case START_DESTROY_BLOCK:
                case ABORT_DESTROY_BLOCK:
                case STOP_DESTROY_BLOCK:
                    final BlockPos pos = packet.getPosition();
                    Vector3d interactionPoint = VecHelper.toVector3d(pos);
                    BlockSnapshot blockSnapshot = new Location<>((World) playerMP.world, interactionPoint).createSnapshot();
                    if(SpongeCommonEventFactory.callInteractItemEventPrimary(playerMP, stack, EnumHand.MAIN_HAND, Optional.of(interactionPoint), blockSnapshot).isCancelled()) {
                        ((IMixinEntityPlayerMP) playerMP).sendBlockChange(pos, playerMP.world.getBlockState(pos));
                        return true;
                    }

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
                        if (SpongeCommonEventFactory.callInteractBlockEventPrimary(playerMP, blockSnapshot, EnumHand.MAIN_HAND, packet.getFacing()).isCancelled()) {
                            ((IMixinEntityPlayerMP) playerMP).sendBlockChange(pos, playerMP.world.getBlockState(pos));
                            return true;
                        }
                    }

                    return false;
                default:
                    break;
            }
        } else if (packetIn instanceof CPacketPlayerTryUseItem) {
            CPacketPlayerTryUseItem packet = (CPacketPlayerTryUseItem) packetIn;
            SpongeCommonEventFactory.lastSecondaryPacketTick = SpongeImpl.getServer().getTickCounter();
            long packetDiff = System.currentTimeMillis() - lastTryBlockPacketTimeStamp;
            // If the time between packets is small enough, use the last result.
            if (packetDiff < 100) {
                // Use previous result and avoid firing a second event
                return lastTryBlockPacketItemResult;
            }

            final ItemStack heldItem = playerMP.getHeldItem(packet.getHand());
            Sponge.getCauseStackManager().addContext(EventContextKeys.USED_ITEM, ItemStackUtil.snapshotOf(heldItem));
            boolean isCancelled = SpongeCommonEventFactory.callInteractItemEventSecondary(playerMP, heldItem, packet.getHand(), Optional.empty(), BlockSnapshot.NONE).isCancelled();
            SpongeCommonEventFactory.callInteractBlockEventSecondary(playerMP, heldItem, Optional.empty(), BlockSnapshot.NONE, Direction.NONE, packet.getHand());
            if (isCancelled) {
                // Multiple slots may have been changed on the client. Right
                // clicking armor is one example - the client changes it
                // without the server telling it to.
                playerMP.sendAllContents(playerMP.openContainer, playerMP.openContainer.getInventory());
                return true;
            }
        } else if (packetIn instanceof CPacketPlayerTryUseItemOnBlock) {
            CPacketPlayerTryUseItemOnBlock packet = (CPacketPlayerTryUseItemOnBlock) packetIn;
            lastTryBlockPacketTimeStamp = System.currentTimeMillis();
            SpongeCommonEventFactory.lastSecondaryPacketTick = SpongeImpl.getServer().getTickCounter();
            Vector3d interactionPoint = VecHelper.toVector3d(packet.getPos());
            BlockSnapshot blockSnapshot = new Location<>((World) playerMP.world, interactionPoint).createSnapshot();
            final ItemStack heldItem = playerMP.getHeldItem(packet.getHand());
            Sponge.getCauseStackManager().addContext(EventContextKeys.USED_ITEM, ItemStackUtil.snapshotOf(heldItem));
            boolean isCancelled = SpongeCommonEventFactory.callInteractItemEventSecondary(playerMP, heldItem, packet.getHand(), Optional.of(interactionPoint), blockSnapshot).isCancelled();
            lastTryBlockPacketItemResult = isCancelled;
            if(isCancelled) {
                // update client
                BlockPos pos = packet.getPos();
                playerMP.connection.sendPacket(new SPacketBlockChange(playerMP.world, pos));
                playerMP.connection.sendPacket(new SPacketBlockChange(playerMP.world, pos.offset(packet.getDirection())));
                playerMP.sendAllContents(playerMP.openContainer, playerMP.openContainer.getInventory()); // See above
                return true;
            }
        }

        return false;
    }
}
