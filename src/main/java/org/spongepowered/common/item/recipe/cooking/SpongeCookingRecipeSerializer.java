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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.spongepowered.common.util.Constants;

// Custom Serializer with support for:
// result full ItemStack instead of ItemType+Count
// ingredient itemstacks
public class SpongeCookingRecipeSerializer<R extends AbstractCookingRecipe & ResultFunctionRecipe> implements RecipeSerializer<R> {

    private CookingRecipeFactory<R> factory;
    private Codec<R> codec;
    private final int defaultCookingTime;

    public SpongeCookingRecipeSerializer(final CookingRecipeFactory<R> factory, final int defaultCookingTime) {
        this.defaultCookingTime = defaultCookingTime;
        this.factory = factory;
        this.codec = RecordCodecBuilder.create(
                $$2 -> $$2.group(
                                ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter(AbstractCookingRecipe::getGroup),
                                CookingBookCategory.CODEC.fieldOf("category").orElse(CookingBookCategory.MISC).forGetter(AbstractCookingRecipe::category),
                                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter($$0x -> $$0x.getIngredients().get(0)),
                                BuiltInRegistries.ITEM.byNameCodec().xmap(ItemStack::new, ItemStack::getItem).fieldOf("result")
                                        .forGetter($$0x -> $$0x.getResultItem(null)),
                                Codec.FLOAT.fieldOf("experience").orElse(0.0F).forGetter(AbstractCookingRecipe::getExperience),
                                Codec.INT.fieldOf("cookingtime").orElse(defaultCookingTime).forGetter(AbstractCookingRecipe::getCookingTime),
                                ItemStack.CODEC.optionalFieldOf(Constants.Recipe.SPONGE_RESULT, ItemStack.EMPTY).forGetter(raw -> raw.getResultItem(null).hasTag() ? raw.getResultItem(null) : ItemStack.EMPTY),
                                Codec.STRING.optionalFieldOf(Constants.Recipe.SPONGE_RESULTFUNCTION, null).forGetter(ResultFunctionRecipe::resultFunctionId)
                        )
                        .apply($$2, factory::create)
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

    interface CookingRecipeFactory<T extends AbstractCookingRecipe> {
        default T create(String group, CookingBookCategory category, Ingredient ingredient, ItemStack result, float experience, int cookingTime,
                ItemStack spongeResult, final String resultFunctionId) {
            return this.create(group, category, ingredient, spongeResult.isEmpty() ? result : spongeResult, experience, cookingTime, resultFunctionId);
        }

        T create(String group, CookingBookCategory category, Ingredient ingredient, ItemStack result, float experience, int cookingTime,
                final String resultFunctionId);
    }

}
