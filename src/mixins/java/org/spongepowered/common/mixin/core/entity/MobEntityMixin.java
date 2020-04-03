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
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.ai.goal.GoalExecutorTypes;
import org.spongepowered.api.entity.living.Agent;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
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
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.entity.GrieferBridge;
import org.spongepowered.common.bridge.entity.ai.GoalSelectorBridge;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;

import javax.annotation.Nullable;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntityMixin {

    @Shadow @Final protected GoalSelector goalSelector;
    @Shadow @Final protected GoalSelector targetSelector;
    @Shadow @Nullable private LivingEntity attackTarget;

    @Shadow public abstract boolean isAIDisabled();
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
    @Redirect(method = "livingTick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/MobEntity;canPickUpLoot()Z"))
    private boolean impl$onCanGrief(final MobEntity thisEntity) {
        return thisEntity.canPickUpLoot() && ((GrieferBridge) this).bridge$canGrief();
    }


    @Override
    public void bridge$onJoinWorld() {
        this.initSpongeAI();
    }

}
