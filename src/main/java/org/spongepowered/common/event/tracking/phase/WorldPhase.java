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

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.world.World;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.ITickingPhase;
import org.spongepowered.common.event.tracking.ITrackingPhaseState;
import org.spongepowered.common.event.tracking.TrackingHelper;
import org.spongepowered.common.interfaces.entity.IMixinEntity;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

public class WorldPhase extends TrackingPhase {

    public enum State implements ITrackingPhaseState, ITickingPhase {
        TERRAIN_GENERATION,
        CHUNK_LOADING,
        TICKING_ENTITY() {
            @Override
            public void processPostTick(CauseTracker causeTracker) {
                checkArgument(causeTracker.hasTickingEntity(), "CauseTracker is currently not ticking an entity!!!");
                causeTracker.handlePostTickCaptures(Cause.of(NamedCause.source(causeTracker.getCurrentTickEntity().get())));
                causeTracker.resetTickEntity();
            }
        },
        TICKING_TILE_ENTITY() {
            @Override
            public void processPostTick(CauseTracker causeTracker) {
                checkArgument(causeTracker.hasTickingTileEntity(), "CauseTracker is currently not ticking an entity!!!");
                causeTracker.handlePostTickCaptures(Cause.of(NamedCause.source(causeTracker.getCurrentTickTileEntity().get())));
                causeTracker.resetTickTile();
            }
        },
        TICKING_BLOCK() {
            @Override
            public void processPostTick(CauseTracker causeTracker) {
                checkArgument(causeTracker.hasTickingBlock(), "CauseTracker is currently not ticking an entity!!!");
                causeTracker.handlePostTickCaptures(Cause.of(NamedCause.source(causeTracker.getCurrentTickBlock().get())));
                causeTracker.resetTickBlock();
            }
        },
        RANDOM_TICK_BLOCK() {
            @Override
            public void processPostTick(CauseTracker causeTracker) {
                checkArgument(causeTracker.hasTickingBlock(), "CauseTracker is currently not ticking an entity!!!");
                causeTracker.handlePostTickCaptures(Cause.of(NamedCause.source(causeTracker.getCurrentTickBlock().get())));
                causeTracker.resetTickBlock();
            }
        },
        IDLE;


        @Override
        public TrackingPhase getPhase() {
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
        public boolean canSwitchTo(ITrackingPhaseState state) {
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
        public void processPostTick(CauseTracker causeTracker) {
            checkArgument(this.isTicking(), "Cannot process a tick for a non-ticking state!");
        }

        @Nullable
        @Override
        public SpawnEntityEvent createEventPostPrcess(Cause cause, List<Entity> capturedEntities, List<EntitySnapshot> entitySnapshots, World world) {
            if (this.isTicking()) {
                return SpongeEventFactory.createSpawnEntityEvent(cause, capturedEntities, entitySnapshots, world);
            } else {
                throw new IllegalStateException(String.format("Cannot create a SpawnEntityEvent if this isn't tickable!! Current phase: %s", this));
            }
        }

    }

    public WorldPhase(TrackingPhase parent) {
        super(parent);
    }
}
