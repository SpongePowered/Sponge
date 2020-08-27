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

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IEntityReader;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.ai.goal.GoalExecutorTypes;
import org.spongepowered.api.entity.living.Agent;
import org.spongepowered.api.entity.living.Living;
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
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.entity.GrieferBridge;
import org.spongepowered.common.bridge.entity.ai.GoalSelectorBridge;
import org.spongepowered.common.bridge.entity.player.PlayerEntityBridge;
import org.spongepowered.common.bridge.world.storage.WorldInfoBridge;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.cause.entity.damage.DamageEventHandler;
import org.spongepowered.common.event.tracking.PhaseTracker;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntityMixin {

    @Shadow @Final protected GoalSelector goalSelector;
    @Shadow @Final protected GoalSelector targetSelector;
    @Shadow @Nullable private LivingEntity attackTarget;
    @Shadow @Nullable public abstract net.minecraft.entity.Entity shadow$getLeashHolder();
    @Shadow protected abstract void shadow$registerGoals();

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/MobEntity;registerGoals()V"))
    private void impl$registerGoals(final MobEntity this$0) {
        this.initSpongeAI();
        this.shadow$registerGoals();
    }

    private void initSpongeAI() {
        if (!((GoalSelectorBridge) this.goalSelector).bridge$initialized()) {
            ((GoalSelectorBridge) this.goalSelector).bridge$setOwner((MobEntity) (Object) this);
            ((GoalSelectorBridge) this.goalSelector).bridge$setType(GoalExecutorTypes.NORMAL.get());
            ((GoalSelectorBridge) this.goalSelector).bridge$setInitialized(true);
        }
        if (!((GoalSelectorBridge) this.targetSelector).bridge$initialized()) {
            ((GoalSelectorBridge) this.targetSelector).bridge$setOwner((MobEntity) (Object) this);
            ((GoalSelectorBridge) this.targetSelector).bridge$setType(GoalExecutorTypes.TARGET.get());
            ((GoalSelectorBridge) this.targetSelector).bridge$setInitialized(true);
        }
    }

    @Inject(method = "clearLeashed",
        at = @At(value = "FIELD",
            target = "Lnet/minecraft/entity/MobEntity;leashHolder:Lnet/minecraft/entity/Entity;",
            opcode = Opcodes.PUTFIELD
        ),
        cancellable = true)
    private void impl$ThrowUnleashEvent(final boolean sendPacket, final boolean dropLead, final CallbackInfo ci) {
        final net.minecraft.entity.Entity entity = this.shadow$getLeashHolder();
        if (!this.world.isRemote) {
            final CauseStackManager csm = PhaseTracker.getCauseStackManager();
            if (entity == null) {
                csm.pushCause(this);
            } else {
                csm.pushCause(entity);
            }
            final UnleashEntityEvent event = SpongeEventFactory.createUnleashEntityEvent(csm.getCurrentCause(), (Living) this);
            SpongeCommon.postEvent(event);
            csm.popCause();
            if (event.isCancelled()) {
                ci.cancel();
            }
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
    @Inject(method = "setAttackTarget", at = @At("HEAD"), cancellable = true)
    private void onSetAttackTarget(@Nullable final LivingEntity entitylivingbaseIn, final CallbackInfo ci) {
        if (this.world.isRemote || entitylivingbaseIn == null) {
            return;
        }
        //noinspection ConstantConditions
        if (EntityUtil.isUntargetable((net.minecraft.entity.Entity) (Object) this, entitylivingbaseIn)) {
            this.attackTarget = null;
            ci.cancel();
            return;
        }
        if (ShouldFire.SET_A_I_TARGET_EVENT) {
            final SetAITargetEvent event = SpongeCommonEventFactory.callSetAttackTargetEvent((Entity) entitylivingbaseIn, (Agent) this);
            if (event.isCancelled()) {
                ci.cancel();
            } else {
                this.attackTarget = ((LivingEntity) event.getTarget().orElse(null));
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
    public LivingEntity getAttackTarget() {
        if (this.attackTarget != null) {
            //noinspection ConstantConditions
            if (EntityUtil.isUntargetable((net.minecraft.entity.Entity) (Object) this, this.attackTarget)) {
                this.attackTarget = null;
            }
        }
        return this.attackTarget;
    }

    /**
     * @author gabizou - April 11th, 2018
     * @reason Instead of redirecting the gamerule request, redirecting the dead check
     * to avoid compatibility issues with Forge's change of the gamerule check to an
     * event check that doesn't exist in sponge except in the case of griefing data.
     */
    @Redirect(method = "livingTick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/MobEntity;canPickUpLoot()Z"))
    private boolean impl$onCanGrief(final MobEntity thisEntity) {
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
    public boolean attackEntityAsMob(net.minecraft.entity.Entity targetEntity) {
        // Sponge Start - Prepare our event values
        // float baseDamage = this.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
        final double originalBaseDamage = this.shadow$getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getValue();
        final List<DamageFunction> originalFunctions = new ArrayList<>();
        // Sponge End
        float knockbackModifier = (float) this.shadow$getAttribute(SharedMonsterAttributes.ATTACK_KNOCKBACK).getValue();

        if (targetEntity instanceof LivingEntity) {
            // Sponge Start - Gather modifiers
            originalFunctions.addAll(DamageEventHandler
                .createAttackEnchantmentFunction(this.shadow$getHeldItemMainhand(), ((LivingEntity) targetEntity).getCreatureAttribute(), 1.0F)); // 1.0F is for full attack strength since mobs don't have the concept
            // baseDamage += EnchantmentHelper.getModifierForCreature(this.getHeldItem(), ((EntityLivingBase) targetEntity).getCreatureAttribute());
            knockbackModifier += EnchantmentHelper.getKnockbackModifier((MobEntity) (Object) this);
        }

        // Sponge Start - Throw our event and handle appropriately
        final DamageSource damageSource = DamageSource.causeMobDamage((MobEntity) (Object) this);
        PhaseTracker.getCauseStackManager().pushCause(damageSource);
        final AttackEntityEvent event = SpongeEventFactory.createAttackEntityEvent(PhaseTracker.getCauseStackManager().getCurrentCause(), (org.spongepowered.api.entity.Entity) targetEntity,
            originalFunctions, knockbackModifier, originalBaseDamage);
        SpongeCommon.postEvent(event);
        PhaseTracker.getCauseStackManager().popCause();
        if (event.isCancelled()) {
            return false;
        }
        knockbackModifier = event.getKnockbackModifier();
        // boolean attackSucceeded = targetEntity.attackEntityFrom(DamageSource.causeMobDamage(this), baseDamage);
        boolean attackSucceeded = targetEntity.attackEntityFrom(damageSource, (float) event.getFinalOutputDamage());
        // Sponge End
        if (attackSucceeded) {
            if (knockbackModifier > 0 && targetEntity instanceof LivingEntity) {
                ((LivingEntity)targetEntity).knockBack((MobEntity) (Object) this, (float) knockbackModifier * 0.5F,
                    MathHelper.sin(this.rotationYaw * ((float)Math.PI / 180F)), -MathHelper.cos(this.rotationYaw * ((float)Math.PI / 180F)));
                this.shadow$setMotion(this.shadow$getMotion().mul(0.6D, 1.0D, 0.6D));
            }

            int j = EnchantmentHelper.getFireAspectModifier((MobEntity) (Object) this);

            if (j > 0) {
                targetEntity.setFire(j * 4);
            }

            if (targetEntity instanceof PlayerEntity) {
                PlayerEntity playerentity = (PlayerEntity) targetEntity;
                ItemStack itemstack = this.shadow$getHeldItemMainhand();
                ItemStack itemstack1 = playerentity.isHandActive() ? playerentity.getActiveItemStack() : ItemStack.EMPTY;
                if (!itemstack.isEmpty() && !itemstack1.isEmpty() && itemstack.getItem() instanceof AxeItem && itemstack1.getItem() == Items.SHIELD) {
                    float f2 = 0.25F + (float)EnchantmentHelper.getEfficiencyModifier((MobEntity) (Object) this) * 0.05F;
                    if (this.rand.nextFloat() < f2) {
                        playerentity.getCooldownTracker().setCooldown(Items.SHIELD, 100);
                        this.world.setEntityState(playerentity, (byte)30);
                    }
                }
            }

            this.shadow$applyEnchantments((MobEntity) (Object) this, targetEntity);
        }

        return attackSucceeded;
    }

    @ModifyConstant(method = "checkDespawn", constant = @Constant(doubleValue = 16384.0D))
    private double getHardDespawnRange(final double value) {
        if (!this.world.isRemote) {
            return Math.pow(((WorldInfoBridge) this.world.getWorldInfo()).bridge$getConfigAdapter().get().getEntity().getHardDespawnRange(), 2);
        }
        return value;
    }

    // Note that this should inject twice.
    @ModifyConstant(method = "checkDespawn", constant = @Constant(doubleValue = 1024.0D), expect = 2)
    private double getSoftDespawnRange(final double value) {
        if (!this.world.isRemote) {
            return Math.pow(((WorldInfoBridge) this.world.getWorldInfo()).bridge$getConfigAdapter().get().getEntity().getSoftDespawnRange(), 2);
        }
        return value;
    }

    @ModifyConstant(method = "checkDespawn", constant = @Constant(intValue = 600))
    private int getMinimumLifetime(final int value) {
        if (!this.world.isRemote) {
            return ((WorldInfoBridge) this.world.getWorldInfo()).bridge$getConfigAdapter().get().getEntity().getMinimumLife() * 20;
        }
        return value;
    }

    @Nullable
    @Redirect(
            method = "checkDespawn()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;getClosestPlayer(Lnet/minecraft/entity/Entity;D)Lnet/minecraft/entity/player/PlayerEntity;"))
    private PlayerEntity impl$getClosestPlayerForSpawning(World world, net.minecraft.entity.Entity entityIn, double distance) {
        double bestDistance = -1.0D;
        PlayerEntity result = null;

        for (final PlayerEntity player : world.getPlayers()) {
            if (player == null || player.removed || !((PlayerEntityBridge) player).bridge$affectsSpawning()) {
                continue;
            }

            final double playerDistance = player.getDistanceSq(entityIn.getPosX(), entityIn.getPosY(), entityIn.getPosZ());

            if ((distance < 0.0D || playerDistance < distance * distance) && (bestDistance == -1.0D || playerDistance < bestDistance)) {
                bestDistance = playerDistance;
                result = player;
            }
        }

        return result;
    }

}
