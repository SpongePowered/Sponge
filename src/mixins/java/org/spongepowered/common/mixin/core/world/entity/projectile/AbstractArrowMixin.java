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
package org.spongepowered.common.mixin.core.world.entity.projectile;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.projectile.source.ProjectileSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.world.entity.projectile.AbstractArrowBridge;
import org.spongepowered.common.bridge.world.level.LevelBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin extends ProjectileMixin implements AbstractArrowBridge {

    // @formatter:off
    @Shadow private int life;
    @Shadow protected boolean inGround;
    @Shadow public int shakeTime;
    @Shadow public AbstractArrow.Pickup pickup;
    @Shadow @Nullable private BlockState lastState;

    @Shadow public abstract void shadow$setCritArrow(boolean critical);
    @Shadow public abstract void shadow$setPierceLevel(byte level);
    @Shadow protected abstract ItemStack shadow$getPickupItem();
    @Shadow protected abstract void resetPiercedEntities();

    // @formatter:on

    @Nullable private Double impl$customKnockback;

    // Not all ProjectileSources are entities (e.g. BlockProjectileSource).
    // This field is used to store a ProjectileSource that isn't an entity.
    @Nullable public ProjectileSource projectileSource;

    @Override
    public double bridge$getKnockback() {
        if (this.impl$customKnockback != null) {
            return this.impl$customKnockback;
        }
        return 0;
    }

    @Override
    public void bridge$setKnockback(@Nullable final Double knockback) {
        this.impl$customKnockback = knockback;
    }

    /**
     * Collide impact event post for plugins to cancel impact.
     */
    @Inject(method = "onHitBlock", at = @At("HEAD"), cancellable = true)
    private void onProjectileHit(final BlockHitResult hitResult, final CallbackInfo ci) {
        if (!((LevelBridge) this.shadow$level()).bridge$isFake() && hitResult.getType() != HitResult.Type.MISS) {
            if (SpongeCommonEventFactory.handleCollideImpactEvent((AbstractArrow) (Object) this,
                    this.impl$getProjectileSource(), hitResult)) {
                this.shadow$playSound(SoundEvents.ARROW_HIT, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
                // Make it almost look like it collided with something
                final BlockHitResult blockraytraceresult = (BlockHitResult)hitResult;
                final BlockState blockstate = this.shadow$level().getBlockState(blockraytraceresult.getBlockPos());
                this.lastState = blockstate;
                final Vec3 vec3d = blockraytraceresult.getLocation().subtract(this.shadow$getX(), this.shadow$getY(), this.shadow$getZ());
                this.shadow$setDeltaMovement(vec3d);
                final Vec3 vec3d1 = vec3d.normalize().scale(0.05F);
                this.shadow$setPos(this.shadow$getX() - vec3d1.x, this.shadow$getY() - vec3d1.y, this.shadow$getZ() - vec3d1.z);
                this.inGround = true;
                this.shakeTime = 7;
                this.shadow$setCritArrow(false);
                this.shadow$setPierceLevel((byte)0);
                this.resetPiercedEntities();

                ci.cancel();
            }
        }
    }

    @Inject(method = "doKnockback", at = @At("HEAD"), cancellable = true)
    private void onProjectileHit(final LivingEntity hitEntity, final DamageSource $$1, final CallbackInfo ci) {
        if (this.impl$customKnockback != null) {
            Vec3 knockBackVector = this.shadow$getDeltaMovement().multiply(1.0, 0.0, 1.0).normalize().scale(this.impl$customKnockback * 0.6);
            if (knockBackVector.lengthSqr() > 0.0) {
                hitEntity.push(knockBackVector.x, 0.1, knockBackVector.z);
            }
            ci.cancel();
        }
    }

    /**
     * Collide impact event post for plugins to cancel impact.
     */
    @Inject(method = "onHitEntity", at = @At("HEAD"), cancellable = true)
    private void onProjectileHit(final EntityHitResult hitResult, final CallbackInfo ci) {
        if (!((LevelBridge) this.shadow$level()).bridge$isFake() && hitResult.getType() != HitResult.Type.MISS) {
            if (SpongeCommonEventFactory.handleCollideImpactEvent((AbstractArrow) (Object) this,
                this.impl$getProjectileSource(), hitResult)) {
                this.shadow$playSound(SoundEvents.ARROW_HIT, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
                // Make it almost look like it collided with something

                // Deflect the arrow as if the entity was invulnerable
                this.shadow$setDeltaMovement(this.shadow$getDeltaMovement().scale(-0.1D));
                this.shadow$setYRot(this.shadow$getYRot() + 180.0F);
                this.yRotO += 180.0F;
                this.life = 0;
                if (!this.shadow$level().isClientSide && this.shadow$getDeltaMovement().lengthSqr() < 1.0E-7D) {
                    if (this.pickup == AbstractArrow.Pickup.ALLOWED) {
                        this.shadow$spawnAtLocation(this.shadow$getPickupItem(), 0.1F);
                    }

                    this.shadow$discard();
                }
                ci.cancel();
            }
        }
    }

    @Inject(
        method = "tickDespawn",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;discard()V")
    )
    private void impl$throwExpireArrow(final CallbackInfo ci) {
        this.impl$callExpireEntityEvent();
    }

}
