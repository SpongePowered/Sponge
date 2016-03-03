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

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.FishHook;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.action.FishingEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.entity.projectile.ProjectileSourceSerializer;
import org.spongepowered.common.interfaces.IMixinEntityFishHook;
import org.spongepowered.common.mixin.core.entity.MixinEntity;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(EntityFishHook.class)
public abstract class MixinEntityFishHook extends MixinEntity implements FishHook, IMixinEntityFishHook {

    @Shadow private boolean inGround;
    @Shadow private EntityPlayer angler;
    @Shadow public net.minecraft.entity.Entity caughtEntity;
    @Shadow private int ticksCatchable;
    @Shadow public abstract net.minecraft.item.ItemStack getFishingResult();

    @Nullable
    public ProjectileSource projectileSource;
    private double damageAmount;
    private net.minecraft.item.ItemStack fishingRod;

    @Override
    public ProjectileSource getShooter() {
        if (this.projectileSource != null) {
            return this.projectileSource;
        } else if (this.angler != null && this.angler instanceof ProjectileSource) {
            return (ProjectileSource) this.angler;
        }
        return ProjectileSource.UNKNOWN;
    }

    @Override
    public void setShooter(ProjectileSource shooter) {
        if (shooter instanceof EntityPlayer) {
            // This allows things like Vanilla kill attribution to take place
            this.angler = (EntityPlayer) shooter;
        } else {
            this.angler = null;
        }
        this.projectileSource = shooter;
    }

    @Override
    public Optional<Entity> getHookedEntity() {
        return Optional.ofNullable((Entity) this.caughtEntity);
    }

    @Override
    public void setHookedEntity(@Nullable Entity entity) {
        this.caughtEntity = (net.minecraft.entity.Entity) entity;
    }

    @Redirect(method = "onUpdate()V", at =
            @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z")
        )
    public boolean onAttackEntityFrom(net.minecraft.entity.Entity entity, DamageSource damageSource, float damage) {
        EntitySnapshot fishHookSnapshot = this.createSnapshot();
        FishingEvent.HookEntity event = SpongeEventFactory.createFishingEventHookEntity(
            Cause.of(NamedCause.source(this.angler)), this.createSnapshot(), this, (Entity) entity);
        if (!SpongeImpl.postEvent(event)) {
            if (this.getShooter() instanceof Entity) {
                DamageSource.causeThrownDamage((net.minecraft.entity.Entity) (Object) this, (net.minecraft.entity.Entity) this.getShooter());
            }
            return entity.attackEntityFrom(damageSource, (float) this.getDamage());
        }
        return false;
    }

    public double getDamage() {
        return this.damageAmount;
    }

    /**
     * @author Aaron1011 - February 6th, 2015
     *
     * Purpose: This needs to handle for both cases where a fish and/or an entity is being caught.
     * There's no real good way to do this with an injection.
     */
    @Overwrite
    public int handleHookRetraction() {
        if (this.worldObj.isRemote) {
            return 0;
        }

        // Sponge start
        byte b0 = 0;

        net.minecraft.item.ItemStack itemStack = null;
        int exp = 0;
        if (this.ticksCatchable > 0) {
            itemStack = this.getFishingResult();
            exp = this.rand.nextInt(6) + 1;
        }

        EntitySnapshot fishHookSnapshot = this.createSnapshot();

        Transaction<ItemStackSnapshot> transaction = null;
        if (itemStack != null) {
            ItemStackSnapshot original = ((ItemStack) itemStack).createSnapshot();
            ItemStackSnapshot replacement = ((ItemStack) itemStack).createSnapshot();
            transaction = new Transaction<>(original, replacement);
        } else {
            transaction = new Transaction<>(ItemStackSnapshot.NONE, ItemStackSnapshot.NONE);
        }

        FishingEvent.Stop event = SpongeEventFactory.createFishingEventStop(Cause.of(NamedCause.source(this.angler)), exp, exp,
            fishHookSnapshot, this, transaction, (Player) this.angler);
        if (!SpongeImpl.postEvent(event)) {
            // Sponge end
            if (this.caughtEntity != null) {
                double d0 = this.angler.posX - this.posX;
                double d2 = this.angler.posY - this.posY;
                double d4 = this.angler.posZ - this.posZ;
                double d6 = (double) MathHelper.sqrt_double(d0 * d0 + d2 * d2 + d4 * d4);
                double d8 = 0.1D;
                this.caughtEntity.motionX += d0 * d8;
                this.caughtEntity.motionY += d2 * d8 + (double)MathHelper.sqrt_double(d6) * 0.08D;
                this.caughtEntity.motionZ += d4 * d8;
                b0 = 3;
            }

            // Sponge Start
            if (!event.getItemStackTransaction().getFinal().getType().equals(ItemTypes.NONE)) {
                ItemStackSnapshot itemSnapshot = event.getItemStackTransaction().getFinal();
                EntityItem entityitem1 = new EntityItem(this.worldObj, this.posX, this.posY, this.posZ, (net.minecraft.item.ItemStack) itemSnapshot.createStack());
                double d1 = this.angler.posX - this.posX;
                double d3 = this.angler.posY - this.posY;
                double d5 = this.angler.posZ - this.posZ;
                double d7 = MathHelper.sqrt_double(d1 * d1 + d3 * d3 + d5 * d5);
                double d9 = 0.1D;
                entityitem1.motionX = d1 * d9;
                entityitem1.motionY = d3 * d9 + MathHelper.sqrt_double(d7) * 0.08D;
                entityitem1.motionZ = d5 * d9;
                this.worldObj.spawnEntityInWorld(entityitem1);
                this.angler.worldObj.spawnEntityInWorld(
                        new EntityXPOrb(this.angler.worldObj, this.angler.posX, this.angler.posY + 0.5D, this.angler.posZ + 0.5D,
                                event.getExperience()));
                // Sponge End
                b0 = 1;
            }

            if (this.inGround) {
                b0 = 2;
            }

            this.setDead();
            this.angler.fishEntity = null;

            // Sponge Start
            if (this.fishingRod != null) {
                this.fishingRod.damageItem(b0, this.angler);
                this.angler.swingArm(EnumHand.MAIN_HAND);
                this.fishingRod = null;
            }
            // Sponge End
        }
        return b0;
    }

    @Override
    public void setFishingRodItemStack(net.minecraft.item.ItemStack fishingRod) {
        this.fishingRod = fishingRod;
    }

    @Override
    public void readFromNbt(NBTTagCompound compound) {
        super.readFromNbt(compound);
        if (compound.hasKey(NbtDataUtil.PROJECTILE_DAMAGE_AMOUNT)) {
            this.damageAmount = compound.getDouble(NbtDataUtil.PROJECTILE_DAMAGE_AMOUNT);
        }
        ProjectileSourceSerializer.readSourceFromNbt(compound, this);
    }

    @Override
    public void writeToNbt(NBTTagCompound compound) {
        super.writeToNbt(compound);
        compound.setDouble(NbtDataUtil.PROJECTILE_DAMAGE_AMOUNT, this.damageAmount);
        ProjectileSourceSerializer.writeSourceToNbt(compound, this.projectileSource, this.angler);
    }
}
