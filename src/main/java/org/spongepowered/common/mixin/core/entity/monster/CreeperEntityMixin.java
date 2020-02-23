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

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
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
import org.spongepowered.common.bridge.explosives.ExplosiveBridge;
import org.spongepowered.common.bridge.explosives.FusedExplosiveBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.util.Constants;
import org.spongepowered.math.vector.Vector3d;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nullable;

@Mixin(CreeperEntity.class)
public abstract class CreeperEntityMixin extends MonsterEntityMixin implements FusedExplosiveBridge, ExplosiveBridge {

    @Shadow private int timeSinceIgnited;
    @Shadow private int fuseTime;
    @Shadow private int explosionRadius;

    @Shadow public abstract void shadow$ignite();
    @Shadow public abstract int shadow$getCreeperState();
    @Shadow public abstract void shadow$setCreeperState(int state);
    @Shadow private void shadow$explode() { } // explode

    private int impl$fuseDuration = Constants.Entity.Creeper.FUSE_DURATION;
    private boolean impl$interactPrimeCancelled;
    private boolean impl$stateDirty;
    private boolean impl$detonationCancelled;

    // FusedExplosive Impl

    @Override
    public Optional<Integer> bridge$getExplosionRadius() {
        return Optional.of(this.explosionRadius);
    }

    @Override
    public void bridge$setExplosionRadius(@Nullable final Integer radius) {
        this.explosionRadius = radius == null ? Constants.Entity.Creeper.DEFAULT_EXPLOSION_RADIUS : radius;
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
        return this.fuseTime - this.timeSinceIgnited;
    }

    @Override
    public void bridge$setFuseTicksRemaining(final int fuseTicks) {
        // Note: The creeper will detonate when timeSinceIgnited >= fuseTime
        // assuming it is within range of a player. Every tick that the creeper
        // is not within a range of a player, timeSinceIgnited is decremented
        // by one until zero.
        this.timeSinceIgnited = 0;
        this.fuseTime = fuseTicks;
    }


    @Inject(method = "setCreeperState(I)V", at = @At("INVOKE"), cancellable = true)
    private void onStateChange(final int state, final CallbackInfo ci) {
        this.bridge$setFuseDuration(this.impl$fuseDuration);
        if (this.world.isRemote) {
            return;
        }

        if (!((Creeper) this).isPrimed() && state == Constants.Entity.Creeper.STATE_PRIMED && !this.bridge$shouldPrime()) {
            ci.cancel();
        } else if (((Creeper) this).isPrimed() && state == Constants.Entity.Creeper.STATE_IDLE && !this.bridge$shouldDefuse()) {
            ci.cancel();
        } else if (this.shadow$getCreeperState() != state) {
            this.impl$stateDirty = true;
        }
    }

    @Inject(method = "setCreeperState(I)V", at = @At("RETURN"))
    private void postStateChange(final int state, final CallbackInfo ci) {
        if (this.world.isRemote) {
            return;
        }

        if (this.impl$stateDirty) {
            if (state == Constants.Entity.Creeper.STATE_PRIMED) {
                this.bridge$postPrime();
            } else if (state == Constants.Entity.Creeper.STATE_IDLE) {
                this.bridge$postDefuse();
            }
            this.impl$stateDirty = false;
        }
    }

    @Redirect(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;createExplosion"
                                                                      + "(Lnet/minecraft/entity/Entity;DDDFZ)Lnet/minecraft/world/Explosion;"))
    @Nullable
    private net.minecraft.world.Explosion onExplode(final net.minecraft.world.World world, final Entity self, final double x,
        final double y, final double z, final float strength, final boolean smoking) {
        return SpongeCommonEventFactory.detonateExplosive(this, Explosion.builder()
                .location(Location.of((World) world, new Vector3d(x, y, z)))
                .sourceExplosive(((Creeper) this))
                .radius(strength)
                .shouldPlaySmoke(smoking)
                .shouldBreakBlocks(smoking && ((GrieferBridge) this).bridge$canGrief()))
                .orElseGet(() -> {
                    this.impl$detonationCancelled = true;
                    return null;
                });
    }

    @Inject(method = "explode", at = @At("RETURN"))
    private void postExplode(final CallbackInfo ci) {
        if (this.impl$detonationCancelled) {
            this.impl$detonationCancelled = this.isDead = false;
        }
    }

    @Redirect(method = "processInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/monster/CreeperEntity;ignite()V"))
    private void impl$onProcessIgnition(final CreeperEntity self) {
        this.impl$interactPrimeCancelled = !this.bridge$shouldPrime();
        if (!this.impl$interactPrimeCancelled) {
            this.shadow$ignite();
        }
    }

    @Redirect(method = "processInteract",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;damageItem(ILnet/minecraft/entity/LivingEntity;Ljava/util/function/Consumer;)V"
        )
    )
    private void impl$onDamageFlintAndSteel(ItemStack fas, int amount, LivingEntity player, Consumer<LivingEntity> onBroken) {
        if (!this.impl$interactPrimeCancelled) {
            fas.damageItem(amount, player, onBroken);
            // TODO put this in the cause somehow?
//            this.primeCause = Cause.of(NamedCause.of(NamedCause.IGNITER, player));
//            this.detonationCause = this.primeCause;
        }
        this.impl$interactPrimeCancelled = false;
    }

}
