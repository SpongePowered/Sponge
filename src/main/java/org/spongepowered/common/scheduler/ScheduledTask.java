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

import com.google.common.base.MoreObjects;

import org.spongepowered.common.relocate.co.aikar.timings.SpongeTimings;
import co.aikar.timings.Timing;

import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.concurrent.TimeUnit;

/**
 * An internal representation of a {@link Task} created by a plugin.
 */
public class ScheduledTask implements Task {

    final long offset; //nanoseconds or ticks
    final long period; //nanoseconds or ticks
    final boolean delayIsTicks;
    final boolean intervalIsTicks;
    private final PluginContainer owner;
    private final Consumer<Task> consumer;
    private long timestamp;

    // As this state is going to be read by multiple threads
    // potentially very quickly, marking this a volatile will
    // give the JVM a hint to not cache this
    private volatile ScheduledTaskState state;
    private final UUID id;
    private final String name;
    private final TaskSynchronicity syncType;
    private final String stringRepresentation;
    private Timing taskTimer;

    // Internal Task state. Not for user-service use.
    public enum ScheduledTaskState {
        /**
         * Never ran before, waiting for the offset to pass.
         */
        WAITING(false),
        /**
         * In the process of switching to the execution state.
         */
        SWITCHING(true),
        /**
         * Has ran, and will continue to unless removed from the task map.
         */
        RUNNING(true),
        /**
         * Is being executed.
         */
        EXECUTING(true),
        /**
         * Task cancelled, scheduled to be removed from the task map.
         */
        CANCELED(false);

        public final boolean isActive;

        ScheduledTaskState(boolean active) {
            this.isActive = active;
        }
    }

    ScheduledTask(TaskSynchronicity syncType, Consumer<Task> task, String taskName, long delay, boolean delayIsTicks, long interval,
            boolean intervalIsTicks, PluginContainer pluginContainer) {
        // All tasks begin waiting.
        this.setState(ScheduledTaskState.WAITING);
        this.offset = delay;
        this.delayIsTicks = delayIsTicks;
        this.period = interval;
        this.intervalIsTicks = intervalIsTicks;
        this.owner = pluginContainer;
        this.consumer = task;
        this.id = UUID.randomUUID();
        this.name = taskName;
        this.syncType = syncType;

        this.stringRepresentation = MoreObjects.toStringHelper(this)
                .add("name", this.name)
                .add("delay", this.offset)
                .add("interval", this.period)
                .add("owner", this.owner)
                .add("id", this.id)
                .add("isAsync", this.isAsynchronous())
                .toString();
    }

    @Override
    public PluginContainer getOwner() {
        return this.owner;
    }

    @Override
    public long getDelay() {
        if (this.delayIsTicks) {
            return this.offset;
        }
        return TimeUnit.NANOSECONDS.toMillis(this.offset);
    }

    @Override
    public long getInterval() {
        if (this.intervalIsTicks) {
            return this.period;
        }
        return TimeUnit.NANOSECONDS.toMillis(this.period);
    }

    @Override
    public boolean cancel() {
        boolean success = false;
        if (getState() != ScheduledTask.ScheduledTaskState.RUNNING && getState() != ScheduledTaskState.EXECUTING) {
            success = true;
        }
        this.setState(ScheduledTask.ScheduledTaskState.CANCELED);
        return success;
    }

    @Override
    public Consumer<Task> getConsumer() {
        return this.consumer;
    }

    @Override
    public UUID getUniqueId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isAsynchronous() {
        return this.syncType == TaskSynchronicity.ASYNCHRONOUS;
    }

    long getTimestamp() {
        return this.timestamp;
    }

    /**
     * Returns a timestamp after which the next execution will take place.
     * Should only be compared to
     * {@link SchedulerBase#getTimestamp(ScheduledTask)}.
     *
     * @return The next execution timestamp
     */
    long nextExecutionTimestamp() {
        if (this.state.isActive) {
            return this.timestamp + this.period;
        }
        return this.timestamp + this.offset;
    }

    void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    ScheduledTaskState getState() {
        return this.state;
    }

    void setState(ScheduledTaskState state) {
        if (this.state != ScheduledTaskState.CANCELED) {
            this.state = state;
        }
    }

    @Override
    public String toString() {
        return this.stringRepresentation;
    }

    public enum TaskSynchronicity {
        SYNCHRONOUS,
        ASYNCHRONOUS
    }

    public Timing getTimingsHandler() {
        if (this.taskTimer == null) {
            this.taskTimer = SpongeTimings.getPluginSchedulerTimings(this.owner);
        }
        return this.taskTimer;
    }
}
