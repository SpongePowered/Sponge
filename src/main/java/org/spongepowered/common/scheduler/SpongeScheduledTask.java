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

import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.common.SpongeCommon;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SpongeScheduledTask implements AbstractScheduledTask {
    private final AbstractScheduler scheduler;
    private final String name;
    private final UUID uuid;
    private final TaskProcedure src;
    private volatile boolean cancelled = false;
    private long time;

    SpongeScheduledTask(final AbstractScheduler scheduler,
                        final String name, final UUID uuid,
                        final TaskProcedure src,
                        final long start) {
        this.scheduler = scheduler;
        this.name = name;
        this.uuid = uuid;
        this.src = src;
        this.time = start;
    }
    @Override
    public void run() {
        if (this.isCancelled()) {
            return;
        }
        final TaskProcedure x = this.src;
        try {
            x.execute(this);
        } catch (final Exception ex) {
            SpongeCommon.logger().error(
                    "The Scheduler tried to run the task '{}' owned by '{}' but an error occurred.",
                    name(), x.plugin().metadata().id(),
                    ex);
        } finally {
            if (isPeriodic()) {
                this.time += x.interval().toNanos();
                VarHandle.releaseFence();
                this.scheduler.submit(this);
            }
        }
    }

    @Override
    public long getDelay(TimeUnit unit) {
        VarHandle.acquireFence();
        final long r = this.time;
        return unit.convert(r - System.nanoTime(), NANOSECONDS);
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
        return !(boolean) CANCELLED.getOpaque(this) ||
                CANCELLED.compareAndSet(this, false, true);
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public boolean isPeriodic() {
        return !this.src.interval().isZero();
    }

    @Override
    public UUID uniqueId() {
        return this.uuid;
    }

    @Override
    public String name() {
        return this.name;
    }

    // VarHandle mechanic
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
