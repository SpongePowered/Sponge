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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.spongepowered.common.item.recipe.ingredient.IngredientResultUtil;
import org.spongepowered.common.item.recipe.ingredient.IngredientUtil;
import org.spongepowered.common.util.Constants;

import java.util.Objects;
import java.util.function.Function;

/**
 * Custom ShapelessRecipe.Serializer with support for:
 * result full ItemStack instead of ItemType+Count
 * result functions
 * ingredient ItemStacks
 * remaining items function
 */
public class SpongeShapelessCraftingRecipeSerializer extends ShapelessRecipe.Serializer {

    @Override
    public ShapelessRecipe fromJson(final ResourceLocation recipeId, final JsonObject json) {
        final String s = GsonHelper.getAsString(json, Constants.Recipe.GROUP, "");
        final CraftingBookCategory category = Objects.requireNonNullElse(CraftingBookCategory.CODEC.byName(GsonHelper.getAsString(json, Constants.Recipe.CATEGORY, null)), CraftingBookCategory.MISC);
        final NonNullList<Ingredient> nonnulllist = this.readIngredients(GsonHelper.getAsJsonArray(json, Constants.Recipe.SHAPELESS_INGREDIENTS));
        if (nonnulllist.isEmpty()) {
            throw new JsonParseException("No ingredients for shapeless recipe");
        }
        if (nonnulllist.size() > 9) {
            throw new JsonParseException("Too many ingredients for shapeless recipe");
        }
        final ItemStack itemstack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, Constants.Recipe.RESULT));
        final ItemStack spongeStack = IngredientResultUtil.deserializeItemStack(json.getAsJsonObject(Constants.Recipe.SPONGE_RESULT));
        final Function<CraftingContainer, ItemStack> resultFunction = IngredientResultUtil.deserializeResultFunction(json);
        final Function<CraftingContainer, NonNullList<ItemStack>> remainingItemsFunction = IngredientResultUtil.deserializeRemainingItemsFunction(json);
        return new SpongeShapelessRecipe(recipeId, s, category, spongeStack == null ? itemstack : spongeStack, nonnulllist, resultFunction, remainingItemsFunction);
    }

    private NonNullList<Ingredient> readIngredients(final JsonArray json) {
        final NonNullList<Ingredient> nonnulllist = NonNullList.create();
        for (final JsonElement element : json) {
            final Ingredient ingredient = IngredientUtil.spongeDeserialize(element);
            if (!ingredient.isEmpty()) {
                nonnulllist.add(ingredient);
            }
        }
        return nonnulllist;
    }

    @Override
    public ShapelessRecipe fromNetwork(final ResourceLocation p_199426_1_, final FriendlyByteBuf p_199426_2_) {
        throw new UnsupportedOperationException("custom serializer needs client side support");
    }

    @Override
    public void toNetwork(final FriendlyByteBuf p_199427_1_, final ShapelessRecipe p_199427_2_) {
        throw new UnsupportedOperationException("custom serializer needs client side support");
    }
}
