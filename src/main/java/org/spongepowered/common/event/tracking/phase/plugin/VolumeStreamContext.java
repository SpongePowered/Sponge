package org.spongepowered.common.event.tracking.phase.plugin;

import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseTracker;

public class VolumeStreamContext extends BasicPluginContext {
    VolumeStreamContext(
        IPhaseState<BasicPluginContext> phaseState,
        PhaseTracker tracker
    ) {
        super(phaseState, tracker);
    }
}
