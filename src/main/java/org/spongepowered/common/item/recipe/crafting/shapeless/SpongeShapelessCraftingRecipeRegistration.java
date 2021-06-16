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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.spongepowered.common.item.recipe.SpongeRecipeRegistration;
import org.spongepowered.common.item.recipe.ingredient.IngredientResultUtil;
import org.spongepowered.common.util.Constants;

import java.util.List;
import java.util.function.Function;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class SpongeShapelessCraftingRecipeRegistration extends SpongeRecipeRegistration {
    // Vanilla Recipe
    private final Item result;
    private final int count;
    private final List<Ingredient> ingredients;

    // Sponge Recipe
    private final ItemStack spongeResult;
    private final Function<CraftingContainer, ItemStack> resultFunction;
    private final Function<CraftingContainer, NonNullList<ItemStack>> remainingItemsFunction;

    public SpongeShapelessCraftingRecipeRegistration(ResourceLocation key, RecipeSerializer<?> serializer, String group,
            List<Ingredient> ingredients, ItemStack spongeResult, Function<CraftingContainer, ItemStack> resultFunction,
            Function<CraftingContainer, NonNullList<ItemStack>> remainingItemsFunction) {
        super(key, serializer, spongeResult.getItem(), group);
        this.ingredients = ingredients;

        this.count = spongeResult.getCount();
        this.result = spongeResult.getItem();
        this.spongeResult = spongeResult.hasTag() ? spongeResult : null;
        this.resultFunction = resultFunction;
        this.remainingItemsFunction = remainingItemsFunction;
    }

    @Override
    public void serializeShape(JsonObject json) {
        final JsonArray jsonarray = new JsonArray();
        this.ingredients.stream().map(Ingredient::toJson).forEach(jsonarray::add);
        json.add(Constants.Recipe.SHAPELESS_INGREDIENTS, jsonarray);
    }

    @Override
    public void serializeResult(JsonObject json) {
        final JsonObject result = new JsonObject();
        result.addProperty(Constants.Recipe.ITEM, Registry.ITEM.getKey(this.result).toString());
        if (this.count > 1) {
            result.addProperty(Constants.Recipe.COUNT, this.count);
        }

        json.add(Constants.Recipe.RESULT, result);

        if (this.spongeResult != null) {
            json.add(Constants.Recipe.SPONGE_RESULT, IngredientResultUtil.serializeItemStack(this.spongeResult));
        }
        if (this.resultFunction != null) {
            json.addProperty(Constants.Recipe.SPONGE_RESULTFUNCTION, IngredientResultUtil.cacheResultFunction(this.getId(), this.resultFunction));
        }
        if (this.remainingItemsFunction != null) {
            json.addProperty(Constants.Recipe.SPONGE_REMAINING_ITEMS, IngredientResultUtil.cacheRemainingItemsFunction(this.getId(), this.remainingItemsFunction));
        }
    }
}
