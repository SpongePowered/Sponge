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

import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.crafting.CraftingGridInventory;
import org.spongepowered.common.accessor.world.inventory.AbstractCraftingMenuAccessor;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.fabric.OffsetFabric;
import org.spongepowered.common.inventory.lens.impl.AbstractLens;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensProvider;
import org.spongepowered.common.inventory.property.KeyValuePair;

public class CraftingGridInventoryLens extends AbstractLens {

    private GridInventoryLens grid;

    public CraftingGridInventoryLens(int base, int width, int height, SlotLensProvider slots) {
        super(base, width * height, CraftingGridInventory.class);
        this.init(slots, width, height);
    }

    private void init(SlotLensProvider slots, int width, int height) {
        // Adding slots
        for (int ord = 0, slot = this.base; ord < this.size; ord++, slot++) {
            this.addChild(slots.getSlotLens(slot), KeyValuePair.slotIndex(ord));
        }

        this.grid = new GridInventoryLens(this.base, width, height, slots);
        this.addSpanningChild(this.grid);
    }

    public GridInventoryLens getGrid() {
        return this.grid;
    }

    @Override
    public Inventory getAdapter(Fabric fabric, Inventory parent) {
        if (fabric instanceof OffsetFabric) {
            fabric = ((OffsetFabric) fabric).fabric();
        }
        if (fabric instanceof CraftingGridInventory) {
            return ((CraftingGridInventory) fabric);
        }
        if (fabric instanceof InventoryMenu) {
            return ((CraftingGridInventory) ((InventoryMenu) fabric).getCraftSlots());
        }
        if (fabric instanceof CraftingMenu) {
            return ((CraftingGridInventory) ((AbstractCraftingMenuAccessor) fabric).accessor$craftSlots());
        }
        throw new IllegalStateException(fabric.getClass().getName() + " is not a known CraftingGridInventory.");
    }
}
