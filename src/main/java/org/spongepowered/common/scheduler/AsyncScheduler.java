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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class AsyncScheduler extends SpongeScheduler {

    // Locking mechanism
    private final Lock lock = new ReentrantLock();
    private final Condition condition = this.lock.newCondition();
    // The dynamic thread pooling executor of asynchronous tasks.
    private final ExecutorService executor = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder()
                    .setNameFormat("Sponge-AsyncScheduler-%d")
                    .build());
    private boolean stateChanged;

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
        while (!executor.isShutdown())
            this.runTick();
        Thread.currentThread().interrupt();
    }

    private long recalibrateMinimumTimeout() {
        long minimumTimeout = Long.MAX_VALUE;
        final Collection<ScheduledTask> tasks = pendingTasks();

        if (!tasks.isEmpty()) {
            // Adjustable timeout for pending Tasks
            final long now = System.nanoTime();

            for (final ScheduledTask tmpTask : tasks) {
                final SpongeScheduledTask task = (SpongeScheduledTask) tmpTask;

                SpongeScheduledTask.ScheduledTaskState state = task.state();
                if (state == SpongeScheduledTask.ScheduledTaskState.EXECUTING) {
                    // bail out for this task. We'll signal when we complete the task.
                    continue;
                }
                // Recalibrate the wait delay for processing tasks before new
                // tasks cause the scheduler to process pending tasks.
                if (task.task.delay == 0 && task.task.interval == 0) {
                    minimumTimeout = 0;
                    // none of the following conditions will be me last thing
                    break;
                }
                // The time since the task last executed or was added to the map
                final long timeSinceLast = now - task.timestamp();

                if (task.task.delay > 0 && state == SpongeScheduledTask.ScheduledTaskState.WAITING) {
                    // There is an delay and the task hasn't run yet
                    minimumTimeout = Math.min(task.task.delay - timeSinceLast, minimumTimeout);
                }
                if (task.task.interval > 0 && state.isActive) {
                    // The task repeats and has run after the initial delay
                    minimumTimeout = Math.min(task.task.interval - timeSinceLast, minimumTimeout);
                }
                if (minimumTimeout <= 0) {
                    break;
                }
            }
            final long latency = System.nanoTime() - lastProcessingTimestamp;
            minimumTimeout -= (latency <= 0) ? 0 : latency;
            minimumTimeout = Math.max(0, minimumTimeout);
        }
        return minimumTimeout;
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
        try {
            this.lock.lockInterruptibly();

            // If we have something that has indicated it needs to change,
            // don't await, just continue.
            if (!stateChanged) {
                this.condition.awaitNanos(recalibrateMinimumTimeout());
            } else {
                // We're processing now. Set to false.
                this.stateChanged = false;
            }
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
                this.stateChanged = true;
                this.condition.signalAll();
            } finally {
                this.lock.unlock();
            }
        }
    }

    @Override
    protected void executeRunnable(final Runnable runnable) {
        this.executor.execute(runnable);
    }

    public <T> CompletableFuture<T> submit(final Callable<T> callable) {
        return Functional.asyncFailableFuture(callable, this.executor);
    }

    public void close() {
        boolean terminated = executor.isTerminated();
        if (!terminated) {
            // Shut down the executor
            this.executor.shutdown();

            List<ScheduledTask> tasks = drainTasks();

            try {
                if (!this.executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    new PrettyPrinter()
                            .add("Sponge async scheduler failed to shut down in 5 seconds! Tasks that may have been active:")
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
}
