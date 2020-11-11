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
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.lens.impl.DelegatingLens;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Checks immediate child for matches first.
 * If no match is found matching continues using a depth-first search.
 */
public abstract class SpongeDepthQuery extends SpongeQuery {

    public abstract boolean matches(Lens lens, Lens parent, Inventory inventory);

    public Inventory execute(Inventory inventory, InventoryAdapter adapter) {
        final Fabric fabric = adapter.inventoryAdapter$getFabric();
        final Lens lens = adapter.inventoryAdapter$getRootLens();

        if (this.matches(lens, null, inventory)) {
            return lens.getAdapter(fabric, inventory);
        }

        return this.toResult(inventory, fabric, this.reduce(fabric, lens, this.depthFirstSearch(inventory, lens)));
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

        if (lens.base() != 0 && !matches.isEmpty() && lens instanceof DelegatingLens) {
            matches.entrySet().forEach(entry -> entry.setValue(entry.getValue() + lens.base()));
        }

        return matches;
    }
}