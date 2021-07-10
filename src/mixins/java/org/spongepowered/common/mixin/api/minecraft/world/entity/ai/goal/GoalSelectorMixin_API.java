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
package org.spongepowered.common.mixin.api.minecraft.world.entity.ai.goal;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.entity.ai.goal.Goal;
import org.spongepowered.api.entity.ai.goal.GoalExecutor;
import org.spongepowered.api.entity.ai.goal.GoalExecutorType;
import org.spongepowered.api.entity.ai.goal.GoalType;
import org.spongepowered.api.entity.living.Agent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.entity.ai.GoalBridge;
import org.spongepowered.common.bridge.world.entity.ai.goal.GoalSelectorBridge;

import java.util.List;
import java.util.Set;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;

@Mixin(GoalSelector.class)
public abstract class GoalSelectorMixin_API<O extends Agent> implements GoalExecutor<O> {

    // @formatter:off
    @Shadow @Final private Set<WrappedGoal> availableGoals;

    @Shadow public abstract void shadow$addGoal(int priority, net.minecraft.world.entity.ai.goal.Goal task);
    @Shadow public abstract void shadow$removeGoal(net.minecraft.world.entity.ai.goal.Goal task);
    // @formatter:on

    @SuppressWarnings("unchecked")
    @Override
    public O owner() {
        return (O) ((GoalSelectorBridge) this).bridge$getOwner();
    }

    @Override
    public GoalExecutorType type() {
        return ((GoalSelectorBridge) this).bridge$getType();
    }

    @Override
    public GoalExecutor<O> addGoal(final int priority, final Goal<? extends O> task) {
        this.shadow$addGoal(priority, (net.minecraft.world.entity.ai.goal.Goal) task);
        return this;
    }

    @Override
    public GoalExecutor<O> removeGoal(final Goal<? extends O> goal) {
        this.shadow$removeGoal((net.minecraft.world.entity.ai.goal.Goal) goal);
        return  this;
    }

    @Override
    public GoalExecutor<O> removeGoals(final GoalType type) {
        this.availableGoals.removeIf(goal -> ((GoalBridge)goal.getGoal()).bridge$getType() == type);
        return this;
    }

    @Override
    public List<? super Goal<? extends O>> tasksByType(final GoalType type) {
        final ImmutableList.Builder<Goal<?>> tasks = ImmutableList.builder();
        this.availableGoals.stream().map(WrappedGoal::getGoal).map(Goal.class::cast)
                .filter(goal -> goal.type() == type)
                .forEach(tasks::add);
        return tasks.build();
    }

    @Override
    public List<? super Goal<? extends O>> tasks() {
        final ImmutableList.Builder<Goal<?>> tasks = ImmutableList.builder();
        this.availableGoals.stream().map(WrappedGoal::getGoal).map(Goal.class::cast).forEach(tasks::add);
        return tasks.build();
    }

    @Override
    public void clear() {
        this.availableGoals.clear();
    }
}
