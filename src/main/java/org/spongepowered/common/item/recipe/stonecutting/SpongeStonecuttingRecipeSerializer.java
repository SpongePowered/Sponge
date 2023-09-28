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
package org.spongepowered.common.item.recipe.stonecutting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import org.spongepowered.common.item.recipe.cooking.ResultFunctionRecipe;
import org.spongepowered.common.item.recipe.ingredient.IngredientResultUtil;
import org.spongepowered.common.util.Constants;

public class SpongeStonecuttingRecipeSerializer<R extends SingleItemRecipe & ResultFunctionRecipe> implements RecipeSerializer<R> {

    private static final MapCodec<ItemStack> RESULT_CODEC = RecordCodecBuilder.mapCodec(
            $$0 -> $$0.group(
                            BuiltInRegistries.ITEM.byNameCodec().fieldOf("result").forGetter(ItemStack::getItem),
                            Codec.INT.fieldOf("count").forGetter(ItemStack::getCount)
                    )
                    .apply($$0, ItemStack::new)
    );

    private final Codec<R> codec;

    public SpongeStonecuttingRecipeSerializer(SingleItemMaker<R> factory) {
        this.codec = RecordCodecBuilder.create(
                $$1 -> $$1.group(
                                ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter(SingleItemRecipe::getGroup),
                                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter($$0x -> $$0x.getIngredients().get(0)),
                                RESULT_CODEC.forGetter($$0x -> $$0x.getResultItem(null)),
                                ItemStack.CODEC.optionalFieldOf(Constants.Recipe.SPONGE_RESULT, ItemStack.EMPTY).forGetter(raw -> raw.getResultItem(null).hasTag() ? raw.getResultItem(null) : ItemStack.EMPTY),
                                IngredientResultUtil.CACHED_RESULT_FUNC_CODEC.optionalFieldOf(Constants.Recipe.SPONGE_RESULTFUNCTION, null).forGetter(ResultFunctionRecipe::resultFunctionId)
                        )
                        .apply($$1, factory::create)
        );
    }

    @Override
    public Codec<R> codec() {
        return this.codec;
    }

    @Override
    public R fromNetwork(final FriendlyByteBuf var1) {
        throw new UnsupportedOperationException("custom serializer needs client side support");
    }

    @Override
    public void toNetwork(final FriendlyByteBuf buffer, final R recipe) {
        throw new UnsupportedOperationException("custom serializer needs client side support");
    }

    public interface SingleItemMaker<T extends SingleItemRecipe> {
        default T create(String group, Ingredient ingredient, ItemStack result, ItemStack spongeResult, String resultFunction) {
            return this.create(group, ingredient, spongeResult.isEmpty() ? result : spongeResult, resultFunction);
        }
        T create(String group, Ingredient ingredient, ItemStack result, String resultFunction);
    }
}
