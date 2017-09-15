package org.spongepowered.common.event.tracking.phase.plugin;

import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;

public class PluginPhaseContext<P extends PluginPhaseContext<P>> extends PhaseContext<P> {


    protected PluginPhaseContext(IPhaseState<P> phaseState) {
        super(phaseState);
    }
}
