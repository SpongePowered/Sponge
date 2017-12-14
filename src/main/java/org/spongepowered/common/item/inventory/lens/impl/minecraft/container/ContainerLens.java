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
package org.spongepowered.common.item.inventory.lens.impl.minecraft.container;

import static org.spongepowered.api.data.Property.Operator.DELEGATE;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.RealLens;
import org.spongepowered.common.item.inventory.property.SlotIndexImpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ContainerLens extends RealLens {

    // The viewed inventories
    protected List<Lens<IInventory, ItemStack>> viewedInventories;
    private List<Lens<IInventory, ItemStack>> additonal;

    public ContainerLens(InventoryAdapter<IInventory, ItemStack> adapter, SlotProvider<IInventory, ItemStack> slots,
            List<Lens<IInventory, ItemStack>> lenses) {
        this(adapter, slots, lenses, Collections.emptyList());
    }

    public ContainerLens(InventoryAdapter<IInventory, ItemStack> adapter, SlotProvider<IInventory, ItemStack> slots,
            List<Lens<IInventory, ItemStack>> lenses, List<Lens<IInventory, ItemStack>> additonal) {
        this(adapter, slots);
        this.viewedInventories = lenses;
        this.additonal = additonal;
        this.init(slots);
    }

    /**
     * Do not forget to call init when using this constructor!
     */
    public ContainerLens(InventoryAdapter<IInventory, ItemStack> adapter, SlotProvider<IInventory, ItemStack> slots) {
        super(0, adapter.getFabric().getSize(), adapter, slots);
        this.additonal = Collections.emptyList();
    }

    @Override
    protected void init(SlotProvider<IInventory, ItemStack> slots) {

        // Adding slots
        for (int ord = 0, slot = this.base; ord < this.size; ord++, slot++) {
            this.addChild(slots.getSlot(slot), new SlotIndexImpl(ord, DELEGATE));
        }

        // Adding spanning children
        for (Lens<IInventory, ItemStack> lens : this.viewedInventories) {
            this.addSpanningChild(lens);
        }

        // Adding additional lenses
        for (Lens<IInventory, ItemStack> lens : this.additonal) {
            this.addChild(lens);
        }

    }
}
