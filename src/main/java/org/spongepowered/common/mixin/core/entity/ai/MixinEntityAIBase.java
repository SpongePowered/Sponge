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
package org.spongepowered.common.mixin.core.entity.ai;

import com.google.common.base.MoreObjects;
import net.minecraft.entity.ai.EntityAIBase;
import org.spongepowered.api.entity.ai.Goal;
import org.spongepowered.api.entity.ai.task.AITask;
import org.spongepowered.api.entity.ai.task.AITaskType;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.ai.IMixinEntityAIBase;
import org.spongepowered.common.registry.type.entity.AITaskTypeModule;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(EntityAIBase.class)
@Implements(value = @Interface(iface = AITask.class, prefix = "task$"))
public abstract class MixinEntityAIBase implements IMixinEntityAIBase {

    @Shadow private int mutexBits;

    private AITaskType type;
    private Optional<Goal<?>> owner = Optional.empty();

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void assignAITaskType(CallbackInfo ci) {
        final Optional<AITaskType> optAiTaskType = AITaskTypeModule.getInstance().getByAIClass(getClass());
        if (optAiTaskType.isPresent()) {
            this.type = optAiTaskType.get();
        }
    }

    public AITaskType task$getType() {
        return this.type;
    }

    public Optional<Goal<?>> task$getGoal() {
        return this.owner;
    }

    public boolean task$canRunConcurrentWith(AITask<?> other) {
        return (this.mutexBits & ((EntityAIBase) other).getMutexBits()) == 0;
    }

    public boolean task$canBeInterrupted() {
        return ((EntityAIBase) (Object) this).isInterruptible();
    }

    @Override
    public void setType(AITaskType type) {
        this.type = type;
    }

    @Override
    public Optional<Goal<?>> getAIGoal() {
        return this.owner;
    }

    @Override
    public void setGoal(@Nullable Goal<?> owner) {
        this.owner = Optional.ofNullable(owner);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .addValue(this.type)
                .addValue(task$getGoal())
                .toString();
    }
}
