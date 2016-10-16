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
package org.spongepowered.common.item.inventory.custom;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.InventoryProperty;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.MinecraftLens;
import org.spongepowered.common.item.inventory.lens.impl.comp.GridInventoryLensImpl;

import java.util.Map;

public class CustomLens extends MinecraftLens {

    private InventoryArchetype archetype;
    private Map<String, InventoryProperty> properties;

    public CustomLens(InventoryAdapter<IInventory, ItemStack> adapter, SlotProvider<IInventory, ItemStack> slots, InventoryArchetype archetype,
            Map<String, InventoryProperty> properties) {
        super(0, adapter.getInventory().getSize(), adapter, slots);
        this.archetype = archetype;
        this.properties = properties;
        this.init(slots);
    }

    @Override
    protected void init(SlotProvider<IInventory, ItemStack> slots) {

        InventoryDimension dimension = (InventoryDimension) archetype.getProperty(CustomInventory.INVENTORY_DIMENSION).orElse(null);
        if (dimension != null) {
            Lens<IInventory, ItemStack> lens = new GridInventoryLensImpl(0, dimension.getColumns(), dimension.getRows(), dimension.getColumns(), slots);
            this.addSpanningChild(lens);
        }
        else {
            int base = 0;
            for (InventoryArchetype childArchetype : archetype.getChildArchetypes()) {
                dimension = childArchetype.getProperty(InventoryDimension.class, CustomInventory.INVENTORY_DIMENSION).get();
                this.addSpanningChild(new GridInventoryLensImpl(base, dimension.getColumns(), dimension.getRows(), dimension.getColumns(), slots));
                base += dimension.getColumns() * dimension.getRows();
            }
        }
    }

    @Override
    protected boolean isDelayedInit() {
        return true;
    }
}
