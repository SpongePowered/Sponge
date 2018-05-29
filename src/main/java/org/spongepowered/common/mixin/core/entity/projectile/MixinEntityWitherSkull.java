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

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.projectile.explosive.WitherSkull;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.interfaces.entity.IMixinGriefer;
import org.spongepowered.common.interfaces.entity.explosive.IMixinExplosive;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(EntityWitherSkull.class)
public abstract class MixinEntityWitherSkull extends MixinEntityFireball implements WitherSkull, IMixinExplosive {

    private static final String EXPLOSION_TARGET = "Lnet/minecraft/world/World;newExplosion"
            + "(Lnet/minecraft/entity/Entity;DDDFZZ)Lnet/minecraft/world/Explosion;";
    private static final int DEFAULT_EXPLOSION_RADIUS = 1;

    @Nullable private Cause detonationCause;
    private int explosionRadius = DEFAULT_EXPLOSION_RADIUS;
    private float damage = 0.0f;
    private boolean damageSet = false;

    @ModifyArg(method = "onImpact",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z"))
    protected float onAttackEntityFrom(float amount) {
        return (float) getDamage();
    }

    public double getDamage() {
        if (this.damageSet) {
            return this.damage;
        }
        if (this.shootingEntity != null) {
            return 8.0f;
        }
        return 5.0f;
    }

    public void setDamage(double damage) {
        this.damageSet = true;
        this.damage = (float) damage;
    }

    @Override
    public void readFromNbt(NBTTagCompound compound) {
        super.readFromNbt(compound);
        if (compound.hasKey(NbtDataUtil.PROJECTILE_DAMAGE_AMOUNT)) {
            this.damage = compound.getFloat(NbtDataUtil.PROJECTILE_DAMAGE_AMOUNT);
            this.damageSet = true;
        }
    }

    @Override
    public void writeToNbt(NBTTagCompound compound) {
        super.writeToNbt(compound);
        if (this.damageSet) {
            compound.setFloat(NbtDataUtil.PROJECTILE_DAMAGE_AMOUNT, this.damage);
        } else {
            compound.removeTag(NbtDataUtil.PROJECTILE_DAMAGE_AMOUNT);
        }
    }

    // Explosive Impl
    @Override
    public Optional<Integer> getExplosionRadius() {
        return Optional.of(this.explosionRadius);
    }

    @Override
    public void setExplosionRadius(Optional<Integer> explosionRadius) {
        this.explosionRadius = explosionRadius.orElse(DEFAULT_EXPLOSION_RADIUS);
    }

    @Override
    public void detonate() {
        onExplode(this.world, (Entity) (Object) this, this.posX, this.posY, this.posZ, 0, false, true);
        setDead();
    }

    @Redirect(method = "onImpact", at = @At(value = "INVOKE", target = EXPLOSION_TARGET))
    protected net.minecraft.world.Explosion onExplode(net.minecraft.world.World worldObj, Entity self, double x,
                                                      double y, double z, float strength, boolean flaming,
                                                      boolean smoking) {
        boolean griefer = ((IMixinGriefer) this).canGrief();
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(this);
            frame.addContext(EventContextKeys.THROWER, getShooter());
            frame.pushCause(getShooter());
            return detonate(Explosion.builder()
                .location(new Location<>((World) worldObj, new Vector3d(x, y, z)))
                .sourceExplosive(this)
                .radius(this.explosionRadius)
                .canCauseFire(flaming)
                .shouldPlaySmoke(smoking && griefer)
                .shouldBreakBlocks(smoking && griefer))
                .orElse(null);
        }
    }

}
