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

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.network.Packet;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;

import java.util.List;
import java.util.stream.Collectors;

final class CloseWindowState extends BasicPacketState {

    @Override
    public void unwind(Packet<?> packet, EntityPlayerMP player, PhaseContext context) {
        final Container container = context.firstNamed(InternalNamedCauses.Packet.OPEN_CONTAINER, Container.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected the open container object, but had nothing!", context));
        ItemStackSnapshot lastCursor = context.firstNamed(InternalNamedCauses.Packet.CURSOR, ItemStackSnapshot.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected a cursor item stack, but had nothing!", context));
        ItemStackSnapshot newCursor = ItemStackUtil.snapshotOf(player.inventory.getItemStack());
        final Cause cause = Cause.source(player).build();
        InteractInventoryEvent.Close event =
                SpongeCommonEventFactory.callInteractInventoryCloseEvent(cause, container, player, lastCursor, newCursor, true);
        if (!event.isCancelled()) {
            // Non-merged items
            context.getCapturedItemsSupplier().ifPresentAndNotEmpty(items -> {
                final Cause spawnCause = Cause.source(
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
                    DropItemEvent.Custom drop = SpongeEventFactory.createDropItemEventCustom(spawnCause, entities);
                    SpongeImpl.postEvent(drop);
                    if (!drop.isCancelled()) {
                        for (Entity droppedItem : drop.getEntities()) {
                            droppedItem.setCreator(player.getUniqueID());
                            ((IMixinWorldServer) player.getServerWorld()).forceSpawnEntity(droppedItem);
                        }
                    }
                }
            });
            // Pre-merged items
            context.getCapturedItemStackSupplier().ifPresentAndNotEmpty(stacks -> {
                final List<EntityItem> items = stacks.stream()
                        .map(drop -> drop.create(player.getServerWorld()))
                        .collect(Collectors.toList());
                final Cause spawnCause = Cause.source(
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
                    DropItemEvent.Custom drop = SpongeEventFactory.createDropItemEventCustom(spawnCause, entities);
                    SpongeImpl.postEvent(drop);
                    if (!drop.isCancelled()) {
                        for (Entity droppedItem : drop.getEntities()) {
                            droppedItem.setCreator(player.getUniqueID());
                            ((IMixinWorldServer) player.getServerWorld()).forceSpawnEntity(droppedItem);
                        }
                    }
                }

            });
            context.getCapturedBlockSupplier()
                    .ifPresentAndNotEmpty(blocks -> TrackingUtil.processBlockCaptures(blocks, this, context));
        }
    }

    @Override
    public void populateContext(EntityPlayerMP playerMP, Packet<?> packet, PhaseContext context) {
        context
                .add(NamedCause.of(InternalNamedCauses.Packet.OPEN_CONTAINER, playerMP.openContainer))
                .addBlockCaptures()
                .addEntityCaptures()
                .addEntityDropCaptures();
    }
}
