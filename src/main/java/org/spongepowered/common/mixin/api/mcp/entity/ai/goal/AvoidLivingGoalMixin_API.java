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
package org.spongepowered.common.mixin.api.mcp.entity.ai.goal;

import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.api.entity.ai.goal.builtin.creature.AvoidLivingGoal;
import org.spongepowered.api.entity.living.Creature;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.mixin.accessor.entity.EntityPredicateAccessor;

import java.util.function.Predicate;

@SuppressWarnings({"unchecked", "rawtypes"})
@Mixin(net.minecraft.entity.ai.goal.AvoidEntityGoal.class)
public abstract class AvoidLivingGoalMixin_API extends GoalMixin_API<Creature> implements AvoidLivingGoal {

    private static final Predicate<LivingEntity> ALWAYS_TRUE = e -> true;

    @Shadow @Final @Mutable private double farSpeed;
    @Shadow @Final @Mutable private double nearSpeed;
    @Shadow @Final @Mutable protected float avoidDistance;
    @Shadow @Final private EntityPredicate builtTargetSelector;

    @Override
    public Predicate<Living> getTargetSelector() {
        final Predicate<LivingEntity> predicate = ((EntityPredicateAccessor) this.builtTargetSelector).accessor$getCustomPredicate();
        return (Predicate<Living>) (Object) (predicate == null ? ALWAYS_TRUE : predicate);
    }

    @Override
    public AvoidLivingGoal setTargetSelector(Predicate<Living> predicate) {
        this.builtTargetSelector.setCustomPredicate((Predicate<LivingEntity>) (Object) predicate);
        return this;
    }

    @Override
    public float getSearchDistance() {
        return this.avoidDistance;
    }

    @Override
    public AvoidLivingGoal setSearchDistance(float distance) {
        this.avoidDistance = distance;
        return this;
    }

    @Override
    public double getCloseRangeSpeed() {
        return this.nearSpeed;
    }

    @Override
    public AvoidLivingGoal setCloseRangeSpeed(double speed) {
       this.nearSpeed = speed;
        return this;
    }

    @Override
    public double getFarRangeSpeed() {
        return this.farSpeed;
    }

    @Override
    public AvoidLivingGoal setFarRangeSpeed(double speed) {
        this.farSpeed = speed;
        return this;
    }
}
