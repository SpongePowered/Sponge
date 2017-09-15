package org.spongepowered.common.event.tracking.phase.entity;

import org.spongepowered.common.event.tracking.IPhaseState;

public class BasicEntityContext extends EntityContext<BasicEntityContext> {
    public BasicEntityContext(IPhaseState<? extends BasicEntityContext> state) {
        super(state);
    }
}
