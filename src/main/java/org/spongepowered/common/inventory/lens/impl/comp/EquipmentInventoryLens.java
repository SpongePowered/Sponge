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

import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.item.inventory.Equipable;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.common.inventory.adapter.impl.comp.EquipmentInventoryAdapter;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.impl.RealLens;
import org.spongepowered.common.inventory.lens.impl.slot.EquipmentSlotLens;
import org.spongepowered.common.inventory.lens.slots.SlotLens;
import org.spongepowered.common.inventory.property.KeyValuePair;

import java.util.Map;
import java.util.Optional;

public class EquipmentInventoryLens extends RealLens {

    private final Map<EquipmentType, SlotLens> lenses;

    public EquipmentInventoryLens(Map<EquipmentType, SlotLens> lenses) {
        super(0, lenses.size(), EquipmentInventoryAdapter.class);
        this.lenses = lenses;
        this.init(this.lenses);
    }

    private void init(Map<EquipmentType, SlotLens> lenses) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            final EquipmentType type = (EquipmentType) (Object) slot;
            final SlotLens lensAtSlot = lenses.get(slot);
            if (lensAtSlot != null) {
                final EquipmentSlotLens equipmentSlotLens = new EquipmentSlotLens(this, lensAtSlot, type);
                this.addSpanningChild(equipmentSlotLens, KeyValuePair.of(Keys.EQUIPMENT_TYPE, type));
                this.lenses.put(type, equipmentSlotLens);
            }
        }
    }

    public SlotLens getSlotLens(EquipmentType equipmentType) {
        return this.lenses.get(equipmentType);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Inventory getAdapter(Fabric fabric, Inventory parent) {
        Equipable carrier = null;
        if (parent instanceof CarriedInventory) {
            Optional opt = ((CarriedInventory) parent).carrier();
            if (opt.isPresent() && opt.get() instanceof Equipable) {
                carrier = ((Equipable) opt.get());
            }
        }
        return new EquipmentInventoryAdapter(carrier, fabric, this, parent);
    }
}
