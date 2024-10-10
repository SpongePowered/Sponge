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

import net.minecraft.world.entity.PathfinderMob;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.ai.goal.builtin.creature.AvoidLivingGoal;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.PathfinderAgent;

import java.util.Objects;
import java.util.function.Predicate;

public final class SpongeAvoidLivingGoalBuilder implements AvoidLivingGoal.Builder {

    private Predicate<Living> targetSelector;
    private float searchDistance;
    private double closeRangeSpeed, farRangeSpeed;

    public SpongeAvoidLivingGoalBuilder() {
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
        return this.targetSelector(value.targetSelector())
            .searchDistance(value.searchDistance())
            .closeRangeSpeed(value.closeRangeSpeed())
            .farRangeSpeed(value.farRangeSpeed());
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
    public AvoidLivingGoal build(PathfinderAgent owner) {
        Objects.requireNonNull(owner);
        Objects.requireNonNull(this.targetSelector);
        return (AvoidLivingGoal) new net.minecraft.world.entity.ai.goal.AvoidEntityGoal((PathfinderMob) owner, Entity.class, this.searchDistance,
            this.closeRangeSpeed, this.farRangeSpeed, this.targetSelector);
    }
}
