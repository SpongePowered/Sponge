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
package org.spongepowered.common.entity.ai.target;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import org.spongepowered.api.entity.ai.goal.builtin.creature.target.FindNearestAttackableTargetGoal;
import org.spongepowered.api.entity.living.Creature;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.util.Functional;

import java.util.function.Predicate;

import javax.annotation.Nullable;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;

public final class SpongeFindNearestAttackableTargetAIBuilder extends SpongeTargetAIBuilder<FindNearestAttackableTargetGoal, FindNearestAttackableTargetGoal.Builder>
        implements FindNearestAttackableTargetGoal.Builder {

    private Class<? extends Living> targetClass;
    private int chance;
    @Nullable private Predicate<? extends Living> predicate;

    public SpongeFindNearestAttackableTargetAIBuilder() {
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
        this.predicate = predicate;
        return this;
    }

    @Override
    public FindNearestAttackableTargetGoal.Builder from(FindNearestAttackableTargetGoal value) {
        return this.target(value.getTargetClass())
            .chance(value.getChance())
            .filter(value.getFilter());
    }

    @Override
    public FindNearestAttackableTargetGoal.Builder reset() {
        this.checkSight = false;
        this.onlyNearby = false;
        this.searchDelay = 0;
        this.interruptTargetUnseenTicks = 0;
        this.targetClass = null;
        this.predicate = null;
        return this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public FindNearestAttackableTargetGoal build(Creature owner) {
        Preconditions.checkNotNull(owner);
        Preconditions.checkNotNull(this.targetClass);
        return (FindNearestAttackableTargetGoal) new NearestAttackableTargetGoal<>((CreatureEntity) owner, (Class<? extends LivingEntity>) this.targetClass, this.chance, this.checkSight,
            this.onlyNearby, this.predicate == null ? Predicates.alwaysTrue() : Functional.java8ToGuava((Predicate) this.predicate));
    }
}
