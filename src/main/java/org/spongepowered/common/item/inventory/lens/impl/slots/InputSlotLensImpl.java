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
package org.spongepowered.common.item.inventory.lens.impl.slots;

import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.slots.InputSlotAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.slots.InputSlotLens;

import java.util.function.Predicate;


public class InputSlotLensImpl extends FilteringSlotLensImpl implements InputSlotLens {

    public InputSlotLensImpl(int index) {
        this(index, (s) -> true, (s) -> true);
    }

    public InputSlotLensImpl(int index, Predicate<ItemStack> stackFilter, Predicate<ItemType> typeFilter) {
        this(index, InputSlotAdapter.class, stackFilter, typeFilter);
    }

    public InputSlotLensImpl(int index, Class<? extends Inventory> adapterType, Predicate<ItemStack> stackFilter, Predicate<ItemType> typeFilter) {
        super(index, adapterType, stackFilter, typeFilter);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public InventoryAdapter getAdapter(Fabric fabric, Inventory parent) {
        return new InputSlotAdapter(fabric, this, parent);
    }

}
