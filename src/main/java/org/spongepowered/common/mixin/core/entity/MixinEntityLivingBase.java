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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.BaseAttributeMap;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.CombatTracker;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.entity.DamageableData;
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
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeHealthData;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.data.value.mutable.SpongeOptionalValue;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.living.human.EntityHuman;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.damage.DamageEventHandler;
import org.spongepowered.common.event.damage.DamageObject;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.EntityPhase;
import org.spongepowered.common.event.tracking.phase.TrackingPhases;
import org.spongepowered.common.interfaces.entity.IMixinEntityLivingBase;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

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

    private int maxAir = 300;
    private DamageSource lastDamageSource;

    @Shadow public int maxHurtResistantTime;
    @Shadow public int hurtTime;
    @Shadow public int maxHurtTime;
    @Shadow public int deathTime;
    @Shadow public boolean potionsNeedUpdate;
    @Shadow public CombatTracker _combatTracker;
    @Shadow public EntityLivingBase entityLivingToAttack;
    @Shadow public float attackedAtYaw;
    @Shadow public float limbSwingAmount;
    @Shadow protected BaseAttributeMap attributeMap;
    @Shadow protected ItemStack[] previousEquipment;
    @Shadow protected int entityAge;
    @Shadow protected int recentlyHit;
    @Shadow protected float lastDamage;
    @Shadow @Nullable protected EntityPlayer attackingPlayer;
    @Shadow protected abstract void damageArmor(float p_70675_1_);
    @Shadow protected abstract void setBeenAttacked();
    @Shadow protected abstract String getDeathSound();
    @Shadow protected abstract float getSoundVolume();
    @Shadow protected abstract float getSoundPitch();
    @Shadow @Nullable protected abstract String getHurtSound();
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
    @Shadow @Nullable public abstract ItemStack getEquipmentInSlot(int slotIn);
    @Shadow protected abstract void applyEntityAttributes();
    @Shadow protected abstract void onDeathUpdate();
    @Shadow public abstract void onDeath(DamageSource cause);
    @Shadow public abstract void knockBack(net.minecraft.entity.Entity entityIn, float p_70653_2_, double p_70653_3_, double p_70653_5_);
    @Shadow public abstract void setRevengeTarget(EntityLivingBase livingBase);
    @Shadow public abstract void setAbsorptionAmount(float amount);
    @Shadow public abstract float getAbsorptionAmount();
    @Shadow public abstract CombatTracker getCombatTracker();

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
        this.lastDamageSource = source;
        if (source == null) {
            Thread.dumpStack();
        }
        if (!hookModAttack((EntityLivingBase) (Object) this, source, amount))
            return false;
        if (this.isEntityInvulnerable(source)) {
            return false;
        } else if (this.worldObj.isRemote) {
            return false;
        } else {
            this.entityAge = 0;

            if (this.getHealth() <= 0.0F) {
                return false;
            } else if (source.isFireDamage() && this.isPotionActive(Potion.fireResistance)) {
                return false;
            } else {
                // Sponge - ignore as this is handled in our damageEntityHook
//                if (false && (source == DamageSource.anvil || source == DamageSource.fallingBlock)
//                    && this.getEquipmentInSlot(4) != null) {
//                    this.getEquipmentInSlot(4).damageItem((int) (amount * 4.0F + this.rand.nextFloat() * amount * 2.0F),
//                            (EntityLivingBase) (Object) this);
//                    amount *= 0.75F;
//                }
                // Sponge End

                this.limbSwingAmount = 1.5F;
                boolean flag = true;

                if ((float) this.hurtResistantTime > (float) this.maxHurtResistantTime / 2.0F) {
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
                    this.hurtResistantTime = this.maxHurtResistantTime;
                    // this.damageEntity(source, amount); // handled above
                    // Sponge end
                    this.hurtTime = this.maxHurtTime = 10;
                }

                this.attackedAtYaw = 0.0F;
                net.minecraft.entity.Entity entity = source.getEntity();

                if (entity != null) {
                    if (entity instanceof EntityLivingBase) {
                        this.setRevengeTarget((EntityLivingBase) entity);
                    }

                    if (entity instanceof EntityPlayer) {
                        this.recentlyHit = 100;
                        this.attackingPlayer = (EntityPlayer) entity;
                    } else if (entity instanceof net.minecraft.entity.passive.EntityTameable) {
                        net.minecraft.entity.passive.EntityTameable entityWolf = (net.minecraft.entity.passive.EntityTameable) entity;

                        if (entityWolf.isTamed()) {
                            this.recentlyHit = 100;
                            this.attackingPlayer = null;
                        }
                    }
                }

                if (flag) {
                    this.worldObj.setEntityState((EntityLivingBase) (Object) this, (byte) 2);

                    if (source != DamageSource.drown) {
                        this.setBeenAttacked();
                    }

                    if (entity != null) {
                        double d1 = entity.posX - this.posX;
                        double d0;

                        for (d0 = entity.posZ - this.posZ; d1 * d1 + d0 * d0 < 1.0E-4D; d0 = (Math.random() - Math.random()) * 0.01D) {
                            d1 = (Math.random() - Math.random()) * 0.01D;
                        }

                        this.attackedAtYaw = (float) (Math.atan2(d0, d1) * 180.0D / Math.PI - (double) this.rotationYaw);
                        this.knockBack(entity, amount, d1, d0);
                    } else {
                        this.attackedAtYaw = (float) ((int) (Math.random() * 2.0D) * 180);
                    }
                }

                String s;

                if (this.getHealth() <= 0.0F) {
                    s = this.getDeathSound();

                    if (flag && s != null) {
                        this.playSound(s, this.getSoundVolume(), this.getSoundPitch());
                    }

                    // Sponge Start - notify the cause tracker
                    final CauseTracker causeTracker = ((IMixinWorldServer) this.getWorld()).getCauseTracker();
                    final boolean tracksEntitySpecificDrops = causeTracker.getStack().peekState().tracksEntitySpecificDrops();
                    if (tracksEntitySpecificDrops) {
                        causeTracker.switchToPhase(TrackingPhases.ENTITY, EntityPhase.State.DEATH_DROPS_SPAWNING, PhaseContext.start()
                                .add(NamedCause.source(this))
                                .add(NamedCause.of(InternalNamedCauses.General.DAMAGE_SOURCE, source))
                                .add(this.getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR).map(NamedCause::owner).orElse(null))
                                .add(this.getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_NOTIFIER).map(NamedCause::notifier).orElse(null))
                                .addCaptures()
                                .complete());
                    }
                    this.onDeath(source);
                    if (tracksEntitySpecificDrops) {
                        causeTracker.completePhase();
                    }
                    // Sponge End
                } else {
                    s = this.getHurtSound();

                    if (flag && s != null) {
                        this.playSound(s, this.getSoundVolume(), this.getSoundPitch());
                    }
                }

                return true;
            }
        }
    }

    @Override
    public boolean damageEntityHook(DamageSource damageSource, float damage) {
        if (!this.isEntityInvulnerable(damageSource)) {
            final boolean human = (Object) this instanceof EntityPlayer;
            // apply forge damage hook
            damage = applyModDamage((EntityLivingBase) (Object) this, damageSource, damage);
            float originalDamage = damage; // set after forge hook.
            if (damage <= 0) {
                damage = 0;
            }

            List<Tuple<DamageModifier, Function<? super Double, Double>>> originalFunctions = new ArrayList<>();
            Optional<Tuple<DamageModifier, Function<? super Double, Double>>> hardHatFunction =
                DamageEventHandler.createHardHatModifier((EntityLivingBase) (Object) this, damageSource);
            Optional<Tuple<DamageModifier, Function<? super Double, Double>>> blockingFunction =
                DamageEventHandler.createBlockingModifier((EntityLivingBase) (Object) this, damageSource);
            Optional<List<Tuple<DamageModifier, Function<? super Double, Double>>>> armorFunction =
                provideArmorModifiers((EntityLivingBase) (Object) this, damageSource, damage);
            Optional<Tuple<DamageModifier, Function<? super Double, Double>>> resistanceFunction =
                DamageEventHandler.createResistanceModifier((EntityLivingBase) (Object) this, damageSource);
            Optional<List<Tuple<DamageModifier, Function<? super Double, Double>>>> armorEnchantments =
                DamageEventHandler.createEnchantmentModifiers((EntityLivingBase) (Object) this, damageSource);
            Optional<Tuple<DamageModifier, Function<? super Double, Double>>> absorptionFunction =
                DamageEventHandler.createAbsorptionModifier((EntityLivingBase) (Object) this, damageSource);

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

            DamageEntityEvent event = SpongeEventFactory.createDamageEntityEvent(cause, originalFunctions, this, originalDamage);
            Sponge.getEventManager().post(event);
            if (event.isCancelled()) {
                return false;
            }

            damage = (float) event.getFinalDamage();

            // Helmet
            if ((damageSource instanceof FallingBlockDamageSource) && this.getEquipmentInSlot(4) != null) {
                this.getEquipmentInSlot(4).damageItem(
                    (int) (event.getBaseDamage() * 4.0F + this.rand.nextFloat() * event.getBaseDamage() * 2.0F), (EntityLivingBase) (Object) this);
            }

            // Armor
            if (!damageSource.isUnblockable()) {
                for (Tuple<DamageModifier, Function<? super Double, Double>> modifier : event.getModifiers()) {
                    applyArmorDamage((EntityLivingBase) (Object) this, damageSource, event, modifier.getFirst());
                }
            }

            double absorptionModifier = 0;
            if (absorptionFunction.isPresent()) {
                absorptionModifier = event.getDamage(absorptionFunction.get().getFirst());
            }

            this.setAbsorptionAmount(Math.max(this.getAbsorptionAmount() + (float) absorptionModifier, 0.0F));
            if (damage != 0.0F) {
                if (human) {
                    ((EntityPlayer) (Object) this).addExhaustion(damageSource.getHungerDamage());
                }
                float f2 = this.getHealth();

                this.setHealth(f2 - damage);
                this.getCombatTracker().trackDamage(damageSource, f2, damage);

                if (human) {
                    return true;
                }

                this.setAbsorptionAmount(this.getAbsorptionAmount() - damage);
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
            DamageEventHandler.acceptArmorModifier((EntityLivingBase) (Object) this, source, modifier, entityEvent.getDamage(modifier));
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
        return !(this.isVanished() && this.ignoresCollision()) && !this.isDead;
    }

    @Override
    public DamageSource getLastDamageSource() {
        return this.lastDamageSource;
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

    @Redirect(method = "onEntityUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;onDeathUpdate()V"))
    private void causeTrackDeathUpdate(EntityLivingBase entityLivingBase) {
        if (!entityLivingBase.worldObj.isRemote) {
            final CauseTracker causeTracker = ((IMixinWorldServer) entityLivingBase.worldObj).getCauseTracker();
            causeTracker.switchToPhase(TrackingPhases.ENTITY, EntityPhase.State.DEATH_UPDATE, PhaseContext.start()
                    .addCaptures()
                    .add(NamedCause.source(entityLivingBase))
                    .complete());
            ((IMixinEntityLivingBase) entityLivingBase).onSpongeDeathUpdate();
            causeTracker.completePhase();
        }
    }


    @Override
    public void onSpongeDeathUpdate() {
        this.onDeathUpdate();
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
