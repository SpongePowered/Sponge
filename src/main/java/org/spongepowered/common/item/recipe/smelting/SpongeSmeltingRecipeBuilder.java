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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.data.CookingRecipeBuilder;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.CookingRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.recipe.RecipeType;
import org.spongepowered.api.item.recipe.smelting.SmeltingRecipe;
import org.spongepowered.common.util.SpongeCatalogBuilder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SpongeSmeltingRecipeBuilder extends SpongeCatalogBuilder<SmeltingRecipe, SmeltingRecipe.Builder>
        implements SmeltingRecipe.Builder.ResultStep, SmeltingRecipe.Builder.IngredientStep, SmeltingRecipe.Builder.EndStep {

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();

    private IRecipeType type;
    private Ingredient ingredient;
    private Item result;
    @Nullable private Float experience;
    @Nullable private Integer smeltTime;
    @Nullable private String group;

    @Override
    public ResultStep ingredient(org.spongepowered.api.item.recipe.crafting.Ingredient ingredient) {
        this.ingredient = (Ingredient) (Object) ingredient;
        return this;
    }

    @Override
    public SmeltingRecipe.Builder reset() {
        super.reset();
        this.type = null;
        this.ingredient = null;
        this.result = null;
        this.experience = null;
        this.smeltTime = null;
        this.group = null;
        return this;
    }

    @Override
    public EndStep result(ItemType result) {
        this.result = (Item) result;
        return this;
    }

    @Override
    public EndStep experience(double experience) {
        checkState(experience >= 0, "The experience must be non-negative.");
        this.experience = (float) experience;
        return this;
    }

    @Override
    public EndStep smeltTime(int ticks) {
        this.smeltTime = ticks;
        return this;
    }

    @Override
    public IngredientStep type(RecipeType<SmeltingRecipe> type) {
        this.type = (IRecipeType) type;
        return this;
    }

    // TODO vanilla does not use groups for cooking recipes @Override
    public EndStep group(String group) {
        this.group = group;
        return this;
    }

    @Override
    protected SmeltingRecipe build(CatalogKey key) {
        checkNotNull(this.type);
        checkNotNull(this.ingredient);
        checkNotNull(this.result);
        checkNotNull(key);
        this.key = key;

        if (this.experience == null) {
            this.experience = 0f;
        }

        CookingRecipeBuilder builder = null;
        if (this.type == IRecipeType.BLASTING) {
            if (this.smeltTime == null) {
                this.smeltTime = 100;
            }
            builder = CookingRecipeBuilder.blastingRecipe(this.ingredient, () -> this.result,
                    this.experience,
                    this.smeltTime);
        } else if (this.type == IRecipeType.CAMPFIRE_COOKING) {
            if (this.smeltTime == null) {
                this.smeltTime = 600;
            }
            builder = CookingRecipeBuilder.cookingRecipe(this.ingredient, () -> this.result,
                    this.experience, this.smeltTime,
                    CookingRecipeSerializer.CAMPFIRE_COOKING);
        } else if (this.type == IRecipeType.SMOKING) {
            if (this.smeltTime == null) {
                this.smeltTime = 100;
            }
            builder = CookingRecipeBuilder.cookingRecipe(this.ingredient, () -> this.result,
                    this.experience, this.smeltTime,
                    CookingRecipeSerializer.SMOKING);
        } else if (this.type == IRecipeType.SMELTING) {
            if (this.smeltTime == null) {
                this.smeltTime = 200;
            }
            builder = CookingRecipeBuilder.smeltingRecipe(this.ingredient, () -> this.result,
                    this.experience, this.smeltTime);
        }

        // TODO criterions
        // TODO groups
        // TODO custom CookingRecipeSerializers

        ResourceLocation resourceLocation = (ResourceLocation) (Object) this.key;
        if (builder != null) {
            builder.build(this::save, resourceLocation);
        }

        MinecraftServer server = (MinecraftServer) Sponge.getServer();
        server.reload();
        return (SmeltingRecipe) server.getRecipeManager().getRecipe(resourceLocation).get();
    }

    private void save(IFinishedRecipe recipe) {
        Path path = null; // TODO get basepath

        this.saveToFile(recipe.getRecipeJson(),
                path.resolve("data/" + recipe.getID().getNamespace() + "/recipes/" + recipe.getID().getPath() + ".json"));
        JsonObject jsonobject = recipe.getAdvancementJson();
        if (jsonobject != null) {
            this.saveToFile(jsonobject,
                    path.resolve("data/" + recipe.getID().getNamespace() + "/advancements/" + recipe.getAdvancementID().getPath() + ".json"));
        }
    }

    private void saveToFile(JsonObject json, Path pathIn) {
        try {
            String s = GSON.toJson(json);
            Files.createDirectories(pathIn.getParent());
            try (BufferedWriter bufferedwriter = Files.newBufferedWriter(pathIn)) {
                bufferedwriter.write(s);
            }
        } catch (IOException e) {
           throw new IllegalStateException(e);
        }

    }

}
