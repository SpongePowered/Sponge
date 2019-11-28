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
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.BlockState;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.Effects;
import net.minecraft.util.CombatTracker;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.damage.DamageFunction;
import org.spongepowered.api.event.cause.entity.damage.DamageModifier;
import org.spongepowered.api.event.cause.entity.damage.source.FallingBlockDamageSource;
import org.spongepowered.api.event.entity.ChangeEntityEquipmentEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.OwnershipTrackedBridge;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.bridge.entity.LivingEntityBaseBridge;
import org.spongepowered.common.bridge.entity.player.InventoryPlayerBridge;
import org.spongepowered.common.bridge.entity.player.EntityPlayerBridge;
import org.spongepowered.common.bridge.entity.player.EntityPlayerMPBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.living.human.EntityHuman;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.damage.DamageEventHandler;
import org.spongepowered.common.event.damage.DamageObject;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.entity.EntityDeathContext;
import org.spongepowered.common.event.tracking.phase.entity.EntityPhase;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.comp.HotbarLens;
import org.spongepowered.common.item.inventory.lens.impl.minecraft.PlayerInventoryLens;
import org.spongepowered.common.item.inventory.lens.impl.slots.SlotLensImpl;
import org.spongepowered.common.item.inventory.lens.slots.SlotLens;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.registry.type.event.DamageSourceRegistryModule;
import org.spongepowered.common.util.Constants;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(value = LivingEntity.class, priority = 999)
public abstract class EntityLivingBaseMixin extends EntityMixin implements LivingEntityBaseBridge {

    @Shadow public int maxHurtResistantTime;
    @Shadow public int hurtTime;
    @Shadow public int maxHurtTime;
    @Shadow public float attackedAtYaw;
    @Shadow public float limbSwingAmount;
    @Shadow protected boolean dead;
    @Shadow protected int idleTime;
    @Shadow protected int scoreValue;
    @Shadow protected int recentlyHit;
    @Shadow protected int activeItemStackUseCount;
    @Shadow protected float lastDamage;
    @Shadow @Nullable protected PlayerEntity attackingPlayer;
    @Shadow protected ItemStack activeItemStack;
    @Shadow private DamageSource lastDamageSource;
    @Shadow private long lastDamageStamp;

    @Shadow public void stopActiveHand() { }
    @Shadow public abstract void setHealth(float health);
    @Shadow public abstract void setLastAttackedEntity(Entity entity);
    @Shadow public abstract void knockBack(Entity entityIn, float p_70653_2_, double p_70653_3_, double p_70653_5_);
    @Shadow public abstract void shadow$setRevengeTarget(LivingEntity livingBase);
    @Shadow public abstract void setAbsorptionAmount(float amount);
    @Shadow public abstract void setHeldItem(Hand hand, @Nullable ItemStack stack);
    @Shadow public abstract void setSprinting(boolean sprinting);
    @Shadow public abstract void resetActiveHand();
    @Shadow public abstract int getItemInUseCount();
    @Shadow public abstract float getAbsorptionAmount();
    @Shadow public abstract float getHealth();
    @Shadow public abstract float getMaxHealth();
    @Shadow public abstract boolean isPotionActive(Effect potion);
    @Shadow public abstract boolean isOnLadder();
    @Shadow protected abstract boolean canBlockDamageSource(DamageSource p_184583_1_);
    @Shadow public abstract IAttributeInstance getEntityAttribute(IAttribute attribute);
    @Shadow public abstract ItemStack getItemStackFromSlot(EquipmentSlotType slotIn);
    @Shadow public abstract ItemStack getHeldItem(Hand hand);
    @Shadow public abstract ItemStack getHeldItemMainhand();
    @Shadow public abstract CombatTracker getCombatTracker();
    @Shadow @Nullable public abstract LivingEntity getAttackingEntity();
    @Shadow public abstract Random getRNG();
    @Shadow public void onKillCommand() { }
    @Shadow public abstract AbstractAttributeMap getAttributeMap();
    @Shadow public abstract Hand getActiveHand();
    @Shadow protected abstract void onDeathUpdate();
    @Shadow protected abstract void markVelocityChanged();
    @Shadow protected abstract void damageShield(float p_184590_1_);
    @Shadow protected abstract void playHurtSound(DamageSource p_184581_1_);
    @Shadow protected abstract void blockUsingShield(LivingEntity p_190629_1_);
    @Shadow protected abstract void dropLoot(boolean wasRecentlyHit, int lootingModifier, DamageSource source);
    @Shadow protected abstract int getExperiencePoints(PlayerEntity attackingPlayer);
    @Shadow protected abstract float getSoundVolume();
    @Shadow protected abstract float getSoundPitch();
    @Shadow protected abstract boolean canDropLoot();
    @Shadow protected abstract SoundEvent getDeathSound();
    @Shadow private boolean checkTotemDeathProtection(final DamageSource p_190628_1_) {
        return false; // SHADOWED
    }

    private int impl$deathEventsPosted;
    private int impl$maxAir = Constants.Sponge.Entity.DEFAULT_MAX_AIR;
    @Nullable private ItemStack impl$activeItemStackCopy;

    @Override
    public int bridge$getMaxAir() {
        return this.impl$maxAir;
    }

    @Override
    public void bridge$setMaxAir(final int air) {
        this.impl$maxAir = air;
        if (air != Constants.Sponge.Entity.DEFAULT_MAX_AIR) {
            final CompoundNBT spongeData = ((DataCompoundHolder) this).data$getSpongeCompound();
            spongeData.putInt(Constants.Sponge.Entity.MAX_AIR, air);
        } else {
            if (((DataCompoundHolder) this).data$hasSpongeCompound()) {
                ((DataCompoundHolder) this).data$getSpongeCompound().remove(Constants.Sponge.Entity.MAX_AIR);
            }
        }
    }

