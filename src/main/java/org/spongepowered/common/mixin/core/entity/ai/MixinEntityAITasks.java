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

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
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
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.ai.AITaskEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.interfaces.ai.IMixinEntityAIBase;
import org.spongepowered.common.interfaces.ai.IMixinEntityAITasks;
import org.spongepowered.common.interfaces.entity.IMixinEntity;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Mixin(EntityAITasks.class)
@Implements(value = @Interface(iface = Goal.class, prefix = "goal$"))
public abstract class MixinEntityAITasks implements IMixinEntityAITasks {

    @Shadow @Final private Set<EntityAITasks.EntityAITaskEntry> taskEntries;
    @Shadow @Final private Set<EntityAITasks.EntityAITaskEntry> executingTaskEntries;

    @Shadow public abstract void addTask(int priority, EntityAIBase task);

    private EntityLiving owner;
    private GoalType type;
    private boolean initialized;

    public Agent goal$getOwner() {
        return (Agent) this.owner;
    }

    public GoalType goal$getType() {
        return getType();
    }

    public Goal<?> goal$addTask(int priority, AITask<?> task) {
        addTask(priority, (EntityAIBase) task);
        return (Goal<?>) this;
    }

    public Goal<?> goal$removeTask(AITask<?> task) {
        removeTask((EntityAIBase) task);
        return (Goal<?>) this;
    }

    public Goal<?> goal$removeTasks(AITaskType type) {
        Iterator<EntityAITasks.EntityAITaskEntry> iterator = this.taskEntries.iterator();

        while (iterator.hasNext()) {
            final EntityAITasks.EntityAITaskEntry entityaitaskentry = iterator.next();
            final EntityAIBase otherAiBase = entityaitaskentry.action;
            final AITask<?> otherTask = (AITask<?>) otherAiBase;

            if (otherTask.getType().equals(type)) {
                if (this.executingTaskEntries.contains(entityaitaskentry)) {
                    otherAiBase.resetTask();
                    this.executingTaskEntries.remove(entityaitaskentry);
                }

                iterator.remove();
            }
        }

        return (Goal<?>) this;
    }

    public List<? extends AITask<?>> goal$getTasksByType(AITaskType type) {
        final ImmutableList.Builder<AITask<?>> tasks = ImmutableList.builder();

        for (EntityAITasks.EntityAITaskEntry entry : this.taskEntries) {
            final AITask<?> task = (AITask<?>) entry.action;

            if (task.getType().equals(type)) {
                tasks.add(task);
            }
        }

        return tasks.build();
    }

    public List<? extends AITask<?>> goal$getTasks() {
        final ImmutableList.Builder<AITask<?>> tasks = ImmutableList.builder();
        for (Object o : this.taskEntries) {
            final EntityAITasks.EntityAITaskEntry entry = (EntityAITasks.EntityAITaskEntry) o;

            tasks.add((AITask<?>) entry.action);
        }
        return tasks.build();
    }

    @Override
    public Set<EntityAITasks.EntityAITaskEntry> getTasksUnsafe() {
        return this.taskEntries;
    }

    /**
     * @author gabizou - February 1st, 2016
     * Purpose: Rewrites the overwrite to use a redirect when an entry is being added
     * to the task entries. We throw an event for plugins to potentially cancel.
     *
     * @param entry
     * @param priority
     * @param base
     * @return
     */
    @Redirect(method = "addTask", at = @At(value = "INVOKE", target =  "Ljava/util/Set;add(Ljava/lang/Object;)Z", remap = false))
    private boolean onAddEntityTask(Set<EntityAITasks.EntityAITaskEntry> set, Object entry, int priority, EntityAIBase base) {
        ((IMixinEntityAIBase) base).setGoal((Goal<?>) this);
        if (this.owner == null || ((IMixinEntity) this.owner).isInConstructPhase()) {
            // Event is fired in firePostConstructEvents
            return set.add(((EntityAITasks) (Object) this).new EntityAITaskEntry(priority, base));
        }
        final AITaskEvent.Add event = SpongeEventFactory.createAITaskEventAdd(Cause.source(Sponge.getGame()).build(), priority, priority,
                (Goal<?>) this, (Agent) this.owner, (AITask<?>) base);
        SpongeImpl.postEvent(event);
        if (event.isCancelled()) {
            ((IMixinEntityAIBase) base).setGoal(null);
            return false;
        } else {
            return set.add(((EntityAITasks) (Object) this).new EntityAITaskEntry(event.getPriority(), base));
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

    public void goal$clear() {
        this.taskEntries.clear();
    }

    /**
     * @author Zidane - November 30th, 2015
     * @reason Integrate Sponge events into the AI task removal.
     *
     * @param aiBase
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Overwrite
    public void removeTask(EntityAIBase aiBase) {
        final Iterator iterator = this.taskEntries.iterator();

        while (iterator.hasNext()) {
            final EntityAITasks.EntityAITaskEntry entityaitaskentry = (EntityAITasks.EntityAITaskEntry)iterator.next();
            final EntityAIBase otherAiBase = entityaitaskentry.action;

            // Sponge start
            if (otherAiBase.equals(aiBase)) {
                AITaskEvent.Remove event = null;
                if (this.owner != null && !((IMixinEntity) this.owner).isInConstructPhase()) {
                    event = SpongeEventFactory.createAITaskEventRemove(Cause.of(NamedCause.source(Sponge.getGame())),
                            (Goal) this, (Agent) this.owner, (AITask) otherAiBase, entityaitaskentry.priority);
                    SpongeImpl.postEvent(event);
                }
                if (event == null || !event.isCancelled()) {

                    if (entityaitaskentry.using) {
                        entityaitaskentry.using = false;
                        otherAiBase.resetTask();
                        this.executingTaskEntries.remove(entityaitaskentry);
                    }

                    iterator.remove();
                    return;
                }
            }
            // Sponge end
        }
    }

    /**
     * @author Zidane - February 22, 2016
     * @reason Use SpongeAPI's method check instead of exposing mutex bits
     *
     * @param taskEntry1 The task entry to check compatibility
     * @param taskEntry2 The second entry to check compatibility
     * @return Whehter the two tasks are compatible or "can run at the same time"
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Overwrite
    private boolean areTasksCompatible(EntityAITasks.EntityAITaskEntry taskEntry1, EntityAITasks.EntityAITaskEntry taskEntry2) {
        return (((AITask) taskEntry2.action).canRunConcurrentWith((AITask) taskEntry1.action));
    }

    @Override
    public boolean initialized() {
        return this.initialized;
    }

    @Override
    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .addValue(getOwner())
                .addValue(getType())
                .toString();
    }
}
