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
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Task;

import java.util.UUID;

/**
 * An internal representation of a {@link Task} created by a plugin.
 */
public final class SpongeScheduledTask implements ScheduledTask {

    final SpongeTask task;
    private final SpongeScheduler scheduler;
    private final UUID id;
    private final String name;
    private long timestamp;
    private ScheduledTaskState state;

    SpongeScheduledTask(SpongeScheduler scheduler, SpongeTask task, String taskName) {
        this.scheduler = scheduler;
        this.id = UUID.randomUUID();
        this.name = taskName;
        this.task = task;
        // All tasks begin waiting.
        this.state = ScheduledTaskState.WAITING;
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
    public Task getTask() {
        return this.task;
    }

    @Override
    public boolean cancel() {
        boolean success = false;
        if (this.getState() != ScheduledTaskState.RUNNING
                && this.getState() != ScheduledTaskState.EXECUTING) {
            success = true;
        }
        this.state = ScheduledTaskState.CANCELED;
        return success;
    }

    public SpongeScheduler getScheduler() {
        return this.scheduler;
    }

    long getTimestamp() {
        return this.timestamp;
    }

    void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Returns a timestamp after which the next execution will take place.
     * Should only be compared to
     * {@link SpongeScheduler#getTimestamp(SpongeScheduledTask)}.
     *
     * @return The next execution timestamp
     */
    long nextExecutionTimestamp() {
        if (this.state.isActive) {
            return this.timestamp + this.task.interval;
        }
        return this.timestamp + this.task.delay;
    }

    ScheduledTaskState getState() {
        return this.state;
    }

    void setState(ScheduledTaskState state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", this.name)
                .add("id", this.id)
                .add("task", this.task)
                .toString();
    }

    // Internal Task state. Not for user-service use.
    public enum ScheduledTaskState {
        /**
         * Never ran before, waiting for the delay to pass.
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
}
