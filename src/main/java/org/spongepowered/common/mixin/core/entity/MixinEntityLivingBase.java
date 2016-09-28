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
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.CombatTracker;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.entity.HealthData;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.data.value.mutable.OptionalValue;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.damage.DamageModifier;
import org.spongepowered.api.event.cause.entity.damage.source.FallingBlockDamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
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
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.living.human.EntityHuman;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.damage.DamageEventHandler;
import org.spongepowered.common.event.damage.DamageObject;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.phase.entity.EntityPhase;
import org.spongepowered.common.interfaces.entity.IMixinEntityLivingBase;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.registry.type.event.DamageSourceRegistryModule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

import javax.annotation.Nullable;

@SuppressWarnings("rawtypes")
@NonnullByDefault
@Mixin(value = EntityLivingBase.class, priority = 999)
public abstract class MixinEntityLivingBase extends MixinEntity implements Living, IMixinEntityLivingBase {

    private static final String WORLD_SPAWN_PARTICLE = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V";

    private int maxAir = 300;

    @Shadow public int maxHurtResistantTime;
    @Shadow public int hurtTime;
    @Shadow public int maxHurtTime;
    @Shadow public int deathTime;
    @Shadow protected int scoreValue;
    @Shadow public float attackedAtYaw;
    @Shadow public float limbSwingAmount;
    @Shadow public boolean potionsNeedUpdate;
    @Shadow public boolean dead;
    @Shadow public CombatTracker _combatTracker;
    @Shadow public EntityLivingBase entityLivingToAttack;
    @Shadow protected AbstractAttributeMap attributeMap;
    @Shadow public ItemStack[] armorArray;
    @Shadow protected int entityAge;
    @Shadow protected int recentlyHit;
    @Shadow protected float lastDamage;
    @Shadow @Nullable protected EntityPlayer attackingPlayer;
    @Shadow protected ItemStack activeItemStack;
    // Empty body so that we can call super() in MixinEntityPlayer
    @Shadow public void stopActiveHand() {

    }

