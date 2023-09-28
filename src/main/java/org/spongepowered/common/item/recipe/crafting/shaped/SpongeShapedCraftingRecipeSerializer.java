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

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipeCodecs;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.common.accessor.world.item.crafting.ShapedRecipeAccessor;
import org.spongepowered.common.accessor.world.item.crafting.ShapedRecipe_SerializerAccessor;
import org.spongepowered.common.item.recipe.ingredient.IngredientResultUtil;
import org.spongepowered.common.util.Constants;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Custom ShapedRecipe.Serializer with support for: result full ItemStack instead of ItemType+Count result functions ingredient itemstacks remaining
 * items function
 */
public class SpongeShapedCraftingRecipeSerializer extends ShapedRecipe.Serializer {


    record RawShapedRecipe(String group,
                           CraftingBookCategory category,
                           Map<String, Ingredient> key,
                           List<String> pattern,
                           ItemStack result,
                           boolean showNotification,

                           ItemStack spongeResult,
                           Optional<String> resultFunctionId,
                           Optional<String> remainingItemsFunctionId
    ) {

        public static final Codec<RawShapedRecipe> CODEC = RecordCodecBuilder.create(
                $$0 -> {
                    final Codec<String> SINGLE_CHARACTER_STRING_CODEC = ShapedRecipe_SerializerAccessor.accessor$SINGLE_CHARACTER_STRING_CODEC();
                    final Codec<List<String>> PATTERN_CODEC = ShapedRecipe_SerializerAccessor.accessor$PATTERN_CODEC();
                    return $$0.group(
                                    ExtraCodecs.strictOptionalField(Codec.STRING, Constants.Recipe.GROUP, "").forGetter($$0x -> $$0x.group),
                                    CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter($$0x -> $$0x.category),
                                    ExtraCodecs.strictUnboundedMap(SINGLE_CHARACTER_STRING_CODEC, Ingredient.CODEC_NONEMPTY).fieldOf("key").forGetter($$0x -> $$0x.key),
                                    PATTERN_CODEC.fieldOf("pattern").forGetter($$0x -> $$0x.pattern),
                                    CraftingRecipeCodecs.ITEMSTACK_OBJECT_CODEC.fieldOf("result").forGetter($$0x -> $$0x.result),
                                    ExtraCodecs.strictOptionalField(Codec.BOOL, "show_notification", true).forGetter($$0x -> $$0x.showNotification),
                                    ItemStack.CODEC.optionalFieldOf(Constants.Recipe.SPONGE_RESULT, ItemStack.EMPTY).forGetter(raw -> raw.spongeResult),
                                    IngredientResultUtil.CACHED_RESULT_FUNC_CODEC.optionalFieldOf(Constants.Recipe.SPONGE_RESULTFUNCTION).forGetter(i -> i.resultFunctionId),
                                    IngredientResultUtil.CACHED_REMAINING_FUNC_CODEC.optionalFieldOf(Constants.Recipe.SPONGE_REMAINING_ITEMS).forGetter(i -> i.remainingItemsFunctionId)
                            )
                            .apply($$0, RawShapedRecipe::new);
                }
        );
    }

    // modified copy of ShapedRecipe.Serializer.CODEC
    private static final Codec<ShapedRecipe> CODEC = RawShapedRecipe.CODEC.flatXmap($$0 -> {
        String[] $$1 = ShapedRecipeAccessor.invoker$shrink($$0.pattern);
        int $$2 = $$1[0].length();
        int $$3 = $$1.length;
        NonNullList<Ingredient> $$4 = NonNullList.withSize($$2 * $$3, Ingredient.EMPTY);
        Set<String> $$5 = Sets.newHashSet($$0.key.keySet());

        for (int $$6 = 0; $$6 < $$1.length; ++$$6) {
            String $$7 = $$1[$$6];

            for (int $$8 = 0; $$8 < $$7.length(); ++$$8) {
                String $$9 = $$7.substring($$8, $$8 + 1);
                Ingredient $$10 = $$9.equals(" ") ? Ingredient.EMPTY : $$0.key.get($$9);
                if ($$10 == null) {
                    return DataResult.error(() -> "Pattern references symbol '" + $$9 + "' but it's not defined in the key");
                }

                $$5.remove($$9);
                $$4.set($$8 + $$2 * $$6, $$10);
            }
        }

        if (!$$5.isEmpty()) {
            return DataResult.error(() -> "Key defines symbols that aren't used in pattern: " + $$5);
        } else {
            ShapedRecipe $$11 = new SpongeShapedRecipe($$0.group, $$0.category, $$2, $$3, $$4, $$0.spongeResult.isEmpty() ? $$0.result : $$0.spongeResult, $$0.showNotification,
                    $$0.resultFunctionId.orElse(null), $$0.remainingItemsFunctionId.orElse(null));
            return DataResult.success($$11);
        }
    }, $$0 -> {
        throw new NotImplementedException("Serializing ShapedRecipe is not implemented yet.");
    });

    @Override
    public Codec<ShapedRecipe> codec() {
        return CODEC;
    }


    @Override
    public ShapedRecipe fromNetwork(final FriendlyByteBuf $$0) {
        throw new UnsupportedOperationException("custom serializer needs client side support");
    }

    @Override
    public void toNetwork(final FriendlyByteBuf p_199427_1_, final ShapedRecipe p_199427_2_) {
        throw new UnsupportedOperationException("custom serializer needs client side support");
    }
}
