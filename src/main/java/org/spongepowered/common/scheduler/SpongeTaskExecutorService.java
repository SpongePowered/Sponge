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

import com.google.common.collect.ImmutableList;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.ScheduledTaskFuture;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.scheduler.TaskExecutorService;
import org.spongepowered.api.scheduler.TaskFuture;

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

class SpongeTaskExecutorService extends AbstractExecutorService implements TaskExecutorService {

    private final Supplier<Task.Builder> taskBuilderProvider;
    private final SpongeScheduler scheduler;

    SpongeTaskExecutorService(final Supplier<Task.Builder> taskBuilderProvider, final SpongeScheduler scheduler) {
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
    public boolean awaitTermination(final long timeout, final TimeUnit unit) {
        return false;
    }

    @Override
    public void execute(final Runnable command) {
        this.submitTask(this.createTask(command).build());
    }

    @Override
    public TaskFuture<?> submit(final Runnable command) {
        return this.submit(command, null);
    }

    @Override
    public <T> TaskFuture<T> submit(final Runnable command, final @Nullable T result) {
        final FutureTask<T> runnable = new FutureTask<>(command, result);
        final Task task = this.createTask(runnable)
                .build();
        return new SpongeScheduledFuture<>(runnable, this.submitTask(task));
    }

    @Override
    public <T> TaskFuture<T> submit(final Callable<T> command) {
        final FutureTask<T> runnable = new FutureTask<>(command);
        final Task task = this.createTask(runnable)
                .build();
        return new SpongeScheduledFuture<>(runnable, this.submitTask(task));
    }

    @Override
    public ScheduledTaskFuture<?> schedule(final Runnable command, final long delay, final TemporalUnit unit) {
        final FutureTask<?> runnable = new FutureTask<>(command, null);
        final Task task = this.createTask(runnable)
                .delay(delay, unit)
                .build();
        return new SpongeScheduledFuture<>(runnable, this.submitTask(task));
    }

    @Override
    public ScheduledTaskFuture<?> schedule(final Runnable command, final long delay, final TimeUnit unit) {
        final FutureTask<?> runnable = new FutureTask<>(command, null);
        final Task task = this.createTask(runnable)
                .delay(delay, unit)
                .build();
        return new SpongeScheduledFuture<>(runnable, this.submitTask(task));
    }

    @Override
    public <V> ScheduledTaskFuture<V> schedule(final Callable<V> callable, final long delay, final TemporalUnit unit) {
        final FutureTask<V> runnable = new FutureTask<>(callable);
        final Task task = this.createTask(runnable)
                .delay(delay, unit)
                .build();
        return new SpongeScheduledFuture<>(runnable, this.submitTask(task));
    }

    @Override
    public <V> ScheduledTaskFuture<V> schedule(final Callable<V> callable, final long delay, final TimeUnit unit) {
        final FutureTask<V> runnable = new FutureTask<>(callable);
        final Task task = this.createTask(runnable)
                .delay(delay, unit)
                .build();
        return new SpongeScheduledFuture<>(runnable, this.submitTask(task));
    }

    @Override
    public ScheduledTaskFuture<?> scheduleAtFixedRate(final Runnable command, final long initialDelay, final long period, final TemporalUnit unit) {
        final RepeatableFutureTask<?> runnable = new RepeatableFutureTask<>(command);
        final Task task = this.createTask(runnable)
                .delay(initialDelay, unit)
                .interval(period, unit)
                .build();
        final AbstractScheduledTask scheduledTask = this.submitTask(task);
        // A repeatable task needs to be able to cancel itself
        runnable.setTask(scheduledTask);
        return new SpongeScheduledFuture<>(runnable, scheduledTask);
    }

    @Override
    public ScheduledTaskFuture<?> scheduleAtFixedRate(final Runnable command, final long initialDelay, final long period, final TimeUnit unit) {
        final RepeatableFutureTask<?> runnable = new RepeatableFutureTask<>(command);
        final Task task = this.createTask(runnable)
                .delay(initialDelay, unit)
                .interval(period, unit)
                .build();
        final AbstractScheduledTask scheduledTask = this.submitTask(task);
        // A repeatable task needs to be able to cancel itself
        runnable.setTask(scheduledTask);
        return new SpongeScheduledFuture<>(runnable, scheduledTask);
    }

    @Override
    public ScheduledTaskFuture<?> scheduleWithFixedDelay(final Runnable command, final long initialDelay, final long delay, final TemporalUnit unit) {
        // Since we don't have full control over the execution, the contract needs to be a little broken
        return this.scheduleAtFixedRate(command, initialDelay, delay, unit);
    }

    @Override
    public ScheduledTaskFuture<?> scheduleWithFixedDelay(final Runnable command, final long initialDelay, final long delay, final TimeUnit unit) {
        // Since we don't have full control over the execution, the contract needs to be a little broken
        return this.scheduleAtFixedRate(command, initialDelay, delay, unit);
    }

    private Task.Builder createTask(final Runnable command) {
        return this.taskBuilderProvider.get().execute(command);
    }

    private AbstractScheduledTask submitTask(final Task task) {
        return this.scheduler.submit(task);
    }

    private static class SpongeScheduledFuture<V> implements org.spongepowered.api.scheduler.ScheduledTaskFuture<V> {

        private final FutureTask<V> runnable;
        private final AbstractScheduledTask task;

        SpongeScheduledFuture(final FutureTask<V> runnable,
                              final AbstractScheduledTask task) {
            this.runnable = runnable;
            this.task = task;
        }

        @Override
        public ScheduledTask task() {
            return this.task;
        }

        @Override
        public boolean isPeriodic() {
            return !this.task.task().interval().isZero();
        }

        @Override
        public long getDelay(final TimeUnit unit) {
            return this.task.getDelay(unit);
        }

        @Override
        public int compareTo(final Delayed other) {
            return this.task.compareTo(other);
        }

        @Override
        public void run() {
            this.runnable.run();
        }

        @Override
        public boolean cancel(final boolean mayInterruptIfRunning) {
            this.task.cancel(); //Ensure Sponge is not going to try to run a cancelled task.
            return this.runnable.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            // It might be externally cancelled, the runnable would never
            // run in that case
            return this.runnable.isCancelled() ||
                    (this.task.isCancelled() && !this.runnable.isDone());
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
        public V get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return this.runnable.get(timeout, unit);
        }
    }

    /**
     * An extension of the JREs FutureTask that can be repeatedly executed,
     * required for scheduling on an interval.
     */
    private static class RepeatableFutureTask<V> extends FutureTask<V> {

        private @Nullable ScheduledTask owningTask = null;

        RepeatableFutureTask(final Runnable runnable) {
            super(runnable, null);
        }

        void setTask(final ScheduledTask task) {
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
