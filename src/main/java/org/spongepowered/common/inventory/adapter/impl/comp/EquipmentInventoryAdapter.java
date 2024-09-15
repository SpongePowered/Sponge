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
package org.spongepowered.common.inventory.adapter.impl.comp;

import org.spongepowered.api.data.KeyValueMatcher;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.item.inventory.Equipable;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackLike;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.equipment.EquipmentInventory;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.common.inventory.adapter.impl.BasicInventoryAdapter;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.impl.comp.ArmorInventoryLens;
import org.spongepowered.common.inventory.lens.impl.comp.EquipmentInventoryLens;

import java.util.Optional;

public class EquipmentInventoryAdapter extends BasicInventoryAdapter implements EquipmentInventory {

    private final Equipable carrier;

    public EquipmentInventoryAdapter(Equipable carrier, Fabric fabric, EquipmentInventoryLens root, Inventory parent) {
        super(fabric, root, parent);
        this.carrier = carrier;
    }

    public EquipmentInventoryAdapter(Equipable carrier, Fabric fabric, ArmorInventoryLens root, Inventory parent) {
        super(fabric, root, parent);
        this.carrier = carrier;
    }

    @Override
    public Optional<Equipable> carrier() {
        return Optional.ofNullable(this.carrier);
    }

    @Override
    public InventoryTransactionResult.Poll poll(EquipmentType equipmentType) {
        return this.queryForType(equipmentType).poll();
    }

    @Override
    public InventoryTransactionResult.Poll poll(EquipmentType equipmentType, int limit) {
        return this.queryForType(equipmentType).poll(limit);
    }

    @Override
    public Optional<ItemStack> peek(EquipmentType equipmentType) {
        Inventory query = this.queryForType(equipmentType);
        if (query.capacity() == 0) {
            return Optional.empty();
        }
        return Optional.of(query.peek());
    }

    @Override
    public InventoryTransactionResult set(EquipmentType equipmentType, ItemStackLike stack) {
        Inventory query = this.queryForType(equipmentType);
        if (query.capacity() == 0) {
            return InventoryTransactionResult.builder().type(InventoryTransactionResult.Type.NO_SLOT).build();
        }
        return query.set(0, stack);
    }

    @Override
    public Optional<Slot> slot(EquipmentType equipmentType) {
        Inventory slot = this.queryForType(equipmentType);
        if (slot instanceof Slot) {
            return Optional.of((Slot) slot);
        }
        return Optional.empty();
    }

    private Inventory queryForType(EquipmentType equipmentType) {
        return this.query(KeyValueMatcher.of(Keys.EQUIPMENT_TYPE, equipmentType));
    }
}
