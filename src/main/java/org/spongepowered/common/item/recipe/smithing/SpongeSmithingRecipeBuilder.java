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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.recipe.RecipeRegistration;
import org.spongepowered.api.item.recipe.single.StoneCutterRecipe;
import org.spongepowered.api.item.recipe.smithing.SmithingRecipe;
import org.spongepowered.common.inventory.util.InventoryUtil;
import org.spongepowered.common.item.recipe.SpongeRecipeRegistration;
import org.spongepowered.common.item.recipe.ingredient.IngredientUtil;
import org.spongepowered.common.item.recipe.stonecutting.SpongeStonecuttingRecipeRegistration;
import org.spongepowered.common.item.recipe.stonecutting.SpongeStonecuttingRecipeSerializer;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.AbstractResourceKeyedBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class SpongeSmithingRecipeBuilder extends AbstractResourceKeyedBuilder<RecipeRegistration, SmithingRecipe.Builder> implements
        SmithingRecipe.Builder, SmithingRecipe.Builder.AdditionStep, SmithingRecipe.Builder.ResultStep, SmithingRecipe.Builder.EndStep {

    private ItemStack result;
    private Ingredient base;
    private Ingredient addition;
    private Function<Container, net.minecraft.world.item.ItemStack> resultFunction;
    private @Nullable String group;

    @Override
    public AdditionStep base(ItemType ingredient) {
        this.base = Ingredient.of(() -> ((Item) ingredient));
        return this;
    }

    @Override
    public AdditionStep base(org.spongepowered.api.item.recipe.crafting.Ingredient ingredient) {
        this.base = IngredientUtil.toNative(ingredient);
        return this;
    }

    @Override
    public ResultStep addition(ItemType ingredient) {
        this.addition = Ingredient.of(() -> ((Item) ingredient));
        return this;
    }

    @Override
    public ResultStep addition(org.spongepowered.api.item.recipe.crafting.Ingredient ingredient) {
        this.addition = IngredientUtil.toNative(ingredient);
        return this;
    }

    @Override
    public EndStep result(ItemStackSnapshot result) {
        this.result = result.createStack();
        this.resultFunction = null;
        return this;
    }

    @Override
    public EndStep result(final ItemStack result) {
        checkNotNull(result, "result");
        this.result = result;
        this.resultFunction = null;
        return this;
    }

    @Override
    public EndStep result(Function<Inventory, ItemStack> resultFunction, ItemStack exemplaryResult) {
        checkNotNull(exemplaryResult, "exemplaryResult");
        checkState(!exemplaryResult.isEmpty(), "exemplaryResult must not be empty");

        this.result = exemplaryResult;
        this.resultFunction = (inv) -> ItemStackUtil.toNative(resultFunction.apply(InventoryUtil.toInventory(inv)));
        return this;
    }

    @Override
    public EndStep group(@Nullable String name) {
        this.group = name;
        return this;
    }

    @Override
    public RecipeRegistration build0() {
        final net.minecraft.world.item.ItemStack result = ItemStackUtil.toNative(this.result);
        final RecipeSerializer<?> serializer = SpongeRecipeRegistration.determineSerializer(result, this.resultFunction, null, Arrays.asList(this.base, this.addition),
                RecipeSerializer.SMITHING, SpongeSmithingRecipeSerializer.SPONGE_SMITHING);

        return new SpongeSmithingRecipeRegistration((ResourceLocation) (Object) key, serializer, this.group, this.base, this.addition, result, this.resultFunction);
    }

    @Override
    public SmithingRecipe.Builder reset() {
        this.result = null;
        this.resultFunction = null;
        this.base = null;
        this.addition = null;
        this.group = null;
        return super.reset();
    }
}
