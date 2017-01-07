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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.Lists;
import net.minecraft.item.crafting.ShapelessRecipes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.recipe.crafting.ShapelessCraftingRecipe;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public final class SpongeShapelessCraftingRecipeBuilder implements ShapelessCraftingRecipe.Builder {

    private List<ItemStack> ingredients = Lists.newArrayList();
    @Nullable private List<ItemStack> results;

    @Override
    public ShapelessCraftingRecipe.Builder ingredients(ItemStack... ingredients) {
        this.ingredients = Lists.newArrayList(checkNotNull(ingredients, "ingredients"));
        return this;
    }

    @Override
    public ShapelessCraftingRecipe.Builder ingredients(Collection<ItemStack> ingredients) {
        this.ingredients = Lists.newArrayList(checkNotNull(ingredients, "ingredients"));
        return this;
    }

    @Override
    public ShapelessCraftingRecipe.Builder results(ItemStack... results) {
        this.results = Lists.newArrayList(checkNotNull(results, "results"));
        return this;
    }

    @Override
    public ShapelessCraftingRecipe.Builder results(Collection<ItemStack> result) {
        this.results = Lists.newArrayList(checkNotNull(results, "results"));
        return this;
    }

    @Override
    public ShapelessCraftingRecipe build() {
        checkState(!this.ingredients.isEmpty(), "no ingredients set");
        checkState(this.results != null && !this.results.isEmpty(), "no results set");
        return (ShapelessCraftingRecipe) new ShapelessRecipes(ItemStackUtil.cloneDefensiveToNative(results.get(0)),
                ingredients.stream().map(stack -> ItemStackUtil.cloneDefensiveToNative(stack)).collect(Collectors.toList()));
    }

    @Override
    public ShapelessCraftingRecipe.Builder from(ShapelessCraftingRecipe value) {
        this.ingredients = convertList(value.getIngredients());
        this.results = convertList(value.getResults());
        return this;
    }

    @Override
    public ShapelessCraftingRecipe.Builder reset() {
        this.ingredients.clear();
        this.results = null;
        return this;
    }

    private static List<ItemStack> convertList(ImmutableCollection<ItemStackSnapshot> list) {
        return list.stream().map(object -> object.createStack()).collect(Collectors.toList());
    }

}
