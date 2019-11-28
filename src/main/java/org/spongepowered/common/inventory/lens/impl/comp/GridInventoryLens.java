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

import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.adapter.impl.comp.GridInventoryAdapter;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensProvider;
import org.spongepowered.common.inventory.lens.impl.struct.LensHandle;

import java.util.ArrayList;
import java.util.List;


public class GridInventoryLens extends Inventory2DLens implements GridInventoryLens {

    protected List<LensHandle> rows;
    protected List<LensHandle> cols;

    public GridInventoryLens(int base, int width, int height, SlotLensProvider slots) {
        this(base, width, height, 1, GridInventoryAdapter.class, slots);
    }

    public GridInventoryLens(int base, int width, int height, Class<? extends Inventory> adapterType, SlotLensProvider slots) {
        this(base, width, height, width, adapterType, slots);
    }

    public GridInventoryLens(int base, int width, int height, int rowStride, SlotLensProvider slots) {
        this(base, width, height, rowStride, 0, 0, GridInventoryAdapter.class, slots);
    }

    public GridInventoryLens(int base, int width, int height, int rowStride, Class<? extends Inventory> adapterType, SlotLensProvider slots) {
        this(base, width, height, rowStride, 0, 0, adapterType, slots);
    }

    public GridInventoryLens(int base, int width, int height, int rowStride, int xBase, int yBase, SlotLensProvider slots) {
        this(base, width, height, rowStride, xBase, yBase, GridInventoryAdapter.class, slots);
    }

    public GridInventoryLens(int base, int width, int height, int rowStride, int xBase, int yBase, Class<? extends Inventory> adapterType, SlotLensProvider slots) {
        super(base, width, height, rowStride, xBase, yBase, adapterType, slots);
    }

    @Override
    protected void init(SlotLensProvider slots) {
    }

    @Override
    protected void init(SlotLensProvider slots, boolean spanning) {
        super.init(slots, false);
        this.rows = new ArrayList<>();
        this.cols = new ArrayList<>();

        for (int y = 0, base = this.base; y < this.height; y++, base += this.stride) {
            org.spongepowered.common.inventory.lens.impl.comp.InventoryRowLens row = new org.spongepowered.common.inventory.lens.impl.comp.InventoryRowLens(base, this.width, this.xBase, this.yBase + y, slots);
            this.addRow(row);
        }

        for (int x = 0, base = this.base; x < this.width; x++, base++) {
            org.spongepowered.common.inventory.lens.impl.comp.InventoryColumnLens column = new org.spongepowered.common.inventory.lens.impl.comp.InventoryColumnLens(base, this.height, this.stride, this.xBase + x, this.yBase, slots);
            this.addColumn(column);
        }

        this.cache();
    }

    protected void addRow(InventoryRowLens row) {
        super.addSpanningChild(row);
        this.rows.add(new LensHandle(row));
    }

    protected void addColumn(InventoryColumnLens column) {
        super.addChild(column);
        this.cols.add(new LensHandle(column));
    }

    @Override
    public InventoryRowLens getRow(int row) {
        return (InventoryRowLens) this.rows.get(row).lens;
    }

    @Override
    public InventoryColumnLens getColumn(int column) {
        return (InventoryColumnLens) this.cols.get(column).lens;
    }

    @Override
    public int getRealIndex(Fabric fabric, int ordinal) {
        LensHandle child = this.getLensForOrdinal(ordinal);
        return child.lens.getRealIndex(fabric, ordinal - child.ordinal);
    }

    @SuppressWarnings("unchecked")
    @Override
    public InventoryAdapter getAdapter(Fabric fabric, Inventory parent) {
        return new GridInventoryAdapter(fabric, this, parent);
    }

}
