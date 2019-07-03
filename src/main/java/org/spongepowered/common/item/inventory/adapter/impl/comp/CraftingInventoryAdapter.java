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

import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.crafting.CraftingGridInventory;
import org.spongepowered.api.item.inventory.crafting.CraftingInventory;
import org.spongepowered.api.item.inventory.crafting.CraftingOutput;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.comp.CraftingInventoryLens;

public class CraftingInventoryAdapter extends OrderedInventoryAdapter implements CraftingInventory {

    protected final CraftingInventoryLens craftingLens;
    
    private CraftingGridInventory craftingGrid;
    private CraftingOutput result;

    public CraftingInventoryAdapter(Fabric inventory, CraftingInventoryLens root, Inventory parent) {
        super(inventory, root, parent);
        this.craftingLens = root;
    }

    @Override
    public CraftingGridInventory getCraftingGrid() {
        if (this.craftingGrid == null) {
            this.craftingGrid = (CraftingGridInventory) this.craftingLens.getCraftingGrid().getAdapter(this.bridge$getFabric(), this);
        }
        return this.craftingGrid;
    }

    @Override
    public CraftingOutput getResult() {
        if (this.result == null) {
            this.result = (CraftingOutput) this.craftingLens.getOutputSlot().getAdapter(this.bridge$getFabric(), this);
        }
        return this.result;
    }
}
