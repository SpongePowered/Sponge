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

import net.minecraft.inventory.IInventory;
import org.spongepowered.api.entity.ArmorEquipable;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.equipment.EquipmentInventory;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.property.EquipmentSlotType;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.comp.OrderedInventoryLens;

import java.util.Optional;

// TODO Inventory - Implement EquipmentInventory

public class EquipmentInventoryAdapter extends OrderedInventoryAdapter implements EquipmentInventory {

    private final ArmorEquipable carrier;

    public EquipmentInventoryAdapter(ArmorEquipable carrier, Fabric<IInventory> inventory, OrderedInventoryLens<IInventory,
            net.minecraft.item.ItemStack> root) {
        super(inventory, root);
        this.carrier = carrier;
    }

    public EquipmentInventoryAdapter(ArmorEquipable carrier, Fabric<IInventory> inventory, OrderedInventoryLens<IInventory,
            net.minecraft.item.ItemStack> root, Inventory parent) {
        super(inventory, root, parent);
        this.carrier = carrier;
    }

    @Override
    public Optional<ArmorEquipable> getCarrier() {
        return Optional.ofNullable(this.carrier);
    }

    @Override
    public Optional<ItemStack> poll(EquipmentSlotType equipmentType) {
        return null;
    }

    @Override
    public Optional<ItemStack> poll(EquipmentSlotType equipmentType, int limit) {
        return null;
    }

    @Override
    public Optional<ItemStack> poll(EquipmentType equipmentType) {
        return null;
    }

    @Override
    public Optional<ItemStack> poll(EquipmentType equipmentType, int limit) {
        return null;
    }

    @Override
    public Optional<ItemStack> peek(EquipmentSlotType equipmentType) {
        return null;
    }

    @Override
    public Optional<ItemStack> peek(EquipmentSlotType equipmentType, int limit) {
        return null;
    }

    @Override
    public Optional<ItemStack> peek(EquipmentType equipmentType) {
        return null;
    }

    @Override
    public Optional<ItemStack> peek(EquipmentType equipmentType, int limit) {
        return null;
    }

    @Override
    public InventoryTransactionResult set(EquipmentSlotType equipmentType, ItemStack stack) {
        return null;
    }

    @Override
    public InventoryTransactionResult set(EquipmentType equipmentType, ItemStack stack) {
        return null;
    }

    @Override
    public Optional<Slot> getSlot(EquipmentSlotType equipmentType) {
        return null;
    }

    @Override
    public Optional<Slot> getSlot(EquipmentType equipmentType) {
        return null;
    }
}
