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
package org.spongepowered.common.mixin.api.mcp.enchantment;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.hooks.PlatformHooks;
import org.spongepowered.common.item.util.ItemStackUtil;

import javax.annotation.Nullable;

@Mixin(net.minecraft.enchantment.Enchantment.class)
@Implements(@Interface(iface = EnchantmentType.class, prefix = "enchantment$"))
public abstract class EnchantmentMixin_API implements EnchantmentType {

    @Shadow protected String name;
    @Shadow @Final private net.minecraft.enchantment.Enchantment.Rarity rarity;
    @Shadow public abstract int shadow$getMinLevel();
    @Shadow public abstract int shadow$getMaxLevel();
    @Shadow public abstract int shadow$getMinEnchantability(int level);
    @Shadow public abstract int shadow$getMaxEnchantability(int level);
    @Shadow protected abstract boolean shadow$canApplyTogether(net.minecraft.enchantment.Enchantment ench);
    @Shadow public abstract String shadow$getName();
    @Shadow public abstract boolean shadow$isTreasureEnchantment();
    @Shadow public abstract boolean shadow$isCurse();

    @Nullable private ResourceLocation api$id;

    @SuppressWarnings("ConstantConditions")
    @Override
    public ResourceKey getKey() {
        if (this.api$id == null) {
            final ResourceLocation id = Registry.ENCHANTMENT.getKey((Enchantment) (Object) this);
            if (id != null) {
                this.api$id = id;
            }
        }
        return (ResourceKey) (Object) this.api$id;
    }

    @Override
    public int getWeight() {
        return this.rarity.getWeight();
    }

    @Override
    public int getMinimumLevel() {
        return this.shadow$getMinLevel();
    }

    @Override
    public int getMaximumLevel() {
        return this.shadow$getMaxLevel();
    }

    @Override
    public int getMinimumEnchantabilityForLevel(final int level) {
        return this.shadow$getMinEnchantability(level);
    }

    @Override
    public int getMaximumEnchantabilityForLevel(final int level) {
        return this.shadow$getMaxEnchantability(level);
    }

    @Override
    public boolean canBeAppliedByTable(final ItemStack stack) {
        return this.canBeAppliedToStack(stack);
    }

    @Override
    public boolean canBeAppliedToStack(final ItemStack stack) {
        return PlatformHooks.getInstance().getItemHooks().canEnchantmentBeAppliedToItem((Enchantment) (Object) this, ItemStackUtil.toNative(stack));
    }

    @Override
    public boolean isCompatibleWith(final EnchantmentType ench) {
        return this.shadow$canApplyTogether((net.minecraft.enchantment.Enchantment) ench);
    }

    @Override
    public Component asComponent() {
        return Component.translatable(this.shadow$getName(), this.shadow$isCurse() ? NamedTextColor.RED : NamedTextColor.GRAY);
    }

    @Override
    public boolean isTreasure() {
        return this.shadow$isTreasureEnchantment();
    }

    @Intrinsic
    public boolean enchantment$isCurse() {
        return this.shadow$isCurse();
    }

}
