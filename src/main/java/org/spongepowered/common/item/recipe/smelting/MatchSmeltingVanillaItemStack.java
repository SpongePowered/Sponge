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

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.function.Predicate;

import javax.annotation.Nonnull;

/**
 * Needs to be a separate class so it can be used in mixed-in code
 */
public class MatchSmeltingVanillaItemStack implements Predicate<ItemStackSnapshot> {

    private final ItemStackSnapshot itemStackSnapshot;

    public MatchSmeltingVanillaItemStack(ItemStackSnapshot itemStackSnapshot) {
        this.itemStackSnapshot = checkNotNull(itemStackSnapshot, "The itemStackSnapshot must not be null");
    }

    @Override
    public boolean test(ItemStackSnapshot itemStackSnapshot) {
        return matchesVanillaItemStack(this.itemStackSnapshot, itemStackSnapshot);
    }

    public static boolean matchesVanillaItemStack(ItemStackSnapshot recipeStack, ItemStackSnapshot inventoryStack) {
        ItemStack recipe = ItemStackUtil.fromSnapshotToNative(recipeStack);
        ItemStack inventory = ItemStackUtil.fromSnapshotToNative(inventoryStack);

        return compareItemStacks(inventory, recipe);
    }

    /**
     * Same method, but static.
     *
     * @see FurnaceRecipes#compareItemStacks(ItemStack, ItemStack)
     */
    public static boolean compareItemStacks(@Nonnull ItemStack stack1, @Nonnull ItemStack stack2) {
        checkNotNull(stack1, "stack1");
        checkNotNull(stack2, "stack2");

        return stack2.getItem() == stack1.getItem() && (stack2.func_77960_j() == Short.MAX_VALUE || stack2.func_77960_j() == stack1.func_77960_j());
    }

}
