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
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.datapack.DataPack;
import org.spongepowered.api.item.recipe.RecipeRegistration;
import org.spongepowered.common.item.recipe.ingredient.SpongeIngredient;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

public abstract class SpongeRecipeRegistration implements RecipeRegistration, FinishedRecipe {

    protected final ResourceLocation key;
    protected final RecipeSerializer<?> serializer;
    protected final ResourceLocation advancementId;
    protected final Advancement.Builder advancementBuilder = Advancement.Builder.advancement();
    protected final String group;
    protected final DataPack<RecipeRegistration> pack;

    public SpongeRecipeRegistration(final ResourceLocation key, final RecipeSerializer<?> serializer, final Item resultItem, final String group, final DataPack<RecipeRegistration> pack, final RecipeCategory recipeCategory) {
        this.key = key;
        this.serializer = serializer;
        this.pack = pack;
        this.advancementId = new ResourceLocation(key.getNamespace(), "recipes/" + recipeCategory.getFolderName() + "/" + key.getPath());
        this.advancementBuilder
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(key))
                .rewards(AdvancementRewards.Builder.recipe(key));
        this.group = group == null ? "" : group;
    }

    public static <C extends Container> RecipeSerializer<?> determineSerializer(final ItemStack resultStack,
                                                                                  final Function<C, ItemStack> resultFunction,
                                                                                  final Function<net.minecraft.world.inventory.CraftingContainer, NonNullList<ItemStack>> remainingItemsFunction,
                                                                                  final Map<Character, Ingredient> ingredients, final RecipeSerializer<?> vanilla, final RecipeSerializer<?> sponge) {
        return SpongeRecipeRegistration.determineSerializer(resultStack, resultFunction, remainingItemsFunction, ingredients.values(), vanilla, sponge);
    }

    public static <C extends Container> RecipeSerializer<?> determineSerializer(final ItemStack resultStack,
                                                                                  final Function<C, ItemStack> resultFunction,
                                                                                  final Function<net.minecraft.world.inventory.CraftingContainer, NonNullList<ItemStack>> remainingItemsFunction,
                                                                                  final Collection<Ingredient> ingredients, final RecipeSerializer<?> vanilla, final RecipeSerializer<?> sponge) {
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
    public ResourceKey key() {
        return (ResourceKey) (Object) this.key;
    }

    @Override
    public RecipeSerializer<?> getType() {
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
    public int contentVersion() {
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
    public DataPack<RecipeRegistration> pack() {
        return this.pack;
    }

    public static JsonObject encode(RecipeRegistration template, RegistryAccess access) {
        return ((FinishedRecipe) template).serializeRecipe();
    }
}
