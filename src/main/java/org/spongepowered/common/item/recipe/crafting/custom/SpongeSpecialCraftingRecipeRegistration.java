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
package org.spongepowered.common.item.recipe.crafting.custom;

import com.google.gson.JsonObject;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.crafting.CraftingGridInventory;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.item.recipe.SpongeRecipeRegistration;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class SpongeSpecialCraftingRecipeRegistration extends SpongeRecipeRegistration {

    private final BiPredicate<CraftingGridInventory, ServerWorld> biPredicate;
    private final Function<CraftingGridInventory, List<ItemStack>> remainingItemsFunction;
    private final Function<CraftingGridInventory, ItemStack> resultFunction;

    private final SpecialRecipeSerializer<?> serializer;
    private final SpongeSpecialRecipe recipe;

    public SpongeSpecialCraftingRecipeRegistration(ResourceLocation key,
            BiPredicate<CraftingGridInventory, ServerWorld> biPredicate,
            Function<CraftingGridInventory, List<ItemStack>> remainingItemsFunction,
            Function<CraftingGridInventory, ItemStack> resultFunction) {
        super(key, null, Items.AIR, "");

        this.biPredicate = biPredicate;
        this.remainingItemsFunction = remainingItemsFunction;
        this.resultFunction = resultFunction;

        this.recipe = new SpongeSpecialRecipe(key, this.biPredicate, this.remainingItemsFunction, this.resultFunction);
        this.serializer = SpongeRecipeRegistration.register(key, new SpecialRecipeSerializer<>(rl -> this.recipe));
    }

    @Override
    public IRecipeSerializer<?> getType() {
        return this.serializer;
    }

    @Override
    public void serializeShape(JsonObject json) {
    }

    @Override
    public void serializeResult(JsonObject json) {
    }
}
