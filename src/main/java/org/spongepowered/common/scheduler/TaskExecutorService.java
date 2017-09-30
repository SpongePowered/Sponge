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
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.scheduler.Task;

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

class TaskExecutorService extends AbstractExecutorService implements SpongeExecutorService {

    private final Supplier<Task.Builder> taskBuilderProvider;
    private final SchedulerBase scheduler;
    private final PluginContainer plugin;

    protected TaskExecutorService(Supplier<Task.Builder> taskBuilderProvider, SchedulerBase scheduler, PluginContainer plugin) {
        this.taskBuilderProvider = taskBuilderProvider;
        this.scheduler = scheduler;
        this.plugin = plugin;
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
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void execute(Runnable command) {
        this.createTask(command).submit(this.plugin);
    }

    @Override
    public SpongeFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        final FutureTask<?> runnable = new FutureTask<>(command, null);

        final Task task = this.createTask(runnable)
                .delay(delay, unit)
                .submit(this.plugin);

        return new SpongeTaskFuture<>(runnable, (ScheduledTask) task, this.scheduler);
    }

    @Override
    public <V> SpongeFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        final FutureTask<V> runnable = new FutureTask<>(callable);

        final Task task = this.createTask(runnable)
                .delay(delay, unit)
                .submit(this.plugin);

        return new SpongeTaskFuture<>(runnable, (ScheduledTask) task, this.scheduler);
    }

    @Override
    public SpongeFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        final RepeatableFutureTask<?> runnable = new RepeatableFutureTask<>(command);

        final Task task = this.createTask(runnable)
                .delay(initialDelay, unit)
                .interval(period, unit)
                .submit(this.plugin);

        // A repeatable task needs to be able to cancel itself
        runnable.setTask(task);

        return new SpongeTaskFuture<>(runnable, (ScheduledTask) task, this.scheduler);
    }

    @Override
    public SpongeFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        //Since we don't have full control over the execution, the contract needs to be a little broken
        return this.scheduleAtFixedRate(command, initialDelay, delay, unit);
    }

    private Task.Builder createTask(Runnable command) {
        return this.taskBuilderProvider.get().execute(command);
    }

    private static class SpongeTaskFuture<V> implements SpongeFuture<V> {

        private final FutureTask<V> runnable;
        private final ScheduledTask task;
        private final SchedulerBase scheduler;

        SpongeTaskFuture(FutureTask<V> runnable, ScheduledTask task, SchedulerBase scheduler) {
            this.runnable = runnable;
            this.task = task;
            this.scheduler = scheduler;
        }

        @Override
        public Task getTask() {
            return this.task;
        }

        @Override
        public boolean isPeriodic() {
            return this.task.getInterval() > 0;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            // Since these tasks are scheduled through
            // SchedulerExecutionService, they are
            // always nanotime-based, not tick-based.
            return unit.convert(
                    this.task.nextExecutionTimestamp() - this.scheduler.getTimestamp(this.task),
                    TimeUnit.NANOSECONDS
            );
        }

        @Override
        public int compareTo(Delayed other) {
            // Since getDelay may return different values for each call,
            // this check is required to correctly implement Comparable
            if (other == this) {
                return 0;
            }

            // If we are considering other sponge tasks, we can order by
            // their internal tasks
            if (other instanceof SpongeTaskFuture) {
                ScheduledTask otherTask = ((SpongeTaskFuture<?>) other).task;
                return ComparisonChain.start()
                        .compare(this.task.nextExecutionTimestamp(), otherTask.nextExecutionTimestamp())
                        .compare(this.task.getUniqueId(), otherTask.getUniqueId())
                        .result();
            }

            return Long.compare(this.getDelay(TimeUnit.NANOSECONDS), other.getDelay(TimeUnit.NANOSECONDS));
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
            return this.runnable.isCancelled() || (this.task.getState() == ScheduledTask.ScheduledTaskState.CANCELED && !this.runnable.isDone());
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

        @Nullable private Task owningTask = null;

        protected RepeatableFutureTask(Runnable runnable) {
            super(runnable, null);
        }

        protected void setTask(Task task) {
            this.owningTask = task;

            // Since it is set after being scheduled, it might have thrown
            // an exception already
            if (isDone() && !isCancelled()) {
                this.owningTask.cancel();
            }
        }

        @Override
        protected void done() {
            // A repeating task that is done but hasn't been cancelled has
            // failed exceptionally. Following the contract of
            // ScheduledExecutorService, this means the task has to be stopped.
            if (!isCancelled() && this.owningTask != null) {
                this.owningTask.cancel();
            }
        }

        @Override
        public void run() {
            super.runAndReset();
        }
    }
}
