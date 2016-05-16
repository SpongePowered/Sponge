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

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.BaseAttributeMap;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.CombatEntry;
import net.minecraft.util.CombatTracker;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.entity.HealthData;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.data.value.mutable.OptionalValue;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.damage.DamageModifier;
import org.spongepowered.api.event.cause.entity.damage.source.FallingBlockDamageSource;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeHealthData;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.data.value.mutable.SpongeOptionalValue;
import org.spongepowered.common.entity.living.human.EntityHuman;
import org.spongepowered.common.event.CauseTracker;
import org.spongepowered.common.event.DamageEventHandler;
import org.spongepowered.common.event.DamageObject;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.entity.IMixinEntityLivingBase;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nullable;

@SuppressWarnings("rawtypes")
@NonnullByDefault
@Mixin(value = EntityLivingBase.class, priority = 999)
public abstract class MixinEntityLivingBase extends MixinEntity implements Living, IMixinEntityLivingBase {

    private EntityLivingBase mcEntityLiving = (EntityLivingBase) (Object) this;
    private int maxAir = 300;

    @Shadow public int maxHurtResistantTime;
    @Shadow public int hurtTime;
    @Shadow public int maxHurtTime;
    @Shadow public int deathTime;
    @Shadow public boolean potionsNeedUpdate;
    @Shadow public CombatTracker _combatTracker;
    @Shadow public EntityLivingBase entityLivingToAttack;
    @Shadow protected BaseAttributeMap attributeMap;
    @Shadow protected ItemStack[] previousEquipment;
    @Shadow protected int entityAge;
    @Shadow protected int recentlyHit;
    @Shadow protected float lastDamage;
    @Shadow protected EntityPlayer attackingPlayer;
    @Shadow protected int scoreValue;
    @Shadow public boolean dead;
    @Shadow public float attackedAtYaw;

    @Shadow protected abstract void damageArmor(float p_70675_1_);
    @Shadow protected abstract void setBeenAttacked();
    @Shadow protected abstract String getDeathSound();
    @Shadow protected abstract float getSoundVolume();
    @Shadow protected abstract float getSoundPitch();
    @Shadow protected abstract String getHurtSound();
    @Shadow public abstract void setHealth(float health);
    @Shadow public abstract void addPotionEffect(net.minecraft.potion.PotionEffect potionEffect);
    @Shadow public abstract void removePotionEffect(int id);
    @Shadow protected abstract void markPotionsDirty();
    @Shadow public abstract void setCurrentItemOrArmor(int slotIn, ItemStack stack);
    @Shadow public abstract void clearActivePotions();
    @Shadow public abstract void setLastAttacker(net.minecraft.entity.Entity entity);
    @Shadow public abstract boolean isPotionActive(Potion potion);
    @Shadow public abstract float getHealth();
    @Shadow public abstract float getMaxHealth();
    @Shadow public abstract float getRotationYawHead();
    @Shadow public abstract void setRotationYawHead(float rotation);
    @Shadow public abstract Collection getActivePotionEffects();
    @Shadow @Nullable public abstract EntityLivingBase getLastAttacker();
    @Shadow public abstract IAttributeInstance getEntityAttribute(IAttribute attribute);
    @Shadow public abstract ItemStack getEquipmentInSlot(int slotIn);
    @Shadow protected abstract void applyEntityAttributes();
    @Shadow public abstract void setSprinting(boolean sprinting);
    @Shadow public abstract boolean isOnLadder();
    @Shadow public abstract CombatTracker getCombatTracker();
    @Shadow public abstract EntityLivingBase getAttackingEntity();
    @Shadow protected abstract void dropFewItems(boolean wasRecentlyHit, int lootingModifier);
    @Shadow protected abstract void dropEquipment(boolean wasRecentlyHit, int lootingModifier);
    @Shadow protected abstract boolean canDropLoot();
    @Shadow protected abstract void addRandomDrop();

    @Override
    public Vector3d getHeadRotation() {
        // pitch, yaw, roll -- Minecraft does not currently support head roll
        return new Vector3d(getRotation().getX(), getRotationYawHead(), 0);
    }

    @Override
    public void setHeadRotation(Vector3d rotation) {
        setRotation(getRotation().mul(0, 1, 1).add(rotation.getX(), 0, 0));
        setRotationYawHead((float) rotation.getY());
    }

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

