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
package org.spongepowered.common.inventory.slot;

import net.minecraft.inventory.IInventory;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryProperty;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.slot.EquipmentSlot;
import org.spongepowered.api.text.translation.Translatable;

public class SpongeEquipmentSlot extends AbstractInventorySlot implements EquipmentSlot {
    private final EquipmentType type;

    public SpongeEquipmentSlot(EquipmentType type, IInventory inventory, int index) {
        super(inventory, index);
        this.type = type;
    }

    @Override
    public boolean isValidItem(EquipmentType type) {
        return this.type.equals(type);
    }

    @Override
    public boolean isValidItem(ItemStack stack) {
        // TODO Figure out EquipmentType from stack
        return false;
    }

    @Override
    public boolean isValidItem(ItemType type) {
        // TODO Figure out EquipmentType from ItemType
        return false;
    }

    @Override public <T extends Inventory> T query(Class<?>... types) {
        return null;
    }

    @Override public <T extends Inventory> T query(ItemType... types) {
        return null;
    }

    @Override public <T extends Inventory> T query(ItemStack... types) {
        return null;
    }

    @Override public <T extends Inventory> T query(InventoryProperty<?, ?>... props) {
        return null;
    }

    @Override public <T extends Inventory> T query(Translatable... names) {
        return null;
    }

    @Override public <T extends Inventory> T query(String... names) {
        return null;
    }

    @Override public <T extends Inventory> T query(Object... args) {
        return null;
    }
}
