package org.spongepowered.common.event.tracking.phase.entity;

import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;

public class EntityContext<E extends EntityContext<E>> extends PhaseContext<E> {
    public EntityContext(IPhaseState<? extends E> state) {
        super(state);
    }
}
