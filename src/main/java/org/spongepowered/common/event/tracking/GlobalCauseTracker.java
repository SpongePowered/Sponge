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

import net.minecraft.world.WorldServer;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.world.WorldManager;

public final class GlobalCauseTracker extends BaseCauseTracker {

    private static final GlobalCauseTracker INSTANCE = new GlobalCauseTracker();

    private GlobalCauseTracker() {
    }

    public static GlobalCauseTracker getInstance() {
        return INSTANCE;
    }

    @Override
    public void switchToPhase(IPhaseState state, PhaseContext phaseContext) {
        super.switchToPhase(state, phaseContext);
        for (WorldServer world : WorldManager.getWorlds()) {
            ((IMixinWorldServer) world).getCauseTracker().switchToPhase(state, phaseContext.copy());
        }
    }

    @Override
    protected void doCompletePhase(PhaseData currentPhaseData) {
        for (WorldServer world : WorldManager.getWorlds()) {
            ((IMixinWorldServer) world).getCauseTracker().completePhase(currentPhaseData.state);
        }
    }

    @Override
    protected String getTrackerDescription() {
        return GlobalCauseTracker.class.getSimpleName();
    }

    @Override
    protected void popFromError() {
        IPhaseState currentState = this.getCurrentState();
        super.popFromError();
        for (WorldServer server : WorldManager.getWorlds()) {
            CauseTracker worldTracker = ((IMixinWorldServer) server).getCauseTracker();
            if (!worldTracker.getCurrentState().equals(currentState)) {
                // The last phase must not have been completed.
                worldTracker.popFromError();
            }
            worldTracker.popFromError();
        }
    }

    public void addWorld(WorldServer world) {
        this.stack.forEachFromBase(data -> ((IMixinWorldServer) world).getCauseTracker()
                .switchToPhase(data.state, data.context.copy()));
    }
}
