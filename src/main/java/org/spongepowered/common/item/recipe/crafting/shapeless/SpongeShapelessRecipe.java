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
package org.spongepowered.common.item.recipe.crafting.shapeless;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import org.spongepowered.common.bridge.world.item.crafting.CraftingInputBridge;
import org.spongepowered.common.bridge.world.item.crafting.PlacementInfoBridge;
import org.spongepowered.common.item.recipe.ingredient.SpongeIngredient;

import java.util.List;
import java.util.function.Function;

/**
 * Customized matching algorithm matching with ingredient predicate instead of packed item in vanilla
 */
public class SpongeShapelessRecipe extends ShapelessRecipe {

    private final boolean onlyVanillaIngredients;

    private final Function<CraftingInput, ItemStack> resultFunction;
    private final Function<CraftingInput, NonNullList<net.minecraft.world.item.ItemStack>> remainingItemsFunction;

    public SpongeShapelessRecipe(final String groupIn,
            final CraftingBookCategory category,
            final List<Ingredient> recipeItemsIn,
            final ItemStack spongeResultStack,
            final Function<CraftingInput, net.minecraft.world.item.ItemStack> resultFunction,
            final Function<CraftingInput, NonNullList<ItemStack>> remainingItemsFunction) {
        super(groupIn, category, spongeResultStack, recipeItemsIn);
        this.onlyVanillaIngredients = recipeItemsIn.stream().noneMatch(i -> i instanceof SpongeIngredient);
        this.resultFunction = resultFunction;
        this.remainingItemsFunction = remainingItemsFunction;
    }

    @Override
    public boolean matches(final CraftingInput input, final Level $$1) {
        if (this.onlyVanillaIngredients) {
            return super.matches(input, $$1);
        }

        // Quick check to avoid complex calculations if possible
        if (input.ingredientCount() != this.placementInfo().ingredients().size()) {
            // The amount of non-empty stacks doesn't match the amount of ingredients
            return false;
        }

        return ((CraftingInputBridge) input).bridge$getItemStackStackedContents().tryPick(
            ((PlacementInfoBridge) this.placementInfo()).bridge$getStackIngredientInfos(), 1, null);
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
