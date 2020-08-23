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

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Transaction;
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
import org.spongepowered.common.bridge.block.BlockEventDataBridge;
import org.spongepowered.common.bridge.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.packet.BasicPacketContext;
import org.spongepowered.common.event.tracking.phase.packet.BasicPacketState;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.world.BlockChange;
import org.spongepowered.common.world.SpongeLocatableBlockBuilder;

import java.util.function.BiConsumer;

@SuppressWarnings("unchecked")
public final class PlaceBlockPacketState extends BasicPacketState {

    private BiConsumer<CauseStackManager.StackFrame, BasicPacketContext> BASIC_PACKET_MODIFIER =
            ((BiConsumer<CauseStackManager.StackFrame, BasicPacketContext>) IPhaseState.DEFAULT_OWNER_NOTIFIER)
                    .andThen((frame, ctx) -> {
                        frame.addContext(EventContextKeys.PLAYER_PLACE, ctx.getSpongePlayer().getWorld());
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
    public void populateContext(final ServerPlayerEntity playerMP, final IPacket<?> packet, final BasicPacketContext context) {
        final CPlayerTryUseItemOnBlockPacket placeBlock = (CPlayerTryUseItemOnBlockPacket) packet;
        final net.minecraft.item.ItemStack itemUsed = playerMP.getHeldItem(placeBlock.getHand());
        final ItemStack itemstack = ItemStackUtil.cloneDefensive(itemUsed);
        context.itemUsed(itemstack);
        final HandType handType = (HandType) (Object) placeBlock.getHand();
        context.handUsed(handType);
    }

    @Override
    public void postBlockTransactionApplication(final BlockChange blockChange, final Transaction<? extends BlockSnapshot> transaction,
        final BasicPacketContext context) {
        TrackingUtil.associateTrackerToTarget(blockChange, transaction, ((ServerPlayer) context.getPacketPlayer()).getUser());
    }

    @Override
    public void appendNotifierToBlockEvent(final BasicPacketContext context, final PhaseContext<?> currentContext,
                                           final ServerWorldBridge mixinWorldServer, final BlockPos pos, final BlockEventDataBridge blockEvent) {
        final Player player = PhaseTracker.getCauseStackManager().getCurrentCause().first(Player.class).get();
        final BlockState state = ((ServerWorld) mixinWorldServer).getBlock(pos.getX(), pos.getY(), pos.getZ());
        final LocatableBlock locatable =
                new SpongeLocatableBlockBuilder().world((ServerWorld) mixinWorldServer).position(pos.getX(), pos.getY(), pos.getZ()).state(state).build();

        blockEvent.bridge$setTickingLocatable(locatable);
        blockEvent.bridge$setSourceUser(((ServerPlayer)player).getUser());
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void unwind(final BasicPacketContext context) {
        final ServerPlayerEntity player = context.getPacketPlayer();
        final ItemStack itemStack = context.getItemUsed();
        final ItemStackSnapshot snapshot = context.getItemUsedSnapshot();
//        context.getCapturedEntitySupplier()
//            .acceptAndClearIfNotEmpty(entities -> {
//                try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
//                    frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.SPAWN_EGG);
//                    SpongeCommonEventFactory.callSpawnEntity(entities, context);
//                }
//            });
        // We can rely on TrackingUtil.processBlockCaptures because it checks for empty contexts.
        // Swap the items used, the item used is what we want to "restore" it to the player
        final Hand hand = (Hand) (Object) context.getHandUsed();
        final net.minecraft.item.ItemStack replaced = player.getHeldItem(hand);
        player.setHeldItem(hand, ItemStackUtil.toNative(itemStack.copy()));
        if (!TrackingUtil.processBlockCaptures(context) && !snapshot.isEmpty()) {
            PacketPhaseUtil.handlePlayerSlotRestore(player, ItemStackUtil.toNative(itemStack), hand);
        } else {
            player.setHeldItem(hand, replaced);
        }

        final TrackedInventoryBridge trackedInventory = (TrackedInventoryBridge) player.openContainer;
        trackedInventory.bridge$setCaptureInventory(false);
        trackedInventory.bridge$getCapturedSlotTransactions().clear();
    }

    @Override
    public SpawnType getEntitySpawnType(final BasicPacketContext context) {
        if (context.getItemUsed().getType() instanceof SpawnEggItem) {
            return SpawnTypes.SPAWN_EGG.get();
        }
        // Some other items directly cause entities to be spawned, such as
        // ender crystals. Default to PLACEMENT for those.
        return SpawnTypes.PLACEMENT.get();
    }

}
