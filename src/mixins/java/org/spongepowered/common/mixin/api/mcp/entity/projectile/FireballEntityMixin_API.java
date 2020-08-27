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
package org.spongepowered.common.mixin.api.mcp.entity.projectile;

import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameRules;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.projectile.explosive.fireball.ExplosiveFireball;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.entity.projectile.FireballEntityBridge;

import java.util.Set;

@Mixin(FireballEntity.class)
public abstract class FireballEntityMixin_API extends AbstractFireballEntityMixin_API implements ExplosiveFireball {

    @Shadow public int explosionPower;

    @Override
    public void detonate() {
        final boolean flag = this.shadow$getEntityWorld().getGameRules().getBoolean(GameRules.MOB_GRIEFING);
        final Explosion.Mode mode = flag ? Explosion.Mode.DESTROY : Explosion.Mode.NONE;
        ((FireballEntityBridge) this).bridge$throwExplosionEventAndExplode(this.shadow$getEntityWorld(), null, this.shadow$getPosX(),
                this.shadow$getPosY(), this.shadow$getPosZ(), this.explosionPower, flag, mode);
        this.shadow$remove();
    }

    @Override
    protected Set<Value.Immutable<?>> api$getVanillaValues() {
        final Set<Value.Immutable<?>> values = super.api$getVanillaValues();

        // Explosive
        this.explosionRadius().map(Value::asImmutable).ifPresent(values::add);

        return values;
    }

}
