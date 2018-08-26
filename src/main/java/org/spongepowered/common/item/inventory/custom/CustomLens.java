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

import com.flowpowered.math.vector.Vector2i;
import org.spongepowered.api.data.property.Property;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.InventoryProperties;
import org.spongepowered.common.item.inventory.PropertyEntry;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.DefaultIndexedLens;
import org.spongepowered.common.item.inventory.lens.impl.RealLens;
import org.spongepowered.common.item.inventory.lens.impl.comp.GridInventoryLensImpl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CustomLens extends RealLens {

    private InventoryArchetype archetype;
    private Map<Property<?>, ?> properties;

    public CustomLens(InventoryAdapter adapter, SlotProvider slots, InventoryArchetype archetype,
            Map<Property<?>, ?> properties) {
        super(0, adapter.getFabric().getSize(), adapter.getClass());
        this.archetype = archetype;
        this.properties = properties;
        this.init(slots);
    }

    protected void init(SlotProvider slots) {
        // TODO this logic should not be done here (see PR #1010)
        // but for now this will have to do:
        final Vector2i dimension = (Vector2i) this.properties.get(InventoryProperties.DIMENSION);
        final Integer capacity = (Integer) this.properties.get(InventoryProperties.CAPACITY);

        if (dimension != null) {
            addLensForDimension(dimension, 0, slots);
        } else if (capacity != null) {
            addLensForCapacity(capacity, 0, slots);
        } else {
            this.addLensFor(this.archetype, 0, slots); // recursively get archetype sizes
        }

        // Adding slots
        for (int ord = 0, slot = this.base; ord < this.size; ord++, slot++) {
            this.addChild(slots.getSlotLens(slot), PropertyEntry.slotIndex(ord));
        }
    }

    private int addLensFor(InventoryArchetype archetype, int base, SlotProvider slots) {
        final Optional<Vector2i> dimension = archetype.getProperty(InventoryProperties.DIMENSION);
        if (dimension.isPresent()) {
            return addLensForDimension(dimension.get(), base, slots);
        }
        final Optional<Integer> capacity = archetype.getProperty(InventoryProperties.CAPACITY);
        if (capacity.isPresent()) {
            return addLensForCapacity(capacity.get(), base, slots);
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

    private int addLensForCapacity(int capacity, int base, SlotProvider slots) {
        addSpanningChild(new DefaultIndexedLens(base, capacity, slots));
        return capacity;
    }

    private int addLensForDimension(Vector2i dimension, int base, SlotProvider slots) {
        final int slotCount = dimension.getX() * dimension.getY();
        final Lens lens;
        if (slotCount == 1) {
            lens = slots.getSlotLens(base);
        } else {
            lens = new GridInventoryLensImpl(base, dimension.getX(), dimension.getY(), slots);
        }
        addSpanningChild(lens);
        return slotCount;
    }
}
