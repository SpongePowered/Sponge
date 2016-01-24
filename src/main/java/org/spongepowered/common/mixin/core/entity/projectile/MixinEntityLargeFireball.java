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

import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import org.spongepowered.api.entity.projectile.explosive.fireball.LargeFireball;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.util.NbtDataUtil;

import java.util.List;

import javax.annotation.Nullable;

@Mixin(EntityLargeFireball.class)
public abstract class MixinEntityLargeFireball extends MixinEntityFireball implements LargeFireball {

    private static final String NEW_EXPLOSION_METHOD =
        "Lnet/minecraft/world/World;newExplosion(Lnet/minecraft/entity/Entity;DDDFZZ)Lnet/minecraft/world/Explosion;";
    @Shadow public int explosionPower;

    private float damage = 6.0f;

    @ModifyArg(method = "onImpact(Lnet/minecraft/util/MovingObjectPosition;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z"))
    protected float onAttackEntityFrom(float amount) {
        return this.damage;
    }

    @Nullable
    @Redirect(method = "onImpact(Lnet/minecraft/util/MovingObjectPosition;)V", at = @At(value = "INVOKE", target = NEW_EXPLOSION_METHOD))
    public Explosion processExplosion(World world, Entity entity, double x, double y, double z, float strength, boolean flaming, boolean smoking) {
        List<Object> cause = Lists.newArrayList();

        if (this.shootingEntity != null) {
            cause.add(NamedCause.igniter(this.shootingEntity));
        }

        cause.add(NamedCause.source(this));

        ExplosionEvent.Pre event = SpongeEventFactory.createExplosionEventPre(Cause.of(cause), (org.spongepowered.api.world.World) this.worldObj);
        if (!SpongeImpl.postEvent(event)) {
            world.newExplosion((Entity) (Object) this, x, y, z, strength, flaming, smoking);
        }

        return null;
    }

    public double getDamage() {
        return this.damage;
    }

    public void setDamage(double damage) {
        this.damage = (float) damage;
    }

    public int getExplosionPower() {
        return this.explosionPower;
    }

    public void setExplosionPower(int explosionPower) {
        this.explosionPower = explosionPower;
    }

    @Override
    public void readFromNbt(NBTTagCompound compound) {
        super.readFromNbt(compound);
        if (compound.hasKey(NbtDataUtil.PROJECTILE_DAMAGE_AMOUNT)) {
            this.damage = compound.getFloat(NbtDataUtil.PROJECTILE_DAMAGE_AMOUNT);
        }
    }

    @Override
    public void writeToNbt(NBTTagCompound compound) {
        super.writeToNbt(compound);
        compound.setFloat(NbtDataUtil.PROJECTILE_DAMAGE_AMOUNT, this.damage);
    }
}
