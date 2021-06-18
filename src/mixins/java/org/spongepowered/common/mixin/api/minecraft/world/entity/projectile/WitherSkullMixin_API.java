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
package org.spongepowered.common.mixin.api.minecraft.world.entity.projectile;

import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.projectile.explosive.WitherSkull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.world.entity.projectile.WitherSkullBridge;

import java.util.Set;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;

@Mixin(net.minecraft.world.entity.projectile.WitherSkull.class)
public abstract class WitherSkullMixin_API extends AbstractHurtingProjectileMixin_API implements WitherSkull {

    @Override
    public void detonate() {
        final Explosion.BlockInteraction mode = this.shadow$getCommandSenderWorld().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) ? Explosion.BlockInteraction.DESTROY : Explosion.BlockInteraction.NONE;
        ((WitherSkullBridge) this).bridge$throwExplosionEventAndExplosde(this.shadow$getCommandSenderWorld(), (net.minecraft.world.entity.projectile.WitherSkull) (Object) this,
                this.shadow$getX(), this.shadow$getY(), this.shadow$getZ(), 1.0F, false, mode);
        this.shadow$remove();
    }

    @Override
    protected Set<Value.Immutable<?>> api$getVanillaValues() {
        final Set<Value.Immutable<?>> values = super.api$getVanillaValues();

        // FireballEntity
        values.add(this.acceleration().asImmutable());

        // Explosive
        this.explosionRadius().map(Value::asImmutable).ifPresent(values::add);

        return values;
    }

}
