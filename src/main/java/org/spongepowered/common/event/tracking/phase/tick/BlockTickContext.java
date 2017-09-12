package org.spongepowered.common.event.tracking.phase.tick;

import org.spongepowered.common.event.tracking.IPhaseState;

public class BlockTickContext extends LocationBasedTickContext<BlockTickContext> {

    protected BlockTickContext(IPhaseState<BlockTickContext> phaseState) {
        super(phaseState);
    }
}
