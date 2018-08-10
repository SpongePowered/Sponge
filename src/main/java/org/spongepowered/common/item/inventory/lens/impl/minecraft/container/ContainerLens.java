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
package org.spongepowered.common.item.inventory.lens.impl.minecraft.container;

import static org.spongepowered.api.data.Property.Operator.DELEGATE;

import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.RealLens;
import org.spongepowered.common.item.inventory.property.SlotIndexImpl;

import java.util.Collections;
import java.util.List;

public class ContainerLens extends RealLens {

    // The viewed inventories
    protected List<Lens> viewedInventories;
    private List<Lens> additonal;

    public ContainerLens(InventoryAdapter adapter, SlotProvider slots, List<Lens> lenses) {
        this(adapter, slots, lenses, Collections.emptyList());
    }

    public ContainerLens(InventoryAdapter adapter, SlotProvider slots, List<Lens> lenses, List<Lens> additonal) {
        this(adapter, slots);
        this.viewedInventories = lenses;
        this.additonal = additonal;
        this.init(slots);
    }

    private ContainerLens(InventoryAdapter adapter, SlotProvider slots) {
        super(0, adapter.getFabric().getSize(), adapter.getClass());
        this.additonal = Collections.emptyList();
    }

    protected void init(SlotProvider slots) {

        // Adding slots
        for (int ord = 0, slot = this.base; ord < this.size; ord++, slot++) {
            this.addChild(slots.getSlotLens(slot), new SlotIndexImpl(ord, DELEGATE));
        }

        // Adding spanning children
        for (Lens lens : this.viewedInventories) {
            this.addSpanningChild(lens);
        }

        // Adding additional lenses
        for (Lens lens : this.additonal) {
            this.addChild(lens);
        }

    }
}
