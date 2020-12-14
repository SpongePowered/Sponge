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
import net.minecraft.item.ItemStack;
import net.minecraft.world.Explosion.Mode;
import org.spongepowered.api.entity.living.monster.Creeper;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.api.world.server.ServerWorld;
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

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

@Mixin(CreeperEntity.class)
public abstract class CreeperEntityMixin extends MonsterEntityMixin implements FusedExplosiveBridge, ExplosiveBridge {

    // @formatter:off
    @Shadow private int swell;
    @Shadow private int maxSwell;
    @Shadow private int explosionRadius;

    @Shadow public abstract void shadow$ignite();
    @Shadow public abstract int shadow$getSwellDir();
    @Shadow public abstract void shadow$setSwellDir(int state);
    // @formatter:on

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
        return this.maxSwell - this.swell;
    }

    @Override
    public void bridge$setFuseTicksRemaining(final int fuseTicks) {
        // Note: The creeper will detonate when timeSinceIgnited >= fuseTime
        // assuming it is within range of a player. Every tick that the creeper
        // is not within a range of a player, timeSinceIgnited is decremented
        // by one until zero.
        this.swell = 0;
        this.maxSwell = fuseTicks;
    }

    @Inject(method = "setSwellDir", at = @At("INVOKE"), cancellable = true)
    private void onStateChange(final int state, final CallbackInfo ci) {
        this.bridge$setFuseDuration(this.impl$fuseDuration);
        if (this.level.isClientSide) {
            return;
        }

        final boolean isPrimed = this.shadow$getSwellDir() == Constants.Entity.Creeper.STATE_PRIMED;

        if (!isPrimed && state == Constants.Entity.Creeper.STATE_PRIMED && !this.bridge$shouldPrime()) {
            ci.cancel();
        } else if (isPrimed && state == Constants.Entity.Creeper.STATE_IDLE && !this.bridge$shouldDefuse()) {
            ci.cancel();
        } else if (this.shadow$getSwellDir() != state) {
            this.impl$stateDirty = true;
        }
    }

    @Inject(method = "setSwellDir", at = @At("RETURN"))
    private void postStateChange(final int state, final CallbackInfo ci) {
        if (this.level.isClientSide) {
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

    @Redirect(method = "explodeCreeper", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;explode(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/world/Explosion$Mode;)Lnet/minecraft/world/Explosion;"))
    @Nullable
    private net.minecraft.world.Explosion impl$useSpongeExplosion(final net.minecraft.world.World world, final Entity self, final double x,
        final double y, final double z, final float strength, final Mode mode) {
        return SpongeCommonEventFactory.detonateExplosive(this, Explosion.builder()
                .location(ServerLocation.of((ServerWorld) world, x, y, z))
                .sourceExplosive(((Creeper) this))
                .radius(strength)
                .shouldPlaySmoke(mode != Mode.NONE)
                .shouldBreakBlocks(mode != Mode.NONE && ((GrieferBridge) this).bridge$canGrief()))
                .orElseGet(() -> {
                    this.impl$detonationCancelled = true;
                    return null;
                });
    }

    @Inject(method = "explodeCreeper", at = @At("RETURN"))
    private void postExplode(final CallbackInfo ci) {
        if (this.impl$detonationCancelled) {
            this.impl$detonationCancelled = this.dead = false;
        }
    }

    @Redirect(method = "mobInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/monster/CreeperEntity;ignite()V"))
    private void impl$onProcessIgnition(final CreeperEntity self) {
        this.impl$interactPrimeCancelled = !this.bridge$shouldPrime();
        if (!this.impl$interactPrimeCancelled) {
            this.shadow$ignite();
        }
    }

    @Redirect(method = "mobInteract",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;hurtAndBreak(ILnet/minecraft/entity/LivingEntity;Ljava/util/function/Consumer;)V"
        )
    )
    private void impl$onDamageFlintAndSteel(ItemStack fas, int amount, LivingEntity player, Consumer<LivingEntity> onBroken) {
        if (!this.impl$interactPrimeCancelled) {
            fas.hurtAndBreak(amount, player, onBroken);
        }
        this.impl$interactPrimeCancelled = false;
    }

}
