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
import org.spongepowered.common.inventory.adapter.impl.comp.GridInventoryAdapter;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensProvider;

import java.util.ArrayList;
import java.util.List;

public class GridInventoryLens extends Inventory2DLens {

    private List<Lens> rows = new ArrayList<>();
    private List<Lens> cols = new ArrayList<>();

    public GridInventoryLens(int base, int width, int height, SlotLensProvider slots) {
        this(base, width, height, GridInventoryAdapter.class, slots);
    }

    public GridInventoryLens(int base, int width, int height, Class adapterType, SlotLensProvider slots) {
        super(base, width, height, 1, 0, 0, adapterType, slots);
        this.init(slots);
    }

    protected void init(SlotLensProvider slots) {
        for (int y = 0, base = this.base; y < this.height; y++, base += this.stride) {
            InventoryRowLens row = new InventoryRowLens(base, this.width, this.xBase, this.yBase + y, slots);
            this.addRow(row);
        }

        for (int x = 0, base = this.base; x < this.width; x++, base++) {
            InventoryColumnLens column = new InventoryColumnLens(base, this.height, this.stride, this.xBase + x, this.yBase, slots);
            this.addColumn(column);
        }
    }

    private void addRow(InventoryRowLens row) {
        super.addSpanningChild(row);
        this.rows.add(row);
    }

    private void addColumn(InventoryColumnLens column) {
        super.addChild(column);
        this.cols.add(column);
    }

    public InventoryRowLens getRow(int row) {
        return (InventoryRowLens) this.rows.get(row);
    }

    public InventoryColumnLens getColumn(int column) {
        return (InventoryColumnLens) this.cols.get(column);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Inventory getAdapter(Fabric fabric, Inventory parent) {
        return new GridInventoryAdapter(fabric, this, parent);
    }

}
