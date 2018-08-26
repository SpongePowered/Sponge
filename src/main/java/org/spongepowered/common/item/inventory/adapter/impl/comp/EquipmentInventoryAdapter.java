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
package org.spongepowered.common.item.inventory.adapter.impl.comp;

import org.spongepowered.api.data.property.PropertyMatcher;
import org.spongepowered.api.entity.Equipable;
import org.spongepowered.api.item.inventory.*;
import org.spongepowered.api.item.inventory.equipment.EquipmentInventory;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.common.item.inventory.adapter.impl.BasicInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.comp.EquipmentInventoryLens;

import java.util.List;
import java.util.Optional;

public class EquipmentInventoryAdapter extends BasicInventoryAdapter implements EquipmentInventory {

    private final Equipable carrier;

    public EquipmentInventoryAdapter(Equipable carrier, Fabric inventory, EquipmentInventoryLens root, Inventory parent) {
        super(inventory, root, parent);
        this.carrier = carrier;
    }

    @Override
    public Optional<Equipable> getCarrier() {
        return Optional.ofNullable(this.carrier);
    }

    @Override
    public Optional<ItemStack> poll(EquipmentType equipmentType) {
        return getSlot(equipmentType).map(Inventory::poll);
    }

    @Override
    public Optional<ItemStack> poll(EquipmentType equipmentType, int limit) {
        return getSlot(equipmentType).map(slot -> slot.poll(limit));
    }

    @Override
    public Optional<ItemStack> peek(EquipmentType equipmentType) {
        return getSlot(equipmentType).map(Inventory::peek);
    }

    @Override
    public Optional<ItemStack> peek(EquipmentType equipmentType, int limit) {
        return getSlot(equipmentType).map(slot -> slot.peek(limit));
    }

    @Override
    public InventoryTransactionResult set(EquipmentType equipmentType, ItemStack stack) {
        return getSlot(equipmentType).map(slot -> slot.set(stack)).orElseGet(() ->
                InventoryTransactionResult.builder().type(InventoryTransactionResult.Type.FAILURE).reject(stack).build());
    }

    @Override
    public Optional<Slot> getSlot(EquipmentType equipmentType) {
        final List<Slot> slots = query(PropertyMatcher.of(InventoryProperties.EQUIPMENT_TYPE, equipmentType)).slots();
        return slots.isEmpty() ? Optional.empty() : Optional.of(slots.get(0));
    }
}
