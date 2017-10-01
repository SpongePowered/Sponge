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
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.GeneralizedContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.phase.TrackingPhase;
import org.spongepowered.common.event.tracking.phase.TrackingPhases;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A generalized
 */
abstract class GeneralGenerationPhaseState<G extends GenerationContext<G>> implements IPhaseState<G> {

    private Set<IPhaseState<?>> compatibleStates = new HashSet<>();
    private boolean isBaked = false;
    private final String id;

    GeneralGenerationPhaseState(String id) {
        this.id = id;
    }

    final GeneralGenerationPhaseState addCompatibleState(IPhaseState<?> state) {
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
    public final boolean canSwitchTo(IPhaseState<?> state) {
        return this.compatibleStates.contains(state);
    }

    @Override
    public final boolean isExpectedForReEntrance() {
        return true;
    }

    @Override
    public boolean ignoresBlockEvent() {
        return true;
    }

    @Override
    public boolean ignoresBlockUpdateTick(PhaseData phaseData) {
        return true;
    }

    @Override
    public boolean requiresBlockCapturing() {
        return false;
    }

    @Override
    public final void unwind(G context) {
        final List<Entity> spawnedEntities = context.getCapturedEntitySupplier().orEmptyList();
        if (spawnedEntities.isEmpty()) {
            return;
        }
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.WORLD_SPAWNER);
    
            final SpawnEntityEvent.Spawner event = SpongeEventFactory.createSpawnEntityEventSpawner(frame.getCurrentCause(), spawnedEntities);
            SpongeImpl.postEvent(event);
            if (!event.isCancelled()) {
                for (Entity entity : event.getEntities()) {
                    EntityUtil.getMixinWorld(entity).forceSpawnEntity(entity);
                }
            }
        }
    }

    @Override
    public boolean spawnEntityOrCapture(G context, Entity entity, int chunkX, int chunkZ) {
        final ArrayList<Entity> entities = new ArrayList<>(1);
        entities.add(entity);
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(entity.getLocation().getExtent());
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.WORLD_SPAWNER);

            final SpawnEntityEvent event = SpongeEventFactory.createSpawnEntityEventSpawner(frame.getCurrentCause(), entities);
            SpongeImpl.postEvent(event);
            if (!event.isCancelled() && event.getEntities().size() > 0) {
                for (Entity item : event.getEntities()) {
                    ((IMixinWorldServer) item.getWorld()).forceSpawnEntity(item);
                }
                return true;
            }
            return false;
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
