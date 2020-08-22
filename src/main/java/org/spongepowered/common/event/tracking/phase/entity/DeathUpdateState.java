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

import net.minecraft.entity.item.ItemEntity;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.ExperienceOrb;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.registry.builtin.sponge.SpawnTypeStreamGenerator;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

final class DeathUpdateState extends EntityPhaseState<BasicEntityContext> {

    DeathUpdateState() {
    }

    @Override
    public BasicEntityContext createNewContext(final PhaseTracker tracker) {
        return new BasicEntityContext(this, tracker)
            .addCaptures()
            .addEntityDropCaptures();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void unwind(final BasicEntityContext context) {
        final Entity dyingEntity = context.getSource(Entity.class)
                .orElseThrow(TrackingUtil.throwWithContext("Dying entity not found!", context));
        context.getCapturedItemsSupplier()
                .acceptAndClearIfNotEmpty(items -> {
                    final DamageSource damageSource = context.getDamageSource();
                    try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                        if (damageSource != null) {
                            frame.pushCause(damageSource);
                        }
                        frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
                        frame.pushCause(dyingEntity);
                        SpongeCommonEventFactory.callDropItemCustom((List<Entity>) (List) items, context);
                    }
                });
        context.getCapturedEntitySupplier()
                .acceptAndClearIfNotEmpty(entities -> {
                    final List<Entity> experience = entities.stream()
                            .filter(entity -> entity instanceof ExperienceOrb)
                            .collect(Collectors.toList());
                    if (!experience.isEmpty()) {
                        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                            frame.pushCause(dyingEntity);
                            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.EXPERIENCE);
                            SpongeCommonEventFactory.callSpawnEntity(experience, context);
                        }
                    }

                    final List<Entity> other = entities.stream()
                            .filter(entity -> !(entity instanceof ExperienceOrb))
                            .collect(Collectors.toList());
                    if (!other.isEmpty()) {
                        try (final StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                            frame.pushCause(dyingEntity);
                            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypeStreamGenerator.ENTITY_DEATH);
                            SpongeCommonEventFactory.callSpawnEntity(other, context);

                        }
                    }

                });
        context.getPerEntityItemEntityDropSupplier().acceptAndClearIfNotEmpty((map) -> {
            try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                final DamageSource damageSource = context.getDamageSource();
                if (damageSource != null) {
                    frame.pushCause(damageSource);
                }
                frame.pushCause(dyingEntity);
                for (final Map.Entry<UUID, Collection<ItemEntity>> entry : map.asMap().entrySet()) {
                    if (entry.getKey().equals(dyingEntity.getUniqueId())) {

                            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
                            SpongeCommonEventFactory.callDropItemCustom((List<Entity>) (List) entry.getValue(), context);
                            frame.popCause();
                            continue;
                    }
                    final Optional<Entity> otherSource = dyingEntity.getWorld().getEntity(entry.getKey());
                    otherSource.ifPresent(other -> {
                        frame.pushCause(other);
                        frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
                        SpongeCommonEventFactory.callDropItemCustom((List<Entity>) (List) entry.getValue(), context);
                        frame.popCause(); // Pop because other entities may be spawning things as well
                    });
                }
            }

        });

        // TODO - Determine if we need to pass the supplier or perform some parameterized
        //  process if not empty method on the capture object.
        TrackingUtil.processBlockCaptures(context);

    }
}
