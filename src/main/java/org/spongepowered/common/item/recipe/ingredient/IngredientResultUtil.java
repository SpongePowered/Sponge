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
package org.spongepowered.common.item.recipe.ingredient;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeInput;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.common.SpongeCommon;

import java.text.MessageFormat;
import java.util.Map;
import java.util.function.Function;

public final class IngredientResultUtil {

    private static final Map<String, Function<?, net.minecraft.world.item.ItemStack>> cachedResultFunctions = new Object2ObjectOpenHashMap<>();
    private static final Map<String, Function<?, NonNullList<net.minecraft.world.item.ItemStack>>> cachedRemainingItemsFunctions = new Object2ObjectOpenHashMap<>();

    public static JsonElement serializeItemStack(final net.minecraft.world.item.ItemStack spongeResult) {
        final DataResult<JsonElement> encoded = net.minecraft.world.item.ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, spongeResult);
        return encoded.result().get();

    }
    public static final Codec<String> CACHED_RESULT_FUNC_CODEC = Codec.STRING.flatXmap(
            id -> IngredientResultUtil.cachedResultFunction(id) != null ? DataResult.success(id) :
                    DataResult.error(() -> "Missing Result Function for id " + id),
            DataResult::success
    );

    @Nullable
    @SuppressWarnings("unchecked")
    public static <I extends RecipeInput> Function<I, net.minecraft.world.item.ItemStack> cachedResultFunction(String id) {
        return (Function<I, net.minecraft.world.item.ItemStack>) IngredientResultUtil.cachedResultFunctions.get(id);
    }


    public static <C extends Container> String cacheResultFunction(ResourceLocation id,
            Function<C, net.minecraft.world.item.ItemStack> resultFunction) {
        if (IngredientResultUtil.cachedResultFunctions.put(id.toString(), resultFunction) != null) {
            SpongeCommon.logger().warn(MessageFormat.format(
                    "Duplicate cache result registration! " + id + " was replaced.",
                    new Object[]{}
            ));
        }
        return id.toString();
    }

    public static final Codec<String> CACHED_REMAINING_FUNC_CODEC = Codec.STRING.flatXmap(
            id -> IngredientResultUtil.cachedRemainingItemsFunction(id) != null ? DataResult.success(id) :
                    DataResult.error(() -> "Missing Result Function for id " + id),
            DataResult::success
    );

    @Nullable
    @SuppressWarnings("unchecked")
    public static <I extends CraftingInput> Function<I, NonNullList<net.minecraft.world.item.ItemStack>> cachedRemainingItemsFunction(String id) {
        return (Function<I, NonNullList<net.minecraft.world.item.ItemStack>>) IngredientResultUtil.cachedRemainingItemsFunctions.get(id);
    }


    public static <C extends Container> String cacheRemainingItemsFunction(ResourceLocation id,
            Function<C, NonNullList<net.minecraft.world.item.ItemStack>> resultFunction) {
        if (IngredientResultUtil.cachedRemainingItemsFunctions.put(id.toString(), resultFunction) != null) {
            SpongeCommon.logger().warn(MessageFormat.format(
                    "Duplicate cache result registration! " + id + " was replaced.",
                    new Object[]{}
            ));
        }
        return id.toString();
    }

    public static void clearCache() {
        IngredientResultUtil.cachedResultFunctions.clear();
        IngredientResultUtil.cachedRemainingItemsFunctions.clear();
    }
}
