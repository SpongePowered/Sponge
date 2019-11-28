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
package org.spongepowered.common.mixin.optimization.enchantment;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(value = EnchantmentHelper.class, priority = 1001)
public abstract class EnchantmentHelperMixin_No_Source_Leak {

    @Shadow @Final private static EnchantmentHelper.ModifierDamage ENCHANTMENT_MODIFIER_DAMAGE;
    @Shadow @Final private static EnchantmentHelper.HurtIterator ENCHANTMENT_ITERATOR_HURT;
    @Shadow @Final private static EnchantmentHelper.DamageIterator ENCHANTMENT_ITERATOR_DAMAGE;


    @Shadow private static void applyEnchantmentModifierArray(final EnchantmentHelper.IModifier modifier, final Iterable<ItemStack> stacks) {
        // SHADOW
    }

    @Shadow private static void applyEnchantmentModifier(final EnchantmentHelper.IModifier modifier, final ItemStack stack) {
        // shadow
    }

    /**
     * @author gabizou - May 21st, 2018
     * @reason Fix memory leak of entities/worlds through damage source.
     * Fixes MC-128547
     */
    @Overwrite
    public static int getEnchantmentModifierDamage(final Iterable<ItemStack> stacks, final DamageSource source) {
        ENCHANTMENT_MODIFIER_DAMAGE.damageModifier = 0;
        ENCHANTMENT_MODIFIER_DAMAGE.source = source;
        applyEnchantmentModifierArray(ENCHANTMENT_MODIFIER_DAMAGE, stacks);
        ENCHANTMENT_MODIFIER_DAMAGE.source = null; // Sponge - Remove reference to Damagesource.
        return ENCHANTMENT_MODIFIER_DAMAGE.damageModifier;
    }


    /**
     * @author gabizou - May 21st, 2018
     * @reason Fix memory leak of entities/worlds through damage source.
     * Fixes MC-128547
     */
    @Overwrite
    public static void applyThornEnchantments(@Nullable final LivingEntity p_151384_0_, final Entity p_151384_1_) {
        ENCHANTMENT_ITERATOR_HURT.attacker = p_151384_1_;
        ENCHANTMENT_ITERATOR_HURT.user = p_151384_0_;

        if (p_151384_0_ != null) {
            applyEnchantmentModifierArray(ENCHANTMENT_ITERATOR_HURT, p_151384_0_.getEquipmentAndArmor());
        }

        if (p_151384_1_ instanceof PlayerEntity) {
            applyEnchantmentModifier(ENCHANTMENT_ITERATOR_HURT, p_151384_0_.getHeldItemMainhand());
        }
        // Sponge Start - remove references to entity objects to avoid memory leaks
        ENCHANTMENT_ITERATOR_HURT.attacker = null;
        ENCHANTMENT_ITERATOR_HURT.user = null;
        // Sponge end

    }

    /**
     * @author gabizou - May 21st, 2018
     * @reason Fix memory leak of entities/worlds through damage source.
     * Fixes MC-128547
     */
    @Overwrite
    public static void applyArthropodEnchantments(@Nullable final LivingEntity p_151385_0_, final Entity p_151385_1_) {
        ENCHANTMENT_ITERATOR_DAMAGE.user = p_151385_0_;
        ENCHANTMENT_ITERATOR_DAMAGE.target = p_151385_1_;

        if (p_151385_0_ != null) {
            applyEnchantmentModifierArray(ENCHANTMENT_ITERATOR_DAMAGE, p_151385_0_.getEquipmentAndArmor());
        }

        if (p_151385_0_ instanceof PlayerEntity) {
            applyEnchantmentModifier(ENCHANTMENT_ITERATOR_DAMAGE, p_151385_0_.getHeldItemMainhand());
        }
        // Sponge Start - remove references to entity objects to avoid memory leaks
        ENCHANTMENT_ITERATOR_DAMAGE.user = null;
        ENCHANTMENT_ITERATOR_DAMAGE.target = null;
        // Sponge end
    }
}
