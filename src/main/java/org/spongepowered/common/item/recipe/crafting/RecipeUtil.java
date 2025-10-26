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

import net.minecraft.core.Holder;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import org.spongepowered.api.Sponge;
import org.spongepowered.common.SpongeCommon;

import java.util.List;

public final class RecipeUtil {

    public static ContextMap serverBasedContextMap() {
        if (!Sponge.isServerAvailable()) {
            return new ContextMap.Builder().create(SlotDisplayContext.CONTEXT);
        }
        final var server = SpongeCommon.server();
        return new ContextMap.Builder()
            .withParameter(SlotDisplayContext.FUEL_VALUES, server.fuelValues())
            .withParameter(SlotDisplayContext.REGISTRIES, server.registryAccess())
            .create(SlotDisplayContext.CONTEXT);
    }

    public class RemainderResolver<T> implements DisplayContentsFactory.ForRemainders<T> {
        @Override
        public T addRemainder(T var1, List<T> var2) {
            return null;
        }
    }

    /**
     * Copied from {@link Inventory#findSlotMatchingCraftingIngredient(Holder, ItemStack)}
     * and adjusted to use exemplary {@link ItemStack} instead of just item type.
     */
    public static int findSlotMatchingCraftingIngredient(
        final Inventory inventory, final ItemStack exemplaryStackToMove, final ItemStack craftInputStack
    ) {
        for (int i = 0; i < inventory.items.size(); i++) {
            final ItemStack stack = inventory.items.get(i);
            if (!stack.isEmpty()
                && Inventory.isUsableForCrafting(stack)
                && ItemStack.isSameItemSameComponents(exemplaryStackToMove, stack)
                && (craftInputStack.isEmpty() || ItemStack.isSameItemSameComponents(craftInputStack, stack))) {
                return i;
            }
        }

        return -1;
    }
}
