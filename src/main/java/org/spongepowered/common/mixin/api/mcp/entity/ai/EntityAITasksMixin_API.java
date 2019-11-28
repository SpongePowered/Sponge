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
package org.spongepowered.common.mixin.api.mcp.entity.ai;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.entity.ai.Goal;
import org.spongepowered.api.entity.ai.GoalType;
import org.spongepowered.api.entity.ai.task.AITask;
import org.spongepowered.api.entity.ai.task.AITaskType;
import org.spongepowered.api.entity.living.Agent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.entity.ai.EntityAITasksBridge;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.entity.ai.goal.GoalSelector;

@Mixin(GoalSelector.class)
public abstract class EntityAITasksMixin_API<O extends Agent> implements Goal<O> {

    @Shadow @Final private Set<GoalSelector.EntityAITaskEntry> taskEntries;
    @Shadow @Final private Set<GoalSelector.EntityAITaskEntry> executingTaskEntries;

    @Shadow public abstract void shadow$addTask(int priority, net.minecraft.entity.ai.goal.Goal task);
    @Shadow public abstract void shadow$removeTask(net.minecraft.entity.ai.goal.Goal task);

    @SuppressWarnings("unchecked")
    @Override
    public O getOwner() {
        return (O) ((EntityAITasksBridge) this).bridge$getOwner();
    }

    @Override
    public GoalType getType() {
        return ((EntityAITasksBridge) this).bridge$getType();
    }

    @Override
    public Goal<O> addTask(final int priority, final AITask<? extends O> task) {
        shadow$addTask(priority, (net.minecraft.entity.ai.goal.Goal) task);
        return this;
    }

    @Override
    public Goal<O> removeTask(final AITask<? extends O> task) {
        shadow$removeTask((net.minecraft.entity.ai.goal.Goal) task);
        return  this;
    }

    @Override
    public Goal<O> removeTasks(final AITaskType type) {
        final Iterator<GoalSelector.EntityAITaskEntry> iterator = this.taskEntries.iterator();

        while (iterator.hasNext()) {
            final GoalSelector.EntityAITaskEntry entityaitaskentry = iterator.next();
            final net.minecraft.entity.ai.goal.Goal otherAiBase = entityaitaskentry.field_75733_a;
            final AITask<?> otherTask = (AITask<?>) otherAiBase;

            if (otherTask.getType().equals(type)) {
                if (this.executingTaskEntries.contains(entityaitaskentry)) {
                    otherAiBase.resetTask();
                    this.executingTaskEntries.remove(entityaitaskentry);
                }

                iterator.remove();
            }
        }

        return this;
    }

    @Override
    public List<? super AITask<? extends O>> getTasksByType(final AITaskType type) {
        final ImmutableList.Builder<AITask<?>> tasks = ImmutableList.builder();

        for (final GoalSelector.EntityAITaskEntry entry : this.taskEntries) {
            final AITask<?> task = (AITask<?>) entry.field_75733_a;

            if (task.getType().equals(type)) {
                tasks.add(task);
            }
        }

        return tasks.build();
    }

    @Override
    public List<? super AITask<? extends O>> getTasks() {
        final ImmutableList.Builder<AITask<?>> tasks = ImmutableList.builder();
        for (final Object o : this.taskEntries) {
            final GoalSelector.EntityAITaskEntry entry = (GoalSelector.EntityAITaskEntry) o;

            tasks.add((AITask<?>) entry.field_75733_a);
        }
        return tasks.build();
    }

    @Override
    public void clear() {
        this.taskEntries.clear();
    }
}
