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

import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.scheduler.TaskExecutorService;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.plugin.PluginContainer;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class SpongeScheduler implements Scheduler {
    private static final AtomicLong TASK_CREATED_COUNTER = new AtomicLong();

    private static final int TICK_DURATION_MS = 50;
    static final long TICK_DURATION_NS = TimeUnit.NANOSECONDS
            .convert(SpongeScheduler.TICK_DURATION_MS, TimeUnit.MILLISECONDS);

    private final String tag;
    private final ConcurrentMap<UUID, ScheduledTask> cachedTasks =
            new ConcurrentHashMap<>();
    private final AtomicLong sequenceNumber = new AtomicLong();

    SpongeScheduler(final String tag) {
        this.tag = tag;
    }

    protected long timestamp(final boolean tickBased) {
        return System.nanoTime();
    }

    protected abstract BlockingQueue<DelayedRunnable> getWorkQueue();

    @Override
    public DelayedRunnable submit(Task task) {
        final String name =
                task.plugin().metadata().id() +
                "-" +
                TASK_CREATED_COUNTER.incrementAndGet();
        return this.submit(task, name);
    }

    @Override
    public DelayedRunnable submit(Task task, String name) {
        final long number = this.sequenceNumber.getAndIncrement();
        final SpongeTask sp = (SpongeTask) task;

        final long start = sp.delay < 0
                ? -(timestamp(true) - sp.delay)
                :  timestamp(false) + sp.delay;

        final UUID uuid = new UUID(number, System.identityHashCode(this));
        final SpongeScheduledTask sc = new SpongeScheduledTask(this,
                "%s-%s-#%s".formatted(name, this.tag, number), uuid,
                sp, start,
                new Cyclic() {
                    @Override
                    public void enqueue(DelayedRunnable command) {
                        getWorkQueue().add(command);
                    }

                    @Override
                    public void finish() {
                        cachedTasks.remove(uuid);
                    }
                });
        if (getWorkQueue().add(sc))
            cachedTasks.put(uuid, sc);
        return sc;
    }

    @Override
    public Optional<ScheduledTask> findTask(final UUID id) {
        Objects.requireNonNull(id, "id");
        return Optional.ofNullable(this.cachedTasks.get(id));
    }

    @Override
    public Set<ScheduledTask> findTasks(final String pattern) {
        final Pattern searchPattern = Pattern.compile(Objects.requireNonNull(pattern, "pattern"));
        final Set<ScheduledTask> matchingTasks = this.tasks();

        final Iterator<ScheduledTask> it = matchingTasks.iterator();
        while (it.hasNext()) {
            final Matcher matcher = searchPattern.matcher(it.next().name());
            if (!matcher.matches()) {
                it.remove();
            }
        }

        return matchingTasks;
    }

    @Override
    public Set<ScheduledTask> tasks() {
        return new HashSet<>(cachedTasks.values());
    }

    @Override
    public Set<ScheduledTask> tasks(final PluginContainer plugin) {
        final String testOwnerId = Objects.requireNonNull(plugin, "plugin").metadata().id();

        final Set<ScheduledTask> allTasks = this.tasks();
        final Iterator<ScheduledTask> it = allTasks.iterator();

        while (it.hasNext()) {
            final String taskOwnerId = it.next().task().plugin().metadata().id();
            if (!testOwnerId.equals(taskOwnerId)) {
                it.remove();
            }
        }

        return allTasks;
    }
    @Override
    public TaskExecutorService executor(PluginContainer plugin) {
        Objects.requireNonNull(plugin, "plugin");
        return new SpongeTaskExecutorService(() -> Task.builder().plugin(plugin), this);
    }

    protected Collection<ScheduledTask> activeTasks() {
        return Collections.unmodifiableCollection(this.cachedTasks.values());
    }

    public <V> Future<V> execute(final Callable<V> callable) {
        final FutureTask<V> runnable = new FutureTask<>(callable);
        this.submit(new SpongeTask.BuilderImpl()
                .execute(runnable)
                .plugin(Launch.instance().commonPlugin())
                .build());
        return runnable;
    }

    public Future<?> execute(final Runnable runnable) {
        return this.execute(Executors.callable(runnable));
    }

}
