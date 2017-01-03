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
package org.spongepowered.common.mixin.core.item.recipe;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.item.crafting.ShapedRecipes;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.recipe.RecipeRegistry;
import org.spongepowered.api.item.recipe.crafting.ShapedCraftingRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.interfaces.item.crafting.IMixinShapedRecipes;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

@Mixin(ShapedRecipes.class)
public class MixinShapedRecipes implements ShapedCraftingRecipe, IMixinShapedRecipes {

    @Shadow @Final private int recipeWidth;
    @Shadow @Final private int recipeHeight;
    @Shadow @Final private net.minecraft.item.ItemStack[] recipeItems;
    @Shadow @Final private net.minecraft.item.ItemStack recipeOutput;
    private ImmutableList<String> shape;
    private Map<Character, net.minecraft.item.ItemStack> ingredientMap;

    @Override
    public ImmutableCollection<String> getShape() {
        return shape;
    }

    @Override
    public ImmutableMap<Character, ItemStackSnapshot> getIngredients() {
        ImmutableMap.Builder<Character, ItemStackSnapshot> builder = ImmutableMap.builder();
        for (Entry<Character, net.minecraft.item.ItemStack> entry : ingredientMap.entrySet()) {
            builder.put(entry.getKey(), ItemStackUtil.snapshotOf(entry.getValue()));
        }
        return builder.build();
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
    public Optional<ItemStackSnapshot> getIngredient(char symbol) {
        return Optional.ofNullable(ItemStackUtil.snapshotOf(this.ingredientMap.get(symbol)));
    }

    @Override
    public ImmutableCollection<ItemStackSnapshot> getResults() {
        return ImmutableSet.of(ItemStackUtil.snapshotOf(recipeOutput));
    }

    @Override
    public void setShape(List<String> shape) {
        this.shape = ImmutableList.copyOf(shape);
    }

    @Override
    public void setIngredientMap(Map<Character, net.minecraft.item.ItemStack> map) {
        this.ingredientMap = map;
    }

    @Override
    public RecipeRegistry<?> getRegistry() {
        return SpongeImpl.getRegistry().getCraftingRegistry();
    }

}
