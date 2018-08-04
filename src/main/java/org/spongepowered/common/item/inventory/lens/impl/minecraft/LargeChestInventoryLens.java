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
package org.spongepowered.common.item.inventory.lens.impl.minecraft;

import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.tileentity.TileEntityChest;
import org.spongepowered.api.block.tileentity.carrier.Chest;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.RealLens;
import org.spongepowered.common.item.inventory.lens.impl.comp.GridInventoryLensImpl;

/**
 * This class is only used as an adapter when explicitly requested from the API, trough.
 * {@link Chest#getDoubleChestInventory()}.
 */
public class LargeChestInventoryLens extends RealLens {

    private int upperChest;
    private int lowerChest;

    public LargeChestInventoryLens(InventoryAdapter adapter, SlotProvider slots) {
        super(0, adapter.getFabric().getSize(), InventoryAdapter.class);
        InventoryLargeChest inventory = (InventoryLargeChest) adapter;
        this.upperChest = inventory.upperChest.getSizeInventory();
        this.lowerChest = inventory.lowerChest.getSizeInventory();
        this.init(slots);
    }

    public LargeChestInventoryLens(int base, InventoryAdapter adapter, SlotProvider slots) {
        super(base, adapter.getFabric().getSize(), InventoryAdapter.class);
        InventoryLargeChest inventory = (InventoryLargeChest) adapter.getFabric().get(0);
        this.upperChest = inventory.upperChest.getSizeInventory();
        this.lowerChest = inventory.lowerChest.getSizeInventory();
        this.init(slots);
    }

    @SuppressWarnings("unchecked")
    private void init(SlotProvider slots) {
        // add grids
        int base = 0;
        this.addSpanningChild(new GridInventoryLensImpl(base, 9, this.upperChest / 9, (Class) TileEntityChest.class, slots));
        base += this.upperChest;
        this.addSpanningChild(new GridInventoryLensImpl(base, 9, this.lowerChest / 9, (Class) TileEntityChest.class, slots));
        base += this.lowerChest;

        this.addChild(new GridInventoryLensImpl(0, 9, (this.upperChest + this.lowerChest) / 9, slots));

        this.addMissingSpanningSlots(base, slots);
    }
}
