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

import org.apache.commons.lang3.ObjectUtils;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.comp.GridInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.comp.GridInventoryLens;
import org.spongepowered.common.item.inventory.lens.comp.InventoryColumnLens;
import org.spongepowered.common.item.inventory.lens.comp.InventoryRowLens;
import org.spongepowered.common.item.inventory.lens.impl.fabric.MinecraftFabric;
import org.spongepowered.common.item.inventory.lens.impl.struct.LensHandle;

import java.util.ArrayList;
import java.util.List;


public class GridInventoryLensImpl extends Inventory2DLensImpl implements GridInventoryLens {

    protected List<LensHandle> rows;
    protected List<LensHandle> cols;

    public GridInventoryLensImpl(int base, int width, int height, SlotProvider slots) {
        this(base, width, height, GridInventoryAdapter.class, slots);
    }

    public GridInventoryLensImpl(int base, int width, int height, Class<? extends Inventory> adapterType, SlotProvider slots) {
        super(base, width, height, 1, 0, 0, adapterType, slots);
        this.init(slots);
    }

    private void init(SlotProvider slots) {

        this.rows = new ArrayList<>();
        this.cols = new ArrayList<>();

        for (int y = 0, base = this.base; y < this.height; y++, base += this.width) {
            InventoryRowLensImpl row = new InventoryRowLensImpl(base, this.width, this.xBase, this.yBase + y, slots);
            this.rows.add(new LensHandle(row));
            this.addChild(row);
        }

        for (int x = 0, base = this.base; x < this.width; x++, base++) {
            InventoryColumnLensImpl column = new InventoryColumnLensImpl(base, this.height, this.width, this.xBase + x, this.yBase, slots);
            this.cols.add(new LensHandle(column));
            this.addChild(column);
        }

    }

    @Override
    public InventoryRowLens getRow(int row) {
        return (InventoryRowLens) this.rows.get(row).lens;
    }

    @Override
    public InventoryColumnLens getColumn(int column) {
        return (InventoryColumnLens) this.cols.get(column).lens;
    }

    @SuppressWarnings("unchecked")
    @Override
    public InventoryAdapter getAdapter(Fabric fabric, Inventory parent) {
        return ObjectUtils.firstNonNull(MinecraftFabric.getAdapter(fabric, parent, this.base, this.adapterType), new GridInventoryAdapter(fabric, this, parent));
    }



}
