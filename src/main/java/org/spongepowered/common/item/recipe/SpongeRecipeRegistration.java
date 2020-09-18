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
package org.spongepowered.common.item.recipe;

import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.item.recipe.RecipeRegistration;
import org.spongepowered.common.item.recipe.ingredient.SpongeIngredient;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

public abstract class SpongeRecipeRegistration implements RecipeRegistration, IFinishedRecipe {

    protected final ResourceLocation key;
    protected final IRecipeSerializer<?> serializer;
    protected final ResourceLocation advancementId;
    protected final Advancement.Builder advancementBuilder = Advancement.Builder.builder();
    protected final String group;

    public static <S extends IRecipeSerializer<T>, T extends IRecipe<?>> S register(String spongeName, S recipeSerializer) {
        return (S)(Registry.<IRecipeSerializer<?>>register(Registry.RECIPE_SERIALIZER, new ResourceLocation("sponge", spongeName), recipeSerializer));
    }
    public static <S extends IRecipeSerializer<T>, T extends IRecipe<?>> S register(ResourceLocation resourceLocation, S recipeSerializer) {
        return (S)(Registry.<IRecipeSerializer<?>>register(Registry.RECIPE_SERIALIZER, resourceLocation, recipeSerializer));
    }

    public SpongeRecipeRegistration(ResourceLocation key, IRecipeSerializer<?> serializer, Item resultItem, String group) {
        this.key = key;
        this.serializer = serializer;
        final ItemGroup itemGroup = resultItem.getGroup();
        this.advancementId = new ResourceLocation(key.getNamespace(), "recipes/" + (itemGroup == null ? "uncategorized" : itemGroup.getPath()) + "/" + key.getPath());
        this.advancementBuilder
                .withCriterion("has_the_recipe", new RecipeUnlockedTrigger.Instance(key))
                .withRewards(AdvancementRewards.Builder.recipe(key));
        this.group = group == null ? "" : group;
    }

    public static <C extends IInventory> IRecipeSerializer<?> determineSerializer(ItemStack resultStack,
            Function<C, ItemStack> resultFunction,
            Function<net.minecraft.inventory.CraftingInventory, NonNullList<ItemStack>> remainingItemsFunction,
            Map<Character, Ingredient> ingredients, IRecipeSerializer<?> vanilla, IRecipeSerializer<?> sponge) {
        return SpongeRecipeRegistration.determineSerializer(resultStack, resultFunction, remainingItemsFunction, ingredients.values(), vanilla, sponge);
    }

    public static <C extends IInventory> IRecipeSerializer<?> determineSerializer(ItemStack resultStack,
            Function<C, ItemStack> resultFunction,
            Function<net.minecraft.inventory.CraftingInventory, NonNullList<ItemStack>> remainingItemsFunction,
            Collection<Ingredient> ingredients, IRecipeSerializer<?> vanilla, IRecipeSerializer<?> sponge) {
        if (resultStack.hasTag() || resultFunction != null || remainingItemsFunction != null) {
            return sponge;
        }
        for (Ingredient value : ingredients) {
            if (value instanceof SpongeIngredient) {
                return sponge;
            }
        }
        return vanilla;
    }

    @Override
    public ResourceLocation getID() {
        return this.key;
    }

    @Override
    public ResourceKey getKey() {
        return (ResourceKey) (Object) this.key;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return this.serializer;
    }

    @Override
    public JsonObject getRecipeJson() {
        final JsonObject jsonobject = new JsonObject();
        jsonobject.addProperty("type", Registry.RECIPE_SERIALIZER.getKey(this.getSerializer()).toString());
        this.serialize(jsonobject);
        return jsonobject;
    }

    @Override
    public void serialize(JsonObject json) {
        if (!this.group.isEmpty()) {
            json.addProperty("group", this.group);
        }
        this.serializeShape(json);
        this.serializeResult(json);
        this.serializeAdditional(json);
    }

    public abstract void serializeShape(JsonObject json);
    public abstract void serializeResult(JsonObject json);
    public void serializeAdditional(JsonObject json) {
    }

    @Override
    public JsonObject getAdvancementJson() {
        return advancementBuilder.serialize();
    }

    @Override
    public ResourceLocation getAdvancementID() {
        return this.advancementId;
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        try {
            return DataFormats.JSON.get().read(this.getRecipeJson().toString());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
