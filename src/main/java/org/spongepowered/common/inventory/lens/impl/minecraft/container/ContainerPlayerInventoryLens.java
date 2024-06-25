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
package org.spongepowered.common.inventory.lens.impl.minecraft.container;

import org.spongepowered.api.data.Keys;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.lens.impl.comp.CraftingInventoryLens;
import org.spongepowered.common.inventory.lens.impl.minecraft.PlayerInventoryLens;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensProvider;
import org.spongepowered.common.inventory.property.KeyValuePair;

import java.util.Arrays;
import java.util.List;

public class ContainerPlayerInventoryLens extends ContainerLens {

    private static final int CRAFTING_OUTPUT = 1;
    private static final int CRAFTING_GRID = 2;

    public ContainerPlayerInventoryLens(int size, Class<? extends Inventory> adapter, SlotLensProvider slots) {
        super(size, adapter, slots, ContainerPlayerInventoryLens.lenses(size, slots));
    }

    private static List<Lens> lenses(int size, SlotLensProvider slots) {
        int base = ContainerPlayerInventoryLens.CRAFTING_OUTPUT; // 1
        final CraftingInventoryLens crafting = new CraftingInventoryLens(0, base, ContainerPlayerInventoryLens.CRAFTING_GRID,
            ContainerPlayerInventoryLens.CRAFTING_GRID, slots);
        base += ContainerPlayerInventoryLens.CRAFTING_GRID * ContainerPlayerInventoryLens.CRAFTING_GRID; // 4
        final PlayerInventoryLens player = new PlayerInventoryLens(base, size - base, slots);
        return Arrays.asList(crafting, player);
    }

    @Override
    protected void init(SlotLensProvider slots) {
        super.init(slots);

        this.addChild(slots.getSlotLens(this.base + 0), KeyValuePair.of(Keys.EQUIPMENT_TYPE, EquipmentTypes.HEAD.get()));
        this.addChild(slots.getSlotLens(this.base + 1), KeyValuePair.of(Keys.EQUIPMENT_TYPE, EquipmentTypes.CHEST.get()));
        this.addChild(slots.getSlotLens(this.base + 2), KeyValuePair.of(Keys.EQUIPMENT_TYPE, EquipmentTypes.LEGS.get()));
        this.addChild(slots.getSlotLens(this.base + 3), KeyValuePair.of(Keys.EQUIPMENT_TYPE, EquipmentTypes.FEET.get()));
        this.addChild(slots.getSlotLens(this.base + 4 + 4 * 9), KeyValuePair.of(Keys.EQUIPMENT_TYPE, EquipmentTypes.OFFHAND.get()));
    }

}
