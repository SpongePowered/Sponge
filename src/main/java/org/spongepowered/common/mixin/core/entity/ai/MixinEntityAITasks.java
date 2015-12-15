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
package org.spongepowered.common.mixin.core.entity.ai;

import com.google.common.base.Objects;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.ai.Goal;
import org.spongepowered.api.entity.ai.GoalType;
import org.spongepowered.api.entity.ai.task.AITask;
import org.spongepowered.api.entity.ai.task.AITaskType;
import org.spongepowered.api.entity.living.Agent;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.ai.AITaskEvent;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.interfaces.ai.IMixinEntityAIBase;
import org.spongepowered.common.interfaces.ai.IMixinEntityAITasks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mixin(EntityAITasks.class)
@Implements(value = @Interface(iface = Goal.class, prefix = "goal$"))
public abstract class MixinEntityAITasks implements IMixinEntityAITasks {
    @Shadow private List taskEntries;
    @Shadow private List executingTaskEntries;
    private EntityLiving owner;
    private GoalType type;

    public Agent goal$getOwner() {
        return (Agent) this.owner;
    }

    public GoalType goal$getType() {
        return getType();
    }

    public Goal goal$addTask(int priority, AITask<?> task) {
        addTask(priority, (EntityAIBase) task);
        return (Goal) (Object) this;
    }

    public Goal goal$removeTask(AITask<?> task) {
        removeTask((EntityAIBase) task);
        return (Goal) (Object) this;
    }

    public Goal goal$removeTasks(AITaskType type) {
        Iterator iterator = this.taskEntries.iterator();

        while (iterator.hasNext()) {
            final EntityAITasks.EntityAITaskEntry entityaitaskentry = (EntityAITasks.EntityAITaskEntry)iterator.next();
            final EntityAIBase otherAiBase = entityaitaskentry.action;
            final AITask otherTask = (AITask) otherAiBase;

            if (otherTask.getType().equals(type)) {
                if (this.executingTaskEntries.contains(entityaitaskentry)) {
                    otherAiBase.resetTask();
                    this.executingTaskEntries.remove(entityaitaskentry);
                }

                iterator.remove();
            }
        }

        return (Goal) (Object) this;
    }

    public List<? extends AITask<?>> goal$getTasksByType(AITaskType type) {
        final List<AITask<?>> tasks = new ArrayList<>();

        for (EntityAITasks.EntityAITaskEntry entry : (List<EntityAITasks.EntityAITaskEntry>) this.taskEntries) {
            final AITask<?> task = (AITask<?>) entry.action;

            if (task.getType().equals(type)) {
                tasks.add(task);
            }
        }

        return tasks;
    }

    public List<? extends AITask<?>> goal$getTasks() {
        return (List<? extends AITask<?>>) this.taskEntries;
    }

    @Overwrite
    public void addTask(int priority, EntityAIBase base) {
        ((IMixinEntityAIBase) base).setGoal((Goal) (Object) this);
        final AITaskEvent.Add event = SpongeEventFactory.createAITaskEventAdd(Cause.of(Sponge.getGame()), priority, priority,
            (Goal) (Object) this, (Agent) this.owner, (AITask) base);
        SpongeImpl.postEvent(event);
        if (event.isCancelled()) {
            ((IMixinEntityAIBase) base).setGoal(null);
        } else {
            this.taskEntries.add(((EntityAITasks) (Object) this).new EntityAITaskEntry(event.getPriority(), base));
        }
    }

    @Override
    public EntityLiving getOwner() {
        return this.owner;
    }

    @Override
    public void setOwner(EntityLiving owner) {
        this.owner = owner;
    }

    @Override
    public GoalType getType() {
        return this.type;
    }

    @Override
    public void setType(GoalType type) {
        this.type = type;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Overwrite
    public void removeTask(EntityAIBase aiBase) {
        final Iterator iterator = this.taskEntries.iterator();

        while (iterator.hasNext()) {
            final EntityAITasks.EntityAITaskEntry entityaitaskentry = (EntityAITasks.EntityAITaskEntry)iterator.next();
            final EntityAIBase otherAiBase = entityaitaskentry.action;

            if (otherAiBase.equals(aiBase)) {
                final AITaskEvent.Remove event = SpongeEventFactory.createAITaskEventRemove(Cause.of(Sponge.getGame()),
                    (Goal) (Object) this, (Agent) this.owner, (AITask) otherAiBase, entityaitaskentry.priority);
                SpongeImpl.postEvent(event);
                if (!event.isCancelled()) {
                    if (this.executingTaskEntries.contains(entityaitaskentry)) {
                        otherAiBase.resetTask();
                        this.executingTaskEntries.remove(entityaitaskentry);
                    }

                    iterator.remove();
                }
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Overwrite
    private boolean areTasksCompatible(EntityAITasks.EntityAITaskEntry taskEntry1, EntityAITasks.EntityAITaskEntry taskEntry2) {
        return (((AITask) taskEntry2.action).canRunConcurrentWith((AITask) taskEntry1.action));
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .addValue(getOwner())
                .addValue(getType())
                .toString();
    }
}
