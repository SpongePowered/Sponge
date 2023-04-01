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

import com.google.common.collect.Sets;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.plugin.PluginPhase;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.plugin.PluginContainer;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class SpongeScheduler implements Scheduler {

    private static final AtomicInteger TASK_CREATED_COUNTER = new AtomicInteger();

    private static final int TICK_DURATION_MS = 50;
    static final long TICK_DURATION_NS = TimeUnit.NANOSECONDS.convert(SpongeScheduler.TICK_DURATION_MS, TimeUnit.MILLISECONDS);

    private final String tag;

    // The simple queue of all pending (and running) ScheduledTasks
    private final Map<UUID, SpongeScheduledTask> tasks = new ConcurrentHashMap<>();
    private long sequenceNumber = 0L;

    SpongeScheduler(final String tag) {
        this.tag = tag;
    }

    /**
     * Gets the timestamp to update the timestamp of a task. This method is task
     * sensitive to support different timestamp types i.e. real time and ticks.
     *
     * <p>Subtracting the result of this method from a previously obtained
     * result should become a representation of the time that has passed
     * between those calls.</p>
     *
     * @param tickBased The task
     * @return Timestamp for the task
     */
    protected long timestamp(final boolean tickBased) {
        return System.nanoTime();
    }

    /**
     * Adds the task to the task map, will attempt to process the task on the
     * next call to {@link #runTick}.
     *
     * @param task The task to add
     */
    protected void addTask(final SpongeScheduledTask task) {
        task.setTimestamp(this.timestamp(task.task.tickBasedDelay));
        this.tasks.put(task.uniqueId(), task);
    }

    /**
     * Removes the task from the task map.
     *
     * @param task The task to remove
     */
    private void removeTask(final SpongeScheduledTask task) {
        this.tasks.remove(task.uniqueId());
    }

    @Override
    public Optional<ScheduledTask> findTask(final UUID id) {
        Objects.requireNonNull(id, "id");
        return Optional.ofNullable(this.tasks.get(id));
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
        return Sets.newHashSet(this.tasks.values());
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
    public SpongeTaskExecutorService executor(final PluginContainer plugin) {
        Objects.requireNonNull(plugin, "plugin");
        return new SpongeTaskExecutorService(() -> Task.builder().plugin(plugin), this);
    }

    @Override
    public SpongeScheduledTask submit(final Task task) {
        Objects.requireNonNull(task, "task");

        final String name = task.plugin().metadata().id() + "-" + SpongeScheduler.TASK_CREATED_COUNTER.incrementAndGet();
        return this.submit(task, name);
    }

    @Override
    public SpongeScheduledTask submit(Task task, String name) {
        Objects.requireNonNull(task, "task");
        if (Objects.requireNonNull(name, "name").isEmpty()) {
            throw new IllegalArgumentException("Task name cannot empty!");
        }

        final SpongeScheduledTask scheduledTask = new SpongeScheduledTask(this, (SpongeTask) task,
                name + "-" + this.tag + "-#" + this.sequenceNumber++);
        this.addTask(scheduledTask);
        return scheduledTask;
    }

    /**
     * Process all tasks in the map.
     */
    final void runTick() {
        this.preTick();
        try {
            this.tasks.values().forEach(this::processTask);
            this.postTick();
        } finally {
            this.finallyPostTick();
        }
    }

    /**
     * Fired when the scheduler begins to tick, before any tasks are processed.
     */
    protected void preTick() {
    }

    /**
     * Fired when the scheduler has processed all tasks.
     */
    protected void postTick() {
    }

    /**
     * Fired after tasks have attempted to be processed, in a finally block to
     * guarantee execution regardless of any error when processing a task.
     */
    protected void finallyPostTick() {
    }

    /**
     * Processes the task.
     *
     * @param task The task to process
     */
    private void processTask(final SpongeScheduledTask task) {
        // If the task is now slated to be cancelled, we just remove it as if it
        // no longer exists.
        if (task.state() == SpongeScheduledTask.ScheduledTaskState.CANCELED) {
            this.removeTask(task);
            return;
        }
        // If the task is already being processed, we wait for the previous
        // occurrence to terminate.
        if (task.state() == SpongeScheduledTask.ScheduledTaskState.EXECUTING) {
            return;
        }
        final long threshold;
        final boolean tickBased;
        // Figure out if we start a delayed Task after threshold ticks or, start
        // it after the interval (interval) of the repeating task parameter.
        if (task.state() == SpongeScheduledTask.ScheduledTaskState.WAITING) {
            threshold = task.task.delay;
            tickBased = task.task.tickBasedDelay;
        } else if (task.state() == SpongeScheduledTask.ScheduledTaskState.RUNNING) {
            threshold = task.task.interval;
            tickBased = task.task.tickBasedInterval;
        } else {
            threshold = Long.MAX_VALUE;
            tickBased = false;
        }
        // This moment is 'now'
        final long now = this.timestamp(tickBased);
        // So, if the current time minus the timestamp of the task is greater
        // than the delay to wait before starting the task, then start the task.
        // Repeating tasks get a reset-timestamp each time they are set RUNNING
        // If the task has a interval of 0 (zero) this task will not repeat, and
        // is removed after we start it.
        if (threshold <= (now - task.timestamp())) {
            task.setState(SpongeScheduledTask.ScheduledTaskState.SWITCHING);
            // It is always interval here because that's the only thing that matters
            // at this point.
            task.setTimestamp(this.timestamp(task.task.tickBasedInterval));
            this.startTask(task);
            // If task is one time shot, remove it from the map.
            if (task.task.interval == 0L) {
                this.removeTask(task);
            }
        }
    }

    /**
     * Begin the execution of a task. Exceptions are caught and logged.
     *
     * @param task The task to start
     */
    private void startTask(final SpongeScheduledTask task) {
        this.executeRunnable(() -> {
            task.setState(SpongeScheduledTask.ScheduledTaskState.EXECUTING);
            try (final @Nullable PhaseContext<@NonNull ?> context = this.createContext(task, task.task().plugin())) {
                if (context != null) {
                    context.buildAndSwitch();
                }
                try {
                    task.task.executor().accept(task);
                } catch (final Throwable t) {
                    SpongeCommon.logger().error("The Scheduler tried to run the task '{}' owned by '{}' but an error occurred.",
                            task.name(), task.task().plugin().metadata().id(), t);
                }
            } finally {
                if (!task.isCancelled()) {
                    task.setState(SpongeScheduledTask.ScheduledTaskState.RUNNING);
                }
                this.onTaskCompletion(task);
            }
        });
    }

    protected @Nullable PhaseContext<?> createContext(final SpongeScheduledTask task, final PluginContainer plugin) {
        return PluginPhase.State.SCHEDULED_TASK.createPhaseContext(PhaseTracker.getInstance())
                .source(task)
                .container(plugin);
    }

    /**
     * Run when a task has completed and is switching into
     * the {@link SpongeScheduledTask.ScheduledTaskState#RUNNING} state
     */
    protected void onTaskCompletion(final SpongeScheduledTask task) {
        // no-op for sync methods.
    }

    protected void executeRunnable(final Runnable runnable) {
        runnable.run();
    }

    public <V> Future<V> execute(final Callable<V> callable) {
        final FutureTask<V> runnable = new FutureTask<>(callable);
        this.submit(new SpongeTask.BuilderImpl().execute(runnable).plugin(Launch.instance().commonPlugin()).build());
        return runnable;
    }

    public Future<?> execute(final Runnable runnable) {
        return this.execute(() -> {
            runnable.run();
            return null;
        });
    }
}
