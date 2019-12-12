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
package org.spongepowered.common.inventory.query.type;

import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.query.Query;
import org.spongepowered.api.item.inventory.query.QueryType;
import org.spongepowered.common.bridge.inventory.InventoryBridge;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.lens.CompoundSlotLensProvider;
import org.spongepowered.common.inventory.lens.impl.CompoundLens;
import org.spongepowered.common.inventory.query.SpongeQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Reverses the slot order of an inventory.
 */
public class ReverseQuery extends SpongeQuery implements QueryType.NoParam {

    private CatalogKey key = CatalogKey.sponge("reverse");

    @Override
    public CatalogKey getKey() {
        return this.key;
    }

    @Override
    public Inventory execute(Inventory parent, InventoryAdapter inventory) {

        List<Slot> slots = new ArrayList<>(((Inventory) inventory).slots());
        Collections.reverse(slots);

        CompoundSlotLensProvider slotProvider = new CompoundSlotLensProvider();
        slots.forEach(s -> slotProvider.add((InventoryAdapter) s));

        InventoryAdapter adapter = ((InventoryBridge) inventory).bridge$getAdapter();

        CompoundLens lens = CompoundLens.builder().add(adapter.inventoryAdapter$getRootLens()).build(slotProvider);

        return (Inventory) lens.getAdapter(adapter.inventoryAdapter$getFabric(), (Inventory) inventory);
    }

    @Override
    public Query toQuery() {
        return this;
    }
}
