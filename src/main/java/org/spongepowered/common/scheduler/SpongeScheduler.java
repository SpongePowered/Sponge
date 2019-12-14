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

import static com.google.common.base.Preconditions.checkNotNull;

import co.aikar.timings.Timing;
import com.google.common.collect.Sets;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.relocate.co.aikar.timings.TimingsManager;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

public abstract class SpongeScheduler implements Scheduler {

    protected static final int TICK_DURATION_MS = 50;
    protected static final long TICK_DURATION_NS = TimeUnit.NANOSECONDS.convert(TICK_DURATION_MS, TimeUnit.MILLISECONDS);

    private final String tag;

    // The simple queue of all pending (and running) ScheduledTasks
    private final Map<UUID, SpongeScheduledTask> taskMap = new ConcurrentHashMap<>();
    private long sequenceNumber = 0L;

    protected SpongeScheduler(String tag) {
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
     * @param task The task
     * @return Timestamp for the task
     */
    protected long getTimestamp(SpongeScheduledTask task) {
        return System.nanoTime();
    }

    /**
     * Adds the task to the task map, will attempt to process the task on the
     * next call to {@link #runTick}.
     *
     * @param task The task to add
     */
    protected void addTask(SpongeScheduledTask task) {
        task.setTimestamp(getTimestamp(task));
        this.taskMap.put(task.getUniqueId(), task);
    }

    /**
     * Removes the task from the task map.
     *
     * @param task The task to remove
     */
    protected void removeTask(SpongeScheduledTask task) {
        this.taskMap.remove(task.getUniqueId());
    }

    @Override
    public SpongeScheduledTask submit(Task task) {
        checkNotNull(task, "task");
        final SpongeScheduledTask scheduledTask = new SpongeScheduledTask(this, (SpongeTask) task,
                task.getName() + "-" + this.tag + "-#" + this.sequenceNumber++);
        addTask(scheduledTask);
        return scheduledTask;
    }

    @Override
    public Optional<ScheduledTask> getTaskById(UUID id) {
        checkNotNull(id, "id");
        synchronized (this.taskMap) {
            return Optional.ofNullable(this.taskMap.get(id));
        }
    }

    @Override
    public Set<ScheduledTask> getTasksByName(String pattern) {
        checkNotNull(pattern, "pattern");
        final Pattern searchPattern = Pattern.compile(pattern);
        final Set<ScheduledTask> matchingTasks = getTasks();

        final Iterator<ScheduledTask> it = matchingTasks.iterator();
        while (it.hasNext()) {
            final Matcher matcher = searchPattern.matcher(it.next().getName());
            if (!matcher.matches()) {
                it.remove();
            }
        }

        return matchingTasks;
    }

    @Override
    public Set<ScheduledTask> getTasks() {
        synchronized (this.taskMap) {
            return Sets.newHashSet(this.taskMap.values());
        }
    }

    @Override
    public Set<ScheduledTask> getTasksByPlugin(PluginContainer plugin) {
        checkNotNull(plugin, "plugin");
        final String testOwnerId = plugin.getId();

        final Set<ScheduledTask> allTasks = getTasks();
        final Iterator<ScheduledTask> it = allTasks.iterator();

        while (it.hasNext()) {
            final String taskOwnerId = it.next().getOwner().getId();
            if (!testOwnerId.equals(taskOwnerId)) {
                it.remove();
            }
        }

        return allTasks;
    }

    @Override
    public SpongeTaskExecutorService createExecutor(PluginContainer plugin) {
        checkNotNull(plugin, "plugin");
        return new SpongeTaskExecutorService(() -> Task.builder().plugin(plugin), this);
    }

    /**
     * Process all tasks in the map.
     */
    protected final void runTick() {
        this.preTick();
        TimingsManager.PLUGIN_SCHEDULER_HANDLER.startTimingIfSync();
        try {
            this.taskMap.values().forEach(this::processTask);
            this.postTick();
        } finally {
            this.finallyPostTick();
        }
        TimingsManager.PLUGIN_SCHEDULER_HANDLER.stopTimingIfSync();
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
    protected void processTask(SpongeScheduledTask task) {
        // If the task is now slated to be cancelled, we just remove it as if it
        // no longer exists.
        if (task.getState() == SpongeScheduledTask.ScheduledTaskState.CANCELED) {
            this.removeTask(task);
            return;
        }
        // If the task is already being processed, we wait for the previous
        // occurrence to terminate.
        if (task.getState() == SpongeScheduledTask.ScheduledTaskState.EXECUTING) {
            return;
        }
        long threshold = Long.MAX_VALUE;
        // Figure out if we start a delayed Task after threshold ticks or, start
        // it after the interval (interval) of the repeating task parameter.
        if (task.getState() == SpongeScheduledTask.ScheduledTaskState.WAITING) {
            threshold = task.task.delay;
        } else if (task.getState() == SpongeScheduledTask.ScheduledTaskState.RUNNING) {
            threshold = task.task.interval;
        }
        // This moment is 'now'
        long now = getTimestamp(task);
        // So, if the current time minus the timestamp of the task is greater
        // than the delay to wait before starting the task, then start the task.
        // Repeating tasks get a reset-timestamp each time they are set RUNNING
        // If the task has a interval of 0 (zero) this task will not repeat, and
        // is removed after we start it.
        if (threshold <= (now - task.getTimestamp())) {
            task.setState(SpongeScheduledTask.ScheduledTaskState.SWITCHING);
            task.setTimestamp(getTimestamp(task));
            startTask(task);
            // If task is one time shot, remove it from the map.
            if (task.task.interval == 0L) {
                removeTask(task);
            }
        }
    }

    /**
     * Begin the execution of a task. Exceptions are caught and logged.
     *
     * @param task The task to start
     */
    protected void startTask(final SpongeScheduledTask task) {
        this.executeTaskRunnable(task, () -> {
            task.setState(SpongeScheduledTask.ScheduledTaskState.EXECUTING);
            try (@Nullable final PhaseContext<?> context = this.createContext(task, task.getOwner());
                    final Timing timings = task.task.getTimingsHandler()) {
                timings.startTimingIfSync();
                if (context != null) {
                    context.buildAndSwitch();
                }
                try {
                    task.task.getConsumer().accept(task);
                } catch (Throwable t) {
                    SpongeImpl.getLogger().error("The Scheduler tried to run the task {} owned by {}, but an error occurred.",
                            task.getName(), task.getOwner(), t);
                }
            } finally {
                task.setState(SpongeScheduledTask.ScheduledTaskState.RUNNING);
                this.onTaskCompletion(task);
            }
        });
    }

    @Nullable
    protected PhaseContext<?> createContext(SpongeScheduledTask task, PluginContainer container) {
        return null;
    }

    /**
     * Run when a task has completed and is switching into
     * the {@link SpongeScheduledTask.ScheduledTaskState#RUNNING} state
     */
    protected void onTaskCompletion(SpongeScheduledTask task) {
        // no-op for sync methods.
    }

    /**
     * Actually run the runnable that will begin the task
     *
     * @param runnable The runnable to run
     */
    protected abstract void executeTaskRunnable(SpongeScheduledTask task, Runnable runnable);

    public <V> Future<V> execute(Callable<V> callable) {
        final FutureTask<V> runnable = new FutureTask<>(callable);
        submit(new SpongeTaskBuilder().execute(runnable).plugin(SpongeImpl.getPlugin()).build());
        return runnable;
    }

    public Future<?> execute(Runnable runnable) {
        return execute(() -> {
            runnable.run();
            return null;
        });
    }
}
