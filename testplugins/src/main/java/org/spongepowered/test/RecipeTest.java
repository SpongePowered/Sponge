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
package org.spongepowered.test;

import static org.spongepowered.api.item.ItemTypes.BED;
import static org.spongepowered.api.item.ItemTypes.BEDROCK;
import static org.spongepowered.api.item.ItemTypes.STONE;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.api.item.recipe.crafting.Ingredient;
import org.spongepowered.api.item.recipe.crafting.ShapedCraftingRecipe;
import org.spongepowered.api.plugin.Plugin;

/**
 * Adds BedRock. Literally.
 */
@Plugin(id = "recipetest", name = "Recipe Test", description = "A plugin to test recipes")
public class RecipeTest {

    @Listener
    public void onInit(GameInitializationEvent event) {
        Ingredient s = Ingredient.of(STONE);
        Ingredient b = Ingredient.of(BED);
        ShapedCraftingRecipe recipe = CraftingRecipe.shapedBuilder().rows()
                .row(s, s, s)
                .row(s, b, s)
                .row(s, s, s)
                .result(ItemStack.of(BEDROCK, 1))
                .build("bedrock", this);
        Sponge.getRegistry().getCraftingRecipeRegistry().register(recipe);

    }

}
