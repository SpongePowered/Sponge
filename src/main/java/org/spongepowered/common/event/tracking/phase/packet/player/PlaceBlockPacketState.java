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
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.packet.BasicPacketContext;
import org.spongepowered.common.event.tracking.phase.packet.BasicPacketState;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.IMixinContainer;
import org.spongepowered.common.interfaces.block.IMixinBlockEventData;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.item.inventory.util.ContainerUtil;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.mixin.optimization.world.MixinWorldServer_Async_Lighting;
import org.spongepowered.common.registry.type.ItemTypeRegistryModule;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.BlockChange;
import org.spongepowered.common.world.SpongeLocatableBlockBuilder;

import java.util.List;
import java.util.stream.Collectors;

public final class PlaceBlockPacketState extends BasicPacketState {

    @Override
    public boolean isInteraction() {
        return true;
    }

    @Override
    public void populateContext(EntityPlayerMP playerMP, Packet<?> packet, BasicPacketContext context) {
        final CPacketPlayerTryUseItemOnBlock placeBlock = (CPacketPlayerTryUseItemOnBlock) packet;
        final net.minecraft.item.ItemStack itemUsed = playerMP.getHeldItem(placeBlock.getHand());
        final ItemStack itemstack = ItemStackUtil.cloneDefensive(itemUsed);
        context.itemUsed(itemstack);

    }

    @Override
    public void postBlockTransactionApplication(BlockChange blockChange, Transaction<BlockSnapshot> transaction,
        BasicPacketContext context) {
        Player player = Sponge.getCauseStackManager().getCurrentCause().first(Player.class).get();
        final Location<World> location = transaction.getFinal().getLocation().get();
        BlockPos pos = VecHelper.toBlockPos(location);
        IMixinChunk spongeChunk = (IMixinChunk) ((WorldServer) location.getExtent()).getChunk(pos);
        if (blockChange == BlockChange.PLACE) {
            spongeChunk.addTrackedBlockPosition((Block) transaction.getFinal().getState().getType(), pos, player, PlayerTracker.Type.OWNER);
        }
        spongeChunk.addTrackedBlockPosition((Block) transaction.getFinal().getState().getType(), pos, player, PlayerTracker.Type.NOTIFIER);
    }

    @Override
    public void appendNotifierToBlockEvent(BasicPacketContext context, IMixinWorldServer mixinWorldServer, BlockPos pos, IMixinBlockEventData blockEvent) {
        final Player player = Sponge.getCauseStackManager().getCurrentCause().first(Player.class).get();
        final BlockState state = ((World) mixinWorldServer).getBlock(pos.getX(), pos.getY(), pos.getZ());
        final LocatableBlock locatable = new SpongeLocatableBlockBuilder().world((World) mixinWorldServer).position(pos.getX(), pos.getY(), pos.getZ()).state(state).build();

        blockEvent.setTickBlock(locatable);
        blockEvent.setSourceUser(player);
    }

    @Override
    public void unwind(BasicPacketContext context) {
        final Packet<?> packet = context.getPacket();
        final EntityPlayerMP player = context.getPacketPlayer();
        final IMixinWorldServer mixinWorld = (IMixinWorldServer) player.world;

        // Note - CPacketPlayerTryUseItem is swapped with
        // CPacketPlayerBlockPlacement
        final ItemStack itemStack = context.getItemUsed();
        final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(itemStack);
        context.getCapturedEntitySupplier()
            .acceptAndClearIfNotEmpty(entities -> {
                try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                    frame.pushCause(player);
                    frame.pushCause(snapshot);
                    frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.SPAWN_EGG);
                    SpongeCommonEventFactory.callSpawnEntity(entities, context);
                }
            });
        if (!context.getCapturedBlockSupplier().isEmpty()) {
            try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(player);
                if (!TrackingUtil.processBlockCaptures(context.getCapturedBlockSupplier(), this, context) && snapshot != ItemTypeRegistryModule.NONE_SNAPSHOT) {
                    frame.pushCause(player);
                    EnumHand hand = ((CPacketPlayerTryUseItemOnBlock) packet).getHand();
                    PacketPhaseUtil.handlePlayerSlotRestore(player, ItemStackUtil.toNative(itemStack), hand);

                }
            }
        }
        context.getCapturedItemStackSupplier().acceptAndClearIfNotEmpty(drops -> {
            final List<Entity> entities =
                drops.stream().map(drop -> drop.create(player.getServerWorld())).map(EntityUtil::fromNative)
                    .collect(Collectors.toList());
            if (!entities.isEmpty()) {
                try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                    frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLACEMENT);
                    frame.pushCause(player);
                    SpongeCommonEventFactory.callDropItemCustom(entities, context, EntityUtil.ENTITY_CREATOR_FUNCTION.apply(context));

                }
            }

        });

        final IMixinContainer mixinContainer = ContainerUtil.toMixin(player.openContainer);
        mixinContainer.setCaptureInventory(false);
        mixinContainer.getCapturedTransactions().clear();
    }
}
