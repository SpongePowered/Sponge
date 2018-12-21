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
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.packet.BasicPacketContext;
import org.spongepowered.common.event.tracking.phase.packet.BasicPacketState;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.registry.type.ItemTypeRegistryModule;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.BlockChange;

public final class UseItemPacketState extends BasicPacketState {

    @Override
    public boolean isInteraction() {
        return true;
    }

    @Override
    public void populateContext(EntityPlayerMP playerMP, Packet<?> packet, BasicPacketContext context) {
        final CPacketPlayerTryUseItem placeBlock = (CPacketPlayerTryUseItem) packet;
        final net.minecraft.item.ItemStack usedItem = playerMP.getHeldItem(placeBlock.getHand());
        final ItemStack itemstack = ItemStackUtil.cloneDefensive(usedItem);
        context.itemUsed(itemstack);
    }

    @Override
    public void postBlockTransactionApplication(BlockChange blockChange, Transaction<BlockSnapshot> transaction,
        BasicPacketContext context) {
        Player player = context.getSpongePlayer();
        BlockPos pos = VecHelper.toBlockPos(transaction.getFinal().getLocation().get());
        IMixinChunk spongeChunk = (IMixinChunk) EntityUtil.getMinecraftWorld(player).getChunk(pos);
        if (blockChange == BlockChange.PLACE) {
            spongeChunk.addTrackedBlockPosition((Block) transaction.getFinal().getState().getType(), pos, player, PlayerTracker.Type.OWNER);
        }

        spongeChunk.addTrackedBlockPosition((Block) transaction.getFinal().getState().getType(), pos, player, PlayerTracker.Type.NOTIFIER);
    }

    @Override
    public void unwind(BasicPacketContext context) {
        final EntityPlayerMP player = context.getPacketPlayer();
        final ItemStack itemStack = context.getItemUsed();
        final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(itemStack);
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(player);
            frame.pushCause(snapshot);
            frame.addContext(EventContextKeys.SPAWN_TYPE,
                itemStack.getType() == ItemTypes.SPAWN_EGG ? SpawnTypes.SPAWN_EGG : SpawnTypes.PLACEMENT);
            context.getCapturedEntitySupplier()
                .acceptAndClearIfNotEmpty(entities -> {
                    SpongeCommonEventFactory.callSpawnEntity(entities, context);
                });
            if (!context.getCapturedBlockSupplier().isEmpty()) {
                // TODO - Determine if we need to pass the supplier or perform some parameterized
                //  process if not empty method on the capture object.
                boolean success = TrackingUtil.processBlockCaptures(context.getCapturedBlockSupplier(), this, context);
                if (!success && snapshot != ItemTypeRegistryModule.NONE_SNAPSHOT) {
                    Sponge.getCauseStackManager().pushCause(player);
                    EnumHand hand = ((CPacketPlayerTryUseItem) context.getPacket()).getHand();
                    PacketPhaseUtil.handlePlayerSlotRestore(player, (net.minecraft.item.ItemStack) itemStack, hand);
                }
            }
        }
    }
}
