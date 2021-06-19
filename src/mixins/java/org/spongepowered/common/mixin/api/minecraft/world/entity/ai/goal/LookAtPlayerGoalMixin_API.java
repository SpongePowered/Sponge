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

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import org.spongepowered.api.entity.ai.goal.builtin.LookAtGoal;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@SuppressWarnings({"unchecked"})
@Mixin(net.minecraft.world.entity.ai.goal.LookAtPlayerGoal.class)
public abstract class LookAtPlayerGoalMixin_API extends Goal implements LookAtGoal {

    // @formatter:off
    @Shadow @Final @Mutable protected Class<? extends LivingEntity> lookAtType;
    @Shadow @Final @Mutable protected float lookDistance;
    @Shadow @Final @Mutable protected float probability;
    // @formatter:on

    @Override
    public Class<? extends Living> watchedClass() {
        return (Class<? extends Living>) this.lookAtType;
    }

    @Override
    public LookAtGoal setWatchedClass(Class<? extends Living> watchedClass) {
        this.lookAtType = (Class<? extends LivingEntity>) watchedClass;
        return this;
    }

    @Override
    public float maxDistance() {
        return this.lookDistance;
    }

    @Override
    public LookAtGoal setMaxDistance(float maxDistance) {
        this.lookDistance = maxDistance;
        return this;
    }

    @Override
    public float chance() {
        return this.probability;
    }

    @Override
    public LookAtGoal setChance(float chance) {
        this.probability = chance;
        return this;
    }
}
