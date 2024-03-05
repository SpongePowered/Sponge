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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.util.PrettyPrinter;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;

public final class AsyncScheduler extends SpongeScheduler {
    private static final int NCPU = Runtime.getRuntime().availableProcessors();
    private static final long KEEP_ALIVE_MILLIS = 10L;
    private final ThreadPoolExecutor executor;
    private final BlockingQueue<DelayedRunnable> workQueue
            = new DelayQueue<>();

    public AsyncScheduler() {
        super("A");
        this.executor = new ThreadPoolExecutor(NCPU, Integer.MAX_VALUE,
                KEEP_ALIVE_MILLIS, TimeUnit.MILLISECONDS,
                new DelayQueueAsRunnable(workQueue),
                new ThreadFactoryBuilder()
                        .setNameFormat("Sponge-AsyncScheduler-%d")
                        .build()
        );
    }

    @Override
    public void submit(DelayedRunnable task) {
        workQueue.add(task);
    }

    @Override
    public AbstractScheduledTask submit(Task task) {
        AbstractScheduledTask sst = super.submit(task);
        this.executor.prestartCoreThread();
        return sst;
    }

    public <T> CompletableFuture<T> submit(final Callable<T> callable) {
        final CompletableFuture<T> ret = new CompletableFuture<>();
        execute(() -> {
            try {
                ret.complete(callable.call());
            } catch (final Throwable e) {
                ret.completeExceptionally(e);
            }
        });
        return ret;
    }

    @Override
    public void close() {
        final ExecutorService scheduler = this.executor;
        if (scheduler.isTerminated()) {
            return;
        }
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                new PrettyPrinter()
                        .add("Sponge async scheduler failed to shut down in 5 seconds! Tasks that may have been active:")
                        .addWithIndices(activeTasks())
                        .add()
                        .add("We will now attempt immediate shutdown.")
                        .log(SpongeCommon.logger(), Level.WARN);

                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            SpongeCommon.logger().error("The async scheduler was interrupted while awaiting shutdown!");

            Thread.currentThread().interrupt();
        }
    }


    private record DelayQueueAsRunnable(
            BlockingQueue<DelayedRunnable> src
    ) implements BlockingQueue<Runnable> {

        @Override
        public Runnable poll() {
            return this.src.poll();
        }

        @Override
        public boolean add(final Runnable runnable) {
            return this.src.add(
                    new DelayedRunnable.NoDelayRunnable(runnable));
        }

        @Override
        public boolean offer(final Runnable runnable) {
            return this.src.offer(
                    new DelayedRunnable.NoDelayRunnable(runnable));
        }

        @Override
        public void put(final Runnable runnable) throws InterruptedException {
            this.src.put(new DelayedRunnable.NoDelayRunnable(runnable));
        }

        @Override
        public boolean offer(final Runnable runnable,
                             final long timeout,
                             final TimeUnit unit
        ) throws InterruptedException {
            return this.src.offer(
                    new DelayedRunnable.NoDelayRunnable(runnable),
                    timeout, unit);
        }

        @Override
        public boolean addAll(final Collection<? extends Runnable> c) {
            Objects.requireNonNull(c);
            if (c == this)
                throw new IllegalArgumentException();
            boolean modified = false;
            for (Runnable e : c)
                if (add(e))
                    modified = true;
            return modified;
        }

        @Override
        public @NotNull Runnable take() throws InterruptedException {
            return this.src.take();
        }

        @Override
        public Runnable poll(final long timeout,
                             final TimeUnit unit
        ) throws InterruptedException {
            return this.src.poll(timeout, unit);
        }

        @Override
        public Runnable remove() {
            return this.src.remove();
        }

        @Override
        public Runnable peek() {
            return this.src.peek();
        }

        @Override
        public int size() {
            return this.src.size();
        }

        @Override
        public void clear() {
            this.src.clear();
        }

        @Override
        public int remainingCapacity() {
            return this.src.remainingCapacity();
        }

        @Override
        public boolean remove(final Object o) {
            return this.src.remove(o);
        }

        @Override
        public DelayedRunnable element() {
            return this.src.element();
        }

        @Override
        public boolean isEmpty() {
            return this.src.isEmpty();
        }

        @Override
        public boolean contains(final Object o) {
            return this.src.contains(o);
        }

        @Override
        public int drainTo(final Collection<? super Runnable> c) {
            return this.src.drainTo(c);
        }

        @Override
        public int drainTo(final Collection<? super Runnable> c,
                           final int maxElements) {
            return this.src.drainTo(c, maxElements);
        }

        @Override
        public boolean containsAll(final Collection<?> c) {
            return this.src.containsAll(c);
        }

        @Override
        public boolean removeAll(final Collection<?> c) {
            return this.src.removeAll(c);
        }

        @Override
        public boolean retainAll(final Collection<?> c) {
            return this.src.retainAll(c);
        }

        @Override
        public Object[] toArray() {
            return this.src.toArray();
        }

        @Override
        public <T> T[] toArray(final T[] a) {
            return this.src.toArray(a);
        }

        @Override
        public <T> T[] toArray(final IntFunction<T[]> generator) {
            return this.src.toArray(generator);
        }

        @Override
        public Iterator<Runnable> iterator() {
            return new Itr<>(this.src.iterator());
        }

        @Override
        public boolean equals(final Object o) {
            return this.src.equals(o);
        }

        @Override
        public String toString() {
            return this.src.toString();
        }

        private record Itr<E extends Runnable>(
                Iterator<E> src
        ) implements Iterator<Runnable> {

            @Override
            public boolean hasNext() {
                return this.src.hasNext();
            }

            @Override
            public Runnable next() {
                return this.src.next();
            }

            @Override
            public void remove() {
                this.src.remove();
            }

            @Override
            public void forEachRemaining(final Consumer<? super Runnable> action) {
                this.src.forEachRemaining(action);
            }
        }
    }
}
