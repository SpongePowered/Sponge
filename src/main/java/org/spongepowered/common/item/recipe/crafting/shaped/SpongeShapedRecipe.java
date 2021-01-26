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

import org.spongepowered.common.item.recipe.ingredient.SpongeIngredient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class SpongeShapedRecipe extends ShapedRecipe {

    private final Function<CraftingContainer, ItemStack> resultFunction;
    private final Function<CraftingContainer, NonNullList<ItemStack>> remainingItemsFunction;

    public SpongeShapedRecipe(ResourceLocation idIn, String groupIn, int recipeWidthIn, int recipeHeightIn, NonNullList<Ingredient> recipeItemsIn,
            ItemStack recipeOutputIn, Function<CraftingContainer, ItemStack> resultFunction,
            Function<CraftingContainer, NonNullList<ItemStack>> remainingItemsFunction) {
        super(idIn, groupIn, recipeWidthIn, recipeHeightIn, recipeItemsIn, recipeOutputIn);
        this.resultFunction = resultFunction;
        this.remainingItemsFunction = remainingItemsFunction;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        if (this.remainingItemsFunction != null) {
            return this.remainingItemsFunction.apply(inv);
        }
        return super.getRemainingItems(inv);
    }

    @Override
    public ItemStack assemble(CraftingContainer p_77572_1_) {
        if (this.resultFunction != null) {
            return this.resultFunction.apply(p_77572_1_);
        }
        return super.assemble(p_77572_1_);
    }

    @Override
    public ItemStack getResultItem() {
        if (this.resultFunction != null) {
            return ItemStack.EMPTY;
        }
        return super.getResultItem();
    }
}
