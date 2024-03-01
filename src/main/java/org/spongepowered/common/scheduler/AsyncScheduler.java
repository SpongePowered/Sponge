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
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.util.PrettyPrinter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public final class AsyncScheduler extends SpongeScheduler implements AutoCloseable {
    private static final long KEEP_ALIVE_MILLIS = 12L;
    private final ThreadPoolExecutor executor;
    private final BlockingQueue<DelayedRunnable> workQueue
            = new DelayQueue<>();

    public AsyncScheduler() {
        super("A");
        this.executor = new ThreadPoolExecutor(1, Integer.MAX_VALUE,
                KEEP_ALIVE_MILLIS, TimeUnit.MILLISECONDS,
                new DelayQueueAsRunnable<>(workQueue),
                new ThreadFactoryBuilder()
                        .setNameFormat("Sponge-AsyncScheduler-%d")
                        .build()
        );
    }

    @Override
    protected BlockingQueue<DelayedRunnable> getWorkQueue() {
        return this.workQueue;
    }

    @Override
    public DelayedRunnable submit(Task task) {
        this.executor.prestartCoreThread();
        return super.submit(task);
    }

    public <T> CompletableFuture<T> submit(final Callable<T> callable) {
        return this.asyncFailableFuture(callable, this.executor);
    }

    private <T> CompletableFuture<T> asyncFailableFuture(Callable<T> call, Executor exec) {
        final CompletableFuture<T> ret = new CompletableFuture<>();
        exec.execute(() -> {
            try {
                ret.complete(call.call());
            } catch (Throwable e) {
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
        }
    }
}
