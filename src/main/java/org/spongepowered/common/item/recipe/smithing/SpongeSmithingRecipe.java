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
package org.spongepowered.common.item.recipe.smithing;


import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;

import java.util.Optional;
import java.util.function.Function;

public class SpongeSmithingRecipe extends SmithingTransformRecipe {

    private Function<SmithingRecipeInput, ItemStack> resultFunction;

    public SpongeSmithingRecipe(final Optional<Ingredient> template, final Optional<Ingredient> base,
            final Optional<Ingredient> addition, final ItemStack spongeResult, final Function<SmithingRecipeInput, ItemStack> resultFunction) {
        super(template, base, addition, spongeResult);
        this.resultFunction = resultFunction;
    }

    @Override
    public ItemStack assemble(final SmithingRecipeInput $$0, final HolderLookup.Provider $$1) {
        if (this.resultFunction != null) {
            return this.resultFunction.apply($$0);
        }

        return super.assemble($$0, $$1);
    }

}
