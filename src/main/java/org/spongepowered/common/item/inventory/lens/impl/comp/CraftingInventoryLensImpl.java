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

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.comp.CraftingInventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.comp.GridInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.comp.CraftingGridInventoryLens;
import org.spongepowered.common.item.inventory.lens.comp.CraftingInventoryLens;
import org.spongepowered.common.item.inventory.lens.slots.CraftingOutputSlotLens;

public class CraftingInventoryLensImpl extends OrderedInventoryLensImpl implements CraftingInventoryLens<IInventory, ItemStack> {

    private final int outputSlotIndex;

    private final CraftingOutputSlotLens<IInventory, ItemStack> outputSlot;

    private final CraftingGridInventoryLens<IInventory, ItemStack> craftingGrid;


    public CraftingInventoryLensImpl(int outputSlotIndex, int gridBase, int width, int height, SlotProvider<IInventory, ItemStack> slots) {
        this(outputSlotIndex, gridBase, width, height, GridInventoryAdapter.class, slots);
    }

    public CraftingInventoryLensImpl(int outputSlotIndex, int gridBase, int width, int height, Class<? extends Inventory> adapterType, SlotProvider<IInventory, ItemStack> slots) {
        super(gridBase, width * height, 1, adapterType, slots);
        this.outputSlotIndex = outputSlotIndex;
        this.outputSlot = (CraftingOutputSlotLens<IInventory, ItemStack>)slots.getSlot(this.outputSlotIndex);
        this.craftingGrid = new CraftingGridInventoryLensImpl(this.base, width, height, width, slots);
        this.size += 1; // output slot
        // Avoid the init() method in the superclass calling our init() too early
        this.initOther();
    }

    @Override
    protected void init(SlotProvider<IInventory, ItemStack> slots) {
        for (int ord = 0, slot = this.base; ord < this.size; ord++, slot += this.stride) {
            this.addChild(slots.getSlot(slot), new SlotIndex(ord));
        }
    }

    private void initOther() {
        this.addSpanningChild(this.outputSlot);
        this.addSpanningChild(this.craftingGrid);
        this.cache();
    }

    @Override
    public CraftingGridInventoryLens<IInventory, ItemStack> getCraftingGrid() {
        return this.craftingGrid;
    }

    @Override
    public CraftingOutputSlotLens<IInventory, ItemStack> getOutputSlot() {
        return this.outputSlot;
    }

    @Override
    public ItemStack getOutputStack(Fabric<IInventory> inv) {
        return this.outputSlot.getStack(inv);
    }

    @Override
    public boolean setOutputStack(Fabric<IInventory> inv, ItemStack stack) {
        return this.outputSlot.setStack(inv, stack);
    }

    @Override
    public int getRealIndex(Fabric<IInventory> inv, int ordinal) {
        if (!this.checkOrdinal(ordinal)) {
            return -1;
        }
        if (ordinal < this.size - 1) {
            return super.getRealIndex(inv, ordinal);
        }
        return this.outputSlotIndex;
    }

    @Override
    public InventoryAdapter<IInventory, ItemStack> getAdapter(Fabric<IInventory> inv, Inventory parent) {
        return new CraftingInventoryAdapter(inv, this, parent);
    }

}
