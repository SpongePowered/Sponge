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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class SpongeTaskBuilder implements Task.Builder {

    private final SpongeScheduler scheduler;
    private Consumer<Task> consumer;
    private ScheduledTask.TaskSynchronicity syncType;
    private String name;
    private long delay; //nanoseconds or ticks
    private long interval; //nanoseconds or ticks
    private boolean delayIsTicks;
    private boolean intervalIsTicks;

    public SpongeTaskBuilder(SpongeScheduler scheduler) {
        this.scheduler = scheduler;
        this.syncType = ScheduledTask.TaskSynchronicity.SYNCHRONOUS;
    }

    @Override
    public Task.Builder async() {
        this.syncType = ScheduledTask.TaskSynchronicity.ASYNCHRONOUS;
        return this;
    }

    @Override
    public Task.Builder execute(Consumer<Task> executor) {
        this.consumer = checkNotNull(executor, "executor");
        return this;
    }

    @Override
    public Task.Builder delay(long delay, TimeUnit unit) {
        checkArgument(delay >= 0, "Delay cannot be negative");
        this.delay = checkNotNull(unit, "unit").toNanos(delay);
        this.delayIsTicks = false;
        return this;
    }

    @Override
    public Task.Builder delayTicks(long delay) {
        checkArgument(delay >= 0, "Delay cannot be negative");
        this.delay = delay;
        this.delayIsTicks = true;
        return this;
    }

    @Override
    public Task.Builder interval(long interval, TimeUnit unit) {
        checkArgument(interval >= 0, "Interval cannot be negative");
        this.interval = checkNotNull(unit, "unit").toNanos(interval);
        this.intervalIsTicks = false;
        return this;
    }

    @Override
    public Task.Builder intervalTicks(long interval) {
        checkArgument(interval >= 0, "Interval cannot be negative");
        this.interval = interval;
        this.intervalIsTicks = true;
        return this;
    }

    @Override
    public Task.Builder name(String name) {
        checkArgument(checkNotNull(name, "name").length() > 0, "Name cannot be empty");
        this.name = name;
        return this;
    }

    @Override
    public Task submit(Object plugin) {
        PluginContainer pluginContainer = this.scheduler.checkPluginInstance(plugin);
        checkState(this.consumer != null, "Runnable task not set");
        String name;
        if (this.name == null) {
            name = this.scheduler.getNameFor(pluginContainer, this.syncType);
        } else {
            name = this.name;
        }
        long delay = this.delay;
        long interval = this.interval;
        boolean delayIsTicks = this.delayIsTicks;
        boolean intervalIsTicks = this.intervalIsTicks;
        if (this.syncType == ScheduledTask.TaskSynchronicity.ASYNCHRONOUS) {
            delay = delayIsTicks ? delay * SpongeScheduler.TICK_DURATION_NS : delay;
            interval = intervalIsTicks ? interval * SpongeScheduler.TICK_DURATION_NS : interval;
            delayIsTicks = intervalIsTicks = false;
        }
        ScheduledTask task = new ScheduledTask(this.syncType, this.consumer, name, delay, delayIsTicks, interval, intervalIsTicks, pluginContainer);
        this.scheduler.submit(task);
        return task;
    }

    @Override
    public Task.Builder from(Task value) {
        this.syncType = value.isAsynchronous() ? ScheduledTask.TaskSynchronicity.ASYNCHRONOUS : ScheduledTask.TaskSynchronicity.SYNCHRONOUS;
        this.consumer = value.getConsumer();
        this.interval = value.getInterval();
        this.delay = value.getDelay();
        this.delayIsTicks = false;
        this.name = value.getName();
        return this;
    }

    @Override
    public Task.Builder reset() {
        this.syncType = ScheduledTask.TaskSynchronicity.SYNCHRONOUS;
        this.consumer = null;
        this.interval = 0;
        this.delay = 0;
        this.delayIsTicks = false;
        this.name = null;
        return this;
    }
}
