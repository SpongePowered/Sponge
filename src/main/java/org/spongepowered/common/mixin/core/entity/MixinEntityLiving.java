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
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.AgentData;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.ai.Goal;
import org.spongepowered.api.entity.ai.GoalType;
import org.spongepowered.api.entity.ai.GoalTypes;
import org.spongepowered.api.entity.ai.task.AITask;
import org.spongepowered.api.entity.living.Agent;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.entity.ModifierFunction;
import org.spongepowered.api.event.cause.entity.damage.DamageModifier;
import org.spongepowered.api.event.entity.AttackEntityEvent;
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
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeAgentData;
import org.spongepowered.common.data.value.SpongeMutableValue;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.damage.DamageEventHandler;
import org.spongepowered.common.interfaces.ai.IMixinEntityAIBase;
import org.spongepowered.common.interfaces.ai.IMixinEntityAITasks;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.entity.IMixinGriefer;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

import java.util.ArrayList;
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
    @Shadow @Nullable public abstract net.minecraft.entity.Entity getLeashHolder();
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
        if (ShouldFire.A_I_TASK_EVENT_ADD) {
            handleDelayedTaskEventFiring((IMixinEntityAITasks) this.tasks);
            handleDelayedTaskEventFiring((IMixinEntityAITasks) this.targetTasks);
        }
    }

    @SuppressWarnings("unchecked")
    private void handleDelayedTaskEventFiring(IMixinEntityAITasks tasks) {
        Iterator<EntityAITasks.EntityAITaskEntry> taskItr = tasks.getTasksUnsafe().iterator();
        while (taskItr.hasNext()) {
            EntityAITasks.EntityAITaskEntry task = taskItr.next();
            final AITaskEvent.Add event = SpongeEventFactory.createAITaskEventAdd(Sponge.getCauseStackManager().getCurrentCause(),
                    task.priority, task.priority, this, (Goal<? extends Agent>) tasks, (AITask<?>) task.action);
            SpongeImpl.postEvent(event);
            if (event.isCancelled()) {
                ((IMixinEntityAIBase) task.action).setGoal(null);
                taskItr.remove();
            }
        }
    }

    @Inject(method = "processInitialInteract", cancellable = true,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLiving;setLeashHolder(Lnet/minecraft/entity/Entity;Z)V"))
    public void callLeashEvent(EntityPlayer playerIn, EnumHand hand, CallbackInfoReturnable<Boolean> ci) {
        if (!playerIn.world.isRemote) {
            Sponge.getCauseStackManager().pushCause(playerIn);
            final LeashEntityEvent event = SpongeEventFactory.createLeashEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), this);
            SpongeImpl.postEvent(event);
            Sponge.getCauseStackManager().popCause();
            if(event.isCancelled()) {
                ci.setReturnValue(false);
            }
        }
    }

    @Inject(method = "clearLeashed", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLiving;isLeashed:Z", opcode = Opcodes.PUTFIELD), cancellable = true)
    public void callUnleashEvent(boolean sendPacket, boolean dropLead, CallbackInfo ci) {
        net.minecraft.entity.Entity entity = getLeashHolder();
        if (!this.world.isRemote) {
            if(entity == null) {
                Sponge.getCauseStackManager().pushCause(this);
            } else {
                Sponge.getCauseStackManager().pushCause(entity);
            }
            UnleashEntityEvent event = SpongeEventFactory.createUnleashEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), this);
            SpongeImpl.postEvent(event);
            Sponge.getCauseStackManager().popCause();
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

    @ModifyConstant(method = "checkDespawn", constant = @Constant(doubleValue = 16384.0D))
    private double getHardDespawnRange(double value) {
        if (!this.world.isRemote) {
            return Math.pow(((IMixinWorldServer) this.world).getActiveConfig().getConfig().getEntity().getHardDespawnRange(), 2);
        }
        return value;
    }

    // Note that this should inject twice.
    @ModifyConstant(method = "checkDespawn", constant = @Constant(doubleValue = 1024.0D), expect = 2)
    private double getSoftDespawnRange(double value) {
        if (!this.world.isRemote) {
            return Math.pow(((IMixinWorldServer) this.world).getActiveConfig().getConfig().getEntity().getSoftDespawnRange(), 2);
        }
        return value;
    }

    @ModifyConstant(method = "checkDespawn", constant = @Constant(intValue = 600))
    private int getMinimumLifetime(int value) {
        if (!this.world.isRemote) {
            return ((IMixinWorldServer) this.world).getActiveConfig().getConfig().getEntity().getMinimumLife() * 20;
        }
        return value;
    }

    @Nullable
    @Redirect(method = "checkDespawn", at = @At(value = "INVOKE", target = GET_CLOSEST_PLAYER))
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
    private void onSetAttackTarget(@Nullable EntityLivingBase entitylivingbaseIn, CallbackInfo ci) {
        if (!this.world.isRemote && ShouldFire.SET_A_I_TARGET_EVENT) {
            if (entitylivingbaseIn != null) {
                if (((IMixinEntity) entitylivingbaseIn).isVanished() && ((IMixinEntity) entitylivingbaseIn).isUntargetable()) {
                    this.attackTarget = null;
                    ci.cancel();
                } else {
                    SetAITargetEvent event = SpongeCommonEventFactory.callSetAttackTargetEvent((Entity) entitylivingbaseIn, this);
                    if (event.isCancelled()) {
                        ci.cancel();
                    } else {
                        this.attackTarget = ((EntityLivingBase) event.getTarget().orElse(null));
                    }
                }
            }
        }
    }


    /**
     * @author gabizou - April 8th, 2016
     * @author gabizou - April 11th, 2016 - Update for 1.9 additions
     * @author Aaron1011 - November 12, 2016 - Update for 1.11
     * @author Aaron1011 - February 7th, 2019 - Update for 1.13, moved from EntityMob
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
        final double originalBaseDamage = this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getValue();
        final List<ModifierFunction<DamageModifier>> originalFunctions = new ArrayList<>();
        // Sponge End
        int knockbackModifier = 0;

        if (targetEntity instanceof EntityLivingBase) {
            // Sponge Start - Gather modifiers
            originalFunctions.addAll(DamageEventHandler
                    .createAttackEnchantmentFunction(this.getHeldItemMainhand(), ((EntityLivingBase) targetEntity).getCreatureAttribute(), 1.0F)); // 1.0F is for full attack strength since mobs don't have the concept
            // baseDamage += EnchantmentHelper.getModifierForCreature(this.getHeldItem(), ((EntityLivingBase) targetEntity).getCreatureAttribute());
            knockbackModifier += EnchantmentHelper.getKnockbackModifier((EntityMob) (Object) this);
        }

        // Sponge Start - Throw our event and handle appropriately
        final DamageSource damageSource = DamageSource.causeMobDamage((EntityMob) (Object) this);
        Sponge.getCauseStackManager().pushCause(damageSource);
        final AttackEntityEvent event = SpongeEventFactory.createAttackEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), EntityUtil.fromNative(targetEntity), originalFunctions,
                knockbackModifier, originalBaseDamage);
        SpongeImpl.postEvent(event);
        Sponge.getCauseStackManager().popCause();
        if (event.isCancelled()) {
            return false;
        }
        knockbackModifier = event.getKnockbackModifier();
        // boolean attackSucceeded = targetEntity.attackEntityFrom(DamageSource.causeMobDamage(this), baseDamage);
        boolean attackSucceeded = targetEntity.attackEntityFrom(damageSource, (float) event.getFinalOutputDamage());
        // Sponge End
        if (attackSucceeded) {
            if (knockbackModifier > 0 && targetEntity instanceof EntityLivingBase) {
                ((EntityLivingBase) targetEntity).knockBack((net.minecraft.entity.Entity) (Object) this, (float)knockbackModifier * 0.5F, (double)MathHelper.sin(this.rotationYaw * ((float)Math.PI / 180F)), (double)(-MathHelper.cos(this.rotationYaw * ((float)Math.PI / 180F))));
                this.motionX *= 0.6D;
                this.motionZ *= 0.6D;
            }

            int j = EnchantmentHelper.getFireAspectModifier((EntityMob) (Object) this);

            if (j > 0) {
                targetEntity.setFire(j * 4);
            }

            if (targetEntity instanceof EntityPlayer) {
                EntityPlayer entityplayer = (EntityPlayer) targetEntity;
                ItemStack itemstack = this.getHeldItemMainhand();
                ItemStack itemstack1 = entityplayer.isHandActive() ? entityplayer.getActiveItemStack() : ItemStack.EMPTY;

                if (!itemstack.isEmpty() && !itemstack1.isEmpty() && itemstack.getItem() instanceof ItemAxe && itemstack1.getItem() == Items.SHIELD) {
                    float f1 = 0.25F + (float) EnchantmentHelper.getEfficiencyModifier((EntityMob) (Object) this) * 0.05F;

                    if (this.rand.nextFloat() < f1) {
                        entityplayer.getCooldownTracker().setCooldown(Items.SHIELD, 100);
                        this.world.setEntityState(entityplayer, (byte) 30);
                    }
                }
            }

            this.applyEnchantments((EntityMob) (Object) this, targetEntity);
        }

        return attackSucceeded;
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

    /**
     * @author gabizou - April 11th, 2018
     * @reason Instead of redirecting the gamerule request, redirecting the dead check
     * to avoid compatibility issues with Forge's change of the gamerule check to an
     * event check that doesn't exist in sponge except in the case of griefing data.
     *
     * @param thisEntity
     * @return
     */
    @Redirect(method = "livingTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLiving;canPickUpLoot()Z"))
    private boolean onCanGrief(EntityLiving thisEntity) {
        return thisEntity.canPickUpLoot() && ((IMixinGriefer) this).canGrief();
    }

    // Data delegated methods


    @Override
    public AgentData getAgentData() {
        return new SpongeAgentData(!this.isAIDisabled());
    }

    @Override
    public Value.Mutable<Boolean> aiEnabled() {
        return new SpongeMutableValue<>(Keys.AI_ENABLED, !this.isAIDisabled());
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
