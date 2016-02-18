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

import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.ISpawnableState;
import org.spongepowered.common.event.tracking.PhaseContext;

import java.util.List;

import javax.annotation.Nullable;

public class PacketPhase extends TrackingPhase {

    public enum State implements IPhaseState, ISpawnableState {
        UNKNOWN,
        INVENTORY,
        DROP_ITEM,
        DROP_INVENTORY,
        MOVEMENT,
        INTERACTION,
        ;


        @Override
        public PacketPhase getPhase() {
            return TrackingPhases.PACKET;
        }

        @Override
        public boolean isBusy() {
            return true;
        }

        @Override
        public boolean isManaged() {
            return false;
        }

        @Nullable
        @Override
        public SpawnEntityEvent createEventPostPrcess(Cause cause, CauseTracker causeTracker, List<EntitySnapshot> entitySnapshots) {


            return null;
        }
    }

    public State getStateForPacket(Packet<?> packet) {
        if (packet instanceof C07PacketPlayerDigging) {
            C07PacketPlayerDigging digging = (C07PacketPlayerDigging) packet;
            if ( digging.getStatus() == C07PacketPlayerDigging.Action.DROP_ITEM) {
                return State.DROP_ITEM;
            }
        }
        return State.UNKNOWN;
    }

    @Override
    public void unwind(CauseTracker causeTracker, IPhaseState state, PhaseContext phaseContext) {
        if (state == State.DROP_ITEM) {

        }
    }

    public PacketPhase(TrackingPhase parent) {
        super(parent);
    }
}
