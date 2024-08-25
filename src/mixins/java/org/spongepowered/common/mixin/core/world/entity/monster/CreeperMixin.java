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
package org.spongepowered.common.mixin.core.world.entity.monster;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.entity.living.monster.Creeper;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.explosives.ExplosiveBridge;
import org.spongepowered.common.bridge.explosives.FusedExplosiveBridge;
import org.spongepowered.common.bridge.world.entity.GrieferBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

@Mixin(net.minecraft.world.entity.monster.Creeper.class)
public abstract class CreeperMixin extends MonsterMixin implements FusedExplosiveBridge, ExplosiveBridge {

    // @formatter:off
    @Shadow private int swell;
    @Shadow private int maxSwell;
    @Shadow private int explosionRadius;

    @Shadow public abstract void shadow$ignite();
    @Shadow public abstract boolean shadow$isIgnited();
    @Shadow public abstract int shadow$getSwellDir();
    // @formatter:on

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
    public boolean bridge$isPrimed() {
        return this.shadow$isIgnited() || this.shadow$getSwellDir() == Constants.Entity.Creeper.STATE_PRIMED;
    }

    @Override
    public int bridge$getFuseDuration() {
        return this.maxSwell;
    }

    @Override
    public void bridge$setFuseDuration(final int fuseTicks) {
        this.maxSwell = fuseTicks;
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

    @Inject(method = "setSwellDir", at = @At("HEAD"), cancellable = true)
    private void impl$preStateChange(final int state, final CallbackInfo ci) {
        if (this.shadow$level().isClientSide) {
            return;
        }

        final boolean isPrimed = this.bridge$isPrimed();

        if (!isPrimed && state == Constants.Entity.Creeper.STATE_PRIMED && !this.bridge$shouldPrime()) {
            ci.cancel();
        } else if (isPrimed && state == Constants.Entity.Creeper.STATE_IDLE && !this.bridge$shouldDefuse()) {
            ci.cancel();
        } else if (this.shadow$getSwellDir() != state) {
            this.impl$stateDirty = true;
        }
    }

    @Inject(method = "setSwellDir", at = @At("RETURN"))
    private void impl$postStateChange(final int state, final CallbackInfo ci) {
        if (this.shadow$level().isClientSide) {
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

    @Redirect(method = "explodeCreeper", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;DDDFLnet/minecraft/world/level/Level$ExplosionInteraction;)Lnet/minecraft/world/level/Explosion;"))
    private net.minecraft.world.level.@Nullable Explosion impl$useSpongeExplosion(final net.minecraft.world.level.Level world, final Entity self, final double x,
        final double y, final double z, final float strength, final Level.ExplosionInteraction mode) {
        return SpongeCommonEventFactory.detonateExplosive(this, Explosion.builder()
                .location(ServerLocation.of((ServerWorld) world, x, y, z))
                .sourceExplosive(((Creeper) this))
                .radius(strength)
                .shouldPlaySmoke(mode != Level.ExplosionInteraction.NONE)
                .shouldBreakBlocks(mode != Level.ExplosionInteraction.NONE && ((GrieferBridge) this).bridge$canGrief()))
                .orElseGet(() -> {
                    this.impl$detonationCancelled = true;
                    return null;
                });
    }

    @Inject(method = "explodeCreeper", at = @At("RETURN"))
    private void impl$postExplode(final CallbackInfo ci) {
        if (this.impl$detonationCancelled) {
            this.impl$detonationCancelled = this.dead = false;
        }
    }

    @Redirect(method = "mobInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Creeper;ignite()V"))
    private void impl$onProcessIgnition(final net.minecraft.world.entity.monster.Creeper self) {
        this.impl$interactPrimeCancelled = !this.bridge$shouldPrime();
        if (!this.impl$interactPrimeCancelled) {
            this.shadow$ignite();
        }
    }

    @Redirect(method = "mobInteract",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V"
        )
    )
    private void impl$onDamageFlintAndSteel(final ItemStack fas, final int amount, final LivingEntity player, final EquipmentSlot slot) {
        if (!this.impl$interactPrimeCancelled) {
            fas.hurtAndBreak(amount, player, slot);
        }
        this.impl$interactPrimeCancelled = false;
    }

}
