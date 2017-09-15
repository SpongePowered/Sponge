package org.spongepowered.common.event.tracking.phase.general;

import org.spongepowered.common.event.tracking.IPhaseState;

public class CommandPhaseContext extends GeneralPhaseContext<CommandPhaseContext> {
    public CommandPhaseContext(IPhaseState<CommandPhaseContext> state) {
        super(state);
    }
}
