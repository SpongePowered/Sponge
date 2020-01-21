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

import org.spongepowered.api.scheduler.Task;
import org.spongepowered.common.SpongeImpl;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AsyncScheduler extends SchedulerBase {

    // Adjustable timeout for pending Tasks
    private long minimumTimeout = Long.MAX_VALUE;
    private long lastProcessingTimestamp;
    // Locking mechanism
    private final Lock lock = new ReentrantLock();
    private final Condition condition = this.lock.newCondition();
    private final AtomicBoolean stateChanged = new AtomicBoolean(false);
    // The dynamic thread pooling executor of asynchronous tasks.
    private final ExecutorService executor = Executors.newCachedThreadPool();

    AsyncScheduler() {
        super(ScheduledTask.TaskSynchronicity.ASYNCHRONOUS);

        Thread thread = new Thread(AsyncScheduler.this::mainLoop);
        thread.setName("Sponge Async Scheduler Thread");
        thread.setDaemon(true);
        thread.start();
    }

    ExecutorService getExecutor() {
        return this.executor;
    }

    private void mainLoop() {
        this.lastProcessingTimestamp = System.nanoTime();
        while (true) {
            recalibrateMinimumTimeout();
            this.runTick();
        }
    }

    private void recalibrateMinimumTimeout() {
        this.lock.lock();
        try {
            Set<Task> tasks = this.getScheduledTasks();
            this.minimumTimeout = Long.MAX_VALUE;
            long now = System.nanoTime();
            for (Task tmpTask : tasks) {
                ScheduledTask task = (ScheduledTask) tmpTask;
                if (task.getState() == ScheduledTask.ScheduledTaskState.EXECUTING) {
                    // bail out for this task. We'll signal when we complete the task.
                    continue;
                }
                // Recalibrate the wait delay for processing tasks before new
                // tasks cause the scheduler to process pending tasks.
                if (task.offset == 0 && task.period == 0) {
                    this.minimumTimeout = 0;
                }
                // The time since the task last executed or was added to the map
                long timeSinceLast = now - task.getTimestamp();

                if (task.offset > 0 && task.getState() == ScheduledTask.ScheduledTaskState.WAITING) {
                    // There is an offset and the task hasn't run yet
                    this.minimumTimeout = Math.min(task.offset - timeSinceLast, this.minimumTimeout);
                }
                if (task.period > 0 && task.getState().isActive) {
                    // The task repeats and has run after the initial delay
                    this.minimumTimeout = Math.min(task.period - timeSinceLast, this.minimumTimeout);
                }
                if (this.minimumTimeout <= 0) {
                    break;
                }
            }
            if (!tasks.isEmpty()) {
                long latency = System.nanoTime() - this.lastProcessingTimestamp;
                this.minimumTimeout -= (latency <= 0) ? 0 : latency;
                this.minimumTimeout = (this.minimumTimeout < 0) ? 0 : this.minimumTimeout;
            }
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
        } catch (InterruptedException ignored) {
            // The taskMap has been modified; there is work to do.
            // Continue on without handling the Exception.
        } catch (IllegalMonitorStateException e) {
            SpongeImpl.getLogger().error("The scheduler internal state machine suffered a catastrophic error", e);
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
    protected void executeTaskRunnable(ScheduledTask task, Runnable runnable) {
        this.executor.submit(runnable);
    }

    @Override
    protected void addTask(ScheduledTask task) {
        this.lock.lock();
        try {
            super.addTask(task);
            this.stateChanged.set(true);
            this.condition.signalAll();
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    protected void onTaskCompletion(ScheduledTask task) {
        if (task.getState() == ScheduledTask.ScheduledTaskState.RUNNING) {
            this.lock.lock();
            try {
                this.stateChanged.set(true);
                this.condition.signalAll();
            } finally {
                this.lock.unlock();
            }
        }
    }

}
