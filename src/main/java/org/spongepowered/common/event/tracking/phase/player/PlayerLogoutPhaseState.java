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
package org.spongepowered.common.event.tracking.phase.player;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.PooledPhaseState;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.context.GeneralizedContext;

final class PlayerLogoutPhaseState extends PooledPhaseState<GeneralizedContext> implements IPhaseState<GeneralizedContext> {

    PlayerLogoutPhaseState() {
    }

    @Override
    public GeneralizedContext createNewContext(final PhaseTracker tracker) {
        return new GeneralizedContext(this, tracker).addCaptures();
    }

    @Override
    public void unwind(final GeneralizedContext phaseContext) {
        final Player player = phaseContext.getSource(Player.class)
            .orElseThrow(TrackingUtil.throwWithContext("Expected to be processing a player leaving, but we're not!", phaseContext));
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(player);
            phaseContext.getCapturedItemsSupplier().acceptAndClearIfNotEmpty(items -> {
                SpongeCommonEventFactory.callDropItemDispense(items, phaseContext);
            });
            phaseContext.getCapturedEntitySupplier().acceptAndClearIfNotEmpty(entities -> {

            });

            // TODO - Determine if we need to pass the supplier or perform some parameterized
            //  process if not empty method on the capture object.
            TrackingUtil.processBlockCaptures(phaseContext);
        }
    }

    private final String desc = TrackingUtil.phaseStateToString("Player", this);

    @Override
    public String toString() {
        return this.desc;
    }
}
