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
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.ai.Goal;
import org.spongepowered.api.entity.ai.GoalType;
import org.spongepowered.api.entity.ai.task.AITask;
import org.spongepowered.api.entity.living.Agent;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.ai.AITaskEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.entity.EntityBridge;
import org.spongepowered.common.bridge.entity.ai.EntityAIBasesBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.bridge.entity.ai.EntityAITasksBridge;

import java.util.Iterator;
import java.util.Set;

import javax.annotation.Nullable;

@Mixin(EntityAITasks.class)
public abstract class EntityAITasksMixin implements EntityAITasksBridge {

    @Shadow @Final private Set<EntityAITasks.EntityAITaskEntry> taskEntries;
    @Shadow @Final private Set<EntityAITasks.EntityAITaskEntry> executingTaskEntries;

    @Nullable private EntityLiving owner;
    @Nullable private GoalType type;
    private boolean initialized;

    @Override
    public Set<EntityAITasks.EntityAITaskEntry> bridge$getTasksUnsafe() {
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
    private boolean onAddEntityTask(final Set<EntityAITasks.EntityAITaskEntry> set, final Object entry, final int priority, final EntityAIBase base) {
        ((EntityAIBasesBridge) base).bridge$setGoal((Goal<?>) this);
        if (!ShouldFire.A_I_TASK_EVENT_ADD || this.owner == null || ((EntityBridge) this.owner).bridge$isConstructing()) {
            // Event is fired in bridge$fireConstructors
            return set.add(((EntityAITasks) (Object) this).new EntityAITaskEntry(priority, base));
        }
        final AITaskEvent.Add event = SpongeEventFactory.createAITaskEventAdd(Sponge.getCauseStackManager().getCurrentCause(), priority, priority,
                (Goal<?>) this, (Agent) this.owner, (AITask<?>) base);
        SpongeImpl.postEvent(event);
        if (event.isCancelled()) {
            ((EntityAIBasesBridge) base).bridge$setGoal(null);
            return false;
        }
        return set.add(((EntityAITasks) (Object) this).new EntityAITaskEntry(event.getPriority(), base));
    }

    @Override
    public EntityLiving bridge$getOwner() {
        return this.owner;
    }

    @Override
    public void bridge$setOwner(final EntityLiving owner) {
        this.owner = owner;
    }

    @Override
    public GoalType bridge$getType() {
        return this.type;
    }

    @Override
    public void bridge$setType(final GoalType type) {
        this.type = type;
    }


    /**
     * @author Zidane - November 30th, 2015
     * @reason Integrate Sponge events into the AI task removal.
     *
     * @param aiBase the base
     */
    @SuppressWarnings({"rawtypes"})
    @Overwrite
    public void removeTask(final EntityAIBase aiBase) {
        final Iterator iterator = this.taskEntries.iterator();

        while (iterator.hasNext()) {
            final EntityAITasks.EntityAITaskEntry entityaitaskentry = (EntityAITasks.EntityAITaskEntry)iterator.next();
            final EntityAIBase otherAiBase = entityaitaskentry.field_75733_a;

            // Sponge start
            if (otherAiBase.equals(aiBase)) {
                AITaskEvent.Remove event = null;
                if (ShouldFire.A_I_TASK_EVENT_REMOVE && this.owner != null && !((EntityBridge) this.owner).bridge$isConstructing()) {
                    event = SpongeEventFactory.createAITaskEventRemove(Sponge.getCauseStackManager().getCurrentCause(),
                            (Goal) this, (Agent) this.owner, (AITask) otherAiBase, entityaitaskentry.field_75731_b);
                    SpongeImpl.postEvent(event);
                }
                if (event == null || !event.isCancelled()) {

                    if (entityaitaskentry.field_188524_c) {
                        entityaitaskentry.field_188524_c = false;
                        otherAiBase.func_75251_c();
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
    private boolean areTasksCompatible(final EntityAITasks.EntityAITaskEntry taskEntry1, final EntityAITasks.EntityAITaskEntry taskEntry2) {
        return (((AITask) taskEntry2.field_75733_a).canRunConcurrentWith((AITask) taskEntry1.field_75733_a));
    }

    @Override
    public boolean bridge$initialized() {
        return this.initialized;
    }

    @Override
    public void bridge$setInitialized(final boolean initialized) {
        this.initialized = initialized;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .addValue(bridge$getOwner())
                .addValue(bridge$getType())
                .toString();
    }
}
