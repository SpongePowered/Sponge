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
package org.spongepowered.common.event.damage;

import static com.google.common.base.Preconditions.checkState;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.common.AbstractDamageSourceBuilder;
import org.spongepowered.common.bridge.util.DamageSourceBridge;
import org.spongepowered.common.mixin.accessor.util.DamageSourceAccessor;

import java.util.function.Function;
import net.minecraft.entity.LivingEntity;

public class SpongeDamageSourceBuilder extends AbstractDamageSourceBuilder<DamageSource, DamageSource.Builder> implements DamageSource.Builder {

    @SuppressWarnings("ConstantConditions")
    @Override
    public DamageSource build() throws IllegalStateException {
        checkState(this.damageType != null, "DamageType was null!");
        final net.minecraft.util.DamageSource source = DamageSourceAccessor.accessor$createDamageSource(this.damageType.toString());
        final DamageSourceAccessor accessor = (DamageSourceAccessor) source;
        if (!this.scales
                && this.bypasses
                && !this.explosion
                && !this.absolute
                && !this.magical
                && !this.creative
                && !this.fire
                && this.exhaustion == null
                && this.damageType.equals(DamageTypes.DROWN)
        ) {
            return (DamageSource) net.minecraft.util.DamageSource.DROWN;
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
            return (DamageSource) net.minecraft.util.DamageSource.DRYOUT;
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
            return (DamageSource) net.minecraft.util.DamageSource.FALL;
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
            return (DamageSource) net.minecraft.util.DamageSource.ON_FIRE;
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
            return (DamageSource) net.minecraft.util.DamageSource.GENERIC;
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
            return (DamageSource) net.minecraft.util.DamageSource.MAGIC;
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
            return (DamageSource) net.minecraft.util.DamageSource.STARVE;
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
            return (DamageSource) net.minecraft.util.DamageSource.OUT_OF_WORLD;
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
            return (DamageSource) net.minecraft.util.DamageSource.WITHER;
        }
        if (this.absolute) {
            accessor.accessor$setDamageIsAbsolute();
        }
        if (this.bypasses) {
            accessor.accessor$setDamageBypassesArmor();
        }
        if (this.creative) {
            accessor.accessor$setDamageAllowedInCreativeMode();
        }
        if (this.magical) {
            source.setMagicDamage();
        }
        if (this.scales) {
            source.setDifficultyScaled();
        }
        if (this.explosion) {
            source.setExplosion();
        }
        if (this.exhaustion != null) {
            accessor.accessor$setHungerDamage(this.exhaustion.floatValue());
        }
        if (this.fire) {
            accessor.accessor$setFireDamage();
        }
        return (DamageSource) source;
    }

}
