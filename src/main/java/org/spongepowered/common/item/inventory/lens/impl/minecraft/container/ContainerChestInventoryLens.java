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
package org.spongepowered.common.item.inventory.lens.impl.minecraft.container;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.block.tileentity.carrier.Chest;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.comp.GridInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.comp.HotbarLensImpl;

import java.util.Arrays;

/**
 * A {@link Lens} comprising of when a Minecraft-like {@link Chest} is opened.
 */
public class ContainerChestInventoryLens extends ContainerLens {

    private GridInventoryLensImpl playerInventory, chestInventory;

    private HotbarLensImpl hotbarInventory;
    private int numRows;

    public ContainerChestInventoryLens(InventoryAdapter<IInventory, ItemStack> adapter, SlotProvider<IInventory, ItemStack> slots, int numRows) {
        super(adapter, slots);
        this.numRows = numRows;
        this.init(slots);
    }

    @Override
    protected void init(SlotProvider<IInventory, ItemStack> slots) {
        // These use chest container ids (distinct from normal slot ids)
        this.chestInventory = new GridInventoryLensImpl(0, 9, this.numRows, 9, slots);
        // (9 * numRows) slots after the chest slots
        this.playerInventory = new GridInventoryLensImpl(9 * this.numRows, 9, 3, 9, slots);
        // Add an additional 27 slots, for the player inventory
        this.hotbarInventory = new HotbarLensImpl((9 * numRows) + 27, 9, slots);
        this.viewedInventories = Arrays.asList(this.chestInventory, this.playerInventory, this.hotbarInventory);

        // Add child inventories and basic slots with SlotIndex
        super.init(slots);
    }
}
