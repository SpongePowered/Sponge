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

public class Query {

    private final InventoryAdapter adapter;

    private final Fabric fabric;

    private final Lens lens;

    private final org.spongepowered.api.item.inventory.query.Query<?>[] queries;

    private Query(InventoryAdapter adapter, org.spongepowered.api.item.inventory.query.Query<?>[] queries) {
        this.adapter = adapter;
        this.fabric = adapter.getFabric();
        this.lens = adapter.getRootLens();
        this.queries = queries;
    }

    public Inventory execute() {
        if (this.matches(this.lens, null, this.fabric)) {
            return this.lens.getAdapter(this.fabric, this.adapter);
        }

        return this.toResult(this.reduce(this.lens, this.depthFirstSearch(this.lens)));
    }

    @SuppressWarnings("unchecked")
    private Inventory toResult(Set<Lens> matches) {
        if (matches.isEmpty()) {
            return new EmptyInventoryImpl(this.adapter);
        }
        if (matches.size() == 1) {
            return matches.iterator().next().getAdapter(this.fabric, this.adapter);
        }

        return new QueryResultAdapter(this.fabric, this.adapter, matches);
    }

    private Set<Lens> depthFirstSearch(Lens lens) {
        Set<Lens> matches = new LinkedHashSet<>();

        for (Lens child : lens.getChildren()) {
            if (child == null) {
                continue;
            }
            if (!child.getChildren().isEmpty()) {
                matches.addAll(this.depthFirstSearch(child));
            }
            if (this.matches(child, lens, this.fabric)) {
                matches.add(child);
            }
        }

        // Only a single match or no matches
        if (matches.size() < 2) {
            return matches;
        }

        return matches;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean matches(Lens lens, Lens parent, Fabric inventory) {
        for (org.spongepowered.api.item.inventory.query.Query<?> operation : this.queries) {
            if (((SpongeQueryOperation) operation).matches(lens, parent, inventory)) {
                return true;
            }
        }
        return false;
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

    public static Query compile(InventoryAdapter adapter, org.spongepowered.api.item.inventory.query.Query<?>... queries) {
        return new Query(adapter, queries);
    }

}
