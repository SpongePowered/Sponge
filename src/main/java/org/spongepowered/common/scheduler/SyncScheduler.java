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

import java.util.concurrent.*;

public class SyncScheduler extends SpongeScheduler {

    private final BlockingQueue<RunnableScheduledFuture<?>>
            ticksQueue = new DelayQueue<>(),
            timedQueue = new DelayQueue<>();
    private long timestamp;


    protected SyncScheduler(final String tag) {
        super(tag);
    }

    @Override
    public ScheduledFuture<?> scheduleAtTick(Runnable command, long ticks) {
        final RunnableScheduledFuture<?> f = new SchedFutureTask<>(
                command, null,
                () -> timestamp,
                ticks * TICK_DURATION_NS);
        this.ticksQueue.add(f);
        return f;
    }

    @Override
    public ScheduledFuture<?> scheduleAtTime(Runnable command, long nanos) {
        final RunnableScheduledFuture<?> f = new SchedFutureTask<>(
                command, null,
                Clock.REAL_TIME,
                nanos);
        this.timedQueue.add(f);
        return f;
    }

    public void tick() {
        timestamp += TICK_DURATION_NS;
        for (Runnable task;
             (task = this.ticksQueue.poll()) != null;
             task.run());
        for (Runnable task;
             (task = this.timedQueue.poll()) != null;
             task.run());
    }


    @Override
    public void close() throws Exception {
        throw new UnsupportedOperationException();
    }


    private static class SchedFutureTask<V>
            extends FutureTask<V>
            implements RunnableScheduledFuture<V> {
        private final Clock clock;
        private final long delay;
        SchedFutureTask(Runnable runnable, V result,
                        Clock clock, long delay) {
            super(runnable, result);
            this.clock = clock;
            this.delay = delay;
        }
        @Override
        public boolean isPeriodic() {
            return false;
        }

        @Override
        public long getDelay(@NotNull TimeUnit unit) {
            return unit.convert(
                    this.delay - clock.currentNanos(),
                    TimeUnit.NANOSECONDS);
        }
        @Override
        public int compareTo(@NotNull Delayed o) {
            return Long.compare(
                    this.getDelay(TimeUnit.NANOSECONDS),
                    o.getDelay(TimeUnit.NANOSECONDS));
        }
    }
}
