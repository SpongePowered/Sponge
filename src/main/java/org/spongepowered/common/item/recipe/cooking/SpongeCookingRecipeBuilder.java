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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.recipe.RecipeRegistration;
import org.spongepowered.api.item.recipe.RecipeType;
import org.spongepowered.api.item.recipe.cooking.CookingRecipe;
import org.spongepowered.common.inventory.util.InventoryUtil;
import org.spongepowered.common.item.recipe.SpongeRecipeRegistration;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.SpongeCatalogBuilder;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class SpongeCookingRecipeBuilder extends SpongeCatalogBuilder<RecipeRegistration, CookingRecipe.Builder>
        implements CookingRecipe.Builder.ResultStep, CookingRecipe.Builder.IngredientStep, CookingRecipe.Builder.EndStep {

    private IRecipeType type;
    private Ingredient ingredient;
    private ItemStack result;
    private Function<IInventory, net.minecraft.item.ItemStack> resultFunction;

    @Nullable private Float experience;
    @Nullable private Integer cookingTime;
    @Nullable private String group;

    @Override
    public ResultStep ingredient(org.spongepowered.api.item.recipe.crafting.Ingredient ingredient) {
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
    public EndStep result(ItemType result) {
        this.result = new ItemStack((Item) result);
        this.resultFunction = null;
        return this;
    }

    @Override
    public EndStep result(org.spongepowered.api.item.inventory.ItemStack result) {
        this.result = ItemStackUtil.toNative(result);
        this.resultFunction = null;
        return this;
    }

    @Override
    public EndStep result(ItemStackSnapshot result) {
        return this.result(result.createStack());
    }

    // currently unused
    public EndStep result(Function<Inventory, org.spongepowered.api.item.inventory.ItemStack> resultFunction, org.spongepowered.api.item.inventory.ItemStack exemplaryResult) {
        this.result = ItemStackUtil.toNative(exemplaryResult);
        this.resultFunction = (inv) -> ItemStackUtil.toNative(resultFunction.apply(InventoryUtil.toInventory(inv)));
        return this;
    }

    @Override
    public EndStep experience(double experience) {
        checkState(experience >= 0, "The experience must be non-negative.");
        this.experience = (float) experience;
        return this;
    }

    @Override
    public EndStep cookingTime(int ticks) {
        this.cookingTime = ticks;
        return this;
    }

    @Override
    public IngredientStep type(RecipeType<CookingRecipe> type) {
        this.type = (IRecipeType) type;
        return this;
    }

    @Override
    public EndStep group(String group) {
        this.group = group;
        return this;
    }

    @Override
    protected RecipeRegistration build(ResourceKey key) {
        checkNotNull(this.type);
        checkNotNull(this.ingredient);
        checkNotNull(this.result);
        checkNotNull(key);
        this.key = key;

        if (this.experience == null) {
            this.experience = 0f;
        }

        final List<Ingredient> ingredientList = Collections.singletonList(this.ingredient);

        IRecipeSerializer<?> serializer;
        if (this.type == IRecipeType.BLASTING) {
            if (this.cookingTime == null) {
                this.cookingTime = 100;
            }
            serializer = SpongeRecipeRegistration.determineSerializer(this.result, this.resultFunction, null, ingredientList, IRecipeSerializer.BLASTING, SpongeCookingRecipeSerializer.Blasting.SPONGE_BLASTING);
        } else if (this.type == IRecipeType.CAMPFIRE_COOKING) {
            if (this.cookingTime == null) {
                this.cookingTime = 600;
            }
            serializer = SpongeRecipeRegistration.determineSerializer(this.result, this.resultFunction, null, ingredientList, IRecipeSerializer.CAMPFIRE_COOKING, SpongeCookingRecipeSerializer.Campfire.SPONGE_CAMPFIRE_COOKING);
        } else if (this.type == IRecipeType.SMOKING) {
            if (this.cookingTime == null) {
                this.cookingTime = 100;
            }
            serializer = SpongeRecipeRegistration.determineSerializer(this.result, this.resultFunction, null, ingredientList, IRecipeSerializer.SMOKING, SpongeCookingRecipeSerializer.Smoking.SPONGE_SMOKING);
        } else if (this.type == IRecipeType.SMELTING) {
            if (this.cookingTime == null) {
                this.cookingTime = 200;
            }
            serializer = SpongeRecipeRegistration.determineSerializer(this.result, this.resultFunction, null, ingredientList, IRecipeSerializer.SMELTING, SpongeCookingRecipeSerializer.Smelting.SPONGE_SMELTING);
        } else {
            throw new IllegalArgumentException("Unknown RecipeType " + this.type);
        }

        return new SpongeCookingRecipeRegistration((ResourceLocation) (Object) this.key, serializer, this.group,
                this.ingredient, this.experience, this.cookingTime, this.result, this.resultFunction);
    }

}
