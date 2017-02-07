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

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.NonNullList;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.api.item.recipe.crafting.ShapedCraftingRecipe;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SpongeShapedCraftingRecipe extends ShapedRecipes implements ShapedCraftingRecipe {

    private final Table<Integer, Integer, Predicate<ItemStackSnapshot>> ingredients;
    private final ItemStackSnapshot exemplaryResult;

    public SpongeShapedCraftingRecipe(int width, int height, ItemStackSnapshot result, Table<Integer, Integer, Predicate<ItemStackSnapshot>> ingredients) {
        super(width, height, new net.minecraft.item.ItemStack[0], ItemStackUtil.fromSnapshotToNative(result));

        Collection<Predicate<ItemStackSnapshot>> ingredientCollection = ingredients.values();

        checkNotNull(result, "result");
        checkArgument(ingredientCollection.size() == width * height,
                "The ingredient table is missing some nodes, make sure it's filled!");
        ingredientCollection.forEach(ingredient -> checkNotNull(ingredient,
                "The ingredient table must not contain `null` values."));

        this.ingredients = ImmutableTable.copyOf(ingredients);
        this.exemplaryResult = result;
    }

    @Override
    public boolean isValid(GridInventory grid, World world) {
        int gapWidth = grid.getColumns() - getWidth();
        int gapHeight = grid.getRows() - getHeight();

        if (gapWidth < 0 || gapHeight < 0) {
            return false;
        }

        // Shift the aisle along the grid wherever possible
        for (int offsetX = 0; offsetX <= gapWidth; offsetX++) {
            byShiftingTheAisle:
            for (int offsetY = 0; offsetY <= gapHeight; offsetY++) {
                // Test each predicate in the aisle
                for (int aisleX = 0; aisleX < getWidth(); aisleX++) {
                    for (int aisleY = 0; aisleY < getHeight(); aisleY++) {
                        int gridX = aisleX + offsetX;
                        int gridY = aisleY + offsetY;
                        Slot slot = grid.getSlot(gridX, gridY)
                                .orElseThrow(() -> new IllegalStateException("Could not access the slot," +
                                        " even though it was supposed to be in bounds."));
                        Optional<ItemStack> itemStackOptional = slot.peek();
                        ItemStackSnapshot itemStackSnapshot = itemStackOptional.map(ItemStack::createSnapshot)
                                .orElse(ItemStackSnapshot.NONE);
                        Predicate<ItemStackSnapshot> ingredientPredicate = getIngredientPredicate(aisleX, aisleY);

                        if (!ingredientPredicate.test(itemStackSnapshot)) {
                            continue byShiftingTheAisle;
                        }
                    }
                }

                // Make sure the gap is empty:

                // First ensure gap rows are empty
                for (int gapY = 0; gapY < gapHeight; gapY++) {
                    for (int gridX = 0; gridX < grid.getColumns(); gridX++) {
                        int gridY = gapY + (gapY >= offsetY ? getHeight() : 0);
                        boolean empty = grid.getSlot(gridX, gridY)
                                .flatMap(Slot::peek)
                                .map(itemStack -> itemStack.getItem() == ItemTypes.NONE)
                                .orElse(true);

                        if (!empty) {
                            continue byShiftingTheAisle;
                        }
                    }
                }

                // Then, check the remaining space to the left & right of the aisle
                for (int aisleY = 0; aisleY < getHeight(); aisleY++) {
                    for (int gapX = 0; gapX < gapWidth; gapX++) {
                        int gridX = gapX + (gapX >= offsetX ? getWidth() : 0);
                        int gridY = aisleY + offsetY;

                        boolean empty = grid.getSlot(gridX, gridY)
                                .flatMap(Slot::peek)
                                .map(itemStack -> itemStack.getItem() == ItemTypes.NONE)
                                .orElse(true);

                        if (!empty) {
                            continue byShiftingTheAisle;
                        }
                    }
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public ItemStackSnapshot getResult(GridInventory grid) {
        return getExemplaryResult();
    }

    @Override
    public List<ItemStackSnapshot> getRemainingItems(GridInventory grid) {
        return StreamSupport.stream(grid.<Slot>slots().spliterator(), false)
                .map(Slot::peek)
                .map(potentialItem -> potentialItem.flatMap(SpongeImplHooks::getContainerItem))
                .map(potentialItem -> potentialItem.map(ItemStack::createSnapshot).orElse(ItemStackSnapshot.NONE))
                .collect(Collectors.toList());
    }

    @Override
    public ItemStackSnapshot getExemplaryResult() {
        return exemplaryResult;
    }

    @Override
    public Predicate<ItemStackSnapshot> getIngredientPredicate(int x, int y) {
        return Optional.ofNullable(this.ingredients.get(x, y))
                .orElseThrow(() -> new IndexOutOfBoundsException("Invalid ingredient predicate location"));
    }

    @Override
    public int getWidth() {
        return this.recipeWidth;
    }

    @Override
    public int getHeight() {
        return this.recipeHeight;
    }

    @Override
    public int getSize() {
        return this.recipeWidth * this.recipeHeight;
    }

    /*
     * IRecipe
     */

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
