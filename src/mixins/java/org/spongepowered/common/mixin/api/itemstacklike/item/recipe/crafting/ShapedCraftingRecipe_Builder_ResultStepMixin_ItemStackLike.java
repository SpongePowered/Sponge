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
package org.spongepowered.common.mixin.api.itemstacklike.item.recipe.crafting;

import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.recipe.crafting.RecipeInput;
import org.spongepowered.api.item.recipe.crafting.ShapedCraftingRecipe;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Function;

@Mixin(value = ShapedCraftingRecipe.Builder.ResultStep.class, remap = false)
public interface ShapedCraftingRecipe_Builder_ResultStepMixin_ItemStackLike {

    default ShapedCraftingRecipe.Builder.EndStep result(ItemStackSnapshot result) {
        return ((ShapedCraftingRecipe.Builder.ResultStep) this).result(result);
    }

    default ShapedCraftingRecipe.Builder.EndStep result(ItemStack result) {
        return ((ShapedCraftingRecipe.Builder.ResultStep) this).result(result);
    }

    default ShapedCraftingRecipe.Builder.EndStep result(Function<RecipeInput.Crafting, ItemStack> resultFunction, ItemStack exemplaryResult) {
        return ((ShapedCraftingRecipe.Builder.ResultStep) this).result(resultFunction, exemplaryResult);
    }
}
