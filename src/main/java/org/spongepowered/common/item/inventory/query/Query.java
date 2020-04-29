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

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
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

public class Query {

    public interface ResultAdapterProvider {

        QueryResult getResultAdapter(Fabric inventory, MutableLensSet matches, Inventory parent);

    }

    private static ResultAdapterProvider defaultResultProvider = new MinecraftResultAdapterProvider();

    private final InventoryAdapter adapter;

    private final Fabric inventory;

    private final Lens lens;

    private final QueryOperation<?>[] queries;

    private Query(final InventoryAdapter adapter, final QueryOperation<?>[] queries) {
        this.adapter = adapter;
        this.inventory = adapter.bridge$getFabric();
        this.lens = adapter.bridge$getRootLens();
        this.queries = queries;
    }

    @SuppressWarnings("unchecked")
    public Inventory execute() {
        return this.execute(Query.defaultResultProvider);
    }

    public Inventory execute(final ResultAdapterProvider resultProvider) {
        if (this.matches(this.lens, null, this.inventory)) {
            return (Inventory) this.lens.getAdapter(this.inventory, (Inventory) this.adapter);
        }

        return this.toResult(resultProvider, this.reduce(this.lens, this.depthFirstSearch(this.lens)));
    }

    @SuppressWarnings("unchecked")
    private Inventory toResult(final ResultAdapterProvider resultProvider, final MutableLensSet matches) {
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
        for (final QueryOperation<?> operation : this.queries) {
            if (((SpongeQueryOperation) operation).matches(lens, parent, inventory)) {
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

        if (matches.size() == 1) {
            return matches; // No need to reduce a single match
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

    public static Query compile(final InventoryAdapter adapter, final QueryOperation<?>... queries) {
        return new Query(adapter, queries);
    }

    public static void setDefaultResultProvider(final ResultAdapterProvider defaultResultProvider) {
        Query.defaultResultProvider = defaultResultProvider;
    }

}
