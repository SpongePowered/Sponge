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
package org.spongepowered.common.item.recipe.crafting;

import net.minecraft.item.crafting.Ingredient;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

public class CustomIngredient extends Ingredient {

    public final List<Predicate<ItemStack>> predicates;
    public final List<ItemStack> matchItems;

    public CustomIngredient(final List<Predicate<ItemStack>> predicates, final List<ItemStack> matchItems, final List<ItemStack> displayItems) {
        super(ItemStackUtil.toNative(displayItems));
        this.predicates = predicates;
        this.matchItems = matchItems;
    }

    @Override
    public boolean apply(@Nullable final net.minecraft.item.ItemStack item) {

        // first check for matching predicates
        if (this.predicates.stream().anyMatch(p -> p.test(ItemStackUtil.fromNative(item)))) {
            return true;
        }

        // then apply same logic as super.apply(..) but with this.items
        if (item == null) {
            return false;
        }

        for (final ItemStack itemStack : this.matchItems) {
            final net.minecraft.item.ItemStack nativeItem = ItemStackUtil.toNative(itemStack);
            if (nativeItem.getItem() == item.getItem()) {
                final int i = nativeItem.func_77960_j();

                if (i == 32767 || i == item.func_77960_j())
                {
                    return true;
                }
            }
        }
        return false;
    }
}
