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
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.entity.projectile.Arrow;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.entity.projectile.ProjectileSourceSerializer;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.interfaces.entity.projectile.IMixinEntityArrow;
import org.spongepowered.common.mixin.core.entity.MixinEntity;

import java.util.List;

import javax.annotation.Nullable;

@Mixin(EntityArrow.class)
public abstract class MixinEntityArrow extends MixinEntity implements Arrow, IMixinEntityArrow {

    private static final String RTR_CTOR_ENTITY = "net/minecraft/util/MovingObjectPosition.entityHit:Lnet/minecraft/entity/Entity;";
    private EntityArrow mcEntity = (EntityArrow) (Object) this;

    @Shadow public Entity shootingEntity;
    @Shadow private int ticksInAir;
    @Shadow public double damage;
    @Shadow public boolean inGround;

    // Not all ProjectileSources are entities (e.g. BlockProjectileSource).
    // This field is used to store a ProjectileSource that isn't an entity.
    @Nullable public ProjectileSource projectileSource;

    @Override
    public ProjectileSource getShooter() {
        if (this.projectileSource instanceof ProjectileSource) {
            return this.projectileSource;
        } else if (this.shootingEntity instanceof ProjectileSource) {
            return (ProjectileSource) this.shootingEntity;
        }
        return ProjectileSource.UNKNOWN;
    }

    @Override
    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
        manipulators.add(getKnockbackData());
    }

    @Override
    public void setShooter(ProjectileSource shooter) {
        if (shooter instanceof Entity) {
            // This allows things like Vanilla kill attribution to take place
            this.shootingEntity = (Entity) shooter;
        } else {
            this.shootingEntity = null;
        }
        this.projectileSource = shooter;
    }

    @Override
    public void readFromNbt(NBTTagCompound compound) {
        super.readFromNbt(compound);
        ProjectileSourceSerializer.readSourceFromNbt(compound, this);
    }

    @Override
    public void writeToNbt(NBTTagCompound compound) {
        super.writeToNbt(compound);
        ProjectileSourceSerializer.writeSourceToNbt(compound, this.projectileSource, this.shootingEntity);
    }

    /**
     * This is the injection used in dev.
     */
    @Inject(method = "onUpdate", at = @At(value = "FIELD", target = RTR_CTOR_ENTITY, ordinal = 3, shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true, require = 0)
    private void onArrowImpact(CallbackInfo ci, BlockPos pos, IBlockState state, Block block, Vec3 vecA, Vec3 vecB, MovingObjectPosition hitResult, Entity entity, List<Entity> aabbs, double d0) {
        this.arrowImpact(ci, hitResult);
    }

    /**
     * This is the injection used in production.
     */
    @Surrogate
    private void onArrowImpact(CallbackInfo ci, Vec3 vecA, Vec3 vecB, MovingObjectPosition hitResult) {
        this.arrowImpact(ci, hitResult);
    }

    /**
     * Collide impact event post for plugins to cancel impact.
     */
    private void arrowImpact(CallbackInfo ci, MovingObjectPosition hitResult) {
        if (!this.worldObj.isRemote) {
            if (SpongeCommonEventFactory.handleCollideImpactEvent(this.mcEntity, getShooter(), hitResult)) {
                // deflect and drop to ground
                this.motionX *= -0.10000000149011612D;
                this.motionY *= -0.10000000149011612D;
                this.motionZ *= -0.10000000149011612D;
                this.rotationYaw += 180.0F;
                this.mcEntity.prevRotationYaw += 180.0F;
                this.ticksInAir = 0;
                ci.cancel();
            }
        }
    }

    @Override
    public boolean isInGround() {
        return this.inGround;
    }
}
