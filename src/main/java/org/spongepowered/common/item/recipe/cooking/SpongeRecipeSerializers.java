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
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.item.recipe.crafting.custom.SpongeSpecialCraftingRecipeRegistration;
import org.spongepowered.common.item.recipe.crafting.shaped.SpongeShapedCraftingRecipeSerializer;
import org.spongepowered.common.item.recipe.crafting.shapeless.SpongeShapelessCraftingRecipeSerializer;
import org.spongepowered.common.item.recipe.smithing.SpongeSmithingRecipeSerializer;
import org.spongepowered.common.item.recipe.stonecutting.SpongeStonecuttingRecipeSerializer;

public interface SpongeRecipeSerializers {

    SpongeCookingRecipeSerializer<BlastingRecipe> SPONGE_BLASTING = register("blasting", new SpongeCookingRecipeSerializer.Blasting());
    SpongeCookingRecipeSerializer<CampfireCookingRecipe> SPONGE_CAMPFIRE_COOKING = register("campfire_cooking", new SpongeCookingRecipeSerializer.Campfire());
    SpongeCookingRecipeSerializer<SmeltingRecipe> SPONGE_SMELTING = register("smelting", new SpongeCookingRecipeSerializer.Smelting());
    RecipeSerializer<?> SPONGE_CRAFTING_SHAPED = register("crafting_shaped", new SpongeShapedCraftingRecipeSerializer());
    RecipeSerializer<?> SPONGE_CRAFTING_SHAPELESS = register("crafting_shapeless", new SpongeShapelessCraftingRecipeSerializer());
    RecipeSerializer<?> SPONGE_SMITHING = register("smithing", new SpongeSmithingRecipeSerializer<>());
    RecipeSerializer<?> SPONGE_STONECUTTING = register("stonecutting", new SpongeStonecuttingRecipeSerializer<>());
    SpongeCookingRecipeSerializer<SmokingRecipe> SPONGE_SMOKING = register("smoking", new SpongeCookingRecipeSerializer.Smoking());

    SimpleCraftingRecipeSerializer<?> SPONGE_SPECIAL = register("special", new SimpleCraftingRecipeSerializer<>(SpongeSpecialCraftingRecipeRegistration::get));

    static <S extends RecipeSerializer<T>, T extends Recipe<?>> S register(final String spongeName, final S recipeSerializer) {
        final Registry<RecipeSerializer<?>> recipeSerializerRegistry = SpongeCommon.vanillaRegistry(Registries.RECIPE_SERIALIZER);
        return (S)(Registry.<RecipeSerializer<?>>register(recipeSerializerRegistry, new ResourceLocation("sponge", spongeName).toString(), recipeSerializer));
    }

    static <S extends RecipeSerializer<T>, T extends Recipe<?>> S register(final ResourceLocation resourceLocation, final S recipeSerializer) {
        final Registry<RecipeSerializer<?>> recipeSerializerRegistry = SpongeCommon.vanillaRegistry(Registries.RECIPE_SERIALIZER);
        return (S)(Registry.<RecipeSerializer<?>>register(recipeSerializerRegistry, resourceLocation.toString(), recipeSerializer));
    }
}
