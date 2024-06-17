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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.entity.LivingEntityBridge;
import org.spongepowered.common.bridge.world.entity.PlatformLivingEntityBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.context.transaction.inventory.PlayerInventoryTransaction;
import org.spongepowered.common.mixin.core.world.entity.EntityMixin;
import org.spongepowered.common.util.DamageEventUtil;
import org.spongepowered.common.util.PrettyPrinter;

import java.util.ArrayList;

@Mixin(value = LivingEntity.class, priority = 900)
public abstract class LivingEntityMixin_Forge_Attack_impl extends EntityMixin
    implements LivingEntityBridge, PlatformLivingEntityBridge {

    //@formatter:off
    @Shadow protected abstract void shadow$playHurtSound(DamageSource param0);
    @Shadow protected abstract void shadow$hurtHelmet(final DamageSource $$0, final float $$1);
    @Shadow protected abstract void shadow$hurtCurrentlyUsedShield(final float $$0);
    @Shadow protected abstract void shadow$blockUsingShield(final LivingEntity $$0);
    @Shadow protected abstract void shadow$hurtArmor(DamageSource source, float damage);
    @Shadow protected abstract float shadow$getKnockback(final Entity $$0, final DamageSource $$1);
    @Shadow public abstract ItemStack shadow$getItemInHand(final InteractionHand $$0);
    @Shadow public abstract float shadow$getAbsorptionAmount();
    @Shadow public abstract void setAbsorptionAmount(final float $$0);
    @Shadow protected int attackStrengthTicker;
    @Shadow protected float lastHurt;


    // @formatter:on
    private float attackImpl$lastHurt;
    private int attackImpl$InvulnerableTime;

    protected DamageEventUtil.Hurt attackImpl$hurt;
    protected DamageEventUtil.ActuallyHurt attackImpl$actuallyHurt;
    protected DamageEventUtil.DamageEventResult attackImpl$actuallyHurtResult;
    protected float attackImpl$actuallyHurtFinalDamage;
    protected boolean attackImpl$actuallyHurtCancelled;
    protected float attackImpl$actuallyHurtBlockedDamage;

    /**
     * Forge onLivingAttack Hook
     */
    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void attackImpl$beforeHurt(final DamageSource source, final float damageTaken, final CallbackInfoReturnable<Boolean> cir) {
        if (source == null) {
            new PrettyPrinter(60).centre().add("Null DamageSource").hr()
                .addWrapped("Sponge has found a null damage source! This should NEVER happen "
                            + "as the DamageSource is used for all sorts of calculations. Usually"
                            + " this can be considered developer error. Please report the following"
                            + " stacktrace to the most appropriate mod/plugin available.")
                .add()
                .add(new IllegalArgumentException("Null DamageSource"))
                .log(SpongeCommon.logger(), Level.WARN);
            cir.setReturnValue(false);
        }
        // Sponge - This hook is for forge use mainly
        if (!this.bridge$onLivingAttack((LivingEntity) (Object) this, source, damageTaken)) {
            cir.setReturnValue(false);
        }
    }

    /**
     * Prepare {@link org.spongepowered.common.util.DamageEventUtil.Hurt} for damage event
     */
    @Inject(method = "hurt", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;noActionTime:I"))
    private void attackImpl$preventEarlyBlock1(final DamageSource $$0, final float $$1, final CallbackInfoReturnable<Boolean> cir) {
        this.attackImpl$hurt = new DamageEventUtil.Hurt($$0, new ArrayList<>());
    }

    /**
     * Prevents shield usage before event
     * Captures the blocked damage as a function
     */
    @Redirect(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurtCurrentlyUsedShield(F)V"))
    private void attackImpl$preventEarlyBlock1(final LivingEntity instance, final float damageToShield) {
        // this.hurtCurrentlyUsedShield(damageToShield);
        this.attackImpl$hurt.functions().add(DamageEventUtil.createShieldFunction(instance));
    }

    /**
     * Prevents shield usage before event
     */
    @Redirect(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;blockUsingShield(Lnet/minecraft/world/entity/LivingEntity;)V"))
    private void attackImpl$preventEarlyBlock2(final LivingEntity instance, final LivingEntity livingDamageSource) {
        // this.blockUsingShield(livingDamageSource);
    }

    /**
     * Capture the bonus freezing damage as a function
     */
    @Inject(method = "hurt", at = @At(value = "CONSTANT", args = "floatValue=5.0F"))
    private void attackImpl$freezingBonus(final DamageSource $$0, final float $$1, final CallbackInfoReturnable<Boolean> cir) {
        this.attackImpl$hurt.functions().add(DamageEventUtil.createFreezingBonus((LivingEntity) (Object) this, $$0, 5.0F));
    }

    /**
     * Prevents {@link #shadow$hurtHelmet} before the event
     * Captures the hard hat damage reduction as a function
     */
    @Redirect(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurtHelmet(Lnet/minecraft/world/damagesource/DamageSource;F)V"))
    private void attackImpl$hardHat(final LivingEntity instance, final DamageSource $$0, final float $$1) {
        // this.hurtHelmet($$0, $$1);
        this.attackImpl$hurt.functions().add(DamageEventUtil.createHardHatModifier(instance.getItemBySlot(EquipmentSlot.HEAD), 0.75F));
    }

    /**
     * Capture the old values to reset if we end up cancelling or blocking.
     */
    @Inject(method = "hurt", at = @At(value = "FIELD",
        target = "Lnet/minecraft/world/entity/LivingEntity;walkAnimation:Lnet/minecraft/world/entity/WalkAnimationState;"))
    private void attackImpl$beforeActuallyHurt(final DamageSource source, final float damageTaken, final CallbackInfoReturnable<Boolean> cir) {
        // Save old values
        this.attackImpl$lastHurt = this.lastHurt;
        this.attackImpl$InvulnerableTime = this.invulnerableTime;
        this.attackImpl$actuallyHurtCancelled = false;
    }

    /**
     * Fake {@link #lastHurt} to be 0 so that we go to #actuallyHurt even if we are invulnerable.
     */
    @Redirect(method = "hurt",
        at = @At(value = "FIELD", ordinal = 0,
            target = "Lnet/minecraft/world/entity/LivingEntity;lastHurt:F"))
    private float attackImpl$afterActuallyHurt(final LivingEntity instance) {
        return 0;
    }

    /**
     * After calling #actuallyHurt (even when invulnerable), if cancelled return early or is still invulnerable
     * and reset {@link #lastHurt} and {@link #invulnerableTime}
     */
    @Inject(method = "hurt", locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true,
        at = @At(value = "INVOKE", shift = At.Shift.AFTER, ordinal = 0,
            target = "Lnet/minecraft/world/entity/LivingEntity;actuallyHurt(Lnet/minecraft/world/damagesource/DamageSource;F)V"))
    private void attackImpl$afterActuallyHurt1(final DamageSource $$0,
                                               final float damageTaken,
                                               final CallbackInfoReturnable<Boolean> cir,
                                               final float dealtDamage,
                                               final boolean isBlocked
    ) {
        if (this.attackImpl$actuallyHurtCancelled || damageTaken <= this.lastHurt) {
            this.invulnerableTime = this.attackImpl$InvulnerableTime;
            this.lastHurt = this.attackImpl$lastHurt;
            cir.setReturnValue(false);
        }
    }

    /**
     * After calling #actuallyHurt, if cancelled return early
     * Also reset values
     */
    @Inject(method = "hurt", locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true,
        at = @At(value = "INVOKE", shift = At.Shift.AFTER, ordinal = 1,
            target = "Lnet/minecraft/world/entity/LivingEntity;actuallyHurt(Lnet/minecraft/world/damagesource/DamageSource;F)V"))
    private void attackImpl$afterActuallyHurt2(final DamageSource $$0,
                                               final float damageTaken,
                                               final CallbackInfoReturnable<Boolean> cir,
                                               final float dealtDamage,
                                               final boolean isBlocked
    ) {
        if (this.attackImpl$actuallyHurtCancelled) {
            this.invulnerableTime = this.attackImpl$InvulnerableTime;
            cir.setReturnValue(false);
        }
    }


    /**
     * Set final damage after #actuallyHurt
     */
    @ModifyVariable(method = "hurt", argsOnly = true,
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;actuallyHurt(Lnet/minecraft/world/damagesource/DamageSource;F)V",
            shift = At.Shift.AFTER))
    private float attackImpl$modifyDamageTaken(float damageTaken) {
        return this.attackImpl$actuallyHurtFinalDamage;
    }

    /**
     * Sets blocked damage after #actuallyHurt
     */
    @ModifyVariable(method = "hurt", ordinal = 2,
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;actuallyHurt(Lnet/minecraft/world/damagesource/DamageSource;F)V",
            shift = At.Shift.AFTER))
    private float attackImpl$modifyBlockedDamage(float damageBlocked) {
        return this.attackImpl$actuallyHurtBlockedDamage;
    }

    @Redirect(method = "hurt", at = @At(value = "INVOKE",  target = "Lnet/minecraft/world/entity/LivingEntity;playHurtSound(Lnet/minecraft/world/damagesource/DamageSource;)V"))
    private void attackImpl$onHurtSound(final LivingEntity instance, final DamageSource $$0) {
        if (this.bridge$vanishState().createsSounds()) {
            this.shadow$playHurtSound($$0);
        }
    }

    @Redirect(method = "hurt", at = @At(value = "INVOKE",  target = "Lnet/minecraft/world/entity/LivingEntity;makeSound(Lnet/minecraft/sounds/SoundEvent;)V"))
    private void attackImpl$onMakeSound(final LivingEntity instance, final SoundEvent $$0) {
        if (this.bridge$vanishState().createsSounds()) {
            instance.makeSound($$0);
        }
    }

    @Redirect(method = "actuallyHurt", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/LivingEntity;isInvulnerableTo(Lnet/minecraft/world/damagesource/DamageSource;)Z"))
    public boolean attackImpl$startActuallyHurt(final LivingEntity instance, final DamageSource damageSource, final DamageSource $$0, final float originalDamage) {
        if (instance.isInvulnerableTo(damageSource)) {
            return true;
        }
        // Call platform hook for adjusting damage
        final var modAdjustedDamage = this.bridge$applyModDamage(instance, damageSource, originalDamage);
        // TODO check for direct call?
        this.attackImpl$actuallyHurt = new DamageEventUtil.ActuallyHurt(instance, new ArrayList<>(), damageSource, modAdjustedDamage);
        return false;
    }

    /**
     * Prevents LivingEntity#hurtArmor from running before event
     * and capture the armor absorption as a function
     */
    @Redirect(method = "getDamageAfterArmorAbsorb",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurtArmor(Lnet/minecraft/world/damagesource/DamageSource;F)V"))
    public void attackImpl$onDamageAfterArmorAbsorb(final LivingEntity instance, final DamageSource $$0, final float $$1) {
        if (this.attackImpl$actuallyHurt != null) {
            // prevents this.hurtArmor($$0, $$1);
            // $$1 = CombatRules.getDamageAfterAbsorb(this, $$1, $$0, (float)this.getArmorValue(), (float)this.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
            var func = DamageEventUtil.createArmorModifiers(instance, this.attackImpl$actuallyHurt.dmgSource());
            this.attackImpl$actuallyHurt.functions().add(func);
        }
    }

    /**
     * Captures the damage resistance as a function
     */
    @Inject(method = "getDamageAfterMagicAbsorb",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getEffect(Lnet/minecraft/core/Holder;)Lnet/minecraft/world/effect/MobEffectInstance;"))
    public void attackImpl$onDamageAfterMagicAbsorb(final DamageSource $$0, final float $$1, final CallbackInfoReturnable<Float> cir) {
        if (this.attackImpl$actuallyHurt != null) {
            var func = DamageEventUtil.createResistanceModifier(this.attackImpl$actuallyHurt.entity());
            this.attackImpl$actuallyHurt.functions().add(func);
        }
    }

    /**
     * Prevents {@link ServerPlayer#awardStat} from running before event
     */
    @Redirect(method = "getDamageAfterMagicAbsorb",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;awardStat(Lnet/minecraft/stats/Stat;I)V"))
    public void attackImpl$onAwardStatDamageResist(final ServerPlayer instance, final Stat<?> stat, final int i) {
        // do nothing
    }

    /**
     * Captures the damage protection as a function
     */
    @Redirect(method = "getDamageAfterMagicAbsorb",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/CombatRules;getDamageAfterMagicAbsorb(FF)F"))
    public float attackImpl$onDetDamageProtection(final float damage, final float protection) {
        if (this.attackImpl$actuallyHurt != null) {
            var func = DamageEventUtil.createEnchantmentModifiers(this.attackImpl$actuallyHurt.entity(), protection);
            this.attackImpl$actuallyHurt.functions().add(func);
        }
        return CombatRules.getDamageAfterMagicAbsorb(damage, protection);
    }

    /**
     * Prevents setting absorption before event
     * Captures the absorption amount as a functions
     * Then calls the DamageEntityEvent
     */
    @Inject(method = "setAbsorptionAmount", cancellable = true, at = @At("HEAD"))
    public void attackImpl$onSetAbsorptionAmount(final float newAmount, final CallbackInfo ci) {
        if (this.attackImpl$actuallyHurt != null) {
            ci.cancel(); // Always cancel this
            var oldAmount = this.shadow$getAbsorptionAmount();
            if (oldAmount > 0) {
                var func = DamageEventUtil.createAbsorptionModifier(this.attackImpl$actuallyHurt.entity(), oldAmount);
                this.attackImpl$actuallyHurt.functions().add(func);
            }

            this.attackImpl$actuallyHurtResult = DamageEventUtil.callLivingDamageEntityEvent(this.attackImpl$hurt, this.attackImpl$actuallyHurt);
            this.attackImpl$actuallyHurt = null;

            if (this.attackImpl$actuallyHurtResult.event().isCancelled()) {
                this.attackImpl$actuallyHurtCancelled = true;
                this.attackImpl$actuallyHurtFinalDamage = 0;
                this.attackImpl$actuallyHurtBlockedDamage = 0;
                return; // Cancel vanilla behaviour by setting absorbed & finalDamage to 0
            }

            // TODO is this actually wrong? we are actually after functions
            // (old comment was: Allow the platform to adjust damage before applying armor/etc)
            this.attackImpl$actuallyHurtFinalDamage = this.bridge$applyModDamageBeforeFunctions(
                (LivingEntity) (Object) (this),
                this.attackImpl$actuallyHurtResult.source(),
                (float) this.attackImpl$actuallyHurtResult.event().finalDamage());

            this.attackImpl$actuallyHurtResult.damageAbsorbed().ifPresent(absorbed -> this.setAbsorptionAmount(oldAmount - absorbed));
            this.attackImpl$actuallyHurtBlockedDamage = this.attackImpl$actuallyHurtResult.damageBlockedByShield().orElse(0f);
        }
    }

    /**
     * Set final damage after calling {@link LivingEntity#setAbsorptionAmount} in which we called the event
     */
    @ModifyVariable(method = "actuallyHurt", ordinal = 1,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setAbsorptionAmount(F)V", ordinal = 0, shift = At.Shift.AFTER))
    public float attackImpl$setFinalDamage(final float value) {
        if (this.attackImpl$actuallyHurtResult.event().isCancelled()) {
            return 0;
        }
        return this.attackImpl$actuallyHurtFinalDamage;
    }

    /**
     * Set absorbed damage after calling {@link LivingEntity#setAbsorptionAmount} in which we called the event
     */
    @ModifyVariable(method = "actuallyHurt", ordinal = 2,
        slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setAbsorptionAmount(F)V", ordinal = 0)),
        at = @At(value = "STORE", ordinal = 0))
    public float attackImpl$setAbsorbed(final float value) {
        if (this.attackImpl$actuallyHurtResult.event().isCancelled()) {
            return 0;
        }
        return this.attackImpl$actuallyHurtResult.damageAbsorbed().orElse(0f);
    }

    /**
     * Replay prevented
     * {@link #shadow$hurtCurrentlyUsedShield} and {@link #shadow$blockUsingShield}
     * {@link #shadow$hurtHelmet}
     * {@link #shadow$hurtArmor}
     * {@link ServerPlayer#awardStat} for {@link Stats#DAMAGE_RESISTED} and {@link Stats#DAMAGE_DEALT}
     * from {@link LivingEntity#hurt} and #actuallyHurt
     *
     * And capture inventory changes if needed
     */
    @Inject(method = "setHealth", at = @At("HEAD"))
    public void attackImpl$afterActuallyHurtEvent(final float $$0, final CallbackInfo ci) {
        final var result = this.attackImpl$actuallyHurtResult;
        if (result != null) {
            final var damageSource = result.source();
            result.damageToShield().ifPresent(dmg -> {
                this.shadow$hurtCurrentlyUsedShield(dmg);
                if (!damageSource.is(DamageTypeTags.IS_PROJECTILE)) {
                    if (damageSource.getDirectEntity() instanceof LivingEntity livingSource) {
                        this.shadow$blockUsingShield(livingSource);
                    }
                }
            });
            result.damageToHelmet().ifPresent(dmg ->
                this.shadow$hurtHelmet(damageSource, dmg));
            result.damageToArmor().ifPresent(dmg ->
                this.shadow$hurtArmor(damageSource, dmg));
            result.damageResisted().ifPresent(dmg -> {
                if ((Object) this instanceof ServerPlayer player) {
                    player.awardStat(Stats.DAMAGE_RESISTED, Math.round(dmg * 10.0F));
                } else if (damageSource.getEntity() instanceof ServerPlayer player) {
                    player.awardStat(Stats.DAMAGE_DEALT_RESISTED, Math.round(dmg * 10.0F));
                }
            });

            // Capture inventory change if we modified stacks
            if ((result.damageToShield().isPresent() ||
                 result.damageToHelmet().isPresent() ||
                 result.damageToArmor().isPresent())
                && (Object) this instanceof Player player) {
                PhaseTracker.SERVER.getPhaseContext().getTransactor().logPlayerInventoryChange(player, PlayerInventoryTransaction.EventCreator.STANDARD);
                player.inventoryMenu.broadcastChanges(); // capture
            }
        }
    }

    /**
     * Cleanup
     * also reverts {@link #attackImpl$beforeActuallyHurt}
     */
    @Inject(method = "actuallyHurt", at = @At("RETURN"))
    public void attackImpl$cleanupActuallyHurt(final DamageSource $$0, final float $$1, final CallbackInfo ci) {
        this.attackImpl$actuallyHurt = null;
        this.attackImpl$actuallyHurtResult = null;
        this.lastHurt = this.attackImpl$lastHurt;
    }

}
