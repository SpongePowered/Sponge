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
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.ai.task.builtin.WatchClosestAITask;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImpl;

import javax.annotation.Nullable;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtGoal;

@SuppressWarnings({"unchecked", "rawtypes"})
@Mixin(LookAtGoal.class)
public abstract class EntityAIWatchClosestMixin_API extends Goal implements WatchClosestAITask {

    @Shadow protected Class watchedClass;
    @Shadow protected float maxDistance;
    @Shadow @Final @Mutable private float chance;

    @Nullable private EntityType api$watchedType;

    @Override
    public Class<? extends Entity> getWatchedClass() {
        if (this.api$watchedType == null) {
            this.api$watchedType = SpongeImpl.getRegistry().getTranslated(this.watchedClass, EntityType.class);
        }
        return this.watchedClass;
    }

    @Override
    public WatchClosestAITask setWatchedClass(final Class<? extends Entity> watchedClass) {
        this.watchedClass = watchedClass;
        return this;
    }

    @Override
    public float getMaxDistance() {
        return this.maxDistance;
    }

    @Override
    public WatchClosestAITask setMaxDistance(final float maxDistance) {
        this.maxDistance = maxDistance;
        return this;
    }

    @Override
    public float getChance() {
        return this.chance;
    }

    @Override
    public WatchClosestAITask setChance(final float chance) {
        this.chance = chance;
        return this;
    }

}
