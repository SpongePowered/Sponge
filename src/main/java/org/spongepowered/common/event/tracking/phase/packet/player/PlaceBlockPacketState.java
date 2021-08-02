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

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.transaction.BlockTransactionReceipt;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.cause.entity.SpawnType;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.bridge.world.TrackedWorldBridge;
import org.spongepowered.common.bridge.world.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.bridge.world.level.TrackableBlockEventDataBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.packet.BasicPacketContext;
import org.spongepowered.common.event.tracking.phase.packet.BasicPacketState;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.world.BlockChange;
import org.spongepowered.common.world.server.SpongeLocatableBlockBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.SpawnEggItem;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class PlaceBlockPacketState extends BasicPacketState {

    private final BiConsumer<CauseStackManager.StackFrame, BasicPacketContext> BASIC_PACKET_MODIFIER =
            super.getFrameModifier()
                    .andThen((frame, ctx) -> {
                        frame.addContext(EventContextKeys.PLAYER_PLACE, ctx.getSpongePlayer().world());
                        frame.addContext(EventContextKeys.USED_HAND, ctx.getHandUsed());
                        frame.addContext(EventContextKeys.USED_ITEM, ctx.getItemUsedSnapshot());
                        frame.pushCause(ctx.getSpongePlayer());
                    });

    @Override
    public BiConsumer<CauseStackManager.StackFrame, BasicPacketContext> getFrameModifier() {
        return this.BASIC_PACKET_MODIFIER;
    }

    @Override
    public boolean isInteraction() {
        return true;
    }

    @Override
    public void populateContext(final net.minecraft.server.level.ServerPlayer playerMP, final Packet<?> packet, final BasicPacketContext context) {
        final ServerboundUseItemOnPacket placeBlock = (ServerboundUseItemOnPacket) packet;
        final net.minecraft.world.item.ItemStack itemUsed = playerMP.getItemInHand(placeBlock.getHand());
        final ItemStack itemstack = ItemStackUtil.cloneDefensive(itemUsed);
        context.itemUsed(itemstack);
        final HandType handType = (HandType) (Object) placeBlock.getHand();
        context.handUsed(handType);
    }

    @Override
    public void postBlockTransactionApplication(
        final BasicPacketContext context, final BlockChange blockChange,
        final BlockTransactionReceipt transaction
    ) {
        TrackingUtil.associateTrackerToTarget(blockChange, transaction, ((ServerPlayer) context.getPacketPlayer()).uniqueId());
    }

    @Override
    public void appendNotifierToBlockEvent(final BasicPacketContext context,
        final TrackedWorldBridge mixinWorldServer, final BlockPos pos, final TrackableBlockEventDataBridge blockEvent
    ) {
        final Player player = PhaseTracker.getCauseStackManager().currentCause().first(Player.class).get();
        final BlockState state = ((ServerWorld) mixinWorldServer).block(pos.getX(), pos.getY(), pos.getZ());
        final LocatableBlock locatable =
                new SpongeLocatableBlockBuilder().world((ServerWorld) mixinWorldServer).position(pos.getX(), pos.getY(), pos.getZ()).state(state).build();

        blockEvent.bridge$setTickingLocatable(locatable);
        blockEvent.bridge$setSourceUserUUID(player.uniqueId());
    }

    @Override
    public Supplier<SpawnType> getSpawnTypeForTransaction(
        final BasicPacketContext context, final Entity entityToSpawn
    ) {
        final ItemStack itemStack = context.getItemUsed();
        return itemStack.type() instanceof SpawnEggItem ? SpawnTypes.SPAWN_EGG : SpawnTypes.PLACEMENT;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void unwind(final BasicPacketContext context) {
        final net.minecraft.server.level.ServerPlayer player = context.getPacketPlayer();
        final ItemStack itemStack = context.getItemUsed();
        final ItemStackSnapshot snapshot = context.getItemUsedSnapshot();
        // We can rely on TrackingUtil.processBlockCaptures because it checks for empty contexts.
        // Swap the items used, the item used is what we want to "restore" it to the player
        final InteractionHand hand = (InteractionHand) (Object) context.getHandUsed();
        final net.minecraft.world.item.ItemStack replaced = player.getItemInHand(hand);
        player.setItemInHand(hand, ItemStackUtil.toNative(itemStack.copy()));
        if (!TrackingUtil.processBlockCaptures(context) && !snapshot.isEmpty()) {
            PacketPhaseUtil.handlePlayerSlotRestore(player, ItemStackUtil.toNative(itemStack), hand);
        } else {
            player.setItemInHand(hand, replaced);
        }

        final TrackedInventoryBridge trackedInventory = (TrackedInventoryBridge) player.containerMenu;
        trackedInventory.bridge$setCaptureInventory(false);
        trackedInventory.bridge$getCapturedSlotTransactions().clear();
    }

}
