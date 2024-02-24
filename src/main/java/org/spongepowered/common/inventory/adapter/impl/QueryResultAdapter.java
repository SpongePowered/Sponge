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
package org.spongepowered.common.inventory.adapter.impl;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.common.bridge.world.inventory.InventoryBridge;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.lens.impl.LensRegistrar;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensProvider;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;


public class QueryResultAdapter implements InventoryAdapter, DefaultImplementedAdapterInventory, InventoryBridge, Inventory {

    private final Fabric fabric;
    protected final SlotLensProvider slotLenses;
    protected final Lens lens;
    private @Nullable SlotCollection slotCollection;

    protected @Nullable List<Inventory> children;

    protected Inventory parent;

    public QueryResultAdapter(final Fabric fabric, final @Nullable Lens root, final @Nullable Inventory parent) {
        this.fabric = fabric;
        this.parent = parent == null ? this : parent;
        this.slotLenses = this.initSlotsLenses(fabric, parent);
        this.lens = root != null ? root : LensRegistrar.getLens(this, this.slotLenses, fabric.fabric$getSize()) ;
    }

    private SlotLensProvider initSlotsLenses(final Fabric fabric, final @Nullable Inventory parent) {
        if (parent instanceof InventoryAdapter) {
            return ((InventoryAdapter) parent).inventoryAdapter$getSlotLensProvider();
        }
        return new LensRegistrar.BasicSlotLensProvider(fabric.fabric$getSize());
    }

    @Override
    public Inventory parent() {
        return this.parent;
    }

    @Override
    public SlotLensProvider inventoryAdapter$getSlotLensProvider() {
        return this.slotLenses;
    }

    @Override
    public Lens inventoryAdapter$getRootLens() {
        return this.lens;
    }

    @Override
    public Fabric inventoryAdapter$getFabric() {
        return this.fabric;
    }

    @Override
    public List<Slot> slots() {
        if (this.slotCollection == null) {
            this.slotCollection = SlotCollection.of(this.parent, this);
        }
        return this.slotCollection.slots();
    }

    @Override
    public List<Inventory> children() {
        if (this.children == null) {
            this.children = this.impl$generateChildren();
        }
        return this.children;
    }

    @Override
    public Optional<Slot> inventoryAdapter$getSlot(final int ordinal) {
        final List<Slot> slots = this.slots();
        if (ordinal >= slots.size()) {
            return Optional.empty();
        }
        return Optional.of(slots.get(ordinal));
    }

    @Override
    public void clear() {
        // TODO clear without generating SlotAdapters
        this.slots().forEach(Inventory::clear);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", QueryResultAdapter.class.getSimpleName() + "[", "]")
                .add("capacity=" + this.capacity())
                .add("children=" + this.children().size())
                .add("parent=" + ((this.parent == this) ? "self" : this.parent.getClass().getSimpleName()))
                .add("fabric=" + this.fabric.getClass().getSimpleName())
                .add("lens=" + this.lens.getClass().getSimpleName())
                .toString();
    }
}
