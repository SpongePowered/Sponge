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
package org.spongepowered.common.inventory.lens.impl.slot;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Equipable;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.common.inventory.adapter.impl.slots.EquipmentSlotAdapter;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.lens.slots.SlotLens;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.function.Predicate;

public class EquipmentSlotLens extends FilteringSlotLens {

    private final Predicate<EquipmentType> equipmentTypeFilter;

    public EquipmentSlotLens(Lens parent, SlotLens lens, EquipmentType type) {
        super(lens, EquipmentSlotLens.equipmentTypeFilter(type), EquipmentSlotAdapter.class);
        this.equipmentTypeFilter = e -> e == type;
        this.setParent(parent);
    }

    private static FilteringSlotLens.ItemStackFilter equipmentTypeFilter(EquipmentType type) {
        return (fabric, item) -> {
            if (item.isEmpty()) {
                return true;
            }
            final var equipable = Equipable.get(ItemStackUtil.fromLikeToNative(item));
            final var itemSlotType = equipable != null ? equipable.getEquipmentSlot() : EquipmentSlot.MAINHAND;
            return itemSlotType == (Object) type;
        };
    }

    public Predicate<EquipmentType> getEquipmentTypeFilter() {
        return this.equipmentTypeFilter;
    }

    @Override
    public Slot getAdapter(Fabric fabric, Inventory parent) {
        return new EquipmentSlotAdapter(fabric, this, parent);
    }

}
