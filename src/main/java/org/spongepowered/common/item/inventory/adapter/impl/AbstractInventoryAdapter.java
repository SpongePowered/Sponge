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
package org.spongepowered.common.item.inventory.adapter.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.item.ItemStack;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.LensProvider;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.DefaultEmptyLens;
import org.spongepowered.common.item.inventory.lens.impl.DefaultIndexedLens;
import org.spongepowered.common.item.inventory.lens.impl.ReusableLens;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;
import org.spongepowered.common.item.inventory.lens.slots.SlotLens;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * Base Adapter implementation for {@link ItemStack} based Inventories.
 */
public class AbstractInventoryAdapter implements MinecraftInventoryAdapter {

    public static final Translation DEFAULT_NAME = new SpongeTranslation("inventory.default.title");


    protected final Fabric inventory;
    protected final SlotCollection slots;
    protected final Lens lens;
    protected final List<Inventory> children = new ArrayList<>();

    protected Inventory parent;
    @Nullable protected Inventory next;
    @Nullable private Iterable<Slot> slotIterator;

    public AbstractInventoryAdapter(Fabric inventory) {
        this(inventory, null, null);
    }

    @SuppressWarnings("unchecked")
    public <T extends Lens> AbstractInventoryAdapter(Fabric inventory, Class<T> lensType) {
        this.inventory = inventory;
        this.parent = this;
        if (inventory.getSize() == 0) {
            this.slots = new SlotCollection(0);
            this.lens = new DefaultEmptyLens(this);
        } else {
            ReusableLens<T> lens = ReusableLens.getLens(lensType, this, () -> this.initSlots(inventory, parent),
                    (slots) -> (T) new DefaultIndexedLens(0, inventory.getSize(), this, ((SlotCollection) slots)));
            this.slots = ((SlotCollection) lens.getSlots());
            this.lens = lens.getLens();
        }
    }

    public AbstractInventoryAdapter(Fabric inventory, @Nullable Lens root, @Nullable Inventory parent) {
        this.inventory = inventory;
        this.parent = parent == null ? this : parent;
        this.slots = this.initSlots(inventory, parent);
        this.lens = root != null ? root : checkNotNull(this.initRootLens(), "root lens");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private SlotCollection initSlots(Fabric inventory, @Nullable Inventory parent) {
        if (parent instanceof MinecraftInventoryAdapter) {
            SlotProvider sp = ((MinecraftInventoryAdapter) parent).getSlotProvider();
            if (sp instanceof SlotCollection) {
                return ((SlotCollection) sp);
            }
        }
        return new SlotCollection(inventory.getSize());
    }

    @Override
    public Inventory parent() {
        return this.parent;
    }

    @SuppressWarnings("unchecked")
    protected Lens initRootLens() {
        if (this instanceof LensProvider) {
            return ((LensProvider) this).rootLens(this.inventory, this);
        }
        int size = this.inventory.getSize();
        if (size == 0) {
            return new DefaultEmptyLens(this);
        }
        return new DefaultIndexedLens(0, size, this, this.slots);
    }

    @Override
    public SlotProvider getSlotProvider() {
        return this.slots;
    }

    @Override
    public Lens getRootLens() {
        return this.lens;
    }

    @Override
    public Fabric getFabric() {
        return this.inventory;
    }

    @Override
    public Inventory getChild(int index) {
        if (index < 0 || index >= this.lens.getChildren().size()) {
            throw new IndexOutOfBoundsException("No child at index: " + index);
        }
        while (index >= this.children.size()) {
            this.children.add(null);
        }
        Inventory child = this.children.get(index);
        if (child == null) {
            child = this.lens.getChildren().get(index).getAdapter(this.inventory, this);
            this.children.set(index, child);
        }
        return child;
    }

    @Override
    public Inventory getChild(Lens lens) {
        // TODO Auto-generated method stub
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Inventory> Iterable<T> slots() {
        if (this.slotIterator == null) {
            this.slotIterator = this.slots.getIterator(this);
        }
        return (Iterable<T>) this.slotIterator;
    }

    @Override
    public void clear() {
        this.slots().forEach(Inventory::clear);
    }

    public static Optional<Slot> forSlot(Fabric inv, SlotLens slotLens, Inventory parent) {
        return slotLens == null ? Optional.<Slot>empty() : Optional.<Slot>ofNullable((Slot) slotLens.getAdapter(inv, parent));
    }

    @Override
    public PluginContainer getPlugin() {
        if (this.parent != this) {
            return this.parent.getPlugin();
        }
        return null; // TODO - this should never return null.
    }
}
