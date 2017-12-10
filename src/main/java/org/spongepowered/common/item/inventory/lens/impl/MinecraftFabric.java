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
package org.spongepowered.common.item.inventory.lens.impl;

import static com.google.common.base.Preconditions.*;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.UnsupportedFabricException;
import org.spongepowered.common.item.inventory.lens.impl.fabric.CompoundFabric;
import org.spongepowered.common.item.inventory.lens.impl.fabric.ContainerFabric;
import org.spongepowered.common.item.inventory.lens.impl.fabric.IInventoryFabric;
import org.spongepowered.common.item.inventory.lens.impl.fabric.SlotFabric;

public abstract class MinecraftFabric implements Fabric<IInventory> {

    @SuppressWarnings("unchecked")
    public static <TFabric> Fabric<IInventory> of(TFabric target) {
        checkNotNull(target, "Fabric target");
        if (target instanceof Fabric) {
            return (Fabric<IInventory>) target;
        } else if (target instanceof Slot) {
            Slot slot = (Slot)target;
            if (slot.inventory == null) {
                return new SlotFabric(slot);
            }
            return new IInventoryFabric(slot.inventory);
        } else if (target instanceof Container) {
            return new ContainerFabric((Container) target);
        } else if (target instanceof InventoryLargeChest) {
            return new CompoundFabric(new IInventoryFabric(((InventoryLargeChest) target).upperChest), new IInventoryFabric(((InventoryLargeChest) target).lowerChest));
        } else if (target instanceof IInventory) {
            return new IInventoryFabric((IInventory) target);
        }
        throw new UnsupportedFabricException("Container of type %s could not be used as an inventory fabric", target.getClass());
    }

    public static InventoryAdapter<IInventory, ItemStack> getAdapter(Fabric<IInventory> fabric, Inventory parent, int base, Class<? extends Inventory> adapterType) {
        IInventory inventory = fabric.get(base);
        if (inventory.getClass() == adapterType) {
            return ((InventoryAdapter) inventory);
        }
        if (fabric instanceof ContainerFabric) {
            inventory = of(inventory).get(base);
            if (inventory.getClass() == adapterType) {
                return ((InventoryAdapter) inventory);
            }
        }
        return null;
    }
}
