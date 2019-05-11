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

import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.item.inventory.EmptyInventoryImpl;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.slots.SlotLens;
import org.spongepowered.common.item.inventory.query.result.QueryResultAdapter;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Checks immediate child for matches first.
 * If no match is found matching continues using a depth-first search.
 */
public abstract class SpongeDepthQuery extends SpongeQuery {

    public abstract boolean matches(Lens lens, Lens parent, Fabric inventory);

    public Inventory execute(InventoryAdapter adapter) {
        Fabric fabric = adapter.getFabric();
        Lens lens = adapter.getRootLens();

        if (this.matches(lens, null, fabric)) {
            return lens.getAdapter(fabric, adapter);
        }

        return this.toResult(adapter, fabric, this.reduce(lens, this.depthFirstSearch(fabric, lens)));
    }

    private Inventory toResult(InventoryAdapter adapter, Fabric fabric, Set<Lens> matches) {
        if (matches.isEmpty()) {
            return new EmptyInventoryImpl(adapter);
        }
        if (matches.size() == 1) {
            return matches.iterator().next().getAdapter(fabric, adapter);
        }

        return new QueryResultAdapter(fabric, adapter, matches);
    }

    private Set<Lens> depthFirstSearch(Fabric fabric, Lens lens) {
        Set<Lens> matches = new LinkedHashSet<>();

        for (Lens child : lens.getChildren()) {
            if (child == null) {
                continue;
            }
            if (!child.getChildren().isEmpty()) {
                matches.addAll(this.depthFirstSearch(fabric, child));
            }
            if (this.matches(child, lens, fabric)) {
                matches.add(child);
            }
        }

        return matches;
    }

    private Set<Lens> reduce(Lens lens, Set<Lens> matches) {
        List<SlotLens> lensSlots = lens.getSlots();
        Set<SlotLens> matchSlots = this.getSlots(matches);

        if (matches.isEmpty()) {
            return matches;
        }

        if (lensSlots.size() == matchSlots.size() && this.allLensesAreSlots(matches) && matchSlots.containsAll(lensSlots) ) {
            matches.clear();
            matches.add(lens);
            return matches;
        }

        for (Lens child : lens.getChildren()) {
            if (child == null || !child.isSubsetOf(matches)) {
                continue;
            }
            matches.removeAll(child.getChildren());
            matches.add(child);
        }

        return matches;
    }

    private boolean allLensesAreSlots(Set<Lens> lenses) {
        for (Lens lens : lenses) {
            if (!(lens instanceof SlotLens)) {
                return false;
            }
        }
        return true;
    }

    private Set<SlotLens> getSlots(Collection<Lens> lenses) {
        Set<SlotLens> slots = new HashSet<>();
        for (Lens lens : lenses) {
            slots.addAll(lens.getSlots());
        }
        return slots;
    }


}
