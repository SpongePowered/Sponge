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

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.SmokingRecipe;
import net.minecraft.item.crafting.StonecuttingRecipe;
import net.minecraft.util.ResourceLocation;

import java.util.function.Function;

public class SpongeStonecuttingRecipe extends StonecuttingRecipe {

    private final Function<IInventory, ItemStack> resultFunction;

    public SpongeStonecuttingRecipe(ResourceLocation idIn, String groupIn, Ingredient ingredientIn, ItemStack resultIn, Function<IInventory, ItemStack> resultFunction) {
        super(idIn, groupIn, ingredientIn, resultIn);
        this.resultFunction = resultFunction;
    }

    @Override
    public ItemStack getCraftingResult(IInventory p_77572_1_) {
        if (this.resultFunction != null) {
            return this.resultFunction.apply(p_77572_1_);
        }
        return super.getCraftingResult(p_77572_1_);
    }

    @Override
    public ItemStack getRecipeOutput() {
        if (this.resultFunction != null) {
            return ItemStack.EMPTY;
        }
        return super.getRecipeOutput();
    }

}
