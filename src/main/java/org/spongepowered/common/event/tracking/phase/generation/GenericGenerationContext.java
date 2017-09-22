package org.spongepowered.common.event.tracking.phase.generation;

import org.spongepowered.common.event.tracking.IPhaseState;

public class GenericGenerationContext extends GenerationContext<GenericGenerationContext> {


    public GenericGenerationContext(IPhaseState<? extends GenericGenerationContext> state) {
        super(state);
    }
}
