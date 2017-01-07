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
package org.spongepowered.common.item.recipe.smelting;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.recipe.RecipeRegistry;
import org.spongepowered.api.item.recipe.smelting.SmeltingRecipe;
import org.spongepowered.common.SpongeImpl;

public class SpongeSmeltingRecipe implements SmeltingRecipe, SmeltingRecipe.Builder {

    private ItemStackSnapshot ingredient;
    private ItemStackSnapshot result;
    private double experience;

    public SpongeSmeltingRecipe() {
        reset();
    }

    @Override
    public Builder reset() {
        ingredient = null;
        result = null;
        experience = 0.0f;
        return this;
    }

    @Override
    public Builder from(SmeltingRecipe value) {
        ingredient = value.getIngredient();
        result = value.getResult();
        experience = value.getExperience();
        return this;
    }

    @Override
    public Builder ingredient(ItemStack ingredient) {
        this.ingredient = ingredient.createSnapshot();
        return this;
    }

    @Override
    public Builder ingredient(ItemType type) {
        this.ingredient = type.getTemplate();
        return this;
    }

    @Override
    public Builder result(ItemStack result) {
        this.result = result.createSnapshot();
        return this;
    }

    @Override
    public Builder experience(double exp) {
        this.experience = exp;
        return this;
    }

    @Override
    public SmeltingRecipe build() {
        checkNotNull(ingredient, "ingredient");
        checkNotNull(result, "result");
        checkNotNull(experience, "experience");
        return this;
    }

    @Override
    public double getExperience() {
        return experience;
    }

    @Override
    public ItemStackSnapshot getIngredient() {
        return ingredient;
    }

    @Override
    public ItemStackSnapshot getResult() {
        return result;
    }

    @Override
    public RecipeRegistry<?> getRegistry() {
        return SpongeImpl.getRegistry().getSmeltingRegistry();
    }

}
