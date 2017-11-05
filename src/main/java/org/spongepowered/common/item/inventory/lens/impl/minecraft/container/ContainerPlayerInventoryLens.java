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
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.comp.MainPlayerInventoryLens;
import org.spongepowered.common.item.inventory.lens.impl.comp.CraftingInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.comp.EquipmentInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.comp.GridInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.comp.HotbarLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.comp.MainPlayerInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.comp.OrderedInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.slots.SlotLens;

import java.util.ArrayList;
import java.util.Arrays;

public class ContainerPlayerInventoryLens extends ContainerLens {

    private static final int CRAFTING_OUTPUT = 1;
    private static final int CRAFTING_GRID = 2;
    private static final int EQUIPMENT = 4;
    private static final int INVENTORY_WIDTH = 9;
    private static final int MAIN_INVENTORY_HEIGHT = 3;
    private static final int HOTBAR = 1;
    private static final int OFFHAND = 1;

    public ContainerPlayerInventoryLens(InventoryAdapter<IInventory, ItemStack> adapter, SlotProvider<IInventory, ItemStack> slots) {
        super(adapter, slots);
        this.init(slots);
    }

    @Override
    protected void init(SlotProvider<IInventory, ItemStack> slots) {
        int base = CRAFTING_OUTPUT; // 1
        final CraftingInventoryLensImpl crafting = new CraftingInventoryLensImpl(0, base, CRAFTING_GRID, CRAFTING_GRID, slots);
        base += CRAFTING_GRID * CRAFTING_GRID; // 4
        // TODO pass player for carrier to EquipmentInventory
        final EquipmentInventoryLensImpl armor = new EquipmentInventoryLensImpl(null, base, EQUIPMENT, 1, slots);
        base += EQUIPMENT; // 4
        final MainPlayerInventoryLensImpl main = new MainPlayerInventoryLensImpl(base, slots);
        base += MAIN_INVENTORY_HEIGHT * INVENTORY_WIDTH + HOTBAR * INVENTORY_WIDTH; // 9
        final SlotLens<IInventory, ItemStack> offHand = slots.getSlot(base);
        base += OFFHAND;

        this.viewedInventories = new ArrayList<>(Arrays.asList(crafting, armor, offHand, main));

        int additionalSlots = this.size - base - 1;
        if (additionalSlots > 0) {
            viewedInventories.add(new OrderedInventoryLensImpl(base, additionalSlots, 1, slots));
        }

        // TODO actual Container order is:
        // CraftingOutput (1) -> Crafting (4) -> ArmorSlots (4) -> MainInventory (27) -> Hotbar (9) -> Offhand (1)
        // how to handle issues like in #939? ; e.g. Inventory#offer using a different insertion order
        super.init(slots);
    }
}
