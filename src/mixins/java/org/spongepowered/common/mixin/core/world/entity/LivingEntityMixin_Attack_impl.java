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
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.Level;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.entity.EntityTypeBridge;
import org.spongepowered.common.bridge.world.entity.LivingEntityBridge;
import org.spongepowered.common.bridge.world.entity.PlatformLivingEntityBridge;
import org.spongepowered.common.event.cause.entity.damage.SpongeDamageSources;
import org.spongepowered.common.util.PrettyPrinter;

@Mixin(value = LivingEntity.class, priority = 900)
public abstract class LivingEntityMixin_Attack_impl extends EntityMixin
    implements LivingEntityBridge, PlatformLivingEntityBridge {

    //@formatter:off
    @Shadow private DamageSource lastDamageSource;
    @Shadow protected float lastHurt;
    @Shadow protected int noActionTime;
    @Shadow public int hurtDuration;
    @Shadow public int hurtTime;
    @Shadow protected int lastHurtByPlayerTime;
    @Shadow @Nullable protected Player lastHurtByPlayer;
    @Shadow private long lastDamageStamp;

    @Shadow public abstract boolean shadow$hasEffect(Holder<MobEffect> param0);
    @Shadow public abstract boolean shadow$isSleeping();
    @Shadow public abstract void shadow$stopSleeping();
    @Shadow public abstract boolean shadow$isDamageSourceBlocked(DamageSource param0);
    @Shadow protected abstract void shadow$actuallyHurt(DamageSource param0, float param1);
    @Shadow public abstract void shadow$setLastHurtByMob(@Nullable LivingEntity param0);
    @Shadow public abstract void shadow$knockback(double param0, double param1, double param2);
    @Shadow protected abstract boolean shadow$checkTotemDeathProtection(DamageSource param0);
    @Shadow @Nullable protected abstract SoundEvent shadow$getDeathSound();
    @Shadow protected abstract float shadow$getSoundVolume();
    @Shadow protected abstract float shadow$getVoicePitch();
    @Shadow public abstract void shadow$die(DamageSource param0);
    @Shadow protected abstract void shadow$playHurtSound(DamageSource param0);
    @Shadow public abstract boolean shadow$isDeadOrDying();
    @Shadow @Final public WalkAnimationState walkAnimation;
    @Shadow public abstract ItemStack shadow$getItemBySlot(final EquipmentSlot var1);
    @Shadow protected abstract void shadow$hurtHelmet(final DamageSource $$0, final float $$1);
    @Shadow public abstract void shadow$indicateDamage(final double $$0, final double $$1);

    // @formatter:on

    /**
     * @author bloodmc - November 22, 2015
     * @author gabizou - Updated April 11th, 2016 - Update for 1.9 changes
     * @author Aaron1011 - Updated Nov 11th, 2016 - Update for 1.11 changes
     * @author gabizou - Updated Nov 15th, 2020 - Update for 1.15 changes
     * @author gabizou - Updated Jan 26th, 2022 - Update for 1.16.5 changes
     * @reason Reroute damageEntity calls to our hook in order to prevent damage.
     */
    @Overwrite
    public boolean hurt(final DamageSource source, float amount) {
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
        } else if (this.shadow$level().isClientSide) {
            return false;
            // Sponge - Also ignore our customary damage source
        } else if (this.shadow$isDeadOrDying() && source != SpongeDamageSources.IGNORED) {
            return false;
        } else if (source.is(DamageTypeTags.IS_FIRE) && this.shadow$hasEffect(MobEffects.FIRE_RESISTANCE)) {
            return false;
        } else {
            if (this.shadow$isSleeping() && !this.shadow$level().isClientSide) {
                this.shadow$stopSleeping();
            }

            this.noActionTime = 0;
            final float f2 = amount;
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
            float blockedAmount = 0.0F;
            boolean isBlocked = false; // $$3

            if (amount > 0.0F && this.shadow$isDamageSourceBlocked(source)) {
// Sponge start - this is handled in our bridge$damageEntityHook
//                this.hurtCurrentlyUsedShield($$1);
                blockedAmount = amount;
//                amount = 0.0F;
//                if (!source.is(DamageTypeTags.IS_PROJECTILE)) {
//                    Entity $$5 = source.getDirectEntity();
//                    if ($$5 instanceof LivingEntity $$6) {
//                        this.blockUsingShield($$6);
//                    }
//                }
                isBlocked = true;
// Sponge end
            }

            if (source.is(DamageTypeTags.IS_FREEZING) && this.shadow$getType().is(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES)) {
                amount *= 5.0F;
            }

            this.walkAnimation.setSpeed(1.5F);
            boolean isNotInvulnerable = true; // $$7

            if ((float) this.invulnerableTime > 10.0F && !source.is(DamageTypeTags.BYPASSES_COOLDOWN)) {
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
                isNotInvulnerable = false;
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

            if (source.is(DamageTypeTags.DAMAGES_HELMET) && !this.shadow$getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
                this.shadow$hurtHelmet(source, amount);
                amount *= 0.75F;
            }

            final Entity entity = source.getEntity(); // $$8
            if (entity != null) {
                if (entity instanceof LivingEntity livingEntity && !source.is(DamageTypeTags.NO_ANGER)) {
                    this.shadow$setLastHurtByMob(livingEntity);
                }

                if (entity instanceof Player player) {
                    this.lastHurtByPlayerTime = 100;
                    this.lastHurtByPlayer = player;
                    // Forge Start - use TameableEntity instead of WolfEntity
                    // } else if (entity1 instanceof WolfEntity) {
                    //    WolfEntity wolfentity = (WolfEntity)entity1;
                } else if (entity instanceof TamableAnimal animal && animal.isTame()) {
                    this.lastHurtByPlayerTime = 100;
                    final LivingEntity owner = animal.getOwner();
                    if (owner instanceof Player player) {
                        this.lastHurtByPlayer = player;
                    } else {
                        this.lastHurtByPlayer = null;
                    }
                }
            }

            if (isNotInvulnerable) {
                if (isBlocked) {
                    this.shadow$level().broadcastEntityEvent((LivingEntity) (Object) this, (byte) 29);
                } else {
                    this.shadow$level().broadcastDamageEvent((LivingEntity) (Object) this, source);
                }

                if (!source.is(DamageTypeTags.NO_IMPACT) && (!isBlocked /*|| amount > 0.0F*/)) { // Sponge - remove 'amount > 0.0F' - it's redundant in Vanilla, and breaks our handling of shields
                    this.shadow$markHurt();
                }

                if (entity != null && !source.is(DamageTypeTags.IS_EXPLOSION)) {
                    double xDir = entity.getX() - this.shadow$getX(); // $$13
                    double zDir; // $$14

                    for (zDir = entity.getZ() - this.shadow$getZ();
                         xDir * xDir + zDir * zDir < 1.0E-4D;
                         zDir = (Math.random() - Math.random()) * 0.01D) {
                         xDir = (Math.random() - Math.random()) * 0.01D;
                    }

                    this.shadow$knockback(0.4F, xDir, zDir);
                    if (!isBlocked) {
                        this.shadow$indicateDamage(xDir, zDir);
                    }
                }
            }

            if (this.shadow$isDeadOrDying()) {
                if (!this.shadow$checkTotemDeathProtection(source)) {
                    final SoundEvent soundevent = this.shadow$getDeathSound();
                    if (isNotInvulnerable && soundevent != null) {
                        if (this.bridge$vanishState().createsSounds()) { // Sponge - Check that we're not vanished
                            this.shadow$playSound(soundevent, this.shadow$getSoundVolume(), this.shadow$getVoicePitch());
                        }
                    }

                    this.shadow$die(source); // Sponge tracker will redirect this call
                }
            } else if (isNotInvulnerable) {
                if (this.bridge$vanishState().createsSounds()) { // Sponge - Check that we're not vanished
                    this.shadow$playHurtSound(source);
                }
            }

            final boolean notBlocked = !isBlocked /* || $$1 > 0.0F*/;// Sponge - remove 'amount > 0.0F' since it's handled in the event
            if (notBlocked) {
                this.lastDamageSource = source;
                this.lastDamageStamp = this.shadow$level().getGameTime();
            }

            if ((LivingEntity) (Object) this instanceof net.minecraft.server.level.ServerPlayer player) {
                CriteriaTriggers.ENTITY_HURT_PLAYER.trigger(player, source, f2, amount, isBlocked);
                if (blockedAmount > 0.0F && blockedAmount < 3.4028235E37F) {
                    player.awardStat(Stats.DAMAGE_BLOCKED_BY_SHIELD, Math.round(blockedAmount * 10.0F));
                }
            }

            if (entity instanceof net.minecraft.server.level.ServerPlayer player) {
                CriteriaTriggers.PLAYER_HURT_ENTITY.trigger(player, (Entity) (Object) this, source, f2, amount, isBlocked);
            }

            return notBlocked;
        }
    }
}
