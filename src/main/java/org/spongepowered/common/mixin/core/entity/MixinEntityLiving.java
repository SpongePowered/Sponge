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

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.entity.AgentData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.ai.Goal;
import org.spongepowered.api.entity.ai.GoalType;
import org.spongepowered.api.entity.ai.GoalTypes;
import org.spongepowered.api.entity.ai.task.AITask;
import org.spongepowered.api.entity.living.Agent;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.LeashEntityEvent;
import org.spongepowered.api.event.entity.UnleashEntityEvent;
import org.spongepowered.api.event.entity.ai.AITaskEvent;
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
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeAgentData;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.interfaces.ai.IMixinEntityAIBase;
import org.spongepowered.common.interfaces.ai.IMixinEntityAITasks;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.entity.IMixinGriefer;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(EntityLiving.class)
public abstract class MixinEntityLiving extends MixinEntityLivingBase implements Agent {

    private static final String GET_CLOSEST_PLAYER =
            "Lnet/minecraft/world/World;getClosestPlayerToEntity(Lnet/minecraft/entity/Entity;D)Lnet/minecraft/entity/player/EntityPlayer;";
    @Shadow @Final private EntityAITasks tasks;
    @Shadow @Final private EntityAITasks targetTasks;
    @Shadow @Nullable private EntityLivingBase attackTarget;

    @Shadow public abstract boolean isAIDisabled();
    @Shadow @Nullable public abstract net.minecraft.entity.Entity getLeashedToEntity();
    @Shadow protected abstract void initEntityAI();

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLiving;initEntityAI()V"))
    public void onInitAi(EntityLiving this$0) {
        this.initSpongeAI();
        this.initEntityAI();
    }

    private void initSpongeAI() {
        if (!((IMixinEntityAITasks) this.tasks).initialized()) {
            ((IMixinEntityAITasks) this.tasks).setOwner((EntityLiving) (Object) this);
            ((IMixinEntityAITasks) this.tasks).setType(GoalTypes.NORMAL);
            ((IMixinEntityAITasks) this.tasks).setInitialized(true);
        }
        if (!((IMixinEntityAITasks) this.targetTasks).initialized()) {
            ((IMixinEntityAITasks) this.targetTasks).setOwner((EntityLiving) (Object) this);
            ((IMixinEntityAITasks) this.targetTasks).setType(GoalTypes.TARGET);
            ((IMixinEntityAITasks) this.targetTasks).setInitialized(true);
        }
    }

    @Override
    public void firePostConstructEvents() {
        super.firePostConstructEvents();
        handleDelayedTaskEventFiring((IMixinEntityAITasks) this.tasks);
        handleDelayedTaskEventFiring((IMixinEntityAITasks) this.targetTasks);
    }

    @SuppressWarnings("unchecked")
    private void handleDelayedTaskEventFiring(IMixinEntityAITasks tasks) {
        Iterator<EntityAITasks.EntityAITaskEntry> taskItr = tasks.getTasksUnsafe().iterator();
        while (taskItr.hasNext()) {
            EntityAITasks.EntityAITaskEntry task = taskItr.next();
            final AITaskEvent.Add event = SpongeEventFactory.createAITaskEventAdd(Cause.of(NamedCause.source(Sponge.getGame())),
                    task.priority, task.priority, (Goal<? extends Agent>) tasks, this, (AITask<?>) task.action);
            SpongeImpl.postEvent(event);
            if (event.isCancelled()) {
                ((IMixinEntityAIBase) task.action).setGoal(null);
                taskItr.remove();
            }
        }
    }

