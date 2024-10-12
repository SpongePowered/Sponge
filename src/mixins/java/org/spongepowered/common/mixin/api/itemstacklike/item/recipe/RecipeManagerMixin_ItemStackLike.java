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
package org.spongepowered.common.mixin.api.itemstacklike.item.recipe;

import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.recipe.Recipe;
import org.spongepowered.api.item.recipe.RecipeManager;
import org.spongepowered.api.item.recipe.RecipeType;
import org.spongepowered.api.item.recipe.cooking.CookingRecipe;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

@Mixin(value = RecipeManager.class, remap = false)
public interface RecipeManagerMixin_ItemStackLike {

    default <T extends Recipe<?>> Collection<T> findByResult(RecipeType<T> type, ItemStackSnapshot result) {
        return ((RecipeManager) this).findByResult(type, result);
    }

    default <T extends Recipe<?>> Collection<T> findByResult(Supplier<? extends RecipeType<T>> supplier, ItemStackSnapshot result) {
        return ((RecipeManager) this).findByResult(supplier, result);
    }

    default <T extends CookingRecipe> Optional<T> findCookingRecipe(RecipeType<T> type, ItemStackSnapshot ingredient) {
        return ((RecipeManager) this).findCookingRecipe(type, ingredient);
    }

    default <T extends CookingRecipe> Optional<T> findCookingRecipe(Supplier<? extends RecipeType<T>> supplier, ItemStackSnapshot ingredient) {
        return ((RecipeManager) this).findCookingRecipe(supplier, ingredient);
    }
}
