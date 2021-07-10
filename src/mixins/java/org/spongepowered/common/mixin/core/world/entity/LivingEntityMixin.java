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
package org.spongepowered.common.mixin.core.world.entity;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.action.SleepingEvent;
import org.spongepowered.api.event.cause.entity.MovementTypes;
import org.spongepowered.api.event.cause.entity.damage.DamageFunction;
import org.spongepowered.api.event.cause.entity.damage.source.FallingBlockDamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.entity.EntityTypeBridge;
import org.spongepowered.common.bridge.world.entity.LivingEntityBridge;
import org.spongepowered.common.bridge.world.entity.PlatformLivingEntityBridge;
import org.spongepowered.common.bridge.world.entity.player.PlayerBridge;
import org.spongepowered.common.entity.living.human.HumanEntity;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.util.DamageEventUtil;
import org.spongepowered.common.event.cause.entity.damage.SpongeDamageSources;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.PrettyPrinter;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

@SuppressWarnings("ConstantConditions")
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends EntityMixin implements LivingEntityBridge, PlatformLivingEntityBridge {

    // @formatter:off
    @Shadow public int hurtTime;
    @Shadow public int hurtDuration;
    @Shadow public float hurtDir;
    @Shadow public float animationSpeed;
    @Shadow protected int noActionTime;
    @Shadow protected int lastHurtByPlayerTime;
    @Shadow protected int useItemRemaining;
    @Shadow protected float lastHurt;
    @Shadow @Nullable protected Player lastHurtByPlayer;
    @Shadow private DamageSource lastDamageSource;
    @Shadow private long lastDamageStamp;
    @Shadow protected boolean dead;
    @Shadow public int deathTime;
    @Shadow protected int deathScore;
    @Shadow protected ItemStack useItem;

    @Shadow public abstract AttributeInstance shadow$getAttribute(Attribute attribute);
    @Shadow public abstract void shadow$setHealth(float health);
    @Shadow public abstract void shadow$knockback(float p_70653_2_, double p_70653_3_, double p_70653_5_);
    @Shadow public abstract void shadow$setLastHurtByMob(LivingEntity livingBase);
    @Shadow public abstract void shadow$setAbsorptionAmount(float amount);
    @Shadow public abstract void shadow$setItemInHand(InteractionHand hand, @Nullable ItemStack stack);
    @Shadow public abstract void shadow$stopUsingItem();
    @Shadow public abstract int shadow$getUseItemRemainingTicks();
    @Shadow public abstract float shadow$getAbsorptionAmount();
    @Shadow public abstract float shadow$getHealth();
    @Shadow public abstract boolean shadow$hasEffect(MobEffect potion);
    @Shadow protected abstract boolean shadow$isDamageSourceBlocked(DamageSource p_184583_1_);
    @Shadow public abstract ItemStack shadow$getItemBySlot(EquipmentSlot slotIn);
    @Shadow public abstract ItemStack shadow$getMainHandItem();
    @Shadow public abstract CombatTracker shadow$getCombatTracker();
    @Shadow public void shadow$kill() { }
    @Shadow public abstract InteractionHand shadow$getUsedItemHand();
    @Shadow protected abstract void shadow$markHurt();
    @Shadow protected abstract void shadow$hurtCurrentlyUsedShield(float p_184590_1_);
    @Shadow protected abstract void shadow$playHurtSound(DamageSource p_184581_1_);
    @Shadow protected abstract void shadow$blockUsingShield(LivingEntity p_190629_1_);
    @Shadow protected abstract float shadow$getSoundVolume();
    @Shadow protected abstract float shadow$getVoicePitch();
    @Shadow protected abstract SoundEvent shadow$getDeathSound();
    @Shadow public abstract boolean shadow$isSleeping();
    @Shadow public abstract Optional<BlockPos> shadow$getSleepingPos();
    @Shadow private boolean shadow$checkTotemDeathProtection(final DamageSource p_190628_1_) {
        return false; // SHADOWED
    }
    @Shadow public abstract void shadow$die(DamageSource cause);
    @Shadow protected abstract void shadow$spawnItemParticles(ItemStack stack, int count);
    @Shadow public abstract void shadow$stopSleeping();
    @Shadow protected abstract void shadow$actuallyHurt(DamageSource damageSrc, float damageAmount);
    @Shadow public abstract boolean shadow$onClimbable();
    @Shadow public abstract void shadow$setSprinting(boolean sprinting);
    @Shadow public abstract void shadow$setLastHurtMob(Entity entityIn);
    @Shadow protected abstract void shadow$hurtArmor(DamageSource source, float damage);
    @Shadow public abstract ItemStack shadow$getItemInHand(InteractionHand hand);
    @Shadow protected abstract void shadow$dropEquipment();
    @Shadow protected abstract void shadow$dropAllDeathLoot(DamageSource damageSourceIn);
    @Shadow @Nullable public abstract LivingEntity shadow$getKillCredit();
    @Shadow protected abstract void shadow$createWitherRose(@Nullable LivingEntity p_226298_1_);
    @Shadow  public abstract Collection<MobEffectInstance> shadow$getActiveEffects();
    @Shadow public abstract float shadow$getMaxHealth();
    @Shadow public abstract AttributeMap shadow$getAttributes();
    @Shadow public abstract void shadow$clearSleepingPos();
    // @formatter:on

    @Nullable private ItemStack impl$activeItemStackCopy;
    @Nullable private Vector3d impl$preTeleportPosition;
    private int impl$deathEventsPosted;

    @Override
    public boolean bridge$damageEntity(final DamageSource damageSource, float damage) {
        if (this.shadow$isInvulnerableTo(damageSource)) {
            return false;
        }
        final boolean isHuman = (LivingEntity) (Object) this instanceof Player;
        // Sponge Start - Call platform hook for adjusting damage
        damage = this.bridge$applyModDamage((LivingEntity) (Object) this, damageSource, damage);
        // Sponge End
        final float originalDamage = damage;
        if (damage <= 0) {
            return false;
        }

        final List<DamageFunction> originalFunctions = new ArrayList<>();
        final Optional<DamageFunction> hardHatFunction =
                DamageEventUtil.createHardHatModifier((LivingEntity) (Object) this, damageSource);
        final Optional<DamageFunction> armorFunction =
                DamageEventUtil.createArmorModifiers((LivingEntity) (Object) this, damageSource);
        final Optional<DamageFunction> resistanceFunction =
                DamageEventUtil.createResistanceModifier((LivingEntity) (Object) this, damageSource);
        final Optional<List<DamageFunction>> armorEnchantments =
                DamageEventUtil.createEnchantmentModifiers((LivingEntity) (Object) this, damageSource);
        final Optional<DamageFunction> absorptionFunction =
                DamageEventUtil.createAbsorptionModifier((LivingEntity) (Object) this);
        final Optional<DamageFunction> shieldFunction =
                DamageEventUtil.createShieldFunction((LivingEntity) (Object) this, damageSource, damage);

        hardHatFunction.ifPresent(originalFunctions::add);

        shieldFunction.ifPresent(originalFunctions::add);

        armorFunction.ifPresent(originalFunctions::add);

        resistanceFunction.ifPresent(originalFunctions::add);

        armorEnchantments.ifPresent(originalFunctions::addAll);

        absorptionFunction.ifPresent(originalFunctions::add);
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            DamageEventUtil.generateCauseFor(damageSource, frame);

            final DamageEntityEvent event = SpongeEventFactory
                    .createDamageEntityEvent(frame.currentCause(), (org.spongepowered.api.entity.Entity) this, originalFunctions,
                            originalDamage);
            if (damageSource
                    != SpongeDamageSources.IGNORED) { // Basically, don't throw an event if it's our own damage source
                SpongeCommon.post(event);
            }
            if (event.isCancelled()) {
                return false;
            }

            damage = (float) event.finalDamage();

            // Sponge Start - Allow the platform to adjust damage before applying armor/etc
            damage = this.bridge$applyModDamageBeforeFunctions((LivingEntity) (Object) this, damageSource, damage);
            // Sponge End

            // Helmet
            final ItemStack helmet = this.shadow$getItemBySlot(EquipmentSlot.HEAD);
            // We still sanity check if a mod is calling to damage the entity with an anvil or falling block
            // without using our mixin redirects in EntityFallingBlockMixin.
            if ((damageSource instanceof FallingBlockDamageSource) || damageSource == DamageSource.ANVIL
                    || damageSource == DamageSource.FALLING_BLOCK && !helmet.isEmpty()) {
                helmet.hurtAndBreak((int) (event.baseDamage() * 4.0F + this.random.nextFloat() * event.baseDamage() * 2.0F),
                        (LivingEntity) (Object) this, (entity) -> {
                            entity.broadcastBreakEvent(EquipmentSlot.HEAD);
                        });
            }

            // Shield
            if (shieldFunction.isPresent()) {
                this.shadow$hurtCurrentlyUsedShield((float) event.baseDamage());
                if (!damageSource.isProjectile()) {
                    final Entity entity = damageSource.getDirectEntity();

                    if (entity instanceof LivingEntity) {
                        this.shadow$blockUsingShield((LivingEntity) entity);
                    }
                }
            }

            // Armor
            if (!damageSource.isBypassArmor() && armorFunction.isPresent()) {
                this.shadow$hurtArmor(damageSource, (float) event.baseDamage());
            }
            // Resistance modifier post calculation
            if (resistanceFunction.isPresent()) {
                final float f2 = (float) event.damage(resistanceFunction.get().modifier()) - damage;
                if (f2 > 0.0F && f2 < 3.4028235E37F) {
                    if (((LivingEntity) (Object) this) instanceof net.minecraft.server.level.ServerPlayer) {
                        ((net.minecraft.server.level.ServerPlayer) ((LivingEntity) (Object) this)).awardStat(Stats.DAMAGE_RESISTED, Math.round(f2 * 10.0F));
                    } else if (damageSource.getEntity() instanceof net.minecraft.server.level.ServerPlayer) {
                        ((net.minecraft.server.level.ServerPlayer) damageSource.getEntity()).awardStat(Stats.DAMAGE_DEALT_RESISTED, Math.round(f2 * 10.0F));
                    }
                }
            }


            double absorptionModifier = absorptionFunction.map(function -> event.damage(function.modifier())).orElse(0d);
            if (absorptionFunction.isPresent()) {
                absorptionModifier = event.damage(absorptionFunction.get().modifier());

            }

            final float f = (float) event.finalDamage() - (float) absorptionModifier;
            this.shadow$setAbsorptionAmount(Math.max(this.shadow$getAbsorptionAmount() + (float) absorptionModifier, 0.0F));
            if (f > 0.0F && f < 3.4028235E37F && damageSource.getEntity() instanceof net.minecraft.server.level.ServerPlayer) {
                ((net.minecraft.server.level.ServerPlayer) damageSource.getEntity()).awardStat(Stats.DAMAGE_DEALT_ABSORBED, Math.round(f * 10.0F));
            }
            if (damage != 0.0F) {
                if (isHuman) {
                    ((Player) (Object) this).causeFoodExhaustion(damageSource.getFoodExhaustion());
                }
                final float f2 = this.shadow$getHealth();

                this.shadow$setHealth(f2 - damage);
                this.shadow$getCombatTracker().recordDamage(damageSource, f2, damage);

                if (isHuman) {
                    return true;
                }

                this.shadow$setAbsorptionAmount(this.shadow$getAbsorptionAmount() - damage);
            }
            return true;
        }
    }

    /**
     * Due to cancelling death events, "healing" the entity is the only way to cancel the death, but we still
     * want to reset the death event counter. This is the simplest way to get it working with forge mods who
     * do not have access to Sponge's API.
     */
    @Inject(method = "setHealth",
        at = @At("HEAD"))
    private void impl$resetDeathEventCounter(final float health, final CallbackInfo info) {
        if (this.shadow$getHealth() <= 0 && health > 0) {
            this.impl$deathEventsPosted = 0;
        }
    }

    @Redirect(method = "dropExperience()V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getExperienceReward(Lnet/minecraft/world/entity/player/Player;)I"))
    protected int impl$exposeGetExperienceForDeath(final LivingEntity entity, final Player attackingPlayer) {
        return this.bridge$getExperiencePointsOnDeath(entity, attackingPlayer);
    }

    /**
     * @author bloodmc
     * @author zidane
     * @reason This shouldn't be used internally but a mod may still call it so we simply reroute to our hook.
     */
    @Overwrite
    protected void actuallyHurt(final DamageSource damageSource, final float damage) {
        this.bridge$damageEntity(damageSource, damage);
    }

    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void impl$throwDestructEntityDeath(DamageSource cause, CallbackInfo ci) {
        final boolean throwEvent = !((WorldBridge) this.level).bridge$isFake() && Sponge.isServerAvailable() && Sponge.server().onMainThread();
        if (!this.dead) { // isDead should be set later on in this method so we aren't re-throwing the events.
            if (throwEvent && this.impl$deathEventsPosted <= Constants.Sponge.MAX_DEATH_EVENTS_BEFORE_GIVING_UP) {
                // ignore because some moron is not resetting the entity.
                this.impl$deathEventsPosted++;
                if (SpongeCommonEventFactory.callDestructEntityEventDeath((LivingEntity) (Object) this, cause).isCancelled()) {
                    // Since the forge event is cancellable
                    ci.cancel();
                }
            }
        } else {
            this.impl$deathEventsPosted = 0;
        }
    }

    @Inject(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;broadcastEntityEvent(Lnet/minecraft/world/entity/Entity;B)V"),
            cancellable = true)
    private void impl$doNotSendStateForHumans(DamageSource cause, CallbackInfo ci) {
        if (((LivingEntity) (Object) this) instanceof HumanEntity) {
            ci.cancel();
        }
    }

    @Redirect(method = "dropAllDeathLoot",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;dropEquipment()V"
            )
    )
    private void tracker$dropInventory(final LivingEntity thisEntity) {
        if (thisEntity instanceof PlayerBridge && ((PlayerBridge) thisEntity).bridge$keepInventory()) {
            return;
        }
        this.shadow$dropEquipment();
    }

    /**
     * @author bloodmc - November 22, 2015
     * @author gabizou - Updated April 11th, 2016 - Update for 1.9 changes
     * @author Aaron1011 - Updated Nov 11th, 2016 - Update for 1.11 changes
     * @author gabizou - Updated Nov 15th, 2020 - Update for 1.15 changes
     *
     * @reason Reroute damageEntity calls to our hook in order to prevent damage.
     */
    @Overwrite
    public boolean hurt(final DamageSource source, final float amount) {
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
                .log(SpongeCommon.logger(), Level.WARN);
            return false;
        }
        // Sponge - This hook is for forge use mainly
        if (!this.bridge$onLivingAttack((LivingEntity) (Object) this, source, amount)) {
            return false;
        }
        // Sponge end
        if (this.shadow$isInvulnerableTo(source)) {
            return false;
        } else if (this.level.isClientSide) {
            return false;
            // Sponge - Also ignore our customary damage source
        } else if (this.shadow$getHealth() <= 0.0F && source != SpongeDamageSources.IGNORED) {
            return false;
        } else if (source.isFire() && this.shadow$hasEffect(MobEffects.FIRE_RESISTANCE)) {
            return false;
        } else {
            if (this.shadow$isSleeping() && !this.level.isClientSide) {
                this.shadow$stopSleeping();
            }

            this.noActionTime = 0;
            final float f = amount;
            // Sponge - ignore as this is handled in our damageEntityHook
//                if ((source == DamageSource.ANVIL || source == DamageSource.FALLING_BLOCK) && !this.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty())
//                {
//                    this.getItemStackFromSlot(EquipmentSlotType.HEAD).damageItem((int)(amount * 4.0F + this.rand.nextFloat() * amount * 2.0F), this, (p_213341_0_) -> {
//                        p_213341_0_.sendBreakAnimation(EquipmentSlotType.HEAD);
//                    });
//                    amount *= 0.75F;
//                }
            // Sponge End

            // Sponge - set the 'shield blocking ran' flag to the proper value, since
            // we comment out the logic below
            float f1 = 0.0F;
            final boolean flag = amount > 0.0F && this.shadow$isDamageSourceBlocked(source);

            // Sponge start - this is handled in our bridge$damageEntityHook
            // but we need to account for the amount later.
            if (flag) {
                f1 = amount;
            }
//                boolean flag = false;
//
//                if (amount > 0.0F && this.shadow$canBlockDamageSource(source))
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

            this.animationSpeed = 1.5F;
            boolean flag1 = true;

            if ((float) this.invulnerableTime > 10.0F) {
                if (amount <= this.lastHurt) { // Technically, this is wrong since 'amount' won't be 0 if a shield is used. However, we need bridge$damageEntityHook so that we process the shield, so we leave it as-is
                    return false;
                }

                // Sponge start - reroute to our damage hook
                // only if the class is unmodded. If it's a modded class, then it should be calling our
                // damageEntity method, which would re-run our bridge$damageEntityHook.
                if (((EntityTypeBridge) this.shadow$getType()).bridge$overridesDamageEntity()) {
                    this.shadow$actuallyHurt(source, amount - this.lastHurt);
                } else {
                    if (!this.bridge$damageEntity(source, amount - this.lastHurt)) {
                        return false;
                    }
                }
                // this.damageEntity(source, amount - this.lastHurt); // handled above
                // Sponge end
                this.lastHurt = amount;
                flag1 = false;
            } else {
                // Sponge start - reroute to our damage hook
                if (((EntityTypeBridge) this.shadow$getType()).bridge$overridesDamageEntity()) {
                    this.shadow$actuallyHurt(source, amount);
                } else {
                    if (!this.bridge$damageEntity(source, amount)) {
                        return false;
                    }
                }
                this.lastHurt = amount;
                this.invulnerableTime = 20;
                // this.damageEntity(source, amount); // handled above
                // Sponge end
                this.hurtDuration = 10;
                this.hurtTime = this.hurtDuration;
            }

            this.hurtDir = 0.0F;
            final Entity entity = source.getEntity();

            if (entity != null) {
                if (entity instanceof LivingEntity) {
                    this.shadow$setLastHurtByMob((LivingEntity)entity);
                }

                if (entity instanceof Player) {
                    this.lastHurtByPlayerTime = 100;
                    this.lastHurtByPlayer = (Player)entity;
                // Forge Start - use TameableEntity instead of WolfEntity
                // } else if (entity1 instanceof WolfEntity) {
                //    WolfEntity wolfentity = (WolfEntity)entity1;
                } else if (entity instanceof TamableAnimal) {
                    TamableAnimal wolfentity = (TamableAnimal)entity;
                    if (wolfentity.isTame()) {
                        this.lastHurtByPlayerTime = 100;
                        LivingEntity livingentity = wolfentity.getOwner();
                        if (livingentity != null && livingentity.getType() == EntityType.PLAYER) {
                            this.lastHurtByPlayer = (Player)livingentity;
                        } else {
                            this.lastHurtByPlayer = null;
                        }
                    }
                }
            }

            if (flag1) {
                if (flag) {
                    this.level.broadcastEntityEvent((LivingEntity) (Object) this, (byte) 29);
                } else if (source instanceof EntityDamageSource && ((EntityDamageSource) source).isThorns()) {
                    this.level.broadcastEntityEvent((LivingEntity) (Object) this, (byte) 33);
                } else {
                    final byte b0;
                    if (source == DamageSource.DROWN) {
                        b0 = 36;
                    } else if (source.isFire()) {
                        b0 = 37;
                    } else if (source == DamageSource.SWEET_BERRY_BUSH) {
                        b0 = 44;
                    } else {
                        b0 = 2;
                    }

                    this.level.broadcastEntityEvent((LivingEntity) (Object) this, b0);
                }


                if (source != DamageSource.DROWN && !flag) { // Sponge - remove 'amount > 0.0F' - it's redundant in Vanilla, and breaks our handling of shields
                    this.shadow$markHurt();
                }

                if (entity != null) {
                    double d1 = entity.getX() - this.shadow$getX();
                    double d0;

                    for (d0 = entity.getZ() - this.shadow$getZ(); d1 * d1 + d0 * d0 < 1.0E-4D; d0 = (Math.random() - Math.random()) * 0.01D) {
                        d1 = (Math.random() - Math.random()) * 0.01D;
                    }

                    this.hurtDir = (float) (Mth.atan2(d0, d1) * 57.2957763671875D - (double) this.yRot);
                    this.shadow$knockback(0.4F, d1, d0);
                } else {
                    this.hurtDir = (float) (Math.random() * 2.0D * 180);
                }
            }

            if (this.shadow$getHealth() <= 0.0F) {
                if (!this.shadow$checkTotemDeathProtection(source)) {
                    final SoundEvent soundevent = this.shadow$getDeathSound();

                    // if (flag1 && soundevent != null) { Vanilla
                    // Sponge - Check that we're not vanished
                    if (!this.bridge$isVanished() && flag1 && soundevent != null) {
                        this.shadow$playSound(soundevent, this.shadow$getSoundVolume(), this.shadow$getVoicePitch());
                    }


                    this.shadow$die(source); // Sponge tracker will redirect this call
                }
            } else if (flag1) {
                // Sponge - Check if we're vanished
                if (!this.bridge$isVanished()) {
                    this.shadow$playHurtSound(source);
                }
            }

            final boolean flag2 = !flag;// Sponge - remove 'amount > 0.0F' since it's handled in the event
            if (flag2) {
                this.lastDamageSource = source;
                this.lastDamageStamp = this.level.getGameTime();
            }

            if ((LivingEntity) (Object) this instanceof net.minecraft.server.level.ServerPlayer) {
                CriteriaTriggers.ENTITY_HURT_PLAYER.trigger((net.minecraft.server.level.ServerPlayer) (Object) this, source, f, amount, flag);
                if (f1 > 0.0F && f1 < 3.4028235E37F) {
                    ((net.minecraft.server.level.ServerPlayer) (Object) this).awardStat(Stats.DAMAGE_BLOCKED_BY_SHIELD, Math.round(f1 * 10.0F));
                }
            }

            if (entity instanceof net.minecraft.server.level.ServerPlayer) {
                CriteriaTriggers.PLAYER_HURT_ENTITY.trigger((net.minecraft.server.level.ServerPlayer) entity, (Entity) (Object) this, source, f, amount, flag);
            }

            return flag2;
        }
    }

    @Redirect(method = "triggerItemUseEffects",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;spawnItemParticles(Lnet/minecraft/world/item/ItemStack;I)V"))
    private void impl$hideItemParticlesIfVanished(final LivingEntity livingEntity, final ItemStack stack, final int count) {
        if (!this.bridge$isVanished()) {
            this.shadow$spawnItemParticles(stack, count);
        }
    }

    @Inject(method = "randomTeleport", at = @At("HEAD"))
    private void impl$snapshotPositionBeforeVanillaTeleportLogic(double x, double y, double z, boolean changeState,
            CallbackInfoReturnable<Boolean> cir) {
        this.impl$preTeleportPosition = new Vector3d(this.shadow$getX(), this.shadow$getY(), this.shadow$getZ());
    }

    @Inject(method = "randomTeleport", at = @At(value = "RETURN", ordinal = 0, shift = At.Shift.BY, by = 2), cancellable = true)
    private void impl$callMoveEntityEventForTeleport(double x, double y, double z, boolean changeState,
            CallbackInfoReturnable<Boolean> cir) {
        if (!ShouldFire.MOVE_ENTITY_EVENT) {
            return;
        }

        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(this);

            // ENTITY_TELEPORT is our fallback context
            if (!frame.currentContext().containsKey(EventContextKeys.MOVEMENT_TYPE)) {
                frame.addContext(EventContextKeys.MOVEMENT_TYPE, MovementTypes.ENTITY_TELEPORT);
            }

            final MoveEntityEvent event = SpongeEventFactory.createMoveEntityEvent(frame.currentCause(),
                    (org.spongepowered.api.entity.Entity) this, this.impl$preTeleportPosition, new Vector3d(this.shadow$getX(), this.shadow$getY(),
                            this.shadow$getZ()),
                    new Vector3d(x, y, z));

            if (SpongeCommon.post(event)) {
                this.shadow$teleportTo(this.impl$preTeleportPosition.x(), this.impl$preTeleportPosition.y(),
                        this.impl$preTeleportPosition.z());
                cir.setReturnValue(false);
                return;
            }

            this.shadow$teleportTo(event.destinationPosition().x(), event.destinationPosition().y(),
                    event.destinationPosition().z());
        }
    }

    /**
     * @author gabizou - January 4th, 2016
     * @reason This allows invisiblity to ignore entity collisions.
     /*
    @Overwrite
    public boolean canBeCollidedWith() {
        return !(this.bridge$isVanished() && this.bridge$isUncollideable()) && !this.removed;
    }

    @Redirect(method = "updateFallState",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/server/ServerWorld;spawnParticle(Lnet/minecraft/particles/IParticleData;DDDIDDDD)I"))
    private int impl$vanishSpawnParticleForFallState(
        final ServerWorld serverWorld, final IParticleData particleTypes, final double xCoord, final double yCoord,
        final double zCoord, final int numberOfParticles, final double xOffset, final double yOffset,
        final double zOffset, final double particleSpeed) {
        if (!this.bridge$isVanished()) {
            return serverWorld.spawnParticle(particleTypes, xCoord, yCoord, zCoord, numberOfParticles, xOffset, yOffset, zOffset, particleSpeed);
        }
        return 0;
    }


    @Inject(method = "onItemUseFinish",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;resetActiveHand()V"))
    private void impl$updateHealthForUseFinish(final CallbackInfo ci) {
        if (this instanceof ServerPlayerEntityBridge) {
            ((ServerPlayerEntityBridge) this).bridge$refreshScaledHealth();
        }
    }

    // Data delegated methods
     */
    // Start implementation of UseItemstackEvent

    @Inject(method = "startUsingItem",
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILHARD,
        at = @At(value = "FIELD",
            target = "Lnet/minecraft/world/entity/LivingEntity;useItem:Lnet/minecraft/world/item/ItemStack;"))
    private void impl$onSetActiveItemStack(final InteractionHand hand, final CallbackInfo ci, final ItemStack stack) {
        if (this.level.isClientSide) {
            return;
        }

        final UseItemStackEvent.Start event;
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(stack);
            final HandType handType = (HandType) (Object) hand;
            this.impl$addSelfToFrame(frame, snapshot, handType);
            event = SpongeEventFactory.createUseItemStackEventStart(PhaseTracker.getCauseStackManager().currentCause(),
                stack.getUseDuration(), stack.getUseDuration(), snapshot);
        }

        if (SpongeCommon.post(event)) {
            ci.cancel();
        } else {
            this.useItemRemaining = event.remainingDuration();
        }
    }

    @Redirect(method = "startUsingItem",
        at = @At(value = "FIELD",
            target = "Lnet/minecraft/world/entity/LivingEntity;useItemRemaining:I"))
    private void impl$getItemDuration(final LivingEntity this$0, final int count) {
        if (this.level.isClientSide) {
            this.useItemRemaining = count;
        }
        // If we're on the server, do nothing, since we already set this field on onSetActiveItemStack
    }

    // A helper method for firing UseItemStackEvent sub-events
    // This ensures that the cause and context for these events
    // always have OWNER and NOTIFIER set (if possible),
    // as well as USED_ITEM and USED_HAND
    private void impl$addSelfToFrame(final CauseStackManager.StackFrame frame, final ItemStackSnapshot snapshot, final HandType hand) {
        frame.addContext(EventContextKeys.USED_HAND, hand);
        this.impl$addSelfToFrame(frame, snapshot);
    }

    private void impl$addSelfToFrame(final CauseStackManager.StackFrame frame, final ItemStackSnapshot snapshot) {
        frame.pushCause(this);
        frame.addContext(EventContextKeys.USED_ITEM, snapshot);
        if (this instanceof ServerPlayer) {
            frame.addContext(EventContextKeys.CREATOR, ((ServerPlayer) this).user());
            frame.addContext(EventContextKeys.NOTIFIER, ((ServerPlayer) this).user());
        }
    }

    @Redirect(method = "updatingUsingItem",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getUseItemRemainingTicks()I",
            ordinal = 0))
    private int impl$onGetRemainingItemDuration(final LivingEntity self) {
        if (this.level.isClientSide) {
            return self.getUseItemRemainingTicks();
        }

        final UseItemStackEvent.Tick event;
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(this.useItem);
            final HandType handType = (HandType) (Object) this.shadow$getUsedItemHand();
            this.impl$addSelfToFrame(frame, snapshot, handType);
            event = SpongeEventFactory.createUseItemStackEventTick(PhaseTracker.getCauseStackManager().currentCause(),
                this.useItemRemaining, this.useItemRemaining, snapshot);
            SpongeCommon.post(event);
        }
        // Because the item usage will only finish if useItemRemaining == 0 and decrements it first, it should be >= 1
        this.useItemRemaining = Math.max(event.remainingDuration(), 1);

        if (event.isCancelled()) {
            // Get prepared for some cool hacks: We're within the condition for updateItemUse
            // So if we don't want it to call the method we just pass a value that makes the
            // condition evaluate to false, so an integer >= 25
            return 26;
        }

        return this.shadow$getUseItemRemainingTicks();
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "completeUsingItem",
        cancellable = true,
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;triggerItemUseEffects(Lnet/minecraft/world/item/ItemStack;I)V"))
    private void impl$onUpdateItemUse(final CallbackInfo ci) {
        if (this.level.isClientSide) {
            return;
        }


        final UseItemStackEvent.Finish event;
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(this.useItem);
            final HandType handType = (HandType) (Object) this.shadow$getUsedItemHand();
            this.impl$addSelfToFrame(frame, snapshot, handType);
            event = SpongeEventFactory.createUseItemStackEventFinish(PhaseTracker.getCauseStackManager().currentCause(),
                this.useItemRemaining, this.useItemRemaining, snapshot);
        }
        SpongeCommon.post(event);
        if (event.remainingDuration() > 0) {
            this.useItemRemaining = event.remainingDuration();
            ci.cancel();
        } else if (event.isCancelled()) {
            this.shadow$stopUsingItem();
            ci.cancel();
        } else {
            this.impl$activeItemStackCopy = this.useItem.copy();
        }
    }

    @Redirect(method = "completeUsingItem",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;setItemInHand(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V"))
    private void impl$onSetHeldItem(final LivingEntity self, final InteractionHand hand, final ItemStack stack) {
        if (this.level.isClientSide) {
            self.setItemInHand(hand, stack);
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
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(stack == null ? ItemStack.EMPTY : stack);
            final HandType handType = (HandType) (Object) hand;
            this.impl$addSelfToFrame(frame, activeItemStackSnapshot, handType);
            event = SpongeEventFactory.createUseItemStackEventReplace(PhaseTracker.getCauseStackManager().currentCause(),
                this.useItemRemaining, this.useItemRemaining, activeItemStackSnapshot,
                new Transaction<>(ItemStackUtil.snapshotOf(this.impl$activeItemStackCopy), snapshot));
        }

        if (SpongeCommon.post(event)) {
            this.shadow$setItemInHand(hand, this.impl$activeItemStackCopy.copy());
            return;
        }

        if (!event.itemStackResult().isValid()) {
            this.shadow$setItemInHand(hand, this.impl$activeItemStackCopy.copy());
            return;
        }

        this.shadow$setItemInHand(hand, ItemStackUtil.fromSnapshotToNative(event.itemStackResult().finalReplacement()));
    }

    @SuppressWarnings("ConstantConditions")
    @Redirect(method = "releaseUsingItem",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;releaseUsing(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V"))
    // stopActiveHand
    private void impl$onStopPlayerUsing(final ItemStack stack, final net.minecraft.world.level.Level world, final LivingEntity self, final int duration) {
        if (this.level.isClientSide) {
            stack.releaseUsing(world, self, duration);
            return;
        }
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(stack);
            final HandType handType = (HandType) (Object) this.shadow$getUsedItemHand();
            this.impl$addSelfToFrame(frame, snapshot, handType);
            if (!SpongeCommon.post(SpongeEventFactory.createUseItemStackEventStop(PhaseTracker.getCauseStackManager().currentCause(),
                duration, duration, snapshot))) {
                stack.releaseUsing(world, self, duration);
            }
        }
    }

    @Inject(method = "stopUsingItem",
        at = @At("HEAD"))
    private void impl$onResetActiveHand(final CallbackInfo ci) {
        if (this.level.isClientSide) {
            return;
        }

        // If we finished using an item, impl$activeItemStackCopy will be non-null
        // However, if a player stopped using an item early, impl$activeItemStackCopy will not be set
        final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(this.impl$activeItemStackCopy != null ? this.impl$activeItemStackCopy : this.useItem);

        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            this.impl$addSelfToFrame(frame, snapshot);
            SpongeCommon.post(SpongeEventFactory.createUseItemStackEventReset(PhaseTracker.getCauseStackManager().currentCause(),
                this.useItemRemaining, this.useItemRemaining, snapshot));
        }
        this.impl$activeItemStackCopy = null;
    }

    // End implementation of UseItemStackEvent

    @Inject(method = "stopSleeping", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;clearSleepingPos()V"))
    private void impl$callFinishSleepingEvent(CallbackInfo ci) {
        final Optional<BlockPos> sleepingPos = this.shadow$getSleepingPos();
        if (!sleepingPos.isPresent()) {
            return;
        }
        BlockSnapshot snapshot = ((ServerWorld) this.level).createSnapshot(sleepingPos.get().getX(), sleepingPos.get().getY(), sleepingPos.get().getZ());
        final Cause currentCause = Sponge.server().causeStackManager().currentCause();
        ServerLocation loc = ServerLocation.of((ServerWorld) this.level, VecHelper.toVector3d(this.shadow$position()));
        Vector3d rot = ((Living) this).rotation();
        final SleepingEvent.Finish event = SpongeEventFactory.createSleepingEventFinish(currentCause, loc, loc, rot, rot, snapshot, (Living) this);
        Sponge.eventManager().post(event);
        this.shadow$clearSleepingPos();
        if (event.toLocation().world() != this.level) {
            throw new UnsupportedOperationException("World change is not supported here.");
        }
        this.shadow$setPos(event.toLocation().x(), event.toLocation().y(), event.toLocation().z());
        ((Living) this).setRotation(event.toRotation());

    }
}
