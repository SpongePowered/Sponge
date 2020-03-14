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
package org.spongepowered.common.mixin.core.entity.projectile;

import net.minecraft.entity.projectile.SnowballEntity;
import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.api.entity.projectile.Snowball;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.common.util.Constants;

@Mixin(SnowballEntity.class)
public abstract class SnowballEntityMixin extends ThrowableEntityMixin implements Snowball {

    private double impl$damageAmount = 0;
    private boolean impl$damageSet = false;

    @ModifyArg(method = "onImpact", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z"))
    private float impl$onAttackEntityFromUseCustomDamage(float damage) {
        return this.impl$damageSet ? (float) this.impl$damageAmount : damage;
    }

    @Override
    public void impl$readFromSpongeCompound(CompoundNBT compound) {
        super.impl$readFromSpongeCompound(compound);
        if (compound.contains(Constants.Sponge.Entity.Projectile.PROJECTILE_DAMAGE_AMOUNT)) {
            this.impl$damageAmount = compound.getDouble(Constants.Sponge.Entity.Projectile.PROJECTILE_DAMAGE_AMOUNT);
            this.impl$damageSet = true;
        }
    }

    @Override
    public void impl$writeToSpongeCompound(CompoundNBT compound) {
        super.impl$writeToSpongeCompound(compound);
        if (this.impl$damageSet) {
            compound.putDouble(Constants.Sponge.Entity.Projectile.PROJECTILE_DAMAGE_AMOUNT, this.impl$damageAmount);
        } else {
            compound.remove(Constants.Sponge.Entity.Projectile.PROJECTILE_DAMAGE_AMOUNT);
        }
    }

}
