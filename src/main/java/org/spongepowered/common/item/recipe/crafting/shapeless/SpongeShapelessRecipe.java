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
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import org.spongepowered.common.item.recipe.ingredient.SpongeIngredient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Customized matching algorithm matching with ingredient predicate instead of packed item in vanilla
 */
public class SpongeShapelessRecipe extends ShapelessRecipe {

    private final boolean onlyVanillaIngredients;
    private final Function<CraftingContainer, ItemStack> resultFunction;
    private final Function<CraftingContainer, NonNullList<ItemStack>> remainingItemsFunction;

    public SpongeShapelessRecipe(ResourceLocation idIn, String groupIn, CraftingBookCategory category, ItemStack recipeOutputIn, NonNullList<Ingredient> recipeItemsIn,
            Function<CraftingContainer, ItemStack> resultFunction,
            Function<CraftingContainer, NonNullList<ItemStack>> remainingItemsFunction) {
        super(idIn, groupIn, category, recipeOutputIn, recipeItemsIn);
        this.onlyVanillaIngredients = recipeItemsIn.stream().noneMatch(i -> i instanceof SpongeIngredient);
        this.resultFunction = resultFunction;
        this.remainingItemsFunction = remainingItemsFunction;
    }

    @Override
    public boolean matches(CraftingContainer inv, Level p_77569_2_) {
        if (this.onlyVanillaIngredients) {
            return super.matches(inv, p_77569_2_);
        }
        List<ItemStack> items = new ArrayList<>();
        for(int j = 0; j < inv.getContainerSize(); ++j) {
            final ItemStack itemstack = inv.getItem(j);
            if (!itemstack.isEmpty()) {
                items.add(itemstack);
            }
        }
        return SpongeShapelessRecipe.matches(items, this.getIngredients());
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        if (this.remainingItemsFunction != null) {
            return this.remainingItemsFunction.apply(inv);
        }
        return super.getRemainingItems(inv);
    }

    @Override
    public ItemStack assemble(CraftingContainer container, final RegistryAccess $$1) {
        if (this.resultFunction != null) {
            return this.resultFunction.apply(container);
        }
        return super.assemble(container, $$1);
    }

    @Override
    public ItemStack getResultItem(final RegistryAccess $$1) {
        if (this.resultFunction != null) {
            return ItemStack.EMPTY;
        }
        return super.getResultItem($$1);
    }

    private static boolean matches(List<ItemStack> stacks, List<Ingredient> ingredients) {
        final int elements = ingredients.size();
        if (stacks.size() != elements) {
            return false;
        }

        // find matched stack -> ingredient list
        final Map<Integer, List<Integer>> matchesMap = new HashMap<>();
        for (int i = 0; i < ingredients.size(); i++) {
            Ingredient ingredient = ingredients.get(i);
            boolean noMatch = true;
            for (int j = 0; j < stacks.size(); j++) {
                if (ingredient.test(stacks.get(j))) {
                    matchesMap.computeIfAbsent(j, k -> new ArrayList<>()).add(i);;
                    noMatch = false;
                }
            }
            if (noMatch) {
                // one ingredient had no match recipe does not match at all
                return false;
            }
        }

        if (matchesMap.isEmpty()) {
            return false;
        }

        // Every ingredient had at least one matching stack
        // Now check if each stack matches one ingredient
        final List<Collection<Integer>> stackList = new ArrayList<>(matchesMap.values());
        stackList.sort(Comparator.comparingInt(Collection::size));
        return SpongeShapelessRecipe.matchesRecursive(stackList, 0, new HashSet<>());
    }

    private static boolean matchesRecursive(List<Collection<Integer>> stackList, int d, Set<Integer> used) {
        if (d == stackList.size()) {
            return true;
        }

        final Collection<Integer> stacks = stackList.get(d);
        for (Integer stack : stacks) {
            if (used.contains(stack)) {
                // each stack is only used once
                continue;
            }
            final HashSet<Integer> copy = new HashSet<>(used);
            copy.add(stack);
            if (SpongeShapelessRecipe.matchesRecursive(stackList, d + 1, copy)) {
                return true;
            }
        }
        return false;
    }

}
