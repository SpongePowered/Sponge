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
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;

import java.util.ArrayList;
import java.util.List;

public abstract class TrackingPhase {

    private final TrackingPhase parent;

    private final List<TrackingPhase> children = new ArrayList<>();

    public TrackingPhase(TrackingPhase parent) {
        this.parent = parent;
    }

    public TrackingPhase getParent() {
        return this.parent;
    }

    public List<TrackingPhase> getChildren() {
        return this.children;
    }

    public TrackingPhase addChild(TrackingPhase child) {
        this.children.add(child);
        return this;
    }

    public boolean requiresBlockCapturing(IPhaseState currentState) {
        return true;
    }

    public boolean ignoresEntitySpawns(IPhaseState currentState) {
        return false;
    }

    public boolean trackEntitySpawns(IPhaseState phaseState, PhaseContext context, Entity entity, Cause cause, int chunkX, int chunkZ) {
        return false;
    }

    public abstract void unwind(CauseTracker causeTracker, IPhaseState state, PhaseContext phaseContext);
}
