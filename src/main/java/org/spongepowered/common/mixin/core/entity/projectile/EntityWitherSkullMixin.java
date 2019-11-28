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
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.projectile.explosive.WitherSkull;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.entity.GrieferBridge;
import org.spongepowered.common.bridge.entity.item.EntityWitherSkullBridge;
import org.spongepowered.common.bridge.explosives.ExplosiveBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(WitherSkullEntity.class)
public abstract class EntityWitherSkullMixin extends EntityFireballMixin implements EntityWitherSkullBridge, ExplosiveBridge {

    private int explosionRadius = Constants.Entity.WitherSkull.DEFAULT_EXPLOSION_RADIUS;
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
    public void spongeImpl$readFromSpongeCompound(final CompoundNBT compound) {
        super.spongeImpl$readFromSpongeCompound(compound);
        if (compound.func_74764_b(Constants.Sponge.Entity.Projectile.PROJECTILE_DAMAGE_AMOUNT)) {
            this.impl$damage = compound.func_74760_g(Constants.Sponge.Entity.Projectile.PROJECTILE_DAMAGE_AMOUNT);
            this.impl$damageSet = true;
        }
    }

    @Override
    public void spongeImpl$writeToSpongeCompound(final CompoundNBT compound) {
        super.spongeImpl$writeToSpongeCompound(compound);
        if (this.impl$damageSet) {
            compound.func_74776_a(Constants.Sponge.Entity.Projectile.PROJECTILE_DAMAGE_AMOUNT, this.impl$damage);
        } else {
            compound.func_82580_o(Constants.Sponge.Entity.Projectile.PROJECTILE_DAMAGE_AMOUNT);
        }
    }

    // Explosive Impl
    @Override
    public Optional<Integer> bridge$getExplosionRadius() {
        return Optional.of(this.explosionRadius);
    }

    @Override
    public void bridge$setExplosionRadius(final @Nullable Integer explosionRadius) {
        this.explosionRadius = explosionRadius == null ? Constants.Entity.WitherSkull.DEFAULT_EXPLOSION_RADIUS : explosionRadius;
    }

    @Override
    @SuppressWarnings("deprecation")
    @Redirect(method = "onImpact", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;newExplosion"
                                                                       + "(Lnet/minecraft/entity/Entity;DDDFZZ)Lnet/minecraft/world/Explosion;"))
    @Nullable
    public net.minecraft.world.Explosion bridge$CreateAndProcessExplosionEvent(
        final net.minecraft.world.World worldObj, final Entity self, final double x,
        final double y, final double z, final float strength, final boolean flaming,
        final boolean smoking) {
        final boolean griefer = ((GrieferBridge) this).bridge$CanGrief();
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(this);
            frame.addContext(EventContextKeys.THROWER, ((WitherSkull) this).getShooter()); // TODO - Remove in API 8/1.13
            frame.addContext(EventContextKeys.PROJECTILE_SOURCE, ((WitherSkull) this).getShooter());
            frame.pushCause(((WitherSkull) this).getShooter());
            return SpongeCommonEventFactory.detonateExplosive(this, Explosion.builder()
                .location(new Location<>((World) worldObj, new Vector3d(x, y, z)))
                .sourceExplosive(((WitherSkull) this))
                .radius(this.explosionRadius)
                .canCauseFire(flaming)
                .shouldPlaySmoke(smoking && griefer)
                .shouldBreakBlocks(smoking && griefer))
                .orElse(null);
        }
    }

}
