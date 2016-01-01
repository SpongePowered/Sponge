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
package org.spongepowered.common.item.inventory.lens.impl.collections;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.common.item.inventory.adapter.impl.SlotCollectionAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.slots.SlotLensImpl;
import org.spongepowered.common.item.inventory.lens.slots.SlotLens;

public class SlotCollection extends DynamicLensCollectionImpl<IInventory, ItemStack> implements SlotProvider<IInventory, ItemStack> {
    
    public SlotCollection(int size) {
        super(size);
        this.populate();
    }

    private void populate() {
        for (int index = 0; index < this.size(); index++) {
            this.lenses[index] = this.createSlotLens(index);
        }
    }

    protected SlotLens<IInventory, ItemStack> createSlotLens(int slotIndex) {
        return new SlotLensImpl(slotIndex, this.getSlotAdapterType(slotIndex));
    }

    protected Class<? extends Inventory> getSlotAdapterType(int slotIndex) {
        return SlotAdapter.class;
    }

    @Override
    public SlotLens<IInventory, ItemStack> getSlot(int index) {
        return (SlotLens<IInventory, ItemStack>) this.get(index);
    }

    public Iterable<Slot> getAdapter(Fabric<IInventory> inv) {
        return new SlotCollectionAdapter(inv, this);
    }

}
