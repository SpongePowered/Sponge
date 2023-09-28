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


import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.spongepowered.common.item.recipe.ingredient.IngredientResultUtil;

public class SpongeShapedRecipe extends ShapedRecipe {


    private final String resultFunctionId;
    private final String remainingItemsFunctionId;

    public SpongeShapedRecipe(String groupIn, final CraftingBookCategory category, int recipeWidthIn, int recipeHeightIn, NonNullList<Ingredient> recipeItemsIn,
            ItemStack recipeOutputIn, boolean showNotification,
            String resultFunctionId, String remainingItemsFunctionId) {
        super(groupIn, category, recipeWidthIn, recipeHeightIn, recipeItemsIn, recipeOutputIn, showNotification);
        this.resultFunctionId = resultFunctionId;
        this.remainingItemsFunctionId = remainingItemsFunctionId;
    }


    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        if (this.remainingItemsFunctionId != null) {
            return IngredientResultUtil.cachedRemainingItemsFunction(this.remainingItemsFunctionId).apply(inv);
        }
        return super.getRemainingItems(inv);
    }

    @Override
    public ItemStack assemble(final CraftingContainer $$0, final RegistryAccess $$1) {
        if (this.resultFunctionId != null) {
            return IngredientResultUtil.cachedResultFunction(this.resultFunctionId).apply($$0);
        }
        return super.assemble($$0, $$1);
    }

    @Override
    public ItemStack getResultItem(final RegistryAccess $$0) {
        if (this.resultFunctionId != null) {
            return ItemStack.EMPTY;
        }
        return super.getResultItem($$0);
    }
}