    @Override
    public void spongeImpl$readFromSpongeCompound(final CompoundNBT compound) {
        super.spongeImpl$readFromSpongeCompound(compound);
        if (compound.contains(Constants.Sponge.Entity.MAX_AIR)) {
            this.impl$maxAir = compound.getInt(Constants.Sponge.Entity.MAX_AIR);
        }
    }

    @Override
    public void spongeImpl$writeToSpongeCompound(final CompoundNBT compound) {
        super.spongeImpl$writeToSpongeCompound(compound);
        if (this.impl$maxAir != Constants.Sponge.Entity.DEFAULT_MAX_AIR) { // We don't need to set max air unless it's really necessary
            compound.putInt(Constants.Sponge.Entity.MAX_AIR, this.impl$maxAir);
        }
    }

    /**
     * @author blood - May 12th, 2016
     * @author gabizou - June 4th, 2016 - Update for 1.9.4 and Cause Tracker changes
     *
     * @reason SpongeForge requires an overwrite so we do it here instead. This handles all living entity death events
     * (except players). Note should be taken that normally, there are lists for drops captured, however, the drops
     * are retroactively captured by the PhaseTracker and associated through the different phases, depending on
     * how they are handled, so no lists and flags on the entities themselves are needed as they are tracked through
     * the {@link PhaseContext} of the current {@link IPhaseState} at which this method is called. The compatibility
     * for Forge's events to throw are handled specially in SpongeForge.
     */
    @SuppressWarnings("ConstantConditions")
    @Overwrite
    public void onDeath(final DamageSource cause) {
        // Sponge Start - Call our event, and forge's event
        // This will transitively call the forge event
        final boolean isMainThread = !((WorldBridge) this.world).bridge$isFake() || Sponge.isServerAvailable() && Sponge.getServer().isMainThread();
        if (!this.isDead) { // isDead should be set later on in this method so we aren't re-throwing the events.
            if (isMainThread && this.impl$deathEventsPosted <= Constants.Sponge.MAX_DEATH_EVENTS_BEFORE_GIVING_UP) {
                // ignore because some moron is not resetting the entity.
                this.impl$deathEventsPosted++;
                if (SpongeCommonEventFactory.callDestructEntityEventDeath((LivingEntity) (Object) this, cause, isMainThread).map(Cancellable::isCancelled).orElse(true)) {
                    // Since the forge event is cancellable
                    return;
                }
            }
        } else {
            this.impl$deathEventsPosted = 0;
        }

        // Double check that the PhaseTracker is already capturing the Death phase
        try (final EntityDeathContext context = createOrNullDeathPhase(isMainThread, cause)) {
            // We re-enter the state only if we aren't already in the death state. This can usually happen when
            // and only when the onDeath method is called outside of attackEntityFrom, which should never happen.
            // but then again, mods....
            if (context != null) {
                context.buildAndSwitch();
            }
            // Sponge End
            if (this.dead) {
                return;
            }

            final Entity entity = cause.getTrueSource();
            final LivingEntity entitylivingbase = this.getAttackingEntity();

            if (this.scoreValue >= 0 && entitylivingbase != null) {
                entitylivingbase.awardKillScore((LivingEntity) (Object) this, this.scoreValue, cause);
            }

            if (entity != null) {
                entity.onKillEntity((LivingEntity) (Object) this);
            }

            this.dead = true;
            this.getCombatTracker().reset();

            if (!this.world.isRemote) {
                int i = 0;

                if (entity instanceof PlayerEntity) {
                    // Sponge Start - use Forge hooks for the looting modifier.
                    //i = EnchantmentHelper.getLootingModifier((EntityLivingBase) entity);
                    i = SpongeImplHooks.getLootingEnchantmentModifier((LivingEntity) (Object) this, (LivingEntity) entity, cause);
                }

                if (this.canDropLoot() && this.world.getGameRules().func_82766_b("doMobLoot")) {
                    final boolean flag = this.recentlyHit > 0;
                    this.dropLoot(flag, i, cause);
                }

            }

            // Sponge Start - Don't send the state if this is a human. Fixes ghost items on client.
            if (!((LivingEntity) (Object) this instanceof EntityHuman)) {
                this.world.setEntityState((LivingEntity) (Object) this, (byte) 3);
            }

        }

        // Sponge End
    }

    @Override
    public void bridge$resetDeathEventsPosted() {
        this.impl$deathEventsPosted = 0;
    }

    /**
     * @author gabizou - April 29th, 2018
     * @reason Due to cancelling death events, "healing" the entity is the only way to cancel the
     * death, but we still want to reset the death event counter. This is the simplest way to get it working
     * with forge mods who do not have access to Sponge's API.
     *
     * @param health The health
     * @param info The callback
     */
    @Inject(method = "setHealth", at = @At("HEAD"))
    private void onSetHealthResetEvents(final float health, final CallbackInfo info) {
        if (this.getHealth() <= 0 && health > 0) {
            bridge$resetDeathEventsPosted();
        }
    }

    @Nullable
    private EntityDeathContext createOrNullDeathPhase(final boolean isMainThread, final DamageSource source) {
        final boolean tracksEntityDeaths;
        if (((WorldBridge) this.world).bridge$isFake() || !isMainThread) { // Short circuit to avoid erroring on handling
            return null;
        }
        final IPhaseState<?> state = PhaseTracker.getInstance().getCurrentContext().state;
        tracksEntityDeaths = !state.tracksEntityDeaths() && state != EntityPhase.State.DEATH;
        if (tracksEntityDeaths) {
            final EntityDeathContext context = EntityPhase.State.DEATH.createPhaseContext()
                .setDamageSource((org.spongepowered.api.event.cause.entity.damage.source.DamageSource) source)
                .source(this);
            if (this instanceof OwnershipTrackedBridge) {
                ((OwnershipTrackedBridge) this).tracked$getNotifierReference().ifPresent(context::notifier);
                ((OwnershipTrackedBridge) this).tracked$getOwnerReference().ifPresent(context::owner);
            }
            return context;
        }
        return null;
    }

