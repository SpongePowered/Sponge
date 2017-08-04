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
import net.minecraft.network.Packet;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.tracking.ItemDropData;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

final class ExternalPacketState extends BasicPacketState {

    @Override
    public boolean ignoresItemPreMerges() {
        return true;
    }

    @Override
    public boolean tracksBlockSpecificDrops() {
        return true;
    }

    @Override
    public boolean tracksEntitySpecificDrops() {
        return true;
    }

    @Override
    public void unwind(Packet<?> packet, EntityPlayerMP player, PhaseContext context) {
        context.getCapturedBlockSupplier().ifPresentAndNotEmpty(blocks -> TrackingUtil.processBlockCaptures(blocks, this, context));
        context.getCapturedEntitySupplier().ifPresentAndNotEmpty(entities -> {
            final Cause cause = Cause.source(EntitySpawnCause.builder()
                    .entity((Player) player)
                    .type(InternalSpawnTypes.PLACEMENT)
                    .build()
            ).build();
            SpawnEntityEvent event = SpongeEventFactory.createSpawnEntityEvent(cause, entities);
            SpongeImpl.postEvent(event);
            if (!event.isCancelled()) {
                PacketPhaseUtil.processSpawnedEntities(player, event);

            }
        });
        context.getCapturedItemsSupplier().ifPresentAndNotEmpty(entities -> {
            final List<Entity> items = entities.stream().map(EntityUtil::fromNative).collect(Collectors.toList());
            final Cause cause = Cause.source(EntitySpawnCause.builder()
                    .entity((Player) player)
                    .type(InternalSpawnTypes.PLACEMENT)
                    .build()
            ).build();
            SpawnEntityEvent event = SpongeEventFactory.createSpawnEntityEvent(cause, items);
            SpongeImpl.postEvent(event);
            if (!event.isCancelled()) {
                PacketPhaseUtil.processSpawnedEntities(player, event);

            }
        });
        context.getCapturedEntityDropSupplier().ifPresentAndNotEmpty(map -> {
            final PrettyPrinter printer = new PrettyPrinter(80);
            printer.add("Processing Interact At Entity").centre().hr();
            printer.add("The item stacks captured are: ");

            for (Map.Entry<UUID, Collection<ItemDropData>> entry : map.asMap().entrySet()) {
                printer.add("  - Entity with UUID: %s", entry.getKey());
                for (ItemDropData stack : entry.getValue()) {
                    printer.add("    - %s", stack);
                }
            }
            printer.trace(System.err);
        });
        context.getCapturedEntityItemDropSupplier().ifPresentAndNotEmpty(map -> {
            for (Map.Entry<UUID, Collection<EntityItem>> entry : map.asMap().entrySet()) {
                final UUID entityUuid = entry.getKey();
                final net.minecraft.entity.Entity entityFromUuid = player.getServerWorld().getEntityFromUuid(entityUuid);
                final Entity affectedEntity = EntityUtil.fromNative(entityFromUuid);
                if (entityFromUuid != null) {
                    final Cause cause = Cause.source(
                            EntitySpawnCause.builder()
                                    .entity(affectedEntity)
                                    .type(InternalSpawnTypes.CUSTOM)
                                    .build()
                    ).named(NamedCause.notifier(player))
                            .build();
                    final List<Entity> entities = entry.getValue()
                            .stream()
                            .map(EntityUtil::fromNative)
                            .collect(Collectors.toList());
                    if (!entities.isEmpty()) {
                        DropItemEvent.Custom event = SpongeEventFactory.createDropItemEventCustom(cause, entities);
                        SpongeImpl.postEvent(event);
                        if (!event.isCancelled()) {
                            PacketPhaseUtil.processSpawnedEntities(player, event);

                        }
                    }
                }
            }
        });
        context.getCapturedItemStackSupplier().ifPresentAndNotEmpty(drops -> {
            final List<EntityItem>
                    items =
                    drops.stream().map(drop -> drop.create(player.getServerWorld())).collect(Collectors.toList());
            final Cause cause = Cause.source(
                    EntitySpawnCause.builder()
                            .entity((Entity) player)
                            .type(InternalSpawnTypes.CUSTOM)
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
                    PacketPhaseUtil.processSpawnedEntities(player, event);

                }
            }

        });
    }

    @Override
    public void populateContext(EntityPlayerMP playerMP, Packet<?> packet, PhaseContext context) {
        context.addBlockCaptures().addEntityCaptures().addEntityDropCaptures();
    }

}
