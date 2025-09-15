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

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.api.event.cause.entity.damage.DamageStepTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.common.bridge.world.entity.TrackedDamageBridge;
import org.spongepowered.common.event.cause.entity.damage.SpongeDamageStep;
import org.spongepowered.common.event.cause.entity.damage.SpongeDamageTracker;

// Forge and Vanilla
@Mixin(value = LivingEntity.class, priority = 900)
public abstract class LivingEntityMixin_Shared_Damage implements TrackedDamageBridge {

    @ModifyVariable(method = "hurtServer", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;noActionTime:I"), argsOnly = true)
    private float damage$modifyAfterStart(final float damage) {
        final SpongeDamageTracker tracker = this.damage$tracker();
        if (tracker == null) {
            return damage;
        }
        final SpongeDamageStep step = tracker.currentStep(DamageStepTypes.START);
        return step == null ? damage : (float) step.damageAfterChildren().getAsDouble();
    }

    @ModifyVariable(method = "actuallyHurt", at = @At("STORE"), argsOnly = true, slice = @Slice(
        from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getDamageAfterMagicAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F"),
        to = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getAbsorptionAmount()F", ordinal = 0)))
    private float damage$modifyBeforeAbsorption(final float damage) {
        final SpongeDamageTracker tracker = this.damage$tracker();
        return tracker == null ? damage : tracker.startStep(DamageStepTypes.ABSORPTION, damage, this);
    }

    @WrapWithCondition(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setAbsorptionAmount(F)V", ordinal = 0))
    private boolean damage$skipAbsorption(final LivingEntity self, final float absorption) {
        final SpongeDamageTracker tracker = this.damage$tracker();
        return tracker == null || !tracker.isSkipped(DamageStepTypes.ABSORPTION);
    }

    @WrapWithCondition(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;awardStat(Lnet/minecraft/resources/ResourceLocation;I)V"))
    private boolean damage$skipAbsorptionStat(final ServerPlayer self, final ResourceLocation stat, final int amount) {
        final SpongeDamageTracker tracker = this.damage$tracker();
        return tracker == null || !tracker.isSkipped(DamageStepTypes.ABSORPTION);
    }
}
