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

import com.google.common.base.Preconditions;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.plugin.PluginContainer;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public final class SpongeTaskBuilder implements Task.Builder {

    private static final AtomicInteger taskCounter = new AtomicInteger();

    private @Nullable Consumer<ScheduledTask> consumer;
    private @Nullable PluginContainer plugin;
    private @Nullable String name;
    private long delay;
    private long interval;
    private boolean tickBasedDelay;
    private boolean tickBasedInterval;

    public SpongeTaskBuilder() {
    }

    @Override
    public Task.Builder execute(final Consumer<ScheduledTask> executor) {
        this.consumer = Objects.requireNonNull(executor, "executor");
        return this;
    }

    @Override
    public Task.Builder delay(final long delay, final TemporalUnit unit) {
        Preconditions.checkArgument(delay >= 0, "Delay cannot be negative");
        this.delay = Objects.requireNonNull(unit, "unit").getDuration().toNanos() * delay;
        this.tickBasedDelay = false;
        return this;
    }

    @Override
    public Task.Builder delay(final long delay, final TimeUnit unit) {
        Preconditions.checkArgument(delay >= 0, "Delay cannot be negative");
        this.delay = Objects.requireNonNull(unit, "unit").toNanos(delay);
        this.tickBasedDelay = false;
        return this;
    }

    @Override
    public Task.Builder delay(final Ticks delay) {
        Preconditions.checkArgument(delay.ticks() >= 0, "Delay cannot be negative");
        this.delay = delay.ticks() * SpongeScheduler.TICK_DURATION_NS;
        this.tickBasedDelay = true;
        return this;
    }

    @Override
    public Task.Builder delay(final Duration delay) {
        this.delay = Objects.requireNonNull(delay, "delay").toNanos();
        this.tickBasedDelay = false;
        return this;
    }

    @Override
    public Task.Builder interval(final Duration interval) {
        this.interval = Objects.requireNonNull(interval, "interval").toNanos();
        this.tickBasedInterval = false;
        return this;
    }

    @Override
    public Task.Builder interval(final long delay, final TemporalUnit unit) {
        Preconditions.checkArgument(delay >= 0, "Interval cannot be negative");
        this.interval = Objects.requireNonNull(unit, "unit").getDuration().toNanos() * delay;
        this.tickBasedInterval = false;
        return this;
    }

    @Override
    public Task.Builder interval(final long interval, final TimeUnit unit) {
        Preconditions.checkArgument(interval >= 0, "Interval cannot be negative");
        this.interval = Objects.requireNonNull(unit, "unit").toNanos(interval);
        this.tickBasedInterval = false;
        return this;
    }

    @Override
    public Task.Builder interval(final Ticks interval) {
        Preconditions.checkArgument(interval.ticks() >= 0, "Interval cannot be negative");
        this.interval = interval.ticks() * SpongeScheduler.TICK_DURATION_NS;
        this.tickBasedInterval = true;
        return this;
    }

    @Override
    public Task.Builder name(final String name) {
        Preconditions.checkArgument(!Objects.requireNonNull(name, "name").isEmpty(), "Name cannot be empty");
        this.name = name;
        return this;
    }

    @Override
    public Task.Builder plugin(final PluginContainer plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        return this;
    }

    @Override
    public Task build() {
        if (this.consumer == null) {
            throw new IllegalStateException("Runnable task not set");
        }
        if (this.plugin == null) {
            throw new IllegalStateException("Plugin not set");
        }

        final String name;
        if (this.name == null) {
            name = this.plugin.metadata().id() + "-" + SpongeTaskBuilder.taskCounter.incrementAndGet();
        } else {
            name = this.name;
        }
        return new SpongeTask(this.consumer, name, this.name, this.plugin, this.delay, this.interval,
                this.tickBasedDelay, this.tickBasedInterval);
    }

    @Override
    public Task.Builder from(final Task value) {
        final SpongeTask task = (SpongeTask) value;
        this.consumer = value.consumer();
        this.plugin = task.owner();
        this.interval = task.interval;
        this.delay = task.delay;
        this.tickBasedDelay = task.tickBasedDelay;
        this.tickBasedInterval = task.tickBasedInterval;
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
