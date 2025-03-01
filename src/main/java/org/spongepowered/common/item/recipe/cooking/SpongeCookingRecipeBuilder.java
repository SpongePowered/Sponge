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

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStackLike;
import org.spongepowered.api.item.recipe.RecipeType;
import org.spongepowered.api.item.recipe.cooking.CookingRecipe;
import org.spongepowered.api.item.recipe.crafting.RecipeInput;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.common.inventory.util.InventoryUtil;
import org.spongepowered.common.item.recipe.SpongeRecipeRegistration;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.SpongeTicks;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public final class SpongeCookingRecipeBuilder implements CookingRecipe.Builder.ResultStep, CookingRecipe.Builder.IngredientStep, CookingRecipe.Builder.EndStep {

    private net.minecraft.world.item.crafting.RecipeType<?> type;
    private Ingredient ingredient;
    private ItemStack result;
    private Function<SingleRecipeInput, net.minecraft.world.item.ItemStack> resultFunction;

    private @Nullable Float experience;
    private @Nullable Ticks cookingTime;
    private @Nullable String group;

    private RecipeCategory recipeCategory = RecipeCategory.MISC; // TODO support category
    private CookingBookCategory cookingCategory = CookingBookCategory.MISC; // TODO support category

    @Override
    public ResultStep ingredient(final org.spongepowered.api.item.recipe.crafting.Ingredient ingredient) {
        this.ingredient = (Ingredient) (Object) ingredient;
        return this;
    }

    @Override
    public CookingRecipe.Builder reset() {
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
    public EndStep result(final ItemStackLike result) {
        this.result = ItemStackUtil.fromLikeToNative(result);
        this.resultFunction = null;
        return this;
    }

    // currently unused
    public EndStep result(final Function<RecipeInput.Single, ? extends ItemStackLike> resultFunction, final ItemStackLike exemplaryResult) {
        this.result = ItemStackUtil.fromLikeToNative(exemplaryResult);
        this.resultFunction = (inv) -> ItemStackUtil.fromLikeToNative(resultFunction.apply(InventoryUtil.toSponge(inv)));
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
    public EndStep cookingTime(final Ticks ticks) {
        this.cookingTime = Objects.requireNonNull(ticks);
        return this;
    }

    @Override
    public IngredientStep type(final RecipeType<CookingRecipe> type) {
        this.type = (net.minecraft.world.item.crafting.RecipeType<?>) type;
        return this;
    }

    @Override
    public EndStep group(final String group) {
        this.group = group;
        return this;
    }

    @Override
    public CookingRecipe build() {
        Objects.requireNonNull(this.type, "type");
        Objects.requireNonNull(this.ingredient, "ingredient");
        Objects.requireNonNull(this.result, "result");

        final var ingredientList = Collections.singletonList(this.ingredient);
        final boolean isVanilla = SpongeRecipeRegistration.isVanillaSerializer(this.result, this.resultFunction, null, ingredientList);

        String group = this.group == null ? "" : this.group;
        float experience = this.experience == null ? 0 : this.experience;

        if (this.type == net.minecraft.world.item.crafting.RecipeType.BLASTING) {
            final int ticksCookingTime = Optional.ofNullable(this.cookingTime).map(SpongeTicks::toSaturatedIntOrInfinite).orElse(100);
            if (!isVanilla) {
                return (CookingRecipe) new SpongeBlastingRecipe(group, this.cookingCategory, this.ingredient, this.result, experience, ticksCookingTime, this.resultFunction);
            }
            return (CookingRecipe) new BlastingRecipe(group, this.cookingCategory, this.ingredient, this.result, experience, ticksCookingTime);
        }
        if (this.type == net.minecraft.world.item.crafting.RecipeType.CAMPFIRE_COOKING) {
            final int ticksCookingTime = Optional.ofNullable(this.cookingTime).map(SpongeTicks::toSaturatedIntOrInfinite).orElse(600);
            if (!isVanilla) {
                return (CookingRecipe) new SpongeCampfireCookingRecipe(group, this.cookingCategory, this.ingredient, this.result, experience, ticksCookingTime, this.resultFunction);
            }
            return (CookingRecipe) new CampfireCookingRecipe(group, this.cookingCategory, this.ingredient, this.result, experience, ticksCookingTime);
        }
        if (this.type == net.minecraft.world.item.crafting.RecipeType.SMOKING) {
            final int ticksCookingTime = Optional.ofNullable(this.cookingTime).map(SpongeTicks::toSaturatedIntOrInfinite).orElse(100);
            if (!isVanilla) {
                return (CookingRecipe) new SpongeSmokingRecipe(group, this.cookingCategory, this.ingredient, this.result, experience, ticksCookingTime, this.resultFunction);
            }
            return (CookingRecipe) new SmokingRecipe(group, this.cookingCategory, this.ingredient, this.result, experience, ticksCookingTime);
        }
        if (this.type == net.minecraft.world.item.crafting.RecipeType.SMELTING) {
            final int ticksCookingTime = Optional.ofNullable(this.cookingTime).map(SpongeTicks::toSaturatedIntOrInfinite).orElse(200);
            if (!isVanilla) {
                return (CookingRecipe) new SpongeSmeltingRecipe(group, this.cookingCategory, this.ingredient, this.result, experience, ticksCookingTime, this.resultFunction);
            }
            return (CookingRecipe) new SmeltingRecipe(group, this.cookingCategory, this.ingredient, this.result, experience, ticksCookingTime);
        }
        throw new IllegalArgumentException("Unknown RecipeType " + this.type);
    }
}
