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
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.scheduler.TaskExecutorService;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.plugin.PluginContainer;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class SpongeScheduler implements AbstractScheduler {
    private static final AtomicLong TASK_CREATED_COUNTER = new AtomicLong();
    private final String tag;
    private final ConcurrentMap<UUID, AbstractScheduledTask> cachedTasks =
            new ConcurrentHashMap<>();
    private final AtomicLong sequenceNumber = new AtomicLong();

    SpongeScheduler(final String tag) {
        this.tag = tag;
    }

    @Override
    public AbstractScheduledTask submit(Task task) {
        final String name =
                task.plugin().metadata().id() +
                        "-" +
                        TASK_CREATED_COUNTER.incrementAndGet();
        return this.submit(task, name);
    }


    @Override
    public AbstractScheduledTask submit(Task task, String name) {
        final long number = this.sequenceNumber.getAndIncrement();
        final TaskProcedure sp = (TaskProcedure) task;

        final long start = System.nanoTime() + sp.delay().toNanos();

        final UUID uuid = new UUID(number, System.identityHashCode(this));

        final AbstractScheduledTask scheduledTask =
                new SpongeScheduledTask(this,
                        "%s-%s-#%s".formatted(name, this.tag, number), uuid,
                        sp, start) {
                    @Override
                    public void run() {
                        super.run();
                        if (this.isCancelled() || !this.isPeriodic())
                            cachedTasks.remove(uniqueId());
                    }
        };
        cachedTasks.put(uuid, scheduledTask);
        submit(scheduledTask);
        return scheduledTask;
    }

    @Override
    public Optional<ScheduledTask> findTask(final UUID id) {
        Objects.requireNonNull(id, "id");
        return Optional.ofNullable(this.cachedTasks.get(id));
    }

    @Override
    public Set<ScheduledTask> findTasks(final String pattern) {
        final Pattern searchPattern = Pattern.compile(
                Objects.requireNonNull(pattern, "pattern"));

        return cachedTasks
                .values()
                .stream()
                .filter(x -> searchPattern.matcher(x.name()).matches())
                .collect(Collectors.toSet());
    }

    @Override
    public Set<ScheduledTask> tasks() {
        return new HashSet<>(cachedTasks.values());
    }

    @Override
    public Set<ScheduledTask> tasks(final PluginContainer plugin) {
        final String testOwnerId = Objects
                .requireNonNull(plugin, "plugin")
                .metadata().id();
        return cachedTasks
                .values()
                .stream()
                .filter(x -> {
                    final String taskOwnerId = x.task().plugin().metadata().id();
                    return testOwnerId.equals(taskOwnerId);
                }).collect(Collectors.toSet());
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
