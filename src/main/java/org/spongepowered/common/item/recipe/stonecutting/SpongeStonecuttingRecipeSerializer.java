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
package org.spongepowered.common.item.recipe.stonecutting;

import com.google.gson.JsonObject;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.SingleItemRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import org.spongepowered.common.item.recipe.SpongeRecipeRegistration;
import org.spongepowered.common.item.recipe.ingredient.IngredientUtil;
import org.spongepowered.common.item.recipe.ingredient.ResultUtil;
import org.spongepowered.common.util.Constants;

import java.util.function.Function;

public class SpongeStonecuttingRecipeSerializer<R extends SingleItemRecipe> implements IRecipeSerializer<R> {

    public static IRecipeSerializer<?> SPONGE_STONECUTTING = SpongeRecipeRegistration.register("stonecutting", new SpongeStonecuttingRecipeSerializer<>());

    @SuppressWarnings("unchecked")
    public R read(ResourceLocation recipeId, JsonObject json) {
        final String group = JSONUtils.getString(json, Constants.Recipe.GROUP, "");
        final Ingredient ingredient = IngredientUtil.spongeDeserialize(json.get(Constants.Recipe.STONECUTTING_INGREDIENT));

        final Function<IInventory, ItemStack> resultFunction = ResultUtil.deserializeResultFunction(json);
        final ItemStack spongeStack = ResultUtil.deserializeItemStack(json.getAsJsonObject(Constants.Recipe.SPONGE_RESULT));
        if (spongeStack != null) {
            return (R) new SpongeStonecuttingRecipe(recipeId, group, ingredient, spongeStack, resultFunction);
        }

        final String type = JSONUtils.getString(json, Constants.Recipe.RESULT);
        final int count = JSONUtils.getInt(json, Constants.Recipe.COUNT);
        final ItemStack itemstack = new ItemStack(Registry.ITEM.getOrDefault(new ResourceLocation(type)), count);
        return (R) new SpongeStonecuttingRecipe(recipeId, group, ingredient, itemstack, resultFunction);
    }

    @SuppressWarnings("unchecked")
    public R read(ResourceLocation recipeId, PacketBuffer buffer) {
        throw new UnsupportedOperationException("custom serializer needs client side support");
    }

    public void write(PacketBuffer buffer, R recipe) {
        throw new UnsupportedOperationException("custom serializer needs client side support");
    }
}
