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

import net.minecraft.entity.ai.goal.TargetGoal;
import org.spongepowered.api.entity.ai.task.builtin.creature.target.TargetAITask;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.mixin.core.entity.ai.EntityAIBaseMixin;

@Mixin(TargetGoal.class)
public abstract class EntityAITargetMixin_API<A extends TargetAITask<A>> extends EntityAIBaseMixin implements TargetAITask<A> {

    @Shadow protected boolean shouldCheckSight;
    @Shadow @Final @Mutable private boolean nearbyOnly;
    @Shadow private int targetSearchStatus;
    @Shadow private int targetSearchDelay;
    @Shadow private int targetUnseenTicks;

    @Override
    public boolean shouldCheckSight() {
        return this.shouldCheckSight;
    }

    @SuppressWarnings("unchecked")
    @Override
    public A setCheckSight(boolean checkSight) {
        this.shouldCheckSight = checkSight;
        return (A) this;
    }

    @Override
    public boolean onlyNearby() {
        return this.nearbyOnly;
    }

    @SuppressWarnings("unchecked")
    @Override
    public A setOnlyNearby(boolean nearby) {
        this.nearbyOnly = nearby;
        return (A) this;
    }

}
