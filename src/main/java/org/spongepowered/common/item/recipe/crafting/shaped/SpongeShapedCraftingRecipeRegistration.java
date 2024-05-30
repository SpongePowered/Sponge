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
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import org.spongepowered.api.datapack.DataPack;
import org.spongepowered.api.item.recipe.Recipe;
import org.spongepowered.api.item.recipe.RecipeRegistration;
import org.spongepowered.api.item.recipe.crafting.ShapedCraftingRecipe;
import org.spongepowered.common.item.recipe.SpongeRecipeRegistration;

import java.util.function.Function;

public class SpongeShapedCraftingRecipeRegistration extends SpongeRecipeRegistration<ShapedRecipe> implements
        SpongeRecipeRegistration.ResultFunctionRegistration<CraftingInput>,
        SpongeRecipeRegistration.RemainingItemsFunctionRegistration<CraftingInput> {

    // Vanilla Recipe
    private final ShapedRecipePattern pattern;

    // Sponge Recipe
    private final ItemStack spongeResult;
    private final Function<CraftingInput, ItemStack> resultFunction;
    private final Function<CraftingInput, NonNullList<ItemStack>> remainingItemsFunction;
    private final CraftingBookCategory craftingBookCategory;
    private final boolean showNotification = true;

    public SpongeShapedCraftingRecipeRegistration(final ResourceLocation key, final String group, final ShapedRecipePattern pattern,
            final ItemStack spongeResult, final Function<CraftingInput, ItemStack> resultFunction,
            final Function<CraftingInput, NonNullList<ItemStack>> remainingItemsFunction,
            final DataPack<RecipeRegistration> pack, final RecipeCategory category, final CraftingBookCategory craftingBookCategory) {
        super(key, group, pack, category, RecipeSerializer.SHAPED_RECIPE);
        this.pattern = pattern;
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
        if (SpongeRecipeRegistration.isVanillaSerializer(this.spongeResult, this.resultFunction, this.remainingItemsFunction, this.pattern.ingredients())) {
            return (ShapedCraftingRecipe) new ShapedRecipe(this.group, this.craftingBookCategory, this.pattern, this.spongeResult, this.showNotification);
        }
        this.ensureCached();
        return (ShapedCraftingRecipe) new SpongeShapedRecipe(this.group, this.craftingBookCategory, this.pattern, this.showNotification,
                this.spongeResult,
                this.resultFunction == null ? null : this.key.toString(),
                this.remainingItemsFunction == null ? null : this.key.toString());
    }

}
