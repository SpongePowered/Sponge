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
package org.spongepowered.common.item.recipe.crafting.shapeless;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipeCodecs;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.spongepowered.common.item.recipe.ingredient.IngredientResultUtil;
import org.spongepowered.common.item.recipe.ingredient.IngredientUtil;
import org.spongepowered.common.util.Constants;

/**
 * Custom ShapelessRecipe.Serializer with support for:
 * result full ItemStack instead of ItemType+Count
 * result functions
 * ingredient ItemStacks
 * remaining items function
 */
public class SpongeShapelessCraftingRecipeSerializer extends ShapelessRecipe.Serializer {

    private static final Codec<SpongeShapelessRecipe> CODEC = RecordCodecBuilder.create(
            $$0 -> $$0.group(
                            ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter(ShapelessRecipe::getGroup),
                            CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ShapelessRecipe::category),
                            CraftingRecipeCodecs.ITEMSTACK_OBJECT_CODEC.fieldOf("result").forGetter($$0x -> $$0x.getResultItem(null)),
                            // TODO Constants.Recipe.SPONGE_RESULT
                            // TODO IngredientResultUtil.deserializeResultFunction
                            // TODO IngredientResultUtil.deserializeRemainingItemsFunction
                            Ingredient.CODEC_NONEMPTY
                                    .listOf()
                                    .fieldOf("ingredients")
                                    .flatXmap(
                                            $$0x -> {
                                                Ingredient[] $$1 = $$0x.stream().filter($$0xx -> !$$0xx.isEmpty()).toArray(Ingredient[]::new);
                                                if ($$1.length == 0) {
                                                    return DataResult.error(() -> "No ingredients for shapeless recipe");
                                                } else {
                                                    return $$1.length > 9
                                                            ? DataResult.error(() -> "Too many ingredients for shapeless recipe")
                                                            : DataResult.success(NonNullList.of(Ingredient.EMPTY, $$1));
                                                }
                                            },
                                            DataResult::success
                                    )
                                    .forGetter(ShapelessRecipe::getIngredients),
                            IngredientResultUtil.CACHED_RESULT_FUNC_CODEC.optionalFieldOf(Constants.Recipe.SPONGE_RESULTFUNCTION, null).forGetter(SpongeShapelessRecipe::resultFunctionId),
                            IngredientResultUtil.CACHED_REMAINING_FUNC_CODEC.optionalFieldOf(Constants.Recipe.SPONGE_REMAINING_ITEMS, null).forGetter(SpongeShapelessRecipe::remainingItemsFunctionId)
                    )
                    .apply($$0, SpongeShapelessRecipe::new)
    );

    @Override
    public Codec<ShapelessRecipe> codec() {
        return CODEC;
    }

    private NonNullList<Ingredient> readIngredients(final JsonArray json) {
        final NonNullList<Ingredient> nonnulllist = NonNullList.create();
        for (final JsonElement element : json) {
            final Ingredient ingredient = IngredientUtil.spongeDeserialize(element);
            if (!ingredient.isEmpty()) {
                nonnulllist.add(ingredient);
            }
        }
        return nonnulllist;
    }

    @Override
    public ShapelessRecipe fromNetwork(final FriendlyByteBuf $$0) {
        throw new UnsupportedOperationException("custom serializer needs client side support");
    }

    @Override
    public void toNetwork(final FriendlyByteBuf p_199427_1_, final ShapelessRecipe p_199427_2_) {
        throw new UnsupportedOperationException("custom serializer needs client side support");
    }
}
