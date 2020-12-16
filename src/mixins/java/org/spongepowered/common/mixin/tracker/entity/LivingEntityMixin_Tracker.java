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
package org.spongepowered.common.mixin.tracker.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.CombatTracker;
import net.minecraft.util.DamageSource;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.context.transaction.EffectTransactor;

import javax.annotation.Nullable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin_Tracker extends EntityMixin_Tracker {

    // @formatter:off
    @Shadow protected boolean dead;
    @Shadow protected int deathScore;
    @Shadow protected int lastHurtByPlayerTime;

    @Shadow protected abstract void shadow$tickDeath();
    @Shadow protected abstract int shadow$getExperienceReward(PlayerEntity player);
    @Shadow protected abstract boolean shadow$shouldDropExperience();
    @Shadow protected abstract void shadow$dropFromLootTable(DamageSource damageSourceIn, boolean p_213354_2_);
    @Shadow protected abstract void shadow$dropEquipment();
    @Shadow public abstract CombatTracker shadow$getCombatTracker();
    @Shadow @Nullable public abstract LivingEntity shadow$getKillCredit();
    @Shadow public void shadow$die(final DamageSource cause) {}
    @Shadow protected abstract void shadow$dropAllDeathLoot(DamageSource damageSourceIn);
    @Shadow protected abstract void shadow$createWitherRose(@Nullable LivingEntity p_226298_1_);
    @Shadow public abstract boolean shadow$isEffectiveAi();
    // @formatter:on

    /**
     * @author i509VCB
     * @author gabizou
     *
     * @reason We can enter in to the entity drops transaction here which will
     * successfully batch the side effects (this is effectively a singular side
     * effect/transaction instead of a pipeline) to perform some things around
     * the entity's death. This successfully records the transactions associated
     * with this entity entering into the death state.
     */
    @Redirect(method = "baseTick()V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;tickDeath()V"))
    private void tracker$enterDeathPhase(final LivingEntity livingEntity) {
        final PhaseTracker instance = PhaseTracker.SERVER;
        if (!instance.onSidedThread()) {
            this.shadow$tickDeath();
            return;
        }
        if (((WorldBridge) this.level).bridge$isFake()) {
            this.shadow$tickDeath();
            return;
        }
        final PhaseContext<@NonNull ?> context = instance.getPhaseContext();
        if (!context.doesBlockEventTracking()) {
            this.shadow$tickDeath();
            return;
        }
        try (final EffectTransactor ignored = context.getTransactor().ensureEntityDropTransactionEffect((LivingEntity) (Object) this)) {
            this.shadow$tickDeath();
        }
    }

    /**
     * @author gabizou
     * @reason Instead of inlining the onDeath method with the main mixin, we can "toggle"
     * the usage of the death state control in the tracker mixin.
     */
    @Redirect(method = "hurt",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;die(Lnet/minecraft/util/DamageSource;)V"
        )
    )
    private void tracker$wrapOnDeathWithState(final LivingEntity thisEntity, final DamageSource cause) {
        // Sponge Start - notify the cause tracker
        final PhaseTracker instance = PhaseTracker.SERVER;
        if (!instance.onSidedThread()) {
            return;
        }
        if (((WorldBridge) this.level).bridge$isFake()) {
            return;
        }
        final PhaseContext<@NonNull ?> context = instance.getPhaseContext();
        if (!context.doesBlockEventTracking()) {
            return;
        }
        try (final EffectTransactor ignored = context.getTransactor().ensureEntityDropTransactionEffect((LivingEntity) (Object) this)) {
            // Create new EntityDeathTransaction
            // Add new EntityDeathEffect
            this.shadow$die(cause);
        }
        // Sponge End
    }
}
