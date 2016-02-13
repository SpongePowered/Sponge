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
package org.spongepowered.common.event.tracking.phase;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.world.World;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.ISpawnablePhase;
import org.spongepowered.common.event.tracking.ITickingPhase;
import org.spongepowered.common.event.tracking.IPhaseState;

import java.util.List;

import javax.annotation.Nullable;

public class SpawningPhase extends TrackingPhase {

    public enum State implements IPhaseState, ISpawnablePhase {
        DEATH_DROPS_SPAWNING(true),
        DROP_ITEM,
        WORLD_SPAWNER_SPAWNING,
        CHUNK_SPAWNING,
        ENTITY_SPAWNING,
        PROCESSING,
        COMPLETE;

        private final boolean managed;

        State() {
            this.managed = false;
        }

        State(boolean managed) {
            this.managed = managed;
        }

        @Override
        public boolean isBusy() {
            return this != COMPLETE;
        }

        @Override
        public boolean isManaged() {
            return this.managed;
        }

        @Override
        public boolean canSwitchTo(IPhaseState state) {
            return this == CHUNK_SPAWNING && state instanceof ITickingPhase;
        }

        @Override
        public SpawningPhase getPhase() {
            return TrackingPhases.SPAWNING;
        }

        public void process(Cause cause, CauseTracker causeTracker) {
            causeTracker.handlePostTickCaptures(cause);
        }

        @Nullable
        @Override
        public SpawnEntityEvent createEventPostPrcess(Cause cause, CauseTracker causeTracker, List<EntitySnapshot> entitySnapshots) {
            final World world = causeTracker.getWorld();
            final List<Entity> capturedEntities = causeTracker.getCapturedEntities();
            if (this == WORLD_SPAWNER_SPAWNING) {
                return SpongeEventFactory.createSpawnEntityEventSpawner(cause, capturedEntities, entitySnapshots, world);
            } else if (this == CHUNK_SPAWNING) {
                return SpongeEventFactory.createSpawnEntityEventChunkLoad(cause, capturedEntities, entitySnapshots, world);
            } else {
                return SpongeEventFactory.createSpawnEntityEvent(cause, capturedEntities, entitySnapshots, world);
            }
        }
    }

    public SpawningPhase(TrackingPhase parent) {
        super(parent);
    }

}
