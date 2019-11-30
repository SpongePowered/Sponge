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
import org.spongepowered.common.inventory.lens.slots.SlotLens;
import org.spongepowered.common.inventory.lens.MutableLensSet;
import org.spongepowered.common.inventory.lens.impl.collections.MutableLensSetImpl;
import org.spongepowered.common.inventory.query.result.MinecraftResultAdapterProvider;
import org.spongepowered.common.inventory.query.result.QueryResult;

import java.util.Collection;

public class Query {

    public interface ResultAdapterProvider {

        QueryResult getResultAdapter(Fabric inventory, MutableLensSet matches, Inventory parent);

    }

    private static org.spongepowered.common.inventory.query.Query.ResultAdapterProvider defaultResultProvider = new MinecraftResultAdapterProvider();

    private final InventoryAdapter adapter;

    private final Fabric inventory;

    private final Lens lens;

    private final org.spongepowered.api.item.inventory.query.Query<?>[] queries;

    private Query(final InventoryAdapter adapter, final org.spongepowered.api.item.inventory.query.Query<?>[] queries) {
        this.adapter = adapter;
        this.inventory = adapter.bridge$getFabric();
        this.lens = adapter.bridge$getRootLens();
        this.queries = queries;
    }

    @SuppressWarnings("unchecked")
    public Inventory execute() {
        return this.execute(Query.defaultResultProvider);
    }

    public Inventory execute(final org.spongepowered.common.inventory.query.Query.ResultAdapterProvider resultProvider) {
        if (this.matches(this.lens, null, this.inventory)) {
            return (Inventory) this.lens.getAdapter(this.inventory, (Inventory) this.adapter);
        }

        return this.toResult(resultProvider, this.reduce(this.lens, this.depthFirstSearch(this.lens)));
    }

    @SuppressWarnings("unchecked")
    private Inventory toResult(final org.spongepowered.common.inventory.query.Query.ResultAdapterProvider resultProvider, final MutableLensSet matches) {
        if (matches.isEmpty()) {
            return new EmptyInventoryImpl((Inventory) this.adapter);
        }
        if (matches.size() == 1) {
            return (Inventory) matches.getLens(0).getAdapter(this.inventory, (Inventory) this.adapter);
        }

        if (resultProvider != null) {
            return (Inventory) resultProvider.getResultAdapter(this.inventory, matches, (Inventory) this.adapter);
        }

        return (Inventory) Query.defaultResultProvider.getResultAdapter(this.inventory, matches, (Inventory) this.adapter);
    }

    private MutableLensSet depthFirstSearch(final Lens lens) {
        final MutableLensSet matches = new MutableLensSetImpl(true);

        for (final Lens child : lens.getChildren()) {
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

    @SuppressWarnings({"rawtypes"})
    private boolean matches(final Lens lens, final Lens parent, final Fabric inventory) {
        for (final org.spongepowered.api.item.inventory.query.Query<?> operation : this.queries) {
            if (((SpongeQuery) operation).matches(lens, parent, inventory)) {
                return true;
            }
        }
        return false;
    }

    private MutableLensSet reduce(final Lens lens, final MutableLensSet matches) {
        if (matches.isEmpty()) {
            return matches;
        }

        if (lens.getSlots().equals(this.getSlots(matches)) && this.allLensesAreSlots(matches)) {
            matches.clear();
            matches.add(lens);
            return matches;
        }

        for (final Lens child : lens.getChildren()) {
            if (child == null || !child.isSubsetOf(matches)) {
                continue;
            }
            matches.removeAll(child.getChildren());
            matches.add(child);
        }

        return matches;
    }

    private boolean allLensesAreSlots(final MutableLensSet lenses) {
        for (final Lens lens : lenses) {
            if (!(lens instanceof SlotLens)) {
                return false;
            }
        }
        return true;
    }

    private IntSet getSlots(final Collection<Lens> lenses) {
        final IntSet slots = new IntOpenHashSet();
        for (final Lens lens : lenses) {
            slots.addAll(lens.getSlots());
        }
        return slots;
    }

    public static Query compile(final InventoryAdapter adapter, final org.spongepowered.api.item.inventory.query.Query<?>... queries) {
        return new Query(adapter, queries);
    }

    public static void setDefaultResultProvider(final org.spongepowered.common.inventory.query.Query.ResultAdapterProvider defaultResultProvider) {
        Query.defaultResultProvider = defaultResultProvider;
    }

}
