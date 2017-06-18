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
package org.spongepowered.common.item.inventory.util;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import org.spongepowered.api.item.inventory.crafting.CraftingGridInventory;
import org.spongepowered.common.item.inventory.adapter.impl.comp.CraftingGridInventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.comp.CraftingInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.comp.CraftingGridInventoryLens;
import org.spongepowered.common.item.inventory.lens.impl.comp.CraftingGridInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.fabric.DefaultInventoryFabric;
import org.spongepowered.common.item.inventory.lens.impl.slots.SlotLensImpl;

import java.util.Iterator;

public final class InventoryUtil {

    private InventoryUtil() {}

    public static CraftingGridInventory toSpongeInventory(InventoryCrafting inv) {
        DefaultInventoryFabric fabric = new DefaultInventoryFabric(inv);
        CraftingGridInventoryLens lens = new CraftingGridInventoryLensImpl(0, inv.getWidth(), inv.getHeight(), inv.getWidth(), SlotLensImpl::new);

        return new CraftingGridInventoryAdapter(fabric, lens);
    }

    public static InventoryCrafting toNativeInventory(CraftingGridInventory inv) {
        Fabric<IInventory> fabric = ((CraftingInventoryAdapter) inv).getInventory();
        Iterator<IInventory> inventories = fabric.allInventories().iterator();
        InventoryCrafting inventoryCrafting = (InventoryCrafting) inventories.next();

        if (inventories.hasNext()) {
            throw new IllegalStateException("Another inventory found: " + inventories.next());
        }

        return inventoryCrafting;
    }

}
