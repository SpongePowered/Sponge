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
package org.spongepowered.common.inventory.lens;

import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.inventory.lens.slots.SlotLens;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * A custom slot provider without duplicate slot-lenses.
 */
public class CompoundSlotProvider implements SlotLensProvider {

    private final List<SlotLens> slotList = new ArrayList<>();

    /**
     * Adds all slots from this inventory adapter.
     *
     * @param adapter The adapter
     *
     * @return this provider for chaining
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public CompoundSlotProvider add(InventoryAdapter adapter) {
        for (Inventory slot : ((Inventory) adapter).slots()) {
            SlotLens slotLens = ((SlotLens) ((SlotAdapter) slot).bridge$getRootLens());
            if (!this.slotList.contains(slotLens)) {
                this.slotList.add(slotLens);
            }
        }
        return this;
    }

    /**
     * Adds a single slot-lens.
     *
     * @param slotLens The slot-lens
     *
     * @return This provider for chaining
     */
    public CompoundSlotProvider add(SlotLens slotLens) {
        if (!this.slotList.contains(slotLens)) {
            this.slotList.add(slotLens);
        }
        return this;
    }

    @Override
    public SlotLens getSlotLens(int index) {
        return this.slotList.get(index);
    }

    public int size() {
        return this.slotList.size();
    }
}
