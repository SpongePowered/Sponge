package org.spongepowered.common.scheduler;

public interface Cyclic {

    void enqueue(DelayedRunnable command);


    void finish();
}
