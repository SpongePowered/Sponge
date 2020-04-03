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

import org.spongepowered.api.entity.ai.goal.GoalExecutor;
import org.spongepowered.api.entity.ai.goal.Goal;
import org.spongepowered.api.entity.ai.goal.GoalType;
import org.spongepowered.api.entity.living.Agent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.entity.ai.GoalBridge;

import java.util.EnumSet;
import java.util.Optional;

@SuppressWarnings("unchecked")
@Mixin(net.minecraft.entity.ai.goal.Goal.class)
public abstract class GoalMixin_API<O extends Agent> implements Goal<O> {

    @Shadow public abstract EnumSet<net.minecraft.entity.ai.goal.Goal.Flag> shadow$getMutexFlags();

    @Override
    public GoalType getType() {
        return ((GoalBridge) this).bridge$getType();
    }

    @Override
    public Optional<GoalExecutor<O>> getExecutor() {
        return (Optional<GoalExecutor<O>>) (Optional<?>) ((GoalBridge) this).bridge$getGoalExecutor();
    }

    @Override
    public boolean canRunConcurrentWith(Goal<O> other) {
        return (this.mutexBits & ((net.minecraft.entity.ai.goal.Goal) other).getMutexBits()) == 0;
    }

    @Override
    public boolean canBeInterrupted() {
        return ((net.minecraft.entity.ai.goal.Goal) (Object) this).isPreemptible();
    }
}
