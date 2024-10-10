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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.datapack.DataPack;
import org.spongepowered.api.datapack.DataPacks;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStackLike;
import org.spongepowered.api.item.recipe.RecipeRegistration;
import org.spongepowered.api.item.recipe.crafting.RecipeInput;
import org.spongepowered.api.item.recipe.crafting.ShapelessCraftingRecipe;
import org.spongepowered.common.inventory.util.InventoryUtil;
import org.spongepowered.common.item.recipe.ingredient.IngredientUtil;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.AbstractResourceKeyedBuilder;
import org.spongepowered.common.util.Preconditions;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class SpongeShapelessCraftingRecipeBuilder extends AbstractResourceKeyedBuilder<RecipeRegistration, ShapelessCraftingRecipe.Builder>
        implements ShapelessCraftingRecipe.Builder.EndStep, ShapelessCraftingRecipe.Builder.ResultStep {

    private org.spongepowered.api.item.inventory.ItemStack result;
    private Function<CraftingInput, net.minecraft.world.item.ItemStack> resultFunction;
    private Function<CraftingInput, NonNullList<net.minecraft.world.item.ItemStack>> remainingItemsFunction;
    private final NonNullList<Ingredient> ingredients = NonNullList.create();
    private String group;
    private DataPack<RecipeRegistration> pack = DataPacks.RECIPE;

    private RecipeCategory recipeCategory = RecipeCategory.MISC; // TODO support category
    private CraftingBookCategory craftingBookCategory = CraftingBookCategory.MISC; // TODO support category

    @Override
    public ResultStep addIngredients(ItemType... ingredients) {
        for (ItemType ingredient : ingredients) {
            this.ingredients.add(Ingredient.of(() -> ((Item) ingredient)));
        }
        return this;
    }

    @Override
    public ResultStep addIngredients(Supplier<? extends ItemType>... ingredients) {
        for (Supplier<? extends ItemType> ingredient : ingredients) {
            this.ingredients.add(Ingredient.of(() -> ((Item) ingredient.get())));
        }
        return this;
    }

    @Override
    public ResultStep addIngredients(org.spongepowered.api.item.recipe.crafting.Ingredient... ingredients) {
        for (org.spongepowered.api.item.recipe.crafting.Ingredient ingredient : ingredients) {
            this.ingredients.add(IngredientUtil.toNative(ingredient));
        }
        return this;
    }

    @Override
    public ResultStep remainingItems(Function<RecipeInput.Crafting, ? extends List<? extends ItemStackLike>> remainingItemsFunction) {
        this.remainingItemsFunction = grid -> {
            final NonNullList<ItemStack> mcList = NonNullList.create();
            remainingItemsFunction.apply(InventoryUtil.toSponge(grid)).forEach(stack -> mcList.add(ItemStackUtil.fromLikeToNative(stack)));
            return mcList;
        };
        return this;
    }

    @Override
    public EndStep result(ItemStackLike result) {
        Objects.requireNonNull(result, "result");
        this.result = result.asMutable();
        this.resultFunction = null;
        return this;
    }

    @Override
    public EndStep result(Function<RecipeInput.Crafting, ? extends ItemStackLike> resultFunction, ItemStackLike exemplaryResult) {
        this.resultFunction = (input) -> ItemStackUtil.fromLikeToNative(resultFunction.apply(InventoryUtil.toSponge(input)));
        this.result = exemplaryResult.asMutableCopy();
        return this;
    }

    @Override
    public EndStep group(final @Nullable String name) {
        this.group = name;
        return this;
    }

    @Override
    public EndStep pack(final DataPack<RecipeRegistration> pack) {
        this.pack = pack;
        return this;
    }

    @Override
    public RecipeRegistration build0() {
        Preconditions.checkState(!this.ingredients.isEmpty(), "The ingredients are not set.");
        return new SpongeShapelessCraftingRecipeRegistration((ResourceLocation) (Object) key, this.group, this.ingredients,
                ItemStackUtil.toNative(this.result), this.resultFunction, this.remainingItemsFunction, this.pack,
                this.recipeCategory, this.craftingBookCategory);
    }

    @Override
    public ShapelessCraftingRecipe.Builder reset() {
        super.reset();
        this.result = null;
        this.resultFunction = null;
        this.ingredients.clear();
        this.group = null;
        this.remainingItemsFunction = null;
        this.pack = DataPacks.RECIPE;
        return this;
    }
}
