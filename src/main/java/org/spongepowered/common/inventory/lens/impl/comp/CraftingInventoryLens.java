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

import net.minecraft.world.item.ItemStack;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.crafting.CraftingInventory;
import org.spongepowered.common.inventory.adapter.impl.comp.CraftingInventoryAdapter;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.impl.DefaultIndexedLens;
import org.spongepowered.common.inventory.lens.impl.slot.CraftingOutputSlotLens;
import org.spongepowered.common.inventory.lens.impl.slot.FilteringSlotLens;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensProvider;
import org.spongepowered.common.inventory.property.KeyValuePair;

public class CraftingInventoryLens extends DefaultIndexedLens {

    private final int outputSlotIndex;

    private final CraftingOutputSlotLens outputSlot;
    private final CraftingGridInventoryLens craftingGrid;

    public CraftingInventoryLens(int outputSlotIndex, int gridBase, int width, int height, SlotLensProvider slots) {
        super(0, width * height + 1, 1, CraftingInventory.class, slots);
        this.outputSlotIndex = outputSlotIndex;
        this.outputSlot = new CraftingOutputSlotLens(slots.getSlotLens(this.outputSlotIndex), FilteringSlotLens.ItemStackFilter.filterNone());;
        this.craftingGrid = new CraftingGridInventoryLens(gridBase, width, height, slots);
        this.addChild(this.outputSlot, KeyValuePair.slotIndex(0));
        this.addChild(this.craftingGrid);
    }

    public CraftingGridInventoryLens getCraftingGrid() {
        return this.craftingGrid;
    }

    public CraftingOutputSlotLens getOutputSlot() {
        return this.outputSlot;
    }

    public ItemStack getOutputStack(Fabric fabric) {
        return this.outputSlot.getStack(fabric);
    }

    public boolean setOutputStack(Fabric fabric, ItemStack stack) {
        return this.outputSlot.setStack(fabric, stack);
    }

    @Override
    public Inventory getAdapter(Fabric fabric, Inventory parent) {
        return new CraftingInventoryAdapter(fabric, this, parent);
    }

}
