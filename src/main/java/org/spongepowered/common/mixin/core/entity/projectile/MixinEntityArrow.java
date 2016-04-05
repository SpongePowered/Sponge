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
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.entity.projectile.Arrow;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.entity.projectile.ProjectileSourceSerializer;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.mixin.core.entity.MixinEntity;

import java.util.List;

import javax.annotation.Nullable;

@Mixin(EntityArrow.class)
public abstract class MixinEntityArrow extends MixinEntity implements Arrow {

    private EntityArrow mcEntity = (EntityArrow) (Object) this;

    @Shadow private int xTile = -1;
    @Shadow private int yTile = -1;
    @Shadow private int zTile = -1;
    @Shadow private Block inTile;
    @Shadow private int inData;
    @Shadow private boolean inGround;
    @Shadow public int canBePickedUp;
    @Shadow public int arrowShake;
    @Shadow public Entity shootingEntity;
    @Shadow private int ticksInGround;
    @Shadow private int ticksInAir;
    @Shadow public double damage;
    @Shadow public int knockbackStrength;

    @Shadow public abstract void setIsCritical(boolean critical);
    @Shadow public abstract boolean getIsCritical();

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
     * @author blood - April 5th, 2016
     * 
     * Added CollideEntityEvent hooks for plugins to cancel impact.
     * 
     */
    @Overwrite
    public void onUpdate() {
        super.shadow$onUpdate();

        if (this.mcEntity.prevRotationPitch == 0.0F && this.mcEntity.prevRotationYaw == 0.0F) {
            float f = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
            this.mcEntity.prevRotationYaw = this.rotationYaw = (float) (MathHelper.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);
            this.mcEntity.prevRotationPitch = this.rotationPitch = (float) (MathHelper.atan2(this.motionY, (double) f) * 180.0D / Math.PI);
        }

        BlockPos blockpos = new BlockPos(this.xTile, this.yTile, this.zTile);
        IBlockState iblockstate = this.worldObj.getBlockState(blockpos);
        Block block = iblockstate.getBlock();

        if (block.getMaterial() != Material.air) {
            block.setBlockBoundsBasedOnState(this.worldObj, blockpos);
            AxisAlignedBB axisalignedbb = block.getCollisionBoundingBox(this.worldObj, blockpos, iblockstate);

            if (axisalignedbb != null && axisalignedbb.isVecInside(new Vec3(this.posX, this.posY, this.posZ))) {
                this.inGround = true;
            }
        }

        if (this.arrowShake > 0) {
            --this.arrowShake;
        }

        if (this.inGround) {
            int j = block.getMetaFromState(iblockstate);

            if (block == this.inTile && j == this.inData) {
                ++this.ticksInGround;

                if (this.ticksInGround >= 1200) {
                    this.setDead();
                }
            } else {
                this.inGround = false;
                this.motionX *= (double) (this.rand.nextFloat() * 0.2F);
                this.motionY *= (double) (this.rand.nextFloat() * 0.2F);
                this.motionZ *= (double) (this.rand.nextFloat() * 0.2F);
                this.ticksInGround = 0;
                this.ticksInAir = 0;
            }
        } else {
            ++this.ticksInAir;
            Vec3 vec31 = new Vec3(this.posX, this.posY, this.posZ);
            Vec3 vec3 = new Vec3(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
            MovingObjectPosition movingobjectposition = this.worldObj.rayTraceBlocks(vec31, vec3, false, true, false);
            vec31 = new Vec3(this.posX, this.posY, this.posZ);
            vec3 = new Vec3(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

            if (movingobjectposition != null) {
                vec3 = new Vec3(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
            }

            Entity entity = null;
            List<Entity> list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this.mcEntity,
                    this.getEntityBoundingBox().addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D));
            double d0 = 0.0D;

            for (int i = 0; i < list.size(); ++i) {
                Entity entity1 = (Entity) list.get(i);

                if (entity1.canBeCollidedWith() && (entity1 != this.shootingEntity || this.ticksInAir >= 5)) {
                    float f1 = 0.3F;
                    AxisAlignedBB axisalignedbb1 = entity1.getEntityBoundingBox().expand((double) f1, (double) f1, (double) f1);
                    MovingObjectPosition movingobjectposition1 = axisalignedbb1.calculateIntercept(vec31, vec3);

                    if (movingobjectposition1 != null) {
                        double d1 = vec31.squareDistanceTo(movingobjectposition1.hitVec);

                        if (d1 < d0 || d0 == 0.0D) {
                            entity = entity1;
                            d0 = d1;
                        }
                    }
                }
            }

            if (entity != null) {
                movingobjectposition = new MovingObjectPosition(entity);
            }

            if (movingobjectposition != null && movingobjectposition.entityHit != null && movingobjectposition.entityHit instanceof EntityPlayer) {
                EntityPlayer entityplayer = (EntityPlayer) movingobjectposition.entityHit;

                if (entityplayer.capabilities.disableDamage
                        || this.shootingEntity instanceof EntityPlayer && !((EntityPlayer) this.shootingEntity).canAttackPlayer(entityplayer)) {
                    movingobjectposition = null;
                }
            }

            if (movingobjectposition != null) {
                // Sponge start
                if (!this.worldObj.isRemote) {
                    if (SpongeCommonEventFactory.handleCollideImpactEvent(this.mcEntity, getShooter(), movingobjectposition)) {
                        return;
                    }

                    // deflect and drop to ground
                    this.motionX *= -0.10000000149011612D;
                    this.motionY *= -0.10000000149011612D;
                    this.motionZ *= -0.10000000149011612D;
                    this.rotationYaw += 180.0F;
                    this.mcEntity.prevRotationYaw += 180.0F;
                    this.ticksInAir = 0;
                    return;
                }
                // Sponge end

                if (movingobjectposition.entityHit != null) {
                    float f2 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
                    int l = MathHelper.ceiling_double_int((double) f2 * this.damage);

                    if (this.getIsCritical()) {
                        l += this.rand.nextInt(l / 2 + 2);
                    }

                    DamageSource damagesource;

                    if (this.shootingEntity == null) {
                        damagesource = DamageSource.causeArrowDamage(this.mcEntity, this.mcEntity);
                    } else {
                        damagesource = DamageSource.causeArrowDamage(this.mcEntity, this.shootingEntity);
                    }

                    if (this.mcEntity.isBurning() && !(movingobjectposition.entityHit instanceof EntityEnderman)) {
                        movingobjectposition.entityHit.setFire(5);
                    }

                    if (movingobjectposition.entityHit.attackEntityFrom(damagesource, (float) l)) {
                        if (movingobjectposition.entityHit instanceof EntityLivingBase) {
                            EntityLivingBase entitylivingbase = (EntityLivingBase) movingobjectposition.entityHit;

                            if (!this.worldObj.isRemote) {
                                entitylivingbase.setArrowCountInEntity(entitylivingbase.getArrowCountInEntity() + 1);
                            }

                            if (this.knockbackStrength > 0) {
                                float f7 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);

                                if (f7 > 0.0F) {
                                    movingobjectposition.entityHit.addVelocity(
                                            this.motionX * (double) this.knockbackStrength * 0.6000000238418579D / (double) f7, 0.1D,
                                            this.motionZ * (double) this.knockbackStrength * 0.6000000238418579D / (double) f7);
                                }
                            }

                            if (this.shootingEntity instanceof EntityLivingBase) {
                                EnchantmentHelper.applyThornEnchantments(entitylivingbase, this.shootingEntity);
                                EnchantmentHelper.applyArthropodEnchantments((EntityLivingBase) this.shootingEntity, entitylivingbase);
                            }

                            if (this.shootingEntity != null && movingobjectposition.entityHit != this.shootingEntity
                                    && movingobjectposition.entityHit instanceof EntityPlayer && this.shootingEntity instanceof EntityPlayerMP) {
                                ((EntityPlayerMP) this.shootingEntity).playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(6, 0.0F));
                            }
                        }

                        this.mcEntity.playSound("random.bowhit", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));

                        if (!(movingobjectposition.entityHit instanceof EntityEnderman)) {
                            this.setDead();
                        }
                    } else {
                        this.motionX *= -0.10000000149011612D;
                        this.motionY *= -0.10000000149011612D;
                        this.motionZ *= -0.10000000149011612D;
                        this.rotationYaw += 180.0F;
                        this.mcEntity.prevRotationYaw += 180.0F;
                        this.ticksInAir = 0;
                    }
                } else {
                    BlockPos blockpos1 = movingobjectposition.getBlockPos();
                    this.xTile = blockpos1.getX();
                    this.yTile = blockpos1.getY();
                    this.zTile = blockpos1.getZ();
                    IBlockState iblockstate1 = this.worldObj.getBlockState(blockpos1);
                    this.inTile = iblockstate1.getBlock();
                    this.inData = this.inTile.getMetaFromState(iblockstate1);
                    this.motionX = (double) ((float) (movingobjectposition.hitVec.xCoord - this.posX));
                    this.motionY = (double) ((float) (movingobjectposition.hitVec.yCoord - this.posY));
                    this.motionZ = (double) ((float) (movingobjectposition.hitVec.zCoord - this.posZ));
                    float f5 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
                    this.posX -= this.motionX / (double) f5 * 0.05000000074505806D;
                    this.posY -= this.motionY / (double) f5 * 0.05000000074505806D;
                    this.posZ -= this.motionZ / (double) f5 * 0.05000000074505806D;
                    this.mcEntity.playSound("random.bowhit", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
                    this.inGround = true;
                    this.arrowShake = 7;
                    this.setIsCritical(false);

                    if (this.inTile.getMaterial() != Material.air) {
                        this.inTile.onEntityCollidedWithBlock(this.worldObj, blockpos1, iblockstate1, this.mcEntity);
                    }
                }
            }

            if (this.getIsCritical()) {
                for (int k = 0; k < 4; ++k) {
                    this.worldObj.spawnParticle(EnumParticleTypes.CRIT, this.posX + this.motionX * (double) k / 4.0D,
                            this.posY + this.motionY * (double) k / 4.0D, this.posZ + this.motionZ * (double) k / 4.0D, -this.motionX,
                            -this.motionY + 0.2D, -this.motionZ, new int[0]);
                }
            }

            this.posX += this.motionX;
            this.posY += this.motionY;
            this.posZ += this.motionZ;
            float f3 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
            this.rotationYaw = (float) (MathHelper.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);

            for (this.rotationPitch = (float) (MathHelper.atan2(this.motionY, (double) f3) * 180.0D / Math.PI); this.rotationPitch
                    - this.mcEntity.prevRotationPitch < -180.0F; this.mcEntity.prevRotationPitch -= 360.0F) {
                ;
            }

            while (this.rotationPitch - this.mcEntity.prevRotationPitch >= 180.0F) {
                this.mcEntity.prevRotationPitch += 360.0F;
            }

            while (this.rotationYaw - this.mcEntity.prevRotationYaw < -180.0F) {
                this.mcEntity.prevRotationYaw -= 360.0F;
            }

            while (this.rotationYaw - this.mcEntity.prevRotationYaw >= 180.0F) {
                this.mcEntity.prevRotationYaw += 360.0F;
            }

            this.rotationPitch = this.mcEntity.prevRotationPitch + (this.rotationPitch - this.mcEntity.prevRotationPitch) * 0.2F;
            this.rotationYaw = this.mcEntity.prevRotationYaw + (this.rotationYaw - this.mcEntity.prevRotationYaw) * 0.2F;
            float f4 = 0.99F;
            float f6 = 0.05F;

            if (this.mcEntity.isInWater()) {
                for (int i1 = 0; i1 < 4; ++i1) {
                    float f8 = 0.25F;
                    this.worldObj.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * (double) f8,
                            this.posY - this.motionY * (double) f8, this.posZ - this.motionZ * (double) f8, this.motionX, this.motionY, this.motionZ,
                            new int[0]);
                }

                f4 = 0.6F;
            }

            if (this.mcEntity.isWet()) {
                this.mcEntity.extinguish();
            }

            this.motionX *= (double) f4;
            this.motionY *= (double) f4;
            this.motionZ *= (double) f4;
            this.motionY -= (double) f6;
            this.setPosition(this.posX, this.posY, this.posZ);
            this.doBlockCollisions();
        }
    }

}
