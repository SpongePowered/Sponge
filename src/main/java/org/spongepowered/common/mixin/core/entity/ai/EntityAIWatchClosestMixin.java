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

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

import javax.annotation.Nullable;

@SuppressWarnings({"unchecked", "rawtypes"})
@Mixin(LookAtGoal.class)
public abstract class EntityAIWatchClosestMixin extends Goal {

    @Shadow protected Class watchedClass;

    @Nullable
    @Redirect(method = "shouldExecute",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;findNearestEntityWithinAABB(Ljava/lang/Class;Lnet/minecraft/util/math/AxisAlignedBB;Lnet/minecraft/entity/Entity;)Lnet/minecraft/entity/Entity;"))
    private net.minecraft.entity.Entity onFindNearestEntityWithinAABB(final World world, final Class clazz, final AxisAlignedBB aabb,
        final net.minecraft.entity.Entity entity) {
        net.minecraft.entity.Entity entity1 = null;
        double d0 = Double.MAX_VALUE;

        for (final net.minecraft.entity.Entity foundEntity: (List< net.minecraft.entity.Entity>) world.getEntities(this.watchedClass,
                EntityPredicates.NOT_SPECTATING)) {
            if (foundEntity.getBoundingBox().intersects(aabb) && foundEntity != entity) {
                final double d1 = entity.getDistanceSq(foundEntity);

                if (d1 <= d0)
                {
                    entity1 = foundEntity;
                    d0 = d1;
                }
            }
        }

        return entity1;
    }

}
