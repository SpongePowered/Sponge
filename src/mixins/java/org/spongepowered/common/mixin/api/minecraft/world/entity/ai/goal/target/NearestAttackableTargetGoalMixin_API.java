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
package org.spongepowered.common.mixin.api.minecraft.world.entity.ai.goal.target;

import org.spongepowered.api.entity.ai.goal.builtin.creature.target.FindNearestAttackableTargetGoal;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.accessor.world.entity.ai.targeting.TargetingConditionsAccessor;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

@SuppressWarnings("unchecked")
@Mixin(NearestAttackableTargetGoal.class)
public abstract class NearestAttackableTargetGoalMixin_API extends TargetGoalMixin_API<FindNearestAttackableTargetGoal>
        implements FindNearestAttackableTargetGoal {

    private static Predicate<? extends LivingEntity> ALWAYS_TRUE = e -> true;

    // @formatter:off
    @Shadow @Final @Mutable protected Class<? extends LivingEntity> targetType;
    @Shadow protected TargetingConditions targetConditions;
    @Shadow @Final @Mutable protected int randomInterval;
    // @formatter:on

    @Override
    public Class<? extends Living> targetClass() {
        return (Class<? extends Living>) this.targetType;
    }

    @Override
    public FindNearestAttackableTargetGoal setTargetClass(Class<? extends Living> targetClass) {
        this.targetType = (Class<? extends LivingEntity>) targetClass;
        return this;
    }

    @Override
    public int chance() {
        return this.randomInterval;
    }

    @Override
    public FindNearestAttackableTargetGoal setChance(int chance) {
        this.randomInterval = chance;
        return this;
    }

    @Override
    public FindNearestAttackableTargetGoal filter(Predicate<Living> predicate) {
        this.targetConditions.selector(((Predicate<LivingEntity>) (Object) predicate));
        return this;
    }

    @Override
    public Predicate<Living> filter() {
        final Predicate<LivingEntity> predicate = ((TargetingConditionsAccessor) this.targetConditions).accessor$selector();
        return (Predicate<Living>) (Object) (predicate == null ? NearestAttackableTargetGoalMixin_API.ALWAYS_TRUE : predicate);
    }
}
