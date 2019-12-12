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
package org.spongepowered.common.mixin.api.mcp.entity;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.entity.AgentData;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.ai.GoalExecutor;
import org.spongepowered.api.entity.ai.GoalExecutorType;
import org.spongepowered.api.entity.ai.GoalExecutorTypes;
import org.spongepowered.api.entity.living.Agent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeAgentData;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Collection;
import java.util.Optional;

import javax.annotation.Nullable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.GoalSelector;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin_API extends LivingEntityMixin_API implements Agent {

    @Shadow @Final protected GoalSelector tasks;
    @Shadow @Final protected GoalSelector targetTasks;
    @Shadow @Nullable private LivingEntity attackTarget;

    @Shadow public abstract boolean isAIDisabled();

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Agent> Optional<GoalExecutor<T>> getGoal(GoalExecutorType type) {
        if (GoalExecutorTypes.NORMAL.equals(type)) {
            return Optional.of((GoalExecutor<T>) this.tasks);
        } else if (GoalExecutorTypes.TARGET.equals(type)) {
            return Optional.of((GoalExecutor<T>) this.targetTasks);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Entity> getTarget() {
        return Optional.ofNullable((Entity) this.attackTarget);
    }

    @Override
    public void setTarget(@Nullable Entity target) {
        if (target instanceof LivingEntity) {
            this.attackTarget = (LivingEntity) target;
        } else {
            this.attackTarget = null;
        }
    }



    @Override
    public AgentData getAgentData() {
        return new SpongeAgentData(!this.isAIDisabled());
    }

    @Override
    public Mutable<Boolean> aiEnabled() {
        return new SpongeValue<>(Keys.AI_ENABLED, true, !this.isAIDisabled());
    }

    @Override
    public void spongeApi$supplyVanillaManipulators(Collection<? super org.spongepowered.api.data.DataManipulator.Mutable<?, ?>> manipulators) {
        super.spongeApi$supplyVanillaManipulators(manipulators);
        manipulators.add(this.getAgentData());
    }

}
