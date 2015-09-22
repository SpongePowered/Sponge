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
package org.spongepowered.common.service.scheduler;

import com.google.common.base.Objects;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.scheduler.Task;

import java.util.UUID;

/**
 * An internal representation of a {@link Task} created by a plugin.
 */
public class ScheduledTask implements Task {

    final long offset;
    final long period;
    final boolean delayIsTicks;
    final boolean intervalIsTicks;
    private final PluginContainer owner;
    private final Runnable runnableBody;
    private long timestamp;
    private ScheduledTaskState state;
    private final UUID id;
    private final String name;
    private final TaskSynchronicity syncType;
    private final String stringRepresentation;

    // Internal Task state. Not for user-service use.
    public enum ScheduledTaskState {
        /**
         * Never ran before, waiting for the offset to pass.
         */
        WAITING(false),
        /**
         * In the process of switching to the running state.
         */
        SWITCHING(true),
        /**
         * Has ran, and will continue to unless removed from the task map.
         */
        RUNNING(true),
        /**
         * Task cancelled, scheduled to be removed from the task map.
         */
        CANCELED(false);

        public final boolean isActive;

        private ScheduledTaskState(boolean active) {
            this.isActive = active;
        }
    }

    ScheduledTask(TaskSynchronicity syncType, Runnable task, String taskName, long delay, boolean delayIsTicks, long interval,
            boolean intervalIsTicks, PluginContainer pluginContainer) {
        // All tasks begin waiting.
        this.setState(ScheduledTaskState.WAITING);
        this.offset = delay;
        this.delayIsTicks = delayIsTicks;
        this.period = interval;
        this.intervalIsTicks = intervalIsTicks;
        this.owner = pluginContainer;
        this.runnableBody = task;
        this.id = UUID.randomUUID();
        this.name = taskName;
        this.syncType = syncType;

        this.stringRepresentation = Objects.toStringHelper(this)
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
        return this.offset;
    }

    @Override
    public long getInterval() {
        return this.period;
    }

    @Override
    public boolean cancel() {
        boolean success = false;
        if (this.getState() != ScheduledTask.ScheduledTaskState.RUNNING) {
            success = true;
        }
        this.setState(ScheduledTask.ScheduledTaskState.CANCELED);
        return success;
    }

    @Override
    public Runnable getRunnable() {
        return this.runnableBody;
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

    void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    ScheduledTaskState getState() {
        return this.state;
    }

    void setState(ScheduledTaskState state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return this.stringRepresentation;
    }

    public enum TaskSynchronicity {
        SYNCHRONOUS,
        ASYNCHRONOUS
    }

}
