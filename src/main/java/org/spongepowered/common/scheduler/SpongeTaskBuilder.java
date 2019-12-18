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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.TemporalUnits;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public final class SpongeTaskBuilder implements Task.Builder {

    private static final AtomicInteger taskCounter = new AtomicInteger();

    @Nullable private Consumer<ScheduledTask> consumer;
    @Nullable private PluginContainer plugin;
    @Nullable private String name;
    private long delay;
    private long interval;
    private boolean tickBasedDelay;
    private boolean tickBasedInterval;

    public SpongeTaskBuilder() {
    }

    @Override
    public Task.Builder execute(Consumer<ScheduledTask> executor) {
        this.consumer = checkNotNull(executor, "executor");
        return this;
    }

    @Override
    public Task.Builder delay(long delay, TemporalUnit unit) {
        checkArgument(delay >= 0, "Delay cannot be negative");
        this.delay = checkNotNull(unit, "unit").getDuration().toNanos() * delay;
        this.tickBasedDelay = TemporalUnits.MINECRAFT_TICKS == unit;
        return this;
    }

    @Override
    public Task.Builder delay(long delay, TimeUnit unit) {
        checkArgument(delay >= 0, "Delay cannot be negative");
        this.delay = checkNotNull(unit, "unit").toNanos(delay);
        this.tickBasedDelay = false;
        return this;
    }

    @Override
    public Task.Builder delayTicks(long delay) {
        checkArgument(delay >= 0, "Delay cannot be negative");
        this.delay = delay * SpongeScheduler.TICK_DURATION_NS;
        this.tickBasedDelay = true;
        return this;
    }

    @Override
    public Task.Builder delay(Duration delay) {
        this.delay = checkNotNull(delay, "delay").toNanos();
        this.tickBasedDelay = false;
        return this;
    }

    @Override
    public Task.Builder interval(Duration interval) {
        this.interval = checkNotNull(interval, "interval").toNanos();
        this.tickBasedInterval = false;
        return this;
    }

    @Override
    public Task.Builder interval(long delay, TemporalUnit unit) {
        checkArgument(delay >= 0, "Interval cannot be negative");
        this.interval = checkNotNull(unit, "unit").getDuration().toNanos() * delay;
        this.tickBasedInterval = TemporalUnits.MINECRAFT_TICKS == unit;
        return this;
    }

    @Override
    public Task.Builder interval(long interval, TimeUnit unit) {
        checkArgument(interval >= 0, "Interval cannot be negative");
        this.interval = checkNotNull(unit, "unit").toNanos(interval);
        this.tickBasedInterval = false;
        return this;
    }

    @Override
    public Task.Builder intervalTicks(long interval) {
        checkArgument(interval >= 0, "Interval cannot be negative");
        this.interval = interval * SpongeScheduler.TICK_DURATION_NS;
        this.tickBasedInterval = true;
        return this;
    }

    @Override
    public Task.Builder name(String name) {
        checkArgument(checkNotNull(name, "name").length() > 0, "Name cannot be empty");
        this.name = name;
        return this;
    }

    @Override
    public Task.Builder plugin(PluginContainer plugin) {
        checkNotNull(plugin, "plugin");
        this.plugin = plugin;
        return this;
    }

    @Override
    public Task build() {
        checkState(this.consumer != null, "Runnable task not set");
        checkState(this.plugin != null, "Plugin not set");

        final String name;
        if (this.name == null) {
            name = this.plugin.getId() + "-" + taskCounter.incrementAndGet();
        } else {
            name = this.name;
        }
        return new SpongeTask(this.consumer, name, this.name, this.plugin, this.delay, this.interval, this.tickBasedDelay &&
                this.tickBasedInterval);
    }

    @Override
    public Task.Builder from(Task value) {
        final SpongeTask task = (SpongeTask) value;
        this.consumer = value.getConsumer();
        this.plugin = task.getOwner();
        this.interval = task.interval;
        this.delay = task.delay;
        this.tickBasedDelay = task.tickBased;
        this.tickBasedInterval = task.tickBased;
        this.name = task.customName;
        return this;
    }

    @Override
    public Task.Builder reset() {
        this.consumer = null;
        this.plugin = null;
        this.interval = 0;
        this.delay = 0;
        this.tickBasedDelay = false;
        this.tickBasedInterval = false;
        this.name = null;
        return this;
    }
}
