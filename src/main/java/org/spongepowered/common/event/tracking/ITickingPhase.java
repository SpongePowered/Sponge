package org.spongepowered.common.event.tracking;

public interface ITickingPhase extends ITrackingPhaseState, ISpawnablePhase {

    boolean isTicking();

    void processPostTick(CauseTracker causeTracker);

}
