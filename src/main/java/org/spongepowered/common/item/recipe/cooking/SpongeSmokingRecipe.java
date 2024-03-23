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

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmokingRecipe;
import org.spongepowered.common.item.recipe.ResultFunctionRecipe;
import org.spongepowered.common.item.recipe.ingredient.IngredientResultUtil;

import java.util.Optional;

public class SpongeSmokingRecipe extends SmokingRecipe implements ResultFunctionRecipe {

    private final String resultFunctionId;

    public SpongeSmokingRecipe(final String group, final CookingBookCategory category, final Ingredient ingredient, final ItemStack result, final float experience, final int cookingTime, final String resultFunctionId) {
        super(group, category, ingredient, result, experience, cookingTime);
        this.resultFunctionId = resultFunctionId;
    }

    @Override
    public Optional<String> resultFunctionId() {
        return Optional.ofNullable(this.resultFunctionId);
    }


    @Override
    public ItemStack assemble(final Container container, final HolderLookup.Provider $$1) {
        if (this.resultFunctionId != null) {
            final ItemStack result = IngredientResultUtil.cachedResultFunction(this.resultFunctionId).apply(container);
            result.setCount(1);
            return result;
        }
        return super.assemble(container, $$1);
    }

    @Override
    public ItemStack getResultItem(final HolderLookup.Provider $$1) {
        if (this.resultFunctionId != null) {
            return ItemStack.EMPTY;
        }
        return super.getResultItem($$1);
    }


}
