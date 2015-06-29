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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.scheduler.Task;
import org.spongepowered.api.service.scheduler.TaskBuilder;

import java.util.concurrent.TimeUnit;

public class SpongeTaskBuilder implements TaskBuilder {

    private static final long TICK_DURATION = 50;

    private Runnable runnable;
    private ScheduledTask.TaskSynchronicity syncType;
    private String name;
    private long delay;
    private long tickDelay;
    private long interval;
    private long tickInterval;

    public SpongeTaskBuilder() {
        this.syncType = ScheduledTask.TaskSynchronicity.SYNCHRONOUS;
    }

    @Override
    public TaskBuilder async() {
        this.syncType = ScheduledTask.TaskSynchronicity.ASYNCHRONOUS;
        if (this.tickDelay != -1) {
            this.delay = this.tickDelay * TICK_DURATION;
            this.tickDelay = -1;
        }
        if (this.tickInterval != -1) {
            this.interval = this.tickInterval * TICK_DURATION;
            this.tickInterval = -1;
        }
        return this;
    }

    @Override
    public TaskBuilder execute(Runnable runnable) {
        this.runnable = runnable;
        return this;
    }

    @Override
    public TaskBuilder delay(long delay, TimeUnit unit) {
        checkArgument(delay >= 0, "Delay cannot be negative");
        this.delay = checkNotNull(unit, "unit").toMillis(delay);
        this.tickDelay = -1;
        return this;
    }

    @Override
    public TaskBuilder delay(long delay) {
        checkArgument(delay >= 0, "Delay cannot be negative");
        if (this.syncType == ScheduledTask.TaskSynchronicity.ASYNCHRONOUS) {
            this.delay = delay;
        } else {
            this.tickDelay = delay;
        }
        return this;
    }

    @Override
    public TaskBuilder interval(long interval, TimeUnit unit) {
        checkArgument(interval >= 0, "Interval cannot be negative");
        this.interval = checkNotNull(unit, "unit").toMillis(interval);
        this.tickInterval = -1;
        return this;
    }

    @Override
    public TaskBuilder interval(long interval) {
        checkArgument(interval >= 0, "Interval cannot be negative");
        if (this.syncType == ScheduledTask.TaskSynchronicity.ASYNCHRONOUS) {
            this.interval = interval;
        } else {
            this.tickInterval = interval;
        }
        return this;
    }

    @Override
    public TaskBuilder name(String name) {
        checkArgument(checkNotNull(name, "name").length() > 0, "Name cannot be empty");
        this.name = name;
        return this;
    }

    @Override
    public Task submit(Object plugin) {
        PluginContainer pluginContainer = SpongeScheduler.checkPluginInstance(plugin);
        checkState(this.runnable != null, "Runnable task not set");
        String name;
        if (this.name == null) {
            name = SpongeScheduler.getInstance().getNameFor(pluginContainer, this.syncType);
        } else {
            name = this.name;
        }
        long delay = this.tickDelay != -1 ? this.tickDelay : this.delay;
        long interval = this.tickInterval != -1 ? this.tickInterval : this.interval;
        ScheduledTask task = new ScheduledTask(this.syncType, this.runnable, name, delay, this.tickDelay != -1, interval, this.tickInterval != -1,
                pluginContainer);
        SpongeScheduler.getInstance().submit(task);
        return task;
    }
}
