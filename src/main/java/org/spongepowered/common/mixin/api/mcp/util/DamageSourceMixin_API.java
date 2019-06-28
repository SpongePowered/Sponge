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
package org.spongepowered.common.mixin.api.mcp.util;

import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.util.DamageSourceBridge;

@Mixin(value = net.minecraft.util.DamageSource.class)
public abstract class DamageSourceMixin_API implements DamageSource {

    @Shadow public abstract boolean isProjectile();
    @Shadow public abstract boolean isUnblockable();
    @Shadow public abstract boolean canHarmInCreative();
    @Shadow public abstract boolean isDamageAbsolute();
    @Shadow public abstract boolean isMagicDamage();
    @Shadow public abstract float getHungerDamage();
    @Shadow public abstract boolean isDifficultyScaled();
    @Shadow public abstract boolean isExplosion();
    @Shadow public abstract String getDamageType();

    @Shadow public String damageType;

    @Override
    public boolean isExplosive() {
        return isExplosion();
    }

    @Override
    public boolean isMagic() {
        return isMagicDamage();
    }

    @Override
    public boolean doesAffectCreative() {
        return canHarmInCreative();
    }

    @Override
    public boolean isAbsolute() {
        return isDamageAbsolute();
    }

    @Override
    public boolean isBypassingArmor() {
        return isUnblockable();
    }

    @Override
    public boolean isScaledByDifficulty() {
        return isDifficultyScaled();
    }

    @Override
    public double getExhaustion() {
        return getHungerDamage();
    }

    @Override
    public DamageType getType() {
        return ((DamageSourceBridge) this).bridge$getDamageType();
    }

}
