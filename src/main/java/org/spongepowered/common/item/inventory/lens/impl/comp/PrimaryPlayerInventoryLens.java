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
import org.spongepowered.common.item.inventory.adapter.impl.comp.PrimaryPlayerInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.comp.GridInventoryLens;
import org.spongepowered.common.item.inventory.lens.comp.HotbarLens;
import org.spongepowered.common.item.inventory.lens.impl.RealLens;
import org.spongepowered.common.item.inventory.lens.slots.SlotLens;

public class PrimaryPlayerInventoryLens extends RealLens {

    private static final int MAIN_INVENTORY_HEIGHT = 3;
    private static final int INVENTORY_WIDTH = 9;

    private HotbarLensImpl hotbar;
    private GridInventoryLensImpl mainGrid;
    private GridInventoryLensImpl fullGrid;
    private boolean isContainer;

    public PrimaryPlayerInventoryLens(int base, SlotProvider slots, boolean isContainer) {
        this(base, PrimaryPlayerInventoryAdapter.class, slots, isContainer);
    }

    public PrimaryPlayerInventoryLens(int base, Class<? extends Inventory> adapterType, SlotProvider slots, boolean isContainer) {
        super(base, 9 * 4, adapterType);
        this.isContainer = isContainer;

        this.init(slots);
    }

    private void init(SlotProvider slots) {
        int base = this.base;

        if (this.isContainer) {
            this.mainGrid = new GridInventoryLensImpl(base, INVENTORY_WIDTH, MAIN_INVENTORY_HEIGHT, slots);
            base += INVENTORY_WIDTH * 3;
            this.hotbar = new HotbarLensImpl(base, INVENTORY_WIDTH, slots);
            /*
            1 |G|G|G|G|G|G|G|G|G|
            2 |G|G|G|G|G|G|G|G|G|
            3 |G|G|G|G|G|G|G|G|G|
            4 |H|H|H|H|H|H|H|H|H|
            */

            this.addSpanningChild(this.mainGrid);
            this.addSpanningChild(this.hotbar);

            fullGrid = new GridInventoryLensImpl(this.base, INVENTORY_WIDTH, MAIN_INVENTORY_HEIGHT + 1, slots);
            this.addChild(fullGrid);

        } else {
            this.hotbar = new HotbarLensImpl(base, INVENTORY_WIDTH, slots);
            base += INVENTORY_WIDTH;
            this.mainGrid = new GridInventoryLensImpl(base, INVENTORY_WIDTH, MAIN_INVENTORY_HEIGHT, slots);

            /*
            2 |G|G|G|G|G|G|G|G|G|
            3 |G|G|G|G|G|G|G|G|G|
            4 |G|G|G|G|G|G|G|G|G|
            1 |H|H|H|H|H|H|H|H|H|
            */

            this.addSpanningChild(this.hotbar);
            this.addSpanningChild(this.mainGrid);

            // Shift slots so that Hotbar is always after the MainGrid
            ShiftedSlotProvider shiftedSlots = new ShiftedSlotProvider(slots, INVENTORY_WIDTH, INVENTORY_WIDTH * 4);
            fullGrid = new GridInventoryLensImpl(this.base, INVENTORY_WIDTH, MAIN_INVENTORY_HEIGHT + 1, shiftedSlots);
            this.addChild(fullGrid);
        }

    }

    private class ShiftedSlotProvider implements SlotProvider {

        private final SlotProvider provider;
        private final int shiftBy;
        private final int shiftAt;

        public ShiftedSlotProvider(SlotProvider provider, int shiftBy, int shiftAt) {
            this.provider = provider;
            this.shiftBy = shiftBy;
            this.shiftAt = shiftAt;
        }

        @Override
        public SlotLens getSlotLens(int index) {
            index = index + this.shiftBy;
            if (index >= this.shiftAt) {
                index -= this.shiftAt;
            }
            return this.provider.getSlotLens(index);
        }
    }

    @Override
    public InventoryAdapter getAdapter(Fabric fabric, Inventory parent) {
        return new PrimaryPlayerInventoryAdapter(fabric, this, parent);
    }

    public HotbarLens getHotbar() {
        return this.hotbar;
    }

    public GridInventoryLens getMain() {
        return this.mainGrid;
    }

    public GridInventoryLens getFullGrid() {
        return fullGrid;
    }
}
