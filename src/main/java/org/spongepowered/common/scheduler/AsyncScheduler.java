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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.util.Functional;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.util.PrettyPrinter;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class AsyncScheduler extends SpongeScheduler {

    // Locking mechanism
    private final Lock lock = new ReentrantLock();
    private final Condition condition = this.lock.newCondition();
    private final AtomicBoolean stateChanged = new AtomicBoolean(false);
    // The dynamic thread pooling executor of asynchronous tasks.
    private final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
                                                                                   .setNameFormat("Sponge-AsyncScheduler-%d")
                                                                                   .build());
    private volatile boolean running = true;

    // Adjustable timeout for pending Tasks
    private long minimumTimeout = Long.MAX_VALUE;
    private long lastProcessingTimestamp;

    public AsyncScheduler() {
        super("A");

        final Thread thread = new Thread(AsyncScheduler.this::mainLoop);
        thread.setName("Sponge Async Scheduler Thread");
        thread.setDaemon(true);
        thread.start();
    }

    private void mainLoop() {
        this.lastProcessingTimestamp = System.nanoTime();
        while (this.running) {
            this.recalibrateMinimumTimeout();
            this.runTick();
        }
    }

    private void recalibrateMinimumTimeout() {
        this.lock.lock();
        try {
            final Set<ScheduledTask> tasks = this.tasks();
            this.minimumTimeout = Long.MAX_VALUE;
            final long now = System.nanoTime();
            for (final ScheduledTask tmpTask : tasks) {
                final SpongeScheduledTask task = (SpongeScheduledTask) tmpTask;
                if (task.state() == SpongeScheduledTask.ScheduledTaskState.EXECUTING) {
                    // bail out for this task. We'll signal when we complete the task.
                    continue;
                }
                // Recalibrate the wait delay for processing tasks before new
                // tasks cause the scheduler to process pending tasks.
                if (task.task.delay == 0 && task.task.interval == 0) {
                    this.minimumTimeout = 0;
                }
                // The time since the task last executed or was added to the map
                final long timeSinceLast = now - task.timestamp();

                if (task.task.delay > 0 && task.state() == SpongeScheduledTask.ScheduledTaskState.WAITING) {
                    // There is an delay and the task hasn't run yet
                    this.minimumTimeout = Math.min(task.task.delay - timeSinceLast, this.minimumTimeout);
                }
                if (task.task.interval > 0 && task.state().isActive) {
                    // The task repeats and has run after the initial delay
                    this.minimumTimeout = Math.min(task.task.interval - timeSinceLast, this.minimumTimeout);
                }
                if (this.minimumTimeout <= 0) {
                    break;
                }
            }
            if (!tasks.isEmpty()) {
                final long latency = System.nanoTime() - this.lastProcessingTimestamp;
                this.minimumTimeout -= (latency <= 0) ? 0 : latency;
                this.minimumTimeout = (this.minimumTimeout < 0) ? 0 : this.minimumTimeout;
            }
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    protected void addTask(final SpongeScheduledTask task) {
        this.lock.lock();
        try {
            super.addTask(task);
            this.condition.signalAll();
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    protected void preTick() {
        this.lock.lock();
        try {
            // If we have something that has indicated it needs to change,
            // don't await, just continue.
            if (!this.stateChanged.get()) {
                this.condition.await(this.minimumTimeout, TimeUnit.NANOSECONDS);
            }
            // We're processing now. Set to false.
            this.stateChanged.set(false);
        } catch (final InterruptedException ignored) {
            // The taskMap has been modified; there is work to do.
            // Continue on without handling the Exception.
        } catch (final IllegalMonitorStateException e) {
            SpongeCommon.logger().error("The scheduler internal state machine suffered a catastrophic error", e);
        }
    }

    @Override
    protected void postTick() {
        this.lastProcessingTimestamp = System.nanoTime();
    }

    @Override
    protected void finallyPostTick() {
        this.lock.unlock();
    }

    @Override
    protected void onTaskCompletion(final SpongeScheduledTask task) {
        if (task.state() == SpongeScheduledTask.ScheduledTaskState.RUNNING) {
            this.lock.lock();
            try {
                this.stateChanged.set(true);
                this.condition.signalAll();
            } finally {
                this.lock.unlock();
            }
        }
    }

    @Override
    protected void executeTaskRunnable(final SpongeScheduledTask task, final Runnable runnable) {
        this.executor.submit(runnable);
    }

    public <T> CompletableFuture<T> submit(final Callable<T> callable) {
        return Functional.asyncFailableFuture(callable, this.executor);
    }

    public void close() {
        this.running = false;
        // Cancel all tasks
        final Set<ScheduledTask> tasks = this.tasks();
        tasks.forEach(ScheduledTask::cancel);

        // Shut down the executor
        this.executor.shutdown();

        try {
            if (!this.executor.awaitTermination(10, TimeUnit.SECONDS)) {
                new PrettyPrinter()
                        .add("Sponge async scheduler failed to shut down in 10 seconds! Tasks that may have been active:")
                        .addWithIndices(tasks)
                        .add()
                        .add("We will now attempt immediate shutdown.")
                        .log(SpongeCommon.logger(), Level.WARN);

                this.executor.shutdownNow();
            }
        } catch (final InterruptedException e) {
            SpongeCommon.logger().error("The async scheduler was interrupted while awaiting shutdown!");
        }
    }
}
