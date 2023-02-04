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
package org.spongepowered.common.mixin.optimization.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.world.WorldInfoBridge;

import java.util.List;

@Mixin(EntityXPOrb.class)
public abstract class EntityXPOrbMixin_Merge {
    /**
     * A simple cached value of the merge radius for this xp orb.
     * Since the value is configurable, the first time searching for
     * other xp orbs, this value is cached.
     */
    private double cachedRadius = -1;

    @Shadow
    private int xpValue;

    @Inject(method = "<init>(Lnet/minecraft/world/World;DDDI)V", at = @At("RETURN"))
    private void onConstructed(World worldIn, double x, double y, double z, int expValue, CallbackInfo callbackInfo) {
        if (this.cachedRadius == -1) {
            final double configRadius = ((WorldInfoBridge) worldIn.getWorldInfo()).bridge$getConfigAdapter().getConfig().getWorld().getXpOrbMergeRadius();
            this.cachedRadius = configRadius < 0 ? 0 : configRadius;
        }

        final EntityXPOrb self = (EntityXPOrb) (Object) this;
        final List<EntityXPOrb> nearbyOrbs = worldIn.getEntitiesWithinAABB(EntityXPOrb.class, self.getEntityBoundingBox().grow(cachedRadius, 0.0, cachedRadius), Entity::isEntityAlive);
        if (!nearbyOrbs.isEmpty()) {
            int combinedXP = 0;
            for (EntityXPOrb entityXPOrb : nearbyOrbs) {
                if (self.getUniqueID().equals(entityXPOrb.getUniqueID())) {
                    continue;
                }

                combinedXP += entityXPOrb.getXpValue();
                entityXPOrb.setDead();
            }

            this.xpValue = combinedXP + expValue;
        }
    }
}