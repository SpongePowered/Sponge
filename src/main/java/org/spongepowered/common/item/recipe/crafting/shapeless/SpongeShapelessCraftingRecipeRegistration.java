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

import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.spongepowered.api.datapack.DataPack;
import org.spongepowered.api.item.recipe.Recipe;
import org.spongepowered.api.item.recipe.RecipeRegistration;
import org.spongepowered.api.item.recipe.crafting.ShapelessCraftingRecipe;
import org.spongepowered.common.item.recipe.SpongeRecipeRegistration;

import java.util.List;
import java.util.function.Function;

public class SpongeShapelessCraftingRecipeRegistration extends SpongeRecipeRegistration<ShapelessRecipe> implements
        SpongeRecipeRegistration.ResultFunctionRegistration<CraftingInput>,
        SpongeRecipeRegistration.RemainingItemsFunctionRegistration<CraftingInput>
{
    // Vanilla Recipe
    private final List<Ingredient> ingredients;

    // Sponge Recipe
    private final ItemStack spongeResult;
    private final Function<CraftingInput, ItemStack> resultFunction;
    private final Function<CraftingInput, NonNullList<ItemStack>> remainingItemsFunction;
    private final CraftingBookCategory craftingBookCategory;

    public SpongeShapelessCraftingRecipeRegistration(final ResourceLocation key, final String group,
            final List<Ingredient> ingredients, final ItemStack spongeResult, final Function<CraftingInput, ItemStack> resultFunction,
            final Function<CraftingInput, NonNullList<ItemStack>> remainingItemsFunction,
            final DataPack<RecipeRegistration> pack, final RecipeCategory category, final CraftingBookCategory craftingBookCategory) {
        super(key, group, pack, category, RecipeSerializer.SHAPELESS_RECIPE);
        this.ingredients = ingredients;
        this.spongeResult = spongeResult;
        this.resultFunction = resultFunction;
        this.remainingItemsFunction = remainingItemsFunction;
        this.craftingBookCategory = craftingBookCategory;
    }


    @Override
    public Function<CraftingInput, ItemStack> resultFunction() {
        return this.resultFunction;
    }

    @Override
    public Function<CraftingInput, NonNullList<ItemStack>> remainingItems() {
        return this.remainingItemsFunction;
    }

    @Override
    public Recipe recipe() {
        this.ensureCached();
        final NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.addAll(this.ingredients);

        if (SpongeRecipeRegistration.isVanillaSerializer(this.spongeResult, this.resultFunction, this.remainingItemsFunction, ingredients)) {
            return (ShapelessCraftingRecipe) new ShapelessRecipe(this.group, this.craftingBookCategory, this.spongeResult, ingredients);
        }

        return (ShapelessCraftingRecipe) new SpongeShapelessRecipe(this.group, this.craftingBookCategory, ingredients, this.spongeResult,
                this.resultFunction == null ? null : this.key.toString(),
                this.remainingItemsFunction == null ? null : this.key.toString());

    }
}
