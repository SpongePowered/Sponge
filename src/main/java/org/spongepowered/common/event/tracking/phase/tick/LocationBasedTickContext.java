package org.spongepowered.common.event.tracking.phase.tick;

import org.spongepowered.common.event.tracking.IPhaseState;

public class LocationBasedTickContext<T extends LocationBasedTickContext<T>> extends TickContext<T> {

    protected LocationBasedTickContext(IPhaseState<T> phaseState) {
        super(phaseState);
    }
}
