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

import net.minecraft.world.entity.Mob;
import org.spongepowered.api.entity.ai.goal.builtin.LookAtGoal;
import org.spongepowered.api.entity.living.Agent;
import org.spongepowered.api.entity.living.Living;

import java.util.Objects;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class SpongeWatchClosestAIBuilder implements LookAtGoal.Builder {

    private Class<? extends Living> watchedClass;
    private float maxDistance;
    private float chance;

    public SpongeWatchClosestAIBuilder() {
        this.reset();
    }

    @Override
    public LookAtGoal.Builder watch(Class<? extends Living> watchedClass) {
        this.watchedClass = watchedClass;
        return this;
    }

    @Override
    public LookAtGoal.Builder maxDistance(float maxDistance) {
        this.maxDistance = maxDistance;
        return this;
    }

    @Override
    public LookAtGoal.Builder chance(float chance) {
        this.chance = chance;
        return this;
    }

    @Override
    public LookAtGoal.Builder from(LookAtGoal value) {
        return this.watch(value.watchedClass())
            .maxDistance(value.maxDistance())
            .chance(value.chance());
    }

    @Override
    public LookAtGoal.Builder reset() {
        this.watchedClass = null;
        this.maxDistance = 8;
        this.chance = 0.02f;
        return this;
    }

    @Override
    public LookAtGoal build(Agent owner) {
        Objects.requireNonNull(this.watchedClass);
        return (LookAtGoal) new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal((Mob) owner, (Class) this.watchedClass, this.maxDistance, this.chance);
    }
}
