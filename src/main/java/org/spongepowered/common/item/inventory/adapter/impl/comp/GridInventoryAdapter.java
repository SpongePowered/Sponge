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
package org.spongepowered.common.item.inventory.adapter.impl.comp;

import com.flowpowered.math.vector.Vector2i;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.api.item.inventory.type.InventoryColumn;
import org.spongepowered.api.item.inventory.type.InventoryRow;
import org.spongepowered.common.item.inventory.adapter.impl.AbstractInventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.AdapterLogic;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.comp.GridInventoryLens;
import org.spongepowered.common.item.inventory.lens.comp.InventoryColumnLens;
import org.spongepowered.common.item.inventory.lens.comp.InventoryRowLens;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GridInventoryAdapter extends Inventory2DAdapter implements GridInventory {
    
    protected final GridInventoryLens gridLens;
    
    protected final List<InventoryRow> rows = new ArrayList<InventoryRow>();
    protected final List<InventoryColumn> columns = new ArrayList<InventoryColumn>();

    public GridInventoryAdapter(Fabric inventory, GridInventoryLens root) {
        this(inventory, root, null);
    }

    public GridInventoryAdapter(Fabric inventory, GridInventoryLens root, Inventory parent) {
        super(inventory, root, parent);
        this.gridLens = root;
    }
    
    @Override
    public int getColumns() {
        return this.lens2d.getWidth();
    }

    @Override
    public int getRows() {
        return this.lens2d.getHeight();
    }

    @Override
    public Vector2i getDimensions() {
        return new Vector2i(this.getColumns(), this.getRows());
    }

    @Override
    public Optional<Slot> getSlot(int x, int y) {
        return AbstractInventoryAdapter.forSlot(this.bridge$getFabric(), this.getSlotLens(x, y), this);
    }

    @Override
    public Optional<InventoryRow> getRow(int y) {
        try {
            InventoryRowLens rowLens = this.gridLens.getRow(y);
            return Optional.<InventoryRow>ofNullable((InventoryRow) rowLens.getAdapter(this.bridge$getFabric(), this));
        } catch (IndexOutOfBoundsException ex) {
            return Optional.<InventoryRow>empty();
        }
    }

    @Override
    public Optional<InventoryColumn> getColumn(int x) {
        try {
            InventoryColumnLens columnLens = this.gridLens.getColumn(x);
            return Optional.<InventoryColumn>ofNullable((InventoryColumn) columnLens.getAdapter(this.bridge$getFabric(), this));
        } catch (IndexOutOfBoundsException ex) {
            return Optional.<InventoryColumn>empty();
        }
    }

    @Override
    public Optional<ItemStack> poll(int x, int y) {
        return AdapterLogic.pollSequential(this.bridge$getFabric(), this.getSlotLens(x, y));
    }

    @Override
    public Optional<ItemStack> poll(int x, int y, int limit) {
        return AdapterLogic.pollSequential(this.bridge$getFabric(), this.getSlotLens(x, y), limit);
    }

    @Override
    public Optional<ItemStack> peek(int x, int y) {
        return AdapterLogic.peekSequential(this.bridge$getFabric(), this.getSlotLens(x, y));
    }

    @Override
    public Optional<ItemStack> peek(int x, int y, int limit) {
        return AdapterLogic.peekSequential(this.bridge$getFabric(), this.getSlotLens(x, y), limit);
    }

    @Override
    public InventoryTransactionResult set(int x, int y, ItemStack stack) {
        return AdapterLogic.insertSequential(this.bridge$getFabric(), this.getSlotLens(x, y), stack);
    }

}
