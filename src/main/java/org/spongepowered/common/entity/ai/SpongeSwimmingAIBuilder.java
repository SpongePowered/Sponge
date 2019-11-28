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
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.SwimGoal;
import org.spongepowered.api.entity.ai.task.builtin.SwimmingAITask;
import org.spongepowered.api.entity.living.Agent;

public final class SpongeSwimmingAIBuilder implements SwimmingAITask.Builder {

    float chance = 0.8f;

    public SpongeSwimmingAIBuilder() {
        this.reset();
    }

    @Override
    public SwimmingAITask.Builder swimChance(float chance) {
        this.chance = chance;
        return this;
    }

    @Override
    public SwimmingAITask.Builder from(SwimmingAITask value) {
        return swimChance(value.getSwimChance());
    }

    @Override
    public SwimmingAITask.Builder reset() {
        this.chance = 0.8f;
        return this;
    }

    @Override
    public SwimmingAITask build(Agent owner) {
        Preconditions.checkNotNull(owner);
        final SwimmingAITask task = (SwimmingAITask) new SwimGoal((MobEntity) owner);
        task.setSwimChance(this.chance);
        return task;
    }
}
