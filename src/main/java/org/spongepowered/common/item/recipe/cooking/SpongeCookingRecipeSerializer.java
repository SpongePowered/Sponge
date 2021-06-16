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
import org.spongepowered.common.item.recipe.SpongeRecipeRegistration;
import org.spongepowered.common.item.recipe.ingredient.IngredientUtil;
import org.spongepowered.common.item.recipe.ingredient.IngredientResultUtil;
import org.spongepowered.common.util.Constants;

import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;

// Custom Serializer with support for:
// result full ItemStack instead of ItemType+Count
// ingredient itemstacks
public abstract class SpongeCookingRecipeSerializer<R extends AbstractCookingRecipe> implements RecipeSerializer<R> {

    private final int defaultCookingTime;

    public SpongeCookingRecipeSerializer(final int defaultCookingTime) {
        this.defaultCookingTime = defaultCookingTime;
    }

    @Override
    public R fromJson(final ResourceLocation recipeId, final JsonObject json) {
        final String group = GsonHelper.getAsString(json, Constants.Recipe.GROUP, "");
        final JsonElement jsonelement = GsonHelper.isArrayNode(json, Constants.Recipe.COOKING_INGREDIENT) ? GsonHelper.getAsJsonArray(json, Constants.Recipe.COOKING_INGREDIENT) : GsonHelper.getAsJsonObject(json, Constants.Recipe.COOKING_INGREDIENT);
        final Ingredient ingredient = IngredientUtil.spongeDeserialize(jsonelement);
        final String result = GsonHelper.getAsString(json, Constants.Recipe.RESULT);
        final ResourceLocation resourcelocation = new ResourceLocation(result);
        final ItemStack itemstack = new ItemStack(Registry.ITEM.getOptional(resourcelocation).orElseThrow(() -> new IllegalStateException("Item: " + result + " does not exist")));
        final ItemStack spongeStack = IngredientResultUtil.deserializeItemStack(json.getAsJsonObject(Constants.Recipe.SPONGE_RESULT));
        final Function<Container, ItemStack> resultFunction = IngredientResultUtil.deserializeResultFunction(json);
        final float exp = GsonHelper.getAsFloat(json, Constants.Recipe.COOKING_EXP, 0.0F);
        final int cookTime = GsonHelper.getAsInt(json, Constants.Recipe.COOKING_TIME, this.defaultCookingTime);
        return this.create(recipeId, group, ingredient, spongeStack == null ? itemstack : spongeStack, exp, cookTime, resultFunction);
    }

    protected abstract R create(final ResourceLocation id, final String group, final Ingredient ingredient, final ItemStack result,
                                final float experience, final int cookingTime, final Function<Container, ItemStack> resultFunction);

    @Override
    public R fromNetwork(final ResourceLocation recipeId, final FriendlyByteBuf buffer) {
        throw new UnsupportedOperationException("custom serializer needs client side support");
    }

    @Override
    public void toNetwork(final FriendlyByteBuf buffer, final R recipe) {
        throw new UnsupportedOperationException("custom serializer needs client side support");
    }

    public static class Smelting extends SpongeCookingRecipeSerializer<SmeltingRecipe> {

        public static final SpongeCookingRecipeSerializer<SmeltingRecipe> SPONGE_SMELTING = SpongeRecipeRegistration.register("smelting", new Smelting());

        public Smelting() {
            super(200);
        }

        @Override
        protected SmeltingRecipe create(final ResourceLocation id, final String group, final Ingredient ingredient, final ItemStack result,
                                       final float experience, final int cookingTime, final Function<Container, ItemStack> resultFunction) {
            return new SpongeFurnaceRecipe(id, group, ingredient, result, experience, cookingTime, resultFunction);
        }
    }

    public static class Blasting extends SpongeCookingRecipeSerializer<BlastingRecipe> {

        public static final SpongeCookingRecipeSerializer<BlastingRecipe> SPONGE_BLASTING = SpongeRecipeRegistration.register("blasting", new Blasting());

        public Blasting() {
            super(100);
        }

        @Override
        protected BlastingRecipe create(final ResourceLocation id, final String group, final Ingredient ingredient, final ItemStack result,
                                        final float experience, final int cookingTime, final Function<Container, ItemStack> resultFunction) {
            return new SpongeBlastingRecipe(id, group, ingredient, result, experience, cookingTime, resultFunction);
        }
    }

    public static class Smoking extends SpongeCookingRecipeSerializer<SmokingRecipe> {

        public static final SpongeCookingRecipeSerializer<SmokingRecipe> SPONGE_SMOKING = SpongeRecipeRegistration.register("smoking", new Smoking());

        public Smoking() {
            super(100);
        }

        @Override
        protected SmokingRecipe create(final ResourceLocation id, final String group, final Ingredient ingredient, final ItemStack result,
                                       final float experience, final int cookingTime, final Function<Container, ItemStack> resultFunction) {
            return new SpongeSmokingRecipe(id, group, ingredient, result, experience, cookingTime, resultFunction);
        }
    }

    public static class Campfire extends SpongeCookingRecipeSerializer<CampfireCookingRecipe> {

        public static final SpongeCookingRecipeSerializer<CampfireCookingRecipe> SPONGE_CAMPFIRE_COOKING = SpongeRecipeRegistration.register("campfire_cooking", new Campfire());

        public Campfire() {
            super(100);
        }

        @Override
        protected CampfireCookingRecipe create(final ResourceLocation id, final String group, final Ingredient ingredient, final ItemStack result,
                                               final float experience, final int cookingTime, final Function<Container, ItemStack> resultFunction) {
            return new SpongeCampfireCookingRecipe(id, group, ingredient, result, experience, cookingTime, resultFunction);
        }
    }

}
