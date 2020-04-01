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

import org.spongepowered.api.data.Keys;
import org.spongepowered.api.item.inventory.ArmorEquipable;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.common.inventory.adapter.impl.comp.EquipmentInventoryAdapter;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.impl.SlotBasedLens;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensProvider;
import org.spongepowered.common.inventory.property.KeyValuePair;

import java.util.Optional;

public class ArmorInventoryLens extends SlotBasedLens {

    public ArmorInventoryLens(int base, SlotLensProvider slots, boolean isContainer) {
        super(base, 4, 1, EquipmentInventoryAdapter.class, slots);
        if (isContainer) {
            this.initContainer(slots);
        } else {
            this.initInventory(slots);
        }
    }

    private void initInventory(SlotLensProvider slots) {
        int index = this.base;

        this.addSpanningChild(slots.getSlotLens(index), KeyValuePair.of(Keys.EQUIPMENT_TYPE.get(), EquipmentTypes.BOOTS.get()));
        index += this.stride;
        this.addSpanningChild(slots.getSlotLens(index), KeyValuePair.of(Keys.EQUIPMENT_TYPE.get(), EquipmentTypes.LEGGINGS.get()));
        index += this.stride;
        this.addSpanningChild(slots.getSlotLens(index), KeyValuePair.of(Keys.EQUIPMENT_TYPE.get(), EquipmentTypes.CHESTPLATE.get()));
        index += this.stride;
        this.addSpanningChild(slots.getSlotLens(index), KeyValuePair.of(Keys.EQUIPMENT_TYPE.get(), EquipmentTypes.HEADWEAR.get()));
    }

    private void initContainer(SlotLensProvider slots) {
        int index = this.base;
        this.addSpanningChild(slots.getSlotLens(index), KeyValuePair.of(Keys.EQUIPMENT_TYPE.get(), EquipmentTypes.HEADWEAR.get()));
        index += this.stride;
        this.addSpanningChild(slots.getSlotLens(index), KeyValuePair.of(Keys.EQUIPMENT_TYPE.get(), EquipmentTypes.CHESTPLATE.get()));
        index += this.stride;
        this.addSpanningChild(slots.getSlotLens(index), KeyValuePair.of(Keys.EQUIPMENT_TYPE.get(), EquipmentTypes.LEGGINGS.get()));
        index += this.stride;
        this.addSpanningChild(slots.getSlotLens(index), KeyValuePair.of(Keys.EQUIPMENT_TYPE.get(), EquipmentTypes.BOOTS.get()));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Inventory getAdapter(Fabric fabric, Inventory parent) {
        ArmorEquipable carrier = null;
        if (parent instanceof CarriedInventory) {
            Optional opt = ((CarriedInventory) parent).getCarrier();
            if (opt.isPresent() && opt.get() instanceof ArmorEquipable) {
                carrier = ((ArmorEquipable) opt.get());
            }
        }
        // TODO
        return new EquipmentInventoryAdapter(carrier, fabric, this, parent);
    }
}