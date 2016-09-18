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

import net.minecraft.world.WorldServer;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.TrackingPhase;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

public final class EntityPhase extends TrackingPhase {

    public static final class State {
        public static final IPhaseState DEATH = new DeathPhase();
        public static final IPhaseState DEATH_UPDATE = new DeathUpdateState();
        public static final IPhaseState CHANGING_TO_DIMENSION = new ChangingToDimensionState();
        public static final IPhaseState LEAVING_DIMENSION = new LeavingDimensionState();
        public static final IPhaseState PLAYER_WAKE_UP = new PlayerWakeUpState();

        private State() {
        }
    }

    @Override
    public void unwind(CauseTracker causeTracker, IPhaseState state, PhaseContext phaseContext) {
        if (state instanceof EntityPhaseState) {
            ((EntityPhaseState) state).unwind(causeTracker, phaseContext);
        }

    }

    @Override
    public boolean spawnEntityOrCapture(IPhaseState phaseState, PhaseContext context, Entity entity, int chunkX, int chunkZ) {
        if (phaseState == State.CHANGING_TO_DIMENSION) {
            final WorldServer worldServer = context.firstNamed(InternalNamedCauses.Teleporting.TARGET_WORLD, WorldServer.class)
                    .orElseThrow(TrackingUtil.throwWithContext("Expected to capture the target World for a teleport!", context));
            ((IMixinWorldServer) worldServer).forceSpawnEntity(entity);
            return true;
        }
        return super.spawnEntityOrCapture(phaseState, context, entity, chunkX, chunkZ);
    }

    @Override
    public boolean doesCaptureEntityDrops(IPhaseState currentState) {
        return true;
    }


    public static EntityPhase getInstance() {
        return Holder.INSTANCE;
    }

    private EntityPhase() {
    }

    private static final class Holder {
        static final EntityPhase INSTANCE = new EntityPhase();
    }

}
