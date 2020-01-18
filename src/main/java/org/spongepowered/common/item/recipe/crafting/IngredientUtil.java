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
package org.spongepowered.common.item.recipe.crafting;

import net.minecraft.item.Item;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.item.ItemType;

import java.util.Arrays;

import javax.annotation.Nullable;

public class IngredientUtil {

    public static org.spongepowered.api.item.recipe.crafting.Ingredient fromNative(Ingredient ingredient) {
        return (org.spongepowered.api.item.recipe.crafting.Ingredient) (Object) ingredient;
    }

    public static Ingredient toNative(org.spongepowered.api.item.recipe.crafting.Ingredient ingredient) {
        return (Ingredient) (Object) ingredient;

    }

    public static org.spongepowered.api.item.recipe.crafting.Ingredient of(ItemType... items) {
        IItemProvider[] providers = Arrays.stream(items).map(item -> (IItemProvider) () -> ((Item) item)).toArray(IItemProvider[]::new);
        return fromNative(Ingredient.fromItems(providers));
    }

    @Nullable
    public static org.spongepowered.api.item.recipe.crafting.Ingredient of(CatalogKey tagKey) {
        Tag<Item> itemTag = ItemTags.getCollection().get(((ResourceLocation) (Object) tagKey));
        if (itemTag == null) {
            return null;
        }
        return fromNative(Ingredient.fromTag(itemTag));
    }

}
