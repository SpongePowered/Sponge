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

import static com.google.common.base.Preconditions.checkArgument;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.world.World;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.ITickingPhase;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

public class WorldPhase extends TrackingPhase {

    public enum State implements IPhaseState, ITickingPhase {
        TERRAIN_GENERATION,
        POPULATOR_RUNNING,
        CHUNK_LOADING,
        TICKING_ENTITY() {
            @Override
            public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
                final Optional<Entity> currentTickingEntity = phaseContext.firstNamed(NamedCause.SOURCE, Entity.class);
                checkArgument(currentTickingEntity.isPresent(), "Not ticking on an Entity! Please analyze the current phase context: %n%s",
                        phaseContext);
                causeTracker.handlePostTickCaptures(Cause.of(NamedCause.source(currentTickingEntity.get())), this, phaseContext);
            }
        },
        TICKING_TILE_ENTITY() {
            @Override
            public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
                final Optional<TileEntity> currentTickingTileEntity = phaseContext.firstNamed(NamedCause.SOURCE, TileEntity.class);
                checkArgument(currentTickingTileEntity.isPresent(), "Not ticking on a TileEntity! Please analyze the current phase context: %n%s",
                        phaseContext);
                causeTracker.handlePostTickCaptures(Cause.of(NamedCause.source(currentTickingTileEntity.get())), this, phaseContext);
            }
        },
        TICKING_BLOCK() {
            @Override
            public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
                final Optional<BlockSnapshot> currentTickingBlock = phaseContext.firstNamed(NamedCause.SOURCE, BlockSnapshot.class);
                checkArgument(currentTickingBlock.isPresent(), "Not ticking on a block snapshot! Please analyze the current phase context: %n%s",
                        phaseContext);
                causeTracker.handlePostTickCaptures(Cause.of(NamedCause.source(currentTickingBlock.get())), this, phaseContext);
            }
        },
        RANDOM_TICK_BLOCK() {
            @Override
            public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
                final Optional<BlockSnapshot> currentTickingBlock = phaseContext.firstNamed(NamedCause.SOURCE, BlockSnapshot.class);
                checkArgument(currentTickingBlock.isPresent(), "Not ticking on a block snapshot! Please analyze the current phase context: %n%s",
                        phaseContext);
                causeTracker.handlePostTickCaptures(Cause.of(NamedCause.source(currentTickingBlock.get())), this, phaseContext);
            }
        },
        IDLE;


        @Override
        public WorldPhase getPhase() {
            return TrackingPhases.WORLD;
        }

        @Override
        public boolean isBusy() {
            return this != IDLE;
        }

        @Override
        public boolean isManaged() {
            return false;
        }

        @Override
        public boolean canSwitchTo(IPhaseState state) {
            if (this == TERRAIN_GENERATION) {
                if (state instanceof ITickingPhase && ((ITickingPhase) state).isTicking()) {
                    return true;
                } else if (state == BlockPhase.State.BLOCK_DECAY) {
                    return true;
                }
                // I'm sure there will be more cases.
            }
            return false;
        }

        @Override
        public boolean isTicking() {
            return this == TICKING_BLOCK || this == TICKING_ENTITY || this == TICKING_TILE_ENTITY || this == RANDOM_TICK_BLOCK;
        }

        @Override
        public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
        }

        @Nullable
        @Override
        public SpawnEntityEvent createEventPostPrcess(Cause cause, CauseTracker causeTracker, List<EntitySnapshot> entitySnapshots) {
            final World world = causeTracker.getWorld();
            final List<Entity> capturedEntities = causeTracker.getCapturedEntities();
            if (this.isTicking()) {
                return SpongeEventFactory.createSpawnEntityEvent(cause, capturedEntities, entitySnapshots, world);
            } else {
                throw new IllegalStateException(String.format("Cannot create a SpawnEntityEvent if this isn't tickable!! Current phase: %s", this));
            }
        }

    }

    @Override
    public void unwind(CauseTracker causeTracker, IPhaseState state, PhaseContext phaseContext) {
        if (state instanceof ITickingPhase) {
            ((ITickingPhase) state).processPostTick(causeTracker, phaseContext);
        }
        if (state == State.TERRAIN_GENERATION) {
            causeTracker.getCapturedPopulators().clear();
        }

    }

    public WorldPhase(TrackingPhase parent) {
        super(parent);
    }

    @Override
    public WorldPhase addChild(TrackingPhase child) {
        super.addChild(child);
        return this;
    }
}
