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

import org.spongepowered.api.entity.ai.goal.builtin.creature.AvoidLivingGoal;
import org.spongepowered.api.entity.living.Creature;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.accessor.world.entity.ai.targeting.TargetingConditionsAccessor;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

@SuppressWarnings({"unchecked"})
@Mixin(net.minecraft.world.entity.ai.goal.AvoidEntityGoal.class)
public abstract class AvoidEntityGoalMixin_API extends GoalMixin_API<Creature> implements AvoidLivingGoal {

    private static final Predicate<LivingEntity> ALWAYS_TRUE = e -> true;

    // @formatter:off
    @Shadow @Final @Mutable private double walkSpeedModifier;
    @Shadow @Final @Mutable private double sprintSpeedModifier;
    @Shadow @Final @Mutable protected float maxDist;
    @Shadow @Final private TargetingConditions avoidEntityTargeting;
    // @formatter:on

    @Override
    public Predicate<Living> targetSelector() {
        final Predicate<LivingEntity> predicate = ((TargetingConditionsAccessor) this.avoidEntityTargeting).accessor$selector();
        return (Predicate<Living>) (Object) (predicate == null ? AvoidEntityGoalMixin_API.ALWAYS_TRUE : predicate);
    }

    @Override
    public AvoidLivingGoal setTargetSelector(Predicate<Living> predicate) {
        this.avoidEntityTargeting.selector((Predicate<LivingEntity>) (Object) predicate);
        return this;
    }

    @Override
    public float searchDistance() {
        return this.maxDist;
    }

    @Override
    public AvoidLivingGoal setSearchDistance(float distance) {
        this.maxDist = distance;
        return this;
    }

    @Override
    public double closeRangeSpeed() {
        return this.sprintSpeedModifier;
    }

    @Override
    public AvoidLivingGoal setCloseRangeSpeed(double speed) {
       this.sprintSpeedModifier = speed;
        return this;
    }

    @Override
    public double farRangeSpeed() {
        return this.walkSpeedModifier;
    }

    @Override
    public AvoidLivingGoal setFarRangeSpeed(double speed) {
        this.walkSpeedModifier = speed;
        return this;
    }
}
