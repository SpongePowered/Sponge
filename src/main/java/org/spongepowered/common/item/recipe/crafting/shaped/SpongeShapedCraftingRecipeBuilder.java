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


import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.datapack.DataPack;
import org.spongepowered.api.datapack.DataPacks;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.recipe.RecipeRegistration;
import org.spongepowered.api.item.recipe.crafting.Ingredient;
import org.spongepowered.api.item.recipe.crafting.RecipeInput;
import org.spongepowered.api.item.recipe.crafting.ShapedCraftingRecipe;
import org.spongepowered.common.inventory.util.InventoryUtil;
import org.spongepowered.common.item.recipe.ingredient.IngredientUtil;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.AbstractResourceKeyedBuilder;
import org.spongepowered.common.util.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class SpongeShapedCraftingRecipeBuilder extends AbstractResourceKeyedBuilder<RecipeRegistration, ShapedCraftingRecipe.Builder> implements
        ShapedCraftingRecipe.Builder, ShapedCraftingRecipe.Builder.AisleStep.ResultStep,
        ShapedCraftingRecipe.Builder.RowsStep.ResultStep, ShapedCraftingRecipe.Builder.EndStep {

    private List<String> aisle = Lists.newArrayList();
    private final Map<Character, Ingredient> ingredientMap = new Char2ObjectArrayMap<>();
    private final Map<Ingredient, Character> reverseIngredientMap = new IdentityHashMap<>();

    private ItemStack result = ItemStack.empty();
    private Function<CraftingInput, NonNullList<net.minecraft.world.item.ItemStack>> remainingItemsFunction;
    private Function<CraftingInput, net.minecraft.world.item.ItemStack> resultFunction;

    private String group;
    private DataPack<RecipeRegistration> pack = DataPacks.RECIPE;

    private RecipeCategory recipeCategory = RecipeCategory.MISC; // TODO support category
    private CraftingBookCategory craftingBookCategory = CraftingBookCategory.MISC; // TODO support category

    @Override
    public AisleStep aisle(final String... aisle) {
        Objects.requireNonNull(aisle, "aisle");
        this.aisle.clear();
        this.ingredientMap.clear();
        this.reverseIngredientMap.clear();
        Collections.addAll(this.aisle, aisle);
        return this;
    }

    @Override
    public AisleStep.ResultStep where(final char symbol, final Ingredient ingredient) throws IllegalArgumentException {
        if (this.aisle.stream().noneMatch(row -> row.indexOf(symbol) >= 0)) {
            throw new IllegalArgumentException("The symbol '" + symbol + "' is not defined in the aisle pattern.");
        }
        this.ingredientMap.put(symbol, ingredient == null ? Ingredient.empty() : ingredient);
        this.reverseIngredientMap.put(ingredient, symbol);
        return this;
    }

    @Override
    public AisleStep.ResultStep where(final Map<Character, Ingredient> ingredientMap) throws IllegalArgumentException {
        for (final Map.Entry<Character, Ingredient> entry : ingredientMap.entrySet()) {
            this.where(entry.getKey(), entry.getValue());
        }
        return this;
    }

    @Override
    public RowsStep rows() {
        this.aisle.clear();
        this.ingredientMap.clear();
        return this;
    }

    @Override
    public RowsStep.ResultStep row(final int skip, final Ingredient... ingredients) {
        final int columns = ingredients.length + skip;
        if (!this.aisle.isEmpty()) {
            Preconditions.checkState(this.aisle.get(0).length() == columns, "The rows have an inconsistent width.");
        }
        final StringBuilder row = new StringBuilder();
        for (int i = 0; i < skip; i++) {
            row.append(" ");
        }

        int key = 'A' + (columns * this.aisle.size());
        for (final Ingredient ingredient : ingredients) {
            Character usedKey = this.reverseIngredientMap.get(ingredient);
            if (usedKey == null) {
                usedKey = (char) key;
                key++;
            }
            row.append(usedKey);
            this.ingredientMap.put(usedKey, ingredient);
            this.reverseIngredientMap.put(ingredient, usedKey);
        }
        this.aisle.add(row.toString());
        return this;
    }

    @Override
    public ShapedCraftingRecipe.Builder.ResultStep remainingItems(Function<RecipeInput.Crafting, List<ItemStack>> remainingItemsFunction) {
        this.remainingItemsFunction = grid -> {
            final NonNullList<net.minecraft.world.item.ItemStack> mcList = NonNullList.create();
            remainingItemsFunction.apply(InventoryUtil.toSponge(grid)).forEach(stack -> mcList.add(ItemStackUtil.toNative(stack)));
            return mcList;
        };
        return this;
    }

    @Override
    public EndStep result(ItemStackSnapshot result) {
        Objects.requireNonNull(result, "result");
        return this.result(result.createStack());
    }

    @Override
    public EndStep result(final ItemStack result) {
        Objects.requireNonNull(result, "result");
        this.result = result.copy();
        this.resultFunction = null;
        return this;
    }

    @Override
    public EndStep result(Function<RecipeInput.Crafting, ItemStack> resultFunction, ItemStack exemplaryResult) {
        this.resultFunction = (inv) -> ItemStackUtil.toNative(resultFunction.apply(InventoryUtil.toSponge(inv)));
        this.result = exemplaryResult.copy();
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
        Preconditions.checkState(!this.aisle.isEmpty(), "aisle has not been set");
        Preconditions.checkState(!this.ingredientMap.isEmpty(), "no ingredients set");
        Preconditions.checkState(!this.result.isEmpty(), "no result set");

        final Iterator<String> aisleIterator = this.aisle.iterator();
        String aisleRow = aisleIterator.next();
        final int width = aisleRow.length();

        Preconditions.checkState(width > 0, "The aisle cannot be empty.");

        while (aisleIterator.hasNext()) {
            aisleRow = aisleIterator.next();
            Preconditions.checkState(aisleRow.length() == width, "The aisle has an inconsistent width.");
        }

        final Map<Character, net.minecraft.world.item.crafting.Ingredient> ingredientsMap = this.ingredientMap.entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, e -> IngredientUtil.toNative(e.getValue())));

        // Default space to Empty Ingredient
//        ingredientsMap.putIfAbsent(' ', net.minecraft.item.crafting.Ingredient.EMPTY);
        final ShapedRecipePattern pattern = ShapedRecipePattern.of(ingredientsMap, this.aisle);
        return new SpongeShapedCraftingRecipeRegistration((ResourceLocation) (Object) key, this.group, pattern,
                ItemStackUtil.toNative(this.result), this.resultFunction, this.remainingItemsFunction, this.pack, this.recipeCategory, this.craftingBookCategory);
    }

    @Override
    public ShapedCraftingRecipe.Builder reset() {
        super.reset();
        this.aisle = new ArrayList<>();
        this.ingredientMap.clear();
        this.result = ItemStack.empty();
        this.resultFunction = null;
        this.group = null;
        this.remainingItemsFunction = null;
        this.pack = DataPacks.RECIPE;
        return this;
    }

}
