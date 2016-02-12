package org.spongepowered.common.event.tracking;

public interface ITickingPhase extends ITrackingPhaseState {

    boolean isTicking();

    void processPostTick(CauseTracker causeTracker);

}
