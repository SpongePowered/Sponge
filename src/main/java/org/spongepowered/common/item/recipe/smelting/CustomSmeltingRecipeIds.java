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
package org.spongepowered.common.item.recipe.smelting;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.recipe.smelting.SmeltingRecipe;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.IdentityHashMap;
import java.util.Map;

public final class CustomSmeltingRecipeIds {

    private static final Map<SmeltingRecipe, String> ids = new IdentityHashMap<>();
    private static final Reference2IntMap<Class<?>> counters = new Reference2IntOpenHashMap<>();

    public static String getDefaultId(SmeltingRecipe recipe) {
        return ids.computeIfAbsent(recipe, CustomSmeltingRecipeIds::loadDefaultId);
    }

    private static String loadDefaultId(SmeltingRecipe recipe) {
        final Class<?> clazz = recipe.getClass();
        final int value = counters.getInt(clazz);
        counters.put(clazz, value + 1);
        // Just take the current plugin from the cause stack, it will most likely
        // be the plugin constructing the recipe, fallback to sponge if it fails.
        final String pluginId = Sponge.getCauseStackManager().getCurrentCause()
                .first(PluginContainer.class).map(PluginContainer::getId).orElse("sponge");
        return pluginId + ":" + clazz.getName().replaceAll("[.$]", "_").toLowerCase() + "_" + value;
    }
}
