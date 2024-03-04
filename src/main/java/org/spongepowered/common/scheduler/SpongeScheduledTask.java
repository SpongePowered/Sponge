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

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.plugin.PluginPhase;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class SpongeScheduledTask implements ScheduledTask, DelayedRunnable {
    private final Scheduler scheduler;
    private final String name;
    private final UUID uuid;
    private final TaskExecutor src;
    private volatile boolean cancelled = false;
    private volatile long time;
    private final Cyclic cyclic;

    SpongeScheduledTask(final Scheduler scheduler,
                        final String name, final UUID uuid,
                        final TaskExecutor src,
                        final long start,
                        final Cyclic cyclic) {
        this.scheduler = scheduler;
        this.name = name;
        this.uuid = uuid;
        this.src = src;
        this.time = start;
        this.cyclic = cyclic;
    }
    @Override
    public void run() {
        if (this.isCancelled())
            return;
        final TaskExecutor x = this.src;
        try (final PhaseContext<@NonNull ?> context = PluginPhase.State.SCHEDULED_TASK
                .createPhaseContext(PhaseTracker.getInstance())
                .source(this)
                .container(x.plugin())) {
            context.buildAndSwitch();
            try {
                x.accept(this);
            } catch (final Throwable t) {
                SpongeCommon.logger().error("The Scheduler tried to run the task '{}' owned by '{}' but an error occurred.",
                        name, x.plugin().metadata().id(), t);
            }
        } finally {
            if (isPeriodic()) {
                final long q = x.intervalNanos();
                if (x.tickBased())
                    this.time += q;
                else
                    this.time = System.nanoTime() + q;
                this.cyclic.enqueue(this);
            } else {
                this.cyclic.finish();
            }
        }
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(
                this.time - System.nanoTime(), NANOSECONDS);
    }

    @Override
    public Scheduler scheduler() {
        return this.scheduler;
    }

    @Override
    public Task task() {
        return this.src;
    }

    @Override
    public boolean cancel() {
        final boolean cancelled = !this.cancelled ||
                CANCELLED.compareAndSet(this, false, true);
        if (cancelled)
            this.cyclic.finish();
        return cancelled;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public boolean isPeriodic() {
        return this.src.intervalNanos() != 0;
    }

    @Override
    public UUID uniqueId() {
        return this.uuid;
    }

    @Override
    public String name() {
        return this.name;
    }


    private static final VarHandle CANCELLED;
    static {
        try {
            MethodHandles.Lookup l = MethodHandles.lookup();
            CANCELLED = l.findVarHandle(SpongeScheduledTask.class,
                    "cancelled", boolean.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
