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
package org.spongepowered.common.item.recipe.ingredient;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.recipe.crafting.Ingredient;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class SpongeIngredientBuilder implements Ingredient.Builder {

    private ItemType[] types;
    private ResourceKey itemTag;
    private ItemStack[] stacks;
    private Predicate<ItemStack> predicate;
    private ResourceKey key;

    @Override
    public Ingredient.Builder reset() {
        this.types = null;
        this.itemTag = null;
        this.stacks = null;
        this.predicate = null;
        this.key = null;
        return this;
    }

    @Override
    public Ingredient.Builder with(ItemType... types) {
        this.reset();
        this.types = types;
        return this;
    }


    @Override
    public Ingredient.Builder with(Supplier<? extends ItemType>... types) {
        this.reset();
        this.types = Arrays.stream(types).map(Supplier::get).toArray(ItemType[]::new);
        return this;
    }

    @Override
    public Ingredient.Builder with(ResourceKey itemTag) {
        this.reset();
        this.itemTag = itemTag;
        return this;
    }

    @Override
    public Ingredient.Builder with(ItemStack... types) {
        this.reset();
        this.stacks = types;
        return this;
    }

    @Override
    public Ingredient.Builder with(ItemStackSnapshot... types) {
        this.reset();
        this.stacks = Arrays.stream(types).map(ItemStackSnapshot::createStack).toArray(ItemStack[]::new);
        return this;
    }

    @Override
    public Ingredient.Builder with(ResourceKey key, Predicate<ItemStack> predicate, ItemStack... exemplaryTypes) {
        this.reset();
        this.stacks = exemplaryTypes;
        this.predicate = predicate;
        this.key = key;
        return this;
    }

    @Override
    public Ingredient build() {
        if (this.itemTag != null) {
            return IngredientUtil.of(this.itemTag);
        }
        if (this.types != null && this.types.length > 0) {
            return IngredientUtil.of(this.types);
        }
        if (this.stacks != null) {
            if (this.predicate != null) {
                return IngredientUtil.of(this.key, this.predicate, this.stacks);
            }
            return IngredientUtil.of(this.stacks);
        }
        throw new IllegalStateException("An ingredient must have at least one ItemType or an item tag");
    }
}