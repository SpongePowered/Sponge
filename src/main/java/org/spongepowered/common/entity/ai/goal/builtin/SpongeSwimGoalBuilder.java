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
package org.spongepowered.common.entity.ai.goal.builtin;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.entity.MobEntity;
import org.spongepowered.api.entity.ai.goal.builtin.SwimGoal;
import org.spongepowered.api.entity.living.Agent;

public final class SpongeSwimGoalBuilder implements SwimGoal.Builder {

    private float chance;

    public SpongeSwimGoalBuilder() {
        this.reset();
    }

    @Override
    public SwimGoal.Builder swimChance(float chance) {
        this.chance = chance;
        return this;
    }

    @Override
    public SwimGoal.Builder from(SwimGoal value) {
        checkNotNull(value);
        return this.swimChance(value.getSwimChance());
    }

    @Override
    public SwimGoal.Builder reset() {
        this.chance = 0.8f;
        return this;
    }

    @Override
    public SwimGoal build(Agent owner) {
        checkNotNull(owner);
        final SwimGoal task = (SwimGoal) new net.minecraft.entity.ai.goal.SwimGoal((MobEntity) owner);
        task.setSwimChance(this.chance);
        return task;
    }
}
