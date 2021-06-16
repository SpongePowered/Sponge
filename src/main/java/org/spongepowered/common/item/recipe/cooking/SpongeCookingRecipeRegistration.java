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

import com.google.gson.JsonObject;
import org.spongepowered.common.item.recipe.SpongeRecipeRegistration;
import org.spongepowered.common.item.recipe.ingredient.IngredientResultUtil;
import org.spongepowered.common.util.Constants;

import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class SpongeCookingRecipeRegistration extends SpongeRecipeRegistration {

    // Vanilla Recipe
    private final Ingredient ingredient;
    private final Item result;
    private final float experience;
    private final int cookingTime;

    // Sponge Recipe
    private final ItemStack spongeResult;
    private final Function<Container, ItemStack> resultFunction;

    public SpongeCookingRecipeRegistration(final ResourceLocation key, final RecipeSerializer<?> serializer,
                                           final String group, final Ingredient ingredient, final float experience, final int cookingTime,
                                           final ItemStack spongeResult, final Function<Container, ItemStack> resultFunction) {
        super(key, serializer, spongeResult.getItem(), group);
        this.ingredient = ingredient;
        this.result = spongeResult.getItem();
        this.experience = experience;
        this.cookingTime = cookingTime;
        this.spongeResult = spongeResult.hasTag() ? spongeResult : null;
        this.resultFunction = resultFunction;
    }

    @Override
    public void serializeShape(final JsonObject json) {
        json.add(Constants.Recipe.COOKING_INGREDIENT, this.ingredient.toJson());
    }

    @Override
    public void serializeResult(final JsonObject json) {
        json.addProperty(Constants.Recipe.RESULT, Registry.ITEM.getKey(this.result).toString());
        // Sponge Recipe
        if (this.spongeResult != null) {
            this.spongeResult.setCount(1);
            json.add(Constants.Recipe.SPONGE_RESULT, IngredientResultUtil.serializeItemStack(this.spongeResult));
        }
        if (this.resultFunction != null) {
            json.addProperty(Constants.Recipe.SPONGE_RESULTFUNCTION, IngredientResultUtil.cacheResultFunction(this.getId(), this.resultFunction));
        }
    }

    @Override
    public void serializeAdditional(final JsonObject json) {
        json.addProperty(Constants.Recipe.COOKING_EXP, this.experience);
        json.addProperty(Constants.Recipe.COOKING_TIME, this.cookingTime);
    }
}
