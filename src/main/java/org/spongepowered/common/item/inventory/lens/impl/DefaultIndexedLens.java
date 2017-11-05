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
package org.spongepowered.common.item.inventory.lens.impl;

import net.minecraft.item.ItemStack;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.AbstractInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;

public class DefaultIndexedLens<TInventory> extends AbstractLens<TInventory, ItemStack> {

    public DefaultIndexedLens(int offset, int size, Class<? extends Inventory> adapterType, SlotProvider<TInventory, ItemStack> slots) {
        super(offset, size, adapterType, slots);
        this.init(slots);
    }

    public DefaultIndexedLens(int offset, int size, InventoryAdapter<TInventory, ItemStack> adapter, SlotCollection<TInventory> slots) {
        super(offset, size, adapter, slots);
        this.init(slots);
    }

    @Override
    protected void init(SlotProvider<TInventory, ItemStack> slots) {
        for (int slot = 0; slot < this.size; slot++) {
            this.addSpanningChild(slots.getSlot(slot), new SlotIndex(slot));
        }
    }
    
    @Override
    public int getRealIndex(Fabric<TInventory> inv, int ordinal) {
        return ordinal >= this.base + this.size ? -1 : Math.max(-1, this.base + ordinal);
    }

    @Override
    public InventoryAdapter<TInventory, ItemStack> getAdapter(Fabric<TInventory> inv, Inventory parent) {
        return new AbstractInventoryAdapter<>(inv, this, parent);
    }
}
