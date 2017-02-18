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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.api.item.recipe.crafting.ShapelessCraftingRecipe;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SpongeShapelessCraftingRecipe extends ShapelessRecipes implements ShapelessCraftingRecipe {

    private final ItemStackSnapshot exemplaryResult;
    private final List<Predicate<ItemStackSnapshot>> ingredients;

    public SpongeShapelessCraftingRecipe(ItemStackSnapshot exemplaryResult, List<Predicate<ItemStackSnapshot>> ingredients) {
        super(ItemStackUtil.fromSnapshotToNative(exemplaryResult), null);

        checkNotNull(exemplaryResult, "exemplaryResult");
        checkArgument(exemplaryResult != ItemStackSnapshot.NONE, "exemplaryResult");

        ingredients.forEach(ingredient ->
                checkNotNull(ingredient, "The ingredient list must not contain null values."));

        this.exemplaryResult = exemplaryResult;
        this.ingredients = ImmutableList.copyOf(ingredients);
    }

    @Override
    public ItemStackSnapshot getExemplaryResult() {
        return this.exemplaryResult;
    }

    @Override
    public List<Predicate<ItemStackSnapshot>> getIngredientPredicates() {
        return this.ingredients;
    }

    @Override
    public boolean isValid(GridInventory grid, World world) {
        List<Predicate<ItemStackSnapshot>> ingredients = Lists.newArrayList(this.ingredients);

        for (int y = 0; y < grid.getRows(); y++) {
            byIncreasingTheSlotIndex:
            for (int x = 0; x < grid.getColumns(); x++) {
                ItemStackSnapshot itemStackSnapshot = grid.getSlot(x, y)
                        .flatMap(Slot::peek).map(ItemStack::createSnapshot)
                        .orElse(ItemStackSnapshot.NONE);

                if (itemStackSnapshot == ItemStackSnapshot.NONE) {
                    continue;
                }

                Iterator<Predicate<ItemStackSnapshot>> iterator = ingredients.iterator();

                while (iterator.hasNext()) {
                    Predicate<ItemStackSnapshot> ingredient = iterator.next();

                    if (ingredient.test(itemStackSnapshot)) {
                        iterator.remove();

                        continue byIncreasingTheSlotIndex;
                    }
                }

                return false;
            }
        }

        return ingredients.isEmpty();
    }

    @Override
    public ItemStackSnapshot getResult(GridInventory grid) {
        return this.exemplaryResult;
    }

    @Override
    public List<ItemStackSnapshot> getRemainingItems(GridInventory grid) {
        checkNotNull(grid, "grid");

        return StreamSupport.stream(grid.<Slot>slots().spliterator(), false)
                .map(Slot::peek)
                .map(potentialItem -> potentialItem.flatMap(SpongeImplHooks::getContainerItem))
                .map(potentialItem -> potentialItem.map(ItemStack::createSnapshot).orElse(ItemStackSnapshot.NONE))
                .collect(Collectors.toList());
    }

    @Override
    public int getSize() {
        return this.ingredients.size();
    }

    // IRecipe

    @Override
    public boolean matches(InventoryCrafting inv, net.minecraft.world.World worldIn) {
        return AbstractSpongeCraftingRecipe.matches(this::isValid, inv, worldIn);
    }

    @Override
    public net.minecraft.item.ItemStack getCraftingResult(InventoryCrafting inv) {
        return AbstractSpongeCraftingRecipe.getCraftingResult(this::getResult, inv);
    }

    @Override
    public int getRecipeSize() {
        return AbstractSpongeCraftingRecipe.getRecipeSize(this::getSize);
    }

    @Override
    public net.minecraft.item.ItemStack getRecipeOutput() {
        return AbstractSpongeCraftingRecipe.getRecipeOutput(this::getExemplaryResult);
    }

    @Override
    public NonNullList<net.minecraft.item.ItemStack> getRemainingItems(InventoryCrafting inv) {
        return AbstractSpongeCraftingRecipe.getRemainingItems(this::getRemainingItems, inv);
    }

}
