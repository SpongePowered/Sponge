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

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackLike;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.Arrays;
import java.util.function.Predicate;


public class IngredientUtil {

    public static org.spongepowered.api.item.recipe.crafting.Ingredient fromNative(Ingredient ingredient) {
        return (org.spongepowered.api.item.recipe.crafting.Ingredient) (Object) ingredient;
    }

    public static Ingredient toNative(org.spongepowered.api.item.recipe.crafting.Ingredient ingredient) {
        return (Ingredient) (Object) ingredient;

    }

    public static org.spongepowered.api.item.recipe.crafting.Ingredient of(ItemType... items) {
        ItemLike[] providers = Arrays.stream(items).map(item -> (ItemLike) () -> ((Item) item)).toArray(ItemLike[]::new);
        return IngredientUtil.fromNative(Ingredient.of(providers));
    }

    public static org.spongepowered.api.item.recipe.crafting.@Nullable Ingredient of(ResourceKey tagKey) {
        final TagKey<Item> key = TagKey.create(Registries.ITEM, (ResourceLocation) (Object) tagKey);
        return IngredientUtil.fromNative(Ingredient.of(key));
    }

    private static net.minecraft.world.item.ItemStack[] toNativeStacks(ItemStack[] stacks) {
        return Arrays.stream(stacks).map(ItemStackUtil::toNative).toArray(net.minecraft.world.item.ItemStack[]::new);
    }

    public static org.spongepowered.api.item.recipe.crafting.Ingredient of(ItemStack... stacks) {
        final SpongeIngredient ingredient = SpongeIngredient.spongeFromStacks(IngredientUtil.toNativeStacks(stacks));
        return IngredientUtil.fromNative(ingredient);
    }

    public static org.spongepowered.api.item.recipe.crafting.Ingredient of(ResourceKey key, Predicate<? super ItemStackLike> predicate, ItemStack... stacks) {
        final SpongeIngredient ingredient = SpongeIngredient.spongeFromPredicate(key, predicate, IngredientUtil.toNativeStacks(stacks));
        return IngredientUtil.fromNative(ingredient);
    }



}
