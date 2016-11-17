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

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.ai.task.builtin.WatchClosestAITask;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeImpl;

import java.util.List;

import javax.annotation.Nullable;

@SuppressWarnings({"unchecked", "rawtypes"})
@Mixin(EntityAIWatchClosest.class)
public abstract class MixinEntityAIWatchClosest extends EntityAIBase implements WatchClosestAITask {

    @Shadow private Class watchedClass;
    @Shadow private float maxDistanceForPlayer;
    @Shadow @Final @Mutable private float chance;
    @Nullable
    private EntityType watchedType;

    @Override
    public Class<? extends Entity> getWatchedClass() {
        if (this.watchedType == null) {
            this.watchedType = SpongeImpl.getRegistry().getTranslated(this.watchedClass, EntityType.class);
        }
        return this.watchedClass;
    }

    @Override
    public WatchClosestAITask setWatchedClass(Class<? extends Entity> watchedClass) {
        this.watchedClass = watchedClass;
        return this;
    }

    @Override
    public float getMaxDistance() {
        return this.maxDistanceForPlayer;
    }

    @Override
    public WatchClosestAITask setMaxDistance(float maxDistance) {
        this.maxDistanceForPlayer = maxDistance;
        return this;
    }

    @Override
    public float getChance() {
        return this.chance;
    }

    @Override
    public WatchClosestAITask setChance(float chance) {
        this.chance = chance;
        return this;
    }

    @Nullable
    @Redirect(method = "shouldExecute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;findNearestEntityWithinAABB(Ljava/lang/Class;"
            + "Lnet/minecraft/util/math/AxisAlignedBB;Lnet/minecraft/entity/Entity;)Lnet/minecraft/entity/Entity;"))
    public net.minecraft.entity.Entity onFindNearestEntityWithinAABB(World world, Class clazz, AxisAlignedBB aabb, net.minecraft.entity.Entity entity) {
        net.minecraft.entity.Entity entity1 = null;
        double d0 = Double.MAX_VALUE;

        for (net.minecraft.entity.Entity foundEntity: (List< net.minecraft.entity.Entity>) world.getEntities(this.watchedClass,
                EntitySelectors.NOT_SPECTATING)) {
            if (foundEntity.getEntityBoundingBox().intersectsWith(aabb) && foundEntity != entity) {
                double d1 = entity.getDistanceSqToEntity(foundEntity);

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
