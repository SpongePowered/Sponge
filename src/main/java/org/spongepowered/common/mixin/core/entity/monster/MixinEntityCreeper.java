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
package org.spongepowered.common.mixin.core.entity.monster;

import static com.google.common.base.Preconditions.checkState;

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.entity.living.monster.Creeper;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.entity.IMixinGriefer;
import org.spongepowered.common.interfaces.entity.explosive.IMixinFusedExplosive;
import org.spongepowered.common.mixin.api.minecraft.entity.monster.MixinEntityMob_API;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(EntityCreeper.class)
public abstract class MixinEntityCreeper extends MixinEntityMob_API implements Creeper, IMixinFusedExplosive {

    private static final String TARGET_NEW_EXPLOSION = "Lnet/minecraft/world/World;createExplosion"
            + "(Lnet/minecraft/entity/Entity;DDDFZ)Lnet/minecraft/world/Explosion;";
    private static final String TARGET_IGNITE = "Lnet/minecraft/entity/monster/EntityCreeper;ignite()V";
    private static final String TARGET_DAMAGE_ITEM = "Lnet/minecraft/item/ItemStack;damageItem"
            + "(ILnet/minecraft/entity/EntityLivingBase;)V";

    private static final int DEFAULT_EXPLOSION_RADIUS = 3;
    private static final int STATE_IDLE = -1;
    private static final int STATE_PRIMED = 1;

    @Shadow private int timeSinceIgnited;
    @Shadow private int fuseTime;
    @Shadow private int explosionRadius;

    @Shadow public abstract void ignite();
    @Shadow public abstract int getCreeperState();
    @Shadow public abstract void setCreeperState(int state);
    @Shadow private void explode() { } // explode

    private int fuseDuration = 30;
    private boolean interactPrimeCancelled;
    private boolean stateDirty;
    private boolean detonationCancelled;

    // FusedExplosive Impl

    @Override
    public Optional<Integer> getExplosionRadius() {
        return Optional.of(this.explosionRadius);
    }

    @Override
    public void setExplosionRadius(Optional<Integer> radius) {
        this.explosionRadius = radius.orElse(DEFAULT_EXPLOSION_RADIUS);
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
        return this.fuseTime - this.timeSinceIgnited;
    }

    @Override
    public void setFuseTicksRemaining(int fuseTicks) {
        // Note: The creeper will detonate when timeSinceIgnited >= fuseTime
        // assuming it is within range of a player. Every tick that the creeper
        // is not within a range of a player, timeSinceIgnited is decremented
        // by one until zero.
        this.timeSinceIgnited = 0;
        this.fuseTime = fuseTicks;
    }

    @Override
    public void prime() {
        checkState(!isPrimed(), "already primed");
        setCreeperState(STATE_PRIMED);
    }

    @Override
    public void defuse() {
        checkState(isPrimed(), "not primed");
        setCreeperState(STATE_IDLE);
    }

    @Override
    public boolean isPrimed() {
        return getCreeperState() == STATE_PRIMED;
    }

    @Override
    public void detonate() {
        this.explode();
    }

    @Inject(method = "setCreeperState(I)V", at = @At("INVOKE"), cancellable = true)
    private void onStateChange(int state, CallbackInfo ci) {
        setFuseDuration(this.fuseDuration);
        if (this.world.isRemote) {
            return;
        }

        if (!isPrimed() && state == STATE_PRIMED && !shouldPrime()) {
            ci.cancel();
        } else if (isPrimed() && state == STATE_IDLE && !shouldDefuse()) {
            ci.cancel();
        } else if (getCreeperState() != state) {
            this.stateDirty = true;
        }
    }

    @Inject(method = "setCreeperState(I)V", at = @At("RETURN"))
    private void postStateChange(int state, CallbackInfo ci) {
        if (this.world.isRemote) {
            return;
        }

        if (this.stateDirty) {
            if (state == STATE_PRIMED) {
                postPrime();
            } else if (state == STATE_IDLE) {
                postDefuse();
            }
            this.stateDirty = false;
        }
    }

    @Redirect(method = "explode", at = @At(value = "INVOKE", target = TARGET_NEW_EXPLOSION))
    @Nullable
    private net.minecraft.world.Explosion onExplode(net.minecraft.world.World world, Entity self, double x,
        double y, double z, float strength, boolean smoking) {
        return detonate(Explosion.builder()
                .location(new Location<>((World) world, new Vector3d(x, y, z)))
                .sourceExplosive(this)
                .radius(strength)
                .shouldPlaySmoke(smoking)
                .shouldBreakBlocks(smoking && ((IMixinGriefer) this).canGrief()))
                .orElseGet(() -> {
                    this.detonationCancelled = true;
                    return null;
                });
    }

    @Inject(method = "explode", at = @At("RETURN"))
    private void postExplode(CallbackInfo ci) {
        if (this.detonationCancelled) {
            this.detonationCancelled = this.isDead = false;
        }
    }

    @Redirect(method = "processInteract", at = @At(value = "INVOKE", target = TARGET_IGNITE))
    private void onInteractIgnite(EntityCreeper self) {
        this.interactPrimeCancelled = !shouldPrime();
        if (!this.interactPrimeCancelled) {
            ignite();
        }
    }

    @Redirect(method = "processInteract", at = @At(value = "INVOKE", target = TARGET_DAMAGE_ITEM))
    private void onDamageFlintAndSteel(ItemStack fas, int amount, EntityLivingBase player) {
        if (!this.interactPrimeCancelled) {
            fas.damageItem(amount, player);
            // TODO put this in the cause somehow?
//            this.primeCause = Cause.of(NamedCause.of(NamedCause.IGNITER, player));
//            this.detonationCause = this.primeCause;
        }
        this.interactPrimeCancelled = false;
    }

}
