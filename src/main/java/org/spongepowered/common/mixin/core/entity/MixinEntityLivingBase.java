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
package org.spongepowered.common.mixin.core.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.CombatTracker;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.damage.DamageModifier;
import org.spongepowered.api.event.cause.entity.damage.source.FallingBlockDamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.entity.living.human.EntityHuman;
import org.spongepowered.common.event.DamageEventHandler;
import org.spongepowered.common.event.DamageObject;
import org.spongepowered.common.interfaces.entity.IMixinEntityLivingBase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings("rawtypes")
@NonnullByDefault
@Mixin(value = EntityLivingBase.class, priority = 999)
public abstract class MixinEntityLivingBase extends MixinEntity implements Living, IMixinEntityLivingBase {

    private EntityLivingBase nmsEntityLiving = (EntityLivingBase) (Object) this;
    private int maxAir = 300;

    @Shadow public int maxHurtResistantTime;
    @Shadow public int hurtTime;
    @Shadow public int maxHurtTime;
    @Shadow public int deathTime;
    @Shadow public boolean potionsNeedUpdate;
    @Shadow public CombatTracker _combatTracker;
    @Shadow public EntityLivingBase entityLivingToAttack;
    @Shadow protected int entityAge;
    @Shadow protected int recentlyHit;
    @Shadow protected float lastDamage;
    @Shadow protected EntityPlayer attackingPlayer;
    @Shadow protected abstract void damageArmor(float p_70675_1_);
    @Shadow protected abstract void setBeenAttacked();
    @Shadow protected abstract String getDeathSound();
    @Shadow protected abstract float getSoundVolume();
    @Shadow protected abstract float getSoundPitch();
    @Shadow protected abstract String getHurtSound();
    @Shadow public abstract void setHealth(float health);
    @Shadow public abstract void addPotionEffect(net.minecraft.potion.PotionEffect potionEffect);
    @Shadow public abstract void removePotionEffect(int id);
    @Shadow public abstract void setCurrentItemOrArmor(int slotIn, ItemStack stack);
    @Shadow public abstract void clearActivePotions();
    @Shadow public abstract void setLastAttacker(net.minecraft.entity.Entity entity);
    @Shadow public abstract boolean isPotionActive(Potion potion);
    @Shadow public abstract float getHealth();
    @Shadow public abstract float getMaxHealth();
    @Shadow public abstract Collection getActivePotionEffects();
    @Shadow public abstract EntityLivingBase getLastAttacker();
    @Shadow public abstract IAttributeInstance getEntityAttribute(IAttribute attribute);
    @Shadow public abstract ItemStack getEquipmentInSlot(int slotIn);

    @Override
    public int getMaxAir() {
        return this.maxAir;
    }

    @Override
    public void setMaxAir(int air) {
        this.maxAir = air;
    }

    @Override
    public double getLastDamage() {
        return this.lastDamage;
    }

    @Override
    public void setLastDamage(double damage) {
        this.lastDamage = (float) damage;
    }

    @Override
    public void readFromNbt(NBTTagCompound compound) {
        super.readFromNbt(compound);
        if (compound.hasKey("maxAir")) {
            this.maxAir = compound.getInteger("maxAir");
        }
    }

    @Override
    public void writeToNbt(NBTTagCompound compound) {
        super.writeToNbt(compound);
        compound.setInteger("maxAir", this.maxAir);
    }

    @Override
    public Text getTeamRepresentation() {
        return Text.of(this.getUniqueID().toString());
    }

