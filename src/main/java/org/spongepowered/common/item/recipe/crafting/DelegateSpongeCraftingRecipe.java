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

import com.google.common.base.Preconditions;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.crafting.CraftingGridInventory;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;

public class DelegateSpongeCraftingRecipe extends AbstractSpongeCraftingRecipe {

    private final CraftingRecipe recipe;
    private final String id;

    public DelegateSpongeCraftingRecipe(CraftingRecipe recipe) {
        Preconditions.checkNotNull(recipe, "recipe");

        this.recipe = recipe;
        this.id = recipe.getId();
    }

    public CraftingRecipe getDelegate() {
        return this.recipe;
    }

    @Override
    public ItemStackSnapshot getExemplaryResult() {
        return this.recipe.getExemplaryResult();
    }

    @Override
    public boolean isValid(CraftingGridInventory grid, World world) {
        return this.recipe.isValid(grid, world);
    }

    @Override
    public ItemStackSnapshot getResult(CraftingGridInventory grid) {
        return this.recipe.getResult(grid);
    }

    @Override
    public List<ItemStackSnapshot> getRemainingItems(CraftingGridInventory grid) {
        return this.recipe.getRemainingItems(grid);
    }

    @Override
    public Optional<String> getGroup() {
        return this.recipe.getGroup();
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.recipe.getName();
    }

    @Override
    public boolean isDynamic() {
        return true; // For RecipeBook
    }
}
