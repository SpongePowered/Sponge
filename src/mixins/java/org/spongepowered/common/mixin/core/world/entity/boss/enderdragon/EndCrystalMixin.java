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
package org.spongepowered.common.mixin.core.world.entity.boss.enderdragon;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.explosives.ExplosiveBridge;
import org.spongepowered.common.bridge.world.entity.boss.enderdragon.EndCrystalBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.mixin.core.world.entity.EntityMixin;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.DamageEventUtil;

import java.util.Optional;

@Mixin(EndCrystal.class)
public abstract class EndCrystalMixin extends EntityMixin implements ExplosiveBridge, EndCrystalBridge {

    private int impl$explosionStrength = Constants.Entity.EnderCrystal.DEFAULT_EXPLOSION_STRENGTH;

    // Explosive Impl

    @Override
    public Optional<Integer> bridge$getExplosionRadius() {
        return Optional.of(this.impl$explosionStrength);
    }

    @Override
    public void bridge$setExplosionRadius(@Nullable final Integer radius) {
        this.impl$explosionStrength = radius == null ? Constants.Entity.EnderCrystal.DEFAULT_EXPLOSION_STRENGTH : radius;
    }

    @Redirect(method = "hurt",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;)Lnet/minecraft/world/level/Explosion;"
        )
    )
    private net.minecraft.world.level.@Nullable Explosion impl$throwEventWithEntity(final net.minecraft.world.level.Level world,
        final Entity entityIn, final DamageSource damageSource, final ExplosionDamageCalculator exDamageCalc, final double xIn, final double yIn, final double zIn, final float explosionRadius,
            final boolean fire, final Level.ExplosionInteraction modeIn) {
        // TODO fire?
        // TODO smoking value correct?
        return this.bridge$throwExplosionEventAndExplode(world, entityIn, xIn, yIn, zIn, modeIn.compareTo(Level.ExplosionInteraction.TNT) <= 0, damageSource);
    }

    @Override
    public net.minecraft.world.level.@Nullable Explosion bridge$throwExplosionEventAndExplode(final net.minecraft.world.level.Level world,
        @Nullable final Entity nil, final double x, final double y, final double z, final boolean smoking,
        @Nullable final DamageSource source) {
        final CauseStackManager causeStackManager = PhaseTracker.getCauseStackManager();
        try (final CauseStackManager.StackFrame frame = causeStackManager.pushCauseFrame()) {
            frame.pushCause(this);
            if (source != null) {
                frame.pushCause(source);
            }
            return SpongeCommonEventFactory.detonateExplosive(this, Explosion.builder()
                .location(ServerLocation.of((ServerWorld) world, x, y, z))
                .radius(this.impl$explosionStrength)
                .shouldPlaySmoke(smoking))
                .orElse(null);
        }
    }

    @Inject(method = "hurt", cancellable = true, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/boss/enderdragon/EndCrystal;remove(Lnet/minecraft/world/entity/Entity$RemovalReason;)V"))
    private void attackImpl$onAttackEntityFrom(final DamageSource source, final float amount, final CallbackInfoReturnable<Boolean> cir) {
        if (DamageEventUtil.callOtherAttackEvent((Entity) (Object) this, source, amount).isCancelled()) {
            cir.setReturnValue(true);
        }
    }

}
