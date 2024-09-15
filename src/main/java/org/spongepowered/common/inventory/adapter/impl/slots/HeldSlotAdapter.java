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
package org.spongepowered.common.inventory.adapter.impl.slots;

import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackLike;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.slot.EquipmentSlot;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.impl.slot.HeldHandSlotLens;

import java.util.function.Predicate;

public class HeldSlotAdapter extends SlotAdapter implements EquipmentSlot {

    protected final HeldHandSlotLens equipmentSlot;

    public HeldSlotAdapter(Fabric fabric, HeldHandSlotLens lens, Inventory parent) {
        super(fabric, lens, parent);
        this.equipmentSlot = lens;
    }

    @Override
    public boolean isValidItem(EquipmentType type) {
        Predicate<EquipmentType> filter = this.equipmentSlot.getEquipmentTypeFilter();
        return filter == null || filter.test(type);
    }

    @Override
    public boolean isValidItem(ItemStackLike stack) {
        Predicate<ItemStackLike> filter = this.equipmentSlot.getItemStackFilter();
        return filter == null || filter.test(stack);
    }

    @Override
    public boolean isValidItem(ItemType type) {
        Predicate<ItemType> filter = this.equipmentSlot.getItemTypeFilter();
        return filter == null || filter.test(type);
    }

    @Override
    public InventoryTransactionResult set(ItemStackLike stack) {
        final boolean canSet = this.isValidItem(stack);
        if (!canSet) {
            final InventoryTransactionResult.Builder result = InventoryTransactionResult.builder().type(InventoryTransactionResult.Type.FAILURE);
            result.reject(stack);
            return result.build();
        }

        return super.set(stack);
    }
}
