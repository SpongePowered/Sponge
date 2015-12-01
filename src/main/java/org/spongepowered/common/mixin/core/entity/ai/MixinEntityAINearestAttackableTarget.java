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

import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import org.spongepowered.api.entity.ai.task.builtin.creature.target.FindNearestAttackableTargetAITask;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.util.GuavaJavaUtils;

import java.util.ArrayList;
import java.util.List;

@Mixin(EntityAINearestAttackableTarget.class)
public abstract class MixinEntityAINearestAttackableTarget extends MixinEntityAITarget<FindNearestAttackableTargetAITask>
        implements FindNearestAttackableTargetAITask {

    @Shadow protected Class targetClass;
    @Shadow private int targetChance;
    @Shadow protected Predicate targetEntitySelector;

    @Override
    public Class<? extends Living> getTargetClass() {
        return targetClass;
    }

    @Override
    public FindNearestAttackableTargetAITask setTargetClass(Class<? extends Living> targetClass) {
        this.targetClass = targetClass;
        return this;
    }

    @Override
    public int getChance() {
        return targetChance;
    }

    @Override
    public FindNearestAttackableTargetAITask setChance(int chance) {
        this.targetChance = chance;
        return this;
    }

    @Override
    public FindNearestAttackableTargetAITask filter(java.util.function.Predicate<Living> predicate) {
        this.targetEntitySelector = predicate == null ? null : GuavaJavaUtils.asGuavaPredicate(predicate);
        return this;
    }

    @Redirect(method = "shouldExecute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getEntitiesWithinAABB(Ljava/lang/Class;"
            + "Lnet/minecraft/util/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;"))
    public List<Entity> onGetEntitiesWithinAABB(World world, Class clazz, AxisAlignedBB aabb, Predicate predicate) {
        List<Entity> entities = new ArrayList<>();
        for (Entity entity : (List<Entity>) world.getEntities(this.targetClass, predicate)) {
            if (entity.getEntityBoundingBox().intersectsWith(aabb)) {
                entities.add(entity);
            }
        }
        return entities;
    }

}
