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
package org.spongepowered.common.mixin.core.world.entity.vehicle;

import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.vehicle.minecart.TNTMinecart;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.AttackEntityEvent;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.explosives.ExplosiveBridge;
import org.spongepowered.common.bridge.explosives.FusedExplosiveBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.util.Constants;

import javax.annotation.Nullable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import java.util.ArrayList;
import java.util.Optional;

@Mixin(MinecartTNT.class)
public abstract class MinecartTNTMixin extends AbstractMinecartMixin implements FusedExplosiveBridge, ExplosiveBridge {

    // @formatter:off
    @Shadow private int fuse;
    // @formatter:on

    @Nullable private Integer impl$explosionRadius = null;
    private int impl$fuseDuration = Constants.Entity.Minecart.DEFAULT_FUSE_DURATION;
    private boolean impl$detonationCancelled;
    @Nullable private Object impl$primeCause;

    @Override
    public Optional<Integer> bridge$getExplosionRadius() {
        return Optional.ofNullable(this.impl$explosionRadius);
    }

    @Override
    public void bridge$setExplosionRadius(final @Nullable
        Integer radius) {
        this.impl$explosionRadius = radius;
    }

    @Override
    public int bridge$getFuseDuration() {
        return this.impl$fuseDuration;
    }

    @Override
    public void bridge$setFuseDuration(final int fuseTicks) {
        this.impl$fuseDuration = fuseTicks;
    }

    @Override
    public int bridge$getFuseTicksRemaining() {
        return this.fuse;
    }

    @Override
    public void bridge$setFuseTicksRemaining(final int fuseTicks) {
        this.fuse = fuseTicks;
    }


    @Inject(method = "hurt",
        at = @At("INVOKE"))
    private void impl$onAttackSetPrimeCause(final DamageSource damageSource, final float amount, final CallbackInfoReturnable<Boolean> ci) {
        this.impl$primeCause = damageSource;
    }

    @Inject(method = "activateMinecart(IIIZ)V",
        at = @At("INVOKE"))
    private void impl$onActivateSetPrimeCauseNotifier(final int x, final int y, final int z, final boolean receivingPower, final CallbackInfo ci) {
        if (((WorldBridge) this.level).bridge$isFake()) {
            return;
        }
        if (receivingPower) {
            ((ServerWorld) this.level).get(x, y, z, Keys.NOTIFIER).ifPresent(notifier -> this.impl$primeCause = notifier);
        }
    }

    @Inject(method = "primeFuse",
        at = @At("INVOKE"),
        cancellable = true)
    private void impl$preIgnite(final CallbackInfo ci) {
        if (!this.bridge$shouldPrime()) {
            this.bridge$setFuseTicksRemaining(-1);
            ci.cancel();
        }
    }

    @Inject(method = "primeFuse",
        at = @At("RETURN"))
    private void impl$postSpongeIgnite(final CallbackInfo ci) {
        this.bridge$setFuseTicksRemaining(this.impl$fuseDuration);
        if (this.impl$primeCause != null) {
            PhaseTracker.getCauseStackManager().pushCause(this.impl$primeCause);
        }
        this.bridge$postPrime();
        if (this.impl$primeCause != null) {
            PhaseTracker.getCauseStackManager().popCause();
        }
    }

    @Redirect(
        method = "explode",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;DDDFLnet/minecraft/world/level/Explosion$BlockInteraction;)Lnet/minecraft/world/level/Explosion;"
        )
    )
    @Nullable
    private net.minecraft.world.level.Explosion impl$useSpongeExplosion(final net.minecraft.world.level.Level world, final Entity entityIn,
        final double xIn, final double yIn, final double zIn, final float explosionRadius, final net.minecraft.world.level.Explosion.BlockInteraction modeIn) {
        return SpongeCommonEventFactory.detonateExplosive(this, Explosion.builder()
                .location(ServerLocation.of((ServerWorld) world, xIn, yIn, zIn))
                .sourceExplosive((TNTMinecart) this)
                .radius(this.impl$explosionRadius != null ? this.impl$explosionRadius : explosionRadius)
                .shouldPlaySmoke(modeIn.ordinal() > net.minecraft.world.level.Explosion.BlockInteraction.NONE.ordinal())
                .shouldBreakBlocks(modeIn.ordinal() > net.minecraft.world.level.Explosion.BlockInteraction.NONE.ordinal()))
                .orElseGet(() -> {
                            this.impl$detonationCancelled = true;
                            return null;
                        }
                );
    }

    @Inject(method = "explode",
        at = @At("RETURN"))
    private void impL$postExplode(final CallbackInfo ci) {
        if (this.impl$detonationCancelled) {
            this.impl$detonationCancelled = this.removed = false;
        }
    }

    @Inject(method = "hurt",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/vehicle/MinecartTNT;explode(D)V"), cancellable = true)
    private void impl$postOnAttackEntityFrom(final DamageSource source, final float amount, final CallbackInfoReturnable<Boolean> cir) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(source);
            final AttackEntityEvent event = SpongeEventFactory.createAttackEntityEvent(frame.currentCause(),
                (TNTMinecart) this, new ArrayList<>(), 0, amount);
            SpongeCommon.post(event);
            if (event.isCancelled()) {
                cir.setReturnValue(true);
            }
        }
    }

}
