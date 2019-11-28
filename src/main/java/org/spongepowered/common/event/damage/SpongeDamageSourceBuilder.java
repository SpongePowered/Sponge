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

import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.common.AbstractDamageSourceBuilder;
import org.spongepowered.common.bridge.util.DamageSourceBridge;
import org.spongepowered.common.mixin.core.util.DamageSourceAccessor;

import java.util.function.Function;

public class SpongeDamageSourceBuilder extends AbstractDamageSourceBuilder<DamageSource, DamageSource.Builder> implements DamageSource.Builder {

    private static final Function<String, net.minecraft.util.DamageSource> DAMAGE_SOURCE_CTOR;

    static {
        DAMAGE_SOURCE_CTOR = (id) -> {
            final net.minecraft.util.DamageSource source = net.minecraft.util.DamageSource.func_188405_b((EntityLivingBase) null);
            ((DamageSourceAccessor) source).accessor$setId(id);
            ((DamageSourceBridge) source).bridge$resetDamageType();
            return source;
        };
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public DamageSource build() throws IllegalStateException {
        checkState(this.damageType != null, "DamageType was null!");
        final net.minecraft.util.DamageSource source = DAMAGE_SOURCE_CTOR.apply(this.damageType.toString());
        final DamageSourceAccessor accessor = (DamageSourceAccessor) source;
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
            source.func_82726_p();
        }
        if (this.scales) {
            source.func_76351_m();
        }
        if (this.explosion) {
            source.func_94540_d();
        }
        if (this.exhaustion != null) {
            accessor.accessor$setHungerDamage(this.exhaustion.floatValue());
        }
        return (DamageSource) source;
    }

}
