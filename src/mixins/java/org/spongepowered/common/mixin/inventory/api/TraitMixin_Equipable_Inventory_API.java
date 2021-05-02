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
package org.spongepowered.common.mixin.inventory.api;

import org.spongepowered.api.item.inventory.Equipable;
import org.spongepowered.api.item.inventory.equipment.EquipmentInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.world.inventory.InventoryBridge;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.lens.impl.comp.EquipmentInventoryLens;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;

@Mixin(value = {ArmorStand.class, Mob.class})
public abstract class TraitMixin_Equipable_Inventory_API implements Equipable {

    private EquipmentInventory impl$equipmentInventory = null;

    @Override
    public EquipmentInventory equipment() {
        if (this.impl$equipmentInventory == null) {
            final InventoryAdapter inv = ((InventoryBridge) this).bridge$getAdapter();
            final EquipmentInventoryLens lens = (EquipmentInventoryLens) inv.inventoryAdapter$getRootLens();
            this.impl$equipmentInventory = (EquipmentInventory) lens.getAdapter(inv.inventoryAdapter$getFabric(), null);
        }
        return this.impl$equipmentInventory;
    }

}
