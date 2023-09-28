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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipeCodecs;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import org.spongepowered.common.bridge.world.item.crafting.SmithingRecipeBridge;
import org.spongepowered.common.item.recipe.cooking.ResultFunctionRecipe;
import org.spongepowered.common.item.recipe.ingredient.IngredientResultUtil;
import org.spongepowered.common.util.Constants;

public class SpongeSmithingRecipeSerializer<R extends SpongeSmithingRecipe> implements RecipeSerializer<R> {


    private final Codec<R> codec;

    public SpongeSmithingRecipeSerializer(SmithingItemMaker<R> factory) {
        this.codec = RecordCodecBuilder.create(
                $$0 -> $$0.group(
                                Ingredient.CODEC.fieldOf("template").forGetter($$0x -> ((SmithingRecipeBridge) $$0x).bridge$template()),
                                Ingredient.CODEC.fieldOf("base").forGetter($$0x -> ((SmithingRecipeBridge) $$0x).bridge$base()),
                                Ingredient.CODEC.fieldOf("addition").forGetter($$0x -> ((SmithingRecipeBridge) $$0x).bridge$addition()),
                                CraftingRecipeCodecs.ITEMSTACK_OBJECT_CODEC.fieldOf("result").forGetter($$0x -> $$0x.getResultItem(null)),
                                ItemStack.CODEC.optionalFieldOf(Constants.Recipe.SPONGE_RESULT, ItemStack.EMPTY).forGetter(raw -> raw.getResultItem(null).hasTag() ? raw.getResultItem(null) : ItemStack.EMPTY),
                                IngredientResultUtil.CACHED_RESULT_FUNC_CODEC.optionalFieldOf(Constants.Recipe.SPONGE_RESULTFUNCTION, null)
                                        .forGetter(ResultFunctionRecipe::resultFunctionId)

                        )
                        .apply($$0, factory::create)
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

    public interface SmithingItemMaker<T extends SmithingTransformRecipe> {
        default T create(Ingredient template, Ingredient base, Ingredient addition, ItemStack resultIn, ItemStack spongeResult, String resultFunctionId) {
            return this.create(template, base, addition, spongeResult.isEmpty() ? resultIn : spongeResult, resultFunctionId);
        }
        T create(Ingredient template, Ingredient base, Ingredient addition, ItemStack resultIn, String resultFunctionId);
    }
}