    @Inject(method = "processInitialInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLiving;setLeashedToEntity(Lnet/minecraft/entity/Entity;Z)V"), cancellable = true)
    public void callLeashEvent(EntityPlayer playerIn, EnumHand hand, CallbackInfoReturnable<Boolean> ci) {
        if (!playerIn.world.isRemote) {
            final LeashEntityEvent event = SpongeEventFactory.createLeashEntityEvent(Cause.of(NamedCause.source(playerIn)), this);
            SpongeImpl.postEvent(event);
            if(event.isCancelled()) {
                ci.setReturnValue(false);
            }
        }
    }

    @Inject(method = "clearLeashed", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLiving;isLeashed:Z", opcode = Opcodes.PUTFIELD), cancellable = true)
    public void callUnleashEvent(boolean sendPacket, boolean dropLead, CallbackInfo ci) {
        net.minecraft.entity.Entity entity = getLeashedToEntity();
        if (!this.world.isRemote) {
            UnleashEntityEvent event = SpongeEventFactory.createUnleashEntityEvent(entity == null ? Cause.of(NamedCause.of("Self", this))
                : Cause.of(NamedCause.source(entity)), this);
            SpongeImpl.postEvent(event);
            if(event.isCancelled()) {
                ci.cancel();
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Agent> Optional<Goal<T>> getGoal(GoalType type) {
        if (GoalTypes.NORMAL.equals(type)) {
            return Optional.of((Goal<T>) this.tasks);
        } else if (GoalTypes.TARGET.equals(type)) {
            return Optional.of((Goal<T>) this.targetTasks);
        }
        return Optional.empty();
    }

    @ModifyConstant(method = "despawnEntity", constant = @Constant(doubleValue = 16384.0D))
    private double getHardDespawnRange(double value) {
        if (!this.world.isRemote) {
            return Math.pow(((IMixinWorldServer) this.world).getWorldConfig().getConfig().getEntity().getHardDespawnRange(), 2);
        }
        return value;
    }

    // Note that this should inject twice.
    @ModifyConstant(method = "despawnEntity", constant = @Constant(doubleValue = 1024.0D), expect = 2)
    private double getSoftDespawnRange(double value) {
        if (!this.world.isRemote) {
            return Math.pow(((IMixinWorldServer) this.world).getWorldConfig().getConfig().getEntity().getSoftDespawnRange(), 2);
        }
        return value;
    }

    @ModifyConstant(method = "despawnEntity", constant = @Constant(intValue = 600))
    private int getMinimumLifetime(int value) {
        if (!this.world.isRemote) {
            return ((IMixinWorldServer) this.world).getWorldConfig().getConfig().getEntity().getMinimumLife() * 20;
        }
        return value;
    }

    @Nullable
    @Redirect(method = "despawnEntity", at = @At(value = "INVOKE", target = GET_CLOSEST_PLAYER))
    public EntityPlayer onDespawnEntity(World world, net.minecraft.entity.Entity entity, double distance) {
        return ((IMixinWorld) world).getClosestPlayerToEntityWhoAffectsSpawning(entity, distance);
    }

    @Override
    public Optional<Entity> getTarget() {
        return Optional.ofNullable((Entity) this.attackTarget);
    }

    @Override
    public void setTarget(@Nullable Entity target) {
        if (target instanceof EntityLivingBase) {
            this.attackTarget = (EntityLivingBase) target;
        } else {
            this.attackTarget = null;
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
    public void onSetAttackTarget(@Nullable EntityLivingBase entitylivingbaseIn, CallbackInfo ci) {
        if (entitylivingbaseIn != null && ((IMixinEntity) entitylivingbaseIn).isVanished()
            && ((IMixinEntity) entitylivingbaseIn).isUntargetable()) {
            this.attackTarget = null;
            ci.cancel();
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
    public EntityLivingBase getAttackTarget() {
        if (this.attackTarget != null) {
            if (((IMixinEntity) this.attackTarget).isVanished() && ((IMixinEntity) this.attackTarget).isUntargetable()) {
                this.attackTarget = null;
            }
        }
        return this.attackTarget;
    }

    @Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Ljava/lang/String;)Z"))
    private boolean onCanGrief(GameRules gameRules, String rule) {
        return gameRules.getBoolean(rule) && ((IMixinGriefer) this).canGrief();
    }

    // Data delegated methods


    @Override
    public AgentData getAgentData() {
        return new SpongeAgentData(!this.isAIDisabled());
    }

    @Override
    public Value<Boolean> aiEnabled() {
        return new SpongeValue<>(Keys.AI_ENABLED, true, !this.isAIDisabled());
    }

    @Override
    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
        super.supplyVanillaManipulators(manipulators);
        manipulators.add(getAgentData());
    }

    @Override
    public void onJoinWorld() {
        this.initSpongeAI();
    }

}
