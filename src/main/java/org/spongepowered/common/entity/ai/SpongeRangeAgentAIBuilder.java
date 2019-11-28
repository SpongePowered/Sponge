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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.goal.RangedAttackGoal;
import org.spongepowered.api.entity.ai.task.builtin.creature.RangeAgentAITask;
import org.spongepowered.api.entity.living.Ranger;

public final class SpongeRangeAgentAIBuilder implements RangeAgentAITask.Builder {

    private double maxSpeed;
    private int delayBetweenAttacks;
    private float attackRadius;

    public SpongeRangeAgentAIBuilder() {
        this.reset();
    }

    @Override
    public RangeAgentAITask.Builder moveSpeed(double speed) {
        this.maxSpeed = speed;
        return this;
    }

    @Override
    public RangeAgentAITask.Builder delayBetweenAttacks(int delay) {
        this.delayBetweenAttacks = delay;
        return this;
    }

    @Override
    public RangeAgentAITask.Builder attackRadius(float radius) {
        this.attackRadius = radius;
        return this;
    }

    @Override
    public RangeAgentAITask.Builder from(RangeAgentAITask value) {
        checkNotNull(value);
        this.maxSpeed = value.getMoveSpeed();
        this.delayBetweenAttacks = value.getDelayBetweenAttacks();
        this.attackRadius = value.getAttackRadius();
        return this;
    }

    @Override
    public RangeAgentAITask.Builder reset() {
        // I'ma use Snowmen defaults. I like Snowmen.
        this.maxSpeed = 1.25D;
        this.delayBetweenAttacks = 20;
        this.attackRadius = 10.0f;
        return this;
    }

    @Override
    public RangeAgentAITask build(Ranger owner) {
        checkNotNull(owner);
        checkArgument(owner instanceof IRangedAttackMob, "Ranger must be an IRangedAttackMob!");
        return (RangeAgentAITask) new RangedAttackGoal((IRangedAttackMob) owner, this.maxSpeed, this.delayBetweenAttacks, this.attackRadius);
    }
}
