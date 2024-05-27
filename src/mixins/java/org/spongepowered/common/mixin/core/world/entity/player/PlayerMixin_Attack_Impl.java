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
package org.spongepowered.common.mixin.core.world.entity.player;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.entity.damage.DamageFunction;
import org.spongepowered.api.event.cause.entity.damage.DamageModifier;
import org.spongepowered.api.event.cause.entity.damage.DamageModifierTypes;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.ModifierFunction;
import org.spongepowered.api.event.entity.AttackEntityEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.context.transaction.TransactionalCaptureSupplier;
import org.spongepowered.common.event.tracking.context.transaction.inventory.PlayerInventoryTransaction;
import org.spongepowered.common.hooks.EventHooks;
import org.spongepowered.common.hooks.PlatformHooks;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.mixin.core.world.entity.LivingEntityMixin;
import org.spongepowered.common.util.DamageEventUtil;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ConstantConditions")
@Mixin(value = Player.class, priority = 900)
public abstract class PlayerMixin_Attack_Impl extends LivingEntityMixin {

    //@formatter:off
    @Shadow @Final public InventoryMenu inventoryMenu;
    @Shadow public abstract float shadow$getAttackStrengthScale(float p_184825_1_);
    @Shadow public abstract void shadow$resetAttackStrengthTicker();
    @Shadow public abstract float shadow$getSpeed();
    @Shadow public abstract void shadow$sweepAttack();
    @Shadow public abstract void shadow$crit(Entity p_71009_1_);
    @Shadow public abstract void shadow$magicCrit(Entity p_71047_1_);
    @Shadow public abstract void shadow$awardStat(ResourceLocation stat, int amount);
    @Shadow public abstract void shadow$causeFoodExhaustion(float p_71020_1_);
    //@formatter:on

