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
package org.spongepowered.common.item.inventory.lens.impl.comp;

import static com.google.common.base.Preconditions.checkArgument;

import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.comp.OrderedInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.comp.OrderedInventoryLens;
import org.spongepowered.common.item.inventory.lens.impl.ConceptualLens;
import org.spongepowered.common.item.inventory.lens.impl.struct.LensHandle;
import org.spongepowered.common.item.inventory.lens.slots.SlotLens;

import java.util.ArrayList;
import java.util.List;

public class OrderedInventoryLensImpl extends ConceptualLens implements OrderedInventoryLens {

    protected final int stride;
    
    protected final List<LensHandle> slotCache = new ArrayList<>();

    public OrderedInventoryLensImpl(int base, int size, SlotProvider slots) {
        this(base, size, 1, OrderedInventoryAdapter.class, slots);
    }

    protected OrderedInventoryLensImpl(int base, int size, int stride, Class<? extends Inventory> adapterType, SlotProvider slots) {
        super(base, size, adapterType);
        checkArgument(stride > 0, "Invalid stride: %s", stride);
        this.stride = stride;
        this.init(slots);
    }

    protected void cache() {
        for (LensHandle child : this.spanningChildren) {
            this.cache0(child.lens);
        }
    }

    private void cache0(Lens lens) {
        if (lens instanceof SlotLens) {
            this.slotCache.add(new LensHandle(lens, lens.getProperties(0)));
            return;
        }
        for (Lens child : lens.getSpanningChildren()) {
            if (child instanceof SlotLens) {
                this.slotCache.add(new LensHandle(child, lens.getProperties(child)));
            } else {
                this.cache0(child);
            }
        }
    }

    @Override
    public int getStride() {
        return this.stride;
    }

    @Override
    public SlotLens getSlot(int ordinal) {
        return (SlotLens) this.slotCache.get(ordinal).lens;
    }

    protected void init(SlotProvider slots) {
        for (int ord = 0, slot = this.base; ord < this.size; ord++, slot += this.stride) {
            this.addSpanningChild(slots.getSlot(slot), new SlotIndex(ord));
        }
        this.cache();
    }

    @Override
    public int getRealIndex(Fabric inv, int ordinal) {
        if (!this.checkOrdinal(ordinal)) {
            return -1;
        }

        return this.base + (ordinal * this.stride);
    }

    @Override
    public boolean hasSlotRealIndex(int index) {
        return this.availableSlots.contains(this.base + index);
    }

    @Override
    public InventoryAdapter getAdapter(Fabric inv, Inventory parent) {
        return new OrderedInventoryAdapter(inv, this, parent);
    }
}
