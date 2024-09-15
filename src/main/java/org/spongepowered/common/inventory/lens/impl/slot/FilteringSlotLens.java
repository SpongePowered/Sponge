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
package org.spongepowered.common.inventory.lens.impl.slot;

import net.minecraft.world.Container;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackLike;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.common.bridge.world.inventory.InventoryBridge;
import org.spongepowered.common.inventory.adapter.impl.slots.FilteringSlotAdapter;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.slots.SlotLens;
import org.spongepowered.common.item.util.ItemStackUtil;

public class FilteringSlotLens extends DelegatingSlotLens {

    private final ItemStackFilter stackFilter;

    public FilteringSlotLens(SlotLens lens, ItemStackFilter stackFilter, Class<? extends Inventory> adapterType) {
        super(lens, adapterType);
        this.stackFilter = stackFilter;
    }

    @Override
    public boolean setStack(Fabric fabric, net.minecraft.world.item.ItemStack stack) {
        return this.getItemStackFilter().test(fabric, ItemStackUtil.fromNative(stack)) && super.setStack(fabric, stack);
    }

    public ItemStackFilter getItemStackFilter() {
        return this.stackFilter;
    }

    @Override
    public Slot getAdapter(Fabric fabric, Inventory parent) {
        return new FilteringSlotAdapter(fabric, this, parent);
    }

    @FunctionalInterface
    public interface ItemStackFilter {
        boolean test(Fabric fabric, ItemStackLike itemStack);

        static ItemStackFilter filterNone() {
            return (f, i) -> true;
        }

        static ItemStackFilter filterIInventory(int slot) {
            return (fabric, item) -> {
                InventoryBridge inventory = fabric.fabric$get(slot);
                if (inventory instanceof Container) {
                    return ((Container) inventory).canPlaceItem(slot, ItemStackUtil.fromLikeToNative(item));
                }
                return true;
            };
        }
    }
}
