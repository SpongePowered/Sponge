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

import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.comp.InventoryRowAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.comp.InventoryRowLens;


public class InventoryRowLensImpl extends Inventory2DLensImpl implements InventoryRowLens {

    public InventoryRowLensImpl(int base, int width, SlotProvider slots) {
        this(base, width, 0, 0, InventoryRowAdapter.class, slots);
    }

    public InventoryRowLensImpl(int base, int width, Class<? extends Inventory> adapterType, SlotProvider slots) {
        this(base, width, 0, 0, adapterType, slots);
    }
    
    public InventoryRowLensImpl(int base, int width, int xBase, int yBase, SlotProvider slots) {
        this(base, width, xBase, yBase, InventoryRowAdapter.class, slots);
    }
    
    public InventoryRowLensImpl(int base, int width, int xBase, int yBase, Class<? extends Inventory> adapterType, SlotProvider slots) {
        super(base, width, 1, width, xBase, yBase, adapterType, slots);
    }
    
    @Override
    public int getRealIndex(Fabric inv, int ordinal) {
        if (!this.checkOrdinal(ordinal)) {
            return -1;
        }

        return this.base + ordinal;
    }

    @Override
    public InventoryAdapter getAdapter(Fabric inv, Inventory parent) {
        return new InventoryRowAdapter(inv, this, parent);
    }

}
