package org.spongepowered.common.event.tracking.phase.generation;

import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.common.event.tracking.IPhaseState;

public class PopulatorPhaseContext extends GenerationContext<PopulatorPhaseContext> {

    private PopulatorType type;

    PopulatorPhaseContext(
        IPhaseState<? extends PopulatorPhaseContext> state) {
        super(state);
    }

    public PopulatorPhaseContext populator(PopulatorType type) {
        this.type = type;
        return this;
    }

    public PopulatorType getPopulatorType() {
        return this.type;
    }
}
