package org.spongepowered.common.scheduler;

import org.spongepowered.api.scheduler.ScheduledTask;

import java.util.concurrent.Delayed;

public interface DelayedRunnable extends Delayed, Runnable, ScheduledTask {
    boolean isPeriodic();
}
