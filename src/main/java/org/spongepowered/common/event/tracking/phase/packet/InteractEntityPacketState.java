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

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketUseEntity;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

final class InteractEntityPacketState extends BasicPacketState {

    @Override
    public boolean ignoresItemPreMerges() {
        return true;
    }
    @Override
    public boolean isPacketIgnored(Packet<?> packetIn, EntityPlayerMP packetPlayer) {
        final CPacketUseEntity useEntityPacket = (CPacketUseEntity) packetIn;
        // There are cases where a player is interacting with an entity that doesn't exist on the server.
        @Nullable net.minecraft.entity.Entity entity = useEntityPacket.getEntityFromWorld(packetPlayer.world);
        return entity == null;
    }

    @Override
    public void populateContext(EntityPlayerMP playerMP, Packet<?> packet, PhaseContext context) {
        final CPacketUseEntity useEntityPacket = (CPacketUseEntity) packet;
        net.minecraft.entity.Entity entity = useEntityPacket.getEntityFromWorld(playerMP.world);
        if (entity != null) {
            // unused, to be removed and re-located when phase context is cleaned up
            //context.add(NamedCause.of(InternalNamedCauses.Packet.TARGETED_ENTITY, entity));
            //context.add(NamedCause.of(InternalNamedCauses.Packet.TRACKED_ENTITY_ID, entity.getEntityId()));
            final ItemStack stack = ItemStackUtil.cloneDefensive(playerMP.getHeldItem(useEntityPacket.getHand()));
            if (stack != null) {
                context.add(NamedCause.of(InternalNamedCauses.Packet.ITEM_USED, stack));
            }
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
    public boolean doesCaptureEntityDrops() {
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

        context.getCapturedBlockSupplier()
                .ifPresentAndNotEmpty(blocks -> {
                    final PrettyPrinter printer = new PrettyPrinter(80);
                    printer.add("Processing Interact Entity").centre().hr();
                    printer.add("The blocks captured are:");
                    for (BlockSnapshot blockSnapshot : blocks) {
                        printer.add("  Block: %s", blockSnapshot);
                    }
                    printer.trace(System.err);
                });
        context.getCapturedEntitySupplier()
                .ifPresentAndNotEmpty(entities -> {
                    final PrettyPrinter printer = new PrettyPrinter(80);
                    printer.add("Processing Interact Entity").centre().hr();
                    printer.add("The entities captured are:");
                    for (Entity capturedEntity : entities) {
                        printer.add("  Entity: %s", capturedEntity);
                    }
                    printer.trace(System.err);
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
                TrackingUtil.processSpawnedEntities(player, event);

            }
        });
        context.getCapturedEntityDropSupplier()
                .ifPresentAndNotEmpty(map -> {
                    final PrettyPrinter printer = new PrettyPrinter(80);
                    printer.add("Processing Interact Entity").centre().hr();
                    printer.add("The item stacks captured are: ");

                    for (Map.Entry<UUID, Collection<ItemDropData>> entry : map.asMap().entrySet()) {
                        printer.add("  - Entity with UUID: %s", entry.getKey());
                        for (ItemDropData stack : entry.getValue()) {
                            printer.add("    - %s", stack);
                        }
                    }
                    printer.trace(System.err);
                });

        context.getCapturedBlockSupplier()
                .ifPresentAndNotEmpty(snapshots ->
                        TrackingUtil.processBlockCaptures(snapshots, this, context)
                );
    }
}
