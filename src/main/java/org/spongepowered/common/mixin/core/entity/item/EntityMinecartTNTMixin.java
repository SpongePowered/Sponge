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

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.minecart.TNTMinecartEntity;
import net.minecraft.util.DamageSource;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.vehicle.minecart.TNTMinecart;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.AttackEntityEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.explosives.ExplosiveBridge;
import org.spongepowered.common.bridge.explosives.FusedExplosiveBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.util.Constants;

import java.util.ArrayList;
import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(TNTMinecartEntity.class)
public abstract class EntityMinecartTNTMixin extends EntityMinecartMixin implements FusedExplosiveBridge, ExplosiveBridge {

    @Shadow private int minecartTNTFuse;

    @Shadow public abstract void ignite();

    @Nullable private Integer impl$explosionRadius = null;
    private int impl$fuseDuration = Constants.Entity.Minecart.DEFAULT_FUSE_DURATION;
    private boolean impl$detonationCancelled;
    @Nullable private Object impl$primeCause;

    @Override
    public Optional<Integer> bridge$getExplosionRadius() {
        return Optional.ofNullable(this.impl$explosionRadius);
    }

    @Override
    public void bridge$setExplosionRadius(final @Nullable Integer radius) {
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
        return this.minecartTNTFuse;
    }

    @Override
    public void bridge$setFuseTicksRemaining(final int fuseTicks) {
        this.minecartTNTFuse = fuseTicks;
    }


    @Inject(method = "attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z", at = @At("INVOKE"))
    private void onAttack(final DamageSource damageSource, final float amount, final CallbackInfoReturnable<Boolean> ci) {
        this.impl$primeCause = damageSource;
    }

    @Inject(method = "onActivatorRailPass(IIIZ)V", at = @At("INVOKE"))
    private void onActivate(final int x, final int y, final int z, final boolean receivingPower, final CallbackInfo ci) {
        if (((WorldBridge) this.world).bridge$isFake()) {
            return;
        }
        if (receivingPower) {
            ((World) this.world).getNotifier(x, y, z).ifPresent(notifier -> this.impl$primeCause = notifier);
        }
    }

    @Inject(method = "ignite", at = @At("INVOKE"), cancellable = true)
    private void preIgnite(final CallbackInfo ci) {
        if (!bridge$shouldPrime()) {
            bridge$setFuseTicksRemaining(-1);
            ci.cancel();
        }
    }

    @Inject(method = "ignite", at = @At("RETURN"))
    private void postSpongeIgnite(final CallbackInfo ci) {
        bridge$setFuseTicksRemaining(this.impl$fuseDuration);
        if (this.impl$primeCause != null) {
            Sponge.getCauseStackManager().pushCause(this.impl$primeCause);
        }
        bridge$postPrime();
        if (this.impl$primeCause != null) {
            Sponge.getCauseStackManager().popCause();
        }
    }

    @Redirect(
        method = "explodeCart",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;createExplosion(Lnet/minecraft/entity/Entity;DDDFZ)Lnet/minecraft/world/Explosion;"
        )
    )
    @Nullable
    private net.minecraft.world.Explosion onSpongeExplode(final net.minecraft.world.World worldObj, final Entity self, final double x, final double y, final double z,
        final float strength, final boolean smoking) {
        return SpongeCommonEventFactory.detonateExplosive(this, Explosion.builder().location(new Location<>((World) worldObj, new Vector3d(x, y, z))).sourceExplosive((TNTMinecart) this)
                .radius(this.impl$explosionRadius != null ? this.impl$explosionRadius : strength).shouldPlaySmoke(smoking).shouldBreakBlocks(smoking))
                        .orElseGet(() -> {
                            this.impl$detonationCancelled = true;
                            return null;
                        });
    }

    @Inject(method = "explodeCart", at = @At("RETURN"))
    private void postExplode(final CallbackInfo ci) {
        if (this.impl$detonationCancelled) {
            this.impl$detonationCancelled = this.isDead = false;
        }
    }

    @Inject(method = "attackEntityFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityMinecartTNT;explodeCart(D)V"))
    private void onAttackEntityFrom(final DamageSource source, final float amount, final CallbackInfoReturnable<Boolean> cir) {
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(source);
            final AttackEntityEvent event = SpongeEventFactory.createAttackEntityEvent(frame.getCurrentCause(), new ArrayList<>(),
                (TNTMinecart) this, 0, amount);
            SpongeImpl.postEvent(event);
            if (event.isCancelled()) {
                cir.setReturnValue(true);
            }
        }
    }

}
