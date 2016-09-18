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

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.TrackingPhases;
import org.spongepowered.common.event.tracking.phase.block.BlockPhase;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.world.IMixinLocation;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.registry.type.ItemTypeRegistryModule;
import org.spongepowered.common.world.BlockChange;

import javax.annotation.Nullable;

enum GeneralPacketState implements IPacketState, IPhaseState {
    UNKNOWN() {
        @Override
        public boolean ignoresItemPreMerges() {
            return true;
        }

        @Override
        public boolean tracksBlockSpecificDrops() {
            return true;
        }

        @Override
        public boolean tracksEntitySpecificDrops() {
            return true;
        }

        @Override
        public void populateContext(EntityPlayerMP playerMP, Packet<?> packet, PhaseContext context) {
            context.addBlockCaptures().addEntityCaptures().addEntityDropCaptures();
        }
    },
    MOVEMENT() {
        @Override
        public void populateContext(EntityPlayerMP playerMP, Packet<?> packet, PhaseContext context) {
            context.addBlockCaptures().addEntityCaptures();
        }

    },
    INTERACTION() {

        @Override
        public boolean isInteraction() {
            return true;
        }

        @Override
        public void populateContext(EntityPlayerMP playerMP, Packet<?> packet, PhaseContext context) {
            final ItemStack stack = ItemStackUtil.cloneDefensive(playerMP.getHeldItemMainhand());
            if (stack != null) {
                context.add(NamedCause.of(InternalNamedCauses.Packet.ITEM_USED, stack));
            }
            context.addEntityDropCaptures()
                    .addEntityCaptures()
                    .addBlockCaptures();
        }

        @Override
        public boolean doesCaptureEntityDrops() {
            return true;
        }

        @Override
        public boolean canSwitchTo(IPhaseState state) {
            return state == BlockPhase.State.BLOCK_DECAY || state == BlockPhase.State.BLOCK_DROP_ITEMS;
        }

        @Override
        public boolean tracksBlockSpecificDrops() {
            return true;
        }

        @Override
        public boolean tracksEntitySpecificDrops() {
            return true;
        }
    },
    IGNORED() {
        @Override
        public boolean ignoresItemPreMerges() {
            return true;
        }
    },
    INTERACT_ENTITY {
        @Override
        public boolean ignoresItemPreMerges() {
            return true;
        }
        @Override
        public boolean isPacketIgnored(Packet<?> packetIn, EntityPlayerMP packetPlayer) {
            final CPacketUseEntity useEntityPacket = (CPacketUseEntity) packetIn;
            // There are cases where a player is interacting with an entity that doesn't exist on the server.
            @Nullable net.minecraft.entity.Entity entity = useEntityPacket.getEntityFromWorld(packetPlayer.worldObj);
            return entity == null;
        }

        @Override
        public void populateContext(EntityPlayerMP playerMP, Packet<?> packet, PhaseContext context) {
            final CPacketUseEntity useEntityPacket = (CPacketUseEntity) packet;
            net.minecraft.entity.Entity entity = useEntityPacket.getEntityFromWorld(playerMP.worldObj);
            if (entity != null) {
                context.add(NamedCause.of(InternalNamedCauses.Packet.TARGETED_ENTITY, entity));
                context.add(NamedCause.of(InternalNamedCauses.Packet.TRACKED_ENTITY_ID, entity.getEntityId()));
                final ItemStack stack = ItemStackUtil.cloneDefensive(playerMP.getHeldItem(useEntityPacket.getHand()));
                if (stack != null) {
                    context.add(NamedCause.of(InternalNamedCauses.Packet.ITEM_USED, stack));
                }
            }

            context.addEntityDropCaptures()
                    .addEntityCaptures()
                    .addBlockCaptures();
        }

        @Override
        public boolean tracksEntitySpecificDrops() {
            return true;
        }

        @Override
        public boolean doesCaptureEntityDrops() {
            return true;
        }
    },
    ATTACK_ENTITY() {
        @Override
        public boolean isPacketIgnored(Packet<?> packetIn, EntityPlayerMP packetPlayer) {
            final CPacketUseEntity useEntityPacket = (CPacketUseEntity) packetIn;
            // There are cases where a player is interacting with an entity that doesn't exist on the server.
            @Nullable net.minecraft.entity.Entity entity = useEntityPacket.getEntityFromWorld(packetPlayer.worldObj);
            return entity == null;
        }

        @Override
        public void populateContext(EntityPlayerMP playerMP, Packet<?> packet, PhaseContext context) {
            final CPacketUseEntity useEntityPacket = (CPacketUseEntity) packet;
            net.minecraft.entity.Entity entity = useEntityPacket.getEntityFromWorld(playerMP.worldObj);
            context.add(NamedCause.of(InternalNamedCauses.Packet.TARGETED_ENTITY, entity));
            context.add(NamedCause.of(InternalNamedCauses.Packet.TRACKED_ENTITY_ID, entity.getEntityId()));
            final ItemStack stack = ItemStackUtil.cloneDefensive(playerMP.getHeldItemMainhand());
            if (stack != null) {
                context.add(NamedCause.of(InternalNamedCauses.Packet.ITEM_USED, stack));
            }
            context.addEntityDropCaptures()
                    .addEntityCaptures()
                    .addBlockCaptures();
        }

        @Override
        public boolean tracksEntitySpecificDrops() {
            return true;
        }
    },
    INTERACT_AT_ENTITY {
        @Override
        public boolean ignoresItemPreMerges() {
            return true;
        }

        @Override
        public boolean isPacketIgnored(Packet<?> packetIn, EntityPlayerMP packetPlayer) {
            final CPacketUseEntity useEntityPacket = (CPacketUseEntity) packetIn;
            // There are cases where a player is interacting with an entity that doesn't exist on the server.
            @Nullable net.minecraft.entity.Entity entity = useEntityPacket.getEntityFromWorld(packetPlayer.worldObj);
            return entity == null;
        }

        @Override
        public void populateContext(EntityPlayerMP playerMP, Packet<?> packet, PhaseContext context) {
            final CPacketUseEntity useEntityPacket = (CPacketUseEntity) packet;
            final ItemStack stack = ItemStackUtil.cloneDefensive(playerMP.getHeldItem(useEntityPacket.getHand()));
            if (stack != null) {
                context.add(NamedCause.of(InternalNamedCauses.Packet.ITEM_USED, stack));
            }
            final net.minecraft.entity.Entity entity = useEntityPacket.getEntityFromWorld(playerMP.worldObj);
            if (entity != null) {
                context.add(NamedCause.of(InternalNamedCauses.Packet.TARGETED_ENTITY, entity));
                context.add(NamedCause.of(InternalNamedCauses.Packet.TRACKED_ENTITY_ID, entity.getEntityId()));
            }
            context.addEntityDropCaptures()
                    .addEntityCaptures()
                    .addBlockCaptures();
        }

        @Override
        public boolean doesCaptureEntityDrops() {
            return true;
        }
    },
    CHAT() {
        @Override
        public void populateContext(EntityPlayerMP playerMP, Packet<?> packet, PhaseContext context) {
            CPacketChatMessage chatMessage = (CPacketChatMessage) packet;
            if (chatMessage.getMessage().contains("kill")) {
                context.add(NamedCause.of(InternalNamedCauses.General.DESTRUCT_ITEM_DROPS, true));
            }
        }
    },
    CREATIVE_INVENTORY {

        @Override
        public void populateContext(EntityPlayerMP playerMP, Packet<?> packet, PhaseContext context) {
            context
                    .addEntityCaptures()
                    .addEntityDropCaptures();
        }

        @Override
        public boolean ignoresItemPreMerges() {
            return true;
        }

        @Override
        public boolean doesCaptureEntityDrops() {
            // We specifically capture because the entities are already
            // being captured in a drop event, and therefor will be
            // spawned manually into the world by the creative event handling.
            return true;
        }
    },
    PLACE_BLOCK() {
        @Override
        public boolean isInteraction() {
            return true;
        }

        @Override
        public void populateContext(EntityPlayerMP playerMP, Packet<?> packet, PhaseContext context) {
            // Note - CPacketPlayerTryUseItem is swapped with CPacketPlayerBlockPlacement
            final CPacketPlayerTryUseItemOnBlock placeBlock = (CPacketPlayerTryUseItemOnBlock) packet;
            final net.minecraft.item.ItemStack itemUsed = playerMP.getHeldItem(placeBlock.getHand());
            final ItemStack itemstack = ItemStackUtil.cloneDefensive(itemUsed);
            if (itemstack != null) {
                context.add(NamedCause.of(InternalNamedCauses.Packet.ITEM_USED, itemstack));
            } else {
                context.add(NamedCause.of(InternalNamedCauses.Packet.ITEM_USED, ItemTypeRegistryModule.NONE));
            }
            context.add(NamedCause.of(InternalNamedCauses.Packet.PLACED_BLOCK_POSITION, placeBlock.getPos()));
            context.add(NamedCause.of(InternalNamedCauses.Packet.PLACED_BLOCK_FACING, placeBlock.getDirection()));

            context
                    .addBlockCaptures()
                    .addEntityCaptures()
                    .addEntityDropCaptures();
        }

        @Override
        public void handleBlockChangeWithUser(@Nullable BlockChange blockChange, WorldServer minecraftWorld, Transaction<BlockSnapshot> transaction, PhaseContext context) {
            Player player = context.first(Player.class).get();
            BlockPos pos = ((IMixinLocation) (Object) transaction.getFinal().getLocation().get()).getBlockPos();
            IMixinChunk spongeChunk = (IMixinChunk) minecraftWorld.getChunkFromBlockCoords(pos);
            if (blockChange == BlockChange.PLACE) {
                spongeChunk.addTrackedBlockPosition((Block) transaction.getFinal().getState().getType(), pos, player, PlayerTracker.Type.OWNER);
            }
            spongeChunk.addTrackedBlockPosition((Block) transaction.getFinal().getState().getType(), pos, player, PlayerTracker.Type.NOTIFIER);
        }

    },
    OPEN_INVENTORY,
    REQUEST_RESPAWN,
    USE_ITEM {
        @Override
        public boolean isInteraction() {
            return true;
        }

        @Override
        public void populateContext(EntityPlayerMP playerMP, Packet<?> packet, PhaseContext context) {
            // Note - CPacketPlayerTryUseItem is swapped with CPacketPlayerBlockPlacement
            final CPacketPlayerTryUseItem placeBlock = (CPacketPlayerTryUseItem) packet;
            final net.minecraft.item.ItemStack usedItem = playerMP.getHeldItem(placeBlock.getHand());
            final ItemStack itemstack = ItemStackUtil.cloneDefensive(usedItem);
            if (itemstack != null) {
                context.add(NamedCause.of(InternalNamedCauses.Packet.HAND_USED, placeBlock.getHand()));
                context.add(NamedCause.of(InternalNamedCauses.Packet.ITEM_USED, itemstack));
            }

            context
                    .addEntityCaptures()
                    .addEntityDropCaptures()
                    .addBlockCaptures();
        }
    },
    INVALID() {
        @Override
        public boolean isPacketIgnored(Packet<?> packetIn, EntityPlayerMP packetPlayer) {
            return true;
        }
    },
    CLIENT_SETTINGS,
    START_RIDING_JUMP,
    ANIMATION,
    START_SNEAKING,
    STOP_SNEAKING,
    START_SPRINTING,
    STOP_SPRINTING,
    STOP_SLEEPING,
    CLOSE_WINDOW {
        @Override
        public void populateContext(EntityPlayerMP playerMP, Packet<?> packet, PhaseContext context) {
            context
                    .add(NamedCause.of(InternalNamedCauses.Packet.OPEN_CONTAINER, playerMP.openContainer))
                    .addBlockCaptures()
                    .addEntityCaptures()
                    .addEntityDropCaptures();
        }
    },
    UPDATE_SIGN {

        @Override
        public void populateContext(EntityPlayerMP playerMP, Packet<?> packet, PhaseContext context) {
            context
                    .addBlockCaptures()
                    .addEntityCaptures()
                    .addEntityDropCaptures();

        }
    },

    HANDLED_EXTERNALLY() {
        @Override
        public boolean ignoresItemPreMerges() {
            return true;
        }

        @Override
        public void populateContext(EntityPlayerMP playerMP, Packet<?> packet, PhaseContext context) {
            context
                    .addBlockCaptures()
                    .addEntityCaptures()
                    .addEntityDropCaptures();

        }
    },
    RESOURCE_PACK,
    STOP_RIDING_JUMP,
    SWAP_HAND_ITEMS,
    START_FALL_FLYING;

    @Override
    public PacketPhase getPhase() {
        return TrackingPhases.PACKET;
    }


    @Override
    public boolean matches(int packetState) {
        return false;
    }

}
