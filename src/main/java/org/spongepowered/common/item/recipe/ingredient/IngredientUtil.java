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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.common.accessor.world.item.crafting.IngredientAccessor;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


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
        Tag<Item> itemTag = ItemTags.getAllTags().getTag(((ResourceLocation) (Object) tagKey));
        if (itemTag == null) {
            return null;
        }
        return IngredientUtil.fromNative(Ingredient.of(itemTag));
    }

    private static net.minecraft.world.item.ItemStack[] toNativeStacks(ItemStack[] stacks) {
        return Arrays.stream(stacks).map(ItemStackUtil::toNative).toArray(net.minecraft.world.item.ItemStack[]::new);
    }

    public static org.spongepowered.api.item.recipe.crafting.Ingredient of(ItemStack... stacks) {
        final SpongeIngredient ingredient = SpongeIngredient.spongeFromStacks(IngredientUtil.toNativeStacks(stacks));
        return IngredientUtil.fromNative(ingredient);
    }

    public static org.spongepowered.api.item.recipe.crafting.Ingredient of(ResourceKey key, Predicate<ItemStack> predicate, ItemStack... stacks) {
        final SpongeIngredient ingredient = SpongeIngredient.spongeFromPredicate(key, predicate, IngredientUtil.toNativeStacks(stacks));
        return IngredientUtil.fromNative(ingredient);
    }

    public static Ingredient spongeDeserialize(JsonElement json) {
        if (json == null || json.isJsonNull()) {
            throw new JsonSyntaxException("Item cannot be null");
        }
        if (json.isJsonObject()) {
            // Detect Sponge ingredient
            final JsonObject ingredientJson = json.getAsJsonObject();
            if (ingredientJson.has(SpongeItemList.INGREDIENT_TYPE)) {

                // Sponge Ingredient
                final String type = GsonHelper.getAsString(ingredientJson, SpongeItemList.INGREDIENT_TYPE);
                switch (type) {
                    case SpongeStackItemList.TYPE_STACK:
                        return new SpongeIngredient(IngredientUtil.spongeDeserializeItemList(ingredientJson));
                    case SpongePredicateItemList.TYPE_PREDICATE:
                        return new SpongeIngredient(IngredientUtil.spongeDeserializePredicateItemList(ingredientJson));
                    default:
                        throw new JsonSyntaxException("Unknown Sponge ingredient type " + type);
                }

            } else {
                // Vanilla Ingredient
                final Ingredient.Value itemList = IngredientAccessor.invoker$valueFromJson(ingredientJson);
                return IngredientAccessor.invoker$fromValues(Stream.of(itemList));
            }
        }
        if (json.isJsonArray()) {
            JsonArray jsonarray = json.getAsJsonArray();
            if (jsonarray.size() == 0) {
                throw new JsonSyntaxException("Item array cannot be empty, at least one item must be defined");
            }
            return IngredientAccessor.invoker$fromValues(StreamSupport.stream(jsonarray.spliterator(), false).map((p_209355_0_) ->
                IngredientAccessor.invoker$valueFromJson(GsonHelper.convertToJsonObject(p_209355_0_, "item"))));
        }
        throw new JsonSyntaxException("Expected item to be object or array of objects");
    }

    private static Stream<? extends Ingredient.Value> spongeDeserializePredicateItemList(JsonObject json) {
        if (!json.has(SpongeItemList.INGREDIENT_ITEM)) {
            throw new JsonParseException("Sponge Ingredient is missing " + SpongeItemList.INGREDIENT_ITEM);
        }
        if (!json.has(SpongePredicateItemList.INGREDIENT_PREDICATE)) {
            throw new JsonParseException("Sponge Ingredient Predicate is missing " + SpongePredicateItemList.INGREDIENT_PREDICATE);
        }
        if (!json.get(SpongeItemList.INGREDIENT_ITEM).isJsonArray()) {
            throw new JsonParseException("Sponge Ingredient " + SpongeItemList.INGREDIENT_ITEM + " is not an object");
        }
        final String id = GsonHelper.getAsString(json, SpongePredicateItemList.INGREDIENT_PREDICATE);
        final Predicate<net.minecraft.world.item.ItemStack> predicate = SpongeIngredient.getCachedPredicate(id);
        if (predicate == null) {
            throw new JsonParseException("Sponge Ingredient Predicate not found: " + id);
        }
        final JsonArray jsonArray = json.getAsJsonArray(SpongeItemList.INGREDIENT_ITEM);
        return StreamSupport.stream(jsonArray.spliterator(), false)
            .map(JsonElement::getAsJsonObject)
            .map(IngredientResultUtil::deserializeItemStack)
            .map(stacks -> new SpongePredicateItemList(id, predicate, stacks));
    }

    public static Stream<Ingredient.Value> spongeDeserializeItemList(JsonObject json) {
        if (!json.has(SpongeItemList.INGREDIENT_ITEM)) {
            throw new JsonParseException("Sponge Ingredient is missing " + SpongeItemList.INGREDIENT_ITEM);
        }
        if (!json.get(SpongeItemList.INGREDIENT_ITEM).isJsonArray()) {
            throw new JsonParseException("Sponge Ingredient " + SpongeItemList.INGREDIENT_ITEM + " is not an object");
        }
        final JsonArray jsonArray = json.getAsJsonArray(SpongeItemList.INGREDIENT_ITEM);
        return StreamSupport.stream(jsonArray.spliterator(), false)
            .map(JsonElement::getAsJsonObject)
            .map(IngredientResultUtil::deserializeItemStack)
            .map(SpongeStackItemList::new);
    }


}
