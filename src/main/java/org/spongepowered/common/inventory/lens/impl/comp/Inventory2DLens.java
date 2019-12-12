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
package org.spongepowered.common.inventory.lens.impl.comp;

import static com.google.common.base.Preconditions.checkArgument;

import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryProperties;
import org.spongepowered.common.inventory.property.PropertyEntry;
import org.spongepowered.common.inventory.adapter.impl.comp.Inventory2DAdapter;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.impl.SlotBasedLens;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensProvider;
import org.spongepowered.common.inventory.lens.slots.SlotLens;
import org.spongepowered.math.vector.Vector2i;

public class Inventory2DLens extends SlotBasedLens {

    protected final int width;
    protected final int height;

    protected final int xBase;
    protected final int yBase;

    public Inventory2DLens(int base, int width, int height, SlotLensProvider slots) {
        this(base, width, height, width, Inventory2DAdapter.class, slots);
    }

    public Inventory2DLens(int base, int width, int height, int rowStride, Class<? extends Inventory> adapterType, SlotLensProvider slots) {
        this(base, width, height, rowStride, 0, 0, adapterType, slots);
    }

    protected Inventory2DLens(int base, int width, int height, int rowStride, int xBase, int yBase, Class<? extends Inventory> adapterType, SlotLensProvider slots) {
        super(base, width * height, rowStride, adapterType, slots);

        checkArgument(width > 0, "Invalid width: %s", width);
        checkArgument(height > 0, "Invalid height: %s", height);

        this.width = width;
        this.height = height;
        this.xBase = xBase;
        this.yBase = yBase;

        this.init(slots);
    }

    private void init(SlotLensProvider slots) {
        for (int y = 0, slot = this.base; y < this.height; y++) {
            for (int x = 0; x < this.width; x++, slot += this.stride) {
                SlotLens slotLens = slots.getSlotLens(slot);
                this.addSpanningChild(slotLens, PropertyEntry.of(InventoryProperties.SLOT_POSITION, new Vector2i(this.xBase + x, this.yBase + y)));
            }
        }
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public SlotLens getSlot(Vector2i pos) {
        return (SlotLens) this.spanningChildren.get(pos.getY()).lens.getLens(pos.getX());
    }

    @Override
    public Inventory getAdapter(Fabric fabric, Inventory parent) {
        return new Inventory2DAdapter(fabric, this, parent);
    }

}
