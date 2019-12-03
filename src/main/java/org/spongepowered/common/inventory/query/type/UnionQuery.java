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

import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.bridge.inventory.InventoryBridge;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.lens.CompoundSlotProvider;
import org.spongepowered.common.inventory.lens.impl.CompoundLens;
import org.spongepowered.common.inventory.lens.impl.fabric.CompoundFabric;
import org.spongepowered.common.inventory.query.SpongeQuery;

public final class UnionQuery extends SpongeQuery {

    private final Inventory other;

    public UnionQuery(Inventory inventory) {
        this.other = inventory;
    }

    @Override
    public Inventory execute(Inventory inventory, InventoryAdapter adapter) {
        final CompoundLens.Builder lensBuilder = CompoundLens.builder().add(adapter.bridge$getRootLens());
        final CompoundFabric fabric = new CompoundFabric(adapter.bridge$getFabric(), ((InventoryBridge)this.other).bridge$getAdapter().bridge$getFabric());
        final CompoundSlotProvider provider = new CompoundSlotProvider().add(adapter);
        for (final Inventory inv : this.other.children()) {
            lensBuilder.add(((InventoryAdapter) inv).bridge$getRootLens());
            provider.add((InventoryAdapter) inv);
        }
        final CompoundLens lens = lensBuilder.build(provider);
        return (Inventory) lens.getAdapter(fabric, inventory);
    }


}
