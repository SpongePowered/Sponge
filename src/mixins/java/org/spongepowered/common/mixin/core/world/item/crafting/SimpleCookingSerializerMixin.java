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
package org.spongepowered.common.mixin.core.world.item.crafting;

import static org.spongepowered.common.util.Constants.Recipe.SPONGE_TYPE;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SimpleCookingSerializer;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.world.item.crafting.RecipeResultBridge;
import org.spongepowered.common.item.recipe.ResultFunctionRecipe;
import org.spongepowered.common.item.recipe.cooking.CookingRecipeFactory;
import org.spongepowered.common.item.recipe.cooking.SpongeBlastingRecipe;
import org.spongepowered.common.item.recipe.cooking.SpongeCampfireCookingRecipe;
import org.spongepowered.common.item.recipe.cooking.SpongeSmeltingRecipe;
import org.spongepowered.common.item.recipe.cooking.SpongeSmokingRecipe;
import org.spongepowered.common.util.Constants;

import java.util.function.Function;

@Mixin(SimpleCookingSerializer.class)
public abstract class SimpleCookingSerializerMixin<T extends AbstractCookingRecipe> {

    @Redirect(method = "<init>", at = @At(value = "INVOKE",
            target = "Lcom/mojang/serialization/codecs/RecordCodecBuilder;mapCodec(Ljava/util/function/Function;)Lcom/mojang/serialization/MapCodec;"))
    private MapCodec<T> impl$onCreateCodec(
            final Function<RecordCodecBuilder.Instance<T>, ? extends App<RecordCodecBuilder.Mu<T>, T>> builder,
            final AbstractCookingRecipe.Factory<T> $$0,
            final int defaultCookingTime) {
        final var mcMapCodec = RecordCodecBuilder.mapCodec(builder);

        final T constructed = $$0.create(null, null, null, null, 0, 0);
        if (constructed instanceof BlastingRecipe) {
            var spongeCodec = SimpleCookingSerializerMixin.impl$buildCodec(SpongeBlastingRecipe::new, defaultCookingTime);
            return Codec.mapEither(spongeCodec, mcMapCodec).xmap(to -> to.map(si -> (T) si, i -> i),
                    fr -> {
                        if (fr instanceof SpongeBlastingRecipe si) {
                            return Either.left(si);
                        }
                        return Either.right(fr);
                    });
        }

        if (constructed instanceof CampfireCookingRecipe) {
            var spongeCodec = SimpleCookingSerializerMixin.impl$buildCodec(SpongeCampfireCookingRecipe::new, defaultCookingTime);
            return Codec.mapEither(spongeCodec, mcMapCodec).xmap(to -> to.map(si -> (T) si, i -> i),
                    fr -> {
                        if (fr instanceof SpongeCampfireCookingRecipe si) {
                            return Either.left(si);
                        }
                        return Either.right(fr);
                    });
        }


        if (constructed instanceof SmeltingRecipe) {
            var spongeCodec = SimpleCookingSerializerMixin.impl$buildCodec(SpongeSmeltingRecipe::new, defaultCookingTime);
            return Codec.mapEither(spongeCodec, mcMapCodec).xmap(to -> to.map(si -> (T) si, i -> i),
                    fr -> {
                        if (fr instanceof SpongeSmeltingRecipe si) {
                            return Either.left(si);
                        }
                        return Either.right(fr);
                    });
        }



        if (constructed instanceof SmokingRecipe) {
            var spongeCodec = SimpleCookingSerializerMixin.impl$buildCodec(SpongeSmokingRecipe::new, defaultCookingTime);
            return Codec.mapEither(spongeCodec, mcMapCodec).xmap(to -> to.map(si -> (T) si, i -> i),
                    fr -> {
                        if (fr instanceof SpongeSmokingRecipe si) {
                            return Either.left(si);
                        }
                        return Either.right(fr);
                    });
        }

        // TODO no sponge handling?
        return mcMapCodec;
    }

   private static <S extends AbstractCookingRecipe & ResultFunctionRecipe> MapCodec<S> impl$buildCodec(CookingRecipeFactory<S> factory, int defaultCookingTime)
   {
       return RecordCodecBuilder.mapCodec(
               $$2 -> $$2.group(
                               Codec.STRING.fieldOf(SPONGE_TYPE).forGetter(a -> "custom"),
                               Codec.STRING.optionalFieldOf("group", "").forGetter(AbstractCookingRecipe::getGroup),
                               CookingBookCategory.CODEC.fieldOf("category").orElse(CookingBookCategory.MISC).forGetter(AbstractCookingRecipe::category),
                               Ingredient.CODEC_NONEMPTY.fieldOf(Constants.Recipe.COOKING_INGREDIENT).forGetter($$0x -> $$0x.getIngredients().get(0)),
                               BuiltInRegistries.ITEM.byNameCodec().xmap(ItemStack::new, ItemStack::getItem).fieldOf(Constants.Recipe.RESULT).forGetter($$0x -> ((RecipeResultBridge)$$0x).bridge$result()),
                               Codec.FLOAT.fieldOf(Constants.Recipe.COOKING_EXP).orElse(0.0F).forGetter(AbstractCookingRecipe::getExperience),
                               Codec.INT.fieldOf(Constants.Recipe.COOKING_TIME).orElse(defaultCookingTime).forGetter(AbstractCookingRecipe::getCookingTime),
                               ItemStack.CODEC.optionalFieldOf(Constants.Recipe.SPONGE_RESULT, ItemStack.EMPTY).forGetter($$0x -> ((RecipeResultBridge)$$0x).bridge$spongeResult()),
                               Codec.STRING.optionalFieldOf(Constants.Recipe.SPONGE_RESULTFUNCTION).forGetter(ResultFunctionRecipe::resultFunctionId)
                       )
                       .apply($$2, factory::create)
       );
   }
}
