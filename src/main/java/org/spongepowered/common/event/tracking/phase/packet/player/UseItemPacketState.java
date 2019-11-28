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

import net.minecraft.block.Block;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.packet.BasicPacketContext;
import org.spongepowered.common.event.tracking.phase.packet.BasicPacketState;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.item.inventory.SpongeItemStackSnapshot;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.BlockChange;

import java.util.function.BiConsumer;

@SuppressWarnings("unchecked")
public final class UseItemPacketState extends BasicPacketState {

    private BiConsumer<CauseStackManager.StackFrame, BasicPacketContext> BASIC_PACKET_MODIFIER =
            ((BiConsumer<CauseStackManager.StackFrame, BasicPacketContext>) IPhaseState.DEFAULT_OWNER_NOTIFIER)
                    .andThen((frame, ctx) -> {
                        frame.addContext(EventContextKeys.PLAYER_PLACE, ctx.getSpongePlayer().getWorld());
                        frame.addContext(EventContextKeys.USED_HAND, ctx.getHandUsed());
                        frame.addContext(EventContextKeys.USED_ITEM, ctx.getItemUsedSnapshot());
                        final ItemStack itemStack = ctx.getItemUsed();
                        frame.addContext(EventContextKeys.SPAWN_TYPE,
                                itemStack.getType() == ItemTypes.SPAWN_EGG ? SpawnTypes.SPAWN_EGG : SpawnTypes.PLACEMENT);
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
    public void populateContext(ServerPlayerEntity playerMP, IPacket<?> packet, BasicPacketContext context) {
        final CPlayerTryUseItemPacket placeBlock = (CPlayerTryUseItemPacket) packet;
        final net.minecraft.item.ItemStack usedItem = playerMP.func_184586_b(placeBlock.func_187028_a());
        final ItemStack itemstack = ItemStackUtil.cloneDefensive(usedItem);
        context.itemUsed(itemstack);
        final HandType handType = (HandType) (Object) placeBlock.func_187028_a();
        context.handUsed(handType);
    }

    @Override
    public void postBlockTransactionApplication(BlockChange blockChange, Transaction<? extends BlockSnapshot> transaction,
        BasicPacketContext context) {
        Player player = context.getSpongePlayer();
        BlockPos pos = VecHelper.toBlockPos(transaction.getFinal().getLocation().get());
        ChunkBridge spongeChunk = (ChunkBridge) ((ServerWorld) player.getWorld()).func_175726_f(pos);
        if (blockChange == BlockChange.PLACE) {
            spongeChunk.bridge$addTrackedBlockPosition((Block) transaction.getFinal().getState().getType(), pos, player, PlayerTracker.Type.OWNER);
        }

        spongeChunk.bridge$addTrackedBlockPosition((Block) transaction.getFinal().getState().getType(), pos, player, PlayerTracker.Type.NOTIFIER);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void unwind(BasicPacketContext context) {
        final ServerPlayerEntity player = context.getPacketPlayer();
        final ItemStack itemStack = context.getItemUsed();
        final SpongeItemStackSnapshot snapshot = context.getItemUsedSnapshot();
        final HandType hand = context.getHandUsed();
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE,
                itemStack.getType() == ItemTypes.SPAWN_EGG ? SpawnTypes.SPAWN_EGG : SpawnTypes.PLACEMENT);
            context.getCapturedEntitySupplier()
                .acceptAndClearIfNotEmpty(entities -> {
                    SpongeCommonEventFactory.callSpawnEntity(entities, context);
                });
            if (!context.getCapturedBlockSupplier().isEmpty()) {
                // TODO - Determine if we need to pass the supplier or perform some parameterized
                //  process if not empty method on the capture object.
                boolean success = TrackingUtil.processBlockCaptures(context);
                if (!success && snapshot.isNone()) {
                    Sponge.getCauseStackManager().pushCause(player);
                    PacketPhaseUtil.handlePlayerSlotRestore(player, (net.minecraft.item.ItemStack) itemStack, (Hand) (Object) hand);
                }
            }
        }
    }
}
