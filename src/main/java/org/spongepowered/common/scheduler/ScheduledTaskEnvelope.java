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

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;

import java.util.UUID;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScheduledTaskEnvelope implements AbstractScheduledTask {
    private final AbstractScheduler scheduler;
    private final TaskProcedure task;
    private final String name;
    private final UUID uuid;
    private final AtomicBoolean cancelled = new AtomicBoolean();
    volatile Delayed delayed; // init ?

    ScheduledTaskEnvelope(AbstractScheduler scheduler,
                          TaskProcedure task,
                          String name, UUID uuid) {
        this.scheduler = scheduler;
        this.task = task;
        this.name = name;
        this.uuid = uuid;
    }
    @Override
    public boolean cancel() {
        return !this.cancelled.getOpaque() &&
                this.cancelled.compareAndSet(false, true);
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled.get();
    }

    @Override
    public Scheduler scheduler() {
        return this.scheduler;
    }

    @Override
    public Task task() {
        return this.task;
    }

    @Override
    public UUID uniqueId() {
        return this.uuid;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public long getDelay(@NotNull TimeUnit unit) {
        return this.delayed.getDelay(unit);
    }

    @Override
    public int compareTo(@NotNull Delayed other) {
        return this.delayed.compareTo(other);
    }
}