    @Redirect(method = "applyPotionDamageCalculations", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;isPotionActive(Lnet/minecraft/potion/Potion;)Z") )
    private boolean onIsPotionActive(final LivingEntity entityIn, final Effect potion) {
        return false; // handled in our bridge$damageEntityHook
    }

    /**
     * @author blood - Some time ago in 2015?
     * @reason Our damage hook handles armor modifiers and "replaying" damage to armor.
     *
     * @param entityIn The entity being damaged
     * @param damage The damage to deal
     */
    @Redirect(method = "applyArmorCalculations", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;damageArmor(F)V") )
    private void onDamageArmor(final LivingEntity entityIn, final float damage) {
        // do nothing as this is handled in our bridge$damageEntityHook
    }

    /**
     * @author bloodmc - November 21, 2015
     * @reason This shouldn't be used internally but a mod may still call it so we simply reroute to our hook.
     */
    @Overwrite
    protected void damageEntity(final DamageSource damageSource, final float damage) {
        this.bridge$damageEntityHook(damageSource, damage);
    }

    /**
     * @author bloodmc - November 22, 2015
     * @author gabizou - Updated April 11th, 2016 - Update for 1.9 changes
     * @author Aaron1011 - Updated Nov 11th, 2016 - Update for 1.11 changes
     *
     * @reason Reroute damageEntity calls to our hook in order to prevent damage.
     */
    @SuppressWarnings("ConstantConditions")
    @Override
    @Overwrite
    public boolean attackEntityFrom(final DamageSource source, final float amount) {
        // Sponge start - Add certain hooks for necessities
        this.lastDamageSource = source;
        if (source == null) {
            new PrettyPrinter(60).centre().add("Null DamageSource").hr()
                .addWrapped("Sponge has found a null damage source! This should NEVER happen "
                            + "as the DamageSource is used for all sorts of calculations. Usually"
                            + " this can be considered developer error. Please report the following"
                            + " stacktrace to the most appropriate mod/plugin available.")
                .add()
                .add(new IllegalArgumentException("Null DamageSource"))
                .log(SpongeImpl.getLogger(), Level.WARN);
            return false;
        }
        // Sponge - This hook is for forge use mainly
        if (!this.bridge$hookModAttack((LivingEntity) (Object) this, source, amount)) {
            return false;
        }
        // Sponge end
        if (this.isEntityInvulnerable(source)) {
            return false;
        } else if (this.world.isRemote) {
            return false;
        } else {
            this.idleTime = 0;

            // Sponge - if the damage source is ignored, then do not return false here, as the health
            // has already been set to zero if Keys#HEALTH or SpongeHealthData is set to zero.
            if (this.getHealth() <= 0.0F && source != DamageSourceRegistryModule.IGNORED_DAMAGE_SOURCE) {
                return false;
            } else if (source.isFireDamage() && this.isPotionActive(Effects.FIRE_RESISTANCE)) {
                return false;
            } else {

                final float f = amount;

                // Sponge - ignore as this is handled in our damageEntityHookge
//                if ((source == DamageSource.ANVIL || source == DamageSource.FALLING_BLOCK) && !this.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty())
//                {
//                    this.getItemStackFromSlot(EntityEquipmentSlot.HEAD).damageItem((int)(amount * 4.0F + this.rand.nextFloat() * amount * 2.0F), this);
//                    amount *= 0.75F;
//                }
                // Sponge End

                // Sponge - set the 'shield blocking ran' flag to the proper value, since
                // we comment out the logic below
                final boolean flag = amount > 0.0F && this.canBlockDamageSource(source);

                // Sponge start - this is handled in our bridge$damageEntityHook
//                boolean flag = false;
//
//                if (amount > 0.0F && this.canBlockDamageSource(source))
//                {
//                    this.damageShield(amount);
//                    amount = 0.0F;
//
//                    if (!source.isProjectile())
//                    {
//                        Entity entity = source.getImmediateSource();
//
//                        if (entity instanceof EntityLivingBase)
//                        {
//                            this.blockUsingShield((EntityLivingBase)entity);
//                        }
//                    }
//
//                    flag = true;
//                }
                // Sponge end

                this.limbSwingAmount = 1.5F;
                boolean flag1 = true;

                if ((float) this.hurtResistantTime > (float) this.maxHurtResistantTime / 2.0F) {
                    if (amount <= this.lastDamage) { // Technically, this is wrong since 'amount' won't be 0 if a shield is used. However, we need bridge$damageEntityHook so that we process the shield, so we leave it as-is
                        return false;
                    }

                    // Sponge start - reroute to our damage hook
                    // only if the class is unmodded. If it's a modded class, then it should be calling our
                    // damageEntity method, which would re-run our bridge$damageEntityHook.
                    if (this.entityType.isModdedDamageEntityMethod) {
                        this.damageEntity(source, amount - this.lastDamage);
                    } else {
                        if (!this.bridge$damageEntityHook(source, amount - this.lastDamage)) {
                            return false;
                        }
                    }

                    // this.damageEntity(source, amount - this.lastDamage); // handled above
                    // Sponge end
                    this.lastDamage = amount;
                    flag1 = false;
                } else {
                    // Sponge start - reroute to our damage hook
                    if (this.entityType.isModdedDamageEntityMethod) {
                        this.damageEntity(source, amount);
                    } else {
                        if (!this.bridge$damageEntityHook(source, amount)) {
                            return false;
                        }
                    }
                    this.lastDamage = amount;
                    this.hurtResistantTime = this.maxHurtResistantTime;
                    // this.damageEntity(source, amount); // handled above
                    // Sponge end
                    this.hurtTime = this.maxHurtTime = 10;
                }

                this.attackedAtYaw = 0.0F;
                final Entity entity = source.getTrueSource();

                if (entity instanceof LivingEntity) {
                    this.shadow$setRevengeTarget((LivingEntity) entity);
                }

                if (entity instanceof PlayerEntity) {
                    this.recentlyHit = 100;
                    this.attackingPlayer = (PlayerEntity) entity;
                } else if (entity instanceof TameableEntity) {

                    final TameableEntity entitywolf = (TameableEntity)entity;

                    if (entitywolf.isTamed()) {
                        this.recentlyHit = 100;
                        this.attackingPlayer = null;
                    }
                }

                if (flag1) {
                    if (flag) {
                        this.world.setEntityState((LivingEntity) (Object) this, (byte) 29);
                    } else if (source instanceof net.minecraft.util.EntityDamageSource && ((net.minecraft.util.EntityDamageSource) source).getIsThornsDamage()) {
                        this.world.setEntityState((LivingEntity) (Object) this, (byte) 33);
                    } else {
                        final byte b0;

                        if (source == DamageSource.DROWN) {
                            b0 = 36;
                        } else if (source.isFireDamage()) {
                            b0 = 37;
                        } else {
                            b0 = 2;
                        }

                        this.world.setEntityState((LivingEntity) (Object) this, b0);
                    }


                    if (source != DamageSource.DROWN && !flag) { // Sponge - remove 'amount > 0.0F' - it's redundant in Vanilla, and breaks our handling of shields
                        this.markVelocityChanged();
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
                        this.attackedAtYaw = (float) (Math.random() * 2.0D * 180);
                    }
                }

                if (this.getHealth() <= 0.0F) {
                    if (!this.checkTotemDeathProtection(source)) {
                        final SoundEvent soundevent = this.getDeathSound();

                        if (flag1 && soundevent != null) {
                            this.playSound(soundevent, this.getSoundVolume(), this.getSoundPitch());
                        }

                        // Sponge Start - notify the cause tracker
                        try (final EntityDeathContext context = createOrNullDeathPhase(true, source)) {
                            if (context != null) {
                                context.buildAndSwitch();
                            }
                            this.onDeath(source);
                        }
                    }
                    // Sponge End
                } else if (flag1) {
                    this.playHurtSound(source);
                }


                if (!flag) // Sponge - remove 'amount > 0.0F'
                {
                    this.lastDamageSource = source;
                    this.lastDamageStamp = this.world.getGameTime();
                }

                if ((LivingEntity) (Object) this instanceof ServerPlayerEntity) {
                    CriteriaTriggers.ENTITY_HURT_PLAYER.trigger((ServerPlayerEntity) (Object) this, source, f, amount, flag);
                }

                if (entity instanceof ServerPlayerEntity) {
                    CriteriaTriggers.PLAYER_HURT_ENTITY.trigger((ServerPlayerEntity) entity, (Entity) (Object) this, source, f, amount, flag);
                }

                return !flag; // Sponge - remove 'amount > 0.0F'
            }
        }
    }

    /**
     * @author gabizou - January 4th, 2016
     * This is necessary for invisibility checks so that vanish players don't actually send the particle stuffs.
     */
    @Redirect(method = "updateItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    private void spawnItemParticle(final World world, final EnumParticleTypes particleTypes, final double xCoord, final double yCoord, final double zCoord, final double xOffset,
            final double yOffset, final double zOffset, final int ... p_175688_14_) {
        if (!this.bridge$isVanished()) {
            this.world.func_175688_a(particleTypes, xCoord, yCoord, zCoord, xOffset, yOffset, zOffset, p_175688_14_);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean bridge$damageEntityHook(final DamageSource damageSource, float damage) {
        if (!this.isEntityInvulnerable(damageSource)) {
            final boolean human = (LivingEntity) (Object) this instanceof PlayerEntity;
            // apply forge damage hook
            damage = bridge$applyModDamage((LivingEntity) (Object) this, damageSource, damage);
            final float originalDamage = damage; // set after forge hook.
            if (damage <= 0) {
                damage = 0;
            }

            final List<DamageFunction> originalFunctions = new ArrayList<>();
            final Optional<DamageFunction> hardHatFunction =
                DamageEventHandler.createHardHatModifier((LivingEntity) (Object) this, damageSource);
            final Optional<List<DamageFunction>> armorFunction =
                bridge$provideArmorModifiers((LivingEntity) (Object) this, damageSource, damage);
            final Optional<DamageFunction> resistanceFunction =
                DamageEventHandler.createResistanceModifier((LivingEntity) (Object) this, damageSource);
            final Optional<List<DamageFunction>> armorEnchantments =
                DamageEventHandler.createEnchantmentModifiers((LivingEntity) (Object) this, damageSource);
            final Optional<DamageFunction> absorptionFunction =
                DamageEventHandler.createAbsorptionModifier((LivingEntity) (Object) this, damageSource);
            final Optional<DamageFunction> shieldFunction =
                DamageEventHandler.createShieldFunction((LivingEntity) (Object) this, damageSource, damage);

            hardHatFunction.ifPresent(originalFunctions::add);

            shieldFunction.ifPresent(originalFunctions::add);

            armorFunction.ifPresent(originalFunctions::addAll);

            resistanceFunction.ifPresent(originalFunctions::add);

            armorEnchantments.ifPresent(originalFunctions::addAll);

            absorptionFunction.ifPresent(originalFunctions::add);
            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                DamageEventHandler.generateCauseFor(damageSource, frame);

                final DamageEntityEvent event = SpongeEventFactory.createDamageEntityEvent(frame.getCurrentCause(), originalFunctions, (org.spongepowered.api.entity.Entity) this, originalDamage);
                if (damageSource != DamageSourceRegistryModule.IGNORED_DAMAGE_SOURCE) { // Basically, don't throw an event if it's our own damage source
                    Sponge.getEventManager().post(event);
                }
                if (event.isCancelled()) {
                    return false;
                }

                damage = (float) event.getFinalDamage();

                damage = this.bridge$applyModDamagePost((LivingEntity) (Object) this, damageSource, damage);

                // Helmet
                final ItemStack helmet = this.getItemStackFromSlot(EquipmentSlotType.HEAD);
                // We still sanity check if a mod is calling to damage the entity with an anvil or falling block
                // without using our mixin redirects in EntityFallingBlockMixin.
                if ((damageSource instanceof FallingBlockDamageSource) || damageSource == DamageSource.ANVIL || damageSource == DamageSource.FALLING_BLOCK && !helmet.isEmpty()) {
                    helmet.func_77972_a((int) (event.getBaseDamage() * 4.0F + this.rand.nextFloat() * event.getBaseDamage() * 2.0F), (LivingEntity) (Object) this);
                }

                // Shield
                if (shieldFunction.isPresent()) {
                    this.damageShield((float) event.getBaseDamage()); // TODO gabizou: Should this be in the API?
                    if (!damageSource.isProjectile()) {
                        final Entity entity = damageSource.getImmediateSource();

                        if (entity instanceof LivingEntity) {
                            this.blockUsingShield((LivingEntity) entity);
                        }
                    }
                }

                // Armor
                if (!damageSource.isUnblockable()) {
                    for (final DamageFunction modifier : event.getModifiers()) {
                        bridge$applyArmorDamage((LivingEntity) (Object) this, damageSource, event, modifier.getModifier());
                    }
                }

                double absorptionModifier = absorptionFunction.map(function -> event.getDamage(function.getModifier())).orElse(0d);
                if (absorptionFunction.isPresent()) {
                    absorptionModifier = event.getDamage(absorptionFunction.get().getModifier());
                }

                this.setAbsorptionAmount(Math.max(this.getAbsorptionAmount() + (float) absorptionModifier, 0.0F));
                if (damage != 0.0F) {
                    if (human) {
                        ((PlayerEntity) (Object) this).addExhaustion(damageSource.getHungerDamage());
                    }
                    final float f2 = this.getHealth();

                    this.setHealth(f2 - damage);
                    this.getCombatTracker().trackDamage(damageSource, f2, damage);

                    if (human) {
                        return true;
                    }

                    this.setAbsorptionAmount(this.getAbsorptionAmount() - damage);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * @author Aaron1011 - August 15, 2016
     * @reason An overwrite avoids the need for a local-capture inject and two redirects
     */
    // TODO: Investigate mixing into setPositionAndUpdate to catch more teleports
    @SuppressWarnings("ConstantConditions")
    @Overwrite
    public boolean attemptTeleport(final double x, final double y, final double z)
    {
        final double d0 = this.posX;
        final double d1 = this.posY;
        final double d2 = this.posZ;
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        boolean flag = false;
        BlockPos blockpos = new BlockPos((Entity) (Object) this);
        final World world = this.world;
        final Random random = this.getRNG();

        if (world.isBlockLoaded(blockpos))
        {
            boolean flag1 = false;

            while (!flag1 && blockpos.getY() > 0)
            {
                final BlockPos blockpos1 = blockpos.down();
                final BlockState iblockstate = world.getBlockState(blockpos1);

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
                    final Transform<org.spongepowered.api.world.World> fromTransform = ((org.spongepowered.api.entity.Entity) this).getTransform().setPosition(new Vector3d(d0, d1, d2));
                    final Transform<org.spongepowered.api.world.World> toTransform = ((org.spongepowered.api.entity.Entity) this).getTransform().setPosition(new Vector3d(this.posX, this.posY, this.posZ));

                    final MoveEntityEvent.Teleport event = EntityUtil.handleDisplaceEntityTeleportEvent((Entity) (Object) this, fromTransform, toTransform);
                    if (event.isCancelled()) {
                        this.posX = d0;
                        this.posY = d1;
                        this.posZ = d2;
                        return false;
                    }
                    final Vector3d position = event.getToTransform().getPosition();
                    this.rotationYaw = (float) event.getToTransform().getYaw();
                    this.rotationPitch = (float) event.getToTransform().getPitch();
                    this.setPositionAndUpdate(position.getX(), position.getY(), position.getZ());
                } else {
                    this.setPositionAndUpdate(this.posX, this.posY, this.posZ);
                }
                // Sponge end

                if (world.func_184144_a((Entity) (Object) this, this.getEntityBoundingBox()).isEmpty() && !world.containsAnyLiquid(this.getEntityBoundingBox()))
                {
                    flag = true;
                }
            }
        }

        if (!flag)
        {
            // Sponge start - this is technically a teleport, since it sends packets to players and calls 'updateEntityWithOptionalForce' - even though it doesn't really move the entity at all
            if (!world.isRemote) {
                final Transform<org.spongepowered.api.world.World> transform = ((org.spongepowered.api.entity.Entity) this).getTransform().setPosition(new Vector3d(d0, d1, d2));
                final MoveEntityEvent.Teleport event = EntityUtil.handleDisplaceEntityTeleportEvent((Entity) (Object) this, transform, transform);
                if (event.isCancelled()) {
                    return false;
                }
                final Vector3d position = event.getToTransform().getPosition();
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
                final double d6 = (double)j / 127.0D;
                final float f = (random.nextFloat() - 0.5F) * 0.2F;
                final float f1 = (random.nextFloat() - 0.5F) * 0.2F;
                final float f2 = (random.nextFloat() - 0.5F) * 0.2F;
                final double d3 = d0 + (this.posX - d0) * d6 + (random.nextDouble() - 0.5D) * (double)this.width * 2.0D;
                final double d4 = d1 + (this.posY - d1) * d6 + random.nextDouble() * (double)this.height;
                final double d5 = d2 + (this.posZ - d2) * d6 + (random.nextDouble() - 0.5D) * (double)this.width * 2.0D;
                world.func_175688_a(EnumParticleTypes.PORTAL, d3, d4, d5, (double)f, (double)f1, (double)f2);
            }

            if ((LivingEntity) (Object) this instanceof CreatureEntity)
            {
                ((CreatureEntity) (Object) this).getNavigator().clearPath();
            }

            return true;
        }
    }

    @Override
    public float bridge$applyModDamage(final LivingEntity entityLivingBase, final DamageSource source, final float damage) {
        return damage;
    }

    @Override
    public Optional<List<DamageFunction>> bridge$provideArmorModifiers(final LivingEntity entityLivingBase,
        final DamageSource source, final double damage) {
        return DamageEventHandler.createArmorModifiers(entityLivingBase, source, damage);
    }

    @Override
    public void bridge$applyArmorDamage(
        final LivingEntity entityLivingBase, final DamageSource source, final DamageEntityEvent entityEvent, final DamageModifier modifier) {
        final Optional<DamageObject> optional = modifier.getCause().first(DamageObject.class);
        if (optional.isPresent()) {
            DamageEventHandler.acceptArmorModifier((LivingEntity) (Object) this, source, modifier, entityEvent.getDamage(modifier));
        }
    }

    @Override
    public float bridge$applyModDamagePost(final LivingEntity entityLivingBase, final DamageSource source, final float damage) {
        return damage;
    }


    @Override
    public boolean bridge$hookModAttack(final LivingEntity entityLivingBase, final DamageSource source, final float amount) {
        return true;
    }

    /**
     * @author gabizou - January 4th, 2016
     * @reason This allows invisiblity to ignore entity collisions.
     */
    @Overwrite
    public boolean canBeCollidedWith() {
        return !(this.bridge$isVanished() && this.bridge$isUncollideable()) && !this.isDead;
    }

    @Redirect(method = "updateFallState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDIDDDD[I)V"))
    private void spongeSpawnParticleForFallState(
        final ServerWorld worldServer, final EnumParticleTypes particleTypes, final double xCoord, final double yCoord,
            final double zCoord, final int numberOfParticles, final double xOffset, final double yOffset, final double zOffset, final double particleSpeed, final int... extraArgs) {
        if (!this.bridge$isVanished()) {
            worldServer.func_175739_a(particleTypes, xCoord, yCoord, zCoord, numberOfParticles, xOffset, yOffset, zOffset, particleSpeed, extraArgs);
        }

    }

    @Redirect(method = "onEntityUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;onDeathUpdate()V"))
    private void causeTrackDeathUpdate(final LivingEntity entityLivingBase) {
        if (!this.world.isRemote) {
            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame();
                 final PhaseContext<?> context = EntityPhase.State.DEATH_UPDATE.createPhaseContext().source(entityLivingBase)) {
                context.buildAndSwitch();
                frame.pushCause(this);
                this.onDeathUpdate();
            }
        } else {
            this.onDeathUpdate();
        }
    }

    private EnumMap<EquipmentSlotType, SlotLens> slotLens = new EnumMap<>(EquipmentSlotType.class);

    @Surrogate
    private void onGetItemStackFromSlot(final CallbackInfo ci, final EquipmentSlotType[] slots, final int j, final int k,
            final EquipmentSlotType entityEquipmentSlot, final ItemStack before) {
        this.onGetItemStackFromSlot(ci, 0, slots, j, k, entityEquipmentSlot, before);
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "onUpdate", locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;getItemStackFromSlot(Lnet/minecraft/inventory/EntityEquipmentSlot;)Lnet/minecraft/item/ItemStack;"))
    private void onGetItemStackFromSlot(final CallbackInfo ci, final int i_unused, final EquipmentSlotType[] slots, final int j, final int k,
                                        final EquipmentSlotType entityEquipmentSlot, final ItemStack before) {
        if (this.ticksExisted == 1 && (LivingEntity) (Object) this instanceof PlayerEntity) {
            return; // Ignore Equipment on player spawn/respawn
        }
        final ItemStack after = this.getItemStackFromSlot(entityEquipmentSlot);
        final LivingEntity entity = (LivingEntity) (LivingEntityBaseBridge) this;
        if (!ItemStack.areItemStacksEqual(after, before)) {
            final InventoryAdapter slotAdapter;
            if (entity instanceof ServerPlayerEntity) {
                final SlotLens slotLens;
                final InventoryPlayerBridge inventory = (InventoryPlayerBridge) ((ServerPlayerEntity) entity).inventory;
                final Lens inventoryLens = ((InventoryAdapter) inventory).bridge$getRootLens();
                if (inventoryLens instanceof PlayerInventoryLens) {
                    switch (entityEquipmentSlot) {
                        case OFFHAND:
                            slotLens = ((PlayerInventoryLens) inventoryLens).getOffhandLens();
                            break;
                        case MAINHAND:
                            final HotbarLens hotbarLens = ((PlayerInventoryLens) inventoryLens).getMainLens().getHotbar();
                            slotLens = hotbarLens.getSlot(hotbarLens.getSelectedSlotIndex(((InventoryAdapter) inventory).bridge$getFabric()));
                            break;
                        default:
                            slotLens = ((PlayerInventoryLens) inventoryLens).getEquipmentLens().getSlot(entityEquipmentSlot.getIndex());
                    }
                } else {
                    slotLens = inventoryLens.getSlotLens(entityEquipmentSlot.getIndex());
                }

                slotAdapter = slotLens.getAdapter(((InventoryAdapter) inventory).bridge$getFabric(), (Inventory) inventory);
            } else {
                if (this.slotLens.isEmpty()) {
                    for (final EquipmentSlotType slot : EquipmentSlotType.values()) {
                        this.slotLens.put(slot, new SlotLensImpl(slot.getSlotIndex()));
                    }
                }
                slotAdapter = this.slotLens.get(entityEquipmentSlot).getAdapter((Fabric) this, null);
            }
            final ChangeEntityEquipmentEvent event = SpongeCommonEventFactory.callChangeEntityEquipmentEvent(entity,
                    ItemStackUtil.snapshotOf(before), ItemStackUtil.snapshotOf(after), (SlotAdapter) slotAdapter);
            if (event.isCancelled()) {
                this.setItemStackToSlot(entityEquipmentSlot, before);
                return;
            }
            final Transaction<ItemStackSnapshot> transaction = event.getTransaction();
            if (!transaction.isValid()) {
                this.setItemStackToSlot(entityEquipmentSlot, before);
                return;
            }
            final Optional<ItemStackSnapshot> optional = transaction.getCustom();
            if (optional.isPresent()) {
                final ItemStack custom = ItemStackUtil.fromSnapshotToNative(optional.get());
                this.setItemStackToSlot(entityEquipmentSlot, custom);
            }
        }
    }

    @Redirect(method = "onDeathUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;getExperiencePoints(Lnet/minecraft/entity/player/EntityPlayer;)I"))
    private int onGetExperiencePoints(final LivingEntity entity, final PlayerEntity attackingPlayer) {
        if (entity instanceof EntityPlayerBridge) {
            if (((EntityPlayerBridge) entity).bridge$keepInventory()) {
                return 0;
            }
        }
        return this.getExperiencePoints(attackingPlayer);
    }

    @Inject(method = "onItemPickup", at = @At("HEAD"))
    public void onEntityItemPickup(final Entity entityItem, final int unused, final CallbackInfo ci) {
        if (!this.world.isRemote) {
//            EntityUtil.toMixin(entityItem).setDestructCause(Cause.of(NamedCause.of("PickedUp", this)));
        }
    }

    @Inject(method = "onItemUseFinish", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;resetActiveHand()V"))
    private void updateHealthForUseFinish(final CallbackInfo ci) {
        if (this instanceof EntityPlayerMPBridge) {
            ((EntityPlayerMPBridge) this).bridge$refreshScaledHealth();
        }
    }

    // Data delegated methods

    // Start implementation of UseItemstackEvent

    @Inject(method = "setActiveHand", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;activeItemStack:Lnet/minecraft/item/ItemStack;"))
    private void onSetActiveItemStack(final Hand hand, final CallbackInfo ci, final ItemStack stack) {
        if (this.world.isRemote) {
            return;
        }

        final UseItemStackEvent.Start event;
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(stack);
            final HandType handType = (HandType) (Object) hand;
            this.addSelfToFrame(frame, snapshot, handType);
            event = SpongeEventFactory.createUseItemStackEventStart(Sponge.getCauseStackManager().getCurrentCause(),
                    stack.getUseDuration(), stack.getUseDuration(), snapshot);
        }

        if (SpongeImpl.postEvent(event)) {
            ci.cancel();
        } else {
            this.activeItemStackUseCount = event.getRemainingDuration();
        }
    }

    @Redirect(method = "setActiveHand", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;activeItemStackUseCount:I"))
    private void getItemDuration(final LivingEntity this$0, final int count) {
        if (this.world.isRemote) {
            this.activeItemStackUseCount = count;
        }
        // If we're on the server, do nothing, since we already set this field on onSetActiveItemStack
    }

    // A helper method for firing UseItemStackEvent sub-events
    // This ensures that the cause and context for these events
    // always have OWNER and NOTIFIER set (if possible),
    // as well as USED_ITEM and USED_HAND
    private void addSelfToFrame(final CauseStackManager.StackFrame frame, final ItemStackSnapshot snapshot, final HandType hand) {
        frame.addContext(EventContextKeys.USED_HAND, hand);
        addSelfToFrame(frame, snapshot);
    }

    private void addSelfToFrame(final CauseStackManager.StackFrame frame, final ItemStackSnapshot snapshot) {
        frame.pushCause(this);
        frame.addContext(EventContextKeys.USED_ITEM, snapshot);
        if (this instanceof User) {
            frame.addContext(EventContextKeys.OWNER, (User) this);
            frame.addContext(EventContextKeys.NOTIFIER, (User) this);
        }
    }

    @Redirect(method = "updateActiveHand",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;getItemInUseCount()I", ordinal = 0))
    private int onGetRemainingItemDuration(final LivingEntity self) {
        if (this.world.isRemote) {
            return self.getItemInUseCount();
        }

        final UseItemStackEvent.Tick event;
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(this.activeItemStack);
            final HandType handType = (HandType) (Object) this.getActiveHand();
            this.addSelfToFrame(frame, snapshot, handType);
            event = SpongeEventFactory.createUseItemStackEventTick(Sponge.getCauseStackManager().getCurrentCause(),
                    this.activeItemStackUseCount, this.activeItemStackUseCount, snapshot);
            SpongeImpl.postEvent(event);
        }
        // Because the item usage will only finish if activeItemStackUseCount == 0 and decrements it first, it should be >= 1
        this.activeItemStackUseCount = Math.max(event.getRemainingDuration(), 1);

        if (event.isCancelled()) {
            // Get prepared for some cool hacks: We're within the condition for updateItemUse
            // So if we don't want it to call the method we just pass a value that makes the
            // condition evaluate to false, so an integer >= 25
            return 26;
        }
        SpongeImplHooks.onUseItemTick((LivingEntity) (Object) this, this.activeItemStack, this.activeItemStackUseCount);


        return getItemInUseCount();
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "onItemUseFinish", cancellable = true,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;updateItemUse(Lnet/minecraft/item/ItemStack;I)V"))
    private void onUpdateItemUse(final CallbackInfo ci) {
        if (this.world.isRemote) {
            return;
        }


        final UseItemStackEvent.Finish event;
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(this.activeItemStack);
            final HandType handType = (HandType) (Object) this.getActiveHand();
            this.addSelfToFrame(frame, snapshot, handType);
            event = SpongeEventFactory.createUseItemStackEventFinish(Sponge.getCauseStackManager().getCurrentCause(),
                    this.activeItemStackUseCount, this.activeItemStackUseCount, snapshot);
        }
        SpongeImpl.postEvent(event);
        if (event.getRemainingDuration() > 0) {
            this.activeItemStackUseCount = event.getRemainingDuration();
            ci.cancel();
        } else if (event.isCancelled()) {
            resetActiveHand();
            ci.cancel();
        } else {
            this.impl$activeItemStackCopy = this.activeItemStack.copy();
        }
    }

    @Redirect(method = "onItemUseFinish", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;"
            + "setHeldItem(Lnet/minecraft/util/EnumHand;Lnet/minecraft/item/ItemStack;)V"))
    private void onSetHeldItem(final LivingEntity self, final Hand hand, final ItemStack stack) {
        if (this.world.isRemote) {
            self.setHeldItem(hand, stack);
            return;
        }

        // Unforunately, ItemFood calls ItemStack#shrink in Item#onItemUseFinish.
        // To ensure that we provide the original ItemStack in the event,
        // we make a copy of in our onUpdateItemUse redirect
        // If the event or transaction is cancelled, we make sure to explicitly
        // set the copy back in the player's hand, since it may have been already
        // modified if an ItemFood is being used.

        final ItemStackSnapshot activeItemStackSnapshot = ItemStackUtil.snapshotOf(this.impl$activeItemStackCopy == null ? ItemStack.EMPTY : this.impl$activeItemStackCopy);


        final UseItemStackEvent.Replace event;
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(stack == null ? ItemStack.EMPTY : stack);
            final HandType handType = (HandType) (Object) hand;
            this.addSelfToFrame(frame, activeItemStackSnapshot, handType);
            event = SpongeEventFactory.createUseItemStackEventReplace(Sponge.getCauseStackManager().getCurrentCause(),
                    this.activeItemStackUseCount, this.activeItemStackUseCount, activeItemStackSnapshot,
                    new Transaction<>(ItemStackUtil.snapshotOf(this.impl$activeItemStackCopy), snapshot));
        }

        if (SpongeImpl.postEvent(event)) {
            this.setHeldItem(hand, this.impl$activeItemStackCopy.copy());
            return;
        }

        if (!event.getItemStackResult().isValid()) {
            this.setHeldItem(hand, this.impl$activeItemStackCopy.copy());
            return;
        }

        setHeldItem(hand, ItemStackUtil.fromSnapshotToNative(event.getItemStackResult().getFinal()));
    }

    @SuppressWarnings("ConstantConditions")
    @Redirect(method = "stopActiveHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;"
                                                                             + "onPlayerStoppedUsing(Lnet/minecraft/world/World;Lnet/minecraft/entity/EntityLivingBase;I)V")) // stopActiveHand
    private void onStopPlayerUsing(final ItemStack stack, final World world, final LivingEntity self, final int duration) {
        if (this.world.isRemote) {
            stack.onPlayerStoppedUsing(world, self, duration);
            return;
        }
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(stack);
            final HandType handType = (HandType) (Object) this.getActiveHand();
            this.addSelfToFrame(frame, snapshot, handType);
            if (!SpongeImpl.postEvent(SpongeEventFactory.createUseItemStackEventStop(Sponge.getCauseStackManager().getCurrentCause(),
                    duration, duration, snapshot))) {
                stack.onPlayerStoppedUsing(world, self, duration);
            }
        }
    }

    @Inject(method = "resetActiveHand", at = @At("HEAD"))
    private void onResetActiveHand(final CallbackInfo ci) {
        if (this.world.isRemote) {
            return;
        }

        // If we finished using an item, impl$activeItemStackCopy will be non-null
        // However, if a player stopped using an item early, impl$activeItemStackCopy will not be set
        final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(this.impl$activeItemStackCopy != null ? this.impl$activeItemStackCopy : this.activeItemStack);

        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            this.addSelfToFrame(frame, snapshot);
            SpongeImpl.postEvent(SpongeEventFactory.createUseItemStackEventReset(Sponge.getCauseStackManager().getCurrentCause(),
                    this.activeItemStackUseCount, this.activeItemStackUseCount, snapshot));
        }
        this.impl$activeItemStackCopy = null;
    }

    // End implementation of UseItemStackEvent

}
