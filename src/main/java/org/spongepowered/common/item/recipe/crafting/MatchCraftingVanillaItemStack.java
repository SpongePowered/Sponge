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

import com.google.common.base.Preconditions;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.crafting.ShapedRecipes;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.function.Predicate;

/**
 * Needs to be a separate class so it can be used in mixed-in code
 */
public class MatchCraftingVanillaItemStack implements Predicate<ItemStackSnapshot> {

    private final ItemStackSnapshot itemStackSnapshot;

    public MatchCraftingVanillaItemStack(ItemStackSnapshot itemStackSnapshot) {
        Preconditions.checkNotNull(itemStackSnapshot, "The itemStackSnapshot must not be null");

        this.itemStackSnapshot = itemStackSnapshot;
    }

    @Override
    public boolean test(ItemStackSnapshot itemStackSnapshot) {
        return matchesVanillaItemStack(this.itemStackSnapshot, itemStackSnapshot);
    }

    /**
     * Mimic the vanilla matching behavior, taken from
     * {@link ShapedRecipes#checkMatch(InventoryCrafting, int, int, boolean)}
     *
     * @param recipeStack The stack required by the recipe
     * @param inventoryStack The stack found in the inventory
     * @return Whether the stacks match according to the vanilla Minecraft
     *         behavior
     */
    public static boolean matchesVanillaItemStack(ItemStackSnapshot recipeStack, ItemStackSnapshot inventoryStack) {
        net.minecraft.item.ItemStack recipeNMS = ItemStackUtil.fromSnapshotToNative(recipeStack);
        net.minecraft.item.ItemStack inventoryNMS = ItemStackUtil.fromSnapshotToNative(inventoryStack);

        if (!recipeNMS.isEmpty() || !inventoryNMS.isEmpty()) {
            if (recipeNMS.isEmpty() != inventoryNMS.isEmpty()) {
                return false;
            }

            if (recipeNMS.getItem() != inventoryNMS.getItem()) {
                return false;
            }

            if (recipeNMS.getMetadata() != 32767 && recipeNMS.getMetadata() != inventoryNMS.getMetadata()) {
                return false;
            }
        }

        return true;
    }

}
