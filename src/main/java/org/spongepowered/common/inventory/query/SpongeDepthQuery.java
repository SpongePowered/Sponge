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
import org.spongepowered.common.inventory.lens.impl.QueryLens;
import org.spongepowered.common.inventory.lens.slots.SlotLens;

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

    public abstract boolean matches(Lens lens, Lens parent, Inventory inventory);

    public Inventory execute(Inventory inventory, InventoryAdapter adapter) {
        Fabric fabric = adapter.bridge$getFabric();
        Lens lens = adapter.bridge$getRootLens();

        if (this.matches(lens, null, inventory)) {
            return (Inventory) lens.getAdapter(fabric, inventory);
        }

        return this.toResult(inventory, fabric, this.reduce(lens, this.depthFirstSearch(inventory, lens)));
    }

    private Inventory toResult(Inventory inventory, Fabric fabric, Set<Lens> matches) {
        if (matches.isEmpty()) {
            return new EmptyInventoryImpl(inventory);
        }
        if (matches.size() == 1) {
            return (Inventory) matches.iterator().next().getAdapter(fabric, inventory);
        }

        QueryLens lens = new QueryLens(matches);
        return (Inventory) lens.getAdapter(fabric, inventory);
    }

    private Set<Lens> depthFirstSearch(Inventory inventory, Lens lens) {
        Set<Lens> matches = new LinkedHashSet<>();

        for (Lens child : lens.getChildren()) {
            if (child == null) {
                continue;
            }
            if (!child.getChildren().isEmpty()) {
                matches.addAll(this.depthFirstSearch(inventory, child));
            }
            if (this.matches(child, lens, inventory)) {
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