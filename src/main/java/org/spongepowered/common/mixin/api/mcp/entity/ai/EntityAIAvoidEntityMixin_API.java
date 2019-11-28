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

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.ai.task.builtin.creature.AvoidEntityAITask;
import org.spongepowered.api.entity.living.Creature;
import org.spongepowered.api.util.Functional;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Predicate;
import net.minecraft.entity.ai.goal.AvoidEntityGoal;

@SuppressWarnings("rawtypes")
@Mixin(AvoidEntityGoal.class)
public abstract class EntityAIAvoidEntityMixin_API extends EntityAIBaseMixin_API<Creature> implements AvoidEntityAITask {

    @Shadow @Final @Mutable private double farSpeed;
    @Shadow @Final @Mutable private double nearSpeed;
    @Shadow @Final @Mutable private float avoidDistance;
    @Shadow @Final @Mutable private com.google.common.base.Predicate avoidTargetSelector;

    @SuppressWarnings("unchecked")
    @Override
    public Predicate<Entity> getTargetSelector() {
        return this.avoidTargetSelector;
    }

    @Override
    public AvoidEntityAITask setTargetSelector(Predicate<Entity> predicate) {
        this.avoidTargetSelector = Functional.java8ToGuava(predicate);
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
