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
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.recipe.crafting.ShapelessCraftingRecipe;

import java.util.List;
import java.util.function.Predicate;

public class SpongeShapelessCraftingRecipeBuilder implements ShapelessCraftingRecipe.Builder {

    private ItemStackSnapshot exemplaryResult = ItemStackSnapshot.NONE;
    private List<Predicate<ItemStackSnapshot>> ingredients = Lists.newArrayList();

    @Override
    public ShapelessCraftingRecipe.Builder from(ShapelessCraftingRecipe value) {
        this.exemplaryResult = value.getExemplaryResult();

        if (this.exemplaryResult == null) {
            this.exemplaryResult = ItemStackSnapshot.NONE;
        }

        this.ingredients.clear();
        this.ingredients.addAll(value.getIngredientPredicates());

        return this;
    }

    @Override
    public ShapelessCraftingRecipe.Builder reset() {
        this.exemplaryResult = ItemStackSnapshot.NONE;
        this.ingredients.clear();

        return this;
    }

    @Override
    public ShapelessCraftingRecipe.Builder addIngredientPredicate(Predicate<ItemStackSnapshot> ingredient) {
        checkNotNull(ingredient, "ingredient");
        this.ingredients.add(ingredient);

        return this;
    }

    @Override
    public ShapelessCraftingRecipe.Builder addIngredientPredicate(ItemStackSnapshot ingredient) {
        checkNotNull(ingredient, "ingredient");

        return addIngredientPredicate(new MatchCraftingVanillaItemStack(ingredient));
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public ShapelessCraftingRecipe.Builder result(ItemStackSnapshot result) {
        checkNotNull(result, "result");
        checkArgument(result != ItemStackSnapshot.NONE, "The result must not be `ItemStackSnapshot.NONE`.");

        this.exemplaryResult = result;

        return this;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public ShapelessCraftingRecipe build() {
        checkState(this.exemplaryResult != null && this.exemplaryResult != ItemStackSnapshot.NONE,
                "The result is not set.");
        checkState(!this.ingredients.isEmpty(),
                "The ingredients are not set.");

        return new SpongeShapelessCraftingRecipe(this.exemplaryResult, this.ingredients);
    }

}
