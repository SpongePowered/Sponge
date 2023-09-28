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

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.spongepowered.common.item.recipe.crafting.custom.SpongeSpecialCraftingRecipeRegistration;
import org.spongepowered.common.item.recipe.crafting.custom.SpongeSpecialCraftingRecipeSerializer;
import org.spongepowered.common.item.recipe.crafting.custom.SpongeSpecialRecipe;
import org.spongepowered.common.item.recipe.crafting.shaped.SpongeShapedCraftingRecipeSerializer;
import org.spongepowered.common.item.recipe.crafting.shapeless.SpongeShapelessCraftingRecipeSerializer;
import org.spongepowered.common.item.recipe.smithing.SpongeSmithingRecipe;
import org.spongepowered.common.item.recipe.smithing.SpongeSmithingRecipeSerializer;
import org.spongepowered.common.item.recipe.stonecutting.SpongeStonecuttingRecipe;
import org.spongepowered.common.item.recipe.stonecutting.SpongeStonecuttingRecipeSerializer;

public interface SpongeRecipeSerializers {

    SpongeCookingRecipeSerializer<SpongeBlastingRecipe> SPONGE_BLASTING = register("blasting", new SpongeCookingRecipeSerializer<>(SpongeBlastingRecipe::new, 100));
    SpongeCookingRecipeSerializer<SpongeCampfireCookingRecipe> SPONGE_CAMPFIRE_COOKING = register("campfire_cooking", new SpongeCookingRecipeSerializer<>(SpongeCampfireCookingRecipe::new, 100));
    SpongeCookingRecipeSerializer<SpongeFurnaceRecipe> SPONGE_SMELTING = register("smelting", new SpongeCookingRecipeSerializer<>(SpongeFurnaceRecipe::new, 200));
    RecipeSerializer<?> SPONGE_CRAFTING_SHAPED = register("crafting_shaped", new SpongeShapedCraftingRecipeSerializer());
    RecipeSerializer<?> SPONGE_CRAFTING_SHAPELESS = register("crafting_shapeless", new SpongeShapelessCraftingRecipeSerializer());
    RecipeSerializer<?> SPONGE_SMITHING = register("smithing", new SpongeSmithingRecipeSerializer<>(SpongeSmithingRecipe::new));
    RecipeSerializer<?> SPONGE_STONECUTTING = register("stonecutting", new SpongeStonecuttingRecipeSerializer<>(SpongeStonecuttingRecipe::new));
    SpongeCookingRecipeSerializer<SpongeSmokingRecipe> SPONGE_SMOKING = register("smoking", new SpongeCookingRecipeSerializer<>(SpongeSmokingRecipe::new, 100));

    SpongeSpecialCraftingRecipeSerializer<SpongeSpecialRecipe> SPONGE_SPECIAL = register("special", new SpongeSpecialCraftingRecipeSerializer<>(SpongeSpecialCraftingRecipeRegistration::get));

    static <S extends RecipeSerializer<T>, T extends Recipe<?>> S register(final String spongeName, final S recipeSerializer) {
        return (S)(Registry.<RecipeSerializer<?>>register(BuiltInRegistries.RECIPE_SERIALIZER, new ResourceLocation("sponge", spongeName).toString(), recipeSerializer));
    }

    static <S extends RecipeSerializer<T>, T extends Recipe<?>> S register(final ResourceLocation resourceLocation, final S recipeSerializer) {
        return (S)(Registry.<RecipeSerializer<?>>register(BuiltInRegistries.RECIPE_SERIALIZER, resourceLocation.toString(), recipeSerializer));
    }
}
