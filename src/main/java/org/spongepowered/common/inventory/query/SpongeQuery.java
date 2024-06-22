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

import org.spongepowered.api.data.Key;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.query.Query;
import org.spongepowered.common.bridge.world.inventory.InventoryBridge;
import org.spongepowered.common.inventory.EmptyInventoryImpl;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.lens.impl.DelegatingLens;
import org.spongepowered.common.inventory.lens.impl.LensRegistrar;
import org.spongepowered.common.inventory.lens.impl.QueryLens;
import org.spongepowered.common.inventory.lens.impl.slot.QueriedSlotLens;
import org.spongepowered.common.inventory.lens.slots.SlotLens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class SpongeQuery implements Query {

    @Override
    public Inventory execute(Inventory inventory) {
        if (!(inventory instanceof InventoryBridge)) {
            throw new IllegalArgumentException("Unsupported Inventory! " + inventory.getClass().getName());
        }
        return this.execute(inventory, ((InventoryBridge) inventory).bridge$getAdapter());
    }

    public abstract Inventory execute(Inventory inventory, InventoryAdapter adapter);

    protected Map<Lens, Integer> reduce(Fabric fabric, Lens lens, Map<Lens, Integer> matches) {
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

        // Remove duplicate slot-lenses
        Map<SlotLens, Map<Key<?>, Object> > lenses = new LinkedHashMap<>();
        Map<Lens, Integer> toRemove = new HashMap<>();
        Map<SlotLens, List<Lens>> backingLenses = new HashMap<>();
        for (Map.Entry<Lens, Integer> entry : matches.entrySet()) {
            final Lens slotLens = entry.getKey();
            if (slotLens.slotCount() == 1) {
                // Remove Lens with one slot
                toRemove.put(slotLens, matches.get(slotLens));
                // Find SlotLens for that Lens
                final SlotLens sl = slotLens.getSlotLens(fabric, 0);
                final Lens parent = slotLens.getParent();
                final Map<Key<?>, Object> dataAt = parent == null ? Collections.emptyMap() : parent.getDataFor(slotLens);
                // Collect all data for the SlotLens
                lenses.computeIfAbsent(sl, k -> new HashMap<>()).putAll(dataAt);
                backingLenses.computeIfAbsent(sl, k -> new ArrayList<>()).add(slotLens);
            }
        }
        for (final Map.Entry<SlotLens, List<Lens>> entry : backingLenses.entrySet()) {
            if (entry.getValue().size() == 1) {
                toRemove.remove(entry.getValue().getFirst());
                lenses.remove(entry.getKey());
            }
        }
        // remove all single-slot lenses
        matches.keySet().removeAll(toRemove.keySet());
        for (Map.Entry<SlotLens, Map<Key<?>, Object>> entry : lenses.entrySet()) {
            final Map<Key<?>, Object> data = entry.getValue();
            if (data.isEmpty()) { // add back slot-lenses
                matches.put(entry.getKey(), toRemove.getOrDefault(entry.getKey(), 0));
            } else { // with data if found
                final QueriedSlotLens delegatingSlotLens = new QueriedSlotLens(entry.getKey(), data);
                matches.put(delegatingSlotLens, toRemove.getOrDefault(entry.getKey(), 0));
            }
        }


        // TODO maybe? Reduce when all matches are slots of this lens
        // TODO maybe? Reduce when subset of matches are child-lens of this lens

        return matches;
    }

    protected Inventory toResult(Inventory inventory, Fabric fabric, Map<Lens, Integer> matches) {
        if (matches.isEmpty()) {
            return new EmptyInventoryImpl(inventory);
        }
        if (matches.size() == 1) {
            final Map.Entry<Lens, Integer> entry = matches.entrySet().iterator().next();
            if (entry.getValue() == 0) {
                return entry.getKey().getAdapter(fabric, inventory);
            }
            final LensRegistrar.BasicSlotLensProvider slotProvider = new LensRegistrar.BasicSlotLensProvider(entry.getKey().slotCount());
            // TODO check correct slotprovider
            final DelegatingLens delegate = new DelegatingLens(entry.getValue(), entry.getKey(), slotProvider);
            return delegate.getAdapter(fabric, inventory);
        }

        final QueryLens lens = new QueryLens(matches, this);
        return lens.getAdapter(fabric, inventory);
    }
}
