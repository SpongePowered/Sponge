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
package org.spongepowered.common.mixin.api.mcp.entity.ai;

import net.minecraft.entity.ai.EntityAIBase;
import org.spongepowered.api.entity.ai.Goal;
import org.spongepowered.api.entity.ai.task.AITask;
import org.spongepowered.api.entity.ai.task.AITaskType;
import org.spongepowered.api.entity.living.Agent;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.entity.ai.EntityAIBasesBridge;

import java.util.Optional;

@Mixin(EntityAIBase.class)
@Implements(value = @Interface(iface = AITask.class, prefix = "task$"))
public abstract class EntityAIBaseMixin_API<O extends Agent> implements AITask<O> {

    @Shadow private int mutexBits;

    @Override
    public AITaskType getType() {
        return ((EntityAIBasesBridge) this).bridge$getType();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<Goal<O>> getGoal() {
        return (Optional<Goal<O>>) (Optional<?>) ((EntityAIBasesBridge) this).bridge$getAIGoal();
    }

    @Override
    public boolean canRunConcurrentWith(AITask<O> other) {
        return (this.mutexBits & ((EntityAIBase) other).func_75247_h()) == 0;
    }

    @Override
    public boolean canBeInterrupted() {
        return ((EntityAIBase) (Object) this).func_75252_g();
    }


}
