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

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;

import java.lang.reflect.Constructor;

public abstract class MinecraftLens extends AbstractLens<IInventory, ItemStack> {

    public MinecraftLens(int base, int size, Class<? extends Inventory> adapterType, SlotProvider<IInventory, ItemStack> slots) {
        super(base, size, adapterType, slots);
    }

    public MinecraftLens(int base, int size, InventoryAdapter<IInventory, ItemStack> adapter, SlotProvider<IInventory, ItemStack> slots) {
        super(base, size, adapter, slots);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Constructor<InventoryAdapter<IInventory, ItemStack>> getAdapterCtor() throws NoSuchMethodException {
        try {
            return (Constructor<InventoryAdapter<IInventory, ItemStack>>)
                    this.adapterType.getConstructor(Fabric.class, this.getClass(), Inventory.class);
        } catch (Exception ex1) {
            return (Constructor<InventoryAdapter<IInventory, ItemStack>>)
                    this.adapterType.getConstructor(Fabric.class, Lens.class, Inventory.class);
        }
    }

    @Override
    public int getMaxStackSize(Fabric<IInventory> inv) {
        return inv.getMaxStackSize();
    }

    @Override
    public void invalidate(Fabric<IInventory> inv) {
        super.invalidate(inv);
//        inv.markDirty();    // Adapter can decide
    }
}
