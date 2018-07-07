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
import org.spongepowered.api.item.inventory.query.QueryOperation;
import org.spongepowered.common.item.inventory.EmptyInventoryImpl;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.MutableLensSet;
import org.spongepowered.common.item.inventory.lens.impl.collections.MutableLensSetImpl;
import org.spongepowered.common.item.inventory.lens.slots.SlotLens;
import org.spongepowered.common.item.inventory.query.result.MinecraftResultAdapterProvider;
import org.spongepowered.common.item.inventory.query.result.QueryResult;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Query {

    public interface ResultAdapterProvider {

        QueryResult getResultAdapter(Fabric inventory, MutableLensSet matches, Inventory parent);

    }

    private static ResultAdapterProvider defaultResultProvider = new MinecraftResultAdapterProvider();

    private final InventoryAdapter adapter;

    private final Fabric inventory;

    private final Lens lens;

    private final QueryOperation<?>[] queries;

    private Query(InventoryAdapter adapter, QueryOperation<?>[] queries) {
        this.adapter = adapter;
        this.inventory = adapter.getFabric();
        this.lens = adapter.getRootLens();
        this.queries = queries;
    }

    @SuppressWarnings("unchecked")
    public Inventory execute() {
        return this.execute(Query.defaultResultProvider);
    }

    public Inventory execute(ResultAdapterProvider resultProvider) {
        if (this.matches(this.lens, null, this.inventory)) {
            return this.lens.getAdapter(this.inventory, this.adapter);
        }

        return this.toResult(resultProvider, this.reduce(this.lens, this.depthFirstSearch(this.lens)));
    }

    @SuppressWarnings("unchecked")
    private Inventory toResult(ResultAdapterProvider resultProvider, MutableLensSet matches) {
        if (matches.isEmpty()) {
            return new EmptyInventoryImpl(this.adapter);
        }
        if (matches.size() == 1) {
            return matches.getLens(0).getAdapter(this.inventory, this.adapter);
        }

        if (resultProvider != null) {
            return resultProvider.getResultAdapter(this.inventory, matches, this.adapter);
        }

        return Query.defaultResultProvider.getResultAdapter(this.inventory, matches, this.adapter);
    }

    private MutableLensSet depthFirstSearch(Lens lens) {
        MutableLensSet matches = new MutableLensSetImpl(true);

        for (Lens child : lens.getChildren()) {
            if (child == null) {
                continue;
            }
            if (!child.getChildren().isEmpty()) {
                matches.addAll(this.depthFirstSearch(child));
            }
            if (this.matches(child, lens, this.inventory)) {
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
        for (QueryOperation<?> operation : this.queries) {
            if (((SpongeQueryOperation) operation).matches(lens, parent, inventory)) {
                return true;
            }
        }
        return false;
    }

    private MutableLensSet reduce(Lens lens, MutableLensSet matches) {
        List<SlotLens> lensSlots = lens.getSlots();
        Set<SlotLens> matchSlots = this.getSlots(matches);

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

    private boolean allLensesAreSlots(MutableLensSet lenses) {
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

    public static Query compile(InventoryAdapter adapter, QueryOperation<?>... queries) {
        return new Query(adapter, queries);
    }

    public static void setDefaultResultProvider(ResultAdapterProvider defaultResultProvider) {
        Query.defaultResultProvider = defaultResultProvider;
    }

}
