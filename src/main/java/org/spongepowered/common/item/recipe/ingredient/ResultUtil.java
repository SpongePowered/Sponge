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
import com.google.gson.JsonObject;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.SpongeHooks;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ResultUtil {

    public static net.minecraft.item.ItemStack deserializeItemStack(JsonObject result) {
        if (result == null) {
            return null;
        }
        try {
            final DataContainer dataContainer = DataFormats.JSON.get().read(result.toString());
            return ItemStackUtil.toNative(ItemStack.builder().fromContainer(dataContainer).build());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static JsonElement serializeItemStack(net.minecraft.item.ItemStack spongeResult) {
        final DataContainer dataContainer = ItemStackUtil.fromNative(spongeResult).toContainer();
        try {
            return JSONUtils.fromJson(DataFormats.JSON.get().write(dataContainer));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static final Map<String, Function<?, net.minecraft.item.ItemStack>> cachedResultFunctions = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <C extends IInventory> Function<C, net.minecraft.item.ItemStack> deserializeResultFunction(JsonObject json) {
        if (json.has(Constants.Recipe.SPONGE_RESULTFUNCTION)) {
            final String id = JSONUtils.getString(json, Constants.Recipe.SPONGE_RESULTFUNCTION);
            return ((Function<C, net.minecraft.item.ItemStack>) ResultUtil.cachedResultFunctions.get(id));
        }
        return null;
    }

    public static <C extends IInventory> String cacheResultFunction(ResourceLocation id, Function<C, net.minecraft.item.ItemStack> resultFunction) {
        if (ResultUtil.cachedResultFunctions.put(id.toString(), resultFunction) != null) {
            SpongeHooks.logWarning("Duplicate cache result registration! " + id.toString() + " was replaced.");
        }
        return id.toString();
    }

    private static final Map<String, Function<?, NonNullList<net.minecraft.item.ItemStack>>> cachedRemainingItemsFunctions = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <C extends IInventory> Function<C, NonNullList<net.minecraft.item.ItemStack>> deserializeRemainingItemsFunction(JsonObject json) {
        if (json.has(Constants.Recipe.SPONGE_REMAINING_ITEMS)) {
            final String id = JSONUtils.getString(json, Constants.Recipe.SPONGE_REMAINING_ITEMS);
            return ((Function<C, NonNullList<net.minecraft.item.ItemStack>>) ResultUtil.cachedRemainingItemsFunctions.get(id));
        }
        return null;
    }

    public static <C extends IInventory> String cacheRemainingItemsFunction(ResourceLocation id, Function<C, NonNullList<net.minecraft.item.ItemStack>> resultFunction) {
        if (ResultUtil.cachedRemainingItemsFunctions.put(id.toString(), resultFunction) != null) {
            SpongeHooks.logWarning("Duplicate cache result registration! " + id.toString() + " was replaced.");
        }
        return id.toString();
    }

    public static void clearCache() {
        ResultUtil.cachedResultFunctions.clear();
        ResultUtil.cachedRemainingItemsFunctions.clear();
    }
}
