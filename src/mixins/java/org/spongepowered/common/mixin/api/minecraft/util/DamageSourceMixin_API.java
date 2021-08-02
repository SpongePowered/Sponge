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
package org.spongepowered.common.mixin.api.minecraft.util;

import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Interface.Remap;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.damagesource.DamageSourceBridge;

@Mixin(value = net.minecraft.world.damagesource.DamageSource.class)
@Implements(@Interface(iface = DamageSource.class, prefix = "damageSource$", remap = Remap.NONE))
public abstract class DamageSourceMixin_API implements DamageSource {

    // @formatter:off
    @Shadow public abstract boolean shadow$isBypassArmor();
    @Shadow public abstract boolean shadow$isBypassInvul();
    @Shadow public abstract boolean shadow$isBypassMagic();
    @Shadow public abstract boolean shadow$isMagic();
    @Shadow public abstract float shadow$getFoodExhaustion();
    @Shadow public abstract boolean shadow$scalesWithDifficulty();
    @Shadow public abstract boolean shadow$isExplosion();
    @Shadow public abstract String shadow$getMsgId();
    // @formatter:on

    @Override
    public boolean isExplosive() {
        return this.shadow$isExplosion();
    }

    @Intrinsic
    public boolean damageSource$isMagic() {
        return this.shadow$isMagic();
    }

    @Override
    public boolean doesAffectCreative() {
        return this.shadow$isBypassInvul();
    }

    @Override
    public boolean isAbsolute() {
        return this.shadow$isBypassMagic();
    }

    @Override
    public boolean isBypassingArmor() {
        return this.shadow$isBypassArmor();
    }

    @Override
    public boolean isScaledByDifficulty() {
        return this.shadow$scalesWithDifficulty();
    }

    @Override
    public double exhaustion() {
        return this.shadow$getFoodExhaustion();
    }

    @Override
    public DamageType type() {
        return ((DamageSourceBridge) this).bridge$getDamageType();
    }

}
