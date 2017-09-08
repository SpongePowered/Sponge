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

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.network.Packet;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.ItemDropData;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.block.BlockPhase;
import org.spongepowered.common.interfaces.IMixinContainer;
import org.spongepowered.common.item.inventory.util.ContainerUtil;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;
import org.spongepowered.common.util.VecHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

final class InteractionPacketState extends BasicPacketState {


    @Override
    public boolean isInteraction() {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void unwind(Packet<?> packet, EntityPlayerMP player, PhaseContext context) {
        final ItemStack usedStack = context.firstNamed(InternalNamedCauses.Packet.ITEM_USED, ItemStack.class)
                .orElse(null);
        final ItemStackSnapshot usedSnapshot = ItemStackUtil.snapshotOf(usedStack);
        final Entity spongePlayer = EntityUtil.fromNative(player);
        if (!context.getCapturedBlockSupplier().isEmpty()) {
            if (!TrackingUtil.processBlockCaptures(context.getCapturedBlocks(), this, context)) {
                // Stop entities like XP from being spawned
                return;
            }
        } else {
            context.getBlockItemDropSupplier().ifPresentAndNotEmpty(map -> {
                final List<BlockSnapshot> capturedBlocks = context.getCapturedBlocks();
                if (ShouldFire.DROP_ITEM_EVENT_DESTRUCT) {
                    final Cause cause = Cause.source(EntitySpawnCause.builder()
                            .entity(spongePlayer)
                            .type(InternalSpawnTypes.DROPPED_ITEM)
                            .build())
                            .build();
                    for (BlockSnapshot blockChange : capturedBlocks) {
                        final Location<World> location = blockChange.getLocation().get();
                        final Vector3d position = location.getPosition();
                        final BlockPos blockPos = VecHelper.toBlockPos(position);
                        final Collection<EntityItem> entityItems = map.get(blockPos);
                        if (!entityItems.isEmpty()) {
                            final List<Entity> items = entityItems.stream().map(EntityUtil::fromNative).collect(Collectors.toList());
                            final DropItemEvent.Destruct event =
                                    SpongeEventFactory.createDropItemEventDestruct(cause, items);
                            SpongeImpl.postEvent(event);
                            if (!event.isCancelled()) {
                                TrackingUtil.processSpawnedEntities(player, event);
                            }
                        }
                    }
                } else {
                    for (BlockSnapshot blockChange : capturedBlocks) {
                        final Location<World> location = blockChange.getLocation().get();
                        final Vector3d position = location.getPosition();
                        final BlockPos blockPos = VecHelper.toBlockPos(position);
                        final Collection<EntityItem> entityItems = map.get(blockPos);
                        if (!entityItems.isEmpty()) {
                            TrackingUtil.processEntities(player, (Collection<Entity>) (Collection<?>) entityItems);
                        }
                    }
                }
            });

        }

        context.getCapturedItemsSupplier()
                .ifPresentAndNotEmpty(items -> {
                    if (items.isEmpty()) {
                        return;
                    }
                    final Cause cause = Cause.source(EntitySpawnCause.builder()
                            .entity(spongePlayer)
                            .type(InternalSpawnTypes.DROPPED_ITEM)
                            .build())
                            .build();
                    final ArrayList<Entity> entities = new ArrayList<>();
                    for (EntityItem item : items) {
                        entities.add(EntityUtil.fromNative(item));
                    }
                    final DropItemEvent.Dispense
                            dispense =
                            SpongeEventFactory.createDropItemEventDispense(cause, entities);
                    SpongeImpl.postEvent(dispense);
                    if (!dispense.isCancelled()) {
                        TrackingUtil.processSpawnedEntities(player, dispense);
                    }
                });
        context.getCapturedEntityDropSupplier()
                .ifPresentAndNotEmpty(map -> {
                    if (map.isEmpty()) {
                        return;
                    }
                    final PrettyPrinter printer = new PrettyPrinter(80);
                    printer.add("Processing Interaction").centre().hr();
                    printer.add("The item stacks captured are: ");
                    for (Map.Entry<UUID, Collection<ItemDropData>> entry : map.asMap().entrySet()) {
                        printer.add("  - Entity with UUID: %s", entry.getKey());
                        for (ItemDropData stack : entry.getValue()) {
                            printer.add("    - %s", stack);
                        }
                    }
                    printer.trace(System.err);
                });
        context.getCapturedEntitySupplier().ifPresentAndNotEmpty(entities -> {
            final List<Entity> projectiles = new ArrayList<>(entities.size());
            final List<Entity> spawnEggs = new ArrayList<>(entities.size());
            final List<Entity> normalPlacement = new ArrayList<>(entities.size());
            final List<Entity> items = new ArrayList<>(entities.size());
            for (Entity entity : entities) {
                if (entity instanceof Projectile || entity instanceof EntityThrowable) {
                    projectiles.add(entity);
                } else if (usedSnapshot.getType() == ItemTypes.SPAWN_EGG) {
                    spawnEggs.add(entity);
                } else if (entity instanceof EntityItem) {
                    items.add(entity);
                } else {
                    normalPlacement.add(entity);
                }
            }
            if (ShouldFire.SPAWN_ENTITY_EVENT) {
                if (!projectiles.isEmpty()) {
                    final Cause cause = Cause.source(
                            EntitySpawnCause.builder()
                                    .entity(spongePlayer)
                                    .type(InternalSpawnTypes.PROJECTILE)
                                    .build())
                            .named(NamedCause.of("UsedItem", usedSnapshot))
                            .owner(player)
                            .build();
                    final SpawnEntityEvent event = SpongeEventFactory.createSpawnEntityEvent(cause, projectiles);
                    if (!SpongeImpl.postEvent(event)) {
                        TrackingUtil.processSpawnedEntities(player, event);
                    }
                }
                if (!spawnEggs.isEmpty()) {
                    final Cause cause = Cause.source(
                            EntitySpawnCause.builder()
                                    .entity(spongePlayer)
                                    .type(InternalSpawnTypes.SPAWN_EGG)
                                    .build())
                            .named(NamedCause.of("UsedItem", usedSnapshot))
                            .owner(player)
                            .build();
                    final SpawnEntityEvent event = SpongeEventFactory.createSpawnEntityEvent(cause, spawnEggs);
                    if (!SpongeImpl.postEvent(event)) {
                        TrackingUtil.processSpawnedEntities(player, event);
                    }
                }
                if (!normalPlacement.isEmpty()) {
                    final Cause cause = Cause.source(
                            EntitySpawnCause.builder()
                                    .entity(spongePlayer)
                                    .type(InternalSpawnTypes.PLACEMENT)
                                    .build())
                            .build();
                    final SpawnEntityEvent event = SpongeEventFactory.createSpawnEntityEvent(cause, normalPlacement);
                    if (!SpongeImpl.postEvent(event)) {
                        TrackingUtil.processSpawnedEntities(player, event);
                    }
                }
            } else {
                TrackingUtil.processEntities(player, projectiles);
                TrackingUtil.processEntities(player, spawnEggs);
                TrackingUtil.processEntities(player, normalPlacement);
            }

            if (!items.isEmpty()) {
                if (ShouldFire.DROP_ITEM_EVENT_DISPENSE) {
                    final Cause cause = Cause.source(
                            EntitySpawnCause.builder()
                                    .entity(spongePlayer)
                                    .type(InternalSpawnTypes.DROPPED_ITEM)
                                    .build())
                            .owner(player)
                            .build();
                    final DropItemEvent.Dispense dispense = SpongeEventFactory.createDropItemEventDispense(cause, items);
                    if (!SpongeImpl.postEvent(dispense)) {
                        TrackingUtil.processSpawnedEntities(player, dispense);
                    }
                } else {
                    TrackingUtil.processEntities(player, items);
                }
            }
        });

        final IMixinContainer mixinContainer = ContainerUtil.toMixin(player.openContainer);
        mixinContainer.setCaptureInventory(false);
        mixinContainer.getCapturedTransactions().clear();
    }

    @Override
    public void populateContext(EntityPlayerMP playerMP, Packet<?> packet, PhaseContext context) {
        final ItemStack stack = ItemStackUtil.cloneDefensive(playerMP.getHeldItemMainhand());
        if (stack != null) {
            context.add(NamedCause.of(InternalNamedCauses.Packet.ITEM_USED, stack));
        }
        context.addEntityDropCaptures()
                .addEntityCaptures()
                .addBlockCaptures();
    }

    @Override
    public boolean shouldCaptureEntity() {
        return true;
    }

    @Override
    public boolean doesCaptureEntityDrops() {
        return true;
    }

    @Override
    public boolean canSwitchTo(IPhaseState state) {
        return state == BlockPhase.State.BLOCK_DECAY || state == BlockPhase.State.BLOCK_DROP_ITEMS;
    }

    @Override
    public boolean tracksBlockSpecificDrops() {
        return true;
    }

    @Override
    public boolean tracksEntitySpecificDrops() {
        return true;
    }
}