    @Redirect(method = "onDeathUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntityInWorld(Lnet/minecraft/entity/Entity;)Z"))
    public boolean onEntityDeathUpdate(net.minecraft.world.World world, net.minecraft.entity.Entity entity) {
        List<NamedCause> namedCauses = new ArrayList<>();
        EntitySpawnCause spawnCause = EntitySpawnCause.builder()
                .entity((Entity) this.mcEntityLiving)
                .type(InternalSpawnTypes.EXPERIENCE)
                .build();
        namedCauses.add(NamedCause.source(spawnCause));
        CombatEntry entry = getCombatTracker().func_94544_f();
        if (entry != null) {
            if (entry.damageSrc != null) {
                namedCauses.add(NamedCause.of("LastDamageSource", entry.damageSrc));
            }
        }
        return ((org.spongepowered.api.world.World) this.worldObj).spawnEntity((Entity) entity, Cause.of(namedCauses));
    }

    /**
     * @author blood - May 12th, 2016
     *
     * @reason SpongeForge requires an overwrite so we do it here instead. This handles all living entity death events
               (except players).
     */
    @Overwrite
    public void onDeath(DamageSource cause) {
        if (SpongeCommonEventFactory.callDestructEntityEventDeath((EntityLivingBase)(Object) this, cause) == null) {
            return;
        }

        net.minecraft.entity.Entity entity = cause.getEntity();
        EntityLivingBase entitylivingbase = this.getAttackingEntity();

        if (this.scoreValue >= 0 && entitylivingbase != null) {
            entitylivingbase.addToPlayerScore((EntityLivingBase)(Object) this, this.scoreValue);
        }

        if (entity != null) {
            entity.onKillEntity((EntityLivingBase)(Object) this);
        }

        this.dead = true;
        this.getCombatTracker().reset();

        if (!this.worldObj.isRemote) {
            int i = 0;

            if (entity instanceof EntityPlayer) {
                i = EnchantmentHelper.getLootingModifier((EntityLivingBase)entity);
            }

            this.captureItemDrops = true;
            this.capturedItemDrops.clear();

            if (this.canDropLoot() && this.worldObj.getGameRules().getBoolean("doMobLoot")) {
                this.dropFewItems(this.recentlyHit > 0, i);
                this.dropEquipment(this.recentlyHit > 0, i);

                if (this.recentlyHit > 0 && this.rand.nextFloat() < 0.025F + (float)i * 0.01F) {
                    this.addRandomDrop();
                }
            }

            this.captureItemDrops = false;
            if (this.capturedItemDrops.size() > 0) {
                IMixinWorld spongeWorld = (IMixinWorld) this.worldObj;
                final CauseTracker causeTracker = spongeWorld.getCauseTracker();
                causeTracker.setIgnoreSpawnEvents(true);
                if (!SpongeCommonEventFactory.callDropItemEventDestruct(this.mcEntityLiving, cause, this.capturedItemDrops).isCancelled()) {
                    for (EntityItem item : this.capturedItemDrops) {
                        worldObj.spawnEntityInWorld(item);
                    }
                }
                causeTracker.setIgnoreSpawnEvents(false);
            }
        }

        // Don't send the state if this is a human. Fixes ghost items on client.
        if (!(this.mcEntityLiving instanceof EntityHuman)) {
            this.worldObj.setEntityState(this.mcEntityLiving, (byte)3);
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
     * @reason This shouldn't be used internally but a mod may still call it so we simply reroute to our hook.
     */
    @Overwrite
    protected void damageEntity(DamageSource damageSource, float damage) {
        this.damageEntityHook(damageSource, damage);
    }

    /**
     * @author bloodmc - November 22, 2015
     * @reason Reroute damageEntity calls to our hook in order to prevent damage.
     */
    @SuppressWarnings("unused")
    @Override
    @Overwrite
    public boolean attackEntityFrom(DamageSource source, float amount) {
        this.lastDamageSource = source;
        if (source == null) {
            Thread.dumpStack();
        }
        if (!hookModAttack(this.mcEntityLiving, source, amount))
            return false;
        if (this.mcEntityLiving.isEntityInvulnerable(source)) {
            return false;
        } else if (this.worldObj.isRemote) {
            return false;
        } else {
            this.entityAge = 0;

            if (this.mcEntityLiving.getHealth() <= 0.0F) {
                return false;
            } else if (source.isFireDamage() && this.mcEntityLiving.isPotionActive(Potion.fireResistance)) {
                return false;
            } else {
                // Sponge - ignore as this is handled in our damageEntityHook
                if (false && (source == DamageSource.anvil || source == DamageSource.fallingBlock)
                    && this.mcEntityLiving.getEquipmentInSlot(4) != null) {
                    this.mcEntityLiving.getEquipmentInSlot(4).damageItem((int) (amount * 4.0F + this.rand.nextFloat() * amount * 2.0F),
                                                                          this.mcEntityLiving);
                    amount *= 0.75F;
                }

                this.mcEntityLiving.limbSwingAmount = 1.5F;
                boolean flag = true;

                if ((float) this.hurtResistantTime > (float) this.mcEntityLiving.maxHurtResistantTime / 2.0F) {
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
                    this.hurtResistantTime = this.mcEntityLiving.maxHurtResistantTime;
                    // this.damageEntity(source, amount); // handled above
                    // Sponge end
                    this.mcEntityLiving.hurtTime = this.mcEntityLiving.maxHurtTime = 10;
                }

                this.mcEntityLiving.attackedAtYaw = 0.0F;
                net.minecraft.entity.Entity entity = source.getEntity();

                if (entity != null) {
                    if (entity instanceof EntityLivingBase) {
                        this.mcEntityLiving.setRevengeTarget((EntityLivingBase) entity);
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
                    this.worldObj.setEntityState(this.mcEntityLiving, (byte) 2);

                    if (source != DamageSource.drown) {
                        this.setBeenAttacked();
                    }

                    if (entity != null) {
                        double d1 = entity.posX - this.posX;
                        double d0;

                        for (d0 = entity.posZ - this.posZ; d1 * d1 + d0 * d0 < 1.0E-4D; d0 = (Math.random() - Math.random()) * 0.01D) {
                            d1 = (Math.random() - Math.random()) * 0.01D;
                        }

                        this.mcEntityLiving.attackedAtYaw = (float) (Math.atan2(d0, d1) * 180.0D / Math.PI - (double) this.rotationYaw);
                        this.mcEntityLiving.knockBack(entity, amount, d1, d0);
                    } else {
                        this.mcEntityLiving.attackedAtYaw = (float) ((int) (Math.random() * 2.0D) * 180);
                    }
                }

                String s;

                if (this.mcEntityLiving.getHealth() <= 0.0F) {
                    s = this.getDeathSound();

                    if (flag && s != null) {
                        this.mcEntityLiving.playSound(s, this.getSoundVolume(), this.getSoundPitch());
                    }

                    this.mcEntityLiving.onDeath(source);
                } else {
                    s = this.getHurtSound();

                    if (flag && s != null) {
                        this.mcEntityLiving.playSound(s, this.getSoundVolume(), this.getSoundPitch());
                    }
                }

                return true;
            }
        }
    }

    @Override
    public boolean damageEntityHook(DamageSource damageSource, float damage) {
        if (!this.mcEntityLiving.isEntityInvulnerable(damageSource)) {
            final boolean human = this.mcEntityLiving instanceof EntityPlayer;
            // apply forge damage hook
            damage = applyModDamage(this.mcEntityLiving, damageSource, damage);
            float originalDamage = damage; // set after forge hook.
            if (damage <= 0) {
                damage = 0;
            }

            List<Tuple<DamageModifier, Function<? super Double, Double>>> originalFunctions = new ArrayList<>();
            Optional<Tuple<DamageModifier, Function<? super Double, Double>>> hardHatFunction =
                DamageEventHandler.createHardHatModifier(this.mcEntityLiving, damageSource);
            Optional<Tuple<DamageModifier, Function<? super Double, Double>>> blockingFunction =
                DamageEventHandler.createBlockingModifier(this.mcEntityLiving, damageSource);
            Optional<List<Tuple<DamageModifier, Function<? super Double, Double>>>> armorFunction =
                provideArmorModifiers(this.mcEntityLiving, damageSource, damage);
            Optional<Tuple<DamageModifier, Function<? super Double, Double>>> resistanceFunction =
                DamageEventHandler.createResistanceModifier(this.mcEntityLiving, damageSource);
            Optional<List<Tuple<DamageModifier, Function<? super Double, Double>>>> armorEnchantments =
                DamageEventHandler.createEnchantmentModifiers(this.mcEntityLiving, damageSource);
            Optional<Tuple<DamageModifier, Function<? super Double, Double>>> absorptionFunction =
                DamageEventHandler.createAbsorptionModifier(this.mcEntityLiving, damageSource);

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
                         (Entity) this.mcEntityLiving, originalDamage);
            Sponge.getEventManager().post(event);
            if (event.isCancelled()) {
                return false;
            }

            damage = (float) event.getFinalDamage();

            // Helmet
            if ((damageSource instanceof FallingBlockDamageSource) && this.mcEntityLiving.getEquipmentInSlot(4) != null) {
                this.mcEntityLiving.getEquipmentInSlot(4).damageItem(
                    (int) (event.getBaseDamage() * 4.0F + this.rand.nextFloat() * event.getBaseDamage() * 2.0F), this.mcEntityLiving);
            }

            // Armor
            if (!damageSource.isUnblockable()) {
                for (Tuple<DamageModifier, Function<? super Double, Double>> modifier : event.getModifiers()) {
                    applyArmorDamage(this.mcEntityLiving, damageSource, event, modifier.getFirst());
                }
            }

            double absorptionModifier = 0;
            if (absorptionFunction.isPresent()) {
                absorptionModifier = event.getDamage(absorptionFunction.get().getFirst());
            }

            this.mcEntityLiving.setAbsorptionAmount(Math.max(this.mcEntityLiving.getAbsorptionAmount() + (float) absorptionModifier, 0.0F));
            if (damage != 0.0F) {
                if (human) {
                    ((EntityPlayer) this.mcEntityLiving).addExhaustion(damageSource.getHungerDamage());
                }
                float f2 = this.mcEntityLiving.getHealth();

                this.mcEntityLiving.setHealth(f2 - damage);
                this.mcEntityLiving.getCombatTracker().trackDamage(damageSource, f2, damage);

                if (human) {
                    return true;
                }

                this.mcEntityLiving.setAbsorptionAmount(this.mcEntityLiving.getAbsorptionAmount() - damage);
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
            DamageEventHandler.acceptArmorModifier(this.mcEntityLiving, source, modifier, entityEvent.getDamage(modifier));
        }
    }

    @Override
    public boolean hookModAttack(EntityLivingBase entityLivingBase, DamageSource source, float amount) {
        return true;
    }

    /**
     * @author gabizou - January 4th, 2016
     * @reason This allows invisiblity to ignore entity collisions.
     */
    @Overwrite
    public boolean canBeCollidedWith() {
        return !(this.isVanished() && this.ignoresCollision()) && !this.isDead;
    }

    @Override
    public int getRecentlyHit() {
        return this.recentlyHit;
    }

    @Redirect(method = "updateFallState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDIDDDD[I)V"))
    private void spongeSpawnParticleForFallState(WorldServer worldServer, EnumParticleTypes particleTypes, double xCoord, double yCoord,
            double zCoord, int numberOfParticles, double xOffset, double yOffset, double zOffset, double particleSpeed, int... extraArgs) {
        if (!this.isVanished()) {
            worldServer.spawnParticle(particleTypes, xCoord, yCoord, zCoord, numberOfParticles, xOffset, yOffset, zOffset, particleSpeed, extraArgs);
        }

    }

    @Inject(method = "onItemPickup", at = @At("HEAD"))
    public void onEntityItemPickup(net.minecraft.entity.Entity entityItem, int unused, CallbackInfo ci) {
        if (!this.worldObj.isRemote) {
            IMixinEntity spongeEntity = (IMixinEntity) entityItem;
            spongeEntity.setDestructCause(Cause.of(NamedCause.of("PickedUp", this)));
        }
    }

    // Data delegated methods


    @Override
    public HealthData getHealthData() {
        return new SpongeHealthData(this.getHealth(), this.getMaxHealth());
    }

    @Override
    public MutableBoundedValue<Double> health() {
        return SpongeValueFactory.boundedBuilder(Keys.HEALTH)
                .minimum(0D)
                .maximum((double) this.getMaxHealth())
                .defaultValue((double) this.getMaxHealth())
                .actualValue((double) this.getHealth())
                .build();
    }

    @Override
    public MutableBoundedValue<Double> maxHealth() {
        return SpongeValueFactory.boundedBuilder(Keys.MAX_HEALTH)
                .minimum(1D)
                .maximum((double) Float.MAX_VALUE)
                .defaultValue(20D)
                .actualValue((double) this.getMaxHealth())
                .build();
    }

    // TODO uncomment when the processor is implemented
//    @Override
//    public DamageableData getMortalData() {
//        return null;
//    }

    @Override
    public OptionalValue<Living> lastAttacker() {
        return new SpongeOptionalValue<>(Keys.LAST_ATTACKER, Optional.ofNullable((Living) this.getLastAttacker()));
    }

    @Override
    public OptionalValue<Double> lastDamage() {
        return new SpongeOptionalValue<>(Keys.LAST_DAMAGE, Optional.ofNullable(this.getLastAttacker() == null ? null : (double) this.lastDamage));
    }

    @Override
    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
        super.supplyVanillaManipulators(manipulators);
        manipulators.add(getHealthData());
    }
}
