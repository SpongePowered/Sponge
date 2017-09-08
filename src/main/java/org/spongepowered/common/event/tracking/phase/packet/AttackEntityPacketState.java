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
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.world.WorldServer;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.ItemDropData;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

final class AttackEntityPacketState extends BasicPacketState {

    @Override
    public boolean isPacketIgnored(Packet<?> packetIn, EntityPlayerMP packetPlayer) {
        final CPacketUseEntity useEntityPacket = (CPacketUseEntity) packetIn;
        // There are cases where a player is interacting with an entity that doesn't exist on the server.
        @Nullable net.minecraft.entity.Entity entity = useEntityPacket.getEntityFromWorld(packetPlayer.world);
        return entity == null;
    }

    @Override
    public void populateContext(EntityPlayerMP playerMP, Packet<?> packet, PhaseContext context) {
//        final CPacketUseEntity useEntityPacket = (CPacketUseEntity) packet;
//        net.minecraft.entity.Entity entity = useEntityPacket.getEntityFromWorld(playerMP.world);
        // unused, to be removed and re-located when phase context is cleaned up
        //context.add(NamedCause.of(InternalNamedCauses.Packet.TARGETED_ENTITY, entity));
        //context.add(NamedCause.of(InternalNamedCauses.Packet.TRACKED_ENTITY_ID, entity.getEntityId()));
        final ItemStack stack = ItemStackUtil.cloneDefensive(playerMP.getHeldItemMainhand());
        if (stack != null) {
            context.add(NamedCause.of(InternalNamedCauses.Packet.ITEM_USED, stack));
        }
        context.addEntityDropCaptures()
                .addEntityCaptures()
                .addBlockCaptures();
    }

    @Override
    public boolean tracksEntitySpecificDrops() {
        return true;
    }

    @Override
    public void unwind(Packet<?> packet, EntityPlayerMP player, PhaseContext context) {
        final CPacketUseEntity useEntityPacket = (CPacketUseEntity) packet;
        final net.minecraft.entity.Entity entity = useEntityPacket.getEntityFromWorld(player.world);
        if (entity == null) {
            // Something happened?
            return;
        }
        //final Optional<ItemStack> itemStack = context.firstNamed(InternalNamedCauses.Packet.ITEM_USED, ItemStack.class);
        final World spongeWorld = EntityUtil.getSpongeWorld(player);
        EntityUtil.toMixin(entity).setNotifier(player.getUniqueID());

        context.getCapturedItemsSupplier()
                .ifPresentAndNotEmpty(items -> {
                    // For destruction, this should be empty, however, some times, it may not be?
                    final PrettyPrinter printer = new PrettyPrinter(60);
                    printer.add("Processing Attack Entity").centre().hr();
                    printer.add("There are some captured items after the entity was destructed!");
                    printer.addWrapped(60, "%s : %s", "Items captured", items);
                    printer.add("Stacktrace:");
                    printer.add(new Exception("Stack trace"));
                    printer.trace(System.err, SpongeImpl.getLogger(), Level.TRACE);
                });
        context.getCapturedBlockSupplier()
                .ifPresentAndNotEmpty(blocks ->
                        TrackingUtil.processBlockCaptures(blocks, this, context));
        context.getCapturedEntityDropSupplier().ifPresentAndNotEmpty(map -> {
            for (Map.Entry<UUID, Collection<ItemDropData>> entry : map.asMap().entrySet()) {
                final UUID key = entry.getKey();
                final Optional<Entity> affectedEntity = spongeWorld.getEntity(key);
                if (!affectedEntity.isPresent()) {
                    continue;
                }
                final Collection<ItemDropData> itemStacks = entry.getValue();
                if (itemStacks.isEmpty()) {
                    return;
                }
                final List<ItemDropData> items = new ArrayList<>();
                items.addAll(itemStacks);

                if (!items.isEmpty()) {
                    final List<Entity> itemEntities = items.stream()
                            .map(data -> data.create(((WorldServer) player.world)))
                            .map(EntityUtil::fromNative)
                            .collect(Collectors.toList());
                    final Cause cause = Cause.source(EntitySpawnCause.builder()
                            .entity(affectedEntity.get())
                            .type(InternalSpawnTypes.DROPPED_ITEM)
                            .build()
                    )
                            .named(NamedCause.of("Attacker", player))
                            .build();
                    final DropItemEvent.Destruct
                            destruct =
                            SpongeEventFactory.createDropItemEventDestruct(cause, itemEntities);
                    SpongeImpl.postEvent(destruct);
                    if (!destruct.isCancelled()) {
                        TrackingUtil.processSpawnedEntities(player, destruct);
                    }
                }
            }
        });
        context.getCapturedEntityItemDropSupplier().ifPresentAndNotEmpty(map -> {
            for (Map.Entry<UUID, Collection<EntityItem>> entry : map.asMap().entrySet()) {
                final UUID key = entry.getKey();
                final Optional<Entity> attackedEntities = spongeWorld.getEntity(key);
                if (!attackedEntities.isPresent()) {
                    continue;
                }
                final List<Entity> items = entry.getValue().stream().map(EntityUtil::fromNative).collect(Collectors.toList());
                final Cause cause = Cause.source(EntitySpawnCause.builder()
                        .entity(EntityUtil.fromNative(player))
                        .type(InternalSpawnTypes.DROPPED_ITEM)
                        .build()
                )
                        .named(NamedCause.of("Attacker", player))
                        .build();
                final DropItemEvent.Destruct
                        destruct =
                        SpongeEventFactory.createDropItemEventDestruct(cause, items);
                SpongeImpl.postEvent(destruct);
                if (!destruct.isCancelled()) {
                    TrackingUtil.processSpawnedEntities(player, destruct);
                }
            }
        });


        context.getCapturedBlockSupplier()
                .ifPresentAndNotEmpty(snapshots ->
                        TrackingUtil.processBlockCaptures(snapshots, this, context)
                );
    }
}