    /**
     * @author gabizou - April 8th, 2016
     * @author gabizou - April 11th, 2016 - Update for 1.9 - This enitre method was rewritten
     * @author i509VCB - February 15th, 2020 - Update for 1.14.4
     * @author gabizou - November 15th, 2020 - Update for 1.15.2
     *
     * @reason Rewrites the attack to throw an {@link AttackEntityEvent} prior
     * to the ensuing {@link org.spongepowered.api.event.entity.DamageEntityEvent}. This should cover all cases where players are
     * attacking entities and those entities override {@link LivingEntity#hurt(DamageSource, float)}
     * and effectively bypass our damage event hooks.
     *
     * LVT Rename Table:
     * float f        | damage               |
     * float f1       | enchantmentDamage    |
     * float f2       | attackStrength       |
     * boolean flag   | isStrongAttack       |
     * boolean flag1  | isSprintingAttack    |
     * boolean flag2  | isCriticalAttack     | Whether critical particles will spawn and of course, multiply the output damage
     * boolean flag3  | isSweapingAttack     | Whether the player is sweaping an attack and will deal AoE damage
     * int i          | knockbackModifier    | The knockback modifier, must be set from the event after it has been thrown
     * float f4       | targetOriginalHealth | This is initially set as the entity original health
     * boolean flag4  | litEntityOnFire      | This is an internal flag to where if the attack failed, the entity is no longer set on fire
     * int j          | fireAspectModifier   | Literally just to check that the weapon used has fire aspect enchantments
     * double d0      | distanceWalkedDelta  | This checks that the distance walked delta is more than the normal walking speed to evaluate if you're making a sweaping attack
     * double d1      | targetMotionX        | Current target entity motion x vector
     * double d2      | targetMotionY        | Current target entity motion y vector
     * double d3      | targetMotionZ        | Current target entity motion z vector
     * boolean flag5  | attackSucceeded      | Whether the attack event succeeded
     *
     * @param targetEntity The target entity
     */
    @Overwrite
    public void attack(final Entity targetEntity) {
        // Sponge Start - Add SpongeImpl hook to override in forge as necessary
        if (!PlatformHooks.INSTANCE.getEntityHooks().checkAttackEntity((net.minecraft.world.entity.player.Player) (Object) this, targetEntity)) {
            return;
        }
        // Sponge End
        if (targetEntity.isAttackable()) {
            if (!targetEntity.skipAttackInteraction((net.minecraft.world.entity.player.Player) (Object) this)) {
                // Sponge Start - Prepare our event values
                // float damage = (float) this.getEntityAttribute(Attributes.ATTACK_DAMAGE).getAttributeValue();
                final double originalBaseDamage = this.shadow$getAttribute(Attributes.ATTACK_DAMAGE).getValue();
                float damage = (float) originalBaseDamage;
                // Sponge End
                float enchantmentDamage = 0.0F;

                // Sponge Start - Redirect getting enchantments for our damage event handlers
                // if (targetEntity instanceof LivingEntity) {
                //     enchantmentDamage = EnchantmentHelper.getModifierForCreature(this.getHeldItemMainhand(), ((LivingEntity) targetEntity).getCreatureAttribute());
                // } else {
                //     enchantmentDamage = EnchantmentHelper.getModifierForCreature(this.getHeldItemMainhand(), CreatureAttribute.UNDEFINED);
                // }
                if (targetEntity.getType().is(EntityTypeTags.REDIRECTABLE_PROJECTILE) && targetEntity instanceof Projectile proj) {
                    proj.deflect(ProjectileDeflection.AIM_DEFLECT, (Player) (Object) this, (Player) (Object) this, true);
                    // sponge - move the reset attack strength early to avoid resetting it from other events
                    this.shadow$resetAttackStrengthTicker();
                    return;
                }
                final float attackStrength = this.shadow$getAttackStrengthScale(0.5F);

                final List<DamageFunction> originalFunctions = new ArrayList<>();

                final List<DamageFunction> enchantmentModifierFunctions = DamageEventUtil
                    .createAttackEnchantmentFunction(this.shadow$getMainHandItem(), targetEntity.getType(), attackStrength);
                // This is kept for the post-damage event handling
                final List<DamageModifier> enchantmentModifiers = enchantmentModifierFunctions.stream()
                    .map(ModifierFunction::modifier)
                    .toList();

                enchantmentDamage = (float) enchantmentModifierFunctions.stream()
                    .map(ModifierFunction::function)
                    .mapToDouble(function -> function.applyAsDouble(originalBaseDamage))
                    .sum();
                originalFunctions.addAll(enchantmentModifierFunctions);
                // Sponge End

                originalFunctions.add(
                    DamageEventUtil.provideCooldownAttackStrengthFunction((net.minecraft.world.entity.player.Player) (Object) this, attackStrength));
                damage = damage * (0.2F + attackStrength * attackStrength * 0.8F);
                enchantmentDamage = enchantmentDamage * attackStrength;
                this.shadow$resetAttackStrengthTicker();

                if (damage > 0.0F || enchantmentDamage > 0.0F) {
                    final boolean isStrongAttack = attackStrength > 0.9F;
                    boolean isSprintingAttack = false;
                    boolean isCriticalAttack = false;
                    boolean isSweapingAttack = false;
                    int knockbackModifier = 0;
                    knockbackModifier = knockbackModifier + EnchantmentHelper.getKnockbackBonus((net.minecraft.world.entity.player.Player) (Object) this);

                    if (this.shadow$isSprinting() && isStrongAttack) {
                        // Sponge - Only play sound after the event has be thrown and not cancelled.
                        // this.world.playSound((EntityPlayer) null, this.posX, this.posY, this.posZ, SoundEvents.entity_player_attack_knockback, this.getSoundCategory(), 1.0F, 1.0F);
                        ++knockbackModifier;
                        isSprintingAttack = true;
                    }

                    isCriticalAttack = isStrongAttack && this.fallDistance > 0.0F && !this.shadow$onGround() && !this.shadow$onClimbable() && !this.shadow$isInWater() && !this.shadow$hasEffect(
                        MobEffects.BLINDNESS) && !this.shadow$isPassenger() && targetEntity instanceof LivingEntity;
                    isCriticalAttack = isCriticalAttack && !this.shadow$isSprinting();
                    final EventHooks.CriticalHitResult criticalResult = PlatformHooks.INSTANCE.getEventHooks().callCriticalHitEvent((net.minecraft.world.entity.player.Player) (Object) this, targetEntity, isCriticalAttack, isCriticalAttack ? 0.5F : 0.0F);
                    isCriticalAttack = criticalResult.criticalHit;
                    if (isCriticalAttack) {
                        // Sponge Start - add critical attack tuple
                        // damage *= 1.5F; // Sponge - This is handled in the event
                        originalFunctions.add(DamageEventUtil.provideCriticalAttackTuple((net.minecraft.world.entity.player.Player) (Object) this, criticalResult.modifier));
                        // Sponge End
                    }

                    // damage = damage + enchantmentDamage; // Sponge - We don't need this since our event will re-assign the damage to deal
                    final double distanceWalkedDelta = (double) (this.walkDist - this.walkDistO);

                    final ItemStack heldItem = this.shadow$getMainHandItem();
                    if (isStrongAttack && !isCriticalAttack && !isSprintingAttack && this.shadow$onGround() && distanceWalkedDelta < (double) this.shadow$getSpeed()) {
                        if (PlatformHooks.INSTANCE.getItemHooks().canPerformSweepAttack(heldItem)) {
                            isSweapingAttack = true;
                        }
                    }

                    // Sponge Start - Create the event and throw it
                    final DamageSource damageSource = this.shadow$level().damageSources().playerAttack((net.minecraft.world.entity.player.Player) (Object) this);
                    final boolean isMainthread = !this.shadow$level().isClientSide;
                    if (isMainthread) {
                        PhaseTracker.getInstance().pushCause(damageSource);
                    }
                    final Cause currentCause = isMainthread ? PhaseTracker.getInstance().currentCause() : Cause.of(
                        EventContext.empty(), damageSource);
                    final AttackEntityEvent event = SpongeEventFactory.createAttackEntityEvent(currentCause, (org.spongepowered.api.entity.Entity) targetEntity,
                        originalFunctions, knockbackModifier, originalBaseDamage);
                    SpongeCommon.post(event);
                    if (isMainthread) {
                        PhaseTracker.getInstance().popCause();
                    }
                    if (event.isCancelled()) {
                        return;
                    }

                    damage = (float) event.finalOutputDamage();
                    // Sponge End

                    // Sponge Start - need final for later events
                    final double attackDamage = damage;
                    knockbackModifier = (int) event.knockbackModifier();
                    enchantmentDamage = (float) enchantmentModifiers.stream()
                        .mapToDouble(event::outputDamage)
                        .sum();
                    // Sponge End

                    float targetOriginalHealth = 0.0F;
                    boolean litEntityOnFire = false;
                    final int fireAspectModifier = EnchantmentHelper.getFireAspect((net.minecraft.world.entity.player.Player) (Object) this);

                    if (targetEntity instanceof LivingEntity) {
                        targetOriginalHealth = ((LivingEntity) targetEntity).getHealth();

                        if (fireAspectModifier > 0 && !targetEntity.isOnFire()) {
                            litEntityOnFire = true;
                            targetEntity.igniteForSeconds(1);
                        }
                    }

                    final Vec3 targetMotion = targetEntity.getDeltaMovement();
                    final boolean attackSucceeded = targetEntity.hurt(this.shadow$level().damageSources().playerAttack((net.minecraft.world.entity.player.Player) (Object) this), damage);

                    if (attackSucceeded) {
                        if (knockbackModifier > 0) {
                            if (targetEntity instanceof LivingEntity le) {
                                le.knockback(
                                    (float) knockbackModifier * 0.5F,
                                    Mth.sin(this.shadow$getYRot() * 0.017453292F),
                                    -Mth.cos(this.shadow$getYRot() * 0.017453292F)
                                );
                            } else {
                                targetEntity.push(
                                    -Mth.sin(this.shadow$getYRot() * 0.017453292F) * (float) knockbackModifier * 0.5F,
                                    0.1D,
                                    Mth.cos(this.shadow$getYRot() * 0.017453292F) * (float) knockbackModifier * 0.5F
                                );
                            }

                            this.shadow$setDeltaMovement(this.shadow$getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
                            this.shadow$setSprinting(false);
                        }

                        if (isSweapingAttack) {
                            // Sponge - add forge compatibility hook
                            final var hitbox = PlatformHooks.INSTANCE.getItemHooks().getSweepingHitBox(((Player) (Object) this), this.shadow$getItemInHand(InteractionHand.MAIN_HAND), targetEntity);
                            for (final LivingEntity livingEntity : this.shadow$level().getEntitiesOfClass(LivingEntity.class, hitbox)) {
                                if (livingEntity != (net.minecraft.world.entity.player.Player) (Object) this && livingEntity != targetEntity && !this.shadow$isAlliedTo(livingEntity) && (!(livingEntity instanceof ArmorStand) || !((ArmorStand)livingEntity).isMarker()) && this.shadow$distanceToSqr(livingEntity) < 9.0D) {
                                    // Sponge Start - Do a small event for these entities
                                    // livingEntity.knockBack(this, 0.4F, (double)MathHelper.sin(this.rotationYaw * 0.017453292F), (double)(-MathHelper.cos(this.rotationYaw * 0.017453292F)));
                                    // livingEntity.attackEntityFrom(DamageSource.causePlayerDamage(this), 1.0F);
                                    final var sweepingAttackSource = org.spongepowered.api.event.cause.entity.damage.source.DamageSource.builder().entity((org.spongepowered.api.entity.living.player.Player) this)
                                            .type(DamageTypes.PLAYER_ATTACK).build();
                                    try (final CauseStackManager.StackFrame frame = isMainthread ? PhaseTracker.getInstance().pushCauseFrame() : null) {
                                        if (isMainthread) {
                                            frame.pushCause(sweepingAttackSource);
                                        }
                                        final ItemStackSnapshot heldSnapshot = ItemStackUtil.snapshotOf(heldItem);
                                        if (isMainthread) {
                                            frame.addContext(EventContextKeys.WEAPON, heldSnapshot);
                                        }
                                        final DamageFunction sweapingFunction = DamageFunction.of(DamageModifier.builder()
                                                .cause(Cause.of(EventContext.empty(), heldSnapshot))
                                                .item(heldSnapshot)
                                                .type(DamageModifierTypes.SWEEPING)
                                                .build(),
                                            incoming -> EnchantmentHelper.getSweepingDamageRatio((net.minecraft.world.entity.player.Player) (Object) this) * attackDamage);
                                        final List<DamageFunction> sweapingFunctions = new ArrayList<>();
                                        sweapingFunctions.add(sweapingFunction);
                                        final AttackEntityEvent sweepingAttackEvent = SpongeEventFactory.createAttackEntityEvent(
                                            currentCause, (org.spongepowered.api.entity.Entity) livingEntity,
                                            sweapingFunctions, 1, 1.0D);
                                        SpongeCommon.post(sweepingAttackEvent);
                                        if (!sweepingAttackEvent.isCancelled()) {
                                            livingEntity.knockback(sweepingAttackEvent.knockbackModifier() * 0.4F,
                                                (double) Mth.sin(this.shadow$getYRot() * ((float)Math.PI / 180F)),
                                                (double) -Mth.cos(this.shadow$getYRot() * ((float)Math.PI / 180F)));

                                            livingEntity.hurt(this.shadow$level().damageSources().playerAttack((net.minecraft.world.entity.player.Player) (Object) this),
                                                (float) sweepingAttackEvent.finalOutputDamage());
                                        }
                                    }
                                    // Sponge End
                                }
                            }

                            if (this.bridge$vanishState().createsSounds()) {
                                this.shadow$level().playSound(
                                    null, this.shadow$getX(), this.shadow$getY(), this.shadow$getZ(),
                                    SoundEvents.PLAYER_ATTACK_SWEEP, this.shadow$getSoundSource(), 1.0F, 1.0F
                                );
                            }
                            this.shadow$sweepAttack();
                        }

                        if (targetEntity instanceof ServerPlayer sp && sp.hurtMarked) {
                            sp.connection.send(new ClientboundSetEntityMotionPacket(targetEntity));
                            sp.hurtMarked = false;
                            sp.setDeltaMovement(targetMotion);
                        }

                        if (isCriticalAttack) {
                            if (this.bridge$vanishState().createsSounds()) {
                                this.shadow$level().playSound(null, this.shadow$getX(), this.shadow$getY(), this.shadow$getZ(), SoundEvents.PLAYER_ATTACK_CRIT, this.shadow$getSoundSource(), 1.0F, 1.0F);
                            }
                            this.shadow$crit(targetEntity);
                        }

                        if (!isCriticalAttack && !isSweapingAttack && this.bridge$vanishState().createsSounds()) {
                            if (isStrongAttack) {
                                this.shadow$level().playSound(null, this.shadow$getX(), this.shadow$getY(), this.shadow$getZ(), SoundEvents.PLAYER_ATTACK_STRONG, this.shadow$getSoundSource(), 1.0F, 1.0F);
                            } else {
                                this.shadow$level().playSound(null, this.shadow$getX(), this.shadow$getY(), this.shadow$getZ(), SoundEvents.PLAYER_ATTACK_WEAK , this.shadow$getSoundSource(), 1.0F, 1.0F);
                            }
                        }

                        if (enchantmentDamage > 0.0F) {
                            this.shadow$magicCrit(targetEntity);
                        }

                        this.shadow$setLastHurtMob(targetEntity);

                        if (targetEntity instanceof LivingEntity le) {
                            EnchantmentHelper.doPostHurtEffects(le, (net.minecraft.world.entity.player.Player) (Object) this);
                        }

                        EnchantmentHelper.doPostDamageEffects((net.minecraft.world.entity.player.Player) (Object) this, targetEntity);
                        final ItemStack itemstack1 = this.shadow$getMainHandItem();
                        Entity entity = targetEntity;

                        // Sponge - Forge compatibility for multi-part entities
                        entity = PlatformHooks.INSTANCE.getEntityHooks().getParentPart(entity);
                        // if (targetEntity instanceof EnderDragonPart) {
                        //    entity = ((EnderDragonPart) targetEntity).parentMob;
                        // }

                        if(!this.shadow$level().isClientSide && !itemstack1.isEmpty() && entity instanceof LivingEntity le) {
                            itemstack1.hurtEnemy(le, (net.minecraft.world.entity.player.Player) (Object) this);
                            if(itemstack1.isEmpty()) {
                                // Sponge - platform hook for forge
                                PlatformHooks.INSTANCE.getEventHooks().callItemDestroyedEvent((net.minecraft.world.entity.player.Player) (Object) this, itemstack1, InteractionHand.MAIN_HAND);
                                this.shadow$setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                            }
                            // Sponge Start
                            final PhaseContext<@NonNull ?> context = PhaseTracker.SERVER.getPhaseContext();
                            final TransactionalCaptureSupplier transactor = context.getTransactor();
                            transactor.logPlayerInventoryChange((net.minecraft.world.entity.player.Player) (Object) this, PlayerInventoryTransaction.EventCreator.STANDARD);
                            this.inventoryMenu.broadcastChanges(); // capture
                            // Spong End
                        }

                        if (targetEntity instanceof LivingEntity le) {
                            final float f5 = targetOriginalHealth - le.getHealth();
                            this.shadow$awardStat(Stats.DAMAGE_DEALT, Math.round(f5 * 10.0F));

                            if (fireAspectModifier > 0) {
                                targetEntity.igniteForSeconds(fireAspectModifier * 4);
                            }

                            if (this.shadow$level() instanceof ServerLevel swe && f5 > 2.0F) {
                                final int k = (int) ((double) f5 * 0.5D);
                                swe.sendParticles(ParticleTypes.DAMAGE_INDICATOR, targetEntity.getX(), targetEntity.getY() + (double) (targetEntity.getBbHeight() * 0.5F), targetEntity.getZ(), k, 0.1D, 0.0D, 0.1D, 0.2D);
                            }
                        }

                        this.shadow$causeFoodExhaustion(0.1F);
                    } else {
                        if (this.bridge$vanishState().createsSounds()) {
                            this.shadow$level().playSound(null, this.shadow$getX(), this.shadow$getY(), this.shadow$getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, this.shadow$getSoundSource(), 1.0F, 1.0F);
                        }

                        if (litEntityOnFire) {
                            targetEntity.clearFire();
                        }
                    }
                }
            }
        }
    }

    /**
     * @author gabizou - January 26th, 2022
     * @reason Add changes according to
     */
    @Override
    @Overwrite
    protected void actuallyHurt(final DamageSource damageSource, final float damage) {
        this.bridge$damageEntity(damageSource, damage);
    }
}
