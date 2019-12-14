/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.scheduler;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.ScheduledTaskFuture;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.scheduler.TaskExecutorService;
import org.spongepowered.api.scheduler.TaskFuture;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import javax.annotation.Nullable;

class SpongeTaskExecutorService extends AbstractExecutorService implements TaskExecutorService {

    private final Supplier<Task.Builder> taskBuilderProvider;
    private final SpongeScheduler scheduler;

    SpongeTaskExecutorService(Supplier<Task.Builder> taskBuilderProvider, SpongeScheduler scheduler) {
        this.taskBuilderProvider = taskBuilderProvider;
        this.scheduler = scheduler;
    }

    @Override
    public void shutdown() {
        // Since this class is delegating its work to SchedulerService
        // and we have no way to stopping execution without keeping
        // track of all the submitted tasks, it makes sense that
        // this ExecutionService cannot be shut down.

        // While it is technically possible to cancel all tasks for
        // a plugin through the SchedulerService, we have no way to
        // ensure those tasks were created through this interface.
    }

    @Override
    public List<Runnable> shutdownNow() {
        return ImmutableList.of();
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) {
        return false;
    }

    @Override
    public void execute(Runnable command) {
        this.submitTask(this.createTask(command).build());
    }

    @Override
    public TaskFuture<?> submit(Runnable command) {
        return this.submit(command, null);
    }

    @Override
    public <T> TaskFuture<T> submit(Runnable command, @Nullable T result) {
        final FutureTask<T> runnable = new FutureTask<>(command, result);
        final Task task = this.createTask(runnable)
                .build();
        return new LanternScheduledFuture<>(runnable, submitTask(task), this.scheduler);
    }

    @Override
    public <T> TaskFuture<T> submit(Callable<T> command) {
        final FutureTask<T> runnable = new FutureTask<>(command);
        final Task task = this.createTask(runnable)
                .build();
        return new LanternScheduledFuture<>(runnable, submitTask(task), this.scheduler);
    }

    @Override
    public ScheduledTaskFuture<?> schedule(Runnable command, long delay, TemporalUnit unit) {
        final FutureTask<?> runnable = new FutureTask<>(command, null);
        final Task task = this.createTask(runnable)
                .delay(delay, unit)
                .build();
        return new LanternScheduledFuture<>(runnable, submitTask(task), this.scheduler);
    }

