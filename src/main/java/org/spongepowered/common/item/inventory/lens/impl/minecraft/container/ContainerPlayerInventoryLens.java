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

import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.item.inventory.property.EquipmentSlotType;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.comp.CraftingInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.minecraft.PlayerInventoryLens;

import java.util.ArrayList;
import java.util.Arrays;

public class ContainerPlayerInventoryLens extends ContainerLens {

    private static final int CRAFTING_OUTPUT = 1;
    private static final int CRAFTING_GRID = 2;

    public ContainerPlayerInventoryLens(int size, Class<? extends Inventory> adapter, SlotProvider slots) {
        super(size, adapter);
        this.init(slots);
    }

    @Override
    protected void init(SlotProvider slots) {
        int base = CRAFTING_OUTPUT; // 1
        final CraftingInventoryLensImpl crafting = new CraftingInventoryLensImpl(0, base, CRAFTING_GRID, CRAFTING_GRID, slots);
        base += CRAFTING_GRID * CRAFTING_GRID; // 4
        final PlayerInventoryLens player = new PlayerInventoryLens(base, this.size - base, slots);
        this.addChild(slots.getSlot(base + 0), new EquipmentSlotType(EquipmentTypes.HEADWEAR));
        this.addChild(slots.getSlot(base + 1), new EquipmentSlotType(EquipmentTypes.CHESTPLATE));
        this.addChild(slots.getSlot(base + 2), new EquipmentSlotType(EquipmentTypes.LEGGINGS));
        this.addChild(slots.getSlot(base + 3), new EquipmentSlotType(EquipmentTypes.BOOTS));
        this.addChild(slots.getSlot(base + 4 + 4*9), new EquipmentSlotType(EquipmentTypes.OFF_HAND));

        this.viewedInventories = new ArrayList<>(Arrays.asList(crafting, player));

        super.init(slots);
    }
}
