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
package org.spongepowered.common.mixin.api.minecraft.world.item.enchantment;

import net.kyori.adventure.text.Component;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.hooks.PlatformHooks;
import org.spongepowered.common.item.util.ItemStackUtil;

@Mixin(net.minecraft.world.item.enchantment.Enchantment.class)
public abstract class EnchantmentMixin_API implements EnchantmentType {

    // @formatter:off
    @Shadow public abstract int shadow$getMinLevel();
    @Shadow public abstract int shadow$getMaxLevel();
    @Shadow public abstract int shadow$getMinCost(int level);
    @Shadow public abstract int shadow$getMaxCost(int level);
    @Shadow public abstract int shadow$getWeight();
    @Shadow @Final private net.minecraft.network.chat.Component description;
    // @formatter:on

    @Override
    public int weight() {
        return this.shadow$getWeight();
    }

    @Override
    public int minimumLevel() {
        return this.shadow$getMinLevel();
    }

    @Override
    public int maximumLevel() {
        return this.shadow$getMaxLevel();
    }

    @Override
    public int minimumEnchantabilityForLevel(final int level) {
        return this.shadow$getMinCost(level);
    }

    @Override
    public int maximumEnchantabilityForLevel(final int level) {
        return this.shadow$getMaxCost(level);
    }

    @Override
    public boolean canBeAppliedByTable(final ItemStack stack) {
        return this.canBeAppliedToStack(stack);
    }

    @Override
    public boolean canBeAppliedToStack(final ItemStack stack) {
        return PlatformHooks.INSTANCE.getItemHooks().canEnchantmentBeAppliedToItem((Enchantment) (Object) this, ItemStackUtil.toNative(stack));
    }

    @Override
    public boolean isCompatibleWith(final EnchantmentType ench) {
        return Enchantment.areCompatible(Holder.direct((Enchantment) (Object) this), Holder.direct((Enchantment) (Object) ench));
    }

    @Override
    public Component asComponent() {
        return SpongeAdventure.asAdventure(this.description);
    }

}
