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

import co.aikar.timings.Timing;
import com.google.common.base.MoreObjects;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.common.relocate.co.aikar.timings.SpongeTimings;

import java.time.Duration;
import java.util.function.Consumer;

import javax.annotation.Nullable;

public final class SpongeTask implements Task {

    final long delay; // nanos
    final long interval; // nanos
    final boolean tickBased;
    @Nullable final String customName;
    private final PluginContainer owner;
    private final Consumer<ScheduledTask> consumer;
    private final String name;
    private final String toString;

    @Nullable private Timing taskTimer;

    SpongeTask(Consumer<ScheduledTask> task, String name, String customName, PluginContainer pluginContainer,
            long delay, long interval, boolean tickBased) {
        this.delay = delay;
        this.interval = interval;
        this.owner = pluginContainer;
        this.consumer = task;
        this.name = name;
        this.customName = customName;
        this.tickBased = tickBased;
        this.toString = MoreObjects.toStringHelper(this)
                .add("name", this.name)
                .add("delay", this.delay)
                .add("interval", this.interval)
                .add("owner", this.owner)
                .toString();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public PluginContainer getOwner() {
        return this.owner;
    }

    @Override
    public Duration getDelay() {
        return Duration.ofNanos(this.delay);
    }

    @Override
    public Duration getInterval() {
        return Duration.ofNanos(this.interval);
    }

    @Override
    public Consumer<ScheduledTask> getConsumer() {
        return this.consumer;
    }

    @Override
    public String toString() {
        return this.toString;
    }

    Timing getTimingsHandler() {
        if (this.taskTimer == null) {
            this.taskTimer = SpongeTimings.getPluginSchedulerTimings(this.owner);
        }
        return this.taskTimer;
    }
}
