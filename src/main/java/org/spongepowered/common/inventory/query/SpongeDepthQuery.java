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
package org.spongepowered.common.inventory.query;

import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.inventory.EmptyInventoryImpl;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.lens.impl.DelegatingLens;
import org.spongepowered.common.inventory.lens.impl.LensRegistrar;
import org.spongepowered.common.inventory.lens.impl.QueryLens;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Checks immediate child for matches first.
 * If no match is found matching continues using a depth-first search.
 */
public abstract class SpongeDepthQuery extends SpongeQuery {

    public abstract boolean matches(Lens lens, Lens parent, Inventory inventory);

    public Inventory execute(Inventory inventory, InventoryAdapter adapter) {
        Fabric fabric = adapter.inventoryAdapter$getFabric();
        Lens lens = adapter.inventoryAdapter$getRootLens();

        if (this.matches(lens, null, inventory)) {
            return lens.getAdapter(fabric, inventory);
        }

        return this.toResult(inventory, fabric, this.reduce(lens, this.depthFirstSearch(inventory, lens)));
    }

    private Inventory toResult(Inventory inventory, Fabric fabric, Map<Lens, Integer> matches) {
        if (matches.isEmpty()) {
            return new EmptyInventoryImpl(inventory);
        }
        if (matches.size() == 1) {
            final Map.Entry<Lens, Integer> entry = matches.entrySet().iterator().next();
            if (entry.getValue() == 0) {
                return entry.getKey().getAdapter(fabric, inventory);
            }
            final LensRegistrar.BasicSlotLensProvider slotProvider = new LensRegistrar.BasicSlotLensProvider(entry.getKey().slotCount());
            final DelegatingLens delegate = new DelegatingLens(entry.getValue(), entry.getKey(), slotProvider);
            return delegate.getAdapter(fabric, inventory);
        }

        QueryLens lens = new QueryLens(matches);
        return lens.getAdapter(fabric, inventory);
    }

    private Map<Lens, Integer> depthFirstSearch(Inventory inventory, Lens lens) {
        Map<Lens, Integer> matches = new LinkedHashMap<>();

        for (Lens child : lens.getChildren()) {
            if (child == null) {
                continue;
            }
            if (!child.getChildren().isEmpty()) {
                matches.putAll(this.depthFirstSearch(inventory, child));
            }
            if (this.matches(child, lens, inventory)) {
                matches.put(child, 0);
            }
        }


        if (lens.base() != 0 && !matches.isEmpty()) {
            matches.entrySet().forEach(entry -> entry.setValue(entry.getValue() + lens.base()));
        }

        return matches;
    }

    private Map<Lens, Integer> reduce(Lens lens, Map<Lens, Integer> matches) {
        if (matches.isEmpty()) {
            return Collections.emptyMap();
        }

        // Check if all matches are the direct children of this lens
        List<Lens> lensSlots = lens.getChildren();
        if (lensSlots.size() == matches.size() && matches.keySet().containsAll(lensSlots) ) {
            // return parent lens instead of constructing a new for the query result
            matches.clear();
            matches.put(lens, 0);
            return matches;
        }

        // TODO maybe? Reduce when all matches are slots of this lens
        // TODO maybe? Reduce when subset of matches are child-lens of this lens

        return matches;
    }
}