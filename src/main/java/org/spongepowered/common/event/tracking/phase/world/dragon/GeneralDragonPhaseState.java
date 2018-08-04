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
package org.spongepowered.common.event.tracking.phase.world.dragon;

import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.context.GeneralizedContext;
import org.spongepowered.common.event.tracking.phase.TrackingPhase;
import org.spongepowered.common.event.tracking.phase.TrackingPhases;

public class GeneralDragonPhaseState implements IPhaseState<GeneralizedContext> {

    @Override
    public final TrackingPhase getPhase() {
        return TrackingPhases.DRAGON;
    }

    @Override
    public GeneralizedContext createPhaseContext() {
        return new GeneralizedContext(this).addCaptures();
    }

    @Override
    public void unwind(GeneralizedContext phaseContext) {

    }

    private final String className = this.getClass().getSimpleName();

    @Override
    public String toString() {
        return this.getPhase() + "{" + this.className + "}";
    }

    /**
     * This is the post dispatch method that is automatically handled for
     * states that deem it necessary to have some post processing for
     * advanced game mechanics. This is always performed when capturing
     * has been turned on during a phases's
     * {@link IPhaseState#unwind(PhaseContext)} is
     * dispatched. The rules of post dispatch are as follows:
     * - Entering extra phases is not allowed: This is to avoid
     *  potential recursion in various corner cases.
     * - The unwinding phase context is provided solely as a root
     *  cause tracking for any nested notifications that require
     *  association of causes
     * - The unwinding phase is used with the unwinding state to
     *  further exemplify during what state that was unwinding
     *  caused notifications. This narrows down to the exact cause
     *  of the notifications.
     * - post dispatch may loop several times until no more notifications
     *  are required to be dispatched. This may include block physics for
     *  neighbor notification events.
     *
     * @param unwindingState
     * @param unwindingContext The context of the state that was unwinding,
     *     contains the root cause for the state
     * @param postContext The post dispatch context captures containing any
     */
    public void postDispatch(IPhaseState<?> unwindingState, PhaseContext<?> unwindingContext, GeneralizedContext postContext) {

    }
}
