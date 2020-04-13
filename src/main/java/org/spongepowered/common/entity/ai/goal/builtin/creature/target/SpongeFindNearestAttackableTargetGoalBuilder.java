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
package org.spongepowered.common.entity.ai.goal.builtin.creature.target;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Preconditions;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.api.entity.ai.goal.builtin.creature.target.FindNearestAttackableTargetGoal;
import org.spongepowered.api.entity.living.Creature;
import org.spongepowered.api.entity.living.Living;

import java.util.function.Predicate;

import javax.annotation.Nullable;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class SpongeFindNearestAttackableTargetGoalBuilder extends SpongeTargetGoalBuilder<FindNearestAttackableTargetGoal, FindNearestAttackableTargetGoal.Builder>
        implements FindNearestAttackableTargetGoal.Builder {

    private static final Predicate<LivingEntity> ALWAYS_TRUE = e -> true;

    private Class<? extends Living> targetClass;
    private int chance;
    @Nullable private Predicate<? extends LivingEntity> predicate;

    public SpongeFindNearestAttackableTargetGoalBuilder() {
        this.reset();
    }

    @Override
    public FindNearestAttackableTargetGoal.Builder target(Class<? extends Living> targetClass) {
        this.targetClass = targetClass;
        return this;
    }

    @Override
    public FindNearestAttackableTargetGoal.Builder chance(int chance) {
        this.chance = chance;
        return this;
    }

    @Override
    public FindNearestAttackableTargetGoal.Builder filter(Predicate<? extends Living> predicate) {
        this.predicate = (Predicate<? extends LivingEntity>) predicate;
        return this;
    }

    @Override
    public FindNearestAttackableTargetGoal.Builder from(FindNearestAttackableTargetGoal value) {
        checkNotNull(value);
        this.targetClass = value.getTargetClass();
        this.checkSight = value.shouldCheckSight();
        this.checkOnlyNearby = value.shouldCheckOnlyNearby();
        this.chance = value.getChance();
        this.predicate = (Predicate<? extends LivingEntity>) (Predicate<?>) value.getFilter();
        return this;
    }

    @Override
    public FindNearestAttackableTargetGoal.Builder reset() {
        this.checkSight = false;
        this.checkOnlyNearby = false;
        this.targetClass = null;
        this.predicate = null;
        return this;
    }

    @Override
    public FindNearestAttackableTargetGoal build(Creature owner) {
        Preconditions.checkNotNull(owner);
        Preconditions.checkNotNull(this.targetClass);

        return (FindNearestAttackableTargetGoal) new NearestAttackableTargetGoal((CreatureEntity) owner, this.targetClass, this.chance,
            this.checkSight, this.checkOnlyNearby, this.predicate == null ? ALWAYS_TRUE : this.predicate);
    }
}
