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

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import org.spongepowered.api.datapack.DataPack;
import org.spongepowered.api.item.recipe.Recipe;
import org.spongepowered.api.item.recipe.RecipeRegistration;
import org.spongepowered.api.item.recipe.single.StoneCutterRecipe;
import org.spongepowered.common.item.recipe.SpongeRecipeRegistration;

import java.util.Collections;
import java.util.function.Function;

public class SpongeStonecuttingRecipeRegistration extends SpongeRecipeRegistration<StonecutterRecipe> implements
        SpongeRecipeRegistration.ResultFunctionRegistration<SingleRecipeInput> {

    // Vanilla Recipe
    private final Ingredient ingredient;

    // Sponge Recipe
    private final ItemStack spongeResult;
    private Function<SingleRecipeInput, ItemStack> resultFunction;

    public SpongeStonecuttingRecipeRegistration(ResourceLocation key, String group, Ingredient ingredient,
            ItemStack spongeResult, Function<SingleRecipeInput, ItemStack> resultFunction,
            DataPack<RecipeRegistration> pack, final RecipeCategory category) {
        super(key, group, pack, category, RecipeSerializer.STONECUTTER);
        this.ingredient = ingredient;
        this.spongeResult = spongeResult;
        this.resultFunction = resultFunction;
    }

    @Override
    public Recipe recipe() {
        this.ensureCached();
        if (SpongeRecipeRegistration.isVanillaSerializer(this.spongeResult, this.resultFunction, null, Collections.singleton(this.ingredient))) {
            return (StoneCutterRecipe) new StonecutterRecipe(this.group, this.ingredient, this.spongeResult);
        }
        return (StoneCutterRecipe) new SpongeStonecuttingRecipe(
                this.group, this.ingredient, this.spongeResult, this.resultFunction == null ? null : this.key.toString());
    }

    @Override
    public Function<SingleRecipeInput, ItemStack> resultFunction() {
        return this.resultFunction;
    }
}
