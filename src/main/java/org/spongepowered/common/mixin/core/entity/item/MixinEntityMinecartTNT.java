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

import static com.google.common.base.Preconditions.checkState;

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecartTNT;
import net.minecraft.util.DamageSource;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.vehicle.minecart.TNTMinecart;
import org.spongepowered.api.event.cause.Cause;
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
import org.spongepowered.common.interfaces.entity.explosive.IMixinFusedExplosive;

import java.util.Optional;

@Mixin(EntityMinecartTNT.class)
public abstract class MixinEntityMinecartTNT extends MixinEntityMinecart implements TNTMinecart, IMixinFusedExplosive {

    private static final String TARGET_NEW_EXPLOSION = "Lnet/minecraft/world/World;createExplosion"
            + "(Lnet/minecraft/entity/Entity;DDDFZ)Lnet/minecraft/world/Explosion;";

    @Shadow
    private int                 minecartTNTFuse;

    @Shadow
    public abstract void ignite();

    private Optional<Integer> explosionRadius = Optional.empty();
    private int               fuseDuration    = 80;
    private boolean           detonationCancelled;
    private Object            primeCause;

    @Override
    public Optional<Integer> getExplosionRadius() {
        return this.explosionRadius;
    }

    @Override
    public void setExplosionRadius(Optional<Integer> radius) {
        this.explosionRadius = radius;
    }

    @Override
    public int getFuseDuration() {
        return this.fuseDuration;
    }

    @Override
    public void setFuseDuration(int fuseTicks) {
        this.fuseDuration = fuseTicks;
    }

    @Override
    public int getFuseTicksRemaining() {
        return this.minecartTNTFuse;
    }

    @Override
    public void setFuseTicksRemaining(int fuseTicks) {
        this.minecartTNTFuse = fuseTicks;
    }

    @Override
    public void prime() {
        checkState(!isPrimed(), "already primed");
        ignite();
    }

    @Override
    public void defuse() {
        checkState(isPrimed(), "not primed");
        if (shouldDefuse()) {
            setFuseTicksRemaining(-1);
            postDefuse();
        }
    }

    @Override
    public boolean isPrimed() {
        return this.minecartTNTFuse >= 0;
    }

    @Override
    public void detonate() {
        setFuseTicksRemaining(0);
    }

    @Inject(method = "attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z", at = @At("INVOKE"))
    protected void onAttack(DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> ci) {
        this.primeCause = damageSource;
    }

    @Inject(method = "onActivatorRailPass(IIIZ)V", at = @At("INVOKE"))
    protected void onActivate(int x, int y, int z, boolean receivingPower, CallbackInfo ci) {
        if (receivingPower) {
            getWorld().getNotifier(x, y, z).ifPresent(notifier -> this.primeCause = notifier);
        }
    }

    @Inject(method = "ignite", at = @At("INVOKE"), cancellable = true)
    protected void preIgnite(CallbackInfo ci) {
        if (!shouldPrime()) {
            setFuseTicksRemaining(-1);
            ci.cancel();
        }
    }

    @Inject(method = "ignite", at = @At("RETURN"))
    protected void postIgnite(CallbackInfo ci) {
        setFuseTicksRemaining(this.fuseDuration);
        if (this.primeCause != null) {
            Sponge.getCauseStackManager().pushCause(this.primeCause);
        }
        postPrime();
        if (this.primeCause != null) {
            Sponge.getCauseStackManager().popCause();
        }
    }

    @Redirect(method = "explodeCart", at = @At(value = "INVOKE", target = TARGET_NEW_EXPLOSION))
    protected net.minecraft.world.Explosion onExplode(net.minecraft.world.World worldObj, Entity self, double x, double y, double z, float strength,
            boolean smoking) {
        return detonate(Explosion.builder().location(new Location<>((World) worldObj, new Vector3d(x, y, z))).sourceExplosive(this)
                .radius(this.explosionRadius.isPresent() ? this.explosionRadius.get() : strength).shouldPlaySmoke(smoking).shouldBreakBlocks(smoking))
                        .orElseGet(() -> {
                            this.detonationCancelled = true;
                            return null;
                        });
    }

    @Inject(method = "explodeCart", at = @At("RETURN"))
    protected void postExplode(CallbackInfo ci) {
        if (this.detonationCancelled) {
            this.detonationCancelled = this.isDead = false;
        }
    }

}
