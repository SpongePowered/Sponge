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

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.common.item.recipe.cooking.ResultFunctionRecipe;
import org.spongepowered.common.item.recipe.ingredient.IngredientResultUtil;

import java.util.function.Function;

public class SpongeStonecuttingRecipe extends StonecutterRecipe implements ResultFunctionRecipe {

    private final String resultFunctionId;

    public SpongeStonecuttingRecipe(String groupIn, Ingredient ingredientIn, ItemStack resultIn, String resultFunctionId) {
        super(groupIn, ingredientIn, resultIn);
        this.resultFunctionId = resultFunctionId;
    }

    @Nullable @Override
    public String resultFunctionId() {
        return this.resultFunctionId;
    }

    @Override
    public ItemStack assemble(final Container container, final RegistryAccess $$1) {
        if (this.resultFunctionId != null) {
            return IngredientResultUtil.cachedResultFunction(this.resultFunctionId).apply(container);
        }
        return super.assemble(container, $$1);
    }

    @Override
    public ItemStack getResultItem(final RegistryAccess $$1) {
        if (this.resultFunctionId != null) {
            return ItemStack.EMPTY;
        }
        return super.getResultItem($$1);
    }

}
