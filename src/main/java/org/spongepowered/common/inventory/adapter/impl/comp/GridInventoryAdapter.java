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
package org.spongepowered.common.inventory.adapter.impl.comp;

import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackLike;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.api.item.inventory.type.InventoryColumn;
import org.spongepowered.api.item.inventory.type.InventoryRow;
import org.spongepowered.common.inventory.adapter.impl.AdapterLogic;
import org.spongepowered.common.inventory.adapter.impl.BasicInventoryAdapter;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.impl.comp.GridInventoryLens;
import org.spongepowered.common.inventory.lens.impl.comp.InventoryColumnLens;
import org.spongepowered.common.inventory.lens.impl.comp.InventoryRowLens;
import org.spongepowered.math.vector.Vector2i;

import java.util.Optional;

public class GridInventoryAdapter extends Inventory2DAdapter implements GridInventory {

    protected final GridInventoryLens gridLens;

    public GridInventoryAdapter(Fabric fabric, GridInventoryLens root) {
        this(fabric, root, null);
    }

    public GridInventoryAdapter(Fabric fabric, GridInventoryLens root, Inventory parent) {
        super(fabric, root, parent);
        this.gridLens = root;
    }

    @Override
    public int columns() {
        return this.lens2d.getWidth();
    }

    @Override
    public int rows() {
        return this.lens2d.getHeight();
    }

    @Override
    public Vector2i dimensions() {
        return new Vector2i(this.columns(), this.rows());
    }

    @Override
    public Optional<Slot> slot(int x, int y) {
        return BasicInventoryAdapter.forSlot(this.inventoryAdapter$getFabric(), this.getSlotLens(x, y), this);
    }

    @Override
    public Optional<InventoryRow> row(int y) {
        try {
            InventoryRowLens rowLens = this.gridLens.getRow(y);
            return Optional.<InventoryRow>ofNullable((InventoryRow) rowLens.getAdapter(this.inventoryAdapter$getFabric(), this));
        } catch (IndexOutOfBoundsException ex) {
            return Optional.<InventoryRow>empty();
        }
    }

    @Override
    public Optional<InventoryColumn> column(int x) {
        try {
            InventoryColumnLens columnLens = this.gridLens.getColumn(x);
            return Optional.<InventoryColumn>ofNullable((InventoryColumn) columnLens.getAdapter(this.inventoryAdapter$getFabric(), this));
        } catch (IndexOutOfBoundsException ex) {
            return Optional.<InventoryColumn>empty();
        }
    }

    @Override
    public InventoryTransactionResult.Poll poll(int x, int y) {
        return AdapterLogic.pollSequential(this.inventoryAdapter$getFabric(), this.getSlotLens(x, y), null);
    }

    @Override
    public InventoryTransactionResult.Poll poll(int x, int y, int limit) {
        return AdapterLogic.pollSequential(this.inventoryAdapter$getFabric(), this.getSlotLens(x, y), limit);
    }

    @Override
    public Optional<ItemStack> peek(int x, int y) {
        return AdapterLogic.peekSequential(this.inventoryAdapter$getFabric(), this.getSlotLens(x, y));
    }

    @Override
    public InventoryTransactionResult set(int x, int y, ItemStackLike stack) {
        return AdapterLogic.insertSequential(this.inventoryAdapter$getFabric(), this.getSlotLens(x, y), stack.asMutable());
    }

}
