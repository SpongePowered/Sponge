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

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.ExperienceOrb;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.registry.type.event.SpawnTypeRegistryModule;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.entity.item.ItemEntity;

final class DeathUpdateState extends EntityPhaseState<BasicEntityContext> {

    DeathUpdateState() {
    }

    @Override
    public BasicEntityContext createNewContext() {
        return new BasicEntityContext(this)
            .addCaptures()
            .addEntityDropCaptures();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void unwind(BasicEntityContext context) {
        final Entity dyingEntity = context.getSource(Entity.class)
                .orElseThrow(TrackingUtil.throwWithContext("Dying entity not found!", context));
        context.getCapturedItemsSupplier()
                .acceptAndClearIfNotEmpty(items -> {
                    final DamageSource damageSource = context.getDamageSource();
                    try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
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
                        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                            frame.pushCause(dyingEntity);
                            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.EXPERIENCE);
                            SpongeCommonEventFactory.callSpawnEntity(experience, context);
                        }
                    }

                    final List<Entity> other = entities.stream()
                            .filter(entity -> !(entity instanceof ExperienceOrb))
                            .collect(Collectors.toList());
                    if (!other.isEmpty()) {
                        try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                            frame.pushCause(dyingEntity);
                            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypeRegistryModule.ENTITY_DEATH);
                            SpongeCommonEventFactory.callSpawnEntity(other, context);

                        }
                    }

                });
        context.getPerEntityItemEntityDropSupplier().acceptAndClearIfNotEmpty((map) -> {
            try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                final DamageSource damageSource = context.getDamageSource();
                if (damageSource != null) {
                    frame.pushCause(damageSource);
                }
                frame.pushCause(dyingEntity);
                for (Map.Entry<UUID, Collection<ItemEntity>> entry : map.asMap().entrySet()) {
                    if (entry.getKey().equals(dyingEntity.getUniqueId())) {

                            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
                            SpongeCommonEventFactory.callDropItemCustom((List<Entity>) (List) entry.getValue(), context);
                            frame.popCause();
                            continue;
                    }
                    Optional<Entity> otherSource = dyingEntity.getWorld().getEntity(entry.getKey());
                    otherSource.ifPresent(other -> {
                        frame.pushCause(other);
                        frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
                        SpongeCommonEventFactory.callDropItemCustom((List<Entity>) (List) entry.getValue(), context);
                        frame.popCause(); // Pop because other entities may be spawning things as well
                    });
                }
            }

        });
        context.getPerEntityItemDropSupplier().acceptAndClearIfNotEmpty(map -> {
            // Unused, ItemDropData needs to be reinvented or reintroduced, maybe for 1.13
        });
        // TODO - Determine if we need to pass the supplier or perform some parameterized
        //  process if not empty method on the capture object.
        TrackingUtil.processBlockCaptures(context);

    }
}
