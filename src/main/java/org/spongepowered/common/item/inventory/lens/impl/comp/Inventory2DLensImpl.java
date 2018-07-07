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

import org.spongepowered.api.data.Property.Operator;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.comp.Inventory2DAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.comp.Inventory2DLens;
import org.spongepowered.common.item.inventory.lens.impl.SlotBasedLens;
import org.spongepowered.common.item.inventory.lens.slots.SlotLens;
import org.spongepowered.common.item.inventory.property.SlotPosImpl;

public class Inventory2DLensImpl extends SlotBasedLens implements Inventory2DLens {

    protected final int width;
    protected final int height;

    protected final int xBase;
    protected final int yBase;

    public Inventory2DLensImpl(int base, int width, int height, SlotProvider slots) {
        this(base, width, height, width, Inventory2DAdapter.class, slots);
    }

    public Inventory2DLensImpl(int base, int width, int height, int rowStride, Class<? extends Inventory> adapterType, SlotProvider slots) {
        this(base, width, height, rowStride, 0, 0, adapterType, slots);
    }

    protected Inventory2DLensImpl(int base, int width, int height, int rowStride, int xBase, int yBase, Class<? extends Inventory> adapterType,
            SlotProvider slots) {
        super(base, width * height, rowStride, adapterType, slots);

        checkArgument(width > 0, "Invalid width: %s", width);
        checkArgument(height > 0, "Invalid height: %s", height);

        this.width = width;
        this.height = height;
        this.xBase = xBase;
        this.yBase = yBase;

        this.init(slots);
    }

    private void init(SlotProvider slots) {
        for (int y = 0, slot = this.base; y < this.height; y++) {
            for (int x = 0; x < this.width; x++, slot += stride) {
                SlotLens slotLens = slots.getSlotLens(slot);
                this.addSpanningChild(slotLens, new SlotPosImpl(this.xBase + x, this.yBase + y));
            }
        }
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public SlotLens getSlot(SlotPos pos) {
        if (pos.getOperator() != Operator.EQUAL) {
            return null;
        }

        return (SlotLens) this.spanningChildren.get(pos.getY()).lens.getLens(pos.getX());
    }

    @Override
    public InventoryAdapter getAdapter(Fabric inv, Inventory parent) {
        return new Inventory2DAdapter(inv, this, parent);
    }

    @Override
    public int getStride() {
        return this.stride;
    }
}
