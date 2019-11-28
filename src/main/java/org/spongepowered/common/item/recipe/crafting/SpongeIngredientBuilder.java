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

import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.recipe.crafting.Ingredient;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.mixin.core.item.crafting.IngredientAccessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class SpongeIngredientBuilder implements Ingredient.Builder {

    private List<Predicate<ItemStack>> predicates = new ArrayList<>();
    private List<ItemStack> matchItems = new ArrayList<>();
    private List<ItemStack> displayItems = new ArrayList<>();

    @SuppressWarnings({"rawtypes", "ConstantConditions"})
    @Override
    public Ingredient.Builder from(final Ingredient value) {
        this.reset();

        final Class ingredientClass = value.getClass();
        if (ingredientClass == net.minecraft.item.crafting.Ingredient.class) {
            // Vanilla Ingredient?
            this.matchItems.addAll(ItemStackUtil.fromNative(((IngredientAccessor) value).accessor$getMatchingStacks()));
            this.displayItems.addAll(this.matchItems);
        }
        else if (ingredientClass == CustomIngredient.class) {
            // CustomIngredient?
            final CustomIngredient custom = (CustomIngredient) (Object) value;
            this.displayItems = ItemStackUtil.fromNative(((IngredientAccessor) custom).accessor$getMatchingStacks());
            this.predicates = custom.predicates;
            this.matchItems = custom.matchItems;
        }
        else if (net.minecraft.item.crafting.Ingredient.class.isAssignableFrom(ingredientClass)) {
            // Extends Vanilla Ingredient?
            this.displayItems.addAll(ItemStackUtil.fromNative(((IngredientAccessor) value).accessor$getMatchingStacks()));
            this.predicates.add(value);
        }
        else {
            // Unknown
            this.predicates.add(value);
        }
        return this;
    }

    @Override
    public Ingredient.Builder reset() {
        this.predicates.clear();
        this.matchItems.clear();
        this.displayItems.clear();
        return this;
    }

    @Override
    public Ingredient.Builder with(final Predicate<ItemStack> predicate) {
        this.predicates.add(predicate);
        this.displayItems.clear();
        return this;
    }

    @Override
    public Ingredient.Builder with(final ItemStackSnapshot... items) {
        Arrays.stream(items).map(ItemStackSnapshot::createStack).forEach(this.matchItems::add);
        return withDisplay(items);
    }

    @Override
    public Ingredient.Builder with(final ItemStack... items) {
        Arrays.stream(items).map(ItemStack::copy).forEach(this.matchItems::add);
        return withDisplay(items);
    }

    @Override
    public Ingredient.Builder with(final ItemType... items) {
        Arrays.stream(items).map(t -> (Predicate<ItemStack>) t::matches).forEach(this.predicates::add);
        return withDisplay(items);
    }

    @Override
    public Ingredient.Builder withDisplay(final ItemStack... items) {
        Arrays.stream(items).map(ItemStack::copy).forEach(this.displayItems::add);
        return this;
    }

    @Override
    public Ingredient.Builder withDisplay(final ItemType... types) {
        Arrays.stream(types).map(t -> ItemStack.of(t, 1)).forEach(this.displayItems::add);
        return this;
    }

    @Override
    public Ingredient.Builder withDisplay(final ItemStackSnapshot... items) {
        Arrays.stream(items).map(ItemStackSnapshot::createStack).forEach(this.displayItems::add);
        return this;
    }

    @Override
    public Ingredient build() {
        if (this.predicates.isEmpty() && this.matchItems.equals(this.displayItems)) {
            final net.minecraft.item.ItemStack[] stacks = this.matchItems.stream().map(ItemStackUtil::toNative).toArray(net.minecraft.item.ItemStack[]::new);
            return IngredientUtil.fromNative(net.minecraft.item.crafting.Ingredient.func_193369_a(stacks));
        }
        return IngredientUtil.fromNative(new CustomIngredient(this.predicates, this.matchItems, this.displayItems));
    }
}
