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
package org.spongepowered.common.accessor.world.level.block.entity;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.common.UntransformedInvokerError;

import javax.annotation.Nullable;

@Mixin(AbstractFurnaceBlockEntity.class)
public interface AbstractFurnaceBlockEntityAccessor {

    @Accessor("litTime") int accessor$litTime();

    @Accessor("litTime") void accessor$litTime(final int litTime);

    @Accessor("litDuration") int accessor$litDuration();

    @Accessor("litDuration") void accessor$litDuration(final int litDuration);

    @Accessor("cookingProgress") int accessor$cookingProgress();

    @Accessor("cookingProgress") void accessor$cookingProgress(final int cookingProgress);

    @Accessor("cookingTotalTime") int accessor$cookingTotalTime();

    @Accessor("cookingTotalTime") void accessor$cookingTotalTime(final int cookingTotalTime);

    @Accessor("quickCheck")  RecipeManager.CachedCheck<Container, ? extends AbstractCookingRecipe> accessor$quickCheck();

    /* @Invoker("canBurn") // TODO SF 1.19.4
    static boolean invoker$canBurn(final RegistryAccess registryAccess, @Nullable final Recipe<?> var0, final NonNullList<ItemStack> var1, final int var2) {
        throw new UntransformedInvokerError();
    } */

}
