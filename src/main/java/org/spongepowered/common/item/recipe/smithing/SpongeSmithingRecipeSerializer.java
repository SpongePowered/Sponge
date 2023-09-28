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
package org.spongepowered.common.item.recipe.smithing;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import org.spongepowered.common.item.recipe.ingredient.IngredientResultUtil;
import org.spongepowered.common.item.recipe.ingredient.IngredientUtil;
import org.spongepowered.common.util.Constants;

import java.util.function.Function;

public class SpongeSmithingRecipeSerializer<R extends SmithingTransformRecipe> implements RecipeSerializer<R> {

    @SuppressWarnings("unchecked")
    @Override
    public R fromJson(final ResourceLocation recipeId, final JsonObject json) {
        final Ingredient template = IngredientUtil.spongeDeserialize(json.get(Constants.Recipe.SMITHING_TEMPLATE_INGREDIENT));
        final Ingredient base = IngredientUtil.spongeDeserialize(json.get(Constants.Recipe.SMITHING_BASE_INGREDIENT));
        final Ingredient addition = IngredientUtil.spongeDeserialize(json.get(Constants.Recipe.SMITHING_ADDITION_INGREDIENT));

        final Function<Container, ItemStack> resultFunction = IngredientResultUtil.deserializeResultFunction(json);

        final ItemStack itemstack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, Constants.Recipe.RESULT));
        final ItemStack spongeStack = IngredientResultUtil.deserializeItemStack(json.getAsJsonObject(Constants.Recipe.SPONGE_RESULT));

        return (R) new SpongeSmithingRecipe(recipeId, template, base, addition, spongeStack == null ? itemstack : spongeStack, resultFunction);
    }

    @Override
    public R fromNetwork(final FriendlyByteBuf var1) {
        throw new UnsupportedOperationException("custom serializer needs client side support");
    }

    @Override
    public void toNetwork(final FriendlyByteBuf buffer, final R recipe) {
        throw new UnsupportedOperationException("custom serializer needs client side support");
    }
}
