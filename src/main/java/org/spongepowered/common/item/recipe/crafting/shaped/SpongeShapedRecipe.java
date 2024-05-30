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
package org.spongepowered.common.item.recipe.crafting.shaped;


import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import org.spongepowered.common.bridge.world.item.crafting.RecipeResultBridge;
import org.spongepowered.common.bridge.world.item.crafting.ShapedRecipeBridge;
import org.spongepowered.common.item.recipe.ResultFunctionRecipe;
import org.spongepowered.common.item.recipe.ingredient.IngredientResultUtil;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

public class SpongeShapedRecipe extends ShapedRecipe implements ResultFunctionRecipe {

    public static final MapCodec<SpongeShapedRecipe> SPONGE_CODEC = RecordCodecBuilder.mapCodec(
            $$0 -> $$0.group(
                            Codec.STRING.fieldOf(Constants.Recipe.SPONGE_TYPE).forGetter(t -> "custom"), // important to fail early when decoding vanilla recipes
                            Codec.STRING.optionalFieldOf(Constants.Recipe.GROUP, "").forGetter(ShapedRecipe::getGroup),
                            CraftingBookCategory.CODEC.fieldOf(Constants.Recipe.CATEGORY).orElse(CraftingBookCategory.MISC).forGetter(ShapedRecipe::category),
                            ShapedRecipePattern.MAP_CODEC.forGetter($$0x -> ((ShapedRecipeBridge) $$0x).bridge$pattern()),
                            ItemStack.CODEC.fieldOf(Constants.Recipe.RESULT).forGetter($$0x -> ((RecipeResultBridge)$$0x).bridge$result()),
                            Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(ShapedRecipe::showNotification),
                            IngredientResultUtil.CACHED_RESULT_FUNC_CODEC.optionalFieldOf(Constants.Recipe.SPONGE_RESULTFUNCTION).forGetter(ResultFunctionRecipe::resultFunctionId),
                            IngredientResultUtil.CACHED_REMAINING_FUNC_CODEC.optionalFieldOf(Constants.Recipe.SPONGE_REMAINING_ITEMS).forGetter(SpongeShapedRecipe::remainingItemsFunctionId)
                    )
                    .apply($$0, SpongeShapedRecipe::of)
    );

    private final String resultFunctionId;
    private final String remainingItemsFunctionId;

    public static SpongeShapedRecipe of(
            final String spongeType,
            final String groupIn, final CraftingBookCategory category,
            final ShapedRecipePattern pattern,
            final ItemStack recipeOutputIn,
            final boolean showNotification,
            final Optional<String> resultFunctionId,
            final Optional<String> remainingItemsFunctionId) {
        return new SpongeShapedRecipe(
                groupIn,
                category,
                pattern,
                showNotification,
                recipeOutputIn,
                resultFunctionId.orElse(null),
                remainingItemsFunctionId.orElse(null));
    }

    public SpongeShapedRecipe(
            final String groupIn,
            final CraftingBookCategory category,
            final ShapedRecipePattern pattern,
            final boolean showNotification,
            final ItemStack resultStack,
            final String resultFunctionId,
            final String remainingItemsFunctionId) {
        super(groupIn, category, pattern, resultStack, showNotification);
        this.resultFunctionId = resultFunctionId;
        this.remainingItemsFunctionId = remainingItemsFunctionId;
    }

    @Override
    public Optional<String> resultFunctionId() {
        return Optional.ofNullable(resultFunctionId);
    }

    public Optional<String> remainingItemsFunctionId() {
        return Optional.ofNullable(remainingItemsFunctionId);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(final CraftingInput $$0) {
        if (this.remainingItemsFunctionId != null) {
            return IngredientResultUtil.cachedRemainingItemsFunction(this.remainingItemsFunctionId).apply($$0);
        }
        return super.getRemainingItems($$0);
    }

    @Override
    public ItemStack assemble(final CraftingInput $$0, final HolderLookup.Provider $$1) {
        if (this.resultFunctionId != null) {
            return IngredientResultUtil.cachedResultFunction(this.resultFunctionId).apply($$0);
        }
        return super.assemble($$0, $$1);
    }

    @Override
    public ItemStack getResultItem(final HolderLookup.Provider $$0) {
        if (this.resultFunctionId != null) {
            return ItemStack.EMPTY;
        }
        return super.getResultItem($$0);
    }

}
