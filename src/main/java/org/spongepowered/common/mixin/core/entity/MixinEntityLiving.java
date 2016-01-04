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
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.ai.Goal;
import org.spongepowered.api.entity.ai.GoalType;
import org.spongepowered.api.entity.ai.GoalTypes;
import org.spongepowered.api.entity.living.Agent;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.LeashEntityEvent;
import org.spongepowered.api.event.entity.UnleashEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.interfaces.ai.IMixinEntityAITasks;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.world.IMixinWorld;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(EntityLiving.class)
public abstract class MixinEntityLiving extends MixinEntityLivingBase implements Agent {

    @Shadow private boolean canPickUpLoot;
    @Shadow private EntityLivingBase attackTarget;
    @Shadow public abstract boolean isAIDisabled();
    @Shadow protected abstract void setNoAI(boolean p_94061_1_);
    @Shadow public abstract net.minecraft.entity.Entity getLeashedToEntity();
    @Shadow public abstract void setLeashedToEntity(net.minecraft.entity.Entity entityIn, boolean sendAttachNotification);
    @Shadow private EntityAITasks tasks;
    @Shadow private EntityAITasks targetTasks;

    public boolean isAiEnabled() {
        return !isAIDisabled();
    }

    public void setAiEnabled(boolean aiEnabled) {
        setNoAI(!aiEnabled);
    }

    public boolean isLeashed() {
        return getLeashedToEntity() != null;
    }

    public void setLeashed(boolean leashed) {
        throw new UnsupportedOperationException(); // TODO
    }

    public Optional<Entity> getLeashHolder() {
        return Optional.ofNullable((Entity) getLeashedToEntity());
    }

    public void setLeashHolder(@Nullable Entity entity) {
        setLeashedToEntity((net.minecraft.entity.Entity) entity, true);
    }

    public boolean getCanPickupItems() {
        return this.canPickUpLoot;
    }

    public void setCanPickupItems(boolean canPickupItems) {
        this.canPickUpLoot = canPickupItems;
    }

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void onConstruct(CallbackInfo ci) {
        ((IMixinEntityAITasks) this.tasks).setOwner((EntityLiving) (Object) this);
        ((IMixinEntityAITasks) this.tasks).setType(GoalTypes.NORMAL);
        ((IMixinEntityAITasks) this.targetTasks).setOwner((EntityLiving) (Object) this);
        ((IMixinEntityAITasks) this.targetTasks).setType(GoalTypes.TARGET);
    }

    @Inject(method = "interactFirst", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLiving;setLeashedToEntity(Lnet/minecraft/entity/Entity;Z)V"), locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true)
    public void callLeashEvent(EntityPlayer playerIn, CallbackInfoReturnable<Boolean> ci, ItemStack itemstack) {
        if (!playerIn.worldObj.isRemote) {
            Entity leashedEntity = (Entity)(Object) this;
            final LeashEntityEvent event = SpongeEventFactory.createLeashEntityEvent(Cause.of(NamedCause.source(playerIn)), leashedEntity);
            SpongeImpl.postEvent(event);
            if(event.isCancelled()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "clearLeashed", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLiving;isLeashed:Z", opcode = Opcodes.PUTFIELD), cancellable = true)
    public void callUnleashEvent(boolean sendPacket, boolean dropLead, CallbackInfo ci) {
        net.minecraft.entity.Entity entity = getLeashedToEntity();
        if (!this.worldObj.isRemote) {
            Entity leashedEntity = (Entity)(Object) this;
            UnleashEntityEvent event = SpongeEventFactory.createUnleashEntityEvent(entity == null ? Cause.of(NamedCause.of("Self", leashedEntity))
                : Cause.of(NamedCause.source(entity)), leashedEntity);
            SpongeImpl.postEvent(event);
            if(event.isCancelled()) {
                ci.cancel();
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<Goal<? extends Agent>> getGoal(GoalType type) {
        if (GoalTypes.NORMAL.equals(type)) {
            return Optional.ofNullable((Goal) this.tasks);
        } else if (GoalTypes.TARGET.equals(type)) {
            return Optional.ofNullable((Goal) this.targetTasks);
        }
        return Optional.empty();
    }

    @Redirect(method = "despawnEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getClosestPlayerToEntity(Lnet/minecraft/entity/Entity;D)Lnet/minecraft/entity/player/EntityPlayer;"))
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
     * This is to instill the check that if the entity is invisible, check whether they're untargetable
     * as well.
     *
     * @param entitylivingbaseIn The entity living base coming in
     */
    @Overwrite
    public void setAttackTarget(EntityLivingBase entitylivingbaseIn) {
        if (entitylivingbaseIn != null && ((IMixinEntity) entitylivingbaseIn).isReallyREALLYInvisible()
            && ((IMixinEntity) entitylivingbaseIn).isUntargetable()) {
            this.attackTarget = null;
            return;
        }
        this.attackTarget = entitylivingbaseIn;
    }

    /**
     * @author gabizou - January 4th, 2016
     *
     * This will still check if the current attack target is invisible and is untargetable.
     *
     * @return The current attack target, if not null
     */
    @Overwrite
    public EntityLivingBase getAttackTarget() {
        if (this.attackTarget != null) {
            if (((IMixinEntity) this.attackTarget).isReallyREALLYInvisible() && ((IMixinEntity) this.attackTarget).isUntargetable()) {
                this.attackTarget = null;
            }
        }
        return this.attackTarget;
    }
}
