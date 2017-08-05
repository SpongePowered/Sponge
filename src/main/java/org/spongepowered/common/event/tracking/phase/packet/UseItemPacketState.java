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
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.world.IMixinLocation;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;
import org.spongepowered.common.world.BlockChange;

import javax.annotation.Nullable;

class UseItemPacketState extends BasicPacketState {

    @Override
    public boolean isInteraction() {
        return true;
    }

    @Override
    public void unwind(Packet<?> packet, EntityPlayerMP player, PhaseContext context) {
        final ItemStack itemStack = context.firstNamed(InternalNamedCauses.Packet.ITEM_USED, ItemStack.class).orElse(null);
        final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(itemStack);
        context.getCapturedEntitySupplier()
                .ifPresentAndNotEmpty(entities -> {
                    final Cause cause = Cause.source(EntitySpawnCause.builder()
                            .entity(EntityUtil.fromNative(player))
                            .type(itemStack.getType() == ItemTypes.SPAWN_EGG ? InternalSpawnTypes.SPAWN_EGG : InternalSpawnTypes.PLACEMENT)
                            .build())
                            .named(NamedCause.of(InternalNamedCauses.Packet.ITEM_USED, snapshot))
                            .build();
                    final SpawnEntityEvent spawnEntityEvent = SpongeEventFactory.createSpawnEntityEvent(cause, entities);
                    SpongeImpl.postEvent(spawnEntityEvent);
                    if (!spawnEntityEvent.isCancelled()) {
                        TrackingUtil.processSpawnedEntities(player, spawnEntityEvent);
                    }
                });
        context.getCapturedBlockSupplier()
                .ifPresentAndNotEmpty(
                        originalBlocks -> TrackingUtil.processBlockCaptures(originalBlocks, this, context));

    }

    @Override
    public void populateContext(EntityPlayerMP playerMP, Packet<?> packet, PhaseContext context) {
        final CPacketPlayerTryUseItem placeBlock = (CPacketPlayerTryUseItem) packet;
        final net.minecraft.item.ItemStack usedItem = playerMP.getHeldItem(placeBlock.getHand());
        final ItemStack itemstack = ItemStackUtil.cloneDefensive(usedItem);
        if (itemstack != null) {
            // unused, to be removed and re-located when phase context is cleaned up
            //context.add(NamedCause.of(InternalNamedCauses.Packet.HAND_USED, placeBlock.getHand()));
            context.add(NamedCause.of(InternalNamedCauses.Packet.ITEM_USED, itemstack));
        }

        context.addEntityCaptures()
               .addEntityDropCaptures()
               .addBlockCaptures();
    }

    @Override
    public void handleBlockChangeWithUser(@Nullable BlockChange blockChange, Transaction<BlockSnapshot> transaction,
        PhaseContext context) {
        Player player = context.first(Player.class).get();
        BlockPos pos = ((IMixinLocation) (Object) transaction.getFinal().getLocation().get()).getBlockPos();
        IMixinChunk spongeChunk = (IMixinChunk) EntityUtil.getMinecraftWorld(player).getChunkFromBlockCoords(pos);
        if (blockChange == BlockChange.PLACE) {
            spongeChunk.addTrackedBlockPosition((Block) transaction.getFinal().getState().getType(), pos, player, PlayerTracker.Type.OWNER);
        }

        spongeChunk.addTrackedBlockPosition((Block) transaction.getFinal().getState().getType(), pos, player, PlayerTracker.Type.NOTIFIER);
    }
}
