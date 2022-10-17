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
package org.spongepowered.common.mixin.core.world.entity.projectile;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion.BlockInteraction;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.entity.projectile.explosive.WitherSkull;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.accessor.world.entity.projectile.ProjectileAccessor;
import org.spongepowered.common.bridge.explosives.ExplosiveBridge;
import org.spongepowered.common.bridge.world.entity.GrieferBridge;
import org.spongepowered.common.bridge.world.entity.projectile.WitherSkullBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(net.minecraft.world.entity.projectile.WitherSkull.class)
public abstract class WitherSkullMixin extends AbstractHurtingProjectileMixin implements WitherSkullBridge, ExplosiveBridge {

    private int impl$explosionRadius = Constants.Entity.WitherSkull.DEFAULT_EXPLOSION_RADIUS;

    // TODO Key not implemented
    private float impl$damage = 0.0f;
    private boolean impl$damageSet = false;

    @ModifyArg(method = "onHitEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private float impl$onAttackEntityFrom(final float amount) {
        if (this.impl$damageSet) {
            return this.impl$damage;
        }
        if (((ProjectileAccessor) this).accessor$ownerUUID() != null) {
            return Constants.Entity.WitherSkull.DEFAULT_WITHER_CREATED_SKULL_DAMAGE;
        }
        return Constants.Entity.WitherSkull.DEFAULT_NO_SOURCE_SKULL_DAMAGE;
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
    @Redirect(method = "onHit", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;DDDFZLnet/minecraft/world/level/Explosion$BlockInteraction;)Lnet/minecraft/world/level/Explosion;"))
    public net.minecraft.world.level.Explosion impl$CreateAndProcessExplosionEvent(final net.minecraft.world.level.Level worldObj, final Entity self,
            final double x, final double y, final double z, final float strength, final boolean flaming, final BlockInteraction mode) {
        return this.bridge$throwExplosionEventAndExplosde(worldObj, self, x, y, z, strength, flaming, mode);
    }

    @Override
    public net.minecraft.world.level.Explosion bridge$throwExplosionEventAndExplosde(
            final net.minecraft.world.level.Level worldObj, final Entity self,
            final double x, final double y, final double z, final float strength, final boolean flaming, final net.minecraft.world.level.Explosion.BlockInteraction mode) {
        final boolean griefer = ((GrieferBridge) this).bridge$canGrief();
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(this);
            ((Projectile) this).get(Keys.SHOOTER).ifPresent(shooter -> {
                frame.addContext(EventContextKeys.PROJECTILE_SOURCE, shooter);
                frame.pushCause(shooter);
            });
            return SpongeCommonEventFactory.detonateExplosive(this, Explosion.builder()
                    .location(ServerLocation.of((ServerWorld) worldObj, x, y, z))
                    .sourceExplosive(((WitherSkull) this))
                    .radius(this.impl$explosionRadius)
                    .canCauseFire(flaming)
                    .shouldPlaySmoke(mode != BlockInteraction.NONE && griefer)
                    .shouldBreakBlocks(mode != BlockInteraction.NONE && griefer))
                    .orElse(null);
        }
    }
}