    @Override
    public ScheduledTaskFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        final FutureTask<?> runnable = new FutureTask<>(command, null);
        final Task task = this.createTask(runnable)
                .delay(delay, unit)
                .build();
        return new LanternScheduledFuture<>(runnable, submitTask(task), this.scheduler);
    }

    @Override
    public <V> ScheduledTaskFuture<V> schedule(Callable<V> callable, long delay, TemporalUnit unit) {
        final FutureTask<V> runnable = new FutureTask<>(callable);
        final Task task = this.createTask(runnable)
                .delay(delay, unit)
                .build();
        return new LanternScheduledFuture<>(runnable, submitTask(task), this.scheduler);
    }

    @Override
    public <V> ScheduledTaskFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        final FutureTask<V> runnable = new FutureTask<>(callable);
        final Task task = this.createTask(runnable)
                .delay(delay, unit)
                .build();
        return new LanternScheduledFuture<>(runnable, submitTask(task), this.scheduler);
    }

    @Override
    public ScheduledTaskFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TemporalUnit unit) {
        final RepeatableFutureTask<?> runnable = new RepeatableFutureTask<>(command);
        final Task task = this.createTask(runnable)
                .delay(initialDelay, unit)
                .interval(period, unit)
                .build();
        final SpongeScheduledTask scheduledTask = this.submitTask(task);
        // A repeatable task needs to be able to cancel itself
        runnable.setTask(scheduledTask);
        return new LanternScheduledFuture<>(runnable, scheduledTask, this.scheduler);
    }

    @Override
    public ScheduledTaskFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        final RepeatableFutureTask<?> runnable = new RepeatableFutureTask<>(command);
        final Task task = this.createTask(runnable)
                .delay(initialDelay, unit)
                .interval(period, unit)
                .build();
        final SpongeScheduledTask scheduledTask = this.submitTask(task);
        // A repeatable task needs to be able to cancel itself
        runnable.setTask(scheduledTask);
        return new LanternScheduledFuture<>(runnable, scheduledTask, this.scheduler);
    }

    @Override
    public ScheduledTaskFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TemporalUnit unit) {
        // Since we don't have full control over the execution, the contract needs to be a little broken
        return scheduleAtFixedRate(command, initialDelay, delay, unit);
    }

    @Override
    public ScheduledTaskFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        // Since we don't have full control over the execution, the contract needs to be a little broken
        return scheduleAtFixedRate(command, initialDelay, delay, unit);
    }

    private Task.Builder createTask(Runnable command) {
        return this.taskBuilderProvider.get().execute(command);
    }

    private SpongeScheduledTask submitTask(Task task) {
        return this.scheduler.submit(task);
    }

    private static class LanternScheduledFuture<V> implements org.spongepowered.api.scheduler.ScheduledTaskFuture<V> {

        private final FutureTask<V> runnable;
        private final SpongeScheduledTask task;
        private final SpongeScheduler scheduler;

        LanternScheduledFuture(FutureTask<V> runnable, SpongeScheduledTask task, SpongeScheduler scheduler) {
            this.runnable = runnable;
            this.task = task;
            this.scheduler = scheduler;
        }

        @Override
        public ScheduledTask getTask() {
            return this.task;
        }

        @Override
        public boolean isPeriodic() {
            final Duration interval = this.task.task.getInterval();
            return interval.toMillis() > 0;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            // Since these tasks are scheduled through
            // SchedulerExecutionService, they are
            // always nanotime-based, not tick-based.
            return unit.convert(this.task.nextExecutionTimestamp() - this.scheduler.getTimestamp(this.task), TimeUnit.NANOSECONDS);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public int compareTo(Delayed other) {
            // Since getDelay may return different values for each call,
            // this check is required to correctly implement Comparable
            if (other == this) {
                return 0;
            }

            // If we are considering other sponge tasks, we can order by
            // their internal tasks
            if (other instanceof LanternScheduledFuture) {
                final SpongeScheduledTask otherTask = ((LanternScheduledFuture) other).task;
                return ComparisonChain.start()
                        .compare(this.task.nextExecutionTimestamp(), otherTask.nextExecutionTimestamp())
                        .compare(this.task.getUniqueId(), otherTask.getUniqueId())
                        .result();
            }

            return Long.compare(getDelay(TimeUnit.NANOSECONDS), other.getDelay(TimeUnit.NANOSECONDS));
        }

        @Override
        public void run() {
            this.runnable.run();
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            this.task.cancel(); //Ensure Sponge is not going to try to run a cancelled task.
            return this.runnable.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            // It might be externally cancelled, the runnable would never
            // run in that case
            return this.runnable.isCancelled() ||
                    (this.task.getState() == SpongeScheduledTask.ScheduledTaskState.CANCELED && !this.runnable.isDone());
        }

        @Override
        public boolean isDone() {
            return this.runnable.isDone();
        }

        @Override
        public V get() throws InterruptedException, ExecutionException {
            return this.runnable.get();
        }

        @Override
        public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return this.runnable.get(timeout, unit);
        }
    }

    /**
     * An extension of the JREs FutureTask that can be repeatedly executed,
     * required for scheduling on an interval.
     */
    private static class RepeatableFutureTask<V> extends FutureTask<V> {

        @Nullable private ScheduledTask owningTask = null;

        RepeatableFutureTask(Runnable runnable) {
            super(runnable, null);
        }

        void setTask(ScheduledTask task) {
            this.owningTask = task;

            // Since it is set after being scheduled, it might have thrown
            // an exception already
            if (this.isDone() && !this.isCancelled()) {
                this.owningTask.cancel();
            }
        }

        @Override
        protected void done() {
            // A repeating task that is done but hasn't been cancelled has
            // failed exceptionally. Following the contract of
            // ScheduledExecutorService, this means the task has to be stopped.
            if (!this.isCancelled() && this.owningTask != null) {
                this.owningTask.cancel();
            }
        }

        @Override
        public void run() {
            super.runAndReset();
        }
    }
}
