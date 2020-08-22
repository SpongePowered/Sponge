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

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.packet.BasicPacketContext;
import org.spongepowered.common.event.tracking.phase.packet.BasicPacketState;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public final class UnknownPacketState extends BasicPacketState {

    @Override
    public boolean ignoresItemPreMerging() {
        return true;
    }

    @Override
    public boolean tracksBlockSpecificDrops(BasicPacketContext context) {
        return true;
    }

    @Override
    public boolean tracksEntitySpecificDrops() {
        return true;
    }

    @Override
    public void unwind(BasicPacketContext context) {
        final ServerPlayerEntity player = context.getPacketPlayer();

        try (CauseStackManager.StackFrame frame1 = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame1.pushCause(player);
            frame1.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLACEMENT);
            // TODO - Determine if we need to pass the supplier or perform some parameterized
            //  process if not empty method on the capture object.
            TrackingUtil.processBlockCaptures(context);
            context.getCapturedEntitySupplier().acceptAndClearIfNotEmpty(entities -> {
                SpongeCommonEventFactory.callSpawnEntity(entities, context);
            });
            context.getCapturedItemsSupplier().acceptAndClearIfNotEmpty(entities -> {
                final List<Entity> items = entities.stream().map(entity -> (Entity) entity).collect(Collectors.toList());
                SpongeCommonEventFactory.callSpawnEntity(items, context);
            });
        }
        context.getPerEntityItemEntityDropSupplier().acceptAndClearIfNotEmpty(map -> {
            for (Map.Entry<UUID, Collection<ItemEntity>> entry : map.asMap().entrySet()) {
                final UUID entityUuid = entry.getKey();
                final net.minecraft.entity.Entity entityFromUuid = player.getServerWorld().getEntityByUuid(entityUuid);
                final Entity affectedEntity = (Entity) entityFromUuid;
                if (entityFromUuid != null) {
                    final List<Entity> entities = entry.getValue()
                        .stream()
                        .map(entity -> (Entity) entity)
                        .collect(Collectors.toList());
                    if (!entities.isEmpty()) {
                        try (CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                            frame.pushCause(player);
                            frame.pushCause(affectedEntity);
                            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.CUSTOM);
                            DropItemEvent.Custom event = SpongeEventFactory.createDropItemEventCustom(frame.getCurrentCause(),
                                entities);
                            SpongeCommon.postEvent(event);
                            if (!event.isCancelled()) {
                                processSpawnedEntities(player, event);

                            }
                        }
                    }
                }
            }
        });
    }
}
