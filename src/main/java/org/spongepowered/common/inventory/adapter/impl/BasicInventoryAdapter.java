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

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.item.ItemStack;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.bridge.item.inventory.InventoryBridge;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.lens.impl.DefaultEmptyLens;
import org.spongepowered.common.inventory.lens.impl.DefaultIndexedLens;
import org.spongepowered.common.inventory.lens.impl.ReusableLens;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensCollection;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensProvider;
import org.spongepowered.common.inventory.lens.slots.SlotLens;
import org.spongepowered.common.bridge.inventory.LensProviderBridge;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * Base Adapter implementation for {@link ItemStack} based Inventories.
 */
public class BasicInventoryAdapter implements InventoryAdapter, DefaultImplementedAdapterInventory, InventoryBridge, Inventory {

    public static final Translation DEFAULT_NAME = new SpongeTranslation("inventory.default.title");

    private final Fabric inventory;
    protected final SlotLensProvider slots;
    protected final Lens lens;
    protected final List<Inventory> children = new ArrayList<>();

    protected Inventory parent;
    @Nullable protected Inventory next;
    @Nullable private Iterable<Slot> slotIterator;

    public BasicInventoryAdapter(final Fabric inventory) {
        this(inventory, null, null);
    }

    @Override
    public Inventory root() {
        return this.parent() == this ? this : this.parent().root();
    }

    @SuppressWarnings("unchecked")
    public <T extends Lens> BasicInventoryAdapter(final Fabric inventory, final Class<T> lensType) {
        this.inventory = inventory;
        this.parent = this;
        if (inventory.fabric$getSize() == 0) {
            this.slots = new SlotLensCollection(0);
            this.lens = new DefaultEmptyLens(this);
        } else {
            final ReusableLens<T> lens = ReusableLens.getLens(lensType, this, () -> this.initSlots(inventory, this.parent),
                    (slots) -> (T) new DefaultIndexedLens(0, inventory.fabric$getSize(), this.getClass(), slots));
            this.slots = lens.getSlots();
            this.lens = lens.getLens();
        }
    }

    public BasicInventoryAdapter(final Fabric inventory, @Nullable final Lens root, @Nullable final Inventory parent) {
        this.inventory = inventory;
        this.parent = parent == null ? this : parent;
        this.slots = this.initSlots(inventory, parent);
        this.lens = root != null ? root : checkNotNull(this.initRootLens(), "root lens");
    }

    private SlotLensProvider initSlots(final Fabric inventory, @Nullable final Inventory parent) {
        if (parent instanceof InventoryAdapter) {
            return ((InventoryAdapter) parent).bridge$getSlotProvider();
        }
        return new SlotLensCollection(inventory.fabric$getSize());
    }

    @Override
    public Inventory parent() {
        return this.parent;
    }

    protected Lens initRootLens() {
        if (this instanceof LensProviderBridge) {
            return ((LensProviderBridge) this).bridge$rootLens(this.inventory, this);
        }
        final int size = this.inventory.fabric$getSize();
        if (size == 0) {
            return new DefaultEmptyLens(this);
        }
        return new DefaultIndexedLens(0, size, this.getClass(), this.slots);
    }

    @Override
    public SlotLensProvider bridge$getSlotProvider() {
        return this.slots;
    }

    @Override
    public Lens bridge$getRootLens() {
        return this.lens;
    }

    @Override
    public Fabric bridge$getFabric() {
        return this.inventory;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Inventory> Iterable<T> slots() {
        if (this.slotIterator == null) {
            this.slotIterator = SlotCollection.of(this, this);
        }
        return (Iterable<T>) this.slotIterator;
    }

    public static Optional<Slot> forSlot(final Fabric inv, final SlotLens slotLens, final Inventory parent) {
        return slotLens == null ? Optional.empty() : Optional.ofNullable((Slot) slotLens.getAdapter(inv, parent));
    }

    @Override
    public void clear() {
        // TODO clear without generating SlotAdapters
        this.slots().forEach(Inventory::clear);
    }

    @Override
    public PluginContainer getPlugin() {
        if (this.parent != this) {
            return this.parent.getPlugin();
        }
        return null; // TODO - this should never return null.
    }
}
