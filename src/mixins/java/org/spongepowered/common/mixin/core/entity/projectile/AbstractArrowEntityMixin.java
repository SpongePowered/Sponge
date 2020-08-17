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
package org.spongepowered.common.mixin.core.entity.projectile;

import net.minecraft.block.BlockState;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.projectile.arrow.Arrow;
import org.spongepowered.api.entity.projectile.arrow.ArrowEntity;
import org.spongepowered.api.projectile.source.ProjectileSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.entity.projectile.ProjectileSourceSerializer;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.mixin.core.entity.EntityMixin;

import java.util.UUID;

import javax.annotation.Nullable;

@Mixin(AbstractArrowEntity.class)
public abstract class AbstractArrowEntityMixin extends EntityMixin {

    @Shadow public UUID shootingEntity;
    @Shadow private int ticksInAir;
    @Shadow private double damage;
    @Shadow protected boolean inGround;
    @Shadow public int arrowShake;
    @Shadow public AbstractArrowEntity.PickupStatus pickupStatus;


    @Shadow public abstract void shadow$setIsCritical(boolean critical);

    @Shadow @Nullable private BlockState inBlockState;

    @Shadow public abstract void shadow$setPierceLevel(byte level);

    @Shadow public abstract void shadow$setShotFromCrossbow(boolean fromCrossbow);

    @Shadow protected abstract void shadow$func_213870_w();

    @Shadow protected abstract ItemStack shadow$getArrowStack();

    // Not all ProjectileSources are entities (e.g. BlockProjectileSource).
    // This field is used to store a ProjectileSource that isn't an entity.
    @Nullable public ProjectileSource projectileSource;

    @Override
    public void impl$readFromSpongeCompound(final CompoundNBT compound) {
        super.impl$readFromSpongeCompound(compound);
        ProjectileSourceSerializer.readSourceFromNbt(compound, (Arrow) this);
    }

    @Override
    public void impl$writeToSpongeCompound(final CompoundNBT compound) {
        super.impl$writeToSpongeCompound(compound);
        ProjectileSourceSerializer.writeSourceToNbt(compound, ((Arrow) this).shooter().get(), this.shootingEntity);
    }

    /**
     * Collide impact event post for plugins to cancel impact.
     */
    @Inject(method = "onHit", at = @At("HEAD"), cancellable = true)
    private void onProjectileHit(final RayTraceResult hitResult, final CallbackInfo ci) {
        if (!((WorldBridge) this.world).bridge$isFake() && hitResult.getType() != RayTraceResult.Type.MISS) {
            if (SpongeCommonEventFactory.handleCollideImpactEvent((AbstractArrowEntity) (Object) this,
                    ((ArrowEntity) this).get(Keys.SHOOTER).orElse(null), hitResult)) {
                this.shadow$playSound(SoundEvents.ENTITY_ARROW_HIT, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
                // Make it almost look like it collided with something
                if (hitResult.getType() == RayTraceResult.Type.BLOCK) {
                    BlockRayTraceResult blockraytraceresult = (BlockRayTraceResult)hitResult;
                    BlockState blockstate = this.world.getBlockState(blockraytraceresult.getPos());
                    this.inBlockState = blockstate;
                    Vec3d vec3d = blockraytraceresult.getHitVec().subtract(this.posX, this.posY, this.posZ);
                    this.shadow$setMotion(vec3d);
                    Vec3d vec3d1 = vec3d.normalize().scale((double)0.05F);
                    this.posX -= vec3d1.x;
                    this.posY -= vec3d1.y;
                    this.posZ -= vec3d1.z;
                    this.inGround = true;
                    this.arrowShake = 7;
                    this.shadow$setIsCritical(false);
                    this.shadow$setPierceLevel((byte)0);
                    this.shadow$setShotFromCrossbow(false);
                    this.shadow$func_213870_w();
                } else {
                    // Deflect the arrow as if the entity was invulnerable
                    this.shadow$setMotion(this.shadow$getMotion().scale(-0.1D));
                    this.rotationYaw += 180.0F;
                    this.prevRotationYaw += 180.0F;
                    this.ticksInAir = 0;
                    if (!this.world.isRemote && this.shadow$getMotion().lengthSquared() < 1.0E-7D) {
                        if (this.pickupStatus == AbstractArrowEntity.PickupStatus.ALLOWED) {
                            this.shadow$entityDropItem(this.shadow$getArrowStack(), 0.1F);
                        }

                        this.shadow$remove();
                    }
                }
                ci.cancel();
            }
        }
    }

}
