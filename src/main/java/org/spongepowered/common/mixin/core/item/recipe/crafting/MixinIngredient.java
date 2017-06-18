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
package org.spongepowered.common.mixin.core.item.recipe.crafting;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(Ingredient.class)
@Implements(@Interface(iface = org.spongepowered.api.item.recipe.crafting.Ingredient.class, prefix = "ingredient$"))
public abstract class MixinIngredient {

    @Shadow @Final protected ItemStack[] matchingStacks;
    @Shadow public abstract boolean apply(ItemStack p_apply_1_);

    @Intrinsic
    public List<org.spongepowered.api.item.inventory.ItemStackSnapshot> ingredient$displayedItems() {
        return Arrays.stream(matchingStacks).map(ItemStackUtil::snapshotOf).collect(Collectors.toList());
    }

    @Intrinsic
    public boolean ingredient$test(org.spongepowered.api.item.inventory.ItemStack itemStack) {
        return this.apply(ItemStackUtil.toNative(itemStack));
    }
}
