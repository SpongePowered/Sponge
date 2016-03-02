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
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import org.spongepowered.api.entity.ai.task.builtin.creature.AttackLivingAITask;
import org.spongepowered.api.entity.living.Creature;
import org.spongepowered.api.entity.living.Living;

public final class SpongeAttackLivingAIBuilder implements AttackLivingAITask.Builder {
    private Class<? extends Living> targetClass;
    private double speed;
    private boolean longMemory;

    @Override
    public AttackLivingAITask.Builder target(Class<? extends Living> targetClass) {
        this.targetClass = targetClass;
        return this;
    }

    @Override
    public AttackLivingAITask.Builder speed(double speed) {
        this.speed = speed;
        return this;
    }

    @Override
    public AttackLivingAITask.Builder longMemory() {
        this.longMemory = true;
        return this;
    }

    @Override
    public AttackLivingAITask.Builder from(AttackLivingAITask value) {
        return target(value.getTargetClass())
            .speed(value.getSpeed())
            .longMemory();
    }

    @Override
    public AttackLivingAITask.Builder reset() {
        this.targetClass = null;
        this.speed = 0;
        this.longMemory = false;
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AttackLivingAITask build(Creature owner) {
        Preconditions.checkNotNull(owner);
        Preconditions.checkNotNull(this.targetClass);
        return (AttackLivingAITask) new EntityAIAttackMelee((EntityCreature) owner, (Class) this.targetClass, this.speed, this.longMemory);
    }
}
