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

import org.spongepowered.api.block.transaction.BlockTransactionReceipt;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.cause.entity.SpawnType;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.common.bridge.world.level.chunk.LevelChunkBridge;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.packet.BasicPacketContext;
import org.spongepowered.common.event.tracking.phase.packet.BasicPacketState;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.BlockChange;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public final class UseItemPacketState extends BasicPacketState {

    private BiConsumer<CauseStackManager.StackFrame, BasicPacketContext> BASIC_PACKET_MODIFIER =
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

    @SuppressWarnings("ConstantConditions")
    @Override
    public void populateContext(
        final net.minecraft.server.level.ServerPlayer playerMP, final Packet<?> packet, final BasicPacketContext context) {
        final ServerboundUseItemPacket placeBlock = (ServerboundUseItemPacket) packet;
        final net.minecraft.world.item.ItemStack usedItem = playerMP.getItemInHand(placeBlock.getHand());
        final ItemStack itemstack = ItemStackUtil.cloneDefensive(usedItem);
        context.itemUsed(itemstack);
        final HandType handType = (HandType) (Object) placeBlock.getHand();
        context.handUsed(handType);
    }

    @Override
    public void postBlockTransactionApplication(
        final BasicPacketContext context, final BlockChange blockChange,
        final BlockTransactionReceipt transaction
    ) {
        final ServerPlayer player = context.getSpongePlayer();
        final BlockPos pos = VecHelper.toBlockPos(transaction.finalBlock().position());
        final LevelChunkBridge spongeChunk = (LevelChunkBridge) ((ServerLevel) player.world()).getChunkAt(pos);
        if (blockChange == BlockChange.PLACE) {
            spongeChunk.bridge$addTrackedBlockPosition((Block) transaction.finalBlock().state().type(), pos, player.user(), PlayerTracker.Type.CREATOR);
        }

        spongeChunk.bridge$addTrackedBlockPosition((Block) transaction.finalBlock().state().type(), pos, player.user(), PlayerTracker.Type.NOTIFIER);
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
        final ItemStackSnapshot snapshot = context.getItemUsedSnapshot();
        final HandType hand = context.getHandUsed();
        final ItemStack itemStack = context.getItemUsed();
        final boolean success = TrackingUtil.processBlockCaptures(context);
        if (!success && snapshot.isEmpty()) {
            PhaseTracker.getCauseStackManager().pushCause(player);
            PacketPhaseUtil.handlePlayerSlotRestore(player, ItemStackUtil.toNative(itemStack), (InteractionHand) (Object) hand);
        }
    }
}
