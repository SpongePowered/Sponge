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

import net.minecraft.entity.ai.EntityAIAvoidEntity;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.ai.task.builtin.creature.AvoidEntityAITask;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.GuavaJavaUtils;

import java.util.function.Predicate;

@SuppressWarnings("rawtypes")
@Mixin(EntityAIAvoidEntity.class)
public abstract class MixinEntityAIAvoidEntity extends MixinEntityAIBase implements AvoidEntityAITask {

    @Shadow private double farSpeed;
    @Shadow private double nearSpeed;
    @Shadow private float avoidDistance;
    @Shadow private com.google.common.base.Predicate avoidTargetSelector;

    @SuppressWarnings("unchecked")
    @Override
    public Predicate<Entity> getTargetSelector() {
        return GuavaJavaUtils.asJavaPredicate(this.avoidTargetSelector);
    }

    @Override
    public AvoidEntityAITask setTargetSelector(Predicate<Entity> predicate) {
        this.avoidTargetSelector = GuavaJavaUtils.asGuavaPredicate(predicate);
        return this;
    }

    @Override
    public float getSearchDistance() {
        return this.avoidDistance;
    }

    @Override
    public AvoidEntityAITask setSearchDistance(float distance) {
        this.avoidDistance = distance;
        return this;
    }

    @Override
    public double getCloseRangeSpeed() {
        return this.nearSpeed;
    }

    @Override
    public AvoidEntityAITask setCloseRangeSpeed(double speed) {
       this.nearSpeed = speed;
        return this;
    }

    @Override
    public double getFarRangeSpeed() {
        return this.farSpeed;
    }

    @Override
    public AvoidEntityAITask setFarRangeSpeed(double speed) {
        this.farSpeed = speed;
        return this;
    }
}
