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

import org.spongepowered.api.entity.ArmorEquipable;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.item.inventory.property.EquipmentSlotType;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.comp.EquipmentInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.comp.EquipmentInventoryLens;

import java.util.Optional;

public class EquipmentInventoryLensImpl extends OrderedInventoryLensImpl implements EquipmentInventoryLens {

    public EquipmentInventoryLensImpl(int base, int size, int stride, SlotProvider slots, boolean isContainer) {
        super(base, size, stride, EquipmentInventoryAdapter.class, slots);
        if (isContainer) {
            this.initContainer(slots);
        } else {
            this.initInventory(slots);
        }
    }

    @Override
    protected void init(SlotProvider slots) {
    }

    private void initInventory(SlotProvider slots) {
        int index = this.base;
        int ord = 0;
        this.addSpanningChild(slots.getSlot(index), new SlotIndex(ord++), EquipmentSlotType.of(EquipmentTypes.BOOTS));
        index += this.stride;
        this.addSpanningChild(slots.getSlot(index), new SlotIndex(ord++), EquipmentSlotType.of(EquipmentTypes.LEGGINGS));
        index += this.stride;
        this.addSpanningChild(slots.getSlot(index), new SlotIndex(ord++), EquipmentSlotType.of(EquipmentTypes.CHESTPLATE));
        index += this.stride;
        this.addSpanningChild(slots.getSlot(index), new SlotIndex(ord), EquipmentSlotType.of(EquipmentTypes.HEADWEAR));

        this.cache();
    }

    private void initContainer(SlotProvider slots) {
        int index = this.base;
        int ord = 0;
        this.addSpanningChild(slots.getSlot(index), new SlotIndex(ord++), EquipmentSlotType.of(EquipmentTypes.HEADWEAR));
        index += this.stride;
        this.addSpanningChild(slots.getSlot(index), new SlotIndex(ord++), EquipmentSlotType.of(EquipmentTypes.CHESTPLATE));
        index += this.stride;
        this.addSpanningChild(slots.getSlot(index), new SlotIndex(ord++), EquipmentSlotType.of(EquipmentTypes.LEGGINGS));
        index += this.stride;
        this.addSpanningChild(slots.getSlot(index), new SlotIndex(ord), EquipmentSlotType.of(EquipmentTypes.BOOTS));

        this.cache();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public InventoryAdapter getAdapter(Fabric inv, Inventory parent) {
        ArmorEquipable carrier = null;
        if (parent instanceof CarriedInventory) {
            Optional opt = ((CarriedInventory) parent).getCarrier();
            if (opt.isPresent() && opt.get() instanceof ArmorEquipable) {
                carrier = ((ArmorEquipable) opt.get());
            }
        }
        return new EquipmentInventoryAdapter(carrier, inv, this, parent);
    }
}
