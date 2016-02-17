package org.spongepowered.common.event.tracking;

public interface ITickingPhase extends IPhaseState, ISpawnablePhase {

    boolean isTicking();

    void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext);

}
