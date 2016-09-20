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
package org.spongepowered.common.event.tracking.phase.entity;

import net.minecraft.entity.item.EntityItem;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.ExperienceOrb;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.ItemDropData;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

final class DeathUpdateState extends EntityPhaseState {

    DeathUpdateState() {
    }

    @Override
    void unwind(CauseTracker causeTracker, PhaseContext context) {
        final Entity dyingEntity = context.getSource(Entity.class)
                .orElseThrow(TrackingUtil.throwWithContext("Dying entity not found!", context));
        context.getCapturedItemsSupplier()
                .ifPresentAndNotEmpty(items -> {
                    final DamageSource damageSource = context.firstNamed(InternalNamedCauses.General.DAMAGE_SOURCE, DamageSource.class).get();
                    final Cause cause = Cause.source(
                            EntitySpawnCause.builder()
                                    .entity(dyingEntity)
                                    .type(InternalSpawnTypes.DROPPED_ITEM)
                                    .build())
                            .named(InternalNamedCauses.General.DAMAGE_SOURCE, damageSource)
                            .build();
                    final ArrayList<Entity> entities = new ArrayList<>();
                    for (EntityItem item : items) {
                        entities.add(EntityUtil.fromNative(item));
                    }
                    final DropItemEvent.Destruct
                            destruct =
                            SpongeEventFactory.createDropItemEventDestruct(cause, entities, causeTracker.getWorld());
                    SpongeImpl.postEvent(destruct);
                    if (!destruct.isCancelled()) {
                        for (Entity entity : destruct.getEntities()) {
                            causeTracker.getMixinWorld().forceSpawnEntity(entity);
                        }
                    }
                });
        context.getCapturedEntitySupplier()
                .ifPresentAndNotEmpty(entities -> {
                            final List<Entity> experience = entities.stream()
                                    .filter(entity -> entity instanceof ExperienceOrb)
                                    .collect(Collectors.toList());
                            if (!experience.isEmpty()) {
                                final Cause cause = Cause.source(
                                        EntitySpawnCause.builder()
                                                .entity(dyingEntity)
                                                .type(InternalSpawnTypes.EXPERIENCE)
                                                .build())
                                        .build();
                                final SpawnEntityEvent
                                        event =
                                        SpongeEventFactory.createSpawnEntityEvent(cause, experience, causeTracker.getWorld());
                                SpongeImpl.postEvent(event);
                                if (!event.isCancelled()) {
                                    for (Entity entity : event.getEntities()) {
                                        causeTracker.getMixinWorld().forceSpawnEntity(entity);
                                    }
                                }
                            }

                            final List<Entity> other = entities.stream()
                                    .filter(entity -> !(entity instanceof ExperienceOrb))
                                    .collect(Collectors.toList());
                            if (!other.isEmpty()) {
                                final Cause cause = Cause.source(
                                        EntitySpawnCause.builder()
                                                .entity(dyingEntity)
                                                .type(InternalSpawnTypes.ENTITY_DEATH)
                                                .build())
                                        .build();
                                final SpawnEntityEvent
                                        event1 =
                                        SpongeEventFactory.createSpawnEntityEvent(cause, other, causeTracker.getWorld());
                                SpongeImpl.postEvent(event1);
                                if (!event1.isCancelled()) {
                                    for (Entity entity : event1.getEntities()) {
                                        causeTracker.getMixinWorld().forceSpawnEntity(entity);
                                    }
                                }
                            }

                        }
                );
        context.getCapturedEntityDropSupplier().ifPresentAndNotEmpty(map -> {
            if (map.isEmpty()) {
                return;
            }
            final PrettyPrinter printer = new PrettyPrinter(80);
            printer.add("Processing Entity Death Updates Spawning").centre().hr();
            printer.add("Entity Dying: " + dyingEntity);
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
                .ifPresentAndNotEmpty(blocks -> TrackingUtil.processBlockCaptures(blocks, causeTracker, this, context));

    }
}
