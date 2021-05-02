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

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmeltingRecipe;

import java.util.function.Function;

public class SpongeFurnaceRecipe extends SmeltingRecipe {

    private final Function<Container, ItemStack> resultFunction;

    public SpongeFurnaceRecipe(final ResourceLocation id, final String group, final Ingredient ingredient, final ItemStack result, final float experience, final int cookingTime, final Function<Container, ItemStack> resultFunction) {
        super(id, group, ingredient, result, experience, cookingTime);
        this.resultFunction = resultFunction;
    }

    @Override
    public ItemStack assemble(final Container container) {
        if (this.resultFunction != null) {
            final ItemStack result = this.resultFunction.apply(container);
            result.setCount(1);
            return result;
        }
        return super.assemble(container);
    }

    @Override
    public ItemStack getResultItem() {
        if (this.resultFunction != null) {
            return ItemStack.EMPTY;
        }
        return super.getResultItem();
    }

}
