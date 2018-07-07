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

import net.minecraft.item.ItemStack;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryProperty;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.slots.SlotLens;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class DefaultEmptyLens implements Lens {

    protected final InventoryAdapter adapter;
    
    public DefaultEmptyLens(InventoryAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public Class<? extends Inventory> getAdapterType() {
        return this.adapter.getClass();
    }

    @Override
    public InventoryAdapter getAdapter(Fabric inv, Inventory parent) {
        return this.adapter;
    }
    
    @Override
    public Translation getName(Fabric inv) {
        return inv.getDisplayName();
    }

    @Override
    public int slotCount() {
        return 0;
    }

    @Override
    public ItemStack getStack(Fabric inv, int ordinal) {
        return ItemStack.EMPTY;
    }
    
    @Override
    public boolean setStack(Fabric inv, int index, ItemStack stack) {
        return false;
    }

    @Override
    public int getMaxStackSize(Fabric inv) {
        return 0;
    }

    @Override
    public List<Lens> getChildren() {
        return Collections.<Lens>emptyList();
    }

    @Override
    public List<Lens> getSpanningChildren() {
        return Collections.<Lens>emptyList();
    }

    @Override
    public Lens getLens(int index) {
        return null;
    }

    @Override
    public Collection<InventoryProperty<?, ?>> getProperties(int index) {
        return Collections.<InventoryProperty<?, ?>>emptyList();
    }
    
    @Override
    public Collection<InventoryProperty<?, ?>> getProperties(Lens child) {
        return Collections.<InventoryProperty<?, ?>>emptyList();
    }

    @Override
    public boolean has(Lens lens) {
        return false;
    }

    @Override
    public boolean isSubsetOf(Collection<Lens> c) {
        return true;
    }
    
    @Override
    public Lens getParent() {
        return null;
    }

    @Override
    public Iterator<Lens> iterator() {
        // TODO
        return null;
    }

    @Override
    public List<SlotLens> getSlots() {
        return Collections.emptyList();
    }

    @Override
    public SlotLens getSlot(int ordinal) {
        return null;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public SlotLens getSlotLens(int ordinal) {
        return null;
    }

    @Override
    public String toString(int deep) {
        return "EmptyLens";
    }
}
