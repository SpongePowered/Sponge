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
package org.spongepowered.common.accessor.tileentity;

import net.minecraft.item.crafting.AbstractCookingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractFurnaceTileEntity.class)
public interface AbstractFurnaceTileEntityAccessor {

    @Accessor("litTime") int accessor$getLitTime();

    @Accessor("litTime") void accessor$setLitTime(int litTime);

    @Accessor("litDuration") int accessor$getLitDuration();

    @Accessor("litDuration") void accessor$setLitDuration(int litDuration);

    @Accessor("cookingProgress") int accessor$getCookingProgress();

    @Accessor("cookingProgress") void accessor$setCookingProgress(int cookingProgress);

    @Accessor("cookingTotalTime") int accessor$getCookingTotalTime();

    @Accessor("cookingTotalTime") void accessor$setCookingTotalTime(int cookingTotalTime);

    @Accessor("recipeType") IRecipeType<? extends AbstractCookingRecipe> accessor$getRecipeType();
}
