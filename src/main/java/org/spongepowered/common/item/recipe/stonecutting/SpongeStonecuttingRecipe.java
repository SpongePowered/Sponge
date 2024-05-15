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

import static org.spongepowered.common.util.Constants.Recipe.SPONGE_TYPE;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import org.spongepowered.common.bridge.world.item.crafting.RecipeResultBridge;
import org.spongepowered.common.item.recipe.ResultFunctionRecipe;
import org.spongepowered.common.item.recipe.ingredient.IngredientResultUtil;
import org.spongepowered.common.util.Constants;

import java.util.Optional;


public class SpongeStonecuttingRecipe extends StonecutterRecipe implements ResultFunctionRecipe {

    private static final MapCodec<ItemStack> RESULT_CODEC = RecordCodecBuilder.mapCodec(
            $$0 -> $$0.group(
                            BuiltInRegistries.ITEM.byNameCodec().fieldOf(Constants.Recipe.RESULT).forGetter(ItemStack::getItem),
                            Codec.INT.fieldOf(Constants.Recipe.COUNT).forGetter(ItemStack::getCount)
                    )
                    .apply($$0, ItemStack::new)
    );

    public static final MapCodec<SpongeStonecuttingRecipe> SPONGE_CODEC = RecordCodecBuilder.mapCodec(
            $$1 -> $$1.group(
                            Codec.STRING.fieldOf(SPONGE_TYPE).forGetter(a -> "custom"),
                            Codec.STRING.optionalFieldOf("group", "").forGetter(SingleItemRecipe::getGroup),
                            Ingredient.CODEC_NONEMPTY.fieldOf(Constants.Recipe.STONECUTTING_INGREDIENT).forGetter($$0x -> $$0x.getIngredients().get(0)),
                            RESULT_CODEC.forGetter($$0x -> ((RecipeResultBridge)$$0x).bridge$result()),
                            ItemStack.CODEC.optionalFieldOf(Constants.Recipe.SPONGE_RESULT, ItemStack.EMPTY).forGetter($$0x -> ((RecipeResultBridge)$$0x).bridge$spongeResult()),
                            IngredientResultUtil.CACHED_RESULT_FUNC_CODEC.optionalFieldOf(Constants.Recipe.SPONGE_RESULTFUNCTION).forGetter(ResultFunctionRecipe::resultFunctionId)
                    )
                    .apply($$1, SpongeStonecuttingRecipe::of)
    );

    private final String resultFunctionId;

    public static SpongeStonecuttingRecipe of(final String spongeType,
            final String groupIn,
            final Ingredient ingredientIn,
            final ItemStack resultIn,
            final ItemStack spongeResult,
            final Optional<String> resultFunctionId) {
        return new SpongeStonecuttingRecipe(groupIn, ingredientIn, spongeResult.isEmpty() ? resultIn : spongeResult, resultFunctionId.orElse(null));
    }

    public SpongeStonecuttingRecipe(final String groupIn, final Ingredient ingredientIn, final ItemStack spongeResult, final String resultFunctionId) {
        super(groupIn, ingredientIn, spongeResult);
        this.resultFunctionId = resultFunctionId;
    }

    @Override
    public Optional<String> resultFunctionId() {
        return Optional.ofNullable(this.resultFunctionId);
    }

    @Override
    public ItemStack assemble(final Container container, final HolderLookup.Provider $$1) {
        if (this.resultFunctionId != null) {
            return IngredientResultUtil.cachedResultFunction(this.resultFunctionId).apply(container);
        }
        return super.assemble(container, $$1);
    }

    @Override
    public ItemStack getResultItem(final HolderLookup.Provider $$1) {
//        if (this.resultFunctionId != null) {
//            return ItemStack.EMPTY;
//        }
        return super.getResultItem($$1);
    }

}
