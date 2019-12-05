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
package org.spongepowered.common.entity.ai;

import com.google.common.base.Preconditions;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.ai.goal.builtin.creature.AvoidEntityGoal;
import org.spongepowered.api.entity.living.Creature;
import org.spongepowered.api.util.Functional;

import java.util.function.Predicate;
import net.minecraft.entity.CreatureEntity;

public final class SpongeAvoidEntityAIBuilder implements AvoidEntityGoal.Builder {

    Predicate<Entity> targetSelector;
    float searchDistance;
    double closeRangeSpeed, farRangeSpeed;

    public SpongeAvoidEntityAIBuilder() {
        this.reset();
    }

    @Override
    public AvoidEntityGoal.Builder targetSelector(Predicate<Entity> predicate) {
        this.targetSelector = predicate;
        return this;
    }

    @Override
    public AvoidEntityGoal.Builder searchDistance(float distance) {
        this.searchDistance = distance;
        return this;
    }

    @Override
    public AvoidEntityGoal.Builder closeRangeSpeed(double speed) {
        this.closeRangeSpeed = speed;
        return this;
    }

    @Override
    public AvoidEntityGoal.Builder farRangeSpeed(double speed) {
        this.farRangeSpeed = speed;
        return this;
    }

    @Override
    public AvoidEntityGoal.Builder from(AvoidEntityGoal value) {
        return this.targetSelector(value.getTargetSelector())
            .searchDistance(value.getSearchDistance())
            .closeRangeSpeed(value.getCloseRangeSpeed())
            .farRangeSpeed(value.getFarRangeSpeed());
    }

    @Override
    public AvoidEntityGoal.Builder reset() {
        this.searchDistance = 1;
        this.closeRangeSpeed = 1;
        this.farRangeSpeed = 1;
        return this;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public AvoidEntityGoal build(Creature owner) {
        Preconditions.checkNotNull(owner);
        Preconditions.checkNotNull(this.targetSelector);
        return (AvoidEntityGoal) new net.minecraft.entity.ai.goal.AvoidEntityGoal((CreatureEntity) owner, Entity.class,
                Functional.java8ToGuava((Predicate) this.targetSelector),
                this.searchDistance, this.closeRangeSpeed, this.farRangeSpeed);
    }
}