    @Shadow protected abstract void setBeenAttacked();
    @Shadow protected abstract SoundEvent getDeathSound();
    @Shadow protected abstract float getSoundVolume();
    @Shadow protected abstract float getSoundPitch();
    @Shadow protected abstract SoundEvent getHurtSound();
    @Shadow public abstract void setHealth(float health);
    @Shadow public abstract void addPotionEffect(net.minecraft.potion.PotionEffect potionEffect);
    @Shadow protected abstract void markPotionsDirty();
    @Shadow public abstract void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack);
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
    @Shadow @Nullable public abstract ItemStack getItemStackFromSlot(EntityEquipmentSlot slotIn);
    @Shadow protected abstract void applyEntityAttributes();
    @Shadow protected abstract void playHurtSound(net.minecraft.util.DamageSource p_184581_1_);
    @Shadow protected abstract void damageShield(float p_184590_1_);
    @Shadow public abstract void setActiveHand(EnumHand hand);
    @Shadow @Nullable public abstract ItemStack getHeldItem(EnumHand hand);
    @Shadow public abstract void setHeldItem(EnumHand hand, @Nullable ItemStack stack);
    @Shadow @Nullable public abstract ItemStack getHeldItemMainhand();
    @Shadow public abstract boolean isHandActive();
    @Shadow protected abstract void onDeathUpdate();
    @Shadow public abstract void knockBack(net.minecraft.entity.Entity entityIn, float p_70653_2_, double p_70653_3_, double p_70653_5_);
    @Shadow public abstract void setRevengeTarget(EntityLivingBase livingBase);
    @Shadow public abstract void setAbsorptionAmount(float amount);
    @Shadow public abstract float getAbsorptionAmount();
    @Shadow public abstract CombatTracker getCombatTracker();
    @Shadow public abstract void setSprinting(boolean sprinting);
    @Shadow public abstract boolean isOnLadder();
    @Shadow @Nullable public abstract EntityLivingBase getAttackingEntity();
    @Shadow protected abstract void dropLoot(boolean wasRecentlyHit, int lootingModifier, DamageSource source);
    @Shadow protected abstract boolean canDropLoot();
    @Shadow public abstract Random getRNG();
    @Shadow private boolean canBlockDamageSource(DamageSource p_184583_1_) {
        return false; // Shadowed
    }

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

    protected boolean tracksEntityDeaths = false;

    /**
     * @author blood - May 12th, 2016
     * @author gabizou - June 4th, 2016 - Update for 1.9.4 and Cause Tracker changes
     *
     * @reason SpongeForge requires an overwrite so we do it here instead. This handles all living entity death events
     * (except players). Note should be taken that normally, there are lists for drops captured, however, the drops
     * are retroactively captured by the CauseTracker and associated through the different phases, depending on
     * how they are handled, so no lists and flags on the entities themselves are needed as they are tracked through
     * the {@link PhaseContext} of the current {@link IPhaseState} at which this method is called. The compatibility
     * for Forge's events to throw are handled specially in SpongeForge.
     */
    @Overwrite
    public void onDeath(DamageSource cause) {
        // Sponge Start - Call our event, and forge's event
        // This will transitively call the forge event
        if (!SpongeCommonEventFactory.callDestructEntityEventDeath((EntityLivingBase) (Object) this, cause)) {
            return;
        }
        // Double check that the CauseTracker is already capturing the Death phase
        final CauseTracker causeTracker;
        if (!this.worldObj.isRemote) {
            causeTracker = ((IMixinWorldServer) this.worldObj).getCauseTracker();
            final PhaseData peek = causeTracker.getCurrentPhaseData();
            final IPhaseState state = peek.state;
            this.tracksEntityDeaths = CauseTracker.ENABLED && !causeTracker.getCurrentState().tracksEntityDeaths() && state != EntityPhase.State.DEATH;
            if (this.tracksEntityDeaths) {
                final PhaseContext context = PhaseContext.start()
                        .add(NamedCause.source(this))
                        .add(NamedCause.of(InternalNamedCauses.General.DAMAGE_SOURCE, cause));
                this.getNotifierUser().ifPresent(context::notifier);
                this.getCreatorUser().ifPresent(context::owner);
                causeTracker.switchToPhase(EntityPhase.State.DEATH, context
                        .addCaptures()
                        .addEntityDropCaptures()
                        .complete());
            }
        } else {
            causeTracker = null;
            this.tracksEntityDeaths = false;
        }
        // Sponge End
        if (this.dead) {
            // Sponge Start - ensure that we finish the tracker if necessary
            if (causeTracker != null && this.tracksEntityDeaths && !properlyOverridesOnDeathForCauseTrackerCompletion()) {
                causeTracker.completePhase();
            }
            // Sponge End
            return;
        }

        Entity entity = cause.getEntity();
        EntityLivingBase entitylivingbase = this.getAttackingEntity();

        if (this.scoreValue >= 0 && entitylivingbase != null) {
            entitylivingbase.addToPlayerScore((EntityLivingBase) (Object) this, this.scoreValue);
        }

        if (entity != null) {
            entity.onKillEntity((EntityLivingBase) (Object) this);
        }

        this.dead = true;
        this.getCombatTracker().reset();

        if (!this.worldObj.isRemote) {
            int i = 0;

            if (entity instanceof EntityPlayer) {
                i = EnchantmentHelper.getLootingModifier((EntityLivingBase) entity);
            }

            if (this.canDropLoot() && this.worldObj.getGameRules().getBoolean("doMobLoot")) {
                boolean flag = this.recentlyHit > 0;
                this.dropLoot(flag, i, cause);
            }

        }

        // Sponge Start - Don't send the state if this is a human. Fixes ghost items on client.
        if (!((EntityLivingBase) (Object) this instanceof EntityHuman)) {
            this.worldObj.setEntityState((EntityLivingBase) (Object) this, (byte) 3);
        }
        if (causeTracker != null && this.tracksEntityDeaths && !properlyOverridesOnDeathForCauseTrackerCompletion()) {
            this.tracksEntityDeaths = false;
            causeTracker.completePhase();
        }
        // Sponge End
    }

    @Redirect(method = "onDeath(Lnet/minecraft/util/DamageSource;)V", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/world/World;setEntityState(Lnet/minecraft/entity/Entity;B)V"))
    private void onDeathSendEntityState(World world, net.minecraft.entity.Entity self, byte state) {
        // Don't send the state if this is a human. Fixes ghost items on client.
        if (!((net.minecraft.entity.Entity) (Object) this instanceof EntityHuman)) {
            world.setEntityState(self, state);
        }
    }

    @Redirect(method = "applyPotionDamageCalculations", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;isPotionActive(Lnet/minecraft/potion/Potion;)Z") )
    private boolean onIsPotionActive(EntityLivingBase entityIn, Potion potion) {
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
     * @author gabizou - Updated April 11th, 2016 - Update for 1.9 changes
     *
     * @reason Reroute damageEntity calls to our hook in order to prevent damage.
     */
    @Override
    @Overwrite
    public boolean attackEntityFrom(DamageSource source, float amount) {
        // Sponge start - Add certain hooks for necessities
        this.lastDamageSource = source;
        if (source == null) {
            Thread.dumpStack();
        }
        // Sponge - This hook is for forge use mainly
        if (!hookModAttack((EntityLivingBase) (Object) this, source, amount))
            return false;
        // Sponge end
        if (this.isEntityInvulnerable(source)) {
            return false;
        } else if (this.worldObj.isRemote) {
            return false;
        } else {
            this.entityAge = 0;

            if (this.getHealth() <= 0.0F) {
                return false;
            } else if (source.isFireDamage() && this.isPotionActive(MobEffects.FIRE_RESISTANCE)) {
                return false;
            } else {
                // Sponge - ignore as this is handled in our damageEntityHookge
//                if (false && (source == DamageSource.anvil || source == DamageSource.fallingBlock)
//                    && this.getEquipmentInSlot(4) != null) {
//                    this.getEquipmentInSlot(4).damageItem((int) (amount * 4.0F + this.rand.nextFloat() * amount * 2.0F),
//                            (EntityLivingBase) (Object) this);
//                    amount *= 0.75F;
//                }
                // Sponge End

                boolean flag = false;

                if (amount > 0.0F && this.canBlockDamageSource(source)) {
                    this.damageShield(amount);

                    if (source.isProjectile()) {
                        amount = 0.0F;
                    } else {
                        amount *= 0.33F;

                        if (source.getSourceOfDamage() instanceof EntityLivingBase) {
                            ((EntityLivingBase) source.getSourceOfDamage()).knockBack((EntityLivingBase) (Object) this, 0.5F, this.posX - source.getSourceOfDamage().posX, this.posZ - source.getSourceOfDamage().posZ);
                        }
                    }

                    flag = true;
                }

                this.limbSwingAmount = 1.5F;
                boolean flag1 = true;

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
                    flag1 = false;
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
                    } else if (entity instanceof EntityWolf) {
                        EntityWolf entityWolf = (EntityWolf) entity;

                        if (entityWolf.isTamed()) {
                            this.recentlyHit = 100;
                            this.attackingPlayer = null;
                        }
                    }
                }

                if (flag1) {
                    if (flag) {
                        this.worldObj.setEntityState((EntityLivingBase) (Object) this, (byte) 29);
                    } else if (source instanceof net.minecraft.util.EntityDamageSource && ((net.minecraft.util.EntityDamageSource) source).getIsThornsDamage()) {
                        this.worldObj.setEntityState((EntityLivingBase) (Object) this, (byte) 33);
                    } else {
                        this.worldObj.setEntityState((EntityLivingBase) (Object) this, (byte) 2);
                    }

                    if (source != DamageSource.drown && (!flag || amount > 0.0F)) {
                        this.setBeenAttacked();
                    }

                    if (entity != null) {
                        double d1 = entity.posX - this.posX;
                        double d0;

                        for (d0 = entity.posZ - this.posZ; d1 * d1 + d0 * d0 < 1.0E-4D; d0 = (Math.random() - Math.random()) * 0.01D) {
                            d1 = (Math.random() - Math.random()) * 0.01D;
                        }

                        this.attackedAtYaw = (float) (MathHelper.atan2(d0, d1) * 180.0D / Math.PI - (double) this.rotationYaw);
                        this.knockBack(entity, 0.4F, d1, d0);
                    } else {
                        this.attackedAtYaw = (float) ((int) (Math.random() * 2.0D) * 180);
                    }
                }

                if (this.getHealth() <= 0.0F) {
                    SoundEvent soundevent = this.getDeathSound();

                    if (flag1 && soundevent != null) {
                        this.playSound(soundevent, this.getSoundVolume(), this.getSoundPitch());
                    }

                    // Sponge Start - notify the cause tracker
                    final CauseTracker causeTracker = ((IMixinWorldServer) this.getWorld()).getCauseTracker();
                    final boolean enterDeathPhase = CauseTracker.ENABLED && !causeTracker.getCurrentState().tracksEntityDeaths();
                    if (enterDeathPhase) {
                        final PhaseContext context = PhaseContext.start()
                                .add(NamedCause.source(this))
                                .add(NamedCause.of(InternalNamedCauses.General.DAMAGE_SOURCE, source));
                        this.getCreatorUser().ifPresent(context::owner);
                        this.getNotifierUser().ifPresent(context::notifier);
                        causeTracker.switchToPhase(EntityPhase.State.DEATH, context
                                .addCaptures()
                                .addEntityDropCaptures()
                                .complete());
                    }
                    this.onDeath(source);
                    if (enterDeathPhase) {
                        causeTracker.completePhase();
                    }
                    // Sponge End
                } else if (flag1) {
                    this.playHurtSound(source);
                }

                return !flag || amount > 0.0F;
            }
        }
    }

    /**
     * @author gabizou - January 4th, 2016
     * This is necessary for invisibility checks so that vanish players don't actually send the particle stuffs.
     */
    @Redirect(method = "updateItemUse", at = @At(value = "INVOKE", target = WORLD_SPAWN_PARTICLE))
    public void spawnItemParticle(World world, EnumParticleTypes particleTypes, double xCoord, double yCoord, double zCoord, double xOffset,
            double yOffset, double zOffset, int ... p_175688_14_) {
        if (!this.isVanished()) {
            this.worldObj.spawnParticle(particleTypes, xCoord, yCoord, zCoord, xOffset, yOffset, zOffset, p_175688_14_);
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
            if (damageSource != DamageSourceRegistryModule.IGNORED_DAMAGE_SOURCE) { // Basically, don't throw an event if it's our own damage source
                Sponge.getEventManager().post(event);
            }
            if (event.isCancelled()) {
                return false;
            }

            damage = (float) event.getFinalDamage();

            // Helmet
            final ItemStack mainHandItem = this.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
            if ((damageSource instanceof FallingBlockDamageSource) && mainHandItem != null) {
                mainHandItem.damageItem((int) (event.getBaseDamage() * 4.0F + this.rand.nextFloat() * event.getBaseDamage() * 2.0F), (EntityLivingBase) (Object) this);
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

    /**
     * @author Aaron1011 - August 15, 2016
     * @reason An overwrite avoids the need for a local-capture inject and two redirects
     */
    // TODO: Investigate mixing into setPositionAndUpdate to catch more teleports
    @Overwrite
    public boolean attemptTeleport(double x, double y, double z)
    {
        double d0 = this.posX;
        double d1 = this.posY;
        double d2 = this.posZ;
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        boolean flag = false;
        BlockPos blockpos = new BlockPos((Entity) (Object) this);
        World world = this.worldObj;
        Random random = this.getRNG();

        if (world.isBlockLoaded(blockpos))
        {
            boolean flag1 = false;

            while (!flag1 && blockpos.getY() > 0)
            {
                BlockPos blockpos1 = blockpos.down();
                IBlockState iblockstate = world.getBlockState(blockpos1);

                if (iblockstate.getMaterial().blocksMovement())
                {
                    flag1 = true;
                }
                else
                {
                    --this.posY;
                    blockpos = blockpos1;
                }
            }

            if (flag1)
            {
                // Sponge start
                if (!world.isRemote) {
                    Transform<org.spongepowered.api.world.World> fromTransform = this.getTransform().setPosition(new Vector3d(d0, d1, d2));
                    Transform<org.spongepowered.api.world.World> toTransform = this.getTransform().setPosition(new Vector3d(this.posX, this.posY, this.posZ));

                    MoveEntityEvent.Teleport event = EntityUtil.handleDisplaceEntityTeleportEvent((Entity) (Object) this, fromTransform, toTransform, false);
                    if (event.isCancelled()) {
                        this.posX = d0;
                        this.posY = d1;
                        this.posZ = d2;
                        return false;
                    }
                    Vector3d position = event.getToTransform().getPosition();
                    this.rotationYaw = (float) event.getToTransform().getYaw();
                    this.rotationPitch = (float) event.getToTransform().getPitch();
                    this.setPositionAndUpdate(position.getX(), position.getY(), position.getZ());
                } else {
                    this.setPositionAndUpdate(this.posX, this.posY, this.posZ);
                }
                // Sponge end

                if (world.getCollisionBoxes((Entity) (Object) this, this.getEntityBoundingBox()).isEmpty() && !world.containsAnyLiquid(this.getEntityBoundingBox()))
                {
                    flag = true;
                }
            }
        }

        if (!flag)
        {
            // Sponge start - this is technically a teleport, since it sends packets to players and calls 'updateEntityWithOptionalForce' - even though it doesn't really move the entity at all
            if (!world.isRemote) {
                Transform<org.spongepowered.api.world.World> transform = this.getTransform().setPosition(new Vector3d(d0, d1, d2));
                MoveEntityEvent.Teleport event = EntityUtil.handleDisplaceEntityTeleportEvent((Entity) (Object) this, transform, transform, false);
                if (event.isCancelled()) {
                    return false;
                }
                Vector3d position = event.getToTransform().getPosition();
                this.rotationYaw = (float) event.getToTransform().getYaw();
                this.rotationPitch = (float) event.getToTransform().getPitch();
                this.setPositionAndUpdate(position.getX(), position.getY(), position.getZ());
            } else {
                this.setPositionAndUpdate(d0, d1, d2);
            }
            // Sponge end

            return false;
        }
        else
        {
            // int i = 128;

            for (int j = 0; j < 128; ++j)
            {
                double d6 = (double)j / 127.0D;
                float f = (random.nextFloat() - 0.5F) * 0.2F;
                float f1 = (random.nextFloat() - 0.5F) * 0.2F;
                float f2 = (random.nextFloat() - 0.5F) * 0.2F;
                double d3 = d0 + (this.posX - d0) * d6 + (random.nextDouble() - 0.5D) * (double)this.width * 2.0D;
                double d4 = d1 + (this.posY - d1) * d6 + random.nextDouble() * (double)this.height;
                double d5 = d2 + (this.posZ - d2) * d6 + (random.nextDouble() - 0.5D) * (double)this.width * 2.0D;
                world.spawnParticle(EnumParticleTypes.PORTAL, d3, d4, d5, (double)f, (double)f1, (double)f2, new int[0]);
            }

            if ((Object) this instanceof EntityCreature)
            {
                ((EntityCreature) (Object) this).getNavigator().clearPathEntity();
            }

            return true;
        }
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

    @Redirect(method = "onEntityUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;onDeathUpdate()V"))
    private void causeTrackDeathUpdate(EntityLivingBase entityLivingBase) {
        if (!entityLivingBase.worldObj.isRemote && CauseTracker.ENABLED) {
            final CauseTracker causeTracker = ((IMixinWorldServer) entityLivingBase.worldObj).getCauseTracker();
            causeTracker.switchToPhase(EntityPhase.State.DEATH_UPDATE, PhaseContext.start()
                    .addCaptures()
                    .addEntityDropCaptures()
                    .add(NamedCause.source(entityLivingBase))
                    .complete());
            ((IMixinEntityLivingBase) entityLivingBase).onSpongeDeathUpdate();
            causeTracker.completePhase();
        } else {
            ((IMixinEntityLivingBase) entityLivingBase).onSpongeDeathUpdate();
        }
    }


    @Override
    public void onSpongeDeathUpdate() {
        this.onDeathUpdate();
    }

    @Inject(method = "onItemPickup", at = @At("HEAD"))
    public void onEntityItemPickup(net.minecraft.entity.Entity entityItem, int unused, CallbackInfo ci) {
        if (!this.worldObj.isRemote) {
//            EntityUtil.toMixin(entityItem).setDestructCause(Cause.of(NamedCause.of("PickedUp", this)));
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
