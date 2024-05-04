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

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.datapack.DataPack;
import org.spongepowered.api.item.recipe.Recipe;
import org.spongepowered.api.item.recipe.RecipeRegistration;
import org.spongepowered.api.item.recipe.cooking.CookingRecipe;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.common.item.recipe.SpongeRecipeRegistration;
import org.spongepowered.common.util.SpongeTicks;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class SpongeCookingRecipeRegistration extends SpongeRecipeRegistration<AbstractCookingRecipe> implements SpongeRecipeRegistration.ResultFunctionRegistration<Container>{

    // Vanilla Recipe
    private final Ingredient ingredient;
    private final float experience;
    private final Ticks cookingTime;

    // Sponge Recipe
    private final ItemStack spongeResult;
    private final Function<Container, ItemStack> resultFunction;
    private final RecipeType<?> type;
    private final CookingBookCategory cookingCategory;

    public SpongeCookingRecipeRegistration(final ResourceLocation key, final RecipeType<?> type, final RecipeSerializer<? extends AbstractCookingRecipe> serializer,
                                           final String group, final Ingredient ingredient, final float experience, final Ticks cookingTime,
                                           final ItemStack spongeResult, final Function<Container, ItemStack> resultFunction,
                                           final DataPack<RecipeRegistration> pack, final RecipeCategory category, final CookingBookCategory cookingCategory) {
        super(key, group, pack, category, serializer);
        this.type = type;
        this.ingredient = ingredient;
        this.experience = experience;
        this.cookingTime = cookingTime;
        this.spongeResult = spongeResult;
        this.resultFunction = resultFunction;
        this.cookingCategory = cookingCategory;
    }

    public static SpongeCookingRecipeRegistration of(final ResourceLocation key, final RecipeType<?> type, final @Nullable String group,
            final Ingredient ingredient, final Float experience, final @Nullable Ticks cookingTime, final ItemStack result,
            final Function<Container, ItemStack> resultFunction, final DataPack<RecipeRegistration> pack, final RecipeCategory recipeCategory, final CookingBookCategory cookingCategory)
    {
        var finalCookingTime = cookingTime;

        final RecipeSerializer<? extends AbstractCookingRecipe> serializer;
        if (type == RecipeType.BLASTING) {
            if (finalCookingTime == null) {
                finalCookingTime = Ticks.of(100);
            }
            serializer = RecipeSerializer.BLASTING_RECIPE;
        } else if (type == RecipeType.CAMPFIRE_COOKING) {
            if (finalCookingTime == null) {
                finalCookingTime = Ticks.of(600);
            }
            serializer = RecipeSerializer.CAMPFIRE_COOKING_RECIPE;
        } else if (type == RecipeType.SMOKING) {
            if (finalCookingTime == null) {
                finalCookingTime = Ticks.of(100);
            }
            serializer = RecipeSerializer.SMOKING_RECIPE;
        } else if (type == RecipeType.SMELTING) {
            if (finalCookingTime == null) {
                finalCookingTime = Ticks.of(200);
            }
            serializer = RecipeSerializer.SMELTING_RECIPE;
        } else {
            throw new IllegalArgumentException("Unknown RecipeType " + type);
        }

        return new SpongeCookingRecipeRegistration(key, type, serializer, group,
                ingredient, experience == null ? 0 : experience, finalCookingTime, result, resultFunction, pack, recipeCategory, cookingCategory);
    }

    @Override
    public Recipe recipe() {
        this.ensureCached();
        final int ticksCookingTime = SpongeTicks.toSaturatedIntOrInfinite(this.cookingTime);
        final String resultFunctionId = this.resultFunction == null ? null : this.key.toString();

        final List<Ingredient> ingredientList = Collections.singletonList(ingredient);
        final boolean isVanilla = SpongeRecipeRegistration.isVanillaSerializer(this.spongeResult, this.resultFunction, null, ingredientList);

        if (type == RecipeType.BLASTING) {
            if (!isVanilla) {
                return (CookingRecipe) new SpongeBlastingRecipe(this.group, this.cookingCategory, this.ingredient, this.spongeResult, this.experience, ticksCookingTime, resultFunctionId);
            }
            return (CookingRecipe) new BlastingRecipe(this.group, this.cookingCategory, this.ingredient, this.spongeResult, this.experience, ticksCookingTime);
        }
        if (type == RecipeType.CAMPFIRE_COOKING) {
            if (!isVanilla) {
                return (CookingRecipe) new SpongeCampfireCookingRecipe(this.group, this.cookingCategory, this.ingredient, this.spongeResult, this.experience, ticksCookingTime, resultFunctionId);
            }
            return (CookingRecipe) new CampfireCookingRecipe(this.group, this.cookingCategory, this.ingredient, this.spongeResult, this.experience, ticksCookingTime);
        }
        if (type == RecipeType.SMOKING) {
            if (!isVanilla) {
                return (CookingRecipe) new SpongeSmokingRecipe(this.group, this.cookingCategory, this.ingredient, this.spongeResult, this.experience, ticksCookingTime, resultFunctionId);
            }
            return (CookingRecipe) new SmokingRecipe(this.group, this.cookingCategory, this.ingredient, this.spongeResult, this.experience, ticksCookingTime);
        }
        if (type == RecipeType.SMELTING) {
            if (!isVanilla) {
                return (CookingRecipe) new SpongeSmeltingRecipe(this.group, this.cookingCategory, this.ingredient, this.spongeResult, this.experience, ticksCookingTime, resultFunctionId);
            }
            return (CookingRecipe) new SmeltingRecipe(this.group, this.cookingCategory, this.ingredient, this.spongeResult, this.experience, ticksCookingTime);
        }
        throw new IllegalArgumentException("Unknown RecipeType " + type);


    }

    @Override
    public Function<Container, ItemStack> resultFunction() {
        return this.resultFunction;
    }
}
