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

import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.comp.MainPlayerInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.comp.GridInventoryLens;
import org.spongepowered.common.item.inventory.lens.comp.HotbarLens;
import org.spongepowered.common.item.inventory.lens.comp.MainPlayerInventoryLens;
import org.spongepowered.common.item.inventory.lens.slots.SlotLens;

public class MainPlayerInventoryLensImpl extends GridInventoryLensImpl implements MainPlayerInventoryLens {

    private static final int MAIN_INVENTORY_HEIGHT = 3;
    private static final int INVENTORY_WIDTH = 9;

    private HotbarLensImpl hotbar;
    private GridInventoryLensImpl grid;
    private boolean isContainer;

    public MainPlayerInventoryLensImpl(int base, SlotProvider slots, boolean isContainer) {
        this(base, MainPlayerInventoryAdapter.class, slots, isContainer);
    }

    public MainPlayerInventoryLensImpl(int base, Class<? extends Inventory> adapterType, SlotProvider slots, boolean isContainer) {
        super(base, 9, 4, adapterType, slots);
        this.isContainer = isContainer;

        this.lateInit(slots);
    }

    @Override
    protected void init(SlotProvider slots) {
    }

    private void lateInit(SlotProvider slots) {
        int base = this.base;

        int hotbarSize = PlayerInventory.func_70451_h();

        if (this.isContainer) {
            this.grid = new GridInventoryLensImpl(base, INVENTORY_WIDTH, MAIN_INVENTORY_HEIGHT, INVENTORY_WIDTH, slots);
            base += INVENTORY_WIDTH * 3;
            this.hotbar = new HotbarLensImpl(base - (hotbarSize - INVENTORY_WIDTH), hotbarSize, slots);
            /*
            1 |G|G|G|G|G|G|G|G|G|
            2 |G|G|G|G|G|G|G|G|G|
            3 |G|G|G|G|G|G|G|G|G|
            4 |H|H|H|H|H|H|H|H|H|
            */

            this.addSpanningChild(this.grid);
            this.addSpanningChild(this.hotbar);

            this.addChild(new GridInventoryLensImpl(this.base, INVENTORY_WIDTH, MAIN_INVENTORY_HEIGHT + 1, INVENTORY_WIDTH, slots));

        } else {
            this.hotbar = new HotbarLensImpl(base, hotbarSize, slots);
            base += INVENTORY_WIDTH;
            this.grid = new GridInventoryLensImpl(base, INVENTORY_WIDTH, MAIN_INVENTORY_HEIGHT, INVENTORY_WIDTH, slots);

            /*
            2 |G|G|G|G|G|G|G|G|G|
            3 |G|G|G|G|G|G|G|G|G|
            4 |G|G|G|G|G|G|G|G|G|
            1 |H|H|H|H|H|H|H|H|H|
            */

            this.addSpanningChild(this.hotbar);
            this.addSpanningChild(this.grid);

            // Shift slots so that Hotbar is always after the MainGrid
            ShiftedSlotProvider shiftedSlots = new ShiftedSlotProvider(slots, INVENTORY_WIDTH, INVENTORY_WIDTH * 4);
            this.addChild(new GridInventoryLensImpl(this.base, INVENTORY_WIDTH, MAIN_INVENTORY_HEIGHT + 1, INVENTORY_WIDTH, shiftedSlots));
        }

        this.cache();
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
        public SlotLens getSlot(int index) {
            index = index + this.shiftBy;
            if (index >= this.shiftAt) {
                index -= this.shiftAt;
            }
            return this.provider.getSlot(index);
        }
    }

    @Override
    protected void init(SlotProvider slots, boolean spanning) {
    }

    @Override
    public InventoryAdapter getAdapter(Fabric fabric, Inventory parent) {
        return new MainPlayerInventoryAdapter(fabric, this, parent);
    }

    @Override
    public HotbarLens getHotbar() {
        return this.hotbar;
    }

    @Override
    public GridInventoryLens getGrid() {
        return this.grid;
    }
}
