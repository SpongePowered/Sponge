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
package org.spongepowered.common.mixin.api.minecraft.world.item.crafting;

import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Interface.Remap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

@Mixin(Ingredient.class)
@Implements(@Interface(iface = org.spongepowered.api.item.recipe.crafting.Ingredient.class, prefix = "ingredient$", remap = Remap.NONE))
public abstract class IngredientMixin_API {

    // @formatter:off
    @Shadow private ItemStack[] itemStacks;
    @Shadow protected abstract void shadow$dissolve();
    @Shadow public abstract boolean shadow$test(@Nullable ItemStack p_test_1_);
    // @formatter:on

    public List<org.spongepowered.api.item.inventory.ItemStackSnapshot> ingredient$displayedItems() {
        this.shadow$dissolve();
        return Arrays.stream(this.itemStacks).map(ItemStackUtil::snapshotOf).collect(Collectors.toList());
    }

    public boolean ingredient$test(final org.spongepowered.api.item.inventory.ItemStack itemStack) {
        return this.shadow$test(ItemStackUtil.toNative(itemStack));
    }

}