    @Redirect(method = "onDeath(Lnet/minecraft/util/DamageSource;)V", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/world/World;setEntityState(Lnet/minecraft/entity/Entity;B)V"))
    public void onDeathSendEntityState(World world, net.minecraft.entity.Entity self, byte state) {
        // Don't send the state if this is a human. Fixes ghost items on client.
        if (!((net.minecraft.entity.Entity) (Object) this instanceof EntityHuman)) {
            world.setEntityState(self, state);
        }
    }

    @Redirect(method = "applyPotionDamageCalculations", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;isPotionActive(Lnet/minecraft/potion/Potion;)Z") )
    public boolean onIsPotionActive(EntityLivingBase entityIn, Potion potion) {
        return false; // handled in our damageEntityHook
    }

    @Redirect(method = "applyArmorCalculations", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;damageArmor(F)V") )
    protected void onDamageArmor(EntityLivingBase entityIn, float damage) {
        // do nothing as this is handled in our damageEntityHook
    }

    /**
     * @author bloodmc - November 21, 2015
     *
     * Purpose: This shouldn't be used internally but a mod may still call it so we simply reroute to our hook.
     */
    @Overwrite
    protected void damageEntity(DamageSource damageSource, float damage) {
        this.damageEntityHook(damageSource, damage);
    }

    /**
     * @author bloodmc - November 22, 2015
     *
     * Purpose: Reroute damageEntity calls to our hook in order to prevent damage.
     */
    @Override
    @Overwrite
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (source == null) {
            Thread.dumpStack();
        }
        if (!hookModAttack(this.nmsEntityLiving, source, amount))
            return false;
        if (this.nmsEntityLiving.isEntityInvulnerable(source)) {
            return false;
        } else if (this.worldObj.isRemote) {
            return false;
        } else {
            this.entityAge = 0;

            if (this.nmsEntityLiving.getHealth() <= 0.0F) {
                return false;
            } else if (source.isFireDamage() && this.nmsEntityLiving.isPotionActive(Potion.fireResistance)) {
                return false;
            } else {
                // Sponge - ignore as this is handled in our damageEntityHook
                if (false && (source == DamageSource.anvil || source == DamageSource.fallingBlock)
                    && this.nmsEntityLiving.getEquipmentInSlot(4) != null) {
                    this.nmsEntityLiving.getEquipmentInSlot(4).damageItem((int) (amount * 4.0F + this.rand.nextFloat() * amount * 2.0F),
                                                                          this.nmsEntityLiving);
                    amount *= 0.75F;
                }

                this.nmsEntityLiving.limbSwingAmount = 1.5F;
                boolean flag = true;

                if ((float) this.hurtResistantTime > (float) this.nmsEntityLiving.maxHurtResistantTime / 2.0F) {
                    if (amount <= this.lastDamage) {
                        return false;
                    }

                    // Sponge start - reroute to our damage hook
                    if (!this.damageEntityHook(source, amount - this.lastDamage)) {
                        return false;
                    }
                    // Sponge end

                    this.lastDamage = amount;
                    flag = false;
                } else {
                    // Sponge start - reroute to our damage hook
                    if (!this.damageEntityHook(source, amount)) {
                        return false;
                    }
                    this.lastDamage = amount;
                    this.hurtResistantTime = this.nmsEntityLiving.maxHurtResistantTime;
                    // this.damageEntity(source, amount); // handled above
                    // Sponge end
                    this.nmsEntityLiving.hurtTime = this.nmsEntityLiving.maxHurtTime = 10;
                }

                this.nmsEntityLiving.attackedAtYaw = 0.0F;
                net.minecraft.entity.Entity entity = source.getEntity();

                if (entity != null) {
                    if (entity instanceof EntityLivingBase) {
                        this.nmsEntityLiving.setRevengeTarget((EntityLivingBase) entity);
                    }

                    if (entity instanceof EntityPlayer) {
                        this.recentlyHit = 100;
                        this.attackingPlayer = (EntityPlayer) entity;
                    } else if (entity instanceof net.minecraft.entity.passive.EntityTameable) {
                        net.minecraft.entity.passive.EntityTameable entitywolf = (net.minecraft.entity.passive.EntityTameable) entity;

                        if (entitywolf.isTamed()) {
                            this.recentlyHit = 100;
                            this.attackingPlayer = null;
                        }
                    }
                }

                if (flag) {
                    this.worldObj.setEntityState(this.nmsEntityLiving, (byte) 2);

                    if (source != DamageSource.drown) {
                        this.setBeenAttacked();
                    }

                    if (entity != null) {
                        double d1 = entity.posX - this.posX;
                        double d0;

                        for (d0 = entity.posZ - this.posZ; d1 * d1 + d0 * d0 < 1.0E-4D; d0 = (Math.random() - Math.random()) * 0.01D) {
                            d1 = (Math.random() - Math.random()) * 0.01D;
                        }

                        this.nmsEntityLiving.attackedAtYaw = (float) (Math.atan2(d0, d1) * 180.0D / Math.PI - (double) this.rotationYaw);
                        this.nmsEntityLiving.knockBack(entity, amount, d1, d0);
                    } else {
                        this.nmsEntityLiving.attackedAtYaw = (float) ((int) (Math.random() * 2.0D) * 180);
                    }
                }

                String s;

                if (this.nmsEntityLiving.getHealth() <= 0.0F) {
                    s = this.getDeathSound();

                    if (flag && s != null) {
                        this.nmsEntityLiving.playSound(s, this.getSoundVolume(), this.getSoundPitch());
                    }

                    this.nmsEntityLiving.onDeath(source);
                } else {
                    s = this.getHurtSound();

                    if (flag && s != null) {
                        this.nmsEntityLiving.playSound(s, this.getSoundVolume(), this.getSoundPitch());
                    }
                }

                return true;
            }
        }
    }

    @Override
    public boolean damageEntityHook(DamageSource damageSource, float damage) {
        if (!this.nmsEntityLiving.isEntityInvulnerable(damageSource)) {
            final boolean human = this.nmsEntityLiving instanceof EntityPlayer;
            // apply forge damage hook
            damage = applyModDamage(this.nmsEntityLiving, damageSource, damage);
            float originalDamage = damage; // set after forge hook.
            if (damage <= 0) {
                damage = 0;
            }

            List<Tuple<DamageModifier, Function<? super Double, Double>>> originalFunctions = new ArrayList<>();
            Optional<Tuple<DamageModifier, Function<? super Double, Double>>> hardHatFunction =
                DamageEventHandler.createHardHatModifier(this.nmsEntityLiving, damageSource);
            Optional<Tuple<DamageModifier, Function<? super Double, Double>>> blockingFunction =
                DamageEventHandler.createBlockingModifier(this.nmsEntityLiving, damageSource);
            Optional<List<Tuple<DamageModifier, Function<? super Double, Double>>>> armorFunction =
                provideArmorModifiers(this.nmsEntityLiving, damageSource, damage);
            Optional<Tuple<DamageModifier, Function<? super Double, Double>>> resistanceFunction =
                DamageEventHandler.createResistanceModifier(this.nmsEntityLiving, damageSource);
            Optional<List<Tuple<DamageModifier, Function<? super Double, Double>>>> armorEnchantments =
                DamageEventHandler.createEnchantmentModifiers(this.nmsEntityLiving, damageSource);
            Optional<Tuple<DamageModifier, Function<? super Double, Double>>> absorptionFunction =
                DamageEventHandler.createAbsorptionModifier(this.nmsEntityLiving, damageSource);

            if (hardHatFunction.isPresent()) {
                originalFunctions.add(hardHatFunction.get());
            }

            if (blockingFunction.isPresent()) {
                originalFunctions.add(blockingFunction.get());
            }

            if (armorFunction.isPresent()) {
                originalFunctions.addAll(armorFunction.get());
            }

            if (resistanceFunction.isPresent()) {
                originalFunctions.add(resistanceFunction.get());
            }

            if (armorEnchantments.isPresent()) {
                originalFunctions.addAll(armorEnchantments.get());
            }

            if (absorptionFunction.isPresent()) {
                originalFunctions.add(absorptionFunction.get());
            }
            final Cause cause = DamageEventHandler.generateCauseFor(damageSource);

            DamageEntityEvent event = SpongeEventFactory.createDamageEntityEvent(cause, originalFunctions,
                         (Entity) this.nmsEntityLiving, originalDamage);
            Sponge.getEventManager().post(event);
            if (event.isCancelled()) {
                return false;
            }

            damage = (float) event.getFinalDamage();

            // Helmet
            if ((damageSource instanceof FallingBlockDamageSource) && this.nmsEntityLiving.getEquipmentInSlot(4) != null) {
                this.nmsEntityLiving.getEquipmentInSlot(4).damageItem(
                    (int) (event.getBaseDamage() * 4.0F + this.rand.nextFloat() * event.getBaseDamage() * 2.0F), this.nmsEntityLiving);
            }

            // Armor
            if (!damageSource.isUnblockable()) {
                for (Tuple<DamageModifier, Function<? super Double, Double>> modifier : event.getModifiers()) {
                    applyArmorDamage(this.nmsEntityLiving, damageSource, event, modifier.getFirst());
                }
            }

            double absorptionModifier = 0;
            if (absorptionFunction.isPresent()) {
                absorptionModifier = event.getDamage(absorptionFunction.get().getFirst());
            }

            this.nmsEntityLiving.setAbsorptionAmount(Math.max(this.nmsEntityLiving.getAbsorptionAmount() + (float) absorptionModifier, 0.0F));
            if (damage != 0.0F) {
                if (human) {
                    ((EntityPlayer) this.nmsEntityLiving).addExhaustion(damageSource.getHungerDamage());
                }
                float f2 = this.nmsEntityLiving.getHealth();

                this.nmsEntityLiving.setHealth(f2 - damage);
                this.nmsEntityLiving.getCombatTracker().trackDamage(damageSource, f2, damage);

                if (human) {
                    return true;
                }

                this.nmsEntityLiving.setAbsorptionAmount(this.nmsEntityLiving.getAbsorptionAmount() - damage);
            }
            return true;
        }
        return false;
    }

    @Override
    public float applyModDamage(EntityLivingBase entityLivingBase, DamageSource source, float damage) {
        return damage;
    }

    @Override
    public Optional<List<Tuple<DamageModifier, Function<? super Double, Double>>>> provideArmorModifiers(EntityLivingBase entityLivingBase,
                                                                                                         DamageSource source, double damage) {
        return DamageEventHandler.createArmorModifiers(entityLivingBase, source, damage);
    }

    @Override
    public void applyArmorDamage(EntityLivingBase entityLivingBase, DamageSource source, DamageEntityEvent entityEvent, DamageModifier modifier) {
        Optional<DamageObject> optional = modifier.getCause().first(DamageObject.class);
        if (optional.isPresent()) {
            DamageEventHandler.acceptArmorModifier(this.nmsEntityLiving, source, modifier, entityEvent.getDamage(modifier));
        }
    }

    @Override
    public boolean hookModAttack(EntityLivingBase entityLivingBase, DamageSource source, float amount) {
        return true;
    }

    /**
     * @author gabizou - January 4th, 2016
     *
     * This allows invisiblity to ignore entity collisions.
     */
    @Overwrite
    public boolean canBeCollidedWith() {
        return !(this.isReallyREALLYInvisible() && this.ignoresCollision()) && !this.isDead;
    }

}
