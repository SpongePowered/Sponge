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
package org.spongepowered.common.mixin.invalid.core.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
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
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.entity.GrieferBridge;
import org.spongepowered.common.bridge.entity.ai.GoalSelectorBridge;
import org.spongepowered.common.bridge.entity.player.PlayerEntityBridge;
import org.spongepowered.common.bridge.world.storage.WorldInfoBridge;
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

        for (final Object entity1 : world.playerEntities) {
            final PlayerEntity player = (PlayerEntity) entity1;
            if (player == null || player.removed || !((PlayerEntityBridge) player).bridge$affectsSpawning()) {
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

}
