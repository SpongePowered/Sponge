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
package org.spongepowered.common.item.recipe.crafting;

import static org.spongepowered.common.item.inventory.util.InventoryUtil.toSpongeInventory;

import com.google.common.base.Preconditions;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.crafting.CraftingGridInventory;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.api.world.World;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AbstractSpongeCraftingRecipe implements CraftingRecipe, IRecipe {

    @Override
    public boolean func_77569_a(InventoryCrafting inv, net.minecraft.world.World worldIn) {
        return matches(this::isValid, inv, worldIn);
    }

    @Override
    public ItemStack func_77572_b(InventoryCrafting inv) {
        return getCraftingResult(this::getResult, inv);
    }

    @Override
    public ItemStack func_77571_b() {
        return getRecipeOutput(this::getExemplaryResult);
    }

    @Override
    public NonNullList<ItemStack> func_179532_b(InventoryCrafting inv) {
        return getRemainingItems(this::getRemainingItems, inv);
    }

    public static boolean matches(BiFunction<CraftingGridInventory, World, Boolean> isValid, InventoryCrafting inv, net.minecraft.world.World worldIn) {
        return isValid.apply(toSpongeInventory(inv), (World) worldIn);
    }

    public static ItemStack getCraftingResult(Function<CraftingGridInventory, ItemStackSnapshot> getResult, InventoryCrafting inv) {
        ItemStackSnapshot result = getResult.apply(toSpongeInventory(inv));

        Preconditions.checkNotNull(result, "The Sponge implementation returned a `null` result.");

        return ItemStackUtil.fromSnapshotToNative(result);
    }

    public static ItemStack getRecipeOutput(Supplier<ItemStackSnapshot> getExemplaryResult) {
        return ItemStackUtil.fromSnapshotToNative(getExemplaryResult.get());
    }

    public static NonNullList<ItemStack> getRemainingItems(Function<CraftingGridInventory, List<ItemStackSnapshot>> getRemainingItems, InventoryCrafting inv) {
        List<ItemStackSnapshot> spongeResult = getRemainingItems.apply(toSpongeInventory(inv));

        if (spongeResult.size() != inv.func_70302_i_()) {
            throw new IllegalStateException("The number of ItemStackSnapshots returned by getRemainingItems must be equal to the size of the GridInventory.");
        }

        NonNullList<ItemStack> result = NonNullList.func_191197_a(inv.func_70302_i_(), ItemStack.field_190927_a);

        for(int i = 0; i < spongeResult.size(); i++) {
            ItemStack item = ItemStackUtil.fromSnapshotToNative(spongeResult.get(i));

            result.set(i, item != null ? item : ItemStack.field_190927_a);
        }

        return result;
    }

}
