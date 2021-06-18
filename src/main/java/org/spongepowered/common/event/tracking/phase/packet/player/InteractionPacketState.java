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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.common.accessor.world.entity.LivingEntityAccessor;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.bridge.world.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.bridge.world.TrackedWorldBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.packet.PacketState;
import org.spongepowered.common.item.util.ItemStackUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import java.util.List;

public final class InteractionPacketState extends PacketState<InteractionPacketContext> {


    @Override
    public InteractionPacketContext createNewContext(final PhaseTracker tracker) {
        return new InteractionPacketContext(this, tracker);
    }

    @Override
    public boolean isInteraction() {
        return true;
    }

    @Override
    public void populateContext(final ServerPlayer playerMP, final Packet<?> packet, final InteractionPacketContext context) {
        final ItemStack stack = ItemStackUtil.cloneDefensive(playerMP.getMainHandItem());
        if (stack != null) {
            context.itemUsed(stack);
        }
        final ItemStack itemInUse = ItemStackUtil.cloneDefensive(playerMP.getUseItem());
        if (itemInUse != null) {
            context.activeItem(itemInUse);
        }
        final BlockPos target = ((ServerboundPlayerActionPacket) packet).getPos();
        if (!playerMP.level.isLoaded(target)) {
            context.targetBlock(BlockSnapshot.empty());
        } else {
            context.targetBlock(((TrackedWorldBridge) playerMP.level).bridge$createSnapshot(target, BlockChangeFlags.NONE));
        }
        context.handUsed(HandTypes.MAIN_HAND.get());
    }

    @Override
    public boolean shouldCaptureEntity() {
        return true;
    }

    @Override
    public boolean doesCaptureNeighborNotifications(final InteractionPacketContext context) {
        return true;
    }

    @Override
    public void unwind(final InteractionPacketContext phaseContext) {

        final ServerPlayer player = phaseContext.getPacketPlayer();
        final ItemStack usedStack = phaseContext.getItemUsed();
        final HandType usedHand = phaseContext.getHandUsed();
        final ItemStackSnapshot usedSnapshot = ItemStackUtil.snapshotOf(usedStack);
        final Entity spongePlayer = (Entity) player;
        final BlockSnapshot targetBlock = phaseContext.getTargetBlock();
        
        final net.minecraft.world.item.ItemStack endActiveItem = player.getUseItem();
        ((LivingEntityAccessor) player).accessor$useItem(ItemStackUtil.toNative(phaseContext.getActiveItem()));

        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(spongePlayer);
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
            frame.addContext(EventContextKeys.USED_ITEM, usedSnapshot);
            frame.addContext(EventContextKeys.USED_HAND, usedHand);
            frame.addContext(EventContextKeys.BLOCK_HIT, targetBlock);
            final boolean hasBlocks = !phaseContext.getTransactor().isEmpty();
            if (hasBlocks) {
                if (!TrackingUtil.processBlockCaptures(phaseContext)) {
                    return;
                }
            }

            final TrackedInventoryBridge trackedInventory = (TrackedInventoryBridge) player.containerMenu;
            trackedInventory.bridge$setCaptureInventory(false);
            trackedInventory.bridge$getCapturedSlotTransactions().clear();
        }
        
        ((LivingEntityAccessor) player).accessor$useItem(endActiveItem);
    }

}
