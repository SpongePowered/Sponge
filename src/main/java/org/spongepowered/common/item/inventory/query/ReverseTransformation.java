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
package org.spongepowered.common.item.inventory.query;

import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryTransformation;
import org.spongepowered.common.bridge.item.inventory.InventoryBridge;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.CompoundSlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.CompoundLens;
import org.spongepowered.common.item.inventory.query.operation.SlotLensQueryOperation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Reverses the slot order of an inventory.
 */
public class ReverseTransformation implements InventoryTransformation {

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Inventory transform(final Inventory inventory) {

        final List<InventoryAdapter> slots = new ArrayList<>();
        for (final Inventory slot : inventory.slots()) {
            slots.add(((InventoryAdapter) slot));
        }
        Collections.reverse(slots);

        final CompoundSlotProvider slotProvider = new CompoundSlotProvider();
        slots.forEach(slotProvider::add);

        final InventoryAdapter adapter = ((InventoryBridge) inventory).bridge$getAdapter();

        final CompoundLens lens = CompoundLens.builder().add(adapter.bridge$getRootLens()).build(slotProvider);

        final InventoryAdapter newAdapter = lens.getAdapter(adapter.bridge$getFabric(), inventory);

        return Query.compile(newAdapter,
                new SlotLensQueryOperation(ImmutableSet.of((Inventory) newAdapter))).execute();
    }
}
