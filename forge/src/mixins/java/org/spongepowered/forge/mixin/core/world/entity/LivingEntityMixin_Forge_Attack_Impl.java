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
package org.spongepowered.forge.mixin.core.world.entity;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.util.DamageEventUtil;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin_Forge_Attack_Impl {

    protected DamageEventUtil.DamageEventResult attackImpl$actuallyHurtResult;
    protected float attackImpl$actuallyHurtFinalDamage;

    /**
     * Forge applies the damage from a local of its own, not the argument the common mixin sets,
     * so hand that local the event's final damage on its way through {@link ForgeHooks#onLivingDamage}
     */
    @ModifyArg(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/ForgeHooks;onLivingDamage(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;F)F"))
    private float attackImpl$useEventDamage(final float damage) {
        if (this.attackImpl$actuallyHurtResult.event().isCancelled()) {
            return 0;
        }
        return this.attackImpl$actuallyHurtFinalDamage;
    }

    /**
     * Prevents {@link ServerPlayer#awardStat} from running before event
     */
    @Redirect(method = "getDamageAfterMagicAbsorb",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;awardStat(Lnet/minecraft/stats/Stat;I)V"))
    public void attackImpl$onAwardStatDamageResist(final ServerPlayer instance, final Stat<?> resourceLocation, final int i) {
        // do nothing
    }

}
