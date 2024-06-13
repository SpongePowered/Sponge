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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.datapack.DataPack;
import org.spongepowered.api.item.recipe.RecipeRegistration;
import org.spongepowered.common.item.recipe.ingredient.IngredientResultUtil;
import org.spongepowered.common.item.recipe.ingredient.SpongeIngredient;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Function;

public abstract class SpongeRecipeRegistration<R extends Recipe<? extends RecipeInput>> implements RecipeRegistration {

    private static final Gson GSON = new Gson();

    protected final ResourceLocation key;
    protected final RecipeSerializer<? extends R> serializer;
    protected final AdvancementHolder advancement;
    protected final String group;
    protected final DataPack<RecipeRegistration> pack;

    public SpongeRecipeRegistration(final ResourceLocation key,
            final String group, final DataPack<RecipeRegistration> pack, final RecipeCategory recipeCategory,
            final RecipeSerializer<? extends R> serializer) {
        this.key = key;
        this.serializer = serializer;
        this.pack = pack;
        this.advancement = Advancement.Builder.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(key))
                .rewards(AdvancementRewards.Builder.recipe(key))
                .build(ResourceLocation.fromNamespaceAndPath(key.getNamespace(), "recipes/" + recipeCategory.getFolderName() + "/" + key.getPath()));
        this.group = group == null ? "" : group;
    }

    public static <R extends Recipe<I>, I extends RecipeInput> RecipeSerializer<? extends R> determineSerializer(final ItemStack resultStack,
            final Function<I, ItemStack> resultFunction,
            final Function<net.minecraft.world.inventory.CraftingContainer, NonNullList<ItemStack>> remainingItemsFunction,
            final Collection<Ingredient> ingredients,
            final RecipeSerializer<R> vanilla, final RecipeSerializer<? extends R> sponge) {
        if (!resultStack.getComponents().isEmpty() || resultFunction != null || remainingItemsFunction != null) {
            return sponge;
        }
        for (final Ingredient value : ingredients) {
            if (value instanceof SpongeIngredient) {
                return sponge;
            }
        }
        return vanilla;
    }

    public static <I extends RecipeInput> boolean isVanillaSerializer(final ItemStack resultStack,
            final Function<I, ItemStack> resultFunction,
            final Function<I, NonNullList<ItemStack>> remainingItemsFunction,
            final Collection<Ingredient> ingredients) {
        if (!resultStack.getComponents().isEmpty() || resultFunction != null || remainingItemsFunction != null) {
            return false;
        }
        for (final Ingredient value : ingredients) {
            if (value instanceof SpongeIngredient) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ResourceKey key() {
        return (ResourceKey) (Object) this.key;
    }

    @Override
    public int contentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        var json = Recipe.CODEC.encodeStart(JsonOps.INSTANCE, (Recipe<?>) this.recipe()).result().get();
        try {
            return DataFormats.JSON.get().read(GSON.toJson(json)); // TODO serialize or get DataContainer without serializing
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public DataPack<RecipeRegistration> pack() {
        return this.pack;
    }

    @SuppressWarnings("unchecked")
    protected void ensureCached() {
        if (this instanceof SpongeRecipeRegistration.ResultFunctionRegistration<?> rfr) {
            IngredientResultUtil.cacheResultFunction(this.key, (Function) rfr.resultFunction());
        }
        if (this instanceof SpongeRecipeRegistration.RemainingItemsFunctionRegistration<?> rifr) {
            IngredientResultUtil.cacheRemainingItemsFunction(this.key, (Function) rifr.remainingItems());
        }
    }

    public AdvancementHolder advancement() {
        return advancement;
    }

    public static JsonObject encode(RecipeRegistration template, RegistryAccess access) {
        try {
            if (template instanceof SpongeRecipeRegistration<?> srr) {
                srr.ensureCached();
            }
            final var ops = RegistryOps.create(JsonOps.INSTANCE, access);
            final DataResult<JsonElement> encoded = Recipe.CODEC.encodeStart(ops, (Recipe<?>) template.recipe());
            if (encoded.result().isPresent()) {
                return encoded.result().get().getAsJsonObject();
            }
            final var error = encoded.error().get();
            throw new RuntimeException(error.message());
        } catch (Exception e) {
            throw new RuntimeException("Could not encode recipe " + template.key(), e);
        }
    }

    public interface ResultFunctionRegistration<I extends RecipeInput> {

        Function<I, net.minecraft.world.item.ItemStack> resultFunction();
    }

    public interface RemainingItemsFunctionRegistration<I extends RecipeInput> {

        Function<I, NonNullList<net.minecraft.world.item.ItemStack>> remainingItems();
    }
}
