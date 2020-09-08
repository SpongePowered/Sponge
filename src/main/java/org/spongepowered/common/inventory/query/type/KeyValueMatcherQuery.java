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

import net.minecraft.inventory.container.Container;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.KeyValueMatcher;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.lens.impl.DelegatingLens;
import org.spongepowered.common.inventory.query.SpongeQuery;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unchecked")
public final class KeyValueMatcherQuery<T> extends SpongeQuery {

    private final KeyValueMatcher<T> matcher;

    public KeyValueMatcherQuery(KeyValueMatcher<T> matcher) {
        this.matcher = matcher;
    }

    @Override
    public Inventory execute(Inventory inventory, InventoryAdapter adapter) {
        final Fabric fabric = adapter.inventoryAdapter$getFabric();
        final Lens lens = adapter.inventoryAdapter$getRootLens();

        if (this.matches(lens, null, inventory)) {
            return lens.getAdapter(fabric, inventory);
        }

        return this.toResult(inventory, fabric, this.reduce(fabric, lens, this.search(inventory, lens)));
    }

    private Map<Lens, Integer> search(Inventory inventory, Lens lens) {
        if (inventory instanceof Container) {
            Map<Lens, Integer> matches = new LinkedHashMap<>();
            // Search for Container Slot properties
            for (net.minecraft.inventory.container.Slot slot : ((Container) inventory).inventorySlots) {
                if (this.matches(null, null, (Inventory) slot)) {
                    matches.put(((InventoryAdapter)slot).inventoryAdapter$getRootLens(), 0);
                }
            }
            if (!matches.isEmpty()) {
                return matches;
            }
            // Search for Container Viewed inventory properties
            Set<Inventory> viewedInventories = new HashSet<>();
            for (Slot slot : inventory.slots()) {
                viewedInventories.add(slot.viewedSlot().parent());
            }
            // TODO does this work?
            for (Inventory viewedInventory : viewedInventories) {
                if (this.matches(null, null, viewedInventory)) {
                    matches.put(((InventoryAdapter) viewedInventory).inventoryAdapter$getRootLens(), 0);
                }
            }
            if (!matches.isEmpty()) {
                this.delegateOffset(lens, matches);
                return matches;
            }
        }
        return this.depthLaterSearch(inventory, lens);
    }

    private Map<Lens, Integer> depthLaterSearch(Inventory inventory, Lens lens) {
        Map<Lens, Integer> matches = new LinkedHashMap<>();

        // Search for any match with the direct children
        for (Lens child : lens.getChildren()) {
            if (child == null) {
                continue;
            }
            if (this.matches(child, lens, inventory)) {
                matches.put(child, 0);
            }
        }
        // If no match was found go one layer deeper
        if (matches.isEmpty()) {
            for (Lens child : lens.getChildren()) {
                if (child == null) {
                    continue;
                }
                if (!child.getChildren().isEmpty()) {
                    matches.putAll(this.depthLaterSearch(inventory, child));
                }
            }
        }

        this.delegateOffset(lens, matches);

        return matches;
    }

    private void delegateOffset(Lens lens, Map<Lens, Integer> matches) {
        if (lens.base() != 0 && !matches.isEmpty() && lens instanceof DelegatingLens) {
            matches.entrySet().forEach(entry -> entry.setValue(entry.getValue() + lens.base()));
        }
    }

    public boolean matches(Lens lens, Lens parent, Inventory inventory) {
        final Key<? extends Value<T>> key = this.matcher.getKey();
        if (this.matcher.matches(inventory.get(key).orElse(null))) {
            return true;
        }
        if (parent == null) {
            return false;
        }
        // Check for lens properties
        final Object value = parent.getDataFor(lens).get(key);
        return this.matcher.matches((T) value);
    }

}
