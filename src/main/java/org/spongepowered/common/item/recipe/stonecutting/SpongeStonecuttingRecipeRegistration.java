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
package org.spongepowered.common.item.recipe.stonecutting;

import com.google.gson.JsonObject;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import org.spongepowered.common.item.recipe.SpongeRecipeRegistration;
import org.spongepowered.common.item.recipe.ingredient.ResultUtil;
import org.spongepowered.common.util.Constants;

import java.util.function.Function;

public class SpongeStonecuttingRecipeRegistration extends SpongeRecipeRegistration {

    // Vanilla Recipe
    private final Ingredient ingredient;
    private final Item result;
    private final int count;

    // Sponge Recipe
    private final ItemStack spongeResult;
    private Function<IInventory, ItemStack> resultFunction;

    public SpongeStonecuttingRecipeRegistration(ResourceLocation key, IRecipeSerializer<?> serializer, String group, Ingredient ingredient,
            ItemStack spongeResult, Function<IInventory, ItemStack> resultFunction) {
        super(key, serializer, spongeResult.getItem(), group);
        this.ingredient = ingredient;
        this.result = spongeResult.getItem();
        this.count = spongeResult.getCount();
        this.spongeResult = spongeResult;
        this.resultFunction = resultFunction;
    }

    @Override
    public void serializeShape(JsonObject json) {
        json.add(Constants.Recipe.STONECUTTING_INGREDIENT, this.ingredient.serialize());
    }

    @Override
    public void serializeResult(JsonObject json) {
        json.addProperty(Constants.Recipe.RESULT, Registry.ITEM.getKey(this.result).toString());
        json.addProperty(Constants.Recipe.COUNT, this.count);

        if (this.spongeResult != null) {
            json.add(Constants.Recipe.SPONGE_RESULT, ResultUtil.serializeItemStack(this.spongeResult));
        }
        if (this.resultFunction != null) {
            json.addProperty(Constants.Recipe.SPONGE_RESULTFUNCTION, ResultUtil.cacheResultFunction(this.getID(), this.resultFunction));
        }
    }
}
