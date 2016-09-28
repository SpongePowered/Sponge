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

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTableList;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.FishHook;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.action.FishingEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.projectile.ProjectileSourceSerializer;
import org.spongepowered.common.mixin.core.entity.MixinEntity;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@Mixin(EntityFishHook.class)
public abstract class MixinEntityFishHook extends MixinEntity implements FishHook {

    @Shadow private boolean inGround;
    @Shadow private EntityPlayer angler;
    @Shadow public net.minecraft.entity.Entity caughtEntity;
    @Shadow private int ticksCatchable;
    @Shadow protected abstract void bringInHookedEntity();

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

    /**
     * @author Aaron1011 - February 6th, 2015
     * @reason This needs to handle for both cases where a fish and/or an entity is being caught.
     * There's no real good way to do this with an injection.
     */

    @Overwrite
    public int handleHookRetraction() {
        if (this.worldObj.isRemote) {
            return 0;
        } else {
            int i = 0;

            if (this.caughtEntity != null) {
                this.bringInHookedEntity();
                this.worldObj.setEntityState((EntityFishHook) (Object) this, (byte)31);
                i = this.caughtEntity instanceof EntityItem ? 3 : 5;

            } else if (this.ticksCatchable > 0) {
                LootContext.Builder lootcontext$builder = new LootContext.Builder((WorldServer)this.worldObj);
                lootcontext$builder.withLuck((float) EnchantmentHelper.getLuckOfSeaModifier(this.angler) + this.angler.getLuck());

                // Sponge start
                // TODO 1.9: Figure out how we want experience to work here
                List<net.minecraft.item.ItemStack> itemstacks = this.worldObj.getLootTableManager().getLootTableFromLocation(LootTableList.GAMEPLAY_FISHING).generateLootForPools(this.rand, lootcontext$builder.build());
                FishingEvent.Stop event = SpongeEventFactory.createFishingEventStop(Cause.of(NamedCause.source(this.angler)), 0, 0,
                        this.createSnapshot(), this, itemstacks.stream().map(s -> {
                            ItemStackSnapshot snapshot = ((ItemStack) s).createSnapshot();
                            return new Transaction<>(snapshot, snapshot);
                        }).collect(Collectors.toList()), (Player) this.angler);

                if (!SpongeImpl.postEvent(event)) {
                    for (net.minecraft.item.ItemStack itemstack : event.getItemStackTransaction().stream().filter(Transaction::isValid).map(t -> (net.minecraft.item.ItemStack) t.getFinal().createStack()).collect(Collectors.toList())) {
                        EntityItem entityitem = new EntityItem(this.worldObj, this.posX, this.posY, this.posZ, itemstack);
                        double d0 = this.angler.posX - this.posX;
                        double d1 = this.angler.posY - this.posY;
                        double d2 = this.angler.posZ - this.posZ;
                        double d3 = (double)MathHelper.sqrt_double(d0 * d0 + d1 * d1 + d2 * d2);
                        double d4 = 0.1D;
                        entityitem.motionX = d0 * d4;
                        entityitem.motionY = d1 * d4 + (double)MathHelper.sqrt_double(d3) * 0.08D;
                        entityitem.motionZ = d2 * d4;
                        this.worldObj.spawnEntityInWorld(entityitem);
                        this.angler.worldObj.spawnEntityInWorld(new EntityXPOrb(this.angler.worldObj, this.angler.posX, this.angler.posY + 0.5D, this.angler.posZ + 0.5D, this.rand.nextInt(6) + 1));
                    } // Sponge end
                }

                i = 1;
            }

            if (this.inGround)
            {
                i = 2;
            }

            this.setDead();
            this.angler.fishEntity = null;
            return i;
        }
    }

    @Override
    public void readFromNbt(NBTTagCompound compound) {
        super.readFromNbt(compound);
        ProjectileSourceSerializer.readSourceFromNbt(compound, this);
    }

    @Override
    public void writeToNbt(NBTTagCompound compound) {
        super.writeToNbt(compound);
        ProjectileSourceSerializer.writeSourceToNbt(compound, this.projectileSource, this.angler);
    }
}
