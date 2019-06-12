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
import org.spongepowered.common.bridge.entity.GrieferBridge;
import org.spongepowered.common.bridge.explosives.FusedExplosiveBridge;
import org.spongepowered.common.data.util.DataConstants;
import org.spongepowered.common.mixin.api.minecraft.entity.monster.MixinEntityMob_API;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(EntityCreeper.class)
public abstract class MixinEntityCreeper extends MixinEntityMob implements FusedExplosiveBridge {

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
        this.explosionRadius = radius.orElse(DataConstants.Entity.Creeper.DEFAULT_EXPLOSION_RADIUS);
    }

    @Override
    public int bridge$getFuseDuration() {
        return this.fuseDuration;
    }

    @Override
    public void bridge$setFuseDuration(int fuseTicks) {
        this.fuseDuration = fuseTicks;
    }

    @Override
    public int bridge$getFuseTicksRemaining() {
        return this.fuseTime - this.timeSinceIgnited;
    }

    @Override
    public void bridge$setFuseTicksRemaining(int fuseTicks) {
        // Note: The creeper will detonate when timeSinceIgnited >= fuseTime
        // assuming it is within range of a player. Every tick that the creeper
        // is not within a range of a player, timeSinceIgnited is decremented
        // by one until zero.
        this.timeSinceIgnited = 0;
        this.fuseTime = fuseTicks;
    }


    @Inject(method = "setCreeperState(I)V", at = @At("INVOKE"), cancellable = true)
    private void onStateChange(int state, CallbackInfo ci) {
        bridge$setFuseDuration(this.fuseDuration);
        if (this.world.isRemote) {
            return;
        }

        if (!((Creeper) this).isPrimed() && state == DataConstants.Entity.Creeper.STATE_PRIMED && !bridge$shouldPrime()) {
            ci.cancel();
        } else if (((Creeper) this).isPrimed() && state == DataConstants.Entity.Creeper.STATE_IDLE && !bridge$shouldDefuse()) {
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
            if (state == DataConstants.Entity.Creeper.STATE_PRIMED) {
                bridge$postPrime();
            } else if (state == DataConstants.Entity.Creeper.STATE_IDLE) {
                bridge$postDefuse();
            }
            this.stateDirty = false;
        }
    }

    @Redirect(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;createExplosion"
                                                                      + "(Lnet/minecraft/entity/Entity;DDDFZ)Lnet/minecraft/world/Explosion;"))
    @Nullable
    private net.minecraft.world.Explosion onExplode(net.minecraft.world.World world, Entity self, double x,
        double y, double z, float strength, boolean smoking) {
        return detonate(Explosion.builder()
                .location(new Location<>((World) world, new Vector3d(x, y, z)))
                .sourceExplosive(((Creeper) this))
                .radius(strength)
                .shouldPlaySmoke(smoking)
                .shouldBreakBlocks(smoking && ((GrieferBridge) this).bridge$CanGrief()))
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

    @Redirect(method = "processInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/monster/EntityCreeper;ignite()V"))
    private void onInteractIgnite(EntityCreeper self) {
        this.interactPrimeCancelled = !bridge$shouldPrime();
        if (!this.interactPrimeCancelled) {
            ignite();
        }
    }

    @Redirect(method = "processInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;damageItem"
                                                                              + "(ILnet/minecraft/entity/EntityLivingBase;)V"))
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
