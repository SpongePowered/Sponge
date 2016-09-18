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
package org.spongepowered.common.event.tracking.phase.general;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.ItemDropData;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.block.BlockPhaseState;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

final class CommandState extends GeneralState {

    @Override
    public boolean canSwitchTo(IPhaseState state) {
        return state instanceof BlockPhaseState;
    }

    @Override
    public boolean tracksEntitySpecificDrops() {
        return true;
    }

    @Override
    void unwind(CauseTracker causeTracker, PhaseContext phaseContext) {
        final CommandSource sender = phaseContext.getSource(CommandSource.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected to be capturing a Command Sender, but none found!", phaseContext));
        phaseContext.getCapturedBlockSupplier()
                .ifPresentAndNotEmpty(list -> TrackingUtil.processBlockCaptures(list, causeTracker, this, phaseContext));
        phaseContext.getCapturedEntitySupplier()
                .ifPresentAndNotEmpty(entities -> {
                    // TODO the entity spawn causes are not likely valid, need to investigate further.
                    final Cause cause = Cause.source(
                            SpawnCause.builder()
                                    .type(InternalSpawnTypes.PLACEMENT)
                                    .build())
                            .build();
                    final SpawnEntityEvent spawnEntityEvent = SpongeEventFactory.createSpawnEntityEvent(cause, entities, causeTracker.getWorld());
                    SpongeImpl.postEvent(spawnEntityEvent);
                    if (!spawnEntityEvent.isCancelled()) {
                        final boolean isPlayer = sender instanceof Player;
                        final Player player = isPlayer ? (Player) sender : null;
                        for (Entity entity : spawnEntityEvent.getEntities()) {
                            if (isPlayer) {
                                EntityUtil.toMixin(entity).setCreator(player.getUniqueId());
                            }
                            causeTracker.getMixinWorld().forceSpawnEntity(entity);
                        }
                    }
                });
        phaseContext.getCapturedEntityDropSupplier()
                .ifPresentAndNotEmpty(uuidItemStackMultimap -> {
                    for (Map.Entry<UUID, Collection<ItemDropData>> entry : uuidItemStackMultimap.asMap().entrySet()) {
                        final UUID key = entry.getKey();
                        final Optional<Entity> affectedEntity = causeTracker.getWorld().getEntity(key);
                        if (!affectedEntity.isPresent()) {
                            continue;
                        }
                        final Collection<ItemDropData> itemStacks = entry.getValue();
                        if (itemStacks.isEmpty()) {
                            return;
                        }
                        final List<ItemDropData> items = new ArrayList<>();
                        items.addAll(itemStacks);
                        itemStacks.clear();

                        if (!items.isEmpty()) {
                            final List<Entity> itemEntities = items.stream()
                                    .map(data -> data.create(causeTracker.getMinecraftWorld()))
                                    .map(EntityUtil::fromNative)
                                    .collect(Collectors.toList());
                            final Cause cause = Cause.source(EntitySpawnCause.builder()
                                    .entity(affectedEntity.get())
                                    .type(InternalSpawnTypes.DROPPED_ITEM)
                                    .build()
                            )
                                    .named(NamedCause.of("CommandSource", sender))
                                    .build();
                            final DropItemEvent.Destruct
                                    destruct =
                                    SpongeEventFactory.createDropItemEventDestruct(cause, itemEntities, causeTracker.getWorld());
                            SpongeImpl.postEvent(destruct);
                            if (!destruct.isCancelled()) {
                                final boolean isPlayer = sender instanceof Player;
                                final Player player = isPlayer ? (Player) sender : null;
                                for (Entity entity : destruct.getEntities()) {
                                    if (isPlayer) {
                                        EntityUtil.toMixin(entity).setCreator(player.getUniqueId());
                                    }
                                    causeTracker.getMixinWorld().forceSpawnEntity(entity);
                                }
                            }

                        }
                    }
                });
    }
}
