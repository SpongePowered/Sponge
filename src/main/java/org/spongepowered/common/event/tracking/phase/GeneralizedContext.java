package org.spongepowered.common.event.tracking.phase;

import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;

public class GeneralizedContext extends PhaseContext<GeneralizedContext> {
    public GeneralizedContext(IPhaseState<? extends GeneralizedContext> state) {
        super(state);
    }
}
