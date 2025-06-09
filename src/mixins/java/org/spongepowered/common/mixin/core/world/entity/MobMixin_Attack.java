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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.event.cause.entity.damage.DamageStepTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.bridge.world.entity.TrackedAttackBridge;
import org.spongepowered.common.event.cause.entity.damage.SpongeAttackTracker;
import org.spongepowered.common.event.cause.entity.damage.SpongeDamageStep;

import java.util.Deque;
import java.util.LinkedList;

@Mixin(Mob.class)
public abstract class MobMixin_Attack extends LivingEntityMixin_Damage implements TrackedAttackBridge {
    private final Deque<SpongeAttackTracker> attack$trackers = new LinkedList<>();

    @Override
    public final @Nullable SpongeAttackTracker attack$tracker() {
        return this.attack$trackers.peekLast();
    }

    @Inject(
        method = "doHurtTarget",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;modifyDamage(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;F)F"),
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void attack$firePreEvent(final ServerLevel level, final Entity target, final CallbackInfoReturnable<Boolean> cir,
                                     final float damage, final ItemStack weapon, final DamageSource source) {
        final SpongeAttackTracker tracker = SpongeAttackTracker.callAttackPreEvent((org.spongepowered.api.entity.Entity) target, source, damage, weapon);
        if (tracker == null) {
            cir.setReturnValue(false);
        } else {
            this.attack$trackers.addLast(tracker);
        }
    }

    @WrapOperation(method = "doHurtTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getAttackDamageBonus(Lnet/minecraft/world/entity/Entity;FLnet/minecraft/world/damagesource/DamageSource;)F"))
    private float attack$modifyBeforeAndAfterWeaponBonus(final Item item, final Entity target, final float originalDamage, final DamageSource source, final Operation<Float> operation) {
        final SpongeAttackTracker tracker = this.attack$tracker();
        if (tracker == null) {
            return operation.call(item, target, originalDamage, source);
        }

        final SpongeDamageStep step = tracker.newStep(DamageStepTypes.WEAPON_BONUS, tracker.weaponSnapshot());
        float damage = (float) step.applyChildrenBefore(originalDamage);
        if (!step.isSkipped()) {
            damage += operation.call(item, target, damage, source);
        }
        return (float) step.applyChildrenAfter(damage) - originalDamage;
    }

    @WrapOperation(method = "doHurtTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean attack$firePostEvent(final Entity target, final ServerLevel level, final DamageSource source, float damage, final Operation<Boolean> operation) {
        final SpongeAttackTracker tracker = this.attack$tracker();
        if (tracker != null) {
            final float knockbackModifier = this.shadow$getKnockback(target, source);
            if (tracker.callAttackPostEvent((org.spongepowered.api.entity.Entity) target, source, damage, knockbackModifier)) {
                return false;
            }
            damage = (float) tracker.postEvent().finalDamage();
        }
        return operation.call(target, level, source, damage);
    }

    @WrapOperation(method = "doHurtTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;getKnockback(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;)F"))
    private float attack$knockbackModifier(final Mob self, final Entity target, final DamageSource source, final Operation<Float> operation) {
        final SpongeAttackTracker tracker = this.attack$tracker();
        return tracker == null ? operation.call(self, target, source) : (float) tracker.postEvent().knockbackModifier();
    }

    @Inject(method = "doHurtTarget", at = @At("RETURN"), slice = @Slice(
        from = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;modifyDamage(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;F)F", shift = At.Shift.AFTER)))
    private void attack$removeTracker(CallbackInfoReturnable<Boolean> cir) {
        this.attack$trackers.removeLast();
    }
}
