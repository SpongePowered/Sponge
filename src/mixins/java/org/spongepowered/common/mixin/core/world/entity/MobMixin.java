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

import org.objectweb.asm.Opcodes;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.ai.goal.GoalExecutorTypes;
import org.spongepowered.api.entity.living.Agent;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.entity.damage.DamageFunction;
import org.spongepowered.api.event.entity.AttackEntityEvent;
import org.spongepowered.api.event.entity.UnleashEntityEvent;
import org.spongepowered.api.event.entity.ai.SetAITargetEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.entity.GrieferBridge;
import org.spongepowered.common.bridge.world.entity.ai.goal.GoalSelectorBridge;
import org.spongepowered.common.bridge.world.entity.player.PlayerBridge;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.cause.entity.damage.DamageEventHandler;
import org.spongepowered.common.event.tracking.PhaseTracker;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntityMixin {

    // @formatter:off
    @Shadow @Final protected GoalSelector goalSelector;
    @Shadow @Final protected GoalSelector targetSelector;
    @Shadow @Nullable private LivingEntity target;
    @Shadow @Nullable public abstract net.minecraft.world.entity.Entity shadow$getLeashHolder();
    @Shadow protected abstract void shadow$registerGoals();
    @Shadow protected abstract void shadow$maybeDisableShield(Player p_233655_1_, ItemStack p_233655_2_, ItemStack p_233655_3_);
    // @formatter:on


    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;registerGoals()V"))
    private void impl$registerGoals(final Mob this$0) {
        this.impl$setupGoalSelectors();
        this.shadow$registerGoals();
    }

    private void impl$setupGoalSelectors() {
        if (!((GoalSelectorBridge) this.goalSelector).bridge$initialized()) {
            ((GoalSelectorBridge) this.goalSelector).bridge$setOwner((Mob) (Object) this);
            ((GoalSelectorBridge) this.goalSelector).bridge$setType(GoalExecutorTypes.NORMAL.get());
            ((GoalSelectorBridge) this.goalSelector).bridge$setInitialized(true);
        }
        if (!((GoalSelectorBridge) this.targetSelector).bridge$initialized()) {
            ((GoalSelectorBridge) this.targetSelector).bridge$setOwner((Mob) (Object) this);
            ((GoalSelectorBridge) this.targetSelector).bridge$setType(GoalExecutorTypes.TARGET.get());
            ((GoalSelectorBridge) this.targetSelector).bridge$setInitialized(true);
        }
    }

    @Inject(method = "dropLeash",
        at = @At(value = "FIELD",
            target = "Lnet/minecraft/world/entity/Mob;leashHolder:Lnet/minecraft/world/entity/Entity;",
            opcode = Opcodes.PUTFIELD
        ),
        cancellable = true)
    private void impl$ThrowUnleashEvent(final boolean sendPacket, final boolean dropLead, final CallbackInfo ci) {
        if (this.level.isClientSide) {
            return;
        }

        final net.minecraft.world.entity.Entity entity = this.shadow$getLeashHolder();

        final CauseStackManager csm = PhaseTracker.getCauseStackManager();
        if (entity == null) {
            csm.pushCause(this);
        } else {
            csm.pushCause(entity);
        }
        final UnleashEntityEvent event = SpongeEventFactory.createUnleashEntityEvent(csm.currentCause(), (Living) this);
        SpongeCommon.post(event);
        csm.popCause();
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    /**
     * @author gabizou - January 4th, 2016
     *
     * This is to instill the check that if the entity is vanish, check whether they're untargetable
     * as well.
     *
     * @param entitylivingbaseIn The entity living base coming in
     */
    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void onSetAttackTarget(@Nullable final LivingEntity entitylivingbaseIn, final CallbackInfo ci) {
        if (this.level.isClientSide || entitylivingbaseIn == null) {
            return;
        }
        //noinspection ConstantConditions
        if (EntityUtil.isUntargetable((net.minecraft.world.entity.Entity) (Object) this, entitylivingbaseIn)) {
            this.target = null;
            ci.cancel();
            return;
        }
        if (ShouldFire.SET_A_I_TARGET_EVENT) {
            final SetAITargetEvent event = SpongeCommonEventFactory.callSetAttackTargetEvent((Entity) entitylivingbaseIn, (Agent) this);
            if (event.isCancelled()) {
                ci.cancel();
            } else {
                this.target = ((LivingEntity) event.target().orElse(null));
            }
        }
    }

    /**
     * @author gabizou - January 4th, 2016
     * @reason This will still check if the current attack target
     * is vanish and is untargetable.
     *
     * @return The current attack target, if not null
     */
    @Nullable
    @Overwrite
    public LivingEntity getTarget() {
        // Sponge start
        if (this.target != null) {
            //noinspection ConstantConditions
            if (EntityUtil.isUntargetable((net.minecraft.world.entity.Entity) (Object) this, this.target)) {
                this.target = null;
            }
        }
        // Sponge end
        return this.target;
    }

    /**
     * @author gabizou - April 11th, 2018
     * @reason Instead of redirecting the gamerule request, redirecting the dead check
     * to avoid compatibility issues with Forge's change of the gamerule check to an
     * event check that doesn't exist in sponge except in the case of griefing data.
     */
    @Redirect(method = "aiStep()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;canPickUpLoot()Z"))
    private boolean impl$onCanGrief(final Mob thisEntity) {
        return thisEntity.canPickUpLoot() && ((GrieferBridge) this).bridge$canGrief();
    }

    /**
     * @author gabizou - April 8th, 2016
     * @author gabizou - April 11th, 2016 - Update for 1.9 additions
     * @author Aaron1011 - November 12, 2016 - Update for 1.11
     * @author Zidane - Minecraft 1.14.4
     *
     * @reason Rewrite this to throw an {@link AttackEntityEvent} and process correctly.
     *
     * float f        | baseDamage
     * int i          | knockbackModifier
     * boolean flag   | attackSucceeded
     *
     * @param targetEntity The entity to attack
     * @return True if the attack was successful
     */
    @Overwrite
    public boolean doHurtTarget(final net.minecraft.world.entity.Entity targetEntity) {
        // Sponge Start - Prepare our event values
        // float baseDamage = this.getEntityAttribute(Attributes.attackDamage).getAttributeValue();
        final double originalBaseDamage = this.shadow$getAttribute(Attributes.ATTACK_DAMAGE).getValue();
        final List<DamageFunction> originalFunctions = new ArrayList<>();
        // Sponge End
        float knockbackModifier = (float) this.shadow$getAttribute(Attributes.ATTACK_KNOCKBACK).getValue();

        if (targetEntity instanceof LivingEntity) {
            // Sponge Start - Gather modifiers
            originalFunctions.addAll(DamageEventHandler
                .createAttackEnchantmentFunction(this.shadow$getMainHandItem(), ((LivingEntity) targetEntity).getMobType(), 1.0F)); // 1.0F is for full attack strength since mobs don't have the concept
            // baseDamage += EnchantmentHelper.getModifierForCreature(this.getHeldItem(), ((EntityLivingBase) targetEntity).getCreatureAttribute());
            knockbackModifier += EnchantmentHelper.getKnockbackBonus((Mob) (Object) this);
        }

        // Sponge Start - Throw our event and handle appropriately
        final DamageSource damageSource = DamageSource.mobAttack((Mob) (Object) this);
        PhaseTracker.getCauseStackManager().pushCause(damageSource);
        final AttackEntityEvent event = SpongeEventFactory.createAttackEntityEvent(PhaseTracker.getCauseStackManager().currentCause(), (org.spongepowered.api.entity.Entity) targetEntity,
            originalFunctions, knockbackModifier, originalBaseDamage);
        SpongeCommon.post(event);
        PhaseTracker.getCauseStackManager().popCause();
        if (event.isCancelled()) {
            return false;
        }
        knockbackModifier = event.knockbackModifier();
        // boolean attackSucceeded = targetEntity.attackEntityFrom(DamageSource.causeMobDamage(this), baseDamage);
        final boolean attackSucceeded = targetEntity.hurt(damageSource, (float) event.finalOutputDamage());
        // Sponge End
        if (attackSucceeded) {
            if (knockbackModifier > 0 && targetEntity instanceof LivingEntity) {
                ((LivingEntity)targetEntity).knockback(knockbackModifier * 0.5F,
                        Mth.sin(this.yRot * ((float)Math.PI / 180F)), -Mth.cos(this.yRot * ((float)Math.PI / 180F)));
                this.shadow$setDeltaMovement(this.shadow$getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
            }

            final int j = EnchantmentHelper.getFireAspect((Mob) (Object) this);

            if (j > 0) {
                targetEntity.setSecondsOnFire(j * 4);
            }

            if (targetEntity instanceof Player) {
                final Player playerentity = (Player) targetEntity;
                final ItemStack mainHandItem = this.shadow$getMainHandItem();
                final ItemStack useItem = playerentity.isUsingItem() ? playerentity.getUseItem() : ItemStack.EMPTY;
                this.shadow$maybeDisableShield(playerentity, mainHandItem, useItem);
            }

            this.shadow$doEnchantDamageEffects((Mob) (Object) this, targetEntity);
            this.shadow$setLastHurtMob(targetEntity);
        }

        return attackSucceeded;
    }

    @Nullable
    @Redirect(
            method = "checkDespawn()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getNearestPlayer(Lnet/minecraft/world/entity/Entity;D)Lnet/minecraft/world/entity/player/Player;"))
    private Player impl$getClosestPlayerForSpawning(final Level world, final net.minecraft.world.entity.Entity entityIn, final double distance) {
        double bestDistance = -1.0D;
        Player result = null;

        for (final Player player : world.players()) {
            if (player == null || player.removed || !((PlayerBridge) player).bridge$affectsSpawning()) {
                continue;
            }

            final double playerDistance = player.distanceToSqr(entityIn);

            if ((distance < 0.0D || playerDistance < distance * distance) && (bestDistance == -1.0D || playerDistance < bestDistance)) {
                bestDistance = playerDistance;
                result = player;
            }
        }

        return result;
    }

    @Inject(method = "setLeashedTo", at = @At("HEAD"), cancellable = true)
    private void impl$onSetLeashedTo(final net.minecraft.world.entity.Entity param0, final boolean param1, final CallbackInfo ci) {
        if (!this.level.isClientSide) {
            final Cause currentCause = Sponge.server().causeStackManager().currentCause();
            if (Sponge.eventManager().post(SpongeEventFactory.createLeashEntityEvent(currentCause, (Entity) this))) {
                ci.cancel();
            }
        }
    }

}
