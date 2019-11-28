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

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.ai.Goal;
import org.spongepowered.api.entity.ai.GoalTypes;
import org.spongepowered.api.entity.ai.task.AITask;
import org.spongepowered.api.entity.living.Agent;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.LeashEntityEvent;
import org.spongepowered.api.event.entity.UnleashEntityEvent;
import org.spongepowered.api.event.entity.ai.AITaskEvent;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.entity.GrieferBridge;
import org.spongepowered.common.bridge.entity.ai.EntityAIBasesBridge;
import org.spongepowered.common.bridge.entity.ai.EntityAITasksBridge;
import org.spongepowered.common.bridge.entity.player.EntityPlayerBridge;
import org.spongepowered.common.bridge.world.WorldInfoBridge;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;

import java.util.Iterator;

import javax.annotation.Nullable;

@Mixin(MobEntity.class)
public abstract class EntityLivingMixin extends EntityLivingBaseMixin {

    @Shadow @Final protected GoalSelector tasks;
    @Shadow @Final protected GoalSelector targetTasks;
    @Shadow @Nullable private LivingEntity attackTarget;

    @Shadow public abstract boolean isAIDisabled();
    @Shadow @Nullable public abstract net.minecraft.entity.Entity getLeashHolder();
    @Shadow protected abstract void initEntityAI();

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLiving;initEntityAI()V"))
    private void spongeImpl$initializeAI(final MobEntity this$0) {
        this.initSpongeAI();
        this.initEntityAI();
    }

    private void initSpongeAI() {
        if (!((EntityAITasksBridge) this.tasks).bridge$initialized()) {
            ((EntityAITasksBridge) this.tasks).bridge$setOwner((MobEntity) (Object) this);
            ((EntityAITasksBridge) this.tasks).bridge$setType(GoalTypes.NORMAL);
            ((EntityAITasksBridge) this.tasks).bridge$setInitialized(true);
        }
        if (!((EntityAITasksBridge) this.targetTasks).bridge$initialized()) {
            ((EntityAITasksBridge) this.targetTasks).bridge$setOwner((MobEntity) (Object) this);
            ((EntityAITasksBridge) this.targetTasks).bridge$setType(GoalTypes.TARGET);
            ((EntityAITasksBridge) this.targetTasks).bridge$setInitialized(true);
        }
    }

    @Override
    public void bridge$fireConstructors() {
        super.bridge$fireConstructors();
        if (ShouldFire.A_I_TASK_EVENT_ADD) {
            handleDelayedTaskEventFiring((EntityAITasksBridge) this.tasks);
            handleDelayedTaskEventFiring((EntityAITasksBridge) this.targetTasks);
        }
    }

    @SuppressWarnings("unchecked")
    private void handleDelayedTaskEventFiring(final EntityAITasksBridge tasks) {
        final Iterator<GoalSelector.EntityAITaskEntry> taskItr = tasks.bridge$getTasksUnsafe().iterator();
        while (taskItr.hasNext()) {
            final GoalSelector.EntityAITaskEntry task = taskItr.next();
            final AITaskEvent.Add event = SpongeEventFactory.createAITaskEventAdd(Sponge.getCauseStackManager().getCurrentCause(),
                    task.field_75731_b, task.field_75731_b, (Goal<? extends Agent>) tasks, (Agent) this, (AITask<?>) task.field_75733_a);
            SpongeImpl.postEvent(event);
            if (event.isCancelled()) {
                ((EntityAIBasesBridge) task.field_75733_a).bridge$setGoal(null);
                taskItr.remove();
            }
        }
    }

    @Inject(method = "processInitialInteract", cancellable = true,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLiving;setLeashHolder(Lnet/minecraft/entity/Entity;Z)V"))
    private void callLeashEvent(final PlayerEntity playerIn, final Hand hand, final CallbackInfoReturnable<Boolean> ci) {
        if (!playerIn.world.isRemote) {
            Sponge.getCauseStackManager().pushCause(playerIn);
            final LeashEntityEvent event = SpongeEventFactory.createLeashEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), (Living) this);
            SpongeImpl.postEvent(event);
            Sponge.getCauseStackManager().popCause();
            if(event.isCancelled()) {
                ci.setReturnValue(false);
            }
        }
    }

    @Inject(method = "clearLeashed",
        at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLiving;isLeashed:Z", opcode = Opcodes.PUTFIELD),
        cancellable = true)
    private void impl$ThrowUnleashEvent(final boolean sendPacket, final boolean dropLead, final CallbackInfo ci) {
        final net.minecraft.entity.Entity entity = getLeashHolder();
        if (!this.world.isRemote) {
            final CauseStackManager csm = Sponge.getCauseStackManager();
            if(entity == null) {
                csm.pushCause(this);
            } else {
                csm.pushCause(entity);
            }
            final UnleashEntityEvent event = SpongeEventFactory.createUnleashEntityEvent(csm.getCurrentCause(), (Living) this);
            SpongeImpl.postEvent(event);
            csm.popCause();
            if(event.isCancelled()) {
                ci.cancel();
            }
        }
    }

    @ModifyConstant(method = "despawnEntity", constant = @Constant(doubleValue = 16384.0D))
    private double getHardDespawnRange(final double value) {
        if (!this.world.isRemote) {
            return Math.pow(((WorldInfoBridge) this.world.getWorldInfo()).bridge$getConfigAdapter().getConfig().getEntity().getHardDespawnRange(), 2);
        }
        return value;
    }

    // Note that this should inject twice.
    @ModifyConstant(method = "despawnEntity", constant = @Constant(doubleValue = 1024.0D), expect = 2)
    private double getSoftDespawnRange(final double value) {
        if (!this.world.isRemote) {
            return Math.pow(((WorldInfoBridge) this.world.getWorldInfo()).bridge$getConfigAdapter().getConfig().getEntity().getSoftDespawnRange(), 2);
        }
        return value;
    }

    @ModifyConstant(method = "despawnEntity", constant = @Constant(intValue = 600))
    private int getMinimumLifetime(final int value) {
        if (!this.world.isRemote) {
            return ((WorldInfoBridge) this.world.getWorldInfo()).bridge$getConfigAdapter().getConfig().getEntity().getMinimumLife() * 20;
        }
        return value;
    }

    @Nullable
    @Redirect(
        method = "despawnEntity",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;getClosestPlayerToEntity(Lnet/minecraft/entity/Entity;D)Lnet/minecraft/entity/player/EntityPlayer;"))
    private PlayerEntity impl$getClosestPlayerForSpawning(final World world, final net.minecraft.entity.Entity entity, final double distance) {
        double bestDistance = -1.0D;
        PlayerEntity result = null;

        for (final Object entity1 : world.field_73010_i) {
            final PlayerEntity player = (PlayerEntity) entity1;
            if (player == null || player.removed || !((EntityPlayerBridge) player).bridge$affectsSpawning()) {
                continue;
            }

            final double playerDistance = player.getDistanceSq(entity.posX, entity.posY, entity.posZ);

            if ((distance < 0.0D || playerDistance < distance * distance) && (bestDistance == -1.0D || playerDistance < bestDistance)) {
                bestDistance = playerDistance;
                result = player;
            }
        }

        return result;
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
     *
     * @param thisEntity
     * @return
     */
    @Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLiving;canPickUpLoot()Z"))
    private boolean onCanGrief(final MobEntity thisEntity) {
        return thisEntity.canPickUpLoot() && ((GrieferBridge) this).bridge$CanGrief();
    }


    @Override
    public void bridge$onJoinWorld() {
        this.initSpongeAI();
    }

}
