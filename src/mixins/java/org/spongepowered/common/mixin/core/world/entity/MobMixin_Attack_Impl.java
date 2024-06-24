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

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.util.DamageEventUtil;

import java.util.ArrayList;

@Mixin(Mob.class)
public abstract class MobMixin_Attack_Impl extends LivingEntityMixin_Attack_Impl {

    private double impl$hurtTargetDamage;
    private double impl$knockbackModifier;

    @Redirect(method = "doHurtTarget", at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Mob;getAttributeValue(Lnet/minecraft/core/Holder;)D"))
    private double attackImpl$onCanGrief(final Mob instance, final Holder<Attribute> attackDamageAttribute) {
        this.impl$hurtTargetDamage = instance.getAttributeValue(attackDamageAttribute);
        return this.impl$hurtTargetDamage;
    }

    @Redirect(method = "doHurtTarget", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean attackImpl$onCanGrief(final net.minecraft.world.entity.Entity targetEntity, final DamageSource damageSource, final float mcFinalDamage) {
        final var thisEntity = (Mob) (Object) this;

        float knockbackModifier = this.shadow$getKnockback(targetEntity, damageSource);

        var attack = new DamageEventUtil.Attack<>(thisEntity, targetEntity, this.shadow$getWeaponItem(), damageSource, 1, (float) this.impl$hurtTargetDamage, new ArrayList<>());
        if (this.shadow$level() instanceof ServerLevel) {
            // baseDamage = EnchantmentHelper.modifyDamage(level, thisEntity.getWeaponItem(), targetEntity, damageSource, baseDamage);//
            attack.functions().addAll(DamageEventUtil.createAttackEnchantmentFunction(attack.weapon(), targetEntity, damageSource));
        }

        final var event = DamageEventUtil.callMobAttackEvent(attack, knockbackModifier);
        this.impl$knockbackModifier = event.knockbackModifier();

        return targetEntity.hurt(damageSource, (float) event.finalOutputDamage());
    }

    @Redirect(method = "doHurtTarget", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Mob;getKnockback(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;)F"))
    private float attackImpl$onCanGrief(final Mob instance, final net.minecraft.world.entity.Entity entity, final DamageSource damageSource) {
        return (float) this.impl$knockbackModifier;
    }

}
