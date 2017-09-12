package org.spongepowered.common.event.tracking.phase.tick;

import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;

public class TickContext<T extends TickContext<T>> extends PhaseContext<T> {

    protected TickContext(IPhaseState<T> phaseState) {
        super(phaseState);
    }
}
