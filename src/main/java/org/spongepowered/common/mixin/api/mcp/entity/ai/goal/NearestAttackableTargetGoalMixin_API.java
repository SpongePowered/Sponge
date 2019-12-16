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
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import org.spongepowered.api.entity.ai.goal.builtin.creature.target.FindNearestAttackableTargetGoal;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.mixin.accessor.entity.EntityPredicateAccessor;

import java.util.function.Predicate;

@SuppressWarnings({"rawtypes", "unchecked"})
@Mixin(NearestAttackableTargetGoal.class)
public abstract class NearestAttackableTargetGoalMixin_API extends TargetGoalMixin_API<FindNearestAttackableTargetGoal>
        implements FindNearestAttackableTargetGoal {

    private static Predicate<? extends LivingEntity> ALWAYS_TRUE = e -> true;

    @Shadow @Final @Mutable protected Class<? extends LivingEntity> targetClass;
    @Shadow protected EntityPredicate targetEntitySelector;
    @Shadow @Final @Mutable protected int targetChance;

    @Override
    public Class<? extends Living> getTargetClass() {
        return (Class<? extends Living>) this.targetClass;
    }

    @Override
    public FindNearestAttackableTargetGoal setTargetClass(Class<? extends Living> targetClass) {
        this.targetClass = (Class<? extends LivingEntity>) targetClass;
        return this;
    }

    @Override
    public int getChance() {
        return this.targetChance;
    }

    @Override
    public FindNearestAttackableTargetGoal setChance(int chance) {
        this.targetChance = chance;
        return this;
    }

    @Override
    public FindNearestAttackableTargetGoal filter(Predicate<Living> predicate) {
        this.targetEntitySelector.setCustomPredicate(((Predicate<LivingEntity>) (Object) predicate));
        return this;
    }

    @Override
    public Predicate<Living> getFilter() {
        final Predicate<LivingEntity> predicate = ((EntityPredicateAccessor) this.targetEntitySelector).accessor$getCustomPredicate();
        return (Predicate<Living>) (predicate == null ? ALWAYS_TRUE : predicate);
    }
}
