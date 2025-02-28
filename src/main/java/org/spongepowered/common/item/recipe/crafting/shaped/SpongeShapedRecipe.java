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
package org.spongepowered.common.item.recipe.crafting.shaped;


import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

import java.util.function.Function;

public class SpongeShapedRecipe extends ShapedRecipe {

    private final Function<CraftingInput, net.minecraft.world.item.ItemStack> resultFunction;
    private final Function<CraftingInput, NonNullList<net.minecraft.world.item.ItemStack>> remainingItemsFunction;

    public SpongeShapedRecipe(
            final String groupIn,
            final CraftingBookCategory category,
            final ShapedRecipePattern pattern,
            final boolean showNotification,
            final ItemStack resultStack,
            final Function<CraftingInput, net.minecraft.world.item.ItemStack> resultFunction,
            final Function<CraftingInput, NonNullList<ItemStack>> remainingItemsFunction) {
        super(groupIn, category, pattern, resultStack, showNotification);
        this.resultFunction = resultFunction;
        this.remainingItemsFunction = remainingItemsFunction;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(final CraftingInput $$0) {
        if (this.remainingItemsFunction != null) {
            return this.remainingItemsFunction.apply($$0);
        }
        return super.getRemainingItems($$0);
    }

    @Override
    public ItemStack assemble(final CraftingInput $$0, final HolderLookup.Provider $$1) {
        if (this.resultFunction != null) {
            return this.resultFunction.apply($$0);
        }
        return super.assemble($$0, $$1);
    }

}
