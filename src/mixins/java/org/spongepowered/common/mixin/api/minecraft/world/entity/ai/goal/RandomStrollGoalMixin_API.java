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

import org.spongepowered.api.entity.ai.goal.builtin.creature.RandomWalkingGoal;
import org.spongepowered.api.entity.living.Creature;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(net.minecraft.world.entity.ai.goal.RandomStrollGoal.class)
public abstract class RandomStrollGoalMixin_API extends GoalMixin_API<Creature> implements RandomWalkingGoal {

    // @formatter:off
    @Shadow @Final @Mutable protected double speedModifier;
    @Shadow protected int interval;
    // @formatter:on

    @Override
    public double speed() {
        return this.speedModifier;
    }

    @Override
    public RandomWalkingGoal setSpeed(double speed) {
        this.speedModifier = speed;
        return this;
    }

    @Override
    public int executionChance() {
        return this.interval;
    }

    @Override
    public RandomWalkingGoal setExecutionChance(int executionChance) {
        this.interval = executionChance;
        return this;
    }
}
