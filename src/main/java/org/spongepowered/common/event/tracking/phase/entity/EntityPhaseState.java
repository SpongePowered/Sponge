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
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PooledPhaseState;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.registry.type.event.SpawnTypeRegistryModule;

import java.util.List;
import java.util.stream.Collectors;

public abstract class EntityPhaseState<E extends EntityContext<E>> extends PooledPhaseState<E> implements IPhaseState<E> {

    private final String desc = TrackingUtil.phaseStateToString("Block", this);

    @Override
    public boolean doesCaptureEntityDrops(final E context) {
        return true;
    }

    @Override
    public void unwind(final E context) {

    }

    @Override
    public String toString() {
        return this.desc;
    }

    void standardSpawnCapturedEntities(final PhaseContext<?> context, final List<? extends Entity> entities) {
        // Separate experience orbs from other entity drops
        final List<Entity> experience = entities.stream()
            .filter(entity -> entity instanceof ExperienceOrb)
            .collect(Collectors.toList());
        if (!experience.isEmpty()) {
            Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.EXPERIENCE);
            SpongeCommonEventFactory.callSpawnEntity(experience, context);

        }

        // Now process other entities, this is separate from item drops specifically
        final List<Entity> other = entities.stream()
            .filter(entity -> !(entity instanceof ExperienceOrb))
            .collect(Collectors.toList());
        if (!other.isEmpty()) {
            Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypeRegistryModule.ENTITY_DEATH);
            SpongeCommonEventFactory.callSpawnEntity(experience, context);
        }
    }

    @Override
    public boolean tracksEntitySpecificDrops() {
        return true;
    }

    @Override
    public boolean doesDenyChunkRequests() {
        return true;
    }

}


