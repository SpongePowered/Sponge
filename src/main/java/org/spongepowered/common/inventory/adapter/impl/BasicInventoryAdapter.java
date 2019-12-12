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

import net.minecraft.item.ItemStack;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.common.bridge.inventory.InventoryBridge;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.lens.impl.LensRegistrar;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensProvider;
import org.spongepowered.common.inventory.lens.slots.SlotLens;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * Base Adapter implementation for {@link ItemStack} based Inventories.
 */
public class BasicInventoryAdapter implements InventoryAdapter, DefaultImplementedAdapterInventory, InventoryBridge, Inventory {

    private final Fabric fabric;
    protected final SlotLensProvider slotLenses;
    protected final Lens lens;
    @Nullable private SlotCollection slotCollection;

    @Nullable
    protected List<Inventory> children;

    protected Inventory parent;

    public BasicInventoryAdapter(final Fabric fabric, @Nullable final Lens root, @Nullable final Inventory parent) {
        this.fabric = fabric;
        this.parent = parent == null ? this : parent;
        this.slotLenses = this.initSlots(fabric, parent);
        this.lens = root != null ? root : LensRegistrar.getLens(this, this.slotLenses, fabric.fabric$getSize()) ;
    }

    private SlotLensProvider initSlots(final Fabric fabric, @Nullable final Inventory parent) {
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
            this.slotCollection = SlotCollection.of(this, this);
        }
        return this.slotCollection.slots();
    }

    @Override
    public List<Inventory> children() {
        // TODO react to lens changes?
        if (this.children == null) {
            this.children = this.impl$generateChildren();
        }
        return this.children;
    }

    public static Optional<Slot> forSlot(final Fabric fabric, final SlotLens slotLens, final Inventory parent) {
        return slotLens == null ? Optional.empty() : Optional.ofNullable((Slot) slotLens.getAdapter(fabric, parent));
    }

    @Override
    public void clear() {
        // TODO clear without generating SlotAdapters
        this.slots().forEach(Inventory::clear);
    }

}
