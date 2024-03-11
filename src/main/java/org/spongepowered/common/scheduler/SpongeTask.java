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
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.plugin.PluginPhase;
import org.spongepowered.plugin.PluginContainer;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class SpongeTask implements TaskProcedure {

    private final PluginContainer plugin;
    private final Consumer<ScheduledTask> executor;
    private final Time delay, interval;

    SpongeTask(final PluginContainer plugin,
               final Consumer<ScheduledTask> executor,
               final Time delay, final Time interval) {
        this.plugin = plugin;
        this.executor = executor;
        this.delay = delay;
        this.interval = interval;
    }

    @Override
    public void execute(ScheduledTask scheduledTask) throws ExecutionException {
        try (final PhaseContext<?> context = PluginPhase.State.SCHEDULED_TASK
                .createPhaseContext(PhaseTracker.getInstance())
                .source(scheduledTask)
                .container(this.plugin())) {
            context.buildAndSwitch();
            try {
                this.executor.accept(scheduledTask);
            } catch (final Throwable t) {
                throw new ExecutionException(t);
            }
        }
    }

    @Override
    public PluginContainer plugin() {
        return this.plugin;
    }

    @Override
    public Time intervalTime() {
        return this.interval;
    }

    @Override
    public Time delayTime() {
        return this.delay;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ",
                SpongeTask.class.getSimpleName() + "[", "]")
                .add("plugin=" + this.plugin.metadata().id())
                .add("delay=" + this.delay().toNanos())
                .add("interval=" + this.interval().toNanos())
                .toString();

    }

    public static final class BuilderImpl implements Task.Builder {

        private @Nullable Consumer<ScheduledTask> executor;
        private @Nullable PluginContainer plugin;

        private Time delay = Time.ZERO;
        private Time interval = Time.ZERO;

        @Override
        public Task.Builder execute(final Consumer<ScheduledTask> executor) {
            this.executor = Objects.requireNonNull(executor, "executor");
            return this;
        }

        @Override
        public Task.Builder delay(final long delay, final TemporalUnit unit) {
            Objects.requireNonNull(unit, "unit");
            if (delay < 0) {
                throw new IllegalArgumentException("Delay must be equal to or greater than zero!");
            }
            this.delay = new Time.RealTime(unit.getDuration().toNanos() * delay);
            return this;
        }

        @Override
        public Task.Builder delay(final long delay, final TimeUnit unit) {
            Objects.requireNonNull(unit, "unit");
            if (delay < 0) {
                throw new IllegalArgumentException("Delay must be equal to or greater than zero!");
            }
            this.delay = new Time.RealTime(unit.toNanos(delay));
            return this;
        }

        @Override
        public Task.Builder delay(final Ticks delay) {
            Objects.requireNonNull(delay, "delay");
            if (delay.ticks() < 0) {
                throw new IllegalArgumentException("Delay must be equal to or greater than zero!");
            }
            this.delay = new Time.TickTime(
                    delay.ticks() * SpongeScheduler.TICK_DURATION_NS);
            return this;
        }

        @Override
        public Task.Builder delay(final Duration delay) {
            Objects.requireNonNull(delay, "delay");
            this.delay = new Time.RealTime(delay.toNanos());
            return this;
        }

        @Override
        public Task.Builder interval(final Duration interval) {
            Objects.requireNonNull(interval, "interval");
            this.interval = new Time.RealTime(interval.toNanos());
            return this;
        }

        @Override
        public Task.Builder interval(final long delay, final TemporalUnit unit) {
            Objects.requireNonNull(unit, "unit");
            if (delay < 0) {
                throw new IllegalArgumentException("Delay must be equal to or greater than zero!");
            }
            this.interval = new Time.RealTime(
                    unit.getDuration().toNanos() * delay);
            return this;
        }

        @Override
        public Task.Builder interval(final long interval, final TimeUnit unit) {
            Objects.requireNonNull(unit, "unit");
            if (interval < 0) {
                throw new IllegalArgumentException("Interval must be equal to or greater than zero!");
            }
            this.interval = new Time.RealTime(unit.toNanos(interval));
            return this;
        }

        @Override
        public Task.Builder interval(final Ticks interval) {
            Objects.requireNonNull(interval, "interval");
            if (interval.ticks() < 0) {
                throw new IllegalArgumentException("Interval must be equal to or greater than zero!");
            }
            this.interval = new Time.TickTime(
                    interval.ticks() * SpongeScheduler.TICK_DURATION_NS);
            return this;
        }

        @Override
        public Task.Builder plugin(final PluginContainer plugin) {
            this.plugin = Objects.requireNonNull(plugin);
            return this;
        }

        @Override
        public Task.Builder from(final Task value) {

            final SpongeTask task = (SpongeTask)
                    Objects.requireNonNull(value, "value");
            this.executor = task.executor;
            this.plugin = task.plugin();
            this.interval = task.interval;
            this.delay = task.delay;
            return this;
        }

        @Override
        public Task.Builder reset() {
            this.executor = null;
            this.plugin = null;
            this.interval = Time.ZERO;
            this.delay = Time.ZERO;
            return this;
        }

        @Override
        public Task build() {
            Objects.requireNonNull(this.executor, "executor");
            Objects.requireNonNull(this.plugin, "plugin");

            return new SpongeTask(this.plugin, this.executor, this.delay, this.interval);
        }
    }
}
