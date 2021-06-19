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

import org.spongepowered.api.entity.ai.goal.GoalExecutor;
import org.spongepowered.api.entity.ai.goal.GoalType;
import org.spongepowered.api.entity.living.Agent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.entity.ai.GoalBridge;

import java.util.EnumSet;
import java.util.Optional;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;

@SuppressWarnings("unchecked")
@Mixin(Goal.class)
public abstract class GoalMixin_API<O extends Agent> implements org.spongepowered.api.entity.ai.goal.Goal<O> {

    // @formatter:off
    @Shadow public abstract EnumSet<Flag> shadow$getFlags();
    // @formatter:on

    @Override
    public GoalType type() {
        return ((GoalBridge) this).bridge$getType();
    }

    @Override
    public Optional<GoalExecutor<O>> executor() {
        return (Optional<GoalExecutor<O>>) (Optional<?>) ((GoalBridge) this).bridge$getGoalExecutor();
    }

    @Override
    public boolean canRunConcurrentWith(org.spongepowered.api.entity.ai.goal.Goal<O> other) {
        final EnumSet<Flag> flagSet1 = this.shadow$getFlags();
        final EnumSet<Flag> flagSet2 = ((Goal) other).getFlags();
        return flagSet1.stream().noneMatch(flagSet2::contains);
    }

    @Override
    public boolean canBeInterrupted() {
        return ((Goal) (Object) this).isInterruptable();
    }
}
