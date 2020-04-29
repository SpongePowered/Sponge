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
package org.spongepowered.common.item.inventory.custom;

import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.InventoryProperty;
import org.spongepowered.api.item.inventory.property.InventoryCapacity;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.RealLens;
import org.spongepowered.common.item.inventory.lens.impl.comp.GridInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.comp.OrderedInventoryLensImpl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CustomLens extends RealLens {

    private InventoryArchetype archetype;
    private Map<String, InventoryProperty<?, ?>> properties;

    public CustomLens(int size, Class<? extends Inventory> adapter, SlotProvider slots, InventoryArchetype archetype,
            Map<String, InventoryProperty<?, ?>> properties) {
        super(0, size, adapter);
        this.archetype = archetype;
        this.properties = properties;
        this.init(slots);
    }

    protected void init(SlotProvider slots) {
        // TODO this logic should not be done here (see PR #1010)
        // but for now this will have to do:
        InventoryProperty<?, ?> size = this.properties.get(CustomInventory.INVENTORY_DIMENSION);
        if (size == null) {
            size = this.properties.get(CustomInventory.INVENTORY_CAPACITY);
        }

        if (size != null) {
            this.addLensFor(size, 0, slots);
        } else {
            this.addLensFor(this.archetype, 0, slots); // recursively get archetype sizes
        }

        // Adding slots
        for (int ord = 0, slot = this.base; ord < this.size; ord++, slot++) {
            this.addChild(slots.getSlot(slot), new SlotIndex(ord));
        }
    }

    private int addLensFor(InventoryArchetype archetype, int base, SlotProvider slots) {
        Optional<InventoryProperty<String, ?>> size = archetype.getProperty(CustomInventory.INVENTORY_DIMENSION);
        if (!size.isPresent()) {
            size = archetype.getProperty(CustomInventory.INVENTORY_CAPACITY);
        }
        if (size.isPresent()) {
            return this.addLensFor(size.get(), base, slots);
        }

        int slotCount = 0;
        List<InventoryArchetype> childs = archetype.getChildArchetypes();
        if (childs.isEmpty()) {
            throw new IllegalArgumentException("Missing dimensions!");
        }
        for (InventoryArchetype child : childs) {
            slotCount += addLensFor(child, base + slotCount, slots);
        }
        return slotCount;
    }

    private int addLensFor(InventoryProperty<?, ?> size, int base, SlotProvider slots) {
        Lens lens;
        int slotCount;
        if (size instanceof InventoryDimension) {
            InventoryDimension dimension = ((InventoryDimension) size);
            slotCount = dimension.getColumns() * dimension.getRows();
            lens = new GridInventoryLensImpl(base, dimension.getColumns(), dimension.getRows(), slots);
        } else if (size instanceof InventoryCapacity) {
            InventoryCapacity capacity = ((InventoryCapacity) size);
            slotCount = capacity.getValue();
            lens = new OrderedInventoryLensImpl(base, capacity.getValue(), slots);
        } else {
            throw new IllegalStateException("Unknown Inventory Size Property " + size.getClass().getName());
        }
        this.addSpanningChild(lens);
        return slotCount;
    }
}
