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
package org.spongepowered.common.mixin.core.entity.ai.goal;

import com.google.common.base.MoreObjects;
import org.spongepowered.api.entity.ai.goal.GoalExecutor;
import org.spongepowered.api.entity.ai.goal.GoalType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.entity.ai.GoalBridge;
import org.spongepowered.common.registry.type.entity.AITaskTypeModule;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(net.minecraft.entity.ai.goal.Goal.class)
public abstract class GoalMixin implements GoalBridge {

    private GoalType impl$type;
    private Optional<GoalExecutor<?>> impl$owner = Optional.empty();

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void assignAITaskType(final CallbackInfo ci) {
        final Optional<GoalType> optAiTaskType = AITaskTypeModule.getInstance().getByAIClass(this.getClass());
        if (optAiTaskType.isPresent()) {
            this.impl$type = optAiTaskType.get();
        }
    }

    @Override
    public GoalType bridge$getType() {
        return this.impl$type;
    }

    @Override
    public void bridge$setType(final GoalType type) {
        this.impl$type = type;
    }

    @Override
    public Optional<GoalExecutor<?>> bridge$getGoalExecutor() {
        return this.impl$owner;
    }

    @Override
    public void bridge$setGoalExecutor(@Nullable final GoalExecutor<?> owner) {
        this.impl$owner = Optional.ofNullable(owner);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .addValue(this.impl$type)
                .addValue(this.impl$owner)
                .toString();
    }
}
