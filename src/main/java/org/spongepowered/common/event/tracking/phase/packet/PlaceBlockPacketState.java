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
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.IMixinContainer;
import org.spongepowered.common.interfaces.block.IMixinBlockEventData;
import org.spongepowered.common.interfaces.world.IMixinLocation;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.item.inventory.util.ContainerUtil;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.registry.type.ItemTypeRegistryModule;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;
import org.spongepowered.common.world.BlockChange;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

class PlaceBlockPacketState extends BasicPacketState {

    @Override
    public boolean isInteraction() {
        return true;
    }

    @Override
    public void unwind(Packet<?> packet, EntityPlayerMP player, PhaseContext context) {
        final IMixinWorldServer mixinWorld = (IMixinWorldServer) player.world;

        // Note - CPacketPlayerTryUseItem is swapped with CPacketPlayerBlockPlacement
        final ItemStack itemStack = context.firstNamed(InternalNamedCauses.Packet.ITEM_USED, ItemStack.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected the used item stack to place a block, but got nothing!", context));
        final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(itemStack);
        context.getCapturedEntitySupplier()
                .ifPresentAndNotEmpty(entities -> {
                    final Cause cause = Cause.source(EntitySpawnCause.builder()
                            .entity(EntityUtil.fromNative(player))
                            .type(InternalSpawnTypes.SPAWN_EGG)
                            .build())
                            .named(NamedCause.of(InternalNamedCauses.Packet.ITEM_USED, snapshot))
                            .build();
                    final SpawnEntityEvent spawnEntityEvent = SpongeEventFactory.createSpawnEntityEvent(cause, entities);
                    SpongeImpl.postEvent(spawnEntityEvent);
                    if (!spawnEntityEvent.isCancelled()) {
                        PacketPhaseUtil.processSpawnedEntities(player, spawnEntityEvent);

                    }
                });
        context.getCapturedBlockSupplier()
                .ifPresentAndNotEmpty(
                        originalBlocks -> {
                            boolean success = TrackingUtil.processBlockCaptures(originalBlocks, this,
                                    context);
                            if (!success && snapshot != ItemTypeRegistryModule.NONE_SNAPSHOT) {
                                EnumHand hand = ((CPacketPlayerTryUseItemOnBlock) packet).getHand();
                                PacketPhaseUtil.handlePlayerSlotRestore(player, (net.minecraft.item.ItemStack) itemStack, hand);
                            }
                        });
        context.getCapturedItemStackSupplier().ifPresentAndNotEmpty(drops -> {
            final List<EntityItem>
                    items =
                    drops.stream().map(drop -> drop.create(player.getServerWorld())).collect(Collectors.toList());
            final Cause cause = Cause.source(
                    EntitySpawnCause.builder()
                            .entity((Entity) player)
                            .type(InternalSpawnTypes.PLACEMENT)
                            .build()
            ).named(NamedCause.notifier(player))
                    .build();
            final List<Entity> entities = items
                    .stream()
                    .map(EntityUtil::fromNative)
                    .collect(Collectors.toList());
            if (!entities.isEmpty()) {
                DropItemEvent.Custom event = SpongeEventFactory.createDropItemEventCustom(cause, entities);
                SpongeImpl.postEvent(event);
                if (!event.isCancelled()) {
                    for (Entity droppedItem : event.getEntities()) {
                        droppedItem.setCreator(player.getUniqueID());
                        mixinWorld.forceSpawnEntity(droppedItem);
                    }
                }
            }

        });

        final IMixinContainer mixinContainer = ContainerUtil.toMixin(player.openContainer);
        mixinContainer.setCaptureInventory(false);
        mixinContainer.getCapturedTransactions().clear();
    }

    @Override
    public void populateContext(EntityPlayerMP playerMP, Packet<?> packet, PhaseContext context) {
        final CPacketPlayerTryUseItemOnBlock placeBlock = (CPacketPlayerTryUseItemOnBlock) packet;
        final net.minecraft.item.ItemStack itemUsed = playerMP.getHeldItem(placeBlock.getHand());
        final ItemStack itemstack = ItemStackUtil.cloneDefensive(itemUsed);
        if (itemstack != null) {
            context.add(NamedCause.of(InternalNamedCauses.Packet.ITEM_USED, itemstack));
        } else {
            context.add(NamedCause.of(InternalNamedCauses.Packet.ITEM_USED, ItemTypeRegistryModule.NONE));
        }
        // unused, to be removed and re-located when phase context is cleaned up
        //context.add(NamedCause.of(InternalNamedCauses.Packet.PLACED_BLOCK_POSITION, placeBlock.getPos()));
        //context.add(NamedCause.of(InternalNamedCauses.Packet.PLACED_BLOCK_FACING, placeBlock.getDirection()));

        context
                .addBlockCaptures()
                .addEntityCaptures()
                .addEntityDropCaptures();
    }

    @Override
    public void handleBlockChangeWithUser(@Nullable BlockChange blockChange, Transaction<BlockSnapshot> transaction,
        PhaseContext context) {
        Player player = context.first(Player.class).get();
        final Location<World> location = transaction.getFinal().getLocation().get();
        BlockPos pos = ((IMixinLocation) (Object) location).getBlockPos();
        IMixinChunk spongeChunk = (IMixinChunk) ((WorldServer) location.getExtent()).getChunkFromBlockCoords(pos);
        if (blockChange == BlockChange.PLACE) {
            spongeChunk.addTrackedBlockPosition((Block) transaction.getFinal().getState().getType(), pos, player, PlayerTracker.Type.OWNER);
        }
        spongeChunk.addTrackedBlockPosition((Block) transaction.getFinal().getState().getType(), pos, player, PlayerTracker.Type.NOTIFIER);
    }

    @Override
    public void associateBlockEventNotifier(PhaseContext context, IMixinWorldServer mixinWorldServer, BlockPos pos, IMixinBlockEventData blockEvent) {
        final Player player = context.first(Player.class).get();
        final Location<World> location = new Location<>(player.getWorld(), pos.getX(), pos.getY(), pos.getZ());
        final LocatableBlock locatableBlock = LocatableBlock.builder()
                .location(location)
                .state(location.getBlock())
                .build();

        blockEvent.setTickBlock(locatableBlock);
        blockEvent.setSourceUser(player);
    }
}
