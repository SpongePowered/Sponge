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

import static com.google.common.base.Preconditions.*;

import net.minecraft.inventory.IInventory;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.item.inventory.type.OrderedInventory;
import org.spongepowered.common.item.inventory.adapter.impl.Adapter;
import org.spongepowered.common.item.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.comp.OrderedInventoryLens;
import org.spongepowered.common.item.inventory.lens.slots.SlotLens;

import java.util.Optional;

public class OrderedInventoryAdapter extends Adapter implements OrderedInventory {

    protected final OrderedInventoryLens<IInventory, net.minecraft.item.ItemStack> orderedLens;

    public OrderedInventoryAdapter(Fabric<IInventory> inventory, OrderedInventoryLens<IInventory, net.minecraft.item.ItemStack> root) {
        this(inventory, root, null);
    }

    public OrderedInventoryAdapter(Fabric<IInventory> inventory, OrderedInventoryLens<IInventory, net.minecraft.item.ItemStack> root, Inventory parent) {
        super(inventory, checkNotNull(root), parent);
        this.orderedLens = root;
    }

    protected SlotLens<IInventory, net.minecraft.item.ItemStack> getSlotLens(int index) {
        if (index < 0) {
            return null;
        }
        if (this.orderedLens.hasSlotRealIndex(index)) {
            return this.orderedLens.getSlot(index);
        }
        return null;
        /*for (Lens<IInventory, net.minecraft.item.ItemStack> lens : orderedLens) {
            if (lens.getSlots().contains(index)) {*/
                /*
                if (lens instanceof OrderedInventoryLens) {
                    ((OrderedInventoryLens) lens).getSlot(orderedLens.)
                }*/
                // TODO getting OrderedInventoryLensImpl here instead of SlotLens
                //return (SlotLens<IInventory, net.minecraft.item.ItemStack>) lens;
                /*SlotAdapter adapter = lens.getAdapter(this.inventory, this).query(SlotAdapter.class).first();
                return (SlotLens<IInventory, net.minecraft.item.ItemStack>) adapter.getRootLens();
            }
        }
        return null;*/
    }

    protected SlotLens<IInventory, net.minecraft.item.ItemStack> getSlotLens(SlotIndex index) {
        return this.getSlotLens(index.getValue().intValue());
    }

    @Override
    public Optional<Slot> getSlot(SlotIndex index) {
        return Adapter.forSlot(this.inventory, this.getSlotLens(index), this);
    }

    @Override
    public Optional<ItemStack> poll(SlotIndex index) {
        return Adapter.Logic.pollSequential(this.inventory, this.getSlotLens(index));
    }

    @Override
    public Optional<org.spongepowered.api.item.inventory.ItemStack> poll(SlotIndex index, int limit) {
        return Adapter.Logic.pollSequential(this.inventory, this.getSlotLens(index), limit);
    }

    @Override
    public Optional<org.spongepowered.api.item.inventory.ItemStack> peek(SlotIndex index) {
        return Adapter.Logic.peekSequential(this.inventory, this.getSlotLens(index));
    }

    @Override
    public Optional<org.spongepowered.api.item.inventory.ItemStack> peek(SlotIndex index, int limit) {
        return Adapter.Logic.peekSequential(this.inventory, this.getSlotLens(index), limit);
    }

    @Override
    public InventoryTransactionResult set(SlotIndex index, org.spongepowered.api.item.inventory.ItemStack stack) {
        return Adapter.Logic.insertSequential(this.inventory, this.getSlotLens(index), stack);
    }

}
