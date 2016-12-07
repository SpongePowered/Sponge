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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.item.crafting.ShapedRecipes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.recipe.crafting.ShapedCraftingRecipe;
import org.spongepowered.common.interfaces.item.crafting.IMixinShapedRecipes;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public final class SpongeShapedCraftingRecipeBuilder implements ShapedCraftingRecipe.Builder {

    private final List<String> aisle = Lists.newArrayList();
    private Map<Character, ItemStack> ingredientMap = Maps.newHashMap();
    @Nullable private List<ItemStack> results;

    @Override
    public ShapedCraftingRecipe.Builder aisle(String... aisle) {
        checkNotNull(aisle, "aisle");
        checkArgument(aisle.length > 0, "aisle cannot be empty");
        this.aisle.clear();
        Collections.addAll(this.aisle, aisle);
        return this;
    }

    @Override
    public ShapedCraftingRecipe.Builder where(char symbol, @Nullable ItemStack ingredient) throws IllegalArgumentException {
        checkState(!this.aisle.isEmpty(), "aisle must be set before setting aisle symbols");
        this.ingredientMap.put(symbol, ingredient);
        return this;
    }

    @Override
    public ShapedCraftingRecipe.Builder results(ItemStack... results) {
        this.results = Lists.newArrayList(checkNotNull(results, "results"));
        return this;
    }

    @Override
    public ShapedCraftingRecipe.Builder results(Iterable<ItemStack> result) {
        this.results = Lists.newArrayList(result);
        return this;
    }

    @Override
    public ShapedCraftingRecipe build() {
        checkState(!this.aisle.isEmpty(), "aisle has not been set");
        checkState(!this.ingredientMap.isEmpty(), "no ingredients set");
        checkState(this.results != null && !this.results.isEmpty(), "no results set");

        net.minecraft.item.ItemStack stack = null;
        String aisle = "";
        int width = 0;
        int height = 0;

        for (String aislePart : this.aisle) {
            height++;
            width = aislePart.length();
            aisle = aisle + aislePart;
        }

        Map<Character, net.minecraft.item.ItemStack> ingredientMap = (Map<Character, net.minecraft.item.ItemStack>) (Object) this.ingredientMap;
        net.minecraft.item.ItemStack[] ingredients = new net.minecraft.item.ItemStack[width * height];

        for (int pos = 0; pos < width * height; pos++) {
            char symbol = aisle.charAt(pos);

            if (ingredientMap.containsKey(symbol)) {
                ingredients[pos] = ingredientMap.get(symbol).copy();
            } else {
                ingredients[pos] = null;
            }
        }

        ShapedRecipes shapedrecipes = new ShapedRecipes(width, height, ingredients, stack);
        ((IMixinShapedRecipes) shapedrecipes).setIngredientMap(ingredientMap);
        return (ShapedCraftingRecipe) shapedrecipes;
    }

    @Override
    public ShapedCraftingRecipe.Builder from(ShapedCraftingRecipe value) {
        this.aisle.clear();
        this.aisle.addAll(value.getAisle());
        this.ingredientMap = Maps.newHashMap(value.getIngredients());
        this.results = Lists.newArrayList(value.getResults());
        return this;
    }

    @Override
    public ShapedCraftingRecipe.Builder reset() {
        this.aisle.clear();
        this.ingredientMap.clear();
        this.results = null;
        return this;
    }

}
