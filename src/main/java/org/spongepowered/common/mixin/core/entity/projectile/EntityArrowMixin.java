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

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import org.spongepowered.api.entity.projectile.arrow.Arrow;
import org.spongepowered.api.projectile.source.ProjectileSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.entity.projectile.ProjectileSourceSerializer;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.mixin.core.entity.EntityMixin;

import javax.annotation.Nullable;

@Mixin(AbstractArrowEntity.class)
public abstract class EntityArrowMixin extends EntityMixin {

    @Shadow public Entity shootingEntity;
    @Shadow private int ticksInAir;
    @Shadow private double damage;
    @Shadow protected boolean inGround;
    @Shadow public int arrowShake;
    @Shadow private int xTile;
    @Shadow private int yTile;
    @Shadow private int zTile;
    @Shadow private Block inTile;
    @Shadow private int inData;

    @Shadow public abstract void setIsCritical(boolean critical);

    // Not all ProjectileSources are entities (e.g. BlockProjectileSource).
    // This field is used to store a ProjectileSource that isn't an entity.
    @Nullable public ProjectileSource projectileSource;

    @Override
    public void spongeImpl$readFromSpongeCompound(final CompoundNBT compound) {
        super.spongeImpl$readFromSpongeCompound(compound);
        ProjectileSourceSerializer.readSourceFromNbt(compound, (Arrow) this);
    }

    @Override
    public void spongeImpl$writeToSpongeCompound(final CompoundNBT compound) {
        super.spongeImpl$writeToSpongeCompound(compound);
        ProjectileSourceSerializer.writeSourceToNbt(compound, ((Arrow) this).getShooter(), this.shootingEntity);
    }

    /**
     * Collide impact event post for plugins to cancel impact.
     */
    @Inject(method = "onHit", at = @At("HEAD"), cancellable = true)
    private void onProjectileHit(final RayTraceResult hitResult, final CallbackInfo ci) {
        if (!this.world.isRemote) {
            if (SpongeCommonEventFactory.handleCollideImpactEvent((AbstractArrowEntity) (Object) this, ((Arrow) this).getShooter(), hitResult)) {
                // deflect and drop to ground
                this.motionX *= -0.10000000149011612D;
                this.motionY *= -0.10000000149011612D;
                this.motionZ *= -0.10000000149011612D;
                this.rotationYaw += 180.0F;
                ((AbstractArrowEntity) (Object) this).prevRotationYaw += 180.0F;
                this.ticksInAir = 0;
                this.playSound(SoundEvents.ENTITY_ARROW_HIT, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
                // if block was hit, change state to reflect it hit block to avoid onHit logic repeating indefinitely
                if (hitResult.entityHit == null) {
                    final BlockPos blockpos = hitResult.getBlockPos();
                    this.xTile = blockpos.getX();
                    this.yTile = blockpos.getY();
                    this.zTile = blockpos.getZ();
                    final BlockState iblockstate = this.world.getBlockState(blockpos);
                    this.inTile = iblockstate.getBlock();
                    this.inData = this.inTile.getMetaFromState(iblockstate);
                    this.inGround = true;
                    this.arrowShake = 7;
                    this.setIsCritical(false);
                }
                ci.cancel();
            }
        }
    }

}
