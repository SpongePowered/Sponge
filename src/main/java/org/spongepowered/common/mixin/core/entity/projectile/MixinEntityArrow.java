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
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.entity.projectile.ProjectileSourceSerializer;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.mixin.core.entity.MixinEntity;

import java.util.List;

import javax.annotation.Nullable;

@Mixin(EntityArrow.class)
public abstract class MixinEntityArrow extends MixinEntity implements Arrow {

    private Entity mcEntity = (Entity)(Object) this;

    @Shadow public double damage;
    @Shadow public Entity shootingEntity;

    // Not all ProjectileSources are entities (e.g. BlockProjectileSource).
    // This field is used to store a ProjectileSource that isn't an entity.
    @Nullable
    public ProjectileSource projectileSource;

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

    @ModifyVariable(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/MovingObjectPosition;<init>(Lnet/minecraft/entity/Entity;)V", shift = At.Shift.BY, by = 2))
    private MovingObjectPosition onAfterMoving(MovingObjectPosition movingObjectPosition) {
        if (this.worldObj.isRemote) {
            return movingObjectPosition;
        }

        if (SpongeCommonEventFactory.handleCollideImpactEvent(this.mcEntity, getShooter(), movingObjectPosition)) {
            movingObjectPosition = null;
        }
        return movingObjectPosition;
     }

    @Inject(method = "onUpdate", at = @At(value = "INVOKE", args="log=true",target = "Lnet/minecraft/block/Block;onEntityCollidedWithBlock(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/entity/Entity;)V"), locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true)
    public void onBlockHit(CallbackInfo ci, BlockPos pos, IBlockState state, Block block, Vec3 vec, Vec3 vec1, MovingObjectPosition movingObjectPosition, Entity entity, List<Entity> list, double d0, BlockPos pos1, IBlockState state1, float f5) {
        if (this.worldObj.isRemote) {
            return;
        }

        if (SpongeCommonEventFactory.handleCollideImpactEvent(this.mcEntity, getShooter(), movingObjectPosition)) {
            ci.cancel();
        }
    }

}
