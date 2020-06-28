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

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.Explosion.Mode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.projectile.explosive.WitherSkull;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.projectile.source.ProjectileSource;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.entity.GrieferBridge;
import org.spongepowered.common.bridge.entity.projectile.WitherSkullEntityBridge;
import org.spongepowered.common.bridge.explosives.ExplosiveBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.util.Constants;
import org.spongepowered.math.vector.Vector3d;

import javax.annotation.Nullable;
import java.util.Optional;

@Mixin(WitherSkullEntity.class)
public abstract class WitherSkullEntityMixin extends DamagingProjectileEntityMixin implements WitherSkullEntityBridge, ExplosiveBridge {

    private int impl$explosionRadius = Constants.Entity.WitherSkull.DEFAULT_EXPLOSION_RADIUS;
    private float impl$damage = 0.0f;
    private boolean impl$damageSet = false;

    @ModifyArg(method = "onImpact",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z"))
    private float onAttackEntityFrom(final float amount) {
        if (this.impl$damageSet) {
            return this.impl$damage;
        }
        if (this.shootingEntity != null) {
            return Constants.Entity.WitherSkull.DEFAULT_WITHER_CREATED_SKULL_DAMAGE;
        }
        return Constants.Entity.WitherSkull.DEFAULT_NO_SOURCE_SKULL_DAMAGE;
    }

    @Override
    public void impl$readFromSpongeCompound(final CompoundNBT compound) {
        super.impl$readFromSpongeCompound(compound);
        if (compound.contains(Constants.Sponge.Entity.Projectile.PROJECTILE_DAMAGE_AMOUNT)) {
            this.impl$damage = compound.getFloat(Constants.Sponge.Entity.Projectile.PROJECTILE_DAMAGE_AMOUNT);
            this.impl$damageSet = true;
        }
    }

    @Override
    public void impl$writeToSpongeCompound(final CompoundNBT compound) {
        super.impl$writeToSpongeCompound(compound);
        if (this.impl$damageSet) {
            compound.putFloat(Constants.Sponge.Entity.Projectile.PROJECTILE_DAMAGE_AMOUNT, this.impl$damage);
        } else {
            compound.remove(Constants.Sponge.Entity.Projectile.PROJECTILE_DAMAGE_AMOUNT);
        }
    }

    // Explosive Impl
    @Override
    public Optional<Integer> bridge$getExplosionRadius() {
        return Optional.of(this.impl$explosionRadius);
    }

    @Override
    public void bridge$setExplosionRadius(final @Nullable Integer explosionRadius) {
        this.impl$explosionRadius = explosionRadius == null ? Constants.Entity.WitherSkull.DEFAULT_EXPLOSION_RADIUS : explosionRadius;
    }

    @Nullable
    @Redirect(method = "onImpact", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/World;createExplosion(Lnet/minecraft/entity/Entity;DDDFZLnet/minecraft/world/Explosion$Mode;)Lnet/minecraft/world/Explosion;"))
    public net.minecraft.world.Explosion impl$CreateAndProcessExplosionEvent(final net.minecraft.world.World worldObj, final Entity self,
            final double x, final double y, final double z, final float strength, final boolean flaming, final Mode mode) {
        return this.bridge$CreateAndProcessExplosionEvent(worldObj, self, x, y, z, strength, flaming, mode);
    }

    @Override
    public net.minecraft.world.Explosion bridge$CreateAndProcessExplosionEvent(
            final net.minecraft.world.World worldObj, final Entity self,
            final double x, final double y, final double z, final float strength, final boolean flaming, final net.minecraft.world.Explosion.Mode mode) {
        final boolean griefer = ((GrieferBridge) this).bridge$canGrief();
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(this);
            final ProjectileSource shooter = ((WitherSkull) this).shooter().get();
            frame.addContext(EventContextKeys.PROJECTILE_SOURCE, shooter);
            frame.pushCause(shooter);
            return SpongeCommonEventFactory.detonateExplosive(this, Explosion.builder()
                    .location(ServerLocation.of((World) worldObj, new Vector3d(x, y, z)))
                    .sourceExplosive(((WitherSkull) this))
                    .radius(this.impl$explosionRadius)
                    .canCauseFire(flaming)
                    .shouldPlaySmoke(mode != Mode.NONE && griefer)
                    .shouldBreakBlocks(mode != Mode.NONE && griefer))
                    .orElse(null);
        }
    }
}
