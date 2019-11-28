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
package org.spongepowered.common.inventory.adapter.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensProvider;
import org.spongepowered.common.inventory.lens.slots.SlotLens;
import java.util.Iterator;
import java.util.List;

public class SlotCollection implements Iterable<Slot> {
    
    private Inventory parent;

    private final Fabric inv;
    
    private final List<Slot> slots;

    public SlotCollection(Inventory parent, Fabric inv, Lens lens, SlotLensProvider slots) {
        this.parent = parent;
        this.inv = inv;
        this.slots = this.traverseSpanningTree(inv, lens, slots, ImmutableList.<Slot>builder()).build();
    }
    
    @SuppressWarnings("rawtypes")
    private Builder<Slot> traverseSpanningTree(Fabric inv, Lens lens, SlotLensProvider slots, Builder<Slot> list) {
        if (lens instanceof SlotLens) {
            list.add(((SlotAdapter) lens.getAdapter(inv, this.parent)));
            return list;
        }
        for (Lens child : lens.getSpanningChildren()) {
            if (child instanceof SlotLens) {
                list.add((SlotAdapter) child.getAdapter(inv, this.parent));
            } else if (child.getSpanningChildren().size() > 0) {
                this.traverseSpanningTree(inv, child, slots, list);
            }
        }
        return list;
    }

    public Fabric getFabric() {
        return this.inv;
    }

    @Override
    public Iterator<Slot> iterator() {
        return this.slots.iterator();
    }

    public static SlotCollection of(Inventory parent, InventoryAdapter adapter) {
        return new SlotCollection(parent, adapter.bridge$getFabric(), adapter.bridge$getRootLens(), adapter.bridge$getSlotProvider());
    }
}
