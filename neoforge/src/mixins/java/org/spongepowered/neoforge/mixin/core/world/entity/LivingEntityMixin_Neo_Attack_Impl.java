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
package org.spongepowered.neoforge.mixin.core.world.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.util.DamageEventUtil;

import java.util.Stack;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin_Neo_Attack_Impl {

    @Shadow protected Stack<DamageContainer> damageContainers;

    protected DamageEventUtil.DamageEventResult attackImpl$actuallyHurtResult;
    protected float attackImpl$actuallyHurtFinalDamage;

    /**
     * Set absorbed damage after calling {@link LivingEntity#setAbsorptionAmount} in which we called the event
     */
    @ModifyVariable(method = "actuallyHurt", ordinal = 2,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setAbsorptionAmount(F)V", shift = At.Shift.AFTER))
    public float attackImpl$setAbsorbed(final float value) {
        if (this.attackImpl$actuallyHurtResult.event().isCancelled()) {
            return 0;
        }
        return this.attackImpl$actuallyHurtResult.damageAbsorbed().orElse(0f);
    }

    /**
     * Neo applies the damage its container holds, not the argument the common mixin sets,
     * so give the container the event's final damage right after the event in {@link LivingEntity#setAbsorptionAmount}
     */
    @Inject(method = "actuallyHurt",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setAbsorptionAmount(F)V", shift = At.Shift.AFTER))
    private void attackImpl$useEventDamage(final DamageSource source, final float damage, final CallbackInfo ci) {
        this.damageContainers.peek().setNewDamage(this.attackImpl$actuallyHurtResult.event().isCancelled() ? 0 : this.attackImpl$actuallyHurtFinalDamage);
    }

    /**
     * Prevents {@link ServerPlayer#awardStat} from running before event
     */
    @Redirect(method = "getDamageAfterMagicAbsorb",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;awardStat(Lnet/minecraft/resources/ResourceLocation;I)V"))
    public void attackImpl$onAwardStatDamageResist(final ServerPlayer instance, final ResourceLocation resourceLocation, final int i) {
        // do nothing
    }

}
