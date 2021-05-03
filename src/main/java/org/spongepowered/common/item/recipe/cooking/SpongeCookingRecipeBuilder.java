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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.recipe.RecipeRegistration;
import org.spongepowered.api.item.recipe.RecipeType;
import org.spongepowered.api.item.recipe.cooking.CookingRecipe;
import org.spongepowered.common.inventory.util.InventoryUtil;
import org.spongepowered.common.item.recipe.SpongeRecipeRegistration;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.AbstractResourceKeyedBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class SpongeCookingRecipeBuilder extends AbstractResourceKeyedBuilder<RecipeRegistration, CookingRecipe.Builder>
        implements CookingRecipe.Builder.ResultStep, CookingRecipe.Builder.IngredientStep, CookingRecipe.Builder.EndStep {

    private net.minecraft.world.item.crafting.RecipeType type;
    private Ingredient ingredient;
    private ItemStack result;
    private Function<Container, net.minecraft.world.item.ItemStack> resultFunction;

    private @Nullable Float experience;
    private @Nullable Integer cookingTime;
    private @Nullable String group;

    @Override
    public ResultStep ingredient(final org.spongepowered.api.item.recipe.crafting.Ingredient ingredient) {
        this.ingredient = (Ingredient) (Object) ingredient;
        return this;
    }

    @Override
    public CookingRecipe.Builder reset() {
        super.reset();
        this.type = null;
        this.ingredient = null;
        this.result = null;
        this.resultFunction = null;
        this.experience = null;
        this.cookingTime = null;
        this.group = null;
        return this;
    }

    @Override
    public EndStep result(final ItemType result) {
        this.result = new ItemStack((Item) result);
        this.resultFunction = null;
        return this;
    }

    @Override
    public EndStep result(final org.spongepowered.api.item.inventory.ItemStack result) {
        this.result = ItemStackUtil.toNative(result);
        this.resultFunction = null;
        return this;
    }

    @Override
    public EndStep result(final ItemStackSnapshot result) {
        return this.result(result.createStack());
    }

    // currently unused
    public EndStep result(final Function<Inventory, org.spongepowered.api.item.inventory.ItemStack> resultFunction, final org.spongepowered.api.item.inventory.ItemStack exemplaryResult) {
        this.result = ItemStackUtil.toNative(exemplaryResult);
        this.resultFunction = (inv) -> ItemStackUtil.toNative(resultFunction.apply(InventoryUtil.toInventory(inv)));
        return this;
    }

    @Override
    public EndStep experience(final double experience) {
        if (experience < 0) {
            throw new IllegalStateException("The experience must be non-negative");
        }
        this.experience = (float) experience;
        return this;
    }

    @Override
    public EndStep cookingTime(final int ticks) {
        this.cookingTime = ticks;
        return this;
    }

    @Override
    public IngredientStep type(final RecipeType<CookingRecipe> type) {
        this.type = (net.minecraft.world.item.crafting.RecipeType) type;
        return this;
    }

    @Override
    public EndStep group(final String group) {
        this.group = group;
        return this;
    }

    @Override
    protected RecipeRegistration build0() {
        Objects.requireNonNull(this.type, "type");
        Objects.requireNonNull(this.ingredient, "ingredient");
        Objects.requireNonNull(this.result, "result");

        if (this.experience == null) {
            this.experience = 0f;
        }

        final List<Ingredient> ingredientList = Collections.singletonList(this.ingredient);

        final RecipeSerializer<?> serializer;
        if (this.type == net.minecraft.world.item.crafting.RecipeType.BLASTING) {
            if (this.cookingTime == null) {
                this.cookingTime = 100;
            }
            serializer = SpongeRecipeRegistration.determineSerializer(this.result, this.resultFunction, null, ingredientList, RecipeSerializer.BLASTING_RECIPE, SpongeCookingRecipeSerializer.Blasting.SPONGE_BLASTING);
        } else if (this.type == net.minecraft.world.item.crafting.RecipeType.CAMPFIRE_COOKING) {
            if (this.cookingTime == null) {
                this.cookingTime = 600;
            }
            serializer = SpongeRecipeRegistration.determineSerializer(this.result, this.resultFunction, null, ingredientList, RecipeSerializer.CAMPFIRE_COOKING_RECIPE, SpongeCookingRecipeSerializer.Campfire.SPONGE_CAMPFIRE_COOKING);
        } else if (this.type == net.minecraft.world.item.crafting.RecipeType.SMOKING) {
            if (this.cookingTime == null) {
                this.cookingTime = 100;
            }
            serializer = SpongeRecipeRegistration.determineSerializer(this.result, this.resultFunction, null, ingredientList, RecipeSerializer.SMOKING_RECIPE, SpongeCookingRecipeSerializer.Smoking.SPONGE_SMOKING);
        } else if (this.type == net.minecraft.world.item.crafting.RecipeType.SMELTING) {
            if (this.cookingTime == null) {
                this.cookingTime = 200;
            }
            serializer = SpongeRecipeRegistration.determineSerializer(this.result, this.resultFunction, null, ingredientList, RecipeSerializer.SMELTING_RECIPE, SpongeCookingRecipeSerializer.Smelting.SPONGE_SMELTING);
        } else {
            throw new IllegalArgumentException("Unknown RecipeType " + this.type);
        }

        return new SpongeCookingRecipeRegistration((ResourceLocation) (Object) this.key, serializer, this.group,
                this.ingredient, this.experience, this.cookingTime, this.result, this.resultFunction);
    }

}
