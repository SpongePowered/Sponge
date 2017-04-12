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
package org.spongepowered.common.event.tracking.phase.generation;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.TrackingPhase;
import org.spongepowered.common.event.tracking.phase.TrackingPhases;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A generalized
 */
class GeneralGenerationPhaseState implements IPhaseState {

    private Set<IPhaseState> compatibleStates = new HashSet<>();
    private boolean isBaked = false;
    private final String id;

    GeneralGenerationPhaseState(String id) {
        this.id = id;
    }

    final GeneralGenerationPhaseState addCompatibleState(IPhaseState state) {
        if (this.isBaked) {
            throw new IllegalStateException("This state is already baked! " + this.id);
        }
        this.compatibleStates.add(state);
        return this;
    }

    final GeneralGenerationPhaseState bake() {
        if (this.isBaked) {
            throw new IllegalStateException("This state is already baked! " + this.id);
        }
        this.compatibleStates = ImmutableSet.copyOf(this.compatibleStates);
        this.isBaked = true;
        return this;
    }

    @Override
    public final TrackingPhase getPhase() {
        return TrackingPhases.GENERATION;
    }

    @Override
    public final boolean canSwitchTo(IPhaseState state) {
        return this.compatibleStates.contains(state);
    }

    @Override
    public final boolean isExpectedForReEntrance() {
        return true;
    }


    Cause provideSpawnCause(PhaseContext context) {
        return Cause.source(InternalSpawnTypes.SpawnCauses.WORLD_SPAWNER_CAUSE)
            .named("World", context
                .firstNamed(InternalNamedCauses.WorldGeneration.WORLD, World.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected a world during generation!", context)))
            .build();
    }

    @SuppressWarnings("unchecked")
    public final void unwind(PhaseContext context) {
        final List<Entity> spawnedEntities = context.getCapturedEntitySupplier().orEmptyList();
        if (spawnedEntities.isEmpty()) {
            return;
        }
        final Cause cause = provideSpawnCause(context);

        final SpawnEntityEvent.Spawner
                event =
                SpongeEventFactory.createSpawnEntityEventSpawner(cause, spawnedEntities);
        SpongeImpl.postEvent(event);
        if (!event.isCancelled()) {
            for (Entity entity : event.getEntities()) {
                EntityUtil.getMixinWorld(entity).forceSpawnEntity(entity);
            }
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GeneralGenerationPhaseState that = (GeneralGenerationPhaseState) o;
        return Objects.equal(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", this.id)
                .toString();
    }
}
