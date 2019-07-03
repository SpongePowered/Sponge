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
package org.spongepowered.common.item.inventory.adapter;

import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.common.item.inventory.adapter.impl.AbstractInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.slots.SlotLens;

import java.util.Optional;

/**
 * If {@link Inventory} represents the "idea" of an inventory, then an
 * {@link InventoryAdapter} represents the reality of one. All <i>Inventory
 * Adapters</i> provide a view of an inventory rooted at a specific
 * {@link Lens}, thus there is always an <i>Adapter</i> for every <i>Lens</i>.
 *  
 * <p>The key difference is that whilst lenses provide the <i>concept</i> of
 * "sub-inventories" and are a lightweight construct designed to represent an
 * internal hierarchy to allow it to be queried, whenever we want to <i>view</i>
 * part of that hierarchy as an actual {@link Inventory} instance (such as when
 * it matches a query), we always return the lens inside an <i>Adapter</i>.</p>
 * 
 * <p>The "top level" of any inventory hierarchy is always the actual inventory
 * itself, thus we can conclude from this that all inventories <i>are their own
 * adapters!</i> From this basic premise we can see that nothing should ever
 * directly implement the {@link Inventory} interface, "real" inventories should
 * always implement this interface instead.</p>
 */
public interface InventoryAdapter {
    
    SlotProvider bridge$getSlotProvider();

    Lens bridge$getRootLens();

    Fabric bridge$getFabric();

    default Optional<Slot> bridge$getSlot(final int ordinal) {
        final SlotLens slotLens = this.bridge$getRootLens().getSlotLens(ordinal);
        return AbstractInventoryAdapter.forSlot(this.bridge$getFabric(), slotLens, (Inventory) this);
    }
}
