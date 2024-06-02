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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.plugin.PluginContainer;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class SpongeTask implements Task {

    private final PluginContainer plugin;
    private final Consumer<ScheduledTask> executor;

    final long delay; // nanos
    final long interval; // nanos
    final boolean tickBasedDelay;
    final boolean tickBasedInterval;

    SpongeTask(final PluginContainer plugin, final Consumer<ScheduledTask> executor, final long delay,
            final long interval, final boolean tickBasedDelay, final boolean tickBasedInterval) {
        this.plugin = plugin;
        this.executor = executor;
        this.delay = delay;
        this.interval = interval;
        this.tickBasedDelay = tickBasedDelay;
        this.tickBasedInterval = tickBasedInterval;
    }

    @Override
    public PluginContainer plugin() {
        return this.plugin;
    }

    @Override
    public Duration delay() {
        return Duration.ofNanos(this.delay);
    }

    @Override
    public Duration interval() {
        return Duration.ofNanos(this.interval);
    }

    public Consumer<ScheduledTask> executor() {
        return this.executor;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SpongeTask.class.getSimpleName() + "[", "]")
                .add("plugin=" + this.plugin.metadata().id())
                .add("delay=" + this.delay)
                .add("interval=" + this.interval)
                .toString();
    }

    public static final class BuilderImpl implements Task.Builder {

        private @Nullable Consumer<ScheduledTask> executor;
        private @Nullable PluginContainer plugin;

        private long delay;
        private long interval;
        private boolean tickBasedDelay;
        private boolean tickBasedInterval;

        @Override
        public Task.Builder execute(final Consumer<ScheduledTask> executor) {
            this.executor = Objects.requireNonNull(executor, "executor");
            return this;
        }

        @Override
        public Task.Builder delay(final long delay, final TemporalUnit unit) {
            Objects.requireNonNull(unit);
            if (delay < 0) {
                throw new IllegalArgumentException("Delay must be equal to or greater than zero!");
            }
            this.delay = Objects.requireNonNull(unit, "unit").getDuration().toNanos() * delay;
            this.tickBasedDelay = false;
            return this;
        }

        @Override
        public Task.Builder delay(final long delay, final TimeUnit unit) {
            Objects.requireNonNull(unit);
            if (delay < 0) {
                throw new IllegalArgumentException("Delay must be equal to or greater than zero!");
            }
            this.delay = Objects.requireNonNull(unit, "unit").toNanos(delay);
            this.tickBasedDelay = false;
            return this;
        }

        @Override
        public Task.Builder delay(final Ticks delay) {
            Objects.requireNonNull(delay);
            if (delay.ticks() < 0) {
                throw new IllegalArgumentException("Delay must be equal to or greater than zero!");
            } else if (delay.isInfinite()) {
                throw new IllegalArgumentException("Delay must not be infinite!");
            }
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
            if (delay < 0) {
                throw new IllegalArgumentException("Delay must be equal to or greater than zero!");
            }
            this.interval = Objects.requireNonNull(unit, "unit").getDuration().toNanos() * delay;
            this.tickBasedInterval = false;
            return this;
        }

        @Override
        public Task.Builder interval(final long interval, final TimeUnit unit) {
            if (interval < 0) {
                throw new IllegalArgumentException("Interval must be equal to or greater than zero!");
            }
            this.interval = Objects.requireNonNull(unit, "unit").toNanos(interval);
            this.tickBasedInterval = false;
            return this;
        }

        @Override
        public Task.Builder interval(final Ticks interval) {
            Objects.requireNonNull(interval, "interval");
            if (interval.ticks() < 0) {
                throw new IllegalArgumentException("Interval must be equal to or greater than zero!");
            } else if (interval.isInfinite()) {
                throw new IllegalArgumentException("Interval must not be infinite!");
            }
            this.interval = interval.ticks() * SpongeScheduler.TICK_DURATION_NS;
            this.tickBasedInterval = true;
            return this;
        }

        @Override
        public Task.Builder plugin(final PluginContainer plugin) {
            this.plugin = Objects.requireNonNull(plugin);
            return this;
        }

        @Override
        public Task.Builder from(final Task value) {

            final SpongeTask task = (SpongeTask) Objects.requireNonNull(value, "value");
            this.executor = task.executor;
            this.plugin = task.plugin();
            this.interval = task.interval;
            this.delay = task.delay;
            this.tickBasedDelay = task.tickBasedDelay;
            this.tickBasedInterval = task.tickBasedInterval;
            return this;
        }

        @Override
        public Task.Builder reset() {
            this.executor = null;
            this.plugin = null;
            this.interval = 0;
            this.delay = 0;
            this.tickBasedDelay = false;
            this.tickBasedInterval = false;
            return this;
        }

        @Override
        public Task build() {
            Objects.requireNonNull(this.executor, "executor");
            Objects.requireNonNull(this.plugin, "plugin");

            return new SpongeTask(this.plugin, this.executor, this.delay, this.interval, this.tickBasedDelay, this.tickBasedInterval);
        }
    }
}
