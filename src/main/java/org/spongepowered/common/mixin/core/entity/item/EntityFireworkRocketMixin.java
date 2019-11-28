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
package org.spongepowered.common.mixin.core.entity.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.FireworkRocketEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.projectile.Firework;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.explosives.ExplosiveBridge;
import org.spongepowered.common.bridge.explosives.FusedExplosiveBridge;
import org.spongepowered.common.bridge.util.DamageSourceBridge;
import org.spongepowered.common.entity.projectile.ProjectileSourceSerializer;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.mixin.core.entity.EntityMixin;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(FireworkRocketEntity.class)
public abstract class EntityFireworkRocketMixin extends EntityMixin implements FusedExplosiveBridge, ExplosiveBridge {

    @Shadow private int fireworkAge;
    @Shadow private int lifetime;

    @Shadow public abstract void onUpdate();

    private ProjectileSource impl$projectileSource = ProjectileSource.UNKNOWN;
    private int impl$explosionRadius = Constants.Entity.Firework.DEFAULT_EXPLOSION_RADIUS;


    @Override
    public void spongeImpl$readFromSpongeCompound(final CompoundNBT compound) {
        super.spongeImpl$readFromSpongeCompound(compound);
        ProjectileSourceSerializer.readSourceFromNbt(compound, (Firework) this);
    }

    @Override
    public void spongeImpl$writeToSpongeCompound(final CompoundNBT compound) {
        super.spongeImpl$writeToSpongeCompound(compound);
        ProjectileSourceSerializer.writeSourceToNbt(compound, this.impl$projectileSource, null);
    }

    @Override
    public int bridge$getFuseDuration() {
        return this.lifetime;
    }

    @Override
    public void bridge$setFuseDuration(final int fuseTicks) {
        this.lifetime = fuseTicks;
    }

    @Override
    public int bridge$getFuseTicksRemaining() {
        return this.lifetime - this.fireworkAge;
    }

    @Override
    public void bridge$setFuseTicksRemaining(final int fuseTicks) {
        this.fireworkAge = 0;
        this.lifetime = fuseTicks;
    }

    @Override
    public Optional<Integer> bridge$getExplosionRadius() {
        return Optional.of(this.impl$explosionRadius);
    }

    @Override
    public void bridge$setExplosionRadius(final @Nullable Integer radius) {
        this.impl$explosionRadius = radius == null ? Constants.Entity.Firework.DEFAULT_EXPLOSION_RADIUS : radius;
    }

    @SuppressWarnings("deprecation")
    @Redirect(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setEntityState"
                                                                       + "(Lnet/minecraft/entity/Entity;B)V"))
    private void onExplode(final World world, final Entity self, final byte state) {
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            // Fireworks don't typically explode like other explosives, but we'll
            // post an event regardless and if the radius is zero the explosion
            // won't be triggered (the default behavior).
            frame.pushCause(this);
            frame.addContext(EventContextKeys.THROWER, ((Firework) this).getShooter()); // TODO - Remove in 1.13/API 8
            frame.addContext(EventContextKeys.PROJECTILE_SOURCE, ((Firework) this).getShooter());
            SpongeCommonEventFactory.detonateExplosive(this, Explosion.builder()
                .sourceExplosive(((Firework) this))
                .location(((Firework) this).getLocation())
                .radius(this.impl$explosionRadius))
                .ifPresent(explosion -> world.setEntityState(self, state));
        }
    }

    @SuppressWarnings("deprecation")
    @Inject(method = "onUpdate", at = @At("RETURN"))
    private void onUpdate(final CallbackInfo ci) {
        if (this.fireworkAge == 1 && !this.world.isRemote) {
            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(this);
                frame.addContext(EventContextKeys.THROWER, ((Firework) this).getShooter()); // TODO - Remove in 1.13/API 8
                frame.addContext(EventContextKeys.PROJECTILE_SOURCE, ((Firework) this).getShooter());
                bridge$postPrime();
            }
        }
    }

    @Redirect(method = "dealExplosionDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;attackEntityFrom"
            + "(Lnet/minecraft/util/DamageSource;F)Z"))
    private boolean useEntitySource(final LivingEntity entityLivingBase, final DamageSource source, final float amount) {
        try {
            final DamageSource fireworks = new EntityDamageSource(DamageSource.FIREWORKS.damageType, (Entity) (Object) this).setExplosion();
            ((DamageSourceBridge) fireworks).bridge$setFireworksSource();
            return entityLivingBase.attackEntityFrom(DamageSource.FIREWORKS, amount);
        } finally {
            ((DamageSourceBridge) source).bridge$setFireworksSource();
        }
    }
}
