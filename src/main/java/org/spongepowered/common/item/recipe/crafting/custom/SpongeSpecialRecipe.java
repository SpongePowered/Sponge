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

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class SpongeSpecialRecipe extends SpecialRecipe {

    private final BiPredicate<org.spongepowered.api.item.inventory.crafting.CraftingInventory, org.spongepowered.api.world.World> biPredicate;
    private final Function<org.spongepowered.api.item.inventory.crafting.CraftingInventory, List<org.spongepowered.api.item.inventory.ItemStack>> remainingItemsFunction;
    private final Function<org.spongepowered.api.item.inventory.crafting.CraftingInventory, org.spongepowered.api.item.inventory.ItemStack> resultFunction;

    public static <S extends IRecipeSerializer<T>, T extends IRecipe<?>> S register(ResourceLocation resourceLocation, S recipeSerializer) {
        return (S)(Registry.<IRecipeSerializer<?>>register(Registry.RECIPE_SERIALIZER, resourceLocation, recipeSerializer));
    }

    public SpongeSpecialRecipe(ResourceLocation idIn,
            BiPredicate<org.spongepowered.api.item.inventory.crafting.CraftingInventory, org.spongepowered.api.world.World> biPredicate,
            Function<org.spongepowered.api.item.inventory.crafting.CraftingInventory, List<org.spongepowered.api.item.inventory.ItemStack>> remainingItemsFunction,
            Function<org.spongepowered.api.item.inventory.crafting.CraftingInventory, org.spongepowered.api.item.inventory.ItemStack> resultFunction) {
        super(idIn);
        this.biPredicate = biPredicate;
        this.remainingItemsFunction = remainingItemsFunction;
        this.resultFunction = resultFunction;
    }

    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {
        return this.biPredicate.test(
                (org.spongepowered.api.item.inventory.crafting.CraftingInventory) inv,
                (org.spongepowered.api.world.World) worldIn);
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        org.spongepowered.api.item.inventory.ItemStack spongeStack =
                this.resultFunction.apply((org.spongepowered.api.item.inventory.crafting.CraftingInventory) inv);
        return ItemStackUtil.toNative(spongeStack);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
        if (this.remainingItemsFunction == null) {
            return super.getRemainingItems(inv);
        }
        List<org.spongepowered.api.item.inventory.ItemStack> remainingSponge =
                this.remainingItemsFunction.apply((org.spongepowered.api.item.inventory.crafting.CraftingInventory) inv);
        NonNullList<ItemStack> remaining = NonNullList.create();
        remainingSponge.forEach(item -> remaining.add(ItemStackUtil.toNative(item)));
        return remaining;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return Registry.RECIPE_SERIALIZER.getOrDefault(this.getId());
    }
}
