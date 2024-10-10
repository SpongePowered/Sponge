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
package org.spongepowered.common.item.recipe.crafting.custom;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import org.spongepowered.api.datapack.DataPack;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.recipe.Recipe;
import org.spongepowered.api.item.recipe.RecipeRegistration;
import org.spongepowered.api.item.recipe.crafting.RecipeInput;
import org.spongepowered.api.item.recipe.crafting.SpecialCraftingRecipe;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.item.recipe.SpongeRecipeRegistration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class SpongeSpecialCraftingRecipeRegistration extends SpongeRecipeRegistration<SpongeSpecialRecipe> {

    public static final Map<String, SpongeSpecialRecipe> RECIPES = new HashMap<>();

    private final BiPredicate<RecipeInput.Crafting, ServerWorld> biPredicate;
    private final Function<RecipeInput.Crafting, List<ItemStack>> remainingItemsFunction;
    private final Function<RecipeInput.Crafting, ItemStack> resultFunction;

    private final SpongeSpecialRecipe recipe;

    public SpongeSpecialCraftingRecipeRegistration(ResourceLocation key,
            final CraftingBookCategory category,
            BiPredicate<RecipeInput.Crafting, ServerWorld> biPredicate,
            Function<RecipeInput.Crafting, List<ItemStack>> remainingItemsFunction,
            Function<RecipeInput.Crafting, ItemStack> resultFunction,
            DataPack<RecipeRegistration> pack, final RecipeCategory recipeCategory) {
        super(key, "", pack, recipeCategory, null);

        this.biPredicate = biPredicate;
        this.remainingItemsFunction = remainingItemsFunction;
        this.resultFunction = resultFunction;

        this.recipe = new SpongeSpecialRecipe(key, category, this.biPredicate, this.remainingItemsFunction, this.resultFunction);
        SpongeSpecialCraftingRecipeRegistration.RECIPES.put(this.recipe.id(), this.recipe);
    }

    public static SpongeSpecialRecipe get(final String id, final CraftingBookCategory category) {
        return SpongeSpecialCraftingRecipeRegistration.RECIPES.getOrDefault(id,
                new SpongeSpecialRecipe(ResourceLocation.tryParse(id), category, (x, y) -> false, null, null));
    }

    @Override
    public Recipe recipe() {
        this.ensureCached();
        return (SpecialCraftingRecipe) (Object) this.recipe;
    }
}
