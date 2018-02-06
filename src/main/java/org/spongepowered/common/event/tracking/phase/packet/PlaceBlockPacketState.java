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
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.ShouldFire;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

class PlaceBlockPacketState extends BasicPacketState {

    @Override
    public boolean isInteraction() {
        return true;
    }

    @Override
    public void populateContext(EntityPlayerMP playerMP, Packet<?> packet, BasicPacketContext context) {
        ContainerUtil.toMixin(playerMP.inventoryContainer).setCaptureInventory(true);
        final CPacketPlayerTryUseItemOnBlock placeBlock = (CPacketPlayerTryUseItemOnBlock) packet;
        final net.minecraft.item.ItemStack itemUsed = playerMP.getHeldItem(placeBlock.getHand());
        final ItemStack itemstack = ItemStackUtil.cloneDefensive(itemUsed);
        context.itemUsed(itemstack);

    }

    @Override public boolean shouldCaptureEntity() {
        return true;
    }

    @Override
    public void handleBlockChangeWithUser(@Nullable BlockChange blockChange, Transaction<BlockSnapshot> transaction,
        BasicPacketContext context) {
        Player player = Sponge.getCauseStackManager().getCurrentCause().first(Player.class).get();
        final Location<World> location = transaction.getFinal().getLocation().get();
        BlockPos pos = ((IMixinLocation) (Object) location).getBlockPos();
        IMixinChunk spongeChunk = (IMixinChunk) ((WorldServer) location.getExtent()).getChunkFromBlockCoords(pos);
        if (blockChange == BlockChange.PLACE) {
            spongeChunk.addTrackedBlockPosition((Block) transaction.getFinal().getState().getType(), pos, player, PlayerTracker.Type.OWNER);
        }
        spongeChunk.addTrackedBlockPosition((Block) transaction.getFinal().getState().getType(), pos, player, PlayerTracker.Type.NOTIFIER);
    }

    @Override
    public void addNotifierToBlockEvent(BasicPacketContext context, IMixinWorldServer mixinWorldServer, BlockPos pos, IMixinBlockEventData blockEvent) {
        final Player player = Sponge.getCauseStackManager().getCurrentCause().first(Player.class).get();
        final Location<World> location = new Location<>(player.getWorld(), pos.getX(), pos.getY(), pos.getZ());
        final LocatableBlock locatableBlock = LocatableBlock.builder()
                .location(location)
                .state(location.getBlock())
                .build();

        blockEvent.setTickBlock(locatableBlock);
        blockEvent.setSourceUser(player);
    }

    @Override
    public void unwind(BasicPacketContext context) {
        final Packet<?> packet = context.getPacket();
        final EntityPlayerMP player = context.getPacketPlayer();
        final IMixinContainer mixinContainer = ContainerUtil.toMixin(player.inventoryContainer);
        mixinContainer.detectAndSendChanges(false);
        final IMixinWorldServer mixinWorld = (IMixinWorldServer) player.world;

        final ItemStack itemStack = context.getItemUsed();
        final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(itemStack);
        context.getCapturedEntitySupplier()
                .acceptAndClearIfNotEmpty(entities -> {
                    try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                        List<Projectile> projectiles = new ArrayList<>();
                        List<Entity> rest = new ArrayList<>();
                        for (Entity entity : entities) {
                            if (entity instanceof Projectile) {
                                projectiles.add((Projectile) entity);
                            } else {
                                rest.add(entity);
                            }
                        }

                        if (!rest.isEmpty()) {
                            if(ShouldFire.SPAWN_ENTITY_EVENT) {
                                frame.addContext(EventContextKeys.SPAWN_TYPE, itemStack.getType() == ItemTypes.SPAWN_EGG
                                        ? InternalSpawnTypes.SPAWN_EGG : InternalSpawnTypes.PLACEMENT);
                                SpawnEntityEvent spawnEntityEvent = SpongeEventFactory.createSpawnEntityEvent(frame.getCurrentCause(), rest);
                                if (!SpongeImpl.postEvent(spawnEntityEvent)) {
                                    processSpawnedEntities(player, spawnEntityEvent);
                                }
                            } else {
                                processEntities(player, rest);
                            }
                        }

                        if (!projectiles.isEmpty()) {
                            PacketPhaseUtil.fireProjectileLaunchEvent(frame, player, projectiles);
                        }
                    }
                });
        context.getCapturedBlockSupplier()
            .acceptAndClearIfNotEmpty(
                originalBlocks -> {
                    Sponge.getCauseStackManager().pushCause(player);
                    boolean success = TrackingUtil.processBlockCaptures(originalBlocks, this, context);
                    if (!success && snapshot != ItemTypeRegistryModule.NONE_SNAPSHOT) {
                        Sponge.getCauseStackManager().pushCause(player);
                        EnumHand hand = ((CPacketPlayerTryUseItemOnBlock) packet).getHand();
                        PacketPhaseUtil.handlePlayerSlotRestore(player, (net.minecraft.item.ItemStack) itemStack, hand);
                    }
                    Sponge.getCauseStackManager().popCause();
                });
        context.getCapturedItemStackSupplier().acceptAndClearIfNotEmpty(drops -> {
            final List<Entity> entities =
                drops.stream().map(drop -> drop.create(player.getServerWorld())).map(EntityUtil::fromNative)
                    .collect(Collectors.toList());
            if (!entities.isEmpty()) {
                try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                    Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLACEMENT);
                    Sponge.getCauseStackManager().pushCause(player);
                    DropItemEvent.Custom event =
                        SpongeEventFactory.createDropItemEventCustom(Sponge.getCauseStackManager().getCurrentCause(), entities);
                    SpongeImpl.postEvent(event);
                    if (!event.isCancelled()) {
                        for (Entity droppedItem : event.getEntities()) {
                            droppedItem.setCreator(player.getUniqueID());
                            mixinWorld.forceSpawnEntity(droppedItem);
                        }
                    }
                }
            }

        });

        mixinContainer.setCaptureInventory(false);
        mixinContainer.getCapturedTransactions().clear();
    }
}
