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
package org.spongepowered.common.mixin.core.item.crafting;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.item.crafting.ShapedRecipes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.api.item.recipe.crafting.ShapedCraftingRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.interfaces.item.crafting.IMixinShapedRecipes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mixin(ShapedRecipes.class)
public class MixinShapedRecipes implements ShapedCraftingRecipe, IMixinShapedRecipes {

    @Shadow @Final private int recipeWidth;
    @Shadow @Final private int recipeHeight;
    @Shadow @Final private net.minecraft.item.ItemStack[] recipeItems;
    @Shadow @Final private net.minecraft.item.ItemStack recipeOutput;
    private List<String> aisle;
    private Map<Character, net.minecraft.item.ItemStack> ingredientMap;

    @Override
    public List<String> getAisle() {
        return this.aisle;
    }

    @Override
    public Map<Character, ItemStack> getIngredients() {
        return ImmutableMap.copyOf((Map<Character, ItemStack>) (Object) this.ingredientMap);
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
    public Optional<ItemStack> getIngredient(char symbol) {
        return Optional.ofNullable((ItemStack) this.ingredientMap.get(symbol));
    }

    @Override
    public List<ItemStack> getResults() {
        // todo
        return null;
    }

    @Override
    public boolean test(GridInventory grid) {
        // todo
        return false;
    }

    @Override
    public List<ItemStack> getResults(GridInventory grid) {
        // todo
        return null;
    }

    @Override
    public void setAisle(List<String> aisle) {
        this.aisle = ImmutableList.copyOf(aisle);
    }

    @Override
    public void setIngredientMap(Map<Character, net.minecraft.item.ItemStack> map) {
        this.ingredientMap = map;
    }

}
