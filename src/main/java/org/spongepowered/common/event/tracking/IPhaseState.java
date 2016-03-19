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
package org.spongepowered.common.event.tracking;

import org.spongepowered.api.world.World;
import org.spongepowered.common.event.tracking.phase.TrackingPhase;

/**
 * A literal phase state of which the {@link World} is currently running
 * in. The state itself is owned by {@link TrackingPhase}s as the phase
 * defines what to do upon
 * {@link TrackingPhase#unwind(CauseTracker, IPhaseState, PhaseContext)}.
 * As these should be enums, there's no data that should be stored on
 * this state. It can have control flow with {@link #canSwitchTo(IPhaseState)}
 * where preventing switching to another state is possible (likely points out
 * either errors or runaway states not being unwound).
 */
public interface IPhaseState {

    TrackingPhase getPhase();

    int ordinal();

    default boolean canSwitchTo(IPhaseState state) {
        return false;
    }

    default boolean ignoresTracking() {
        return false;
    }
}
