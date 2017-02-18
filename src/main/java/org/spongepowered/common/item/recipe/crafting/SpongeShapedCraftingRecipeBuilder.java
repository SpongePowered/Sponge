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

import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.recipe.crafting.ShapedCraftingRecipe;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public final class SpongeShapedCraftingRecipeBuilder implements ShapedCraftingRecipe.Builder {

    private final List<String> aisle = Lists.newArrayList();
    private final Map<Character, Predicate<ItemStackSnapshot>> ingredientMap = new Char2ObjectArrayMap<>();
    private ItemStackSnapshot result = ItemStackSnapshot.NONE;

    @Override
    public ShapedCraftingRecipe.Builder aisle(String... aisle) {
        this.aisle.clear();
        ingredientMap.clear();

        if (aisle != null) {
            Collections.addAll(this.aisle, aisle);
        }

        return this;
    }

    @Override
    public ShapedCraftingRecipe.Builder where(char symbol, Predicate<ItemStackSnapshot> ingredient) throws IllegalArgumentException {
        checkState(!this.aisle.isEmpty(), "aisle must be set before setting aisle symbols");

        if (ingredient != null) {
            this.ingredientMap.put(symbol, ingredient);
        } else {
            this.ingredientMap.remove(symbol);
        }

        return this;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public ShapedCraftingRecipe.Builder where(char symbol, ItemStackSnapshot ingredient) throws IllegalArgumentException {
        return where(symbol, ingredient != null && ingredient != ItemStackSnapshot.NONE
                ? new MatchCraftingVanillaItemStack(ingredient) : null);
    }

    @Override
    public ShapedCraftingRecipe.Builder result(ItemStackSnapshot result) {
        Preconditions.checkNotNull(result, "result");

        this.result = result;

        return this;
    }

    @Override
    public ShapedCraftingRecipe build() {
        checkState(!this.aisle.isEmpty(), "aisle has not been set");
        checkState(!this.ingredientMap.isEmpty(), "no ingredients set");
        checkState(this.result != ItemStackSnapshot.NONE, "no result set");

        ImmutableTable.Builder<Integer, Integer, Predicate<ItemStackSnapshot>> tableBuilder = ImmutableTable.builder();
        Iterator<String> aisleIterator = this.aisle.iterator();
        String aisleRow = aisleIterator.next();
        int width = aisleRow.length();
        int height = 0;

        checkState(width > 0, "The aisle cannot be empty.");

        do {
            checkState(aisleRow.length() == width,
                    "The aisle has an inconsistent width.");

            for (int x = 0; x < width; x++) {
                char symbol = aisleRow.charAt(x);
                Predicate<ItemStackSnapshot> ingredientPredicate = Optional.ofNullable(this.ingredientMap.get(symbol))
                        .orElseGet(() -> itemStackSnapshot -> itemStackSnapshot == ItemStackSnapshot.NONE);

                tableBuilder.put(x, height, ingredientPredicate);
            }

            height++;

            if (!aisleIterator.hasNext()) {
                break;
            }

            aisleRow = aisleIterator.next();
        } while(true);

        return new SpongeShapedCraftingRecipe(width, height, this.result, tableBuilder.build());
    }

    @Override
    public ShapedCraftingRecipe.Builder from(ShapedCraftingRecipe value) {
        this.aisle.clear();
        this.ingredientMap.clear();

        if (value != null) {
            for (int y = 0; y < value.getHeight(); y++) {
                String row = "";

                for (int x = 0; x < value.getWidth(); x++) {
                    char symbol = (char) ('a' + x + y * value.getWidth());
                    row += symbol;
                    Predicate<ItemStackSnapshot> ingredientPredicate = value.getIngredientPredicate(x, y);

                    this.ingredientMap.put(symbol, ingredientPredicate);
                }

                this.aisle.add(row);
            }

            this.result = value.getExemplaryResult();
        } else {
            this.result = null;
        }

        return this;
    }

    @Override
    public ShapedCraftingRecipe.Builder reset() {
        this.aisle.clear();
        this.ingredientMap.clear();
        this.result = ItemStackSnapshot.NONE;

        return this;
    }

}
