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
import org.spongepowered.api.datapack.DataPackType;
import org.spongepowered.api.datapack.DataPackTypes;
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
    protected final Advancement.Builder advancementBuilder = Advancement.Builder.advancement();
    protected final String group;

    public static <S extends IRecipeSerializer<T>, T extends IRecipe<?>> S register(final String spongeName, final S recipeSerializer) {
        return (S)(Registry.<IRecipeSerializer<?>>register(Registry.RECIPE_SERIALIZER, new ResourceLocation("sponge", spongeName).toString(), recipeSerializer));
    }
    public static <S extends IRecipeSerializer<T>, T extends IRecipe<?>> S register(final ResourceLocation resourceLocation, final S recipeSerializer) {
        return (S)(Registry.<IRecipeSerializer<?>>register(Registry.RECIPE_SERIALIZER, resourceLocation.toString(), recipeSerializer));
    }

    public SpongeRecipeRegistration(final ResourceLocation key, final IRecipeSerializer<?> serializer, final Item resultItem, final String group) {
        this.key = key;
        this.serializer = serializer;
        final ItemGroup itemGroup = resultItem.getItemCategory();
        this.advancementId = new ResourceLocation(key.getNamespace(), "recipes/" + (itemGroup == null ? "uncategorized" : itemGroup.getRecipeFolderName()) + "/" + key.getPath());
        this.advancementBuilder
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(key))
                .rewards(AdvancementRewards.Builder.recipe(key));
        this.group = group == null ? "" : group;
    }

    public static <C extends IInventory> IRecipeSerializer<?> determineSerializer(final ItemStack resultStack,
                                                                                  final Function<C, ItemStack> resultFunction,
                                                                                  final Function<net.minecraft.inventory.CraftingInventory, NonNullList<ItemStack>> remainingItemsFunction,
                                                                                  final Map<Character, Ingredient> ingredients, final IRecipeSerializer<?> vanilla, final IRecipeSerializer<?> sponge) {
        return SpongeRecipeRegistration.determineSerializer(resultStack, resultFunction, remainingItemsFunction, ingredients.values(), vanilla, sponge);
    }

    public static <C extends IInventory> IRecipeSerializer<?> determineSerializer(final ItemStack resultStack,
                                                                                  final Function<C, ItemStack> resultFunction,
                                                                                  final Function<net.minecraft.inventory.CraftingInventory, NonNullList<ItemStack>> remainingItemsFunction,
                                                                                  final Collection<Ingredient> ingredients, final IRecipeSerializer<?> vanilla, final IRecipeSerializer<?> sponge) {
        if (resultStack.hasTag() || resultFunction != null || remainingItemsFunction != null) {
            return sponge;
        }
        for (final Ingredient value : ingredients) {
            if (value instanceof SpongeIngredient) {
                return sponge;
            }
        }
        return vanilla;
    }

    @Override
    public ResourceLocation getId() {
        return this.key;
    }

    @Override
    public ResourceKey getKey() {
        return (ResourceKey) (Object) this.key;
    }

    @Override
    public IRecipeSerializer<?> getType() {
        return this.serializer;
    }

    @Override
    public void serializeRecipeData(final JsonObject json) {
        if (!this.group.isEmpty()) {
            json.addProperty("group", this.group);
        }
        this.serializeShape(json);
        this.serializeResult(json);
        this.serializeAdditional(json);
    }

    public abstract void serializeShape(JsonObject json);
    public abstract void serializeResult(JsonObject json);
    public void serializeAdditional(final JsonObject json) {
    }

    @Override
    public JsonObject serializeAdvancement() {
        return this.advancementBuilder.serializeToJson();
    }

    @Override
    public ResourceLocation getAdvancementId() {
        return this.advancementId;
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        try {
            return DataFormats.JSON.get().read(this.serializeRecipe().toString());
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public DataPackType type() {
        return DataPackTypes.RECIPE;
    }
}
