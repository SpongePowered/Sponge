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
package org.spongepowered.common.item.inventory.lens;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.item.inventory.lens.slots.SlotLens;

import java.util.ArrayList;
import java.util.List;

public class CompoundSlotProvider<TInventory, TStack> implements SlotProvider<TInventory, TStack>  {

    private final List<SlotLens<TInventory, TStack>> slotList = new ArrayList<>();

    public CompoundSlotProvider add(InventoryAdapter<TInventory, TStack> adapter) {
        for (Inventory slot : adapter.slots()) {
            SlotLens slotLens = ((SlotLens<IInventory, ItemStack>) ((SlotAdapter) slot).getRootLens());
            if (!slotList.contains(slotLens)) {
                slotList.add(slotLens);
            }
        }
        return this;
    }

    @Override
    public SlotLens<TInventory, TStack> getSlot(int index) {
        return this.slotList.get(index);
    }

    public int size() {
        return this.slotList.size();
    }
}
