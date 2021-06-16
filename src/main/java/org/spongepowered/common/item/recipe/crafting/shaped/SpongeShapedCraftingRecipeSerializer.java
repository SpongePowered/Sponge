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
package org.spongepowered.common.item.recipe.crafting.shaped;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.spongepowered.common.accessor.world.item.crafting.ShapedRecipeAccessor;
import org.spongepowered.common.item.recipe.SpongeRecipeRegistration;
import org.spongepowered.common.item.recipe.ingredient.IngredientUtil;
import org.spongepowered.common.item.recipe.ingredient.IngredientResultUtil;
import org.spongepowered.common.util.Constants;

import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

/**
 * Custom ShapedRecipe.Serializer with support for:
 * result full ItemStack instead of ItemType+Count
 * result functions
 * ingredient itemstacks
 * remaining items function
 */
public class SpongeShapedCraftingRecipeSerializer extends ShapedRecipe.Serializer {

    public static RecipeSerializer<?> SPONGE_CRAFTING_SHAPED = SpongeRecipeRegistration.register("crafting_shaped", new SpongeShapedCraftingRecipeSerializer());

    @Override
    public ShapedRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        final String s = GsonHelper.getAsString(json, Constants.Recipe.GROUP, "");
        final JsonObject ingredientKey = GsonHelper.getAsJsonObject(json, Constants.Recipe.SHAPED_INGREDIENTS);
        final Map<String, Ingredient> map = this.deserializeIngredientKey(ingredientKey);
        final String[] astring = ShapedRecipeAccessor.invoker$shrink(ShapedRecipeAccessor.invoker$patternFromJson(GsonHelper.getAsJsonArray(json, Constants.Recipe.SHAPED_PATTERN)));
        final int i = astring[0].length();
        final int j = astring.length;
        final NonNullList<Ingredient> nonnulllist = ShapedRecipeAccessor.invoker$dissolvePattern(astring, map, i, j);
        final ItemStack itemstack = ShapedRecipe.itemFromJson(GsonHelper.getAsJsonObject(json, Constants.Recipe.RESULT));
        final ItemStack spongeStack = IngredientResultUtil.deserializeItemStack(json.getAsJsonObject(Constants.Recipe.SPONGE_RESULT));
        final Function<CraftingContainer, ItemStack> resultFunction = IngredientResultUtil.deserializeResultFunction(json);
        final Function<CraftingContainer, NonNullList<ItemStack>> remainingItemsFunction = IngredientResultUtil.deserializeRemainingItemsFunction(json);
        return new SpongeShapedRecipe(recipeId, s, i, j, nonnulllist, spongeStack == null ? itemstack : spongeStack, resultFunction, remainingItemsFunction);
    }

    public Map<String, Ingredient> deserializeIngredientKey(JsonObject json) {
        final Map<String, Ingredient> map = Maps.newHashMap();

        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            if (entry.getKey().length() != 1) {
                throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey() + "' is an invalid symbol (must be 1 character only).");
            }

            if (" ".equals(entry.getKey())) {
                throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
            }

            map.put(entry.getKey(), IngredientUtil.spongeDeserialize(entry.getValue()));
        }

        map.put(" ", Ingredient.EMPTY);
        return map;
    }

    @Override
    public ShapedRecipe fromNetwork(ResourceLocation p_199426_1_, FriendlyByteBuf p_199426_2_) {
        throw new UnsupportedOperationException("custom serializer needs client side support");
    }

    @Override
    public void toNetwork(FriendlyByteBuf p_199427_1_, ShapedRecipe p_199427_2_) {
        throw new UnsupportedOperationException("custom serializer needs client side support");
    }
}
