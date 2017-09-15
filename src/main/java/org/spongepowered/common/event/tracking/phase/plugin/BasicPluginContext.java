package org.spongepowered.common.event.tracking.phase.plugin;

import org.spongepowered.common.event.tracking.IPhaseState;

public class BasicPluginContext extends PluginPhaseContext<BasicPluginContext> {
    public BasicPluginContext(IPhaseState<BasicPluginContext> phaseState) {
        super(phaseState);
    }
}
