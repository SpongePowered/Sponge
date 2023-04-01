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
package org.spongepowered.common.event.cause.entity.damage;

import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.common.AbstractDamageSourceBuilder;
import org.spongepowered.common.accessor.world.damagesource.DamageSourceAccessor;

public class SpongeDamageSourceBuilder extends AbstractDamageSourceBuilder<DamageSource, DamageSource.Builder> implements DamageSource.Builder {

    @SuppressWarnings("ConstantConditions")
    @Override
    public DamageSource build() throws IllegalStateException {
        checkState(this.damageType != null, "DamageType was null!");
        final net.minecraft.world.damagesource.DamageSource source = DamageSourceAccessor.invoker$new(this.damageType.toString());
        final DamageSourceAccessor accessor = (DamageSourceAccessor) source;
        if (!this.scales
                && this.bypasses
                && !this.explosion
                && !this.absolute
                && !this.magical
                && !this.creative
                && !this.fire
                && this.exhaustion == null
                && this.damageType.equals(DamageTypes.DROWN.get())
        ) {
            return (DamageSource) net.minecraft.world.damagesource.DamageSource.DROWN;
        }
        if (!this.scales
                && !this.bypasses
                && !this.explosion
                && !this.absolute
                && !this.magical
                && !this.creative
                && !this.fire
                && this.exhaustion == null
                && this.damageType.equals(DamageTypes.DRYOUT.get())
        ) {
            return (DamageSource) net.minecraft.world.damagesource.DamageSource.DRY_OUT;
        }
        if (!this.scales
                && !this.bypasses
                && !this.explosion
                && !this.absolute
                && !this.magical
                && !this.creative
                && !this.fire
                && this.exhaustion == null
                && this.damageType.equals(DamageTypes.FALL.get())
        ) {
            return (DamageSource) net.minecraft.world.damagesource.DamageSource.FALL;
        }
        if (!this.scales
                && !this.bypasses
                && !this.explosion
                && !this.absolute
                && !this.magical
                && !this.creative
                && this.fire
                && this.exhaustion == null
                && this.damageType.equals(DamageTypes.FIRE.get())
        ) {
            return (DamageSource) net.minecraft.world.damagesource.DamageSource.ON_FIRE;
        }
        if (!this.scales
                && this.bypasses
                && !this.explosion
                && !this.absolute
                && !this.magical
                && !this.creative
                && !this.fire
                && this.exhaustion == null
                && this.damageType.equals(DamageTypes.GENERIC.get())
        ) {
            return (DamageSource) net.minecraft.world.damagesource.DamageSource.GENERIC;
        }
        if (!this.scales
                && this.bypasses
                && !this.explosion
                && !this.absolute
                && this.magical
                && !this.creative
                && !this.fire
                && this.exhaustion == null
                && this.damageType.equals(DamageTypes.MAGIC.get())
        ) {
            return (DamageSource) net.minecraft.world.damagesource.DamageSource.MAGIC;
        }
        if (!this.scales
                && this.bypasses
                && !this.explosion
                && this.absolute
                && !this.magical
                && !this.creative
                && !this.fire
                && this.exhaustion == null
                && this.damageType.equals(DamageTypes.HUNGER.get())
        ) {
            return (DamageSource) net.minecraft.world.damagesource.DamageSource.STARVE;
        }
        if (!this.scales
                && this.bypasses
                && !this.explosion
                && !this.absolute
                && !this.magical
                && this.creative
                && !this.fire
                && this.exhaustion == null
                && this.damageType.equals(DamageTypes.VOID.get())
        ) {
            return (DamageSource) net.minecraft.world.damagesource.DamageSource.OUT_OF_WORLD;
        }
        if (!this.scales
                && !this.bypasses
                && !this.explosion
                && !this.absolute
                && this.magical
                && !this.creative
                && !this.fire
                && this.exhaustion == null
                && this.damageType.equals(DamageTypes.MAGIC.get())
        ) {
            return (DamageSource) net.minecraft.world.damagesource.DamageSource.WITHER;
        }
        if (this.absolute) {
            accessor.invoker$bypassMagic();
        }
        if (this.bypasses) {
            accessor.invoker$bypassArmor();
        }
        if (this.creative) {
            accessor.invoker$bypassInvul();
        }
        if (this.magical) {
            source.setMagic();
        }
        if (this.scales) {
            source.setScalesWithDifficulty();
        }
        if (this.explosion) {
            source.setExplosion();
        }
        if (this.fire) {
            accessor.invoker$setIsFire();
        }
        if (this.exhaustion != null) {
            accessor.accessor$exhaustion(this.exhaustion.floatValue());
        }
        return (DamageSource) source;
    }

}
