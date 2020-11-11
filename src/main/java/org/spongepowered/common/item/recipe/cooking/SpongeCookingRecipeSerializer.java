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
package org.spongepowered.common.item.recipe.cooking;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.AbstractCookingRecipe;
import net.minecraft.item.crafting.BlastingRecipe;
import net.minecraft.item.crafting.CampfireCookingRecipe;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.SmokingRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import org.spongepowered.common.item.recipe.SpongeRecipeRegistration;
import org.spongepowered.common.item.recipe.ingredient.IngredientUtil;
import org.spongepowered.common.item.recipe.ingredient.ResultUtil;
import org.spongepowered.common.util.Constants;

import java.util.function.Function;

// Custom Serializer with support for:
// result full ItemStack instead of ItemType+Count
// ingredient itemstacks
public abstract class SpongeCookingRecipeSerializer<R extends AbstractCookingRecipe> implements IRecipeSerializer<R> {

    private final int defaultCookingTime;

    public SpongeCookingRecipeSerializer(int p_i50025_2_) {
        this.defaultCookingTime = p_i50025_2_;
    }

    public R read(ResourceLocation recipeId, JsonObject json) {
        final String group = JSONUtils.getString(json, Constants.Recipe.GROUP, "");
        final JsonElement jsonelement = JSONUtils.isJsonArray(json, Constants.Recipe.COOKING_INGREDIENT) ? JSONUtils.getJsonArray(json, Constants.Recipe.COOKING_INGREDIENT) : JSONUtils.getJsonObject(json, Constants.Recipe.COOKING_INGREDIENT);
        final Ingredient ingredient = IngredientUtil.spongeDeserialize(jsonelement);
        final String result = JSONUtils.getString(json, Constants.Recipe.RESULT);
        final ResourceLocation resourcelocation = new ResourceLocation(result);
        final ItemStack itemstack = new ItemStack(Registry.ITEM.getValue(resourcelocation).orElseThrow(() -> new IllegalStateException("Item: " + result + " does not exist")));
        final ItemStack spongeStack = ResultUtil.deserializeItemStack(json.getAsJsonObject(Constants.Recipe.SPONGE_RESULT));
        final Function<IInventory, ItemStack> resultFunction = ResultUtil.deserializeResultFunction(json);
        final float exp = JSONUtils.getFloat(json, Constants.Recipe.COOKING_EXP, 0.0F);
        final int cookTime = JSONUtils.getInt(json, Constants.Recipe.COOKING_TIME, this.defaultCookingTime);
        return this.create(recipeId, group, ingredient, spongeStack == null ? itemstack : spongeStack, resultFunction, exp, cookTime);
    }

    protected abstract R create(ResourceLocation recipeId, String group, Ingredient ingredient, ItemStack resultStack,
            Function<IInventory, ItemStack> resultFunction, float experience, int cookingTime);

    public R read(ResourceLocation recipeId, PacketBuffer buffer) {
        throw new UnsupportedOperationException("custom serializer needs client side support");
    }

    public void write(PacketBuffer buffer, R recipe) {
        throw new UnsupportedOperationException("custom serializer needs client side support");
    }

    public static class Smelting extends SpongeCookingRecipeSerializer<FurnaceRecipe> {

        public static SpongeCookingRecipeSerializer<FurnaceRecipe> SPONGE_SMELTING = SpongeRecipeRegistration.register("smelting", new Smelting());

        public Smelting() {
            super(200);
        }

        @Override
        protected FurnaceRecipe create(ResourceLocation recipeId, String group, Ingredient ingredient, ItemStack resultStack,
                Function<IInventory, ItemStack> resultFunction, float experience, int cookingTime) {
            return new SpongeFurnaceRecipe(recipeId, group, ingredient, resultStack, experience, cookingTime, resultFunction);
        }
    }

    public static class Blasting extends SpongeCookingRecipeSerializer<BlastingRecipe> {

        public static SpongeCookingRecipeSerializer<BlastingRecipe> SPONGE_BLASTING = SpongeRecipeRegistration.register("blasting", new Blasting());

        public Blasting() {
            super(100);
        }

        @Override
        protected BlastingRecipe create(ResourceLocation recipeId, String group, Ingredient ingredient, ItemStack resultStack,
                Function<IInventory, ItemStack> resultFunction, float experience, int cookingTime) {
            return new SpongeBlastingRecipe(recipeId, group, ingredient, resultStack, experience, cookingTime, resultFunction);
        }
    }

    public static class Smoking extends SpongeCookingRecipeSerializer<SmokingRecipe> {

        public static SpongeCookingRecipeSerializer<SmokingRecipe> SPONGE_SMOKING = SpongeRecipeRegistration.register("smoking", new Smoking());

        public Smoking() {
            super(100);
        }

        @Override
        protected SmokingRecipe create(ResourceLocation recipeId, String group, Ingredient ingredient, ItemStack resultStack,
                Function<IInventory, ItemStack> resultFunction, float experience, int cookingTime) {
            return new SpongeSmokingRecipe(recipeId, group, ingredient, resultStack, experience, cookingTime, resultFunction);
        }
    }

    public static class Campfire extends SpongeCookingRecipeSerializer<CampfireCookingRecipe> {

        public static SpongeCookingRecipeSerializer<CampfireCookingRecipe> SPONGE_CAMPFIRE_COOKING = SpongeRecipeRegistration.register("campfire_cooking", new Campfire());

        public Campfire() {
            super(100);
        }

        @Override
        protected CampfireCookingRecipe create(ResourceLocation recipeId, String group, Ingredient ingredient, ItemStack resultStack,
                Function<IInventory, ItemStack> resultFunction, float experience, int cookingTime) {
            return new SpongeCampfireCookingRecipe(recipeId, group, ingredient, resultStack, experience, cookingTime, resultFunction);
        }
    }

}
