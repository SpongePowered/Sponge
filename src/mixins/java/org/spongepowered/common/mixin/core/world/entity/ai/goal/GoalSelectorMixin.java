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
package org.spongepowered.common.mixin.core.world.entity.ai.goal;

import com.google.common.base.MoreObjects;
import org.spongepowered.api.entity.ai.goal.GoalExecutor;
import org.spongepowered.api.entity.ai.goal.GoalExecutorType;
import org.spongepowered.api.entity.living.Agent;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.ai.goal.GoalEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.entity.EntityBridge;
import org.spongepowered.common.bridge.world.entity.ai.GoalBridge;
import org.spongepowered.common.bridge.world.entity.ai.goal.GoalSelectorBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.tracking.PhaseTracker;

import java.util.Set;

import javax.annotation.Nullable;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;

@Mixin(GoalSelector.class)
public abstract class GoalSelectorMixin implements GoalSelectorBridge {

    // @formatter:off
    @Shadow @Final private Set<WrappedGoal> availableGoals;
    // @formatter:on

    @Nullable private Mob owner;
    @Nullable private GoalExecutorType type;
    private boolean initialized;

    /**
     * @author gabizou - February 1st, 2016
     *
     * Purpose: Rewrites the overwrite to use a redirect when an entry is being added
     * to the task entries. We throw an event for plugins to potentially cancel.
     */
    @Redirect(method = "addGoal", at = @At(value = "INVOKE", target =  "Ljava/util/Set;add(Ljava/lang/Object;)Z", remap = false))
    private boolean onAddEntityTask(final Set<WrappedGoal> goals, final Object task, final int priority, final Goal base) {
        ((GoalBridge) base).bridge$setGoalExecutor((GoalExecutor<?>) this);
        if (!ShouldFire.GOAL_EVENT_ADD || this.owner == null || ((EntityBridge) this.owner).bridge$isConstructing()) {
            // Event is fired in bridge$fireConstructors
            return goals.add(new WrappedGoal(priority, base));
        }
        final GoalEvent.Add event = SpongeEventFactory.createGoalEventAdd(PhaseTracker.getCauseStackManager().currentCause(), priority, priority,
                (Agent) this.owner, (GoalExecutor<?>) this, (org.spongepowered.api.entity.ai.goal.Goal<?>) base);
        SpongeCommon.post(event);
        if (event.isCancelled()) {
            ((GoalBridge) base).bridge$setGoalExecutor(null);
            return false;
        }
        return goals.add(new WrappedGoal(event.priority(), base));
    }

    @Override
    public Mob bridge$getOwner() {
        return this.owner;
    }

    @Override
    public void bridge$setOwner(final Mob owner) {
        this.owner = owner;
    }

    @Override
    public GoalExecutorType bridge$getType() {
        return this.type;
    }

    @Override
    public void bridge$setType(final GoalExecutorType type) {
        this.type = type;
    }

    /**
     * @author Zidane - November 30th, 2015
     * @author Faithcaio - 2020-05-24 update to 1.14
     *
     * @reason Integrate Sponge events into the AI task removal.
     *
     * @param task the base
     */
    @SuppressWarnings({"rawtypes"})
    @Overwrite
    public void removeGoal(final Goal task) {
        this.availableGoals.removeIf(prioritizedGoal -> {
            if (prioritizedGoal.getGoal() == task) {
                if (ShouldFire.GOAL_EVENT_REMOVE && this.owner != null && !((EntityBridge) this.owner).bridge$isConstructing()) {
                    GoalEvent.Remove event = SpongeEventFactory.createGoalEventRemove(PhaseTracker.getCauseStackManager().currentCause(),
                            (Agent) this.owner, (GoalExecutor) this, (org.spongepowered.api.entity.ai.goal.Goal) task, prioritizedGoal.getPriority());
                    SpongeCommon.post(event);
                    if (event.isCancelled()) {
                        return false;
                    }
                }
                if (prioritizedGoal.isRunning()) {
                    prioritizedGoal.stop();
                }
                return true;
            }
            return false;
        });
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
                .addValue(this.bridge$getOwner())
                .addValue(this.bridge$getType())
                .toString();
    }
}
