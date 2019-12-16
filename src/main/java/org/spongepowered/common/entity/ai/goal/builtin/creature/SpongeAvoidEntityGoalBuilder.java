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
package org.spongepowered.common.entity.ai.goal.builtin.creature;

import com.google.common.base.Preconditions;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.ai.goal.builtin.creature.AvoidLivingGoal;
import org.spongepowered.api.entity.living.Creature;

import java.util.function.Predicate;
import net.minecraft.entity.CreatureEntity;
import org.spongepowered.api.entity.living.Living;

public final class SpongeAvoidEntityGoalBuilder implements AvoidLivingGoal.Builder {

    private Predicate<Living> targetSelector;
    private float searchDistance;
    private double closeRangeSpeed, farRangeSpeed;

    public SpongeAvoidEntityGoalBuilder() {
        this.reset();
    }

    @Override
    public AvoidLivingGoal.Builder targetSelector(Predicate<Living> predicate) {
        this.targetSelector = predicate;
        return this;
    }

    @Override
    public AvoidLivingGoal.Builder searchDistance(float distance) {
        this.searchDistance = distance;
        return this;
    }

    @Override
    public AvoidLivingGoal.Builder closeRangeSpeed(double speed) {
        this.closeRangeSpeed = speed;
        return this;
    }

    @Override
    public AvoidLivingGoal.Builder farRangeSpeed(double speed) {
        this.farRangeSpeed = speed;
        return this;
    }

    @Override
    public AvoidLivingGoal.Builder from(AvoidLivingGoal value) {
        return this.targetSelector(value.getTargetSelector())
            .searchDistance(value.getSearchDistance())
            .closeRangeSpeed(value.getCloseRangeSpeed())
            .farRangeSpeed(value.getFarRangeSpeed());
    }

    @Override
    public AvoidLivingGoal.Builder reset() {
        this.targetSelector = null;
        this.searchDistance = 1;
        this.closeRangeSpeed = 1;
        this.farRangeSpeed = 1;
        return this;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public AvoidLivingGoal build(Creature owner) {
        Preconditions.checkNotNull(owner);
        Preconditions.checkNotNull(this.targetSelector);
        return (AvoidLivingGoal) new net.minecraft.entity.ai.goal.AvoidEntityGoal((CreatureEntity) owner, Entity.class, this.searchDistance,
            this.closeRangeSpeed, this.farRangeSpeed, this.targetSelector);
    }
}
